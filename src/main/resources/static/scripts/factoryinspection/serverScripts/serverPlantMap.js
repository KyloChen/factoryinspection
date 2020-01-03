var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
console.log("pathname: " + pathname + " , projectName: " + projectName);
$(document).ready(function () {
    $(".plant").each(function (index,element){
        var alarmLevel = $(this).attr("alarmLevel");
        var clientX = $(this).attr("clientX");
        var clientY = $(this).attr("clientY");
        var plantId = $(this).attr("plantId");
        console.log(typeof(clientX) + " , " + typeof(clientY) +"aaa");
        $(this).css("left", parseInt(clientX));
        $(this).css("top", parseInt(clientY));
        if(alarmLevel == 1){
            $(this).attr("src", "../static/img/Marker-blue.png");
            console.log("blue");
        }else if(alarmLevel == 2){
            $(this).attr("src", "../static/img/Marker-yellow.png");
            console.log("yellow");
        }else if(alarmLevel == 3){
            $(this).attr("src", "../static/img/Marker-orange.png");
            console.log("orange");
        }else if(alarmLevel == 4){
            $(this).attr("src", "../static/img/Marker-red.png");
        }
    });

});
$(this).click(function(e) { // 在页面任意位置点击而触发此事件
    // e.target表示被点击的目标
    var machineType =$('#machineType').val();
    var plantId = $(e.target).attr("plantId");
    if(plantId == null){
        return;
    }
    location.href =  projectName+"/server/index?plantId=" + plantId;

});