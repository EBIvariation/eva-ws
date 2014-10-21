/**
 * Created by jag on 18/10/2014.
 */
function EvaStatistics(args) {
    _.extend(this, Backbone.Events);
    _.extend(this, args);
    this.rendered = false;
    this.render();


}

EvaStatistics.prototype = {
    render: function () {
        var _this = this;
        if(!this.rendered) {
           var el =  document.querySelector("#"+this.targetId);
           var evaStatDiv = '<div class="row"><div id="eva-statistics-chart-species" class="col-md-6">dfef</div><div id="eva-statistics-chart-type" class="col-md-6"></div></div>'
           el.innerHTML = evaStatDiv;
            EvaManager.get({
                host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
                category: 'meta/studies',
                resource: 'stats',
//                        query:variantID,
                success: function (response) {
                    try {
                        var stats = response.response[0].result[0];
                    } catch (e) {
                        console.log(e);
                    }
                    _this._parseData(stats);
                }
            });


        }
    },
    _parseData: function (data) {
            var _this = this;
            var species_data  = data.species
            var type_data  = data.type
            var speciesArray=[];
            for (key in species_data) {
                speciesArray.push([key,  species_data[key]]);
            }
            var speciesChartData = {id:'eva-statistics-chart-species',title:'Species',chartData:speciesArray};
            _this._drawChart(speciesChartData)
            var typeArray=[];
            for (key in type_data) {
                // TODO We must take care of the types returned
                if(key.indexOf(',') == -1) {
                    typeArray.push([key,  type_data[key]]);
                }
            }
            var typeChartData = {id:'eva-statistics-chart-type',title:'Type',chartData:typeArray};
            _this._drawChart(typeChartData)


    },
    _drawChart: function (data) {
            var _this = this;
            var height = 250;
            var width = 200;
            var id = '#'+data.id;
//                    var render_id = _this.shadowRoot.querySelector(id);
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