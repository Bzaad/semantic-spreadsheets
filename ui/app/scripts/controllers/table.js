'use strict'

angular.module('uiApp')
	.controller('tableCtrl', function($scope, dataFactory){
		$scope.rowHeaders = true;
		$scope.colHeaders = true;
		$scope.db = {
			items: dataFactory.generateArrayOfArrays(200, 20)
		};
		$scope.settings = {
			contextMenu: [
				'row_above', 'row_below', 'remove_row'
			]
	};
});