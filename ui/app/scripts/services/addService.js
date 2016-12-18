'use strict';

angular.module('uiApp')
	.factory('AddService', ['$rootScope', '$http', 'ApiUrls', function($rootScope, $http, ApiUrls){
		var addTriples = function(allTriples){
			var tripleFeeder = function(){
				if (allTriples.length > 0){
					postTriple(allTriples.shift());
				} else  {
					console.log('done adding all triples');
				}
			};
			var postTriple = function(triple){
				var url = ApiUrls.baseUrl + 'triples/add/' + triple.subject + '/' + triple.predicate + '/' + triple.object; 
				$http.post(url)
				.success(function(response){
					console.log(response);
					tripleFeeder();
				})
				.error(function(error){
					console.log(error);
				})
				.finally(function(){
					console.log('done adding the triple!');
				});
			};
			tripleFeeder();
		};
		return {
			getTriples: function(triples){
				addTriples(triples);
			}
		};
	}]);