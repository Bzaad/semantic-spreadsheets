'use strict';

angular.module('uiApp')
	.controller('QueryCtrl', ['$scope', 'QueryService', function($scope, QueryService){
		$scope.qObject = {
			object:'',
			predicate: ''
		}
		$scope.qSubject = {
			subject: '',
			predicate: ''
		}

		$scope.queryResults = 'No results yet!';

		$scope.$on('RESULT_READY', function(){
			$scope.queryResults = QueryService.getResult();
		})

		$scope.queryObject = function(){
			QueryService.queryObject($scope.qObject);
		}
		$scope.querySubject = function(){
			QueryService.querySubject($scope.qSubject);
		}

	}])