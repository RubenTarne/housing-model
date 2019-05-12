package housing;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.random.MersenneTwister;

/**************************************************************************************************
 * This represents a household who receives an income, consumes, saves and can buy, sell, let, and
 * invest in houses.
 *
 * @author daniel, davidrpugh, Adrian Carro
 *
 *************************************************************************************************/

public class Household implements IHouseOwner {

    //------------------//
    //----- Fields -----//
    //------------------//

    public static int          id_pool;

    public int                  id; // Only used for identifying households within the class TransactionRecorder
    private double              annualGrossEmploymentIncome;
    private double              monthlyGrossEmploymentIncome;
    private double 				monthlyDisposableIncome;
    private double				equityPosition;
    private double				consumption;
    private double				incomeConsumption;
    private double				financialWealthConsumption;
    private double				housingWealthConsumption;
    private double				debtConsumption;
    private double 				savingForDeleveraging;
    public HouseholdBehaviour   behaviour; // Behavioural plugin

    double                      incomePercentile; // Fixed for the whole lifetime of the household

    private House                           home;
    private Map<House, PaymentAgreement>    housePayments = new TreeMap<>(); // Houses owned and their payment agreements
    private Map<House, RentalAgreement> 	rentalContracts = new TreeMap<>(); // Houses rented out by this landlord and their payment agreements
    private Config                          config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister                 prng;
    private double                          age; // Age of the household representative person
    private double                          bankBalance;
    private double                          monthlyGrossRentalIncome; // Keeps track of monthly rental income, as only tenants keep a reference to the rental contract, not landlords
    private double                          savingRate; // (disposableIncome - nonEssentialConsumption)/grossTotalIncome
    private boolean                         isFirstTimeBuyer;
    private boolean                         isBankrupt;
    private double 							monthlyPayments;
    private double							principalPaidBack; // records how much of mortgage principal this household paid back this period
    private double							principalPaidBackForInheritance; // this records the repayment of principal the bequeather paid back, before passing its wealth on to this household
    private double							principalPaidBackDueToHouseSale; // when a household sells a house with a mortgage on, it gets recorded here
    private double							interestPaidBack; // records how much mortgage interest this household paid back this period
    private double							rentalPayment; // records the rental payment of this period
    private double							monthlyTaxesPaid; // records the monthly taxes paid (including tax relief)
    private double 							monthlyNICPaid; // records national insurance contributions
    private double 							cashInjection; // records the amount of cash injection when household goes bankrupt

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /**
     * Initialises behaviour (determine whether the household will be a BTL investor). Households start off in social
     * housing and with their "desired bank balance" in the bank
     */
    public Household(MersenneTwister prng, double age) {
        this.prng = prng; // Passes the Model's random number generator to a private field of each instance
        this.age = age;
        home = null;
        isFirstTimeBuyer = true;
        isBankrupt = false;
        id = ++id_pool;
        incomePercentile = this.prng.nextDouble();
        behaviour = new HouseholdBehaviour(this.prng, incomePercentile);
        // Find initial values for the annual and monthly gross employment income
        annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(age, incomePercentile);
        monthlyGrossEmploymentIncome = annualGrossEmploymentIncome/config.constants.MONTHS_IN_YEAR;
        bankBalance = data.Wealth.getDesiredBankBalance(getAnnualGrossTotalIncome(), behaviour.getPropensityToSave()); // Desired bank balance is used as initial value for actual bank balance
        // record deposits entering the simulation by initial endowment
        Model.householdStats.recordBankBalanceEndowment(bankBalance);
        monthlyGrossRentalIncome = 0.0;
        
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- General methods -----//

    /**
     * Main simulation step for each household. They age, receive employment and other forms of income, make their rent
     * or mortgage payments, perform an essential consumption, make non-essential consumption decisions, manage their
     * owned properties, and make their housing decisions depending on their current housing state:
     * - Buy or rent if in social housing
     * - Sell house if owner-occupier
     * - Buy/sell/rent out properties if BTL investor
     */
    public void step() {
        isBankrupt = false; // Delete bankruptcies from previous time step
    	// set payment counters and cashInjection to zero, so they can be updated 
    	principalPaidBack = 0.0;
    	principalPaidBackDueToHouseSale = 0.0;
    	interestPaidBack = 0.0; 
    	rentalPayment = 0.0;
    	cashInjection = 0.0;
    	monthlyPayments = 0.0;
    	// record bankBalance very beginning of period
    	Model.householdStats.recordBankBalanceVeryBeginningOfPeriod(bankBalance);
        // Update annual and monthly gross employment income
        annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(age, incomePercentile);
        monthlyGrossEmploymentIncome = annualGrossEmploymentIncome/config.constants.MONTHS_IN_YEAR;
    	// Add monthly disposable income (net total income minus essential consumption and housing expenses) to bank balance
    	monthlyDisposableIncome = getMonthlyDisposableIncome();
    	bankBalance += monthlyDisposableIncome;
    	// record bankBalance before consumption
    	Model.householdStats.recordBankBalanceBeforeConsumption(bankBalance);
    	// set the equityPosition of the household for the beginning of the period. This way HouseholdStats at the end of the period 
    	// does not recalculate the equity position with new HPI and different bank balances
    	setEquityPosition();
    	// Consume according to gross annual income and capped by current bank balance (after disposable income has been added)
    	consumption = behaviour.getDesiredConsumption(this, bankBalance, incomePercentile,
    			monthlyDisposableIncome); // Old implementation: if(isFirstTimeBuyer() || !isInSocialHousing()) bankBalance -= behaviour.getDesiredConsumption(getBankBalance(), getAnnualGrossTotalIncome());
    	bankBalance -= consumption;
    	// Compute saving rate
    	savingRate = (monthlyDisposableIncome - consumption)/getMonthlyGrossTotalIncome();
    	// Deal with bankruptcies
    	// TODO: Improve bankruptcy procedures (currently, simple cash injection), such as terminating contracts!
    	if (bankBalance < 0.0) {
    		setCashInjection(-bankBalance);
    		//System.out.println("household " + id +  " bankrupt. bankbalance: " + bankBalance + " and mDispIncome: " + monthlyDisposableIncome);
    		bankBalance = 1.0;
    		isBankrupt = true;
    	}
    	// Manage owned properties and close debts on previously owned properties. To this end, first, create an
    	// iterator over the house-paymentAgreement pairs at the household's housePayments object
    	Iterator<Entry<House, PaymentAgreement>> paymentIt = housePayments.entrySet().iterator();
    	Entry<House, PaymentAgreement> entry;
    	House h;
    	PaymentAgreement payment;
    	// Iterate over these house-paymentAgreement pairs...
    	while (paymentIt.hasNext()) {
    		entry = paymentIt.next();
    		h = entry.getKey();
    		payment = entry.getValue();
    		// ...if the household is the owner of the house, then manage it
    		if (h.owner == this) {
    			manageHouse(h);
    			// ...otherwise, if the household is not the owner nor the resident, then it is an old debt due to
    			// the household's inability to pay the remaining principal off after selling a property...
    		} else if (h.resident != this) {
    			MortgageAgreement mortgage = (MortgageAgreement) payment;
    			// ...remove this type of houses from payments as soon as the household pays the debt off
    			if ((payment.nPayments == 0) & (mortgage.principal == 0.0)) {
    				paymentIt.remove();
    			}
    		}
    	}
    	// Make housing decisions depending on current housing state
    	if (isInSocialHousing()) {
    		bidForAHome(); // When BTL households are born, they enter here the first time and until they manage to buy a home!
    	} else if (isRenting()) {
    		if (housePayments.get(home).nPayments == 0) { // End of rental period for this tenant
    			endTenancy();
    			bidForAHome();
    		}            
    	} else if (behaviour.isPropertyInvestor()) { // Only BTL investors who already own a home enter here
    		// BTL investors always bid the price corresponding to the maximum mortgage they could get
    		double price = Model.bank.getMaxMortgage(this, false, false);
    		Model.householdStats.countBTLBidsAboveExpAvSalePrice(price);
    		if (behaviour.decideToBuyInvestmentProperty(this)) {
    			Model.houseSaleMarket.bid(this, price, true);
    		}
    	} else if (!isHomeowner()){
    		System.out.println("Strange: this household is not a type I recognize");
    	}
    }

    
    // get the annual gross employment income depending if a trend or inequality  or neither are introduced
    private double setAnnualGrossEmploymentIncome() {
    	annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(age, incomePercentile);

    	if(config.trend && config.periodTrendStarting >= Model.getTime()) {
    		//TODO check if yearly to monthly percentage change is implemented correctly. Same is true for inequality below
    		annualGrossEmploymentIncome = annualGrossEmploymentIncome
    				*Math.pow((1+config.yearlyIncreaseEmploymentIncome/config.constants.MONTHS_IN_YEAR), (Model.getTime()-config.periodTrendStarting));
    	}
    	// implement rising income inequality
    	else if(config.risingIncomeInequality && Model.getTime() >= config.periodIncomeInequalityRises) {
    		// this implies that the top 5% receive additional income. 
    		if(incomePercentile >=0.95) { 
    			annualGrossEmploymentIncome = annualGrossEmploymentIncome*
    					Math.pow((1+config.yearlyInequalityIncrease/config.constants.MONTHS_IN_YEAR), (Model.getTime()-config.periodIncomeInequalityRises));
    		}
    	}
    	// return the annual gross employment income
    	return annualGrossEmploymentIncome;
    }
    
    /**
     * Subtracts the essential, necessary consumption and housing expenses (mortgage and rental payments) from the net
     * total income (employment income plus property income minus taxes)
     */
    public double getMonthlyDisposableIncome() {
        // Start with net monthly income
        monthlyDisposableIncome = getMonthlyNetTotalIncome();
        // Subtract essential, necessary consumption
        // TODO: ESSENTIAL_CONSUMPTION_FRACTION is not explained in the paper, all support is said to be consumed
        monthlyDisposableIncome -= config.ESSENTIAL_CONSUMPTION_FRACTION*config.GOVERNMENT_MONTHLY_INCOME_SUPPORT;
        // Subtract housing consumption
        for(PaymentAgreement payment: housePayments.values()) {
        	monthlyPayments += payment.makeMonthlyPayment(this);
//        	if(monthlyDisposableIncome < -1000) {
//        	//	System.out.println("MonthlyDisposableIncome is negative: " + monthlyDisposableIncome);
//        	}
        }
        // households in social housing pay rent equal to average local authority rent prices for England from 2013-14 
    	// https://data.london.gov.uk/dataset/local-authority-average-rents
        // = 82.44 pounds of WEEKLY rent 
        // TODO this is yearly rental cost of 3,957.12 pounds per year and can therefore be higher than private rents
        if(home==null) {
        	monthlyDisposableIncome -= 4*82.44;
        	// in case disposable income becomes negative, assume the government covering the rent, so households don't dissave
        	if(monthlyDisposableIncome<0) monthlyDisposableIncome=0;
        }
        monthlyDisposableIncome -= monthlyPayments;
        return monthlyDisposableIncome;
    }
    
    /**
     * Subtracts the monthly aliquot part of all due taxes from the monthly gross total income. Note that only income
     * tax on employment and rental income and national insurance contributions are implemented (no capital gains tax)!
     */
    public double getMonthlyNetTotalIncome() {
    	// Income tax (with finance costs tax relief)
        monthlyTaxesPaid = Model.government.incomeTaxDue(getAnnualGrossTotalIncome() - getAnnualFinanceCosts())/config.constants.MONTHS_IN_YEAR;
        // National insurance contributions
    	monthlyNICPaid = Model.government.class1NICsDue(annualGrossEmploymentIncome)/config.constants.MONTHS_IN_YEAR; 
    	
    	return getMonthlyGrossTotalIncome() - monthlyTaxesPaid - monthlyNICPaid;
    }

    /**
     * For the purpose of affordability checks for non-BTL households, a monthly net employment income is needed. This
     * makes sure that no rental income is accounted for, which non-BTL households can temporarily receive as a result
     * of temporarily renting out inherited properties while they manage to sell them. Thus, this subtracts the monthly
     * aliquot part of all due taxes (without any finance cost relief) from the monthly gross employment income
     * (ignoring any rental income). Note that only income tax on employment income and national insurance contributions
     * are implemented (no capital gains tax)!
     */
    double getMonthlyNetEmploymentIncome() {
        return getMonthlyGrossEmploymentIncome()
                - (Model.government.incomeTaxDue(annualGrossEmploymentIncome)  // Income tax
                + Model.government.class1NICsDue(annualGrossEmploymentIncome))  // National insurance contributions
                /config.constants.MONTHS_IN_YEAR;
    }

    /**
     * Adds up all interests paid on buy-to-let properties currently rented by this household, for the purpose of
     * obtaining tax relief on these costs. Note that this algorithm assumes buy-to-let investors always have interest
     * only mortgages, and that non BTL households inheriting properties never inherit any debt on these properties
     */
    private double getAnnualFinanceCosts() {
        double financeCosts = 0.0;
        for (Map.Entry<House, PaymentAgreement> entry : housePayments.entrySet()) {
            House house = entry.getKey();
            PaymentAgreement payment = entry.getValue();
            if (payment instanceof MortgageAgreement && house.owner == this && payment.nextPayment() != 0.0
                    && house.resident != null && house.resident.getHousePayments().get(house).nextPayment() != 0.0) {
                financeCosts += payment.nextPayment();
            }
        }
        return financeCosts*config.constants.MONTHS_IN_YEAR;
    }

    /**
     * Annualised gross total income, i.e., both employment and rental income
     */
    public double getAnnualGrossTotalIncome() { return getMonthlyGrossTotalIncome()*config.constants.MONTHS_IN_YEAR; }

    /**
     * Adds up all sources of (gross) income on a monthly basis, i.e., both employment and rental income
     */
    public double getMonthlyGrossTotalIncome() { return monthlyGrossEmploymentIncome + getMonthlyGrossRentalIncome(); }

    /**
     * Adds up this month's rental income from all currently owned and rented properties
     */
    public double getMonthlyGrossRentalIncome() {
        double monthlyGrossRentalIncome = 0.0;
        for(RentalAgreement rentalAgreement: rentalContracts.values()) {
            monthlyGrossRentalIncome += rentalAgreement.nextPayment();
        }
        return monthlyGrossRentalIncome;
    }

    //----- Methods for house owners -----//

    /**
     * Decide what to do with a house owned by the household:
     * - if the household lives in the house, decide whether to sell it or not
     * - if the house is up for sale, rethink its offer price, and possibly take it out of the sales market if price is
     *   below mortgage debt
     * - if the house is up for rent, rethink the rent demanded
     *
     * @param house A house owned by the household
     */
    private void manageHouse(House house) {
        // If house is for sale (on sale market)...
        HouseOfferRecord forSale = house.getSaleRecord();
        if (forSale != null) {
            // ...and it has not just been inherited...
            if (Model.getTime() > forSale.gettInitialListing()) {
                // ...then update its price, if the new price is above the mortgage debt on this house
                double newPrice = behaviour.rethinkHouseSalePrice(forSale);
                if (newPrice > mortgageFor(house).principal) {
                    Model.houseSaleMarket.updateOffer(forSale, newPrice);
                // ...otherwise, remove the offer from the sale market (note that investment properties will continue to be rented out)
                } else {
                    Model.houseSaleMarket.removeOffer(forSale);
                }
            }
        // Otherwise, if the house is not currently for sale, decide whether to sell it or not
        } else if (decideToSellHouse(house)) putHouseForSale(house);

        // If house is for rent (on rental market), and it has not just been inherited...
        HouseOfferRecord forRent = house.getRentalRecord();
        if (forRent != null && Model.getTime() > forRent.gettInitialListing()) {
            // ...then update its price
            double newPrice = behaviour.rethinkBuyToLetRent(forRent);
            Model.houseRentalMarket.updateOffer(forRent, newPrice);
        }        
    }

    /******************************************************
     * Having decided to sell house h, decide its initial sale price and put it up in the market.
     *
     * @param h the house being sold
     ******************************************************/
    private void putHouseForSale(House h) {
        double principal;
    	double initialSalePrice;

        MortgageAgreement mortgage = mortgageFor(h);
        if(mortgage != null) {
            principal = mortgage.principal;
        } else {
            principal = 0.0;
        }
        if (h == home) {
            Model.houseSaleMarket.offer(h, behaviour.getInitialSalePrice(h.getQuality(), principal), false);
        } else {
            initialSalePrice = behaviour.getInitialSalePrice(h.getQuality(), principal);
        	Model.houseSaleMarket.offer(h, initialSalePrice, true);
        	if(config.recordAgentDecisions 
        			&& (Model.getTime() >= config.TIME_TO_START_RECORDING) 
        			// to make sure that owner-occupiers inheriting a house and selling it do not get recorded
        			&& behaviour.isPropertyInvestor()) {
        		Model.agentDecisionRecorder.decideSellInvestmentProperty.println(initialSalePrice);
        	}
        }
    }

    /////////////////////////////////////////////////////////
    // Houseowner interface
    /////////////////////////////////////////////////////////

    /********************************************************
     * Do all the stuff necessary when this household
     * buys a house:
     * Give notice to landlord if renting,
     * Get loan from mortgage-lender,
     * Pay for house,
     * Put house on rental market if buy-to-let and no tenant.
     ********************************************************/
    void completeHousePurchase(HouseOfferRecord sale) {
        if(isRenting()) { // give immediate notice to landlord and move out
            if(sale.getHouse().resident != null) System.out.println("Strange: my new house has someone in it!");
            if(home == sale.getHouse()) {
                System.out.println("Strange: I've just bought a house I'm renting out");
            } else {
                endTenancy();
            }
        }
        MortgageAgreement mortgage = Model.bank.requestLoan(this, sale.getPrice(), behaviour.decideDownPayment(this,sale.getPrice()), home == null, sale.getHouse());
        if(mortgage == null) {
            // TODO: need to either provide a way for house sales to fall through or to ensure that pre-approvals are always satisfiable
            System.out.println("Can't afford to buy house: strange");
            System.out.println("Bank balance is "+bankBalance);
            System.out.println("Annual income is "+ monthlyGrossEmploymentIncome *config.constants.MONTHS_IN_YEAR);
            if(isRenting()) System.out.println("Is renting");
            if(isHomeowner()) System.out.println("Is homeowner");
            if(isInSocialHousing()) System.out.println("Is homeless");
            if(isFirstTimeBuyer()) System.out.println("Is firsttimebuyer");
            if(behaviour.isPropertyInvestor()) System.out.println("Is investor");
            System.out.println("House owner = "+ sale.getHouse().owner);
            System.out.println("me = "+this);
        } else {
            bankBalance -= mortgage.downPayment;
            housePayments.put(sale.getHouse(), mortgage);
            if (home == null) { // move in to house
                home = sale.getHouse();
                sale.getHouse().resident = this;
            } else if (sale.getHouse().resident == null) { // put empty buy-to-let house on rental market
                Model.houseRentalMarket.offer(sale.getHouse(), behaviour.buyToLetRent(sale.getQuality()),
                        false);
            } else {
                System.out.println("Strange: Bought a home with a resident");
            }
            isFirstTimeBuyer = false;
        }
    }

    /********************************************************
     * Do all stuff necessary when this household sells a house
     ********************************************************/
    public void completeHouseSale(HouseOfferRecord sale) {
        // First, receive money from sale
        bankBalance += sale.getPrice();
        // Second, find mortgage object and pay off as much outstanding debt as possible given bank balance
        MortgageAgreement mortgage = mortgageFor(sale.getHouse());
        bankBalance -= mortgage.payoff(bankBalance, this, true);
        // Third, if there is no more outstanding debt, remove the house from the household's housePayments object
        if (mortgage.nPayments == 0) {
            housePayments.remove(sale.getHouse());
            // TODO: Warning, if bankBalance is not enough to pay mortgage back, then the house stays in housePayments,
            // TODO: consequences to be checked. Looking forward, properties and payment agreements should be kept apart
        }
        // Fourth, if the house is still being offered on the rental market, withdraw the offer
        if (sale.getHouse().isOnRentalMarket()) {
            Model.houseRentalMarket.removeOffer(sale.getHouse().getRentalRecord());
        }
        // Fifth, if the house is the household's home, then the household moves out and becomes temporarily homeless...
        if (sale.getHouse() == home) {
            home.resident = null;
            home = null;
        // ...otherwise, if the house has a resident, it must be a renter, who must get evicted, also the rental income
        // corresponding to this tenancy must be subtracted from the owner's monthly rental income
        } else if (sale.getHouse().resident != null) {
        	rentalContracts.remove(sale.getHouse());
            sale.getHouse().resident.getEvicted();
        }
    }
    
    /**
     * A BTL investor receives this message when a tenant moves out of one of its buy-to-let houses. The household
     * simply puts the house back on the rental market.
     */
    @Override
    public void endOfLettingAgreement(House h, PaymentAgreement contract) {
        // TODO: Not sure if these checks are really needed here
        // Check that this household is the owner of the corresponding house
        if(!housePayments.containsKey(h)) {
            System.out.println("Strange: I don't own this house in endOfLettingAgreement");
        }
        // Check that the house is not currently being already offered in the rental market
        if(h.isOnRentalMarket()) System.out.println("Strange: got endOfLettingAgreement on house on rental market");
        // Remove the old rental contract from the landlord's list of rental contracts
        rentalContracts.remove(h);
        // Put house back on rental market
        Model.houseRentalMarket.offer(h, behaviour.buyToLetRent(h.getQuality()), false);
    }

    /**********************************************************
     * This household moves out of current rented accommodation
     * and becomes homeless (possibly temporarily). Move out,
     * inform landlord and delete rental agreement.
     **********************************************************/
    private void endTenancy() {
        home.resident = null;
        home.owner.endOfLettingAgreement(home, housePayments.get(home));
        housePayments.remove(home);
        home = null;
    }
    
    /*** Landlord has told this household to get out: leave without informing landlord */
    private void getEvicted() {
        if(home == null) {
            System.out.println("Strange: got evicted but I'm homeless");            
        }
        if(home.owner == this) {
            System.out.println("Strange: got evicted from a home I own");
        }
        housePayments.remove(home);
        home.resident = null;
        home = null;        
    }

    /**
     * Do everything necessary when this household moves in to rented accommodation, such as setting up a regular
     * payment contract.
     *
     * @return The rental agreement, for passing it to the landlord
     */
    RentalAgreement completeHouseRental(HouseOfferRecord sale) {
        // Check if renter same as owner, if renter already has a home and if the house is already occupied
        if (sale.getHouse().owner == this) System.out.println("Strange: I'm trying to rent a house I own!");
        if(home != null) System.out.println("Strange: I'm renting a house but not homeless");
        if(sale.getHouse().resident != null) System.out.println("Strange: tenant moving into an occupied house");
        // Create a new rental agreement with the agreed price and with a random length between a minimum and a maximum
        RentalAgreement rent = new RentalAgreement();
        rent.monthlyPayment = sale.getPrice();
        rent.nPayments = config.TENANCY_LENGTH_AVERAGE + prng.nextInt(2*config.TENANCY_LENGTH_EPSILON + 1)
                - config.TENANCY_LENGTH_EPSILON;
        // Add the rental agreement to the house payments object of the tenant household
        housePayments.put(sale.getHouse(), rent);
        // Set the house as the tenant's home and the tenant as the house's resident
        home = sale.getHouse();
        sale.getHouse().resident = this;
        // Return the rental agreement for passing it to the landlord
        return rent;
    }

    /********************************************************
     * Make the decision whether to bid on the housing market or rental market.
     * This is an "intensity of choice" decision (sigma function)
     * on the cost of renting compared to the cost of owning, with
     * COST_OF_RENTING being an intrinsic psychological cost of not
     * owning. 
     ********************************************************/
    private void bidForAHome() {
        // Find household's desired housing expenditure
        double desiredPurchasePrice = behaviour.getDesiredPurchasePrice(monthlyGrossEmploymentIncome);

        // Cap this expenditure to the maximum mortgage available to the household
        double price = Math.min(desiredPurchasePrice, Model.bank.getMaxMortgage(this, true, false));
		
        // write purchase Price in DECISION DATA SH output
		if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
			Model.agentDecisionRecorder.recordDesiredPurchasePriceSH(desiredPurchasePrice);
		}
        
        // Record the bid on householdStats for counting the number of bids above exponential moving average sale price
        Model.householdStats.countNonBTLBidsAboveExpAvSalePrice(price);
        // Compare costs to decide whether to buy or rent...
        if (behaviour.decideRentOrPurchase(this, price)) {
            // ... if buying, bid in the house sale market for the capped desired price
            Model.houseSaleMarket.bid(this, price, false);
        } else {
            // ... if renting, bid in the house rental market for the desired rent price
            Model.houseRentalMarket.bid(this, behaviour.desiredRent(monthlyGrossEmploymentIncome), false);
        }
    }

