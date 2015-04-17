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

/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
 *
 * This file is part of JSorolla.
 *
 * JSorolla is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSorolla is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSorolla. If not, see <http://www.gnu.org/licenses/>.
 */
function ClinvarSummaryPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("ClinVarSummaryDataPanel");

    this.target;
    this.title = "Stats";
    this.height = 700;
    this.autoRender = true;
    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render();
    }
}

ClinvarSummaryPanel.prototype = {
    render: function () {
        var _this = this;

        //HTML skel
        this.div = document.createElement('div');
        this.div.setAttribute('id', this.id);

        this.panel = this._createPanel();

    },
    draw: function () {
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('target not found');
            return;
        }

        this.targetDiv.appendChild(this.div);
        this.panel.render(this.div);

    },
    clear: function () {
        this.summaryContainer.removeAll(true);
    },
    load: function (data) {
        this.clear();
        var panels = [];
//        var summaryPanel = this._createSummaryPanel(data.clinvarList);
//        var clinvarList = data.clinvarList;
//        for (var key in clinvarList) {
//            var summaryData = clinvarList[key];
//            var summaryPanel = this._createSummaryPanel(summaryData);
//            panels.push(summaryPanel);
//        }
//        this.summaryContainer.removeAll();
//        this.summaryContainer.add(panels);

          var summaryData = data;
          var panel = this._createSummaryPanel(summaryData);
          this.summaryContainer.removeAll();
          this.summaryContainer.add(panel);


    },
    _createPanel: function () {
        this.summaryContainer = Ext.create('Ext.container.Container', {
            layout: {
//                type: 'accordion',
                type: 'vbox',
                titleCollapse: true,
//                fill: false,
                multi: true
            }
        });

      this.panel = Ext.create('Ext.container.Container', {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            overflowY: true,
            padding: 10,
            items: [
                {
                    xtype: 'box',
                    cls: 'ocb-header-4',
                    html: '<h4>Summary</h4>',
                    margin: '5 0 10 10'
                },
                this.summaryContainer
            ],
            height: this.height
        });
        return this.panel;
    },
    _createSummaryPanel: function (data) {
        var annotData = data.annot;
        data = data.clinvarSet.referenceClinVarAssertion;
        var lastEvaluated = new Date( data.clinVarAccession.dateUpdated ).toUTCString();
        var origin = data.observedIn[0].sample.origin;
        var traitSet = data.traitSet.trait;
        var citation= 'NA';

        var publications = '-';
        var pubArray = [];
        _.each(_.keys(traitSet), function(key){
            var citation = this[key].citation;
          if(citation){
              _.each(_.keys(citation), function(key){
                  if(this[key].id && this[key].id.source == 'PubMed'){
                      pubArray.push('PMID:<a href="http://www.ncbi.nlm.nih.gov/pubmed/'+this[key].id.value+'" target="_blank">'+this[key].id.value+'</a>')
                  }
              },citation);
          }

        },traitSet);


        if(!_.isEmpty(pubArray)){
            publications = pubArray.join('<br/>');
        }
//        var citation = data.traitSet.trait[0].citation;
//        if(!_.isEmpty(citation) && citation[0].id.source == 'PubMed'){
//            publications = 'Pubmed:<a href="http://www.ncbi.nlm.nih.gov/pubmed/'+citation[0].id.value+'" target="_blank">'+citation[0].id.value+'</a>';
//        }

        var temp_hgvs = data.measureSet.measure[0].attributeSet
        var variation_type = data.measureSet.measure[0].type;
       var hgvs = '-';
       var soTerms = '-';
       if(!_.isUndefined(annotData)){
           var hgvsArray = []
           var hgvs_data = annotData.hgvs.sort().reverse()
           _.each(_.keys(hgvs_data), function(key){
               if(this[key]){
                   hgvsArray.push(this[key]);
               }
           },hgvs_data);
           hgvs = hgvsArray.join("<br\/>");

           var tempArray = [];
           _.each(_.keys(annotData.consequenceTypes), function(key){
               var so_terms = this[key].soTerms;
               _.each(_.keys(so_terms), function(key){
                   tempArray.push(this[key].soName)
               },so_terms);
           },annotData.consequenceTypes);

           var groupedArr = _.groupBy(tempArray);
           console.log(groupedArr)
           var so_array = [];
           _.each(_.keys(groupedArr), function(key){
               var index =  _.indexOf(consequenceTypesHierarchy, key);
//                                        so_array.splice(index, 0, key+' ('+this[key].length+')');
//                                        so_array.push(key+' ('+this[key].length+')')
               so_array[index] = key+' ('+this[key].length+')';
           },groupedArr);

           so_array =  _.compact(so_array);

           soTerms = so_array.join("<br\/>");

       }



        var summaryPanel = Ext.create('Ext.panel.Panel', {
//            title: 'Summary',
//            title: data.clinVarAccession.acc,
            border: false,
            layout: 'hbox',
            overflowX: true,
            overflowY: true,
            items: [  {
                xtype: 'container',
                data: data,
                width:970,
//                tpl: new Ext.XTemplate(
//                    '<div class="col-md-12"><table class="table table-bordered eva-attributes-table">',
//                        '<tr>',
////                            '<td class="header">Clinical Significance <br/> (Last evaluated)</td>',
//                            '<td class="header">Review status</td>',
//                            '<td class="header">Last Evaluated</td>',
//                            '<td class="header">Origin </td>',
//                            '<td class="header">Citations</td>',
//                            '<td class="header">Submitter</td>',
//                        '</tr>',
//                        '<tr>',
////                            '<td>{clinicalSignificance.description}<br/>('+lastEvaluated+')</td>',
//                            '<td>{clinicalSignificance.reviewStatus}</td>',
//                            '<td>'+lastEvaluated+'</td>',
//                            '<td>'+origin+'</td>',
//                            '<td>'+citation+'</td>',
//                            '<td>{clinVarSubmissionID.submitter}</td>',
//                        '</tr>',
//                        '</table></div>'
//                ),
                tpl: new Ext.XTemplate(
                    '<div class="col-md-12"><table class="table table-bordered eva-stats-table">',
//                        '<tr>',
//                            '<td class="header">Clinical Significance<br/> (Last evaluated)</td><td>{clinicalSignificance.description}<br/>('+lastEvaluated+')</td>',
//                        '</tr>',
                        '<tr>',
                            '<td class="header">Review status</td><td>{clinicalSignificance.reviewStatus}</td>',
                        '</tr>',
                        '<tr>',
                            '<td class="header">Last Evaluated</td><td>'+lastEvaluated+'</td>',
                        '</tr>',
                        '<tr>',
                            '<td class="header">HGVS(s)</td><td>'+hgvs+'</td>',
                        '</tr>',
                        '<tr>',
                            '<td class="header">SO Terms(s)</td><td>'+soTerms+'</td>',
                        '</tr>',
                        '<tr>',
                            '<td class="header">Variation Type</td><td>'+variation_type+'</td>',
                        '</tr>',
                        '<tr>',
                            '<td class="header">Publications</td><td>'+publications+'</td>',
                        '</tr>',
//                        '<tr>',
//                            '<td class="header">Submitter</td><td>{clinVarSubmissionID.submitter}</td>',
//                        '</tr>',
                        '</table></div>'
                ),
                margin: '10 5 5 10'
            }]
        });

        return summaryPanel;
    }
};
