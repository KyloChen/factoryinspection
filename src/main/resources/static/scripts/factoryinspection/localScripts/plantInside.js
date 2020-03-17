var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
$(document).ready(function() {
    $('.camera_player').each(function (i, e) {
        var player = new EZUIKit.EZUIPlayer(e.id);
    });
});
function isEmpty(value){
    if(value == null || value == "" || value == "undefined" || value == undefined || value == "null"){
        return true;
    }
}

function saveSensorCode() {
    var sensorCode = document.getElementById("sensorCode").value;
    $("#saveTerminalBtn").attr("disabled", true);
    var inputData = {};
    inputData.sensorCode = sensorCode;
    if(isEmpty(sensorCode)){
        alert("设备特征码不能为空!");
        return;
    }
    console.log(inputData);
    $.ajax({
        //几个参数需要注意一下
        type: "POST",//方法类型
        contentType:"application/x-www-form-urlencoded",
        url: "/factoryinspection/winery/addSensor" ,//url
        data: inputData,
        success: function (result) {
            $("#saveTerminalBtn").attr("disabled", false);
            alert(result);
            window.history.go(0);
        },
        error : function(result) {
            $("#saveTerminalBtn").attr("disabled", false);
            alert(result);
            console.log(result);
        }
    });
}
function showCellarContainer(that) {
    var pitId = $(that).attr('data-pitId');
    var plantId = document.getElementById("plantId").value;
    if(pitId == undefined || pitId == null || pitId == ''){
        alert("该垮区未启用(无设备)！");
        return;
    }
    var inputData = {};
    console.log(pitId);
    inputData.pitId = pitId;
    inputData.plantId = plantId;
    $.ajax({
        url: "/factoryinspection/winery/showCellar",
        method: "post",
        data: inputData,
        success: function (result) {
            var divshow = $("#cellar-container");
            divshow.text("");// 清空数据
            divshow.append(result);

            // $("#tableInfoContainer").html(result);
        },
        error: function () {
            alert("error showing cellar container.");
        }
    });
}
function showCellarInfo(that){
    var cellarId = $(that).attr('data-cellarId');
    var inputData = {};
    console.log(cellarId);
    inputData.cellarId = cellarId;
    $.ajax({
        url: "/factoryinspection/winery/showCellarInfo",
        method: "post",
        data: inputData,
        success: function (result) {
            var divshow = $("#cellar-container");
            divshow.text("");// 清空数据
            divshow.append(result);
            // $("#tableInfoContainer").html(result);
        },
        error: function () {
            alert("error showing cellar container.");
        }
    });
}
function getThresholdTable() {
    $.ajax({
        method: "post",
        url: "/factoryinspection/winery/getThresholdTable",
        dataType: "json",
        success: function (result) {
            $('#table').bootstrapTable({
                data: result, //数据源
                striped: true, //表格显示条纹
                contentType:"application/x-www-form-urlencoded",
                dataType: 'json',
                // pagination: true, //是否分页
                // pageSize: 10, //每页显示的记录数
                // pageNumber:1, //当前第几页
                // search: true, //是否可以查询
                smartDisplay:false,
                // pageList: [5, 10, 15, 20, 25], //记录数可选列表
                columns: [{
                    field: 'alarmLevel',
                    title: '告警等级',
                    align : 'center',
                    valign: 'middle',
                    formatNoMatches:function(){
                        return "无阈值设置，请添加";
                    },
                    sortable:true
                },
                    {
                        field: 'alarmColor',
                        title: '告警颜色',
                        align : 'center',
                        valign: 'middle',
                        formatter: function operateFormatter(value, row, index){
                            if( value == "green"){
                                return "<span style='color:forestgreen;'>绿色正常预警</span>";
                            }else if(value == "orange"){
                                return "<span style='color:darkorange;'>橙色中度预警</span>";
                            }else if(value == "red"){
                                return "<span style='color:red;'>红色严重警告</span>";
                            }
                        }
                    },
                    {
                        field: 'minValue',
                        title: '最小告警值',
                        align : 'center',
                        valign: 'middle'
                    },
                    {
                        field: 'maxValue',
                        title: '最大告警值',
                        align : 'center',
                        valign: 'middle'
                    }
                    , {
                        //按钮
                        field: 'operate',
                        title: '操作',
                        //事件
                        events: window.operateEvents,
                        //按钮样式
                        formatter: AddFunctionAlty,
                        align : 'center',
                        valign: 'middle'
                    }
                ]
            })
        }
    });

}
function AddFunctionAlty(value, row, index){
    return['<a class="tableDeleteThreshold" href="javascript:void(0)"><i class="fa fa-remove"></i></a>'].join("");
}
window.operateEvents = {
    "click .tableDeleteThreshold": function (e, value, row, index){
        var truthBeTold = window.confirm("单击“确定”继续。单击“取消”停止。");
        if (truthBeTold) {
            var inputData = {};
            inputData.alarmLevel = row.alarmLevel;
            $.ajax({
                //几个参数需要注意一下
                type: "POST",//方法类型
                contentType:"application/x-www-form-urlencoded",
                url: "/factoryinspection/winery/deleteThreshold" ,//url
                data: inputData,
                success: function (result) {
                    alert("success 成功! "  + JSON.stringify(result));
                    refreshTable();
                },
                error: function (result) {
                    alert("error" + JSON.stringify(result));
                    refreshTable();
                }
            })
        } else {
            self.close();
        }
    }
};
function setThreshold(){
    console.log("you clicked tableaddthreshold");
    var inputData = {};
    var modelAlarmLevel = document.getElementById("modelAlarmLevel").value;
    var modelMinValue = document.getElementById("modelMinValue").value;
    var modelMaxValue = document.getElementById("modelMaxValue").value;
    if(isEmpty(modelAlarmLevel) || isEmpty(modelMinValue) || isEmpty(modelMaxValue)){
        alert("请输入要设置的报警值！");
        return;
    }
    inputData.alarmLevel = modelAlarmLevel;
    inputData.minValue = modelMinValue;
    inputData.maxValue = modelMaxValue;
    $.ajax({
        //几个参数需要注意一下
        type: "POST",//方法类型
        contentType:"application/x-www-form-urlencoded",
        url: "/factoryinspection/winery/setThreshold" ,//url
        data: inputData,
        success: function (result) {
            alert(JSON.stringify(result));
            refreshTable();
        },
        error: function (result) {
            alert("error: " + JSON.stringify(result));
            refreshTable();
        }
    })
}
function refreshTable(){
    var opt = {
        url: "/factoryinspection/winery/getThresholdTable",
        silent: true
    };
    $("#table").bootstrapTable('refresh', opt);
}

window.onbeforeunload = function(){
    console.log("onbeforeunload is work");
};

function showAllSensor(that){
    var plantId = $(that).attr('data-plantId');
    location.href =  projectName+"/winery/showAllSensor?plantId=" + plantId;
}
