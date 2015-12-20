$.fn.serializeObject = function () {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function () {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};
$.fn.getUrlParameter = function (sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
};

$(document).ready(function () {
    $("form#ajax-form").submit(function (e) {

        $("form#ajax-form button[type='submit'], form#ajax-form input[type='submit']").prop('disabled', true);

        var formData = $('form#ajax-form').serializeObject();

        console.log(formData);

        $.ajax({
            url: window.location.pathname,
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(formData),
            success: function (data, msg) {
                console.log(data);

                if (data.status === "ok") {
                    var rurl = $().getUrlParameter('r');
                    if (rurl !== undefined) {
                        window.location.replace(rurl);
                    } else {
                        window.location.replace(data.url);
                    }
                } else {
                    alert(data.msg);
                    $("form#ajax-form button[type='submit'], form#ajax-form input[type='submit']").prop('disabled', false);
                }
            },
            error: function (jqXHR, msg, throws) {
                console.log(jqXHR);
                console.log(msg);
                console.log(throws);
                alert("Ошибка");
                $("form#ajax-form button[type='submit'], form#ajax-form input[type='submit']").prop('disabled', false);
            }
        });

        e.preventDefault();
        return false;

    });
});