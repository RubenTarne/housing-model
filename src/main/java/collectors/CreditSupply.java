package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import housing.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class CreditSupply {

	private Config config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public CreditSupply(String outputFolder) {
	    outputFolderCopy = outputFolder;
		mortgageCounter = 0;
		ftbCounter = 0;
		btlCounter = 0;
		newDownPayment = 0.0;
		// TODO: This limit in the number of events taken into account to build statistics is not explained in the paper
        // TODO: (affects oo_lti, oo_ltv, btl_ltv, btl_icr, downpayments)
		setArchiveLength(10000);
	}

	/***
	 * collect information for this timestep
	 */
	public void step() {
        oldTotalCredit = totalOOCredit + totalBTLCredit;
        oldTotalDownPayment = totalBTLDownPayment + totalOODownPayment;
        totalOOCredit = 0.0;
        totalBTLCredit = 0.0;
        totalBTLDownPayment = 0.0;
        totalOODownPayment = 0.0;
        for(MortgageAgreement m : Model.bank.mortgages) {
        	if(m.isBuyToLet) {
            	totalBTLCredit += m.principal;
            	totalBTLDownPayment += m.downPayment;
        	} else {
        		totalOOCredit += m.principal;
        		totalOODownPayment += m.downPayment;
        	}
        }
        if (oldTotalCredit > 0.0) {
            netCreditGrowth = (totalOOCredit + totalBTLCredit - oldTotalCredit)/oldTotalCredit;
            netDownPaymentGrowth = (totalBTLDownPayment + totalOODownPayment - oldTotalDownPayment)/oldTotalDownPayment;
        } else {
            netCreditGrowth = 0;
            netDownPaymentGrowth = 0;
        }
        nApprovedMortgages = mortgageCounter;
        nFTBMortgages = ftbCounter;
        nBTLMortgages = btlCounter;
        mortgageCounter = 0;
        ftbCounter = 0;
        btlCounter = 0;
        newDownPaymentsApproved = newDownPayment;
        newDownPayment = 0.0;
        newPrincipalIssued = newPrincipalIssuedCounter;
        newPrincipalIssuedCounter = 0.0;
	}
	//TODO this is not newly issued credit, but total credit in the simulation at time 't'
	public double getNewlyIssuedCredit() {
		return newPrincipalIssued;
	}
	//TODO 
	public double getNewlyPaidDownPayments() {
		return newDownPaymentsApproved;
	}
	

	/***
	 * record information for a newly issued mortgage
	 * @param h
	 * @param approval
	 */
	public void recordLoan(Household h, MortgageAgreement approval, House house) {
		double housePrice;
		housePrice = approval.principal + approval.downPayment;
		// TODO: Check with Arzu, Marc if monthly gross income used here should include total income or just employment income (as of now)
		affordability = config.derivedParams.getAffordabilityDecay()*affordability +
				(1.0-config.derivedParams.getAffordabilityDecay())*approval.monthlyPayment/
				(h.getMonthlyGrossEmploymentIncome());
		// TODO: This condition is redundant, as the method is only called when approval.principal > 0
		if(approval.principal > 0.0) {
			if(approval.isBuyToLet) {
				btl_ltv.addValue(100.0*approval.principal/housePrice);
				double icr = Model.rentalMarketStats.getExpAvFlowYield()*approval.purchasePrice/
						(approval.principal*Model.centralBank.getInterestCoverRatioStressedRate(false));
				btl_icr.addValue(icr);
			} else {
				oo_ltv.addValue(100.0*approval.principal/housePrice);
				oo_lti.addValue(approval.principal/h.getAnnualGrossEmploymentIncome());
			}
			downpayments.addValue(approval.downPayment);
		}
		mortgageCounter += 1;
		newDownPayment += approval.downPayment;
		newPrincipalIssuedCounter += approval.principal;

		if(approval.isFirstTimeBuyer) ftbCounter += 1;
		if(approval.isBuyToLet) btlCounter += 1;

		downpayments.addValue(approval.downPayment);
		mortgageCounter += 1;
		if(approval.isFirstTimeBuyer) ftbCounter += 1;
		if(approval.isBuyToLet) btlCounter += 1;
	}
	
    //TODO: Check which of these functions should be kept and which removed!
	// ---- Mason stuff
	// ----------------
	
    public double [] getOOLTVDistribution() {return(oo_ltv.getValues());}
    public double [] getOOLTIDistribution() {return(oo_lti.getValues());}
    public double [] getBTLLTVDistribution() {return(btl_ltv.getValues());}
    public double [] getDownpaymentDistribution() {return(downpayments.getValues());}
    public double [] getBTLICRDistribution() {return(btl_icr.getValues());}
       
    public boolean getSaveOOLTVDistribution() { return(false);}
    public void setSaveOOLTVDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getOOLTVDistribution(),"ooLTVVals.csv");
    }
    public boolean getSaveBTLLTVDistribution() { return(false);}
    public void setSaveBTLLTVDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getBTLLTVDistribution(),"btlLTVVals.csv");
    }
    public boolean getSaveOOLTIDistribution() { return(false);}
    public void setSaveOOLTIDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getOOLTIDistribution(),"ooLTIVals.csv");
    }
    public boolean getSaveBTLICRDistribution() { return(false);}
    public void setSaveBTLICRDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getBTLICRDistribution(),"btlICRVals.csv");
    }
    

    public int getnRegisteredMortgages() { return(Model.bank.mortgages.size()); }

	public int getArchiveLength() {
		return archiveLength;
	}
	
	public void writeDistributionToFile(double [] vals, String filename) throws FileNotFoundException,
            UnsupportedEncodingException {
        PrintWriter dist = new PrintWriter(outputFolderCopy + filename, "UTF-8");
        if(vals.length > 0) {
        	dist.print(vals[0]);
        	for(int i=1; i<vals.length; ++i) {
        		dist.print(", "+vals[i]);
        	}
        }
        dist.close();
	}

	public void setArchiveLength(int archiveLength) {
		this.archiveLength = archiveLength;
		oo_lti = new DescriptiveStatistics(archiveLength);
		oo_ltv = new DescriptiveStatistics(archiveLength);
		btl_ltv = new DescriptiveStatistics(archiveLength);
		btl_icr = new DescriptiveStatistics(archiveLength);
		downpayments = new DescriptiveStatistics(archiveLength);
	}


	public int archiveLength; // number of mortgage approvals to remember
	public double affordability = 0.0;
	public DescriptiveStatistics oo_lti;
	public DescriptiveStatistics oo_ltv;
	public DescriptiveStatistics btl_ltv;
	public DescriptiveStatistics btl_icr;
	public DescriptiveStatistics downpayments; // TODO: This quantity only includes downpayments when the principal of the loan is > 0
	public int mortgageCounter;
	public int ftbCounter;	
	public int btlCounter;	
	public int nApprovedMortgages; // total number of new mortgages
	public int nFTBMortgages; // number of new first time buyer mortgages given
	public int nBTLMortgages; // number of new buy to let mortgages given
	public double totalBTLCredit = 0.0; // buy to let mortgage credit
	public double totalOOCredit = 0.0; // owner-occupier mortgage credit	
	public double netCreditGrowth; // rate of change of credit per month as percentage
	public double oldTotalCredit;
	
	public double totalBTLDownPayment = 0.0;
	public double totalOODownPayment = 0.0;
	public double netDownPaymentGrowth;
	public double oldTotalDownPayment;
	public double newDownPayment;
	public double newDownPaymentsApproved;
	private double newPrincipalIssuedCounter;
	private double newPrincipalIssued;

	

	private String outputFolderCopy;
}
