package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.training.state.IOUState

class NotificationFlow(val session: FlowSession, val state: IOUState): FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        session.send(state)
    }
}