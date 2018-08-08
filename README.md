TODO: Update links to correct paths upon final migration

![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# Corda Training Solutions

This repo contains all the solutions for the practical exercises of the Corda two day 
training course.


This repository is divided into two parts: A Java solution, and a Kotlin solution. You may complete the training in either
language, but be aware that the Corda platform is written in Kotlin, and many developers prefer writing CorDapps in Kotlin
as well.

# Pre-requisites:
  
* JDK 1.8 latest version
* IntelliJ latest version (2017.1) (as of writing)
* git

# Solution files

### Kotlin Solutions
State solution:

* [`IOUState.kt`](\)

Contract solution:

* [`IOUContract.kt`](\)

Flow solutions:

* Issue: [`IOUIssueFlow.kt`](\)
* Transfer: [`IOUTransferFlow.kt`](\)
* Settle: [`IOUSettleFlow.kt`](\)

The code in the following files was already added for you:

* [`SelfIssueCashFlow.kt`](\)
* [`IOUApi.kt`](\)
* [`Main.kt`](\)
* [`IOUPlugin.kt`](\)


### Java Solutions
State solution:

* [`IOUState.java`](\)

Contract solution:

* [`IOUContract.java`](\)

Flow solutions:

* Issue: [`IOUIssueFlow.java`](\)
* `Not implemented` Transfer: [`IOUTransferFlow.java`](\)
* `Not implemented` Settle: [`IOUSettleFlow.java`](\)

The code in the following files was already added for you:

* [`IOUApi.java`](\)
* [`NodeDriver.java`](\)
* [`IOUPlugin.java`](\)
* `Not implemented` [`SelfIssueCashFlow.java`](\)