/**
 * Created by jag on 17/10/2014.
 */

var variant = {};
var variantID = '';

function EvaVariantView(args) {
    _.extend(this, Backbone.Events);
    _.extend(this, args);
    this.rendered = false;
    this.render();


}
EvaVariantView.prototype = {
    render: function () {
        var _this = this

        this.targetDiv = (this.target instanceof HTMLElement) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVAv-VariantView: target ' + this.target + ' not found');
            return;
        }

        this.targetDiv.innerHTML = _this._varinatViewlayout();
        variantID = this.position;
        EvaManager.get({
            host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
            category: 'variants',
            resource: 'info',
            query:variantID,
            success: function (response) {
                try {
                    variant = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                _this.draw();
            }
        });

        $('#variantViewTabs li').click(function(event) {
            $(this).toggleClass("active");
            $(this).siblings().removeClass("active");
        });
        $( document ).ready(function() {
            $('body').scrollspy({ 'target': '#variantViewScrollspy', 'offset': 250 });
        });
    },
    createVariantStatsPanel: function (data) {
            var _this = this;
            var variantStatsPanel = new VariantStatsPanel({
                target: data,
                handlers: {
                    "load:finish": function (e) {
//                    _this.grid.setLoading(false);
                    }
                },
                statsTpl : new Ext.XTemplate(
                    '<table class="ocb-stats-table">' +
                        '<tr>' +
                        '<td class="header">Minor Allele Frequency:</td>' +
                        '<td><tpl if="maf == -1 || maf == 0">NA <tpl else>{maf:number( "0.000" )} </tpl><tpl if="mafAllele">({mafAllele}) <tpl else></tpl></td>'+
                        '</tr>',
//                                 '<tr>' +
//                                         '<td class="header">Minor Genotype Frequency:</td>' +
//                                         '<td><tpl if="mgf == -1 || mgf == 0">NA <tpl else>{mgf:number( "0.000" )} </tpl><tpl if="mgfGenotype">({mgfGenotype}) <tpl else></tpl></td>' +
//                                         '</tr>',
                    '<tr>' +
                        '<td class="header">Mendelian Errors:</td>' +
                        '<td><tpl if="mendelianErrors == -1">NA <tpl else>{mendelianErrors}</tpl></td>' +
                        '</tr>',
                    '<tr>' +
                        '<td class="header">Missing Alleles:</td>' +
                        '<td><tpl if="missingAlleles == -1">NA <tpl else>{missingAlleles}</tpl></td>' +
                        '</tr>',
                    '<tr>' +
                        '<td class="header">Missing Genotypes:</td>' +
                        '<td><tpl if="missingGenotypes == -1">NA <tpl else>{missingGenotypes}</tpl></td>' +
                        '</tr>',
                    '</table>'
                )
            });

            if (variant[0].files) {
                variantStatsPanel.load(variant[0].files);
            }
            variantStatsPanel.draw();

            return variantStatsPanel;
    },
    createVariantEffectGrid: function (data) {
            var _this = this;
            var variantEffectGrid = new VariantEffectGrid({
                target: data,
                //           headerConfig: this.defaultToolConfig.headerConfig,
                gridConfig: {
                    flex: 1,
                    layout: {
                        align: 'stretch'
                    }
                },
                handlers: {
                    "load:finish": function (e) {
                    }
                }
            });

            var effectData = _this._loadExampleData();
            console.log('_____')
            console.log(effectData)
            variantEffectGrid.load(effectData);
            variantEffectGrid.draw();
            return variantEffectGrid;

    },
    draw: function (data, content) {
            var _this = this;
//                     var variantInfoTitle =  document.querySelector("#variantInfo").textContent = variantID+' Info';
            var variantViewDiv = document.querySelector("#variantView");
            $(variantViewDiv).addClass('show-div');
            var summaryContent =  _this._renderSummaryData(variant);
            var summaryEl = document.querySelector("#summary-grid");
            var summaryElDiv =  document.createElement("div");
            //       summaryElDiv.innerHTML = '<h4>Summary</h4>';
            summaryElDiv.innerHTML = summaryContent;
            summaryEl.appendChild(summaryElDiv);

//                     var effectsEl = document.getElementById("effects-grid");
//                     var effectsElDiv = document.createElement("div");
//                     effectsElDiv.setAttribute('class', 'ocb-variant-effect-grid');
//            //       effectsElDiv.innerHTML = '<h4>Effects</h4>';
//                     effectsEl.appendChild(effectsElDiv);
//                     _this.createVariantEffectGrid(effectsElDiv);

            var studyEl = document.querySelector("#studies-grid");
            var studyElDiv = document.createElement("div");
            studyElDiv.setAttribute('class', 'ocb-variant-stats-panel');
            //       studyElDiv.innerHTML = '<h4>Studies</h4>';
            studyEl.appendChild(studyElDiv);
            _this.createVariantStatsPanel(studyElDiv);

    },
    _renderSummaryData: function (data) {
            var _summaryTable  = '<div class="row"><div class="col-md-8"><table class="table table-bordered">'
            var variantInfoTitle =  document.querySelector("#variantInfo").textContent = data[0].chromosome+':'+data[0].start+':'+data[0].reference+':'+data[0].alternate+' Info';
            if(data[0].id){
                _summaryTable += '<tr><td>ID</td><td>'+data[0].id+'</td></tr>'
            }
            var reference = '-';
            var alternate = '-';

            if(data[0].reference){
                reference = data[0].reference;
            }
            if(data[0].alternate){
                alternate = data[0].alternate;
            }
            _summaryTable += '<tr><td>Type</td><td>'+data[0].type+'</td></tr>' +
                '<tr><td>Chromosome:Start-End</td><td>'+data[0].chromosome+':'+data[0].start+'-'+data[0].end+'</td></tr>' +
                '<tr><td>Assembly</td><td>GRCh37</td></tr>' +
                '<tr><td>Ref</td><td>'+reference+'</td></tr>' +
                '<tr><td>Alt</td><td>'+alternate+'</td></tr>' +
                '</table>'

            _summaryTable += '</div></div>'

            return _summaryTable;

    },
    _loadExampleData: function (data) {
            var data = {"chromosome": "1", "start": 10001, "end": 10001, "referenceAllele": "T", "genes": [], "effects": {"G": [
                    {"allele": "G", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000456328", "featureType": "Transcript", "featureBiotype": "processed_transcript", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": true, "variantToTranscriptDistance": 1868},
                    {"allele": "G", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000488147", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4403},
                    {"allele": "G", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000541675", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "G", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000450305", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 2009},
                    {"allele": "G", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000515242", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1871},
                    {"allele": "G", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000538476", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4410},
                    {"allele": "G", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000518655", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1873},
                    {"allele": "G", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000438504", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": true, "variantToTranscriptDistance": 4362},
                    {"allele": "G", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000423562", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "G", "featureId": "ENSR00000668495", "featureType": "RegulatoryFeature", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1566], "canonical": false, "variantToTranscriptDistance": -1}
                ], "A": [
                    {"allele": "A", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000456328", "featureType": "Transcript", "featureBiotype": "processed_transcript", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": true, "variantToTranscriptDistance": 1868},
                    {"allele": "A", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000488147", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4403},
                    {"allele": "A", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000541675", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "A", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000450305", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 2009},
                    {"allele": "A", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000515242", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1871},
                    {"allele": "A", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000538476", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4410},
                    {"allele": "A", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000518655", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1873},
                    {"allele": "A", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000438504", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": true, "variantToTranscriptDistance": 4362},
                    {"allele": "A", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000423562", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "A", "featureId": "ENSR00000668495", "featureType": "RegulatoryFeature", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1566], "canonical": false, "variantToTranscriptDistance": -1}
                ], "C": [
                    {"allele": "C", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000456328", "featureType": "Transcript", "featureBiotype": "processed_transcript", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": true, "variantToTranscriptDistance": 1868},
                    {"allele": "C", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000488147", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4403},
                    {"allele": "C", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000541675", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "C", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000450305", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 2009},
                    {"allele": "C", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000515242", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1871},
                    {"allele": "C", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000538476", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4410},
                    {"allele": "C", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000518655", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1873},
                    {"allele": "C", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000438504", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": true, "variantToTranscriptDistance": 4362},
                    {"allele": "C", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000423562", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "C", "featureId": "ENSR00000668495", "featureType": "RegulatoryFeature", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1566], "canonical": false, "variantToTranscriptDistance": -1}
                ], "-": [
                    {"allele": "-", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000456328", "featureType": "Transcript", "featureBiotype": "processed_transcript", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": true, "variantToTranscriptDistance": 1868},
                    {"allele": "-", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000488147", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4403},
                    {"allele": "-", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000541675", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "-", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000450305", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 2009},
                    {"allele": "-", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000515242", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1871},
                    {"allele": "-", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000538476", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4410},
                    {"allele": "-", "geneId": "ENSG00000223972", "geneName": "DDX11L1", "geneNameSource": "HGNC", "featureId": "ENST00000518655", "featureType": "Transcript", "featureBiotype": "transcribed_unprocessed_pseudogene", "featureStrand": "1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1631], "canonical": false, "variantToTranscriptDistance": 1873},
                    {"allele": "-", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000438504", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": true, "variantToTranscriptDistance": 4362},
                    {"allele": "-", "geneId": "ENSG00000227232", "geneName": "WASH7P", "geneNameSource": "HGNC", "featureId": "ENST00000423562", "featureType": "Transcript", "featureBiotype": "unprocessed_pseudogene", "featureStrand": "-1", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1632], "canonical": false, "variantToTranscriptDistance": 4362},
                    {"allele": "-", "featureId": "ENSR00000668495", "featureType": "RegulatoryFeature", "cDnaPosition": -1, "cdsPosition": -1, "proteinPosition": -1, "consequenceTypes": [1566], "canonical": false, "variantToTranscriptDistance": -1}
                ]}, "frequencies": {"maf1000G": 0.6, "maf1000GAfrican": 0.5, "maf1000GAmerican": 0.4, "maf1000GAsian": 0.3, "maf1000GEuropean": 0.2, "mafNhlbiEspAfricanAmerican": 0.1, "mafNhlbiEspEuropeanAmerican": 0.2}, "proteinSubstitutionScores": {"polyphenScore": -1.0, "siftScore": -1.0}, "regulatoryEffect": {"motifPosition": 0, "motifScoreChange": 0.0, "highInformationPosition": false}}
                ;
            return data;
    },
    _varinatViewlayout:function(){

        var layout = '<div id="variant-view">'+
                        '<div class="row">'+
                            '<div  class="col-sm-1  col-md-1 col-lg-1"></div>'+
                            '<div  class="col-sm-11 col-md-11 col-lg-11"> <h2 id="variantInfo"></h2></div>'+
                        '</div>'+
                        '<div class="row">'+
                            '<div class="col-sm-1  col-md-1 col-lg-1" id="variantViewScrollspy">'+
                                '<ul id="variantViewTabs" class="nav nav-pills nav-stacked affix variantViewTabs">'+
                                    '<li class="active"><a href="#summary">Summary</a></li>'+
                                    '<li><a href="#studies">Studies</a></li>'+
                               '</ul>'+
                            '</div>'+
                            '<div id="scroll-able" class="col-sm-11 col-md-11 col-lg-11">'+
                                '<div id="summary" class="row">'+
                                    '<div class="col-md-10">'+
                                        '<h4 class="variant-view-h4"> Summary</h4>'+
                                        '<div id="summary-grid"></div>'+
                                    '</div>'+
                                '</div>'+
                                '<div  id="studies" class="row">'+
                                    '<div class="col-md-12">'+
                                        '<div id="studies-grid"></div>'+
                                    '</div>'+
                                '</div>'+
                            '</div>'+
                        '</div>'+
                    '</div>'
        return layout;
    }

}


