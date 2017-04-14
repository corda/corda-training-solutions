![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# Corda Training Solutions

This repo contains all the solutions for the practical exercises of the Corda two day 
training course. 

# Pre-requisites:
  
* JDK 1.8 latest version
* IntelliJ latest version (2017.1) (as of writing)
* git

# Solution files

State solution:

* [`IOUState.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/state/IOUState.kt)

Contract solution:

* [`IOUContract.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/contract/IOUContract.kt)

Flow solutions:

* Issue: [`IOUIssueFlow.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/flow/IOUIssueFlow.kt)
* Transfer: [`IOUTransferFlow.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/flow/IOUTransferFlow.kt)
* Settle: [`IOUSettleFlow.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/flow/IOUSettleFlow.kt)

The code in the following files was already added for you:

* [`SignTransactionFlow.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/flow/SignTransactionFlow.kt)
* [`SelfIssueCashFlow.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/flow/SelfIssueCashFlow.kt)
* [`IOUApi.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/api/IOUApi.kt)
* [`IOUClient.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/client/IOUClient.kt)
* [`IOUPlugin.kt`](https://github.com/roger3cev/corda-training-solutions/blob/master/src/main/kotlin/net/corda/training/plugin/IOUPlugin.kt)