    /********************************************************
     * Decide whether to sell ones own house.
     ********************************************************/
    private boolean decideToSellHouse(House h) {
        if(h == home) {
            return(behaviour.decideToSellHome());
        } else {
            return(behaviour.decideToSellInvestmentProperty(h, this));
        }
    }

    /***
     * Do stuff necessary when BTL investor lets out a rental
     * property
     */
    @Override
    public void completeHouseLet(HouseOfferRecord sale, RentalAgreement rentalAgreement) {
        rentalContracts.put(sale.getHouse(), rentalAgreement);
    }

    /////////////////////////////////////////////////////////
    // Inheritance behaviour
    /////////////////////////////////////////////////////////

    /**
     * Implement inheritance: upon death, transfer all wealth to the previously selected household.
     *
     * Take all houses off the markets, evict any tenants, pay off mortgages, and give property and remaining
     * bank balance to the beneficiary.
     * @param beneficiary The household that will inherit the wealth
     */
    void transferAllWealthTo(Household beneficiary) {
        // Check if beneficiary is the same as the deceased household
        if (beneficiary == this) { // TODO: I don't think this check is really necessary
            System.out.println("Strange: I'm transferring all my wealth to myself");
            System.exit(0);
        }
        // Create an iterator over the house-paymentAgreement pairs at the deceased household's housePayments object
        Iterator<Entry<House, PaymentAgreement>> paymentIt = housePayments.entrySet().iterator();
        Entry<House, PaymentAgreement> entry;
        House h;
        PaymentAgreement payment;
        // Iterate over these house-paymentAgreement pairs
        while (paymentIt.hasNext()) {
            entry = paymentIt.next();
            h = entry.getKey();
            payment = entry.getValue();
            // If the deceased household owns the house, then...
            if (h.owner == this) {
                // ...first, withdraw the house from any market where it is currently being offered
                if (h.isOnRentalMarket()) Model.houseRentalMarket.removeOffer(h.getRentalRecord());
                if (h.isOnMarket()) Model.houseSaleMarket.removeOffer(h.getSaleRecord());
                // ...then, if there is a resident in the house...
                if (h.resident != null) {
                    // ...and this resident is different from the deceased household, then this resident must be a
                    // tenant, who must get evicted
                    if (h.resident != this) {
                        h.resident.getEvicted(); // TODO: Explain in paper that renters always get evicted, not just if heir needs the house
                    // ...otherwise, if the resident is the deceased household, remove it from the house
                    } else {
                        h.resident = null;
                    }
                }
                // ...finally, transfer the property to the beneficiary household
                beneficiary.inheritHouse(h, ((MortgageAgreement) payment).purchasePrice);
            // Otherwise, if the deceased household does not own the house but it is living in it, then it must have
            // been renting it: end the letting agreement
            } else if (h == home) {
                h.resident = null;
                home = null;
                h.owner.endOfLettingAgreement(h, housePayments.get(h));
            }
            // If payment agreement is a mortgage, then try to pay off as much as possible from the deceased household's bank balance
            if (payment instanceof MortgageAgreement) {
        		double payoff = ((MortgageAgreement) payment).payoff(beneficiary);
        		double bankBalanceBeforePayoff = bankBalance;
        		bankBalance -= payoff;
        		// record the payoff of principal at the beneficiaries, as it will otherwise not be recorded. 
        		// ... if the was higher than the bank balance, then record the amount that was paid off and the rest, which was not...
        		if(bankBalance < 0.0 && bankBalanceBeforePayoff >= 0.0) {
        			Model.householdStats.recordPrincipalRepaymentDeceasedHousehold(bankBalanceBeforePayoff);
        			//beneficiary.setPrincipalPaidBackForInheritance(bankBalanceBeforePayoff);
        			Model.householdStats.recordDebtReliefDeceasedHousehold(-bankBalance);
        		// ... if the bank balance was already negative, due to former payoff (like 2nd mortgage), record debt relief as the amount of the principal..
        		} else if(bankBalance < 0.0 && bankBalanceBeforePayoff < 0.0) {
        			Model.householdStats.recordDebtReliefDeceasedHousehold(payoff);
        		//... if the bankBalance is positive, this means no debt was relieved and the bankBalance can be transfered
        		//... record the principal paid back
        		} else {
        			Model.householdStats.recordPrincipalRepaymentDeceasedHousehold(payoff);
        			//beneficiary.setPrincipalPaidBackForInheritance(payoff);
        		}
        	}
        	// Remove the house-paymentAgreement entry from the deceased household's housePayments object
        	paymentIt.remove(); // TODO: Not sure this is necessary. Note, though, that this implies erasing all outstanding debt
        }
        // Finally, transfer all remaining liquid wealth to the beneficiary household
        beneficiary.bankBalance += Math.max(0.0, bankBalance);
    }
    
