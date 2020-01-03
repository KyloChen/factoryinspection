$(function(){
    $('#startDate').datetimepicker({
        language:"zh-CN", //汉化
        todayBtn : "true",  //显示今天按钮
        autoclose : true,   //选择日期后自动关闭日期选择框
        format : "yyyy-mm-dd",
        todayHighlight : true,   //当天高亮显示
        minView: "month",   //不显示时分秒
        showMeridian: 1,
        pickerPosition: "bottom-left",
        startDate:new Date(new Date()-1000 * 60 * 60 * 24 * 365),  //只显示一年的日期365天
        endDate : new Date()
    }).on('click',function(e){
        $("#startDate").datetimepicker("setEndDate", $("#endDate").val());
    });
    $('#endDate').datetimepicker({
        language:"zh-CN",
        todayBtn : "true",
        autoclose : true,
        format : "yyyy-mm-dd",
        todayHighlight : true,
        minView: "month",
        pickerPosition: "bottom-left",
        startDate:new Date(new Date()-1000 * 60 * 60 * 24 * 365),
        endDate : new Date()
    }).on('click',function(e){
        $("#endDate").datetimepicker("setStartDate", $("#startDate").val());
    });
});

function resetTimeRange() {
    $("#startDate").val("");
    $("#endDate").val("");
    myChart.clear();
    myChart.setOption(dailyOption);
}

function showFullScreen() {
    var fullScreenLayer = document.getElementById('fullScreenLayer');
    var closeBtn = document.getElementById('fullScreenLayerBtn');
    closeBtn.style.display = 'block';
    fullScreenLayer.style.display = 'block';
    fullScreenChart.clear();
    window.onresize = fullScreenChart.resize;
    fullScreenChart.setOption(myChart.getOption());
}

function closeFullScreen() {
    var fullScreenLayer = document.getElementById('fullScreenLayer');
    var closeBtn = document.getElementById('fullScreenLayerBtn');
    closeBtn.style.display = 'none';
    fullScreenLayer.style.display = 'none';
    fullScreenChart.clear();
}

function dailyLine(){
    myChart.clear();
    myChart.setOption(dailyOption);
}

