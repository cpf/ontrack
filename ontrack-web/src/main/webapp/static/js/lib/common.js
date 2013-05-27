define(function () {

    var logging = false;

    String.prototype.format = function () {
        var args = arguments;
        return this.replace(/\{\{|\}\}|\{(\d+)\}/g, function (m, n) {
            if (m == "{{") {
                return "{";
            }
            if (m == "}}") {
                return "}";
            }
            return args[n];
        });
    };

    String.prototype.html = function () {
        return $('<i></i>').text(this).html();
    };

    String.prototype.htmlWithLines = function () {
        var text = this.html();
        return text.replace(/\n/g, '<br/>');
    };

    String.prototype.loc = function (args) {
        var code = this;
        var text = l[code];
        if (text != null) {
            return text.format(args);
        } else {
            return "##" + code + "##";
        }
    };

    function log(context) {
        return function (message, args) {
            if (logging && console) {
                if (args) {
                    console.log('[{1}] {0}'.format(message, context), args);
                } else {
                    console.log('[{1}] {0}'.format(message, context));
                }
            }
        }
    }

    function confirmAndCall(text, callback) {
        $('<div>{0}</div>'.format(text)).dialog({
            title: 'general.confirm.title'.loc(),
            dialogClass: 'confirm-dialog',
            modal: true,
            buttons: {
                Ok: function () {
                    $(this).dialog("close");
                    callback();
                },
                Cancel: function () {
                    $(this).dialog("close");
                }
            }
        });
    }

    // source: http://www.w3schools.com/js/js_cookies.asp
    function getCookie(c_name) {
        var i, x, y, ARRcookies = document.cookie.split(";");
        for (i = 0; i < ARRcookies.length; i++) {
            x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
            y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
            x = x.replace(/^\s+|\s+$/g, "");
            if (x == c_name) {
                return unescape(y);
            }
        }
    }

    function tooltips () {
        $('.tooltip-source').tooltip({
            placement: 'bottom'
        });
    }

    function values (base) {
        var data = {};
        $(base).find('input,textarea,select').each (function (index, field) {
            if (field.getAttribute('readonly') != 'readonly' && field.getAttribute('disabled') != 'disabled') {
                var name = field.getAttribute('name');
                var value = field.value;
                data[name] = value;
            }
        });
        return data;
    }

    return {
        log: log,
        confirmAndCall: confirmAndCall,
        getCookie: getCookie,
        tooltips: tooltips,
        values: values
    }

});