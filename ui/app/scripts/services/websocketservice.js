'use strict';

/**
 * @ngdoc service
 * @name uiApp.webSocketService
 * @description
 * # webSocketService
 * Service in the uiApp.
 */
angular.module('uiApp')
    .service('webSocketService', function($websocket) {
        // AngularJS will instantiate a singleton by calling "new" on this function
        //Open a WebSocket connection
        var dataStream = $websocket('ws://website.com/data');
        var collection = [];
        dataStream.onMessage(function(message) {
            collection.push(JSON.parse(message.data));
        });
        var methods = {
            collection: collection,
            get: function() {
                dataStream.send(JSON.stringify({ action: 'get' }));
            }
        };
        return methods;
    });