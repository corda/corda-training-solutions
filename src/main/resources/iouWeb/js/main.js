"use strict";

// Define your backend here.

angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
angular.module('demoAppModule').config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);