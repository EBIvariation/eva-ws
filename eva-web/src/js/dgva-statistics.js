/**
 * Created by jag on 18/10/2014.
 */
function DgvaStatistics(args) {
    _.extend(this, Backbone.Events);
    _.extend(this, args);
    this.rendered = false;
    this.render();


}

DgvaStatistics.prototype = {
    render: function () {
        var _this = this;
        if(!this.rendered) {
           var el =  document.querySelector("#"+this.targetId);
           var dgvaStatDiv = '<div class="row"><div id="dgva-statistics-chart-species" class="col-md-6"></div><div id="dgva-statistics-chart-type" class="col-md-6"></div></div>'
           el.innerHTML = dgvaStatDiv;
            EvaManager.get({
                category: 'meta/studies',
                resource: 'stats',
                params: {structural:true},
//                        query:variantID,
                success: function (response) {
                    try {
                        var stats = response.response[0].result[0];
                    } catch (e) {
                        console.log(e);
                    }
                    _this.parseData(stats);
                }
            });


        }
    },
    parseData: function (data) {
        var _this = this;
        var species_data  = data.species
        var type_data  = data.type
        var speciesArray=[];
        for (key in species_data) {
            speciesArray.push([key,  species_data[key]]);
        }
        var speciesChartData = {id:'dgva-statistics-chart-species',title:'Species',chartData:speciesArray};
        _this.drawChart(speciesChartData)
        var typeArray=[];
        for (key in type_data) {
            // TODO We must take care of the types returned
            if(key.indexOf(',') == -1) {
                typeArray.push([key,  type_data[key]]);
            }
        }
        var typeChartData = {id:'dgva-statistics-chart-type',title:'Type',chartData:typeArray};
        _this.drawChart(typeChartData)


    },
    drawChart: function (data) {
            var _this = this;
            var height = 290;
            var width = 200;
            if(data.id == 'dgva-statistics-chart-type'){

            }else if(data.id == 'dgva-statistics-chart-species'){
                data.chartData = data.chartData.slice(0, 5);

            }
            var id = '#'+data.id;
            var render_id = document.querySelector(id);
            var dataArray  = data.chartData;
            var title = data.title;
            $(function () {
                Highcharts.setOptions({
                    colors: ['#207A7A', '#2BA32B','#2E4988','#54BDBD', '#5DD15D','#6380C4', '#70BDBD', '#7CD17C','#7D92C4','#295C5C', '#377A37','#344366','#0A4F4F', '#0E6A0E','#0F2559' ],
                    chart: {
                        style: {
                            fontFamily: 'sans-serif;'
                        }
                    }
                });
                $(render_id).highcharts({
                    chart: {
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false,
                        height: height,
                        width: width,
                        marginLeft:-50,
                        marginTop:50

                    },
                    legend: {
                        enabled: true,
                        width: 200,
                        margin: 0,
                        labelFormatter: function() {
                            return '<div>' + this.name + '('+ this.y + ')</div>';
                        },
                        layout:'vertical',
                        useHTML:true

                    },
                    title: {
                        text: 'Studies <br> <span style="font-size:12px;">by '+title+'</span>',
                        style: {
//                                    display: 'none'
                        },
                        align: 'left'
                    },
//                            subtitle: {
//                                text: 'by ' + title,
//                                style: {
////                                    display: 'none'
//                                },
//                                align: 'left'
//                            },
                    tooltip: {
                        pointFormat: '<b>{point.y}</b>'
//                                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false
                            },
                            showInLegend: true
                        }
                    },
                    series: [{
                        type: 'pie',
                        name: 'Studies by '+title,
                        data: dataArray
                    }],
                    credits: {
                        enabled: false
                    }
                });

            });


    }

}