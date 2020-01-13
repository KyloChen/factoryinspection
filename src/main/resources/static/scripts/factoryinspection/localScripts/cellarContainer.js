document.oncontextmenu = function(e) {
    //阻止浏览器默认事件
    return false;//一般点击右键会出现浏览器默认的右键菜单，写了这句代码就可以阻止该默认事件。
};

var menu = document.getElementById("menu");
var alterBtn = document.getElementById("menu-alter");
var deleteBtn = document.getElementById("menu-delete");
var confirmBtn = document.getElementById("confirmAlterSensorBtn");
$(function(){
    $('.sensor-img').mousedown(function(e){
        if(3 == e.which){//鼠标右击
            if($(this).attr('sensorId')){
                //鼠标点的坐标
                var sensorId = $(this).attr('sensorId');
                menu.style.display = "block";
                //让自定义菜单随鼠标的箭头位置移动
                menu.style.left = e.clientX + "px";
                menu.style.top = e.clientY + "px";
                alterBtn.value = sensorId;
                deleteBtn.value = sensorId;
                confirmBtn.value = sensorId;
            }else {
                alert("该设备未启用,无法操作.");
            }
        }
    })
});
//实现点击document，自定义菜单消失
document.onclick = function() {
    menu.style.display = "none";
};

function showCurve(that){
    var sensorId = $(that).attr('data-sensorId');
    console.log(sensorId);
    if(sensorId == undefined || sensorId == null || sensorId == ''){
        alert("该设备未启用！");
        return;
    }
    var inputData = {};
    inputData.sensorId = sensorId;
    $.ajax({
        url: "/factoryinspection/winery/showSensorCurve",
        method: "post",
        data: inputData,
        success: function (result) {
            console.log(result);
            var divshow = $("#curve-container");
            divshow.text("");// 清空数据
            divshow.append(result);
            // $("#tableInfoContainer").html(result);
        },
        error: function () {
            alert("今日无数据录入。");
        }
    });
}
function alterSensor() {
    var sensorId = alterBtn.value;
    if(isEmpty(sensorId)){
        alert("当前设备无法修改，请重新选择");
    }
    var inputData = {};
    inputData.sensorId = sensorId;
    console.log(sensorId);
    $.ajax({
        url: "/factoryinspection/winery/showInfoBeforeAlter",
        method: "post",
        data: inputData,
        success: function (result) {
            console.log(result);
            var obj = JSON.parse(result);
            $("#plantCode").val(obj.plantCode);
            $("#territoryCode").val(obj.territoryCode);
            $("#teamCode").val(obj.teamCode);
            $("#pitCode").val(obj.pitCode);
            $("#rowCode").val(obj.rowCode);
            $("#cellarCode").val(obj.cellarCode);
            $("#sensorCode").val(obj.sensorCode);
        },
        error: function () {
            alert("删除失败.");
            window.history.go(0);
        }
    });
}

function confirmAlterSensor() {
    var truthBeTold = window.confirm("确认修改设备信息吗？");
    var sensorId = confirmBtn.value;
    if (truthBeTold) {
        var inputData = {};
        inputData.sensorId = sensorId;
        inputData.plantCode = $("#plantCode").val();
        inputData.territoryCode = $("#territoryCode").val();
        inputData.teamCode = $("#teamCode").val();
        inputData.pitCode = $("#pitCode").val();
        inputData.rowCode = $("#rowCode").val();
        inputData.cellarCode = $("#cellarCode").val();
        inputData.sensorCode = $("#sensorCode").val();
        console.log(inputData);
        $.ajax({
            url: "/factoryinspection/winery/alterSensor",
            method: "post",
            data: inputData,
            success: function (result) {
                alert("修改设备 "+ result + " 信息成功！");
                window.history.go(0);
            },
            error: function () {
                alert("删除失败.");
                window.history.go(0);
            }
        });
    }else{
        self.close();
    }
}

function deleteSensor() {
    var sensorId = deleteBtn.value;
    if(isEmpty(sensorId)){
        alert("当前设备无法删除，请重新选择");
    }
    var truthBeTold = window.confirm("确认删除终端吗？");
    var inputData = {};
    inputData.sensorId = sensorId;
    if (truthBeTold) {
        var confirmTwice = window.confirm("删除后数据无法恢复，请问是否确定删除？");
        if(confirmTwice){
            console.log(sensorId);
            $.ajax({
                url: "/factoryinspection/winery/deleteSensor",
                method: "post",
                data: inputData,
                success: function (result) {
                    alert("二次确认删除设备："+ result + "成功！");
                    window.history.go(0);
                },
                error: function () {
                    alert("删除失败.");
                    window.history.go(0);
                }
            });

        }else {
            self.close();
        }

    }else{
        self.close();
    }
}