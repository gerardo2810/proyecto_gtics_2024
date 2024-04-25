const getOptionChart1=()=>{
    return {
        tooltip: {
            show: true,
            trigger: 'axis',
            triggerOn: "mousemove|click"
        },
        dataZoom:{
          show: true,
            start: 5
        },
        xAxis: {
            type: 'category',
            data: ['Apronax', 'Doloral', 'Ibuprofeno', 'Panadol', 'Paracetamol', 'Salbutamol', 'Loratadina']
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                data: [5, 7, 9, 10, 14, 20, 3],
                type: 'line'
            }
        ]
    };
};

const getOptionChart2=()=>{
    return {
        color:["#3246a8","#00cc66","#ff5050"],
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['7 días', '15 días', '31 días']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: ['Apronax', 'Doloral', 'Ibuprofeno', 'Panadol', 'Paracetamol', 'Salbutamol', 'Loratadina']
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                name: '7 días',
                type: 'line',
                stack: 'Total',
                data: [120, 132, 101, 134, 90, 230, 210]
            },
            {
                name: '15 días',
                type: 'line',
                stack: 'Total',
                data: [220, 182, 191, 234, 290, 330, 310]
            },
            {
                name: '31 días',
                type: 'line',
                stack: 'Total',
                data: [150, 232, 201, 154, 190, 330, 410]
            }
        ]
    };
};
/*
const getOptionChart3=()=>{
    return {
        tooltip:{
            show:true
        },
        xAxis: {
            type: 'category',
            data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                data: [120, 200, 150, 80, 70, 110, 130],
                type: 'bar'
            }
        ]
    };
}
*/
const getOptionChart4=()=>{
    return {
        tooltip: {
            trigger: 'item'
        },
        legend: {
            top: '5%',
            left: 'center'
        },
        series: [
            {
                name: 'Access From',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 40,
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: false
                },
                data: [
                    { value: 1048, name: 'Pando Etapa 2' },
                    { value: 735, name: 'San Miguelito' },
                    { value: 580, name: 'Pando Etapa 7' },
                    { value: 484, name: 'Pando Etapa 3' },
                    { value: 300, name: 'Pando Etapa 4' }
                ]
            }
        ]
    };
}

const initCharts=()=>{
    const chart1 = echarts.init(document.getElementById("chart1"));
    const chart2 = echarts.init(document.getElementById("chart2"));
    //const chart3 = echarts.init(document.getElementById("chart3"));
    const chart4 = echarts.init(document.getElementById("chart4"));

    chart1.setOption(getOptionChart1());
    chart2.setOption(getOptionChart2());
    //chart3.setOption(getOptionChart3());
    chart4.setOption(getOptionChart4());
}

window.addEventListener("load",()=> {
    initCharts();
})
