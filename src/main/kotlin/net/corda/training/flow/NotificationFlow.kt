package net.corda.training.flow

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.training.state.IOUState

@InitiatingFlow
class NotificationFlow(val session: FlowSession, val state: IOUState): FlowLogic<Unit>() {
    override fun call() {
        session.send(state)
    }

}