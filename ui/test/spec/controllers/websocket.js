'use strict';

describe('Controller: WebsocketctrlCtrl', function () {

  // load the controller's module
  beforeEach(module('uiApp'));

  var WebsocketctrlCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    WebsocketctrlCtrl = $controller('WebsocketctrlCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(WebsocketctrlCtrl.awesomeThings.length).toBe(3);
  });
});
