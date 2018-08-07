package net.corda.training.state;

import com.google.common.collect.ImmutableList;
import java.util.*;

import net.corda.core.contracts.Amount;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.ConstructorForDeserialization;

import net.corda.training.contract.IOUContract;

/**
 * The IOU State object, with the following properties:
 * - [amount] The amount owed by the [borrower] to the [lender]
 * - [lender] The lending party.
 * - [borrower] The borrowing party.
 * - [paid] Records how much of the [amount] has been paid.
 * - [linearId] A unique id shared by all LinearState states representing the same agreement throughout history within
 *   the vaults of all parties. Verify methods should check that one input and one output share the id in a transaction,
 *   except at issuance/termination.
 */
public class IOUState implements LinearState {
    private final Amount<Currency> amount;
    private final Party lender;
    private final Party borrower;
    private final Amount<Currency> paid;
    private final UniqueIdentifier linearId;


    // Private copy constructor
    @ConstructorForDeserialization 
    private IOUState(Amount<Currency> amount, Party lender, Party borrower, Amount<Currency> paid, UniqueIdentifier linearId) {
        this.amount = amount;
        this.lender = lender;
        this.borrower = borrower;
        this.paid = paid;
        this.linearId = linearId;
    }

    // For new states
    public IOUState(Amount<Currency> amount, Party lender, Party borrower) {
        this(amount, lender, borrower, new Amount<>(0, amount.getToken()), new UniqueIdentifier());
    }

    public Amount<Currency> getAmount() {
        return amount;
    }

    public Party getLender() {
        return lender;
    }

    public Party getBorrower() {
        return borrower;
    }

    public Amount<Currency> getPaid() {
    	return paid;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(lender, borrower);
    }

    @Override
    public UniqueIdentifier getLinearId(){
    	return linearId;
    }

	/**
	 * Helper methods for when building transactions for settling and transferring IOUs.
	 * - [pay] adds an amount to the paid property. It does no validation.
	 * - [withNewLender] creates a copy of the current state with a newly specified lender. For use when transferring.
	 */
	public IOUState pay(Amount<Currency> amountToPay) {
		Amount<Currency> newAmountPaid = this.paid.plus(amountToPay);
		return new IOUState(amount, lender, borrower, newAmountPaid, linearId);
	}

	public IOUState withNewLender(Party newLender) {
		return new IOUState(amount, newLender, borrower, paid, linearId);
	}	
}