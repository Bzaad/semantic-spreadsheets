/**
 * Simple Alert for displaying warnings on the screen.
 */

bootstrap_alert = function () {}

bootstrap_alert.warning = function (message, alert, timeout) {
    // available: success, info, warning, danger
    $('<div id="floating_alert" class="alert alert-' + alert +
        ' fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>' +
        message + '&nbsp;&nbsp;</div>').appendTo('body');

    setTimeout(function () {
        $(".alert").alert('close');
    }, timeout);
};