'use strict';

describe('Service: webSocketService', function () {

  // load the service's module
  beforeEach(module('uiApp'));

  // instantiate service
  var webSocketService;
  beforeEach(inject(function (_webSocketService_) {
    webSocketService = _webSocketService_;
  }));

  it('should do something', function () {
    expect(!!webSocketService).toBe(true);
  });

});
