/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
 * Copyright (c) 2014 Jag Kandasamy (EBI-EMBL)
 *
 * This file is part of EVA.
 *
 * EVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * EVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EVA. If not, see <http://www.gnu.org/licenses/>.
 */
var summary = {};
var files = [];
function EvaStudyView(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("EVAStudyView");
    this.type ='eva';
    this.projectId = 'PRJEB5473';
    _.extend(this, args);
    this.rendered = false;
    this.render();


}
EvaStudyView.prototype = {
    render: function () {
        var _this = this;
//        $('*').css('cursor','wait');


        var params = {};

        if(this.type === 'dgva'){
            var params = {structural: 'true'};
        }

        EvaManager.get({
            category: 'studies',
            resource: 'summary',
            query:this.projectId,
            params:params,
            async: false,
            success: function (response) {
                try {
                    summary = response.response[0].result;
                    console.log(summary)
                } catch (e) {
                    console.log(e);
                }
                _this._parseData();
            }
        });

        if(this.type === 'eva'){
            _.each(speciesList, function(speciesList){
                if (speciesList.name.indexOf(summary[0].speciesCommonName) !=-1) {
                    console.log(speciesList.name)
                    console.log()
                }
            });
            EvaManager.get({
                category: 'studies',
                resource: 'files',
                query:this.projectId,
                async: false,
                success: function (response) {
                    try {
                        files = response.response[0].result;
                    } catch (e) {
                        console.log(e);
                    }
                    _this._parseData();
                }
            });
        }

    },
    _draw:function(data,content){
        var _this = this;
        var el =  document.querySelector("#"+this.target);
        el.innerHTML = '';
//        $('*').css('cursor','default');
        var elDiv = document.createElement("div");
        $(elDiv).html(content);
        el.appendChild(elDiv);
        el.applyAuthorStyles = true;

    },
    _parseData:function(data){
        var _this = this;
        var data = {};
        var divContent = '';
        if( _.isEmpty(summary) == false && this.type === 'eva'){
            data = {summaryData: summary,filesData: files }
            divContent =  _this._createContent(data)
        }else if (_.isEmpty(summary) == false && this.type === 'dgva'){
            data = {summaryData: summary }
            divContent =  _this._createContent(data)
        }
        _this._draw(data,divContent)

    },
    _createContent: function (data){
            var _this = this;

            if(_this.type === 'eva'){

                var taxonomyId = new Array();

                if(data.summaryData[0].taxonomyId){
                    for (i = 0; i < data.summaryData[0].taxonomyId.length; i++) {
                        var taxLink = 'http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id='+data.summaryData[0].taxonomyId[i];
                        taxonomyId.push(['<a href="'+taxLink+'" target="_blank">'+data.summaryData[0].taxonomyId[i]+'</a>']);
                    }
                }

                if(_.isUndefined(_this._getProjectUrl(data.summaryData[0].id))){
                    var projectURL = '-'
                }else{
                    var projectURL = '<a href="'+_this._getProjectUrl(data.summaryData[0].id)+'" target="_blank">'+_this._getProjectUrl(data.summaryData[0].id)+'</a>'
                }


                var _filesTable  = '<div><h3>'+data.summaryData[0].name+'</h3>' +
                    '<div class="row study-view-data"><div class="col-md-12"><div><h4>General Information</h4></div><table class="table table-bordered study-view-table">' +
                    '<tr><td><b>Species</b></td><td>'+data.summaryData[0].speciesCommonName+'</td></tr>' +
                    '<tr><td><b>Scientific Name</b></td><td>'+data.summaryData[0].speciesScientificName+'</td></tr>' +
                    '<tr><td><b>Taxonomy ID</b></td><td>'+taxonomyId.join()+'</td></tr>' +
                    '<tr><td><b>Center</b></td><td>'+data.summaryData[0].center+'</td></tr>' +
                    '<tr><td><b>Material</b></td><td>'+data.summaryData[0].material+'</td></tr>' +
                    '<tr><td><b>Scope</b></td><td>'+data.summaryData[0].scope+'</td></tr>' +
                    '<tr><td><b>Type</b></td><td>'+data.summaryData[0].experimentType+'</td></tr>' +
                    '<tr><td><b>Source Type</b></td><td>'+data.summaryData[0].sourceType+'</td></tr>' +
                    '<tr><td><b>Platform</b></td><td>'+data.summaryData[0].platform+'</td></tr>' +
                    '<tr><td><b>Samples</b></td><td>'+data.summaryData[0].numSamples+'</td></tr>' +
                    '<tr><td><b>Description</b></td><td>'+data.summaryData[0].description+'</td></tr>' +
                    '<tr><td><b>Resource</b></td><td>'+projectURL+'</td></tr>' +
                    '<tr><td><b>Download</b></td><td><a href="ftp://ftp.ebi.ac.uk/pub/databases/eva/'+data.summaryData[0].id+'" target="_blank">FTP</a></td></tr>' +
                    '</table>'

                if(data.filesData.length > 0){
                    var fileNameArr = [];

                    for (i = 0; i < data.filesData.length; i++) {
                        var fileName = files[i].fileName;
                        var regex =/_accessioned.vcf/g;
                        if(fileName.match(regex)){
                            fileNameArr.push(fileName.replace(regex, ".vcf.gz"));
                        }else{
                            fileNameArr.push(fileName)
                        }
                    }
                    var fileNameList = fileNameArr.join(',');
                    var ftpLink = {};
//                    var info =_.findWhere(infoTags, {id:value});
                    console.log('++++')
                    console.log(speciesList)
                    $.ajax({
                        type: 'GET',
                        url: METADATA_HOST+'/v1/files/'+fileNameList+'/url',
                        dataType: 'json',//still firefox 20 does not auto serialize JSON, You can force it to always do the parsing by adding dataType: 'json' to your call.
                        async: false,
                        success: function (data, textStatus, jqXHR) {
                            ftpLink = data.response;
                        },
                        error: function (jqXHR, textStatus, errorThrown) {

                        }
                    });
                    _filesTable += '<div><h4>Files</h4></div><table class="table table-striped"><thead><tr>' +
                        '<th>File Name</th>'+
                        '<th>Samples with Genotypes</th>'+
                        '<th>Variants Count</th>'+
                        '<th>SNP Count</th>'+
                        '<th>Indel Count</th>'+
                        '<th>Pass Count</th>'+
                        '<th>Transitions/Transversions Ratio</th>'+
                        '<th>Mean Quality</th>'+
                        '</tr></thead><tbody>'
                    for (i = 0; i < data.filesData.length; i++) {
                        if(ftpLink.length > 0){
                            var downloadLink = '<a href="'+ftpLink[i].result[0]+'" target="_blank">'+data.filesData[i].fileName+'</a>';
                        }else{
                            var downloadLink = data.filesData[i].fileName;
                        }
                        var samples_count;
                        if(data.filesData[i].stats.samplesCount){
                            samples_count = data.filesData[i].stats.samplesCount;
                        }else{
                            samples_count = 'NA';
                        }
                        _filesTable += '<tr>'+
                            '<td>'+downloadLink+'</td>' +
                            '<td>'+samples_count+'</td>' +
                            '<td>'+data.filesData[i].stats.variantsCount+'</td>' +
                            '<td>'+data.filesData[i].stats.snpsCount+'</td>' +
                            '<td>'+data.filesData[i].stats.indelsCount+'</td>' +
                            '<td>'+data.filesData[i].stats.passCount+'</td>' +
                            '<td>'+(data.filesData[i].stats.transitionsCount/data.filesData[i].stats.transversionsCount).toFixed(2)+'&nbsp;('+data.filesData[i].stats.transitionsCount+'/'+data.filesData[i].stats.transversionsCount+')</td>' +
                            '<td>'+data.filesData[i].stats.meanQuality.toFixed(2)+'</td>' +
                            '</tr>'
                    }
                    _filesTable += '</tbody></table>'

                }
                _filesTable += '</div></div>'
            }
            else if(_this.type === 'dgva'){
                var taxonomyId = new Array();

                if(data.summaryData[0].taxonomyId){
                    for (i = 0; i < data.summaryData[0].taxonomyId.length; i++) {
                        var taxLink = 'http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id='+data.summaryData[0].taxonomyId[i];
                        taxonomyId.push(['<a href="'+taxLink+'" target="_blank">'+data.summaryData[0].taxonomyId[i]+'</a>']);
                    }
                }



                var _filesTable  = '<div><h3>'+data.summaryData[0].name+'</h3>' +
                    '<div class="row study-view-data"><div class="col-md-12"><div><h4>General Information</h4></div><table class="table table-bordered">' +
                    '<tr><td><b>Species</b></td><td class="eva-capitalize">'+data.summaryData[0].speciesCommonName+'</td></tr>' +
                    '<tr><td><b>Scientific Name</b></td><td>'+data.summaryData[0].speciesScientificName+'</td></tr>' +
                    '<tr><td><b>Taxonomy ID</b></td><td>'+taxonomyId.join()+'</td></tr>' +
                    '<tr><td><b>Study Type</b></td><td>'+data.summaryData[0].typeName+'</td></tr>' +
                    '<tr><td><b>Experiment Type</b></td><td>'+data.summaryData[0].experimentType+'</td></tr>' +
                    '<tr><td><b>Platform</b></td><td>'+data.summaryData[0].platform+'</td></tr>' +
                    '<tr><td><b>Assembly</b></td><td>'+data.summaryData[0].assembly+'</td></tr>' +
                    '<tr><td><b>Variants</b></td><td>'+data.summaryData[0].numVariants+'</td></tr>' +
                    '<tr><td><b>Description</b></td><td>'+data.summaryData[0].description+'</td></tr>' +
                    '<tr><td><b>Download</b></td><td><a href="ftp://ftp.ebi.ac.uk/pub/databases/dgva/'+data.summaryData[0].id+'_'+data.summaryData[0].name+'" target="_blank">FTP</a></td></tr>' +
                    '</table></div></div>'

            }

            return _filesTable;
        },
        _getProjectUrl: function (data){
                var _this = this;
                for (var i = 0; i < projects.length; i++) {
                    if (projects[i].id === data) {
                        return projects[i].url;
                    }
                }
        }

    }


