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