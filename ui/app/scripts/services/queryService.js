'use strict';

angular.module('uiApp')
	.service('QueryService', ['$rootScope', '$http', 'ApiUrls', function($rootScope, $http, ApiUrls){
		var result = {};
		var queryObject = function(request){
			var url = ApiUrls.baseUrl + 'triples/query/' + '_' + '/' + request.predicate + '/' + request.object;
			queryApiCall(url);

		};
		var querySubject = function(request){
			var url = ApiUrls.baseUrl + 'triples/query/' +  request.subject + '/' + request.predicate + '/' + '_';
			queryApiCall(url);
		};
		var queryApiCall = function(url){
			$http.get(url)
			.then(function(response){
				result = response;
				$rootScope.$broadcast('RESULT_READY');
			})
			.catch(function(error){
				console.log(error);
			})
			.finally(function(){
				console.log('done with the query');
			});
		};
		return {
			queryObject: function(request){
				queryObject(request);
			},
			querySubject: function(request){
				querySubject(request);
			},
			getResult: function(){
				return result;
			}
		};
	}]);
