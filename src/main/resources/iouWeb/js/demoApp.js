"use strict";

angular.module('demoAppModule').controller('DemoAppCtrl', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    // TODO: Change this back to /api/iou/
    const apiBaseURL = "http://localhost:10007/api/iou/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openCreateIOUModal = () => {
        const createIOUModal = $uibModal.open({
            templateUrl: 'createIOUModal.html',
            controller: 'CreateIOUModalCtrl',
            controllerAs: 'createIOUModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        createIOUModal.result.then(() => {}, () => {});
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