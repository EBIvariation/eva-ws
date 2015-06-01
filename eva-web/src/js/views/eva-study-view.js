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
                } catch (e) {
                    console.log(e);
                }
//                _this._parseData();
            }
        });

        if(this.type === 'eva'){
            var studySpeciesList = '';
            EvaManager.get({
                category: 'meta/species',
                resource: 'list',
                async: false,
                success: function (response) {
                    try {
                        studySpeciesList = response.response[0].result;
                    } catch (e) {
                        console.log(e);
                    }
                }
            });
            if(!_.isUndefined(_.findWhere(studySpeciesList, {taxonomyEvaName:summary[0].speciesCommonName.toLowerCase()}))){
                var speciesCode = _.findWhere(studySpeciesList, {taxonomyEvaName:summary[0].speciesCommonName.toLowerCase()}).taxonomyCode+'_'+_.findWhere(studySpeciesList, {taxonomyEvaName:summary[0].speciesCommonName.toLowerCase()}).assemblyCode
                var filesParams = {species:speciesCode};
            }

            EvaManager.get({
                category: 'studies',
                resource: 'files',
                query:this.projectId,
                params:filesParams,
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
                var projectURL;
                var ena_link = 'ENA:<a href="http://www.ebi.ac.uk/ena/data/view/'+data.summaryData[0].id+'" target="_blank">'+data.summaryData[0].id+'</a>';
                if(_.isUndefined(_this._getProjectUrl(data.summaryData[0].id))){
                     projectURL = ena_link;
                }else{
                     projectURL = '<a href="'+_this._getProjectUrl(data.summaryData[0].id)+'" target="_blank">'+_this._getProjectUrl(data.summaryData[0].id)+'</a><br /><br />'+ena_link;

                }


                var _filesTable  = '<div><h3>'+data.summaryData[0].name+'</h3>' +
                    '<div class="row study-view-data"><div class="col-md-12"><div><h4>General Information</h4></div><table class="table table-bordered study-view-table">' +
                    '<tr><td><b>Organism</b></td><td>'+data.summaryData[0].speciesCommonName+'</td></tr>' +
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
                            _.extend(data.filesData[i], {ftpId:fileName.replace(regex, ".vcf.gz")});
                            fileNameArr.push(fileName.replace(regex, ".vcf.gz"));
                        }else{
                            fileNameArr.push(fileName)
                        }
                    }
                    var fileNameList = fileNameArr.join(',');
                    var ftpLink = {};
                    EvaManager.get({
                        category: 'files',
                        resource: 'url',
                        query:fileNameList,
                        async: false,
                        success: function (response) {
                            try {
                                ftpLink = response.response;

                            } catch (e) {
                                console.log(e);
                            }
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
                        '<th>View</th>'+
                        '</tr></thead><tbody>'
                    for (i = 0; i < data.filesData.length; i++) {
                        var ftpLocation = '#';
//                        if(!_.isUndefined(_.findWhere(ftpLink, {id:data.filesData[i].ftpId}))){
                        if(!_.isUndefined(_.findWhere(ftpLink, {id:data.filesData[i].fileName}))){
                            ftpLocation = _.findWhere(ftpLink, {id:data.filesData[i].fileName}).result[0];
                        }
                        var iobioLink = '';
                        if(ftpLink.length > 0 && ftpLocation != 'ftp:/null'){
                            var downloadLink = '<a href="'+ftpLocation+'" target="_blank">'+data.filesData[i].fileName+'</a>';
                            var iobio_url = 'http://ega-beacon.windows.ebi.ac.uk:8080/?vcf=http://s3.amazonaws.com/vcf.files/ExAC.r0.2.sites.vep.vcf.gz';
                            iobioLink = '<a href="?eva-iobio&url='+iobio_url+'" target="_blank">Iobio</a>'
                        }else{
                            var downloadLink = data.filesData[i].fileName;
                        }
                        var samples_count;
                        var variantsCount;
                        var snpsCount;
                        var indelsCount;
                        var passCount;
                        var transitionsCount;
                        var meanQuality;
                        if(!_.isUndefined(data.filesData[i].stats) && !_.isNull(data.filesData[i].stats)){
                            if(data.filesData[i].stats.samplesCount){
                                samples_count = data.filesData[i].stats.samplesCount;
                            }else{
                                samples_count = 'NA';
                            }
                            variantsCount = data.filesData[i].stats.variantsCount;
                            snpsCount = data.filesData[i].stats.snpsCount;
                            indelsCount = data.filesData[i].stats.indelsCount;
                            passCount = data.filesData[i].stats.passCount;
                            transitionsCount =(data.filesData[i].stats.transitionsCount/data.filesData[i].stats.transversionsCount).toFixed(2)+'&nbsp;('+data.filesData[i].stats.transitionsCount+'/'+data.filesData[i].stats.transversionsCount+')';
                            meanQuality = data.filesData[i].stats.meanQuality.toFixed(2);
                        }else{
                            samples_count = 'NA';
                            variantsCount = 'NA';
                            snpsCount = 'NA';
                            indelsCount = 'NA';
                            passCount = 'NA';
                            transitionsCount = 'NA';
                            meanQuality = 'NA';
                        }

                        _filesTable += '<tr>'+
                            '<td>'+downloadLink+'</td>' +
                            '<td>'+samples_count+'</td>' +
                            '<td>'+variantsCount+'</td>' +
                            '<td>'+snpsCount+'</td>' +
                            '<td>'+indelsCount+'</td>' +
                            '<td>'+passCount+'</td>' +
                            '<td>'+transitionsCount+'</td>' +
                            '<td>'+meanQuality+'</td>' +
                            '<td>'+iobioLink+'</td>' +
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
                    '<tr><td><b>Organism</b></td><td class="eva-capitalize">'+data.summaryData[0].speciesCommonName+'</td></tr>' +
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


