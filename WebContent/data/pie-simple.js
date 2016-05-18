option = {
    title : {
        text: 'Example Site Visit Source',
        subtext: 'Fictitious',
        x:'center'
    },
    tooltip : {
        trigger: 'item',
        formatter: "{a} <br/>{b} : {c} ({d}%)"
    },
    legend: {
        orient: 'vertical',
        left: 'left',
        data: ['Direct Visit','Email Marketing','Advertising Alliance','Video Ads','Search Engine']
    },
    series : [
        {
            name: 'Visit Sources',
            type: 'pie',
            radius : '55%',
            center: ['50%', '60%'],
            data:[
                {value:335, name:'Direct Visit'},
                {value:310, name:'Email Marketing'},
                {value:234, name:'Advertising Alliance'},
                {value:135, name:'Video Ads'},
                {value:1548, name:'Search Engine'}
            ],
            itemStyle: {
                emphasis: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }
    ]
};
