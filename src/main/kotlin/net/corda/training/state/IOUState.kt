package net.corda.training.state

import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.keys
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.training.contract.IOUContract
import java.security.PublicKey
import java.util.*

/**
 * The IOU State object, with the following properties:
 * - [amount] The amount owed by the [borrower] to the [lender]
 * - [lender] The lending party.
 * - [borrower] The borrowing party.
 * - [contract] Holds a reference to the [IOUContract]
 * - [paid] Records how much of the [amount] has been paid.
 * - [linearId] A unique id shared by all LinearState states representing the same agreement throughout history within
 *   the vaults of all parties. Verify methods should check that one input and one output share the id in a transaction,
 *   except at issuance/termination.
 */
data class IOUState(val amount: Amount<Currency>,
               val lender: Party,
               val borrower: Party,
               val paid: Amount<Currency> = Amount(0, amount.token),
               override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    /**
     * This function determines if the [IOUState] is relevant to a Corda node based on whether the public keys
     * of the lender or borrower are known to the node, i.e. if the node is the lender or borrower.
     *
     * We do this by checking that the set intersection of the vault public keys with the participant public keys
     * is not the empty set.
     */
    override fun isRelevant(ourKeys: Set<PublicKey>): Boolean {
        val participantKeys = participants.map { it.owningKey }
        return ourKeys.intersect(participantKeys).isNotEmpty()
    }

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  lender or the borrower.
     */
    override val participants get() = listOf(lender, borrower)

    /**
     * A toString() helper method for displaying IOUs in the console.
     */
    override fun toString() = "IOU($linearId): ${borrower.name} owes ${lender.name} $amount and has paid $paid so far."

    /**
     * A reference to the IOUContract. Make sure this is not part of the [IOUState] constructor, if it is then
     * equality won't work property on this state type. ** Don't change this property! **
     */
    override val contract get() = IOUContract()

    /**
     * Helper methods for when building transactions for settling and transferring IOUs.
     * - [pay] adds an amount to the paid property. It does no validation.
     * - [withNewLender] creates a copy of the current state with a newly specified lender. For use when transferring.
     */
    fun pay(amountToPay: Amount<Currency>) = copy(paid = paid.plus(amountToPay))
    fun withNewLender(newLender: Party) = copy(lender = newLender)
}