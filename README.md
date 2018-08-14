![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# Corda Training Solutions

This repo contains all the solutions for the practical exercises of the Corda two day 
training course.


This repository is divided into two parts: Java solutions, and Kotlin solutions. You may complete the training in whichever 
language you prefer.

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

# Running the tests
Now that you are all set-up, you can run the unit tests in either language. This project follows a test-based
development style - the unit tests contain all the information you would need to complete this CorDapp. In this repository,
the solutions have been provided for you. With the project open in IntelliJ

* Kotlin: Select `Kotlin - Unit tests` from the dropdown run configuration menu, and click the green play button.
* Java: Select `Java - Unit tests` from the dropdown run configuration menu, and click the green play button.
# Solution files

### Kotlin Solutions
State solution:

* `kotlin-source/src/main/kotlin/net/corda/training/state/IOUState.kt`

Contract solution:

* `kotlin-source/src/main/kotlin/net/corda/training/contract/IOUContract.kotlin`

Flow solutions:

* Issue`kotlin-source/src/main/kotlin/net/corda/training/flow/IOUIssueFlow.kt`
* Transfer `kotlin-source/src/main/kotlin/net/corda/training/flow/IOUTransfer.kt`
* Settle `kotlin-source/src/main/kotlin/net/corda/training/flow/IOUSettleFlow.kt`

The code in the following files was already added for you:

* `kotlin-source/src/main/kotlin/net/corda/training/plugin/IOUPlugin.kt`
* `kotlin-source/src/test/kotlin/net/corda/training/Main.kt`
* `kotling-source/src/main/kotlin/net/corda/training/plugin/IOUPlugin.kt`
* `kotling-source/src/main/java/kotlin/corda/training/flow/SelfIssueCashFlow.kt`


### Java Solutions
State solution:

* `java-source/src/main/java/net/corda/training/state/IOUState.java`

Contract solution:

* `java-source/src/main/java/net/corda/training/contract/IOUContract.java`

Flow solutions:

* Issue`java-source/src/main/java/net/corda/training/flow/IOUIssueFlow.java`
* Transfer `Not implemented:` `java-source/src/main/java/net/corda/training/flow/IOUTransfer.java`
* Settle `Not implemented:` `java-source/src/main/java/net/corda/training/flow/IOUSettleFlow.java`

The code in the following files was already added for you:

* `java-source/src/main/java/net/corda/training/plugin/IOUPlugin.java`
* `java-source/src/test/java/net/corda/training/NodeDriver.java`
* `java-source/src/main/java/net/corda/training/plugin/IOUPlugin.java`
* `Not implemented:` `java-source/src/main/java/net/corda/training/flow/SelfIssueCashFlow.java`


# Running the CorDapp
To run the finished application, you have two choices for each language: from the terminal, and from IntelliJ.

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
