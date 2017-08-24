/**
 * Simple Alert for displaying warnings on the screen.
 */

bootstrap_alert = function () {}

bootstrap_alert.warning = function (message, alert, timeout) {

    $('<div id="floating_alert" class="alert alert-' + alert +
        ' fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>' +
        message + '&nbsp;&nbsp;</div>').appendTo('body');

    setTimeout(function () {
        $(".alert").alert('close');
    }, timeout);
};

$('#clickme').on('click', function () {
    bootstrap_alert.warning('Your text goes here <strong>html test</strong>', 'success', 4000);
    // available: success, info, warning, danger
});