    /**
     * Inherit a house.
     *
     * Write off the mortgage for the house. Move into the house if renting or in social housing.
     * 
     * @param h House to inherit
     */
    private void inheritHouse(House h, double oldPurchasePrice) {
        // Create a null (zero payments) mortgage
        MortgageAgreement nullMortgage = new MortgageAgreement(this,false);
        nullMortgage.nPayments = 0;
        nullMortgage.downPayment = 0.0;
        nullMortgage.monthlyInterestRate = 0.0;
        nullMortgage.monthlyPayment = 0.0;
        nullMortgage.principal = 0.0; // If changed, trigger to re-try selling must be added to manageHouse, otherwise non-BTL households could keep BTL properties indefinitely
        nullMortgage.purchasePrice = oldPurchasePrice;
        // Become the owner of the inherited house and include it in my housePayments list (with a null mortgage)
        housePayments.put(h, nullMortgage);
        h.owner = this;
        // Check for residents in the inherited house
        if(h.resident != null) {
            System.out.println("Strange: inheriting a house with a resident");
            System.exit(0);
        }
        // If renting or homeless, move into the inherited house
        if(!isHomeowner()) {
            // If renting, first cancel my current tenancy
            if(isRenting()) {
                endTenancy();                
            }
            home = h;
            h.resident = this;
        // If owning a home and having the BTL gene...
        } else if(behaviour.isPropertyInvestor()) {
            // ...decide whether to sell the inherited house
            if(decideToSellHouse(h)) {
                putHouseForSale(h);
            }
            // ...and put it to rent (temporarily, if trying to sell it, or permanently, if not trying to sell it)
            Model.houseRentalMarket.offer(h, behaviour.buyToLetRent(h.getQuality()), false);
        // If being an owner-occupier, put inherited house for sale and also for rent temporarily
        } else {
            putHouseForSale(h);
            Model.houseRentalMarket.offer(h, behaviour.buyToLetRent(h.getQuality()), false);
        }
    }

