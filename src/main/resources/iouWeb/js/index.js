"use strict";

// Define your backend here.

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    // TODO: Change this back to /api/iou/
    const apiBaseURL = "http://localhost:10007/api/iou/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openModal = () => {
        const issueModalInstance = $uibModal.open({
            templateUrl: 'demoAppModal.html',
            controller: 'IssueModalCtrl',
            controllerAs: 'issueModalInstance',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        issueModalInstance.result.then(() => {}, () => {});
    };

    demoApp.getIOUs = () => $http.get(apiBaseURL + "ious")
        .then((response) => demoApp.ious = Object.keys(response.data)
            .map((key) => response.data[key].state.data));

    demoApp.getCashBalances = () => $http.get(apiBaseURL + "cash-balances")
        .then((response) => demoApp.cashBalances = response.data);

    // TODO: DEFINE HOW THESE WORK

    // TODO: Define transfer method
//    demoApp.transferIOU = () => {
//        const id = ;
//        const party = ;

//        $http.get(apiBaseURL + "transfer-iou");
//    }

    // TODO: Define settle method
//    demoApp.settleIOU = () => $http.

    // TODO: Define cash issuance


    demoApp.getIOUs();
    demoApp.getCashBalances();

    console.log(demoApp)
});

app.controller('IssueModalCtrl', function ($http, $location, $uibModalInstance, $uibModal, apiBaseURL, peers) {
    const issueModalInstance = this;

    issueModalInstance.peers = peers;
    issueModalInstance.form = {};
    issueModalInstance.formError = false;

    // Validate and create IOU.
    issueModalInstance.create = () => {
        if (invalidFormInput()) {
            issueModalInstance.formError = true;
        } else {
            issueModalInstance.formError = false;

            const amount = issueModalInstance.form.amount;
            const currency = issueModalInstance.form.currency;
            const party = issueModalInstance.form.counterparty;

            $uibModalInstance.close();

            const issueIOUEndpoint =
                apiBaseURL +
                `issue-iou?amount=${amount}&currency=${currency}&party=${party}`;

            // Issue PO and handle success / fail responses.
            $http.get(issueIOUEndpoint).then(
                (result) => issueModalInstance.displayMessage(result),
                (result) => issueModalInstance.displayMessage(result)
            );
        }
    };

    issueModalInstance.displayMessage = (message) => {
        const issueMsgModalInstance = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'IssueMsgModalCtrl',
            controllerAs: 'issueMsgModalInstance',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        issueMsgModalInstance.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    issueModalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the IOU.
    function invalidFormInput() {
        return isNaN(issueModalInstance.form.amount) || (issueModalInstance.form.counterparty === undefined);
    }
});

// Controller for success/fail modal dialogue.
app.controller('IssueMsgModalCtrl', function ($uibModalInstance, message) {
    const issueMsgModalInstance = this;
    issueMsgModalInstance.message = message.data;
});