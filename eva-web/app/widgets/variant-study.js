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

    draw: function (args) {
        var _this = this;
        if(args === 'files'){
            this._createFilesStudyPanel();
        }
        if(args === 'genotype'){
            this._createGenotypePanel();
        }

    },
    _createGenotypePanel:function(){
        var _this = this;
        _this.targetId = (_this.render_id) ? _this.render_id : _this.targetId;
        _this.targetDiv = (_this.targetId instanceof HTMLElement ) ? _this.targetId : $('#' + _this.targetId)[0];

        if (_this.targetDiv === "undefined" || _this.targetDiv == null ) {
            console.log('targetId not found');
            return;
        }
        _this._clear();
        var genotypesData = _this._parseGenotypesData(_this.variantId);

        if(!genotypesData.length > 0){
            _this.div = '<div class="col-md-12">No Data Found</div>'
             $(this.targetDiv).append( $(_this.div));
             return;
        }
        for(var key in genotypesData ){
            var study = genotypesData[key];
            var genotypeDivId = 'genotype-'+study.id+'-'+key;
            if(study.name != null){
                var studyName = study.name;
            }else{
                var studyName = study.id;
            }

            var genotypeArgs = {id:genotypeDivId,data:study.data,studyId:study.id}
            _this.div  = '<h4>'+studyName+'&nbsp;<button id="button-'+genotypeDivId+'" type="button"  onclick="toggleShow(this.id,1)"  class="btn  btn-default btn-xs"><span>-</span></button></h4>'
            _this.div += '<div class="col-md-12" id="'+genotypeDivId+'">'
            _this.div += '</div>'
            $(this.targetDiv).append( $(_this.div));
            _this._loadGenotypeData(genotypeArgs);
        }
    },

    _createFilesStudyPanel:function(){
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

            if(study.name != null){
                var studyName = study.name;
            }else{
                var studyName = study.id;
            }

            var studyDivId = study.id+'-'+key;

            _this.div = '<h4>'+studyName+' <button id="button-'+studyDivId+'" type="button"  onclick="toggleShow(this.id,1)"  class="btn  btn-default btn-xs"><span>-</span></button></h4>'
            _this.div += '<div id="'+studyDivId+'"><div>'
            _this.div += '</div></div>'
            $(this.targetDiv).append( $(_this.div));
            _this.studyDiv = (studyDivId instanceof HTMLElement ) ? studyDivId : $('#' + studyDivId)[0];

            for(var k in study.data){

                var attributesId = studyDivId+'-attributes-'+k;
                var statsId = studyDivId+'-stats-'+k;
                var chartId = studyDivId+'-chart-id-'+k;

                var attributesData = study.data[k].attributes;
                var statsData = study.data[k].stats;
                var chartData = study.data[k].chartData;

                var attributes = {id:attributesId,data:attributesData};
                var stats = {id:statsId,data:statsData};
                var chart = {id:chartId,data:chartData};

                _this.div1  = '<div class="col-md-12"><div class="row"><div class="col-md-12" ></div></div></div>'
                //_this.div1  = '<div class="col-md-12"><div class="row"><div class="col-md-12" ><h5>File ID: &nbsp;<span style="font-size:15px; font-weight:bold; color:#000000;">'+study.data[k].fileID+'</span></h5></div></div></div>'
                _this.div1 += '<div class="col-md-12">'
                _this.div1 += '<div class="col-md-12" id="'+attributesId+'"></div>'
                _this.div1 += '<div class="col-md-7" id="'+statsId+'"></div>'
                _this.div1 += '<div class="col-md-3"><h5 style="margin-left:60px;">Genotype Count</h5><div id="'+chartId+'">'
                if(!chartData[0].data.length > 0){
                    _this.div1 += '<div class="col-md-10"><span style="margin-left:60px;">No Data</span></div>'
                }
                _this.div1 += '</div></div></div>'
                _this.div1 += '<script>'
                _this.div1 += function toggleShow(id,value) {

                                var button = 'button[id="'+id+'"]';
                                id = id.split('button-')
                                var div = 'div[id="'+id[1]+'"]';
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
                _this.div1 += '</script>'
                $(this.studyDiv).append( $(_this.div1));
                _this._loadAttributesData(attributes);
                _this._loadStatsData(stats);
                if(chartData[0].data.length > 0){
                    _this._drawPieChart(chart);
                }

            }
        }

    },

    _clear:function(){
        var _this = this;
        $( "#"+_this.render_id).empty();
    },


    _loadGenotypeData:function(args){
        var variantGenotype = new VariantGenotypeWidget({
            data      : args.data,
            render_id : args.id,
            title: args.studyId,
            pageSize:10
        });
        variantGenotype.draw();
    },

    _loadAttributesData:function(args){
        var _this = this;
        var data = args.data
        var id = args.id

        _this.attributeDiv = '<h5>Attributes</h5>'
        _this.attributeDiv += '<div style="overflow:auto;"><table class="table  table-bordered"><tr>'

        for(var key in data){
            _this.attributeDiv += '<th>'+key+'</th>'
        }
        _this.attributeDiv += '</tr><tr>'
        for(var value in data ){
            _this.attributeDiv += '<td>'+data[value]+'</td>'
        }
        _this.attributeDiv += '</tr></table></div>'

        $('#'+id).append( $(_this.attributeDiv));



    },
    _loadStatsData:function(args){

        var _this = this;
        var data = args.data
        var id = args.id

        if(!data.alleleMaf){
            data.alleleMaf = '-';
        }
        if(!data.genotypeMaf){
            data.genotypeMaf = '-';
        }
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

        var _this = this;

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

            tmpDataArray[studyId].push({fileID:value.fileId,'attributes':value.attributes,'stats':value.stats, 'chartData':chartData });

        });


        var filesDataArray = new Array();
        for (key in tmpDataArray){
            var studyname = _this._getStudyName(key);
            filesDataArray.push({id:key,name:studyname,data:tmpDataArray[key]});
        }



        return filesDataArray;

    },
    _parseGenotypesData:function(args){
        var _this = this;

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
            if(value.samples){
                if(!tmpDataArray[studyId]) tmpDataArray[studyId] = [];
                tmpDataArray[studyId] = {samples:value.samples,'format':value.format};
            }
        });

        var genotypesDataArray = new Array();
        for (key in tmpDataArray){
            var studyname = _this._getStudyName(key);
            genotypesDataArray.push({id:key,name:studyname,data:tmpDataArray[key]});
        }

        return genotypesDataArray;

    },
    _getStudyName:function(args){

        var studyName;
        evaManager.get({
            category: 'studies',
            resource: 'view',
            params: {
                of: 'json'
            },
            query: args,
            async: false,
            success: function (data) {
                studyName = data.response.result[0].studyName
            },
            error: function (data) {
                console.log('Could not get study info');
            }


        });
        return studyName;

    },
    _drawPieChart:function(args){
        var value = args.data[0].data;
        var title = args.data[0].title;
        var id = args.id;


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