    //----- Helpers -----//

    public double getAge() { return age; }
    
    public double getConsumption() {return consumption; }
    
    public double getIncomeConsumption() { return incomeConsumption;}
    
    public void setIncomeConsumption(double incomeConsumption) {this.incomeConsumption = incomeConsumption; }

    public double getFinancialWealthConsumption() { return financialWealthConsumption; }
    
    public void setFinancialWealthConsumption(double financialWealthConsumption) {
    	this.financialWealthConsumption = financialWealthConsumption; }

    public double getHousingWealthConsumption()	{ return housingWealthConsumption; }
    
    public void setHousingWealthConsumption(double housingWealthConsumption) {
    	this.housingWealthConsumption = housingWealthConsumption;
    }
    
    public double getDebtConsumption()	{ return debtConsumption; }
    
    public void setDebtConsumption(double debtConsumption) { this.debtConsumption = debtConsumption; }
    
    public double getSavingForDeleveraging() {
		return savingForDeleveraging;
	}

	public void setSavingForDeleveraging(double savingForDeleveraging) {
		this.savingForDeleveraging = savingForDeleveraging;
	}
    void ageOneMonth() { age += 1.0/config.constants.MONTHS_IN_YEAR; }

    public boolean isHomeowner() {
        if(home == null) return(false);
        return(home.owner == this);
    }