var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
function backToWinery() {
    myChart.clear();
    fullScreenChart.clear();

    window.history.back();
}
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
function lastHours() {
    var hours = $('#hours').val();
    if(isEmpty(hours)){
        alert("小时数不能为空.");
        return;
    }
    var sensorId = $('#sensorId').val();
    var inputData = {};
    inputData.sensorId = sensorId;
    inputData.hours = hours;
    console.log(inputData);
    $.ajax({
        type: "POST",//方法类型
        dataType: "json",//预期服务器返回的数据类型
        url: "/factoryinspection/server/showLastSixHours",//url
        data: inputData,
        success: function (result) {
            console.log(result);
            // 指定图表的配置项和数据
            if (JSON.stringify(result) === '{}') {
                alert("当前范围无历史记录.");
                return;
            }
            var historyOption = {
                noDataLoadingOption: {
                    text: '暂无数据',
                    effect: 'bubble',
                    effectOption: {
                        effect: {
                            n: 0
                        }
                    }
                },
                title: {
                    text: '前 '+ hours + ' 小时窖藏温度',
                    subtext: ''
                }, //图表左上方简介
                legend: {
                    data: ['上层温度值', '中层温度值', '底层温度值']
                }, //显示曲线简介

                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross'
                    }
                },
                toolbox: {
                    show: true,
                    feature: {
                        dataZoom: {
                            show: true,
                            yAxisIndex: 'none'
                        },
                        dataView: {
                            show: true,
                            readOnly: false
                        },
                        magicType: {type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                xAxis: {
                    name: "时间(时：分)",
                    type: 'category',
                    boundaryGap: false,
                    data: result.rangeCreatedTime,
                    axisLabel: {
                        //处理时间格式
                        formatter: function (value, index) {
                            var date = new Date(value);
                            var texts = [date.getFullYear(), (date.getMonth() + 1), date.getDate() + "," + date.getHours() + ":" + date.getMinutes()];
                            // if (index === 0) {
                            //     texts.unshift(date.getFullYear());
                            // }
                            return texts.join('/');
                        }
                    }

                },
                yAxis: {
                    name: "温度值",
                    type: 'value',
                    axisLabel: {
                        formatter: '{value} °C'
                    },
                    axisPointer: {
                        snap: true
                    }
                },

                series: [
                    {
                        name: '上层温度值',
                        type: 'line',
                        smooth: true,
                        data: result.rangeTopTemp
                    },
                    {
                        name: '中层温度值',
                        type: 'line',
                        smooth: true,
                        connectNulls: true,//连接数据为null的前后点
                        data: result.rangeMidTemp
                    },
                    {
                        name: '底层温度值',
                        type: 'line',
                        smooth: true,
                        data: result.rangeBotTemp
                    }
                ]

            };
            myChart.clear();
            myChart.hideLoading();
            myChart.setOption(historyOption);
        }
    })
}

function queryByDateRange() {
    var startDate = $('#startDate').val();
    var endDate = $('#endDate').val();
    var sensorId = $('#sensorId').val();
    var inputData = {};
    if(isEmpty(startDate) || isEmpty(endDate)){
        alert("请输入正确的日期范围！");
        return;
    }
    console.log(startDate + " , " + endDate + " , " + sensorId);
    inputData.startDate = startDate;
    inputData.endDate = endDate;
    inputData.sensorId = sensorId;
    console.log(inputData);
    $.ajax({
        type: "POST",//方法类型
        dataType: "json",//预期服务器返回的数据类型
        url: "/factoryinspection/server/queryByDateRange" ,//url
        data: inputData,
        success: function (result) {
            // console.log(typeof result);
            // var obj = JSON.parse(result);
            // console.log(obj);
            if (JSON.stringify(result) === '{}') {
                alert("当前范围无历史记录.");
                return;
            }
            var historyQueryOption = {
                noDataLoadingOption: {
                    text: '暂无数据',
                    effect: 'bubble',
                    effectOption: {
                        effect: {
                            n: 0
                        }
                    }
                },
                title: {
                    text: '范围窖藏温度',
                    subtext: ''
                },
                legend: {
                    data:['上层温度值', '中层温度值', '底层温度值']
                },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross'
                    }
                },
                toolbox: {
                    show: true,
                    feature: {
                        dataZoom: {
                            show: true,
                            yAxisIndex: 'none'
                        },
                        dataView: {
                            show: true,
                            readOnly: false},
                        magicType: {type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                xAxis:  {
                    type: 'category',
                    boundaryGap: false,
                    data: result.rangeCreatedDate,
                    axisLabel: {
                        formatter: function (value, index) {
                            // 格式化成月/日，只在第一个刻度显示年份
                            var date = new Date(value);
                            var texts = [date.getFullYear(), (date.getMonth() + 1), date.getDate()+ "," +date.getHours() + ":" + date.getMinutes()];
                            // if (index === 0) {
                            //     texts.unshift(date.getFullYear());
                            // }
                            return texts.join('/');
                        }
                    }
                },
                yAxis: {
                    type: 'value',
                    axisLabel: {
                        formatter:'{value} °C'
                    },
                    axisPointer: {
                        snap: true
                    }
                },

                series: [
                    {
                        name:'上层温度值',
                        type:'line',
                        smooth: true,
                        data: result.rangeTopTemp
                    },
                    {
                        name:'中层温度值',
                        type:'line',
                        smooth: true,
                        connectNulls: true ,//连接数据为null的前后点
                        data: result.rangeMidTemp
                    },
                    {
                        name:'底层温度值',
                        type:'line',
                        smooth: true,
                        data: result.rangeBotTemp
                    }
                ]
            };
            myChart.clear();
            myChart.hideLoading();
            myChart.setOption(historyQueryOption);
        },
        error : function(result) {
            alert("此时段无温度数据");
        }
    })
}