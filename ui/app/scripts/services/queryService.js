'use strict';

angular.module('uiApp')
	.service('QueryService', ['$rootScope', '$http', function($rootScope, $http){

		var url = 'http://localhost:9000/triples/query/_/hasJob/Programmer';

		var getTestLink = function(){
		$http.get(url)
			.success(function(response){
				console.log(response);
			})
			.error(function(error){
				console.log(error);
			})
			.finally(function(){
				console.log('done!');
			})
		}

		getTestLink();

		return {
			getUrls: function(){
				ApiUrls;
			}
		};
	}]);