    public boolean isRenting() {
        if(home == null) return(false);
        return(home.owner != this);
    }

    public boolean isInSocialHousing() { return home == null; }

    public boolean isFirstTimeBuyer() { return isFirstTimeBuyer; }

    public boolean isBankrupt() { return isBankrupt; }

    public double getBankBalance() { return bankBalance; }

    public House getHome() { return home; }

    public Map<House, PaymentAgreement> getHousePayments() { return housePayments; }

    public double getAnnualGrossEmploymentIncome() { return annualGrossEmploymentIncome; }

    public double getMonthlyGrossEmploymentIncome() { return monthlyGrossEmploymentIncome; }
    
	public double returnMonthlyDisposableIncome() { return monthlyDisposableIncome; }
	
	public double returnMonthlyGrossRentalIncome() { return monthlyGrossRentalIncome; }

    /***
     * @return Number of properties this household currently has on the sale market
     */
    public int nPropertiesForSale() {
        int n=0;
        for(House h : housePayments.keySet()) {
            if(h.isOnMarket()) ++n;
        }
        return(n);
    }

    /**
     * Counts the number of houses owned by this household. By definition of the model, all households owning property
     * do also own a home, i.e ., they live in one of their owned properties and thus only non-homeless households can
     * own any property. As a consequence, the number of houses owned by renters and households in social housing is 0
     *
     * @return Number of houses owned by this household
     */
    public int getNProperties() {
        if (isHomeowner()) {
            return housePayments.size();
        } else {
            return 0;
        }
    }

