'use strict';

angular.module('uiApp')
	.controller('tableCtrl', ['$scope', 'DataFactoryService', 'QueryService', function($scope, DataFactoryService, QueryService){
		$scope.rowHeaders = true;
		$scope.colHeaders = true;

		$scope.db = {
			items: DataFactoryService.generateArrayOfArrays(200, 20)
		};
		$scope.settings = {
			contextMenu: [
				'row_above', 'row_below', 'remove_row'
			]
	};
}]);