package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import housing.Config;
import housing.Model;

public class MicroDataRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String 		outputFolder;

    private PrintWriter outfileBankBalance;
    private PrintWriter outfileHousingWealth;
    private PrintWriter outfileNHousesOwned;
    private PrintWriter outfileSavingRate;
    private PrintWriter outfileMonthlyGrossTotalIncome;
    private PrintWriter outfileMonthlyGrossEmploymentIncome;
    private PrintWriter outfileMonthlyGrossRentalIncome;
    private PrintWriter outfileMonthlyDisposableIncome;
    private PrintWriter outfileMonthlyMortgagePayments;
    private PrintWriter outfileDebt;
    private PrintWriter outfileConsumption;
    private PrintWriter outfileIncomeConsumption;
    private PrintWriter outfileFinancialWealthConsumption;
    private PrintWriter outfileHousingWealthConsumption;
    private PrintWriter outfileDebtConsumption;
    private PrintWriter outfileSavingForDeleveraging;
    private PrintWriter outfileBTL;
    private PrintWriter outfileFTB;
    private PrintWriter outfileInFirstHome;
    private PrintWriter outfileAge;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public MicroDataRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunSingleVariableFiles(int nRun, boolean recordBankBalance, boolean recordInitTotalWealth,
                                                 boolean recordNHousesOwned, boolean recordSavingRate,
                                                 boolean recordMonthlyGrossTotalIncome, boolean recordMonthlyGrossEmploymentIncome,
                                                 boolean recordMonthlyGrossRentalIncome, boolean recordMonthlyDisposableIncome,
                                                 boolean recordMonthlyMortgagePayments, boolean recordDebt,
                                                 boolean recordConsumption, boolean recordIncomeConsumption,
                                                 boolean recordFinancialWealthConsumption, boolean recordHousingWealthConsumption,
                                                 boolean recordDebtConsumption, boolean recordSavingForDeleveraging, boolean recordBTL, 
                                                 boolean recordFTB, boolean recordInFirstHome, boolean recordAge) {
        if (recordBankBalance) {
            try {
                outfileBankBalance = new PrintWriter(outputFolder + "BankBalance-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordInitTotalWealth) {
            try {
                outfileHousingWealth = new PrintWriter(outputFolder + "NetHousingWealth-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordNHousesOwned) {
            try {
                outfileNHousesOwned = new PrintWriter(outputFolder + "NHousesOwned-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordSavingRate) {
            try {
                outfileSavingRate = new PrintWriter(outputFolder + "SavingRate-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if(recordMonthlyGrossTotalIncome) {
        	try {
        		outfileMonthlyGrossTotalIncome = new PrintWriter(outputFolder + 
        				"MonthlyGrossTotalIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordMonthlyGrossEmploymentIncome) {
        	try {
        		outfileMonthlyGrossEmploymentIncome = new PrintWriter(outputFolder + 
        				"MonthlyGrossEmploymentIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordMonthlyDisposableIncome) {
        	try {
        		outfileMonthlyDisposableIncome = new PrintWriter(outputFolder + 
        				"MonthlyDisposableIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordMonthlyMortgagePayments) {
        	try {
        		outfileMonthlyMortgagePayments = new PrintWriter(outputFolder + 
        				"MonthlyMortgagePayments-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordMonthlyGrossRentalIncome) {
        	try {
        		outfileMonthlyGrossRentalIncome = new PrintWriter(outputFolder + 
        				"MonthlyGrossRentalIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordDebt) {
        	try {
        		outfileDebt = new PrintWriter(outputFolder + 
        				"Debt-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordConsumption) {
        	try {
        		outfileConsumption = new PrintWriter(outputFolder + 
        				"Consumption-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordIncomeConsumption) {
        	try {
        		outfileIncomeConsumption = new PrintWriter(outputFolder + 
        				"IncomeConsumption-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordFinancialWealthConsumption) {
        	try {
        		outfileFinancialWealthConsumption = new PrintWriter(outputFolder + 
        				"FinancialWealthConsumption-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordHousingWealthConsumption) {
        	try {
        		outfileHousingWealthConsumption = new PrintWriter(outputFolder + 
        				"HousingWealthConsumption-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordDebtConsumption) {
        	try {
        		outfileDebtConsumption = new PrintWriter(outputFolder + 
        				"DebtConsumption-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordSavingForDeleveraging) {
        	try {
        		outfileSavingForDeleveraging = new PrintWriter(outputFolder + 
        				"SavingForDeleveraging-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordBTL) {
        	try {
        		outfileBTL = new PrintWriter(outputFolder + 
        				"isBTL-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordFTB) {
        	try {
        		outfileFTB = new PrintWriter(outputFolder + 
        				"isFTB-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordInFirstHome) {
        	try {
        		outfileInFirstHome = new PrintWriter(outputFolder + 
        				"isInFirstHome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordAge) {
        	try {
        		outfileAge = new PrintWriter(outputFolder + 
        				"Age-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
    }

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordBankBalance, boolean recordHousingWealth,
                                               boolean recordNHousesOwned, boolean recordSavingRate, 
                                               boolean recordMonthlyGrossTotalIncome, boolean recordMonthlyGrossEmploymentIncome,
                                               boolean recordMonthlyGrossRentalIncome, boolean recordMonthlyDisposableIncome,
                                               boolean recordMonthlyMortgagePayments, boolean recordDebt,
                                               boolean recordConsumption, boolean recordIncomeConsumption,
                                               boolean recordFinancialWealthConsumption, boolean recordHousingWealthConsumption,
                                               boolean recordDebtConsumption, boolean recordSavingForDeleveraging,
                                               boolean recordBTL, boolean recordFTB, boolean recordInFirstHome, boolean recordAge) {
        if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
            if (recordBankBalance) {
                if (time != 0) {
                    outfileBankBalance.println("");
                }
                outfileBankBalance.print(time);
            }
            if (recordHousingWealth) {
                if (time != 0) {
                    outfileHousingWealth.println("");
                }
                outfileHousingWealth.print(time);
            }
            if (recordNHousesOwned) {
                if (time != 0) {
                    outfileNHousesOwned.println("");
                }
                outfileNHousesOwned.print(time);
            }
            if (recordSavingRate) {
            	if (time != 0) {
            		outfileSavingRate.println("");
            	}
            	outfileSavingRate.print(time);
            }
            if (recordMonthlyGrossTotalIncome) {
            	if(time!=0) {
            		outfileMonthlyGrossTotalIncome.println("");
            	}
            	outfileMonthlyGrossTotalIncome.print(time);
            }
            if (recordMonthlyGrossEmploymentIncome) {
            	if(time!=0) {
            		outfileMonthlyGrossEmploymentIncome.println("");
            	}
            	outfileMonthlyGrossEmploymentIncome.print(time);
            }
            if (recordMonthlyGrossRentalIncome) {
            	if(time!=0) {
            		outfileMonthlyGrossRentalIncome.println("");
            	}
            	outfileMonthlyGrossRentalIncome.print(time);
            }
            if (recordMonthlyDisposableIncome) {
            	if (time != 0) {
            		outfileMonthlyDisposableIncome.println("");
            	}
            	outfileMonthlyDisposableIncome.print(time);
            }
            if (recordMonthlyMortgagePayments) {
            	if (time != 0) {
            		outfileMonthlyMortgagePayments.println("");
            	}
            	outfileMonthlyMortgagePayments.print(time);
            }
            if (recordDebt) {
            	if (time != 0) {
            		outfileDebt.println("");
            	}
            	outfileDebt.print(time);
            }
            if (recordConsumption) {
            	if (time != 0) {
            		outfileConsumption.println("");
            	}
            	outfileConsumption.print(time);
            }
            if (recordIncomeConsumption) {
            	if (time != 0) {
            		outfileIncomeConsumption.println("");
            	}
            	outfileIncomeConsumption.print(time);
            }
            if (recordFinancialWealthConsumption) {
            	if (time != 0) {
            		outfileFinancialWealthConsumption.println("");
            	}
            	outfileFinancialWealthConsumption.print(time);
            }
            if (recordHousingWealthConsumption) {
            	if (time != 0) {
            		outfileHousingWealthConsumption.println("");
            	}
            	outfileHousingWealthConsumption.print(time);
            }
            if (recordDebtConsumption) {
            	if (time != 0) {
            		outfileDebtConsumption.println("");
            	}
            	outfileDebtConsumption.print(time);
            }
            if (recordSavingForDeleveraging) {
            	if (time != 0) {
            		outfileSavingForDeleveraging.println("");
            	}
            	outfileSavingForDeleveraging.print(time);
            }
            if (recordBTL) {
            	if (time != 0) {
            		outfileBTL.println("");
            	}
            	outfileBTL.print(time);
            }
            if (recordFTB) {
            	if (time != 0) {
            		outfileFTB.println("");
            	}
            	outfileFTB.print(time);
            }
            if (recordInFirstHome) {
            	if (time != 0) {
            		outfileInFirstHome.println("");
            	}
            	outfileInFirstHome.print(time);
            }            
            if (recordAge) {
            	if (time != 0) {
            		outfileAge.println("");
            	}
            	outfileAge.print(time);
            }
        }
    }
	
    void recordBankBalance(int time, double bankBalance) {
    	outfileBankBalance.print(", " + bankBalance);
    }

    void recordHousingWealth(int time, double housingWealth) {
    	outfileHousingWealth.print(", " + housingWealth);
    }

    void recordNHousesOwned(int time, int nHousesOwned) {
        outfileNHousesOwned.print(", " + nHousesOwned);
    }

    void recordSavingRate(int time, double savingRate) {
        outfileSavingRate.print(", " + savingRate);
    }
    
    void recordMonthlyGrossTotalIncome(int time, double monthlyGrossTotalIncome) {
    	outfileMonthlyGrossTotalIncome.print(", " + monthlyGrossTotalIncome);
    }
    
    void recordMonthlyGrossEmploymentIncome(int time, double monthlyGrossEmploymentIncome) {
    	outfileMonthlyGrossEmploymentIncome.print(", " + monthlyGrossEmploymentIncome);
    }
    
    void recordMonthlyGrossRentalIncome(int time, double monthlyGrossRentalIncome) {
    	outfileMonthlyGrossRentalIncome.print(", " + monthlyGrossRentalIncome);
    }
    
    void recordMonthlyDisposableIncome(int time, double monthlyDisposableIncome) {
    	outfileMonthlyDisposableIncome.print(", " + monthlyDisposableIncome);
    }
    
    void recordMonthlyMortgagePayments(int time, double monthlyMortgagePayments) {
    	outfileMonthlyMortgagePayments.print(", " + monthlyMortgagePayments);
    }
    
    void recordDebt(int time, double debt) {
    	outfileDebt.print(", " + debt);
    }
    
    void recordConsumption(int time, double consumption) {
		outfileConsumption.print(", " + consumption);
	}
    
    void recordIncomeConsumption(int time, double consumption) {
		outfileIncomeConsumption.print(", " + consumption);
    }
    
    void recordFinancialWealthConsumption(int time, double consumption) {
		outfileFinancialWealthConsumption.print(", " + consumption);
    }
    
    void recordHousingWealthConsumption(int time, double consumption) {
    	outfileHousingWealthConsumption.print(", " + consumption);
    }
    
    void recordDebtConsumption(int time, double consumption) {
		outfileDebtConsumption.print(", " + consumption);
    }
    
    void recordSavingForDeleveraging(int time, double savingForDeleveraging) {
    	outfileSavingForDeleveraging.print(", " + savingForDeleveraging);
    }
    
    void recordBTL(int time, boolean isBTL) {
		if(isBTL)outfileBTL.print(", " + 1);
		else outfileBTL.print(", " + 0);
    }
    void recordFTB(int time, boolean isFTB) {
		if(isFTB)outfileFTB.print(", " + 1);
		else outfileFTB.print(", " + 0);
    }
    void recordInFirstHome(int time, boolean inFirstHome) {
		if(inFirstHome)outfileInFirstHome.print(", " + 1);
		else outfileInFirstHome.print(", " + 0);
    }
    void recordAge(int time, double Age) {
    	outfileAge.print(", " + Age);
    }


	public void finishRun(boolean recordBankBalance, boolean recordHousingWealth, boolean recordNHousesOwned,
                          boolean recordSavingRate, boolean recordMonthlyGrossTotalIncome, boolean recordMonthlyGrossEmploymentIncome,
                          boolean recordMonthlyGrossRentalIncome, boolean recordMonthlyDisposableIncome,
                          boolean recordMonthlyMortgagePayments, boolean recordDebt, boolean recordConsumption, 
                          boolean recordIncomeConsumption, boolean recordFinancialWealthConsumption, boolean recordHousingWealthConsumption,
                          boolean recordDebtConsumption, boolean recordSavingForDeleveraging, boolean recordBTL,
                          boolean recordFTB, boolean recordInFirstHome, boolean recordAge) {
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
        if (recordHousingWealth) {
            outfileHousingWealth.close();
        }
        if (recordNHousesOwned) {
            outfileNHousesOwned.close();
        }
        if (recordSavingRate) {
            outfileSavingRate.close();
        }
        if (recordMonthlyGrossTotalIncome) {
        	outfileMonthlyGrossTotalIncome.close();
        }
        if (recordMonthlyGrossEmploymentIncome) {
        	outfileMonthlyGrossEmploymentIncome.close();
        }
        if (recordMonthlyGrossRentalIncome) {
        	outfileMonthlyGrossRentalIncome.close();
        }
        if (recordMonthlyDisposableIncome) {
        	outfileMonthlyDisposableIncome.close();
        }
        if (recordMonthlyMortgagePayments) {
        	outfileMonthlyMortgagePayments.close();
        }
        if (recordDebt) {
        	outfileDebt.close();
        }
        if (recordConsumption) {
        	outfileConsumption.close();
        }
        if (recordIncomeConsumption) {
        	outfileIncomeConsumption.close();
        }
        if (recordFinancialWealthConsumption) {
        	outfileFinancialWealthConsumption.close();
        }
        if (recordHousingWealthConsumption) {
        	outfileHousingWealthConsumption.close();
        }
        if (recordDebtConsumption) {
        	outfileDebtConsumption.close();
        }
        if (recordSavingForDeleveraging) {
        	outfileSavingForDeleveraging.close();
        }
        if (recordBTL) {
        	outfileBTL.close();
        }
        if (recordFTB) {
        	outfileFTB.close();
        }
        if (recordInFirstHome) {
        	outfileInFirstHome.close();
        }
        if (recordAge) {
        	outfileAge.close();
        }
	}
}
