
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
            // $(this).everyTime("0.5s", function () {
            //     if(element.style.display == "block"){
            //         element.style.display = "none"
            //     }else {
            //         element.style.display = "block"
            //     }
            // });
            console.log("orange");
        }else if(alarmLevel == 4){
            $(this).attr("src", "../static/img/Marker-red.png");
            // $(this).everyTime("0.5s", function () {
            //     if(element.style.display == "block"){
            //         element.style.display = "none"
            //     }else {
            //         element.style.display = "block"
            //     }
            // });
            // console.log("red");
        }
    });

});
function isEmpty(value){
    if(value == null || value == "" || value == "undefined" || value == undefined || value == "null"){
        return true;
    }
    else{
        value = value.replace(/\s/g,"");
        if(value == ""){
            return true;
        }
        return false;
    }
}
function addFactory() {
    var loading = document.getElementById("loading");
    loading.style.display = "block";
}

function rectClick(event) {
    //获取相对于浏览器视口的坐标
    console.log('x:' + event.clientX + "  y:" +  event.clientY);
    $('#clientX').val(event.clientX);
    $('#clientY').val(event.clientY);
}

function saveFactory(event) {
    //获取相对于当前所指向对象的位置坐标
    var plantCode =$('#plantCode').val();
    var plantName =$('#plantName').val();
    var clientX = $('#clientX').val();
    var clientY = $('#clientY').val();
    if(isEmpty(plantCode) || isEmpty(plantName))
    {
        alert("请输入车间号及车间名称.");
    }
    console.log(clientX + "   " + clientY);
    var inputData = {};
    inputData.plantCode = plantCode;
    inputData.plantName = plantName;
    inputData.clientX = parseInt(clientX);
    inputData.clientY = clientY;
    $.ajax({
        url: "/factoryinspection/winery/savePlant",
        method: "post",
        data: inputData,
        success: function (result) {
            alert(result);
            window.history.go(0);
            // $("#tableInfoContainer").html(result);
        },
        error: function (result) {
            alert(result);
            window.history.go(0);
        }
    });
}

$(this).click(function(e) { // 在页面任意位置点击而触发此事件
    // e.target表示被点击的目标
    var machineType =$('#machineType').val();
    var plantId = $(e.target).attr("plantId");
    if(plantId == null){
        return;
    }
    location.href =  projectName+"/winery/index?plantId=" + plantId;
    // if(machineType=="server"){
    //     if(plantId == null){
    //         return;
    //     }
    //     // location.href =  projectName+"/serverfactory/serverfactorycontainer?factoryId=" + plantId;
    //     location.href =  projectName+"/winery/index?plantId=" + plantId;
    // }else if(machineType == "local"){
    //     if(plantId == null){
    //         return;
    //     }
    //     location.href =  projectName+"/winery/index?plantId=" + plantId;
    // }
});

function closeSaveFactory() {
    window.history.go(0);
}