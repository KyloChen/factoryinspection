var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
$(document).ready(function() {
    $('.camera_player').each(function (i, e) {
        var player = new EZUIKit.EZUIPlayer(e.id);
    });
});