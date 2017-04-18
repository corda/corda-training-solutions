package net.corda.training.api

import net.corda.contracts.asset.Cash
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.loggerFor
import net.corda.training.flow.IOUIssueFlow
import net.corda.training.flow.IOUSettleFlow
import net.corda.training.flow.IOUTransferFlow
import net.corda.training.flow.SelfIssueCashFlow
import net.corda.training.state.IOUState
import org.slf4j.Logger
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

val NOTARY_NAMES = listOf("Controller", "NetworkMapService")

/**
 * This API is accessible from /api/iou. The endpoint paths specified below are relative to it.
 * We've defined a bunch of endpoints to deal with IOUs, cash and the various operations you can perform with them.
 */
@Path("iou")
class IOUApi(val services: CordaRPCOps) {
    private val myLegalName: String = services.nodeIdentity().legalIdentity.name

    companion object {
        private val logger: Logger = loggerFor<IOUApi>()
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers() = mapOf("peers" to services.networkMapUpdates().first
            .map { it.legalIdentity.name }
            .filter { it != myLegalName && it !in NOTARY_NAMES })

    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GET
    @Path("ious")
    @Produces(MediaType.APPLICATION_JSON)
    // Filter by state type: IOU.
    fun getIOUs() = services.vaultAndUpdates().first.filter { it.state.data is IOUState }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GET
    @Path("cash")
    @Produces(MediaType.APPLICATION_JSON)
    // Filter by state type: Cash.
    fun getCash() = services.vaultAndUpdates().first.filter { it.state.data is Cash.State }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GET
    @Path("cash-balances")
    @Produces(MediaType.APPLICATION_JSON)
    // Filter by state type: Cash.
    fun getCashBalances() = services.getCashBalances()

    /**
     * Initiates a flow to agree an IOU between two parties.
     */
    @GET
    @Path("issue-iou")
    fun issueIOU(@QueryParam(value = "amount") amount: Int,
                  @QueryParam(value = "currency") currency: String,
                  @QueryParam(value = "party") party: String): Response {
        // Get party objects for myself and the counterparty.
        val me = services.nodeIdentity().legalIdentity
        val lender = services.partyFromName(party) ?: throw IllegalArgumentException("Unknown party name.")
        // Create a new IOU state using the parameters given.
        val state = IOUState(Amount(amount.toLong() * 100, Currency.getInstance(currency)), lender, me)
        // Start the IOUIssueFlow. We block and waits for the flow to return.
        val result = services.startFlowDynamic(IOUIssueFlow::class.java, state, lender).returnValue.get()
        // Return the response.
        return Response
                .status(Response.Status.CREATED)
                .entity("Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single()}")
                .build()
    }

    /**
     * tranfers an IOU specified by [linearId] to a new party.
     */
    @GET
    @Path("transfer-iou")
    fun transferIOU(@QueryParam(value = "id") id: String,
                  @QueryParam(value = "party") party: String): Response {
        val linearId = UniqueIdentifier.fromString(id)
        val newLender = services.partyFromName(party) ?: throw IllegalArgumentException("Unknown party name.")
        services.startFlowDynamic(IOUTransferFlow::class.java, linearId, newLender).returnValue.get()
        return Response.status(Response.Status.CREATED).entity("IOU $id transferred to $party.").build()
    }

    /**
     * Settles an IOU. Requires cash in the right currency to be able to settle.
     */
    @GET
    @Path("settle-iou")
    fun settleIOU(@QueryParam(value = "id") id: String,
                  @QueryParam(value = "amount") amount: Int,
                  @QueryParam(value = "currency") currency: String): Response {
        val linearId = UniqueIdentifier.fromString(id)
        val settleAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))
        services.startFlowDynamic(IOUSettleFlow::class.java, linearId, settleAmount).returnValue.get()
        return Response.status(Response.Status.CREATED).entity("$amount $currency paid off on IOU id $id.").build()
    }

    /**
     * Helper end-point to issue some cash to ourselves.
     */
    @GET
    @Path("self-issue-cash")
    fun selfIssueCash(@QueryParam(value = "amount") amount: Int,
                      @QueryParam(value = "currency") currency: String): Response {
        val issueAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))
        val cashState = services.startFlowDynamic(SelfIssueCashFlow::class.java, issueAmount).returnValue.get()
        return Response.status(Response.Status.CREATED).entity(cashState.toString()).build()
    }
}