(function() {
    $(function() {
        var form;
        form = $(".button-form");
        if (form) {
            if ($(".button-form-link")) {
                $(".button-form-link").click(function() {
                    return form.submit();
                });
            }
        }
        return window.setTimeout((function() {
            return $(".alert").fadeTo(1500, 0).slideUp(500, function() {
                return $(this).remove();
            });
        }), 2000);
    });

}).call(this);