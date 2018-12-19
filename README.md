![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# Corda Training Solutions

This repo contains all the solutions for the practical exercises of the Corda two-day training course.

This repository is divided into two parts: Java Solutions, and Kotlin Solutions.

# Setup

### Tools
* JDK 1.8 latest version
* IntelliJ latest version (2017.1 or newer)
* git

After installing the required tools, clone or download a zip of this repository, and place it in your desired
location.

### IntelliJ setup
* From the main menu, click `open` (not `import`!) then navigate to where you placed this repository.
* Click `File->Project Structure`, and set the `Project SDK` to be the JDK you downloaded (by clicking `new` and
nagivating to where the JDK was installed). Click `Okay`.
* Next, click `import` on the `Import Gradle Project` popup, leaving all options as they are.
* If you do not see the popup: Navigate back to `Project Structure->Modules`, clicking the `+ -> Import` button,
navigate to and select the repository folder, select `Gradle` from the next menu, and finally click `Okay`,
again leaving all options as they are.

### Running the tests
* Kotlin: Select `Kotlin - Unit tests` from the dropdown run configuration menu, and click the green play button.
* Java: Select `Java - Unit tests` from the dropdown run configuration menu, and click the green play button.
* Individual tests can be run by clicking the green arrow in the line number column next to each test.
* When running flow tests you must add the following to your run / debug configuration in the VM options field. This enables us to use
* Quasar - a library that provides high-performance, lightweight threads.
* "-javaagent: /PATH_TO_FILE_FROM_ROOT_DIR/quasar.jar"

# Solutions Files

### Kotlin
State:

* Template: `kotlin-source/src/test/kotlin/net/corda/training/state/IOUState.kt`
* Tests: `kotlin-source/src/main/kotlin/net/corda/training/state/IOUStateTests.kt`

Contract:

* Template: `kotlin-source/src/main/kotlin/net/corda/training/contract/IOUContract.kotlin`
* Issue Tests: `kotlin-source/src/test/kotlin/net/corda/training/contract/IOUIssueTests.kotlin`
* Transfer Tests: `kotlin-source/src/test/kotlin/net/corda/training/contract/IOUTransferTests.kotlin`
* Settle Tests: `kotlin-source/src/test/kotlin/net/corda/training/contract/IOUSettleTests.kotlin`

Flow:

* Issue Solution: `kotlin-source/src/main/kotlin/net/corda/training/flow/IOUIssueFlow.kt`
* Issue tests: `kotlin-source/src/test/kotlin/net/corda/training/flow/IOUIssueFlowTests.kt`
* Transfer Solution: `kotlin-source/src/main/kotlin/net/corda/training/flow/IOUTransfer.kt`
* Transfer tests: `kotlin-source/src/test/kotlin/net/corda/training/flow/IOUTransferFlowTests.kt`
* Settle Solution: `kotlin-source/src/main/kotlin/net/corda/training/flow/IOUSettleFlow.kt`
* Settle tests: `kotlin-source/src/test/kotlin/net/corda/training/flow/IOUSettleFlowTests.kt`

The code in the following files was already added for you:

* `kotlin-source/src/main/kotlin/net/corda/training/plugin/IOUPlugin.kt`
* `kotlin-source/src/test/kotlin/net/corda/training/Main.kt`
* `kotling-source/src/main/kotlin/net/corda/training/plugin/IOUPlugin.kt`
* `kotling-source/src/main/java/kotlin/corda/training/flow/SelfIssueCashFlow.kt`


### Java
State:

* Solution: `java-source/src/main/java/net/corda/training/state/IOUState.java`
* Tests: `java-source/src/test/java/net/corda/training/state/IOUStateTests.java`

Contract:

* Solution: `java-source/src/main/java/net/corda/training/contract/IOUContract.java`
* Issue Tests: `java-source/src/test/java/net/corda/training/contract/IOUIssueTests.java`
* Transfer Tests: `java-source/src/test/java/net/corda/training/contract/IOUIssueTests.java`
* Settle Tests: `java-source/src/test/java/net/corda/training/contract/IOUIssueTests.java`

Flow:

* Issue Solution: `java-source/src/main/java/net/corda/training/flow/IOUIssueFlow.java`
* Issue tests: `java-source/src/test/java/net/corda/training/flow/IOUIssueFlowTests.java`
* Transfer Solution: `java-source/src/main/java/net/corda/training/flow/IOUTransferFlow.java`
* Transfer tests: `java-source/src/test/java/net/corda/training/flow/IOUTransferFlowTests.java`
* Settle Solution: `java-source/src/main/java/net/corda/training/flow/IOUSettleFlow.java`
* Settle tests: `java-source/src/test/java/net/corda/training/flow/IOUSettleFlowTests.java`

The code in the following files was already added for you:

* `java-source/src/main/java/net/corda/training/plugin/IOUPlugin.java`
* `java-source/src/test/java/net/corda/training/NodeDriver.java`
* `java-source/src/main/java/net/corda/training/plugin/IOUPlugin.java`
* `java-source/src/main/java/net/corda/training/flow/SelfIssueCashFlow.java`


# Running the CorDapp
Once your application passes all tests in `IOUStateTests`, `IOUIssueTests`, and `IOUIssueFlowTests`, you can run the application and
interact with it via a web browser. To run the finished application, you have two choices for each language: from the terminal, and from IntelliJ.

### Kotlin
* Terminal: Navigate to the root project folder and run `./gradlew kotlin-source:deployNodes`, followed by
`./kotlin-source/build/node/runnodes`
* IntelliJ: With the project open, select `Kotlin - Node driver` from the dropdown run configuration menu, and click
the green play button.

### Java
* Terminal: Navigate to the root project folder and run `./gradlew java-source:deployNodes`, followed by
`./java-source/build/node/runnodes`
* IntelliJ: With the project open, select `Java - NodeDriver` from the dropdown run configuration menu, and click
the green play button.

### Interacting with the CorDapp
Once all the three nodes have started up (look for `Webserver started up in XXX sec` in the terminal or IntelliJ ), you can interact
with the app via a web browser.
* From a Node Driver configuration, look for `Starting webserver on address localhost:100XX` for the addresses.

* From the terminal: Node A: `localhost:10009`, Node B: `localhost:10012`, Node C: `localhost:10015`.

To access the front-end gui for each node, navigate to `localhost:XXXX/web/iou/`

## Troubleshooting:
When running the flow tests, if you get a Quasar instrumention error then add:

```-ea -javaagent:lib/quasar.jar```

to the VM args property in the default run configuration for JUnit in IntelliJ.