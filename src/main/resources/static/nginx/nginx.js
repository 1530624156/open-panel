
function drawPie(selectId, title,current,total,showStr,color1,color2){
    // 总数固定270，隐藏90
    var all = 270;
    // 计算数值，应该在270了，里面占多少
    current = current * all / total;

    var myChart = echarts.init(document.getElementById(selectId));

  // 指定图表的配置项和数据
  var option = {
    title: {
        text: title,
        left: "center",
        bottom: "18px",
        textStyle: {
            fontSize: "0.8em"
        }
    },
    series: [
        {
            type: "pie",
            label: {
                show: false,
            },
            center: ["50%", "50%"],
            radius: ["50%", "70%"],
            startAngle: 220, //起始角度，根据实际需要调节
            data: [
                {
                    name: "用量",
                    value: current,
                    label: {
                        show: true,
                        position: "center",
                        formatter: showStr,
                        textStyle: {
                            baseline: "bottom",
                            fontSize: "0.8em",
                            color: "rgba(49, 49, 49, 1)",
                        },
                    },
                    labelLine: {
                        show: false,
                    },
                    itemStyle: {

                        color: {
                            type: "linear",
                            x: 0,
                            y: 0,
                            x2: 1,
                            y2: 1,
                            colorStops: [
                                {
                                    offset: 0,
                                    color: color1, // 0% 处的颜色
                                }
                            ],
                            global: false, // 缺省为 false
                        },


                    },
                },
                {
                    name: "rest", // 实际显示部分是总量-用量
                    value: all - current,
                    itemStyle: {
                        color: color2,
                    },
                },
                {
                    name: "bottom",
                    value: 100,//底部透明部分的颜色，看实际情况，如果需要的是半圆图，这个透明部分的value值就变成和all相同的值，以此类推，可以自己调节value的大小
                    itemStyle: {
                        color: "transparent",
                    },
                },
            ],
        },
    ],
};

  // 使用刚指定的配置项和数据显示图表。
  myChart.setOption(option);
}


function  drawLine(selectId,up,down,timeLine){
    option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#00BDBE'
                }
            }
        },
        legend: {
            data: ['上行', '下行'],
        },
        grid: {
            top: 20,
            bottom: 20,
            left: 30,
            right: 5
        },
        color:['#FE6F73','#8AE7C4'],
        xAxis: {
            axisLine: {
                lineStyle: {
                    color: '#E8E8E8'
                },
            },
            axisLabel: {
                color: '#494949'
            },
            // 坐标轴刻度
            axisTick: {
                show: false,
            },
            data: timeLine
        },
        yAxis:[
            {
                name:this.yname,
                type: 'value',
                nameTextStyle:{
                    color:'#323232'
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        color: '#E8E8E8'
                    }
                },
                axisLabel: {
                    color: '#494949'
                },
                // x轴对应的竖线
                splitLine: {
                    show: true,
                    lineStyle: {
                        color: '#E8E8E8'
                    }
                },
                // 坐标轴刻度
                axisTick: {
                    show: false,
                },
            },
            {
                name: this.xname,
                nameLocation: 'start',
                nameTextStyle:{
                    color:'#323232'
                },
                type: 'value',
                inverse: true, //是否反向
                axisLine: {
                    show: true,
                    lineStyle: {
                        color: '#E8E8E8'
                    }
                },
                axisLabel: {
                    color: '#494949'
                },
                // x轴对应的竖线
                splitLine: {
                    show: true,
                    lineStyle: {
                        color: '#E8E8E8'
                    }
                },
                // 坐标轴刻度
                axisTick: {
                    show: false,
                },
                min:1,
            }
        ],
        series: [
            {
                name: '上行',
                type: 'line',
                yAxisIndex: 0,
                data: up
            },
            {
                name: '下行',
                type: 'line',
                yAxisIndex: 0,
                data: down
            }
        ]
    };
    var myChart = echarts.init(document.getElementById(selectId));
    myChart.setOption(option);
}