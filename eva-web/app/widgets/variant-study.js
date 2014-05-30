/**
 * Created by jag on 28/05/2014.
 */
function VariantStudyWidget(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.location = '';

    //set instantiation args, must be last
    _.extend(this, args);

}

VariantStudyWidget.prototype = {

    draw: function () {
        var _this = this;
        this._createStudyPanel();
    },

    _createStudyPanel:function(){
        var _this = this;

        _this.targetId = (_this.render_id) ? _this.render_id : _this.targetId;
        _this.targetDiv = (_this.targetId instanceof HTMLElement ) ? _this.targetId : $('#' + _this.targetId)[0];

        if (_this.targetDiv === "undefined" || _this.targetDiv == null ) {
            console.log('targetId not found');
            return;
        }

        _this._clear();

        var fileData = _this._parseFilesData(_this.variantId);

        for (var key in fileData){
             var study = fileData[key];
             var studyDivId = study.id+'-'+key;


            for(var k in study.data){
                var attributesId = studyDivId+'-attributes-'+k;
                var statsId = studyDivId+'-stats-'+k;
                var chartId = studyDivId+'-chart-id-'+k;

                var attributesData = study.data[k].attributes;
                var statsData = study.data[k].stats;
                var chartData = study.data[k].chartData;

                var attributes = new Array();
                    attributes.push({id:attributesId,data:attributesData});

                var stats = new Array();
                    stats.push({id:statsId,data:statsData});

                var chart = new Array();
                    chart.push({id:chartId,data:chartData});


                _this.div  = '<h4>'+study.id+' Study <button id="'+studyDivId+'" type="button"  onclick="toggleShow(this.id,1)"  class="btn  btn-default btn-xs"><span>-</span></button></h4>'
                _this.div += '<div id="'+studyDivId+'"><div>'
                _this.div += '<div class="col-md-12" style="overflow:scroll;" id="'+attributesId+'"></div>'
                _this.div += '<div class="col-md-7" id="'+statsId+'"></div><div class="col-md-3"><h5 style="margin-left:60px;">Genotype Count</h5><div id="'+chartId+'"></div></div>'
                _this.div += '</div></div>'
                _this.div += '<script>'
                _this.div += function toggleShow(id,value) {
                                    var button = 'button[id="'+id+'"]';
                                    var div = 'div[id="'+id+'"]';
                                    var text = $(button).text();
                                    if(text == "+"){
                                        $(button).text('-');
                                    }else{
                                        $(button).text('+');
                                    }
                                    $(div).toggle(function(event) {

                                        return false;
                                    });

                            }
                _this.div += '</script>'
                $(this.targetDiv).append( $(_this.div));
                _this._loadAttributesData(attributes);
                _this._loadStatsData(stats);
                _this._drawPieChart(chart);

            }
        }


        //$(this.targetDiv).append( $(_this.div));


    },

    _clear:function(){
        var _this = this;
        $( "#"+_this.render_id).empty();
    },

    _loadAttributesData:function(args){
        var _this = this;
        var data = args[0].data
        var id = args[0].id

        _this.attributeDiv = '<h5>File Attributes</h5>'
        _this.attributeDiv += '<table class="table table-striped"><tr>'

        for(var key in data){
            _this.attributeDiv += '<th>'+key+'</th>'
        }
        _this.attributeDiv += '</tr><tr>'
        for(var value in data ){
            _this.attributeDiv += '<td>'+data[value]+'</td>'
        }
        _this.attributeDiv += '</tr></table>'

        $('#'+id).append( $(_this.attributeDiv));



    },
    _loadStatsData:function(args){

        var _this = this;
        var data = args[0].data
        var id = args[0].id

        _this.statsDiv =  '<h5>Stats</h5>'
        _this.statsDiv += '<table class="table">'
        _this.statsDiv += '<tr><td>Minor Allele Frequency</td><td>'+data.maf.toFixed(4)+'('+data.alleleMaf+')</td></tr>'
        _this.statsDiv += '<tr><td>Minor Genotype Frequency</td><td>'+data.mgf.toFixed(4)+'('+data.genotypeMaf+')</td></tr>'
        _this.statsDiv += ' <tr><td>Mendelian Errors</td> <td>'+data.mendelErr+'</td></tr>'
        _this.statsDiv += ' <tr><td>Missing Allele</td> <td>'+data.missAllele+'</td></tr>'
        _this.statsDiv += ' <tr><td>Missing Genotypes</td> <td>'+data.missGenotypes+'</td></tr>'
        _this.statsDiv += '</table>'
        $('#'+id).append( $(_this.statsDiv));
    },


    _parseFilesData:function(args){

        var variantData;
        evaManager.get({
            category: 'variants',
            resource: 'info',
            params: {
                of: 'json'
            },
            query: args,
            async: false,
            success: function (data) {
                variantData = data.response.result[0].files;
            },
            error: function (data) {
                console.log('Could not get variant info');
            }
        });



        var tmpData = variantData;
        var tmpDataArray = [];

        $.each(tmpData, function(key, value) {
            var chartData = [];
            var studyId = value.studyId;
            if(!tmpDataArray[studyId]) tmpDataArray[studyId] = [];
            var chartArray=[];
            for (key in value.stats.genotypeCount) {
                chartArray.push([key,  value.stats.genotypeCount[key]]);
            }
            chartData.push({'title':'Genotype Count','data':chartArray});
            tmpDataArray[studyId].push({'attributes':value.attributes,'stats':value.stats, 'chartData':chartData });

        });
        var filesDataArray = new Array();
        for (key in tmpDataArray){
            filesDataArray.push({id:key,data:tmpDataArray[key]});
        }

        return filesDataArray;

    },

    _drawPieChart:function(args){
        var value = args[0].data[0].data;
        var title = args[0].data[0].title;
        var id = args[0].id;




        var chart1 = new Highcharts.Chart({
            chart: {
                height: 250,
                width: 300,
                renderTo:id,
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },

            title: {
                text: ''
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                    },
                    showInLegend: true
                }

            },
            series: [{
                type: 'pie',
                name: title,
                data: value
            }],
            credits: {
                enabled: false
            },
        });

    }


};
