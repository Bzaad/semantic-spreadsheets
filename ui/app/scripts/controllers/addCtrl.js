'use strict';
angular.module('uiApp')
	.controller('AddCtrl', ['$scope', 'AddService', function($scope, AddService){
		$scope.rowHeaders = true;
		$scope.colHeaders = true;
		var allRequests = [];

		$scope.objectArray = 
		[
			[''],
			[]
		];
		$scope.save = function(){
			var predicates = $scope.objectArray[0];
			var requests = [];

			for (var j = 1; j<$scope.objectArray.length; j++){
				var subject = $scope.objectArray[j][0];
				requests = [];
				for (var i = 1; i<$scope.objectArray[j].length; i++){
					if ($scope.objectArray[j][i] === null){
						continue; 
					}
					var request = {
						object: $scope.objectArray[j][i],
						subject: subject,
						predicate: predicates[i]
					};
					if (!request.object){ 
						continue; 
					}
					requests.push(request);
				}
				if (!requests || requests.length === 0){
					 continue;
				}
				allRequests.push(requests);
			}
			_.flattenDepth(allRequests, 3);
			var triples = [].concat.apply([], allRequests);
			allRequests = [];
			AddService.getTriples(triples);
		};
	}]);