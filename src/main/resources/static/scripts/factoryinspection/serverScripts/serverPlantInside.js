var player, player1, player2;
var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
$(document).ready(function() {
    player = new EZUIKit.EZUIPlayer('myPlayer');
    player1 = new EZUIKit.EZUIPlayer('myPlayer1');
    player2 = new EZUIKit.EZUIPlayer('myPlayer2');
});
function showCellarContainer(that) {
    var pitId = $(that).attr('data-pitId');
    if(pitId == undefined || pitId == null || pitId == ''){
        alert("该垮区未启用(无设备)！");
        return;
    }
    var inputData = {};
    console.log(pitId);
    inputData.pitId = pitId;
    $.ajax({
        url: "/factoryinspection/server/showCellar",
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
        url: "/factoryinspection/server/getThresholdTable",
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
                ]
            })
        }
    });
}

function showAllSensor(that){
    var plantId = $(that).attr('data-plantId');
    location.href =  projectName+"/server/showAllSensor?plantId=" + plantId;
}
