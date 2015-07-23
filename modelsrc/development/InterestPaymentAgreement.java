package development;

import housing.Model;
import utilities.LongSupplier;
import utilities.ModelTime;

public class InterestPaymentAgreement extends PaymentAgreement implements ITriggerable {
	public InterestPaymentAgreement(DepositAccount sourceAC, DepositAccount payeeAC, final DepositAccount interestBearingAC, double iAnnualInterest) {
		this(sourceAC,payeeAC, new LongSupplier() {
			@Override
			public long getAsLong() {
				return(interestBearingAC.balance);
			}
			
		}, iAnnualInterest);
	}
	
	public InterestPaymentAgreement(DepositAccount sourceAC, DepositAccount payeeAC, LongSupplier iBalance, double iAnnualInterest) {
		super(sourceAC,payeeAC);
		annualInterestRate = iAnnualInterest;
		unpaidInterest = 0.0;
		balance = iBalance;
		unpaidInterestTimestamp = Model.timeNow();
	}
	
	@Override
	public long amount() {
		return((long)unpaidInterest);
	}
	
	public void trigger() {
		updateUnpaidInterest();
		super.trigger();
		unpaidInterest -= amount();
	}
	
	public void updateUnpaidInterest() {
		ModelTime newTimestamp = Model.timeNow();
		unpaidInterest += balance.getAsLong()*annualInterestRate*(newTimestamp.inYears() - unpaidInterestTimestamp.inYears());
		unpaidInterestTimestamp = newTimestamp;
	}

	LongSupplier  balance;
//	PeriodicPayment interestPayment;
	double annualInterestRate;
	double unpaidInterest;
	ModelTime unpaidInterestTimestamp;

}