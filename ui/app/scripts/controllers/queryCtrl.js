'use strict';
angular.module('uiApp')
	.controller('QueryCtrl', ['$scope', '$timeout', 'toaster', 'QueryService', function($scope, $timeout, toaster, QueryService){
		$scope.thePredicate = ''; 
		$scope.theSubject = '';
		$scope.theObject = '';
		var queryType = '';
		var currentColumn = 0;
		$scope.objectArray = 
		[
			[''],
			[]
		];
		$scope.queryResults = 'No results yet!';
		$scope.$on('RESULT_READY', function(){
			processResults(QueryService.getResult());
		});
		var processResults = function(results){
			var queryBySubject = function(){
				for (var i in $scope.objectArray){
					if($scope.objectArray[0][i] === results[$scope.theSubject].tPredicate){
						currentColumn = i;
						break;
					}
					if ($scope.objectArray[0][i] === null){
						$scope.objectArray[0][i] = results[$scope.theSubject].tPredicate;
						currentColumn = i;
						break;
					}
				}
				for(var i in $scope.objectArray){
					if($scope.objectArray[i][0] === results[$scope.theSubject].tObject){
						$scope.objectArray[i][currentColumn] = results[$scope.theSubject].tSubject;
						break;
					}
					if($scope.objectArray[i][0] === null){
						$scope.objectArray[i][0] = results[$scope.theSubject].tObject;
						break;
					}
				}
			};
			var queryByObject = function(){
				console.log('querying by object');
			};
			if(queryType === 'qByObj'){
				queryByObject();
			}else if(queryType === 'qBySbj'){
				queryBySubject();
			}
		};
		$scope.theQuery = function() {
			if($scope.thePredicate === ''){
				toaster.pop({
	                type: 'error',
	                title: 'Title text',
	                body: 'Body text',
	                timeout: 3000
            	});
			}
			if($scope.thePredicate !== '' && $scope.theSubject !== '' && $scope.theObject === ''){
				queryType = 'qBySbj';
				//query the subject
				var qSubject = {
					subject: $scope.theSubject,
					predicate: $scope.thePredicate
				};
				QueryService.querySubject(qSubject);
			}
			if($scope.thePredicate !== '' && $scope.theObject !== '' && $scope.theSubject === ''){
				queryType = 'qByObj';
				//query the subject
				var qObject = {
					object: $scope.theObject,
					predicate: $scope.thePredicate
				};
				QueryService.queryObject(qObject);
			}
		};

	}]);