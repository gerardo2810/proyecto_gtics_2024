$(document).ready(function() {
    var opened = false;

    $('#menu-small').hide();

    $('#nav-toggle').click(function (e) {
        e.stopPropagation();
        if (opened) {
            $('#menu-small').hide();
        } else {
            $('#menu-small').show();
        }
        opened = !opened;
    });
});