    /***
     * @return Current mark-to-market (with exponentially averaged prices per quality) equity in this household's home.
     */
    double getHomeEquity() {
        if(!isHomeowner()) return(0.0);
        return Model.housingMarketStats.getExpAvSalePriceForQuality(home.getQuality())
                - mortgageFor(home).principal;
    }
    
    // method for the consumption function in householdBehaviour to get house and investment mark-to-market value
    public double getPropertyValue() {
    	double totalValue = 0.0;
    	if(!isHomeowner())return(totalValue); //BTL investors are always homeowners as well
    	// add value of home
    	totalValue += Model.housingMarketStats.getExpAvSalePriceForQuality(home.getQuality());
    	// add value of all investment properties
    	if(getNProperties() > 1) {
    		for (House h: housePayments.keySet()) {
                if (h.owner == this && h.resident !=this) {
                	totalValue += Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality());	
                }
    		}
    	}
    	return totalValue;
}
    
    // get investment property equity for BTL investors
    double getInvestmentEquity() {
    	if(getNProperties() > 0) {
    		// if house is owned by investor AND it is not the home then
    		double investmentEquity = 0.0;
    		for (House h: housePayments.keySet()) {
                if (h.owner == this && h.resident !=this) {
                	investmentEquity += Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality())
                			- mortgageFor(h).principal;
                }
    		}
    		return investmentEquity;
    	} 
    	else { return 0.0;}
    }
    
    // method for the consumption function in householdBehaviour to get total debt of the household.
    // returns a negative value
    public double getTotalDebt() {
    	double totalDebt = 0.0;
    	// TODO check if households without a home can have outstanding loans under any circumstances
    	if(!isHomeowner())return(totalDebt);
    	// add principal outstanding on home
    	totalDebt -= mortgageFor(home).principal;
    	if(getNProperties() > 0) {
    		// if house is owned by investor AND it is not the home then..
    		for (House h: housePayments.keySet()) {
                if (h.owner == this && h.resident !=this) {
                	totalDebt -= mortgageFor(h).principal;
                }
    		}
    		return totalDebt;
    	} 
    	
    	return totalDebt;
    }
    
    // getter for the total equity position
    public double getEquityPosition() {
    	return equityPosition;
    }
    // set equity position to the value at the beginning of the period, this way collectors 
    public void setEquityPosition() {
    	this.equityPosition = getBankBalance() + getInvestmentEquity() + getHomeEquity();
    }
    
    
    public MortgageAgreement mortgageFor(House h) {
        PaymentAgreement payment = housePayments.get(h);
        if(payment instanceof MortgageAgreement) {
            return((MortgageAgreement)payment);
        }
        return(null);
    }

    public double monthlyPaymentOn(House h) {
        PaymentAgreement payment = housePayments.get(h);
        if(payment != null) {
            return(payment.monthlyPayment);
        }
        return(0.0);        
    }

    public double getSavingRate() { return savingRate; }
    
    public double getIncomePercentile() {return incomePercentile;}
    
    public double getMonthlyPayments() {return monthlyPayments;}

	public double getPrincipalPaidBack() {
		return principalPaidBack;
	}

	public void setPrincipalPaidBack(double principalPaidBack) {
		this.principalPaidBack += principalPaidBack;
	}
	
	public double getPrincipalDueToHouseSale() {
		return principalPaidBackDueToHouseSale;
	}
	
	public void setPrincipalDueToHouseSale(double principalPaidBack) {
		this.principalPaidBackDueToHouseSale += principalPaidBack;
	}

	public double getInterestPaidBack() {
		return interestPaidBack;
	}

	public void setInterestPaidBack(double interestPaidBack) {
		this.interestPaidBack += interestPaidBack;
	}

	public double getRentalPayment() {
		return rentalPayment;
	}

	public void setRentalPayment(double rentalPayment) {
		this.rentalPayment = rentalPayment;
	}

	public double getCashInjection() {
		return cashInjection;
	}

	public void setCashInjection(double cashInjection) {
		this.cashInjection = cashInjection;
	}

	public double getPrincipalPaidBackForInheritance() {
		return principalPaidBackForInheritance;
	}

	public void setPrincipalPaidBackForInheritance(double principalPaidBackForInheritance) {
		this.principalPaidBackForInheritance = principalPaidBackForInheritance;
	}

	public double getMonthlyTaxesPaid() {
		return monthlyTaxesPaid;
	}

	public double getMonthlyNICPaid() {
		return monthlyNICPaid;
	}
	

}
