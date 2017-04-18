"use strict";

angular.module('demoAppModule').controller('CreateIOUModalCtrl', function ($http, $location, $uibModalInstance, $uibModal, apiBaseURL, peers) {
    const createIOUModal = this;

    createIOUModal.peers = peers;
    createIOUModal.form = {};
    createIOUModal.formError = false;

    // Validate and create IOU.
    createIOUModal.create = () => {
        if (invalidFormInput()) {
            createIOUModal.formError = true;
        } else {
            createIOUModal.formError = false;

            const amount = createIOUModal.form.amount;
            const currency = createIOUModal.form.currency;
            const party = createIOUModal.form.counterparty;

            $uibModalInstance.close();

            const issueIOUEndpoint =
                apiBaseURL +
                `issue-iou?amount=${amount}&currency=${currency}&party=${party}`;

            // Issue PO and handle success / fail responses.
            $http.get(issueIOUEndpoint).then(
                (result) => createIOUModal.displayMessage(result),
                (result) => createIOUModal.displayMessage(result)
            );
        }
    };

    createIOUModal.displayMessage = (message) => {
        const createIOUMsgModal = $uibModal.open({
            templateUrl: 'createIOUMsgModal.html',
            controller: 'createIOUMsgModalCtrl',
            controllerAs: 'createIOUMsgModal',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        createIOUMsgModal.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    createIOUModal.cancel = () => $uibModalInstance.dismiss();

    // Validate the IOU.
    function invalidFormInput() {
        return isNaN(createIOUModal.form.amount) || (createIOUModal.form.counterparty === undefined);
    }
});

// Controller for success/fail modal dialogue.
angular.module('demoAppModule').controller('createIOUMsgModalCtrl', function ($uibModalInstance, message) {
    const createIOUMsgModal = this;
    createIOUMsgModal.message = message.data;
});