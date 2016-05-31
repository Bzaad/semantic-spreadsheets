'use strict';
angular.module('uiApp')
	.config(function ($routeProvider) {
	    $routeProvider
	      .when('/', {
	        templateUrl: 'views/main.html',
	        controller: 'MainCtrl',
	        controllerAs: 'main'
	      })
	      .when('/addTable', {
	        templateUrl: 'views/addTable.html',
	        controller: 'AddCtrl',
	        controllerAs: 'add'
	      })
	      .when('/table', {
	        templateUrl: 'views/table.html',
	        controller: 'tableCtrl',
	        controllerAs: 'table'
	      })
	      .when('/query', {
	        templateUrl: 'views/queryTable.html',
	        controller: 'QueryCtrl',
	        controllerAs: 'query'
	      })
	      .otherwise({
	        redirectTo: '/'
	      });
	});