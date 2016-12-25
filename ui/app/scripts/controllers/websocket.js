'use strict';

/**
 * @ngdoc function
 * @name uiApp.controller:WebsocketctrlCtrl
 * @description
 * # WebsocketctrlCtrl
 * Controller of the uiApp
 */
angular.module('uiApp')
    .controller('WebsocketctrlCtrl', function($scope, webSocketService) {
        $scope.data = webSocketService;
        this.awesomeThings = [
            'HTML5 Boilerplate',
            'AngularJS',
            'Karma'
        ];
    });