var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
function showAlertInfo(showId) {
    var loading = document.getElementById(showId);
    loading.style.display = "block";
}
function hideAlertInfo(hideId) {
    var loading = document.getElementById(hideId);
    loading.style.display = "none";
}
function showDetail(that) {
    console.log("clicked the curve.");
    var sensorId = $(that).attr('data-sensorId');
    if(sensorId == null){
        return;
    }
    location.href =  projectName+"/server/serverCurveDetail?sensorId=" + sensorId;
}