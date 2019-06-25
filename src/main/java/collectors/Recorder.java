package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import housing.Model;

/**************************************************************************************************
 * Class to write output to files
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class Recorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String outputFolder;

    private PrintWriter outfile;
    private PrintWriter qualityBandPriceFile;
    // writes the expected price per quality, i.e. adjusted 
    private	PrintWriter qualityBandPriceExpectedFile;
    
    private PrintWriter HPI;
    private PrintWriter top10NetTotalWealthShare;
    private PrintWriter palmerIndex;
    private PrintWriter numberBankruptcies;
    private PrintWriter shareEmptyHouses;
    private PrintWriter BTLMarketShare;
    private PrintWriter financialWealth;
    private PrintWriter incomeConsumption;
    private PrintWriter financialConsumption;
    private PrintWriter grossHousingWealthConsumption;
    private PrintWriter debtConsumption;
    private PrintWriter savingDeleveraging;
    private PrintWriter consumptionToIncome;
    private PrintWriter ooLTI;
    private PrintWriter btlLTV;
    private PrintWriter creditGrowth;
    private PrintWriter debtToIncome;
    private PrintWriter ooDebtToIncome;
    private PrintWriter mortgageApprovals;
    private PrintWriter housingTransactions;
    private PrintWriter advancesToFTBs;
    private PrintWriter advancesToBTL;
    private PrintWriter advancesToHomeMovers;
    private PrintWriter priceToIncome;
    private PrintWriter rentalYield;
    private PrintWriter housePriceGrowth;
    private PrintWriter interestRateSpread;
    
    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public Recorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openMultiRunFiles(boolean recordCoreIndicators) {
        // If recording of core indicators is active...
        if(recordCoreIndicators) {
            // ...try opening necessary files
            try {
            	HPI = new PrintWriter(outputFolder + "coreIndicator-HPI.csv",
                		"UTF-8");
            	top10NetTotalWealthShare = new PrintWriter(outputFolder + "coreIndicator-top10NetTotalWealthShare.csv",
                		"UTF-8");
            	palmerIndex = new PrintWriter(outputFolder + "coreIndicator-palmerIndex.csv",
                		"UTF-8");
            	numberBankruptcies = new PrintWriter(outputFolder + "coreIndicator-numberBankruptcies.csv",
                		"UTF-8");
            	shareEmptyHouses = new PrintWriter(outputFolder + "coreIndicator-shareEmptyHouses.csv",
                		"UTF-8");
            	BTLMarketShare = new PrintWriter(outputFolder + "coreIndicator-BTLMarketShare.csv",
                		"UTF-8");
            	financialWealth = new PrintWriter(outputFolder + "coreIndicator-financialWealth.csv",
                		"UTF-8");
            	incomeConsumption = new PrintWriter(outputFolder + "coreIndicator-incomeConsumption.csv",
                		"UTF-8");
            	financialConsumption = new PrintWriter(outputFolder + "coreIndicator-financialConsumption.csv",
                		"UTF-8");
            	grossHousingWealthConsumption = new PrintWriter(outputFolder + "coreIndicator-grossHousingWealthConsumption.csv",
                		"UTF-8");
            	debtConsumption = new PrintWriter(outputFolder + "coreIndicator-debtConsumption.csv",
                		"UTF-8");
            	savingDeleveraging = new PrintWriter(outputFolder + "coreIndicator-savingDeleveraging.csv",
                		"UTF-8");           	
            	consumptionToIncome = new PrintWriter(outputFolder + "coreIndicator-consumptionToIncome.csv",
                		"UTF-8");
                ooLTI = new PrintWriter(outputFolder + "coreIndicator-ooLTI.csv",
                        "UTF-8");
                btlLTV = new PrintWriter(outputFolder + "coreIndicator-btlLTV.csv",
                        "UTF-8");
                creditGrowth = new PrintWriter(outputFolder + "coreIndicator-creditGrowth.csv",
                        "UTF-8");
                debtToIncome = new PrintWriter(outputFolder + "coreIndicator-debtToIncome.csv",
                        "UTF-8");
                ooDebtToIncome = new PrintWriter(outputFolder + "coreIndicator-ooDebtToIncome.csv",
                        "UTF-8");
                mortgageApprovals = new PrintWriter(outputFolder + "coreIndicator-mortgageApprovals.csv",
                        "UTF-8");
                housingTransactions = new PrintWriter(outputFolder + "coreIndicator-housingTransactions.csv",
                        "UTF-8");
                advancesToFTBs = new PrintWriter(outputFolder + "coreIndicator-advancesToFTB.csv",
                        "UTF-8");
                advancesToBTL = new PrintWriter(outputFolder + "coreIndicator-advancesToBTL.csv",
                        "UTF-8");
                advancesToHomeMovers = new PrintWriter(outputFolder + "coreIndicator-advancesToMovers.csv",
                        "UTF-8");
                priceToIncome = new PrintWriter(outputFolder + "coreIndicator-priceToIncome.csv",
                        "UTF-8");
                rentalYield = new PrintWriter(outputFolder + "coreIndicator-rentalYield.csv",
                        "UTF-8");
                housePriceGrowth = new PrintWriter(outputFolder + "coreIndicator-housePriceGrowth.csv",
                        "UTF-8");
                interestRateSpread = new PrintWriter(outputFolder + "coreIndicator-interestRateSpread.csv",
                        "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void openSingleRunFiles(int nRun, boolean recordOutfile, boolean recordQualityBandPrice, int nQualityBands) {
        // Try opening general output file and write first row header with column names
    	if (recordOutfile) {
    		try {
    			outfile = new PrintWriter(outputFolder + "Output-run" + nRun + ".csv", "UTF-8");
    			outfile.println("Model time, "
    					// Number of households of each type
    					+ "nNonBTLHomeless, nBTLHomeless, nSocialHousing, nRenting, nNonOwner, "
    					+ "nNonBTLOwnerOccupier, nBTLOwnerOccupier, nOwnerOccupier, nActiveBTL, nBTL, nNonBTLBankrupt, "
    					+ "nBTLBankrupt, TotalPopulation, "
    					// Numbers of houses of each type
    					+ "HousingStock, nNewBuild, nUnsoldNewBuild, nEmptyHouses, BTLStockFraction, "
    					// House sale market data
    					+ "Sale HPI, Sale AnnualHPA, Sale AvBidPrice, Sale AvOfferPrice, Sale AvSalePrice, "
    					+ "Sale ExAvSalePrice, Sale AvMonthsOnMarket, Sale ExpAvMonthsOnMarket, Sale nBuyers, "
    					+ "Sale nBTLBuyers, Sale nSellers, Sale nNewSellers, Sale nBTLSellers, Sale nSales, "
    					+ "Sale nNonBTLBidsAboveExpAvSalePrice, Sale nBTLBidsAboveExpAvSalePrice, Sale nSalesToBTL, "
    					+ "Sale nSalesToFTB, "
    					// Rental market data
    					+ "Rental HPI, Rental AnnualHPA, Rental AvBidPrice, Rental AvOfferPrice, Rental AvSalePrice, "
    					+ "Rental AvMonthsOnMarket, Rental ExpAvMonthsOnMarket, Rental nBuyers, Rental nSellers, "
    					+ "Rental nSales, Rental ExpAvFlowYield, "
    					// Credit data
    					+ "nRegisteredMortgages, "
    					//RUBEN additional variables
    					+ "BankBalancesVeryBeginningOfPeriod, monthlyTotalGrossIncome, monthlyTotalNetIncome, monthlyGrossEmploymentIncome, monthlyTaxesPaid, "
    					+ "monthlyInsurancePaid, BankBalancesBeforeConsumption, BankBalancesEndowed, "
    					+ "totalConsumption, totalIncomeConsumption, totalFinancialWealthConsumption, "
    					+ "totalHousingWealthConsumption, totalDebtConsumption, totalSavingForDeleveraging, totalSaving, totalCredit, "
    					+ "totalPrincipalRepayment, totalPrincipalRepaymentsDueToHouseSale, totalPrincipalPaidBackForInheritance, totalInterestRepayment, totalRentalPayments, "
    					+ "totalBankruptcyCashInjection, totalDebtReliefDueToDeceasedHousehold, "
    					+ "creditSupplyTarget, newlyPaidDownPayments, newlyIssuedCredit, nNegativeEquity, "
    					+ "LTV FTB, LTV OO, LTV BTL, interestRateSpread, moneyOutflowToConstructionSector");
    		} catch (FileNotFoundException | UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
    	}
        // If recording of quality band prices is active...
        if(recordQualityBandPrice) {
            // ...try opening output file and write first row header with column names
            try {
                qualityBandPriceFile = new PrintWriter(outputFolder + "QualityBandPrice-run" + nRun + ".csv", "UTF-8");
                StringBuilder str = new StringBuilder();
                str.append(String.format("Time, Q%d", 0));
                for (int i = 1; i < nQualityBands; i++) {
                    str.append(String.format(", Q%d", i));
                }
                qualityBandPriceFile.println(str);
                
                // .. try opening output file for the adjusted  prices per quality and write the first row with column names
                qualityBandPriceExpectedFile = new PrintWriter(outputFolder + "QualityBandPriceExpected-run" + nRun + ".csv", "UTF-8");
                qualityBandPriceExpectedFile.println(str);
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeTimeStampResults(boolean recordOutfile, boolean recordCoreIndicators, int time, boolean recordQualityBandPrice) {
        if (recordCoreIndicators) {
            // If not at the first point in time...
            if (time > 0) {
                // ...write value separation for core indicators (except for time 0)
            	HPI.print(", ");
            	top10NetTotalWealthShare.print(", ");
            	palmerIndex.print(", ");
            	numberBankruptcies.print(", ");
            	shareEmptyHouses.print(", ");
            	BTLMarketShare.print(", ");
                financialWealth.print(", ");
                incomeConsumption.print(", ");
                financialConsumption.print(", ");
                grossHousingWealthConsumption.print(", ");
                debtConsumption.print(", ");
                savingDeleveraging.print(", ");
            	consumptionToIncome.print(", ");
                ooLTI.print(", ");
                btlLTV.print(", ");
                creditGrowth.print(", ");
                debtToIncome.print(", ");
                ooDebtToIncome.print(", ");
                mortgageApprovals.print(", ");
                housingTransactions.print(", ");
                advancesToFTBs.print(", ");
                advancesToBTL.print(", ");
                advancesToHomeMovers.print(", ");
                priceToIncome.print(", ");
                rentalYield.print(", ");
                housePriceGrowth.print(", ");
                interestRateSpread.print(", ");
            }
            // Write core indicators results
            HPI.print(Model.coreIndicators.getHPI());
            top10NetTotalWealthShare.print(Model.coreIndicators.getS90TotalNetWealth());
            palmerIndex.print(Model.coreIndicators.getPalmerIndex());
            numberBankruptcies.print(Model.coreIndicators.getNumberBankruptcies());
        	shareEmptyHouses.print(Model.coreIndicators.getShareEmptyHouses());
        	BTLMarketShare.print(Model.coreIndicators.getBTLMarketShare());
            financialWealth.print(Model.coreIndicators.getTotalFinancialWealth());
            incomeConsumption.print(Model.coreIndicators.getTotalIncomeConsumption());
            financialConsumption.print(Model.coreIndicators.getTotalFinancialConsumption());
            grossHousingWealthConsumption.print(Model.coreIndicators.getTotalGrossHousingWealthConsumption());
            debtConsumption.print(Model.coreIndicators.getDebtConsumption());
            savingDeleveraging.print(Model.coreIndicators.getSavingForDeleveraging());
            consumptionToIncome.print(Model.coreIndicators.getConsumptionOverIncome());
            ooLTI.print(Model.coreIndicators.getOwnerOccupierLTIMeanAboveMedian());
            btlLTV.print(Model.coreIndicators.getBuyToLetLTVMean());
            creditGrowth.print(Model.coreIndicators.getHouseholdCreditGrowth());
            debtToIncome.print(Model.coreIndicators.getDebtToIncome());
            ooDebtToIncome.print(Model.coreIndicators.getOODebtToIncome());
            mortgageApprovals.print(Model.coreIndicators.getMortgageApprovals());
            housingTransactions.print(Model.coreIndicators.getHousingTransactions());
            advancesToFTBs.print(Model.coreIndicators.getAdvancesToFTBs());
            advancesToBTL.print(Model.coreIndicators.getAdvancesToBTL());
            advancesToHomeMovers.print(Model.coreIndicators.getAdvancesToHomeMovers());
            priceToIncome.print(Model.coreIndicators.getPriceToIncome());
            rentalYield.print(Model.coreIndicators.getAvStockYield());
            housePriceGrowth.print(Model.coreIndicators.getQoQHousePriceGrowth());
            interestRateSpread.print(Model.coreIndicators.getInterestRateSpread());
        }

        // Write general output results to output file
        if (recordOutfile) {
        	outfile.println(time + ", " +
        			// Number of households of each type
        			Model.householdStats.getnNonBTLHomeless() + ", " +
        			Model.householdStats.getnBTLHomeless() + ", " +
        			Model.householdStats.getnHomeless() + ", " +
        			Model.householdStats.getnRenting() + ", " +
        			Model.householdStats.getnNonOwner() + ", " +
        			Model.householdStats.getnNonBTLOwnerOccupier() + ", " +
        			Model.householdStats.getnBTLOwnerOccupier() + ", " +
        			Model.householdStats.getnOwnerOccupier() + ", " +
        			Model.householdStats.getnActiveBTL() + ", " +
        			Model.householdStats.getnBTL() + ", " +
        			Model.householdStats.getnNonBTLBankruptcies() + ", " +
        			Model.householdStats.getnBTLBankruptcies() + ", " +
        			Model.households.size() + ", " +
        			// Numbers of houses of each type
        			Model.construction.getHousingStock() + ", " +
        			Model.construction.getnNewBuild() + ", " +
        			Model.housingMarketStats.getnUnsoldNewBuild() + ", " +
        			Model.householdStats.getnEmptyHouses() + ", " +
        			Model.householdStats.getBTLStockFraction() + ", " +
        			// House sale market data
        			Model.housingMarketStats.getHPI() + ", " +
        			Model.housingMarketStats.getAnnualHPA() + ", " +
        			Model.housingMarketStats.getAvBidPrice() + ", " +
        			Model.housingMarketStats.getAvOfferPrice() + ", " +
        			Model.housingMarketStats.getAvSalePrice() + ", " +
        			Model.housingMarketStats.getExpAvSalePrice() + ", " +
        			Model.housingMarketStats.getAvMonthsOnMarket() + ", " +
        			Model.housingMarketStats.getExpAvMonthsOnMarket() + ", " +
        			Model.housingMarketStats.getnBuyers() + ", " +
        			Model.housingMarketStats.getnBTLBuyers() + ", " +
        			Model.housingMarketStats.getnSellers() + ", " +
        			Model.housingMarketStats.getnNewSellers() + ", " +
        			Model.housingMarketStats.getnBTLSellers() + ", " +
        			Model.housingMarketStats.getnSales() + ", " +
        			Model.householdStats.getnNonBTLBidsAboveExpAvSalePrice() + ", " +
        			Model.householdStats.getnBTLBidsAboveExpAvSalePrice() + ", " +
        			Model.housingMarketStats.getnSalesToBTL() + ", " +
        			Model.housingMarketStats.getnSalesToFTB() + ", " +
        			// Rental market data
        			Model.rentalMarketStats.getHPI() + ", " +
        			Model.rentalMarketStats.getAnnualHPA() + ", " +
        			Model.rentalMarketStats.getAvBidPrice() + ", " +
        			Model.rentalMarketStats.getAvOfferPrice() + ", " +
        			Model.rentalMarketStats.getAvSalePrice() + ", " +
        			Model.rentalMarketStats.getAvMonthsOnMarket() + ", " +
        			Model.rentalMarketStats.getExpAvMonthsOnMarket() + ", " +
        			Model.rentalMarketStats.getnBuyers() + ", " +
        			Model.rentalMarketStats.getnSellers() + ", " +
        			Model.rentalMarketStats.getnSales() + ", " +
        			Model.rentalMarketStats.getExpAvFlowYield() + ", " +
        			// Credit data
        			Model.creditSupply.getnRegisteredMortgages() + ", " +
        			//RUBEN additional variables
        			Model.householdStats.getTotalBankBalancesVeryBeginningOfPeriod() + ", " +
        			(Model.householdStats.getOwnerOccupierAnnualisedTotalIncome()/Model.config.constants.MONTHS_IN_YEAR
        					+ Model.householdStats.getActiveBTLAnnualisedTotalIncome()/Model.config.constants.MONTHS_IN_YEAR
        					+ Model.householdStats.getNonOwnerAnnualisedTotalIncome()/Model.config.constants.MONTHS_IN_YEAR) + ", " + 
        					(Model.householdStats.getOwnerOccupierMonthlyNetIncome()
        							+ Model.householdStats.getActiveMonthlyNetIncome()
        							+ Model.householdStats.getNonOwnerMonthlyNetIncome()) + ", " +
					Model.householdStats.getMonthlyGrossEmploymentIncome() + ", " +	
					Model.householdStats.getTotalMonthlyTaxesPaid() + ", " + 
					Model.householdStats.getTotalMonthlyNICPaid() + ", " +
					Model.householdStats.getTotalBankBalancesBeforeConsumption() + ", " + 
					Model.householdStats.getTotalBankBalanceEndowment() + ", " +
					Model.householdStats.getTotalConsumption()  + ", " +
					Model.householdStats.getIncomeConsumption()  + ", " +
					Model.householdStats.getFinancialWealthConsumption()  + ", " +
					Model.householdStats.getHousingWealthConsumption()  + ", " +
					Model.householdStats.getDebtConsumption()  + ", " +
					Model.householdStats.getTotalSavingForDeleveraging() + ", " + 
					Model.householdStats.getTotalSaving() + ", " +
					(Model.creditSupply.totalBTLCredit + Model.creditSupply.totalOOCredit) + ", " +
					Model.householdStats.getTotalPrincipalRepayments() + ", " +
					Model.householdStats.getTotalPrincipalRepaymentsDueToHouseSale() + ", " + 
					Model.householdStats.getTotalPrincipalRepaymentDeceasedHouseholds() + ", " +
					Model.householdStats.getTotalInterestRepayments() + ", " +
					Model.householdStats.getTotalRentalPayments() + ", " +
					Model.householdStats.getTotalBankruptcyCashInjection() + ", " +
					Model.householdStats.getTotalDebtReliefOfDeceasedHouseholds() + ", " +
					Model.bank.creditSupplyTarget(Model.households.size()) + ", " +
					Model.creditSupply.getNewlyPaidDownPayments() + ", " +
					Model.creditSupply.getNewlyIssuedCredit() + ", " + 
					Model.householdStats.getNNegativeEquity() + ", " +
					Model.bank.getLoanToValueLimit(true, true) + ", " +
					Model.bank.getLoanToValueLimit(false, true) + ", " +
					Model.bank.getLoanToValueLimit(false, false) + ", " +
					// divide by 100 as the interest rate in core indicators is calculated as percentage
					Model.coreIndicators.getInterestRateSpread()/100 + ", " +
					Model.housingMarketStats.getMoneyToConstructionSector());
        }

        // Write quality band prices to file
        if (recordQualityBandPrice) {
            String str = Arrays.toString(Model.housingMarketStats.getAvSalePricePerQuality());
            str = str.substring(1, str.length() - 1);
            qualityBandPriceFile.println(time + ", " + str);
            
            // write the expected average sale price to string
            String str2 = Arrays.toString(Model.housingMarketStats.getExpAvSalePricePerQuality());
            str2 = str2.substring(1, str2.length() - 1);
            qualityBandPriceExpectedFile.println(time + ", " + str2);
        }
    }

    public void finishRun(boolean recordOutfile, boolean recordCoreIndicators, boolean recordQualityBandPrice) {
        if (recordCoreIndicators) {
            HPI.println("");
            top10NetTotalWealthShare.println("");
            palmerIndex.println("");
            numberBankruptcies.println("");
            shareEmptyHouses.println("");
            BTLMarketShare.println("");
            financialWealth.println("");
            incomeConsumption.println("");
            financialConsumption.println("");
            grossHousingWealthConsumption.println("");
            debtConsumption.println("");
            savingDeleveraging.println("");
            consumptionToIncome.println("");
            ooLTI.println("");
            btlLTV.println("");
            creditGrowth.println("");
            debtToIncome.println("");
            ooDebtToIncome.println("");
            mortgageApprovals.println("");
            housingTransactions.println("");
            advancesToFTBs.println("");
            advancesToBTL.println("");
            advancesToHomeMovers.println("");
            priceToIncome.println("");
            rentalYield.println("");
            housePriceGrowth.println("");
            interestRateSpread.println("");
        }
        if (recordOutfile) outfile.close();
        if (recordQualityBandPrice) {
            qualityBandPriceFile.close();
            qualityBandPriceExpectedFile.close();
        }
    }

    public void finish(boolean recordCoreIndicators) {
        if (recordCoreIndicators) {
        	HPI.close();
        	top10NetTotalWealthShare.close();
        	palmerIndex.close();
        	numberBankruptcies.close();
        	shareEmptyHouses.close();
        	BTLMarketShare.close();
            financialWealth.close();
            incomeConsumption.close();
            financialConsumption.close();
            grossHousingWealthConsumption.close();
            debtConsumption.close();
            savingDeleveraging.close();
        	consumptionToIncome.close();
            ooLTI.close();
            btlLTV.close();
            creditGrowth.close();
            debtToIncome.close();
            ooDebtToIncome.close();
            mortgageApprovals.close();
            housingTransactions.close();
            advancesToFTBs.close();
            advancesToBTL.close();
            advancesToHomeMovers.close();
            priceToIncome.close();
            rentalYield.close();
            housePriceGrowth.close();
            interestRateSpread.close();
        }
    }
}
