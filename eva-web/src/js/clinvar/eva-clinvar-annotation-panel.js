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
function ClinvarAnnotationPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("ClinVarSummaryDataPanel");

    this.target;
    this.title = "Stats";
    this.height = 500;
    this.autoRender = true;
    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render();
    }
}

ClinvarAnnotationPanel.prototype = {
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
        this.annotContainer.removeAll(true);
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

          var annotData = data.annot;
          var panel = this._createAnnotPanel(annotData);
          this.annotContainer.removeAll();
          this.annotContainer.add(panel);


    },
    _createPanel: function () {
        this.annotContainer = Ext.create('Ext.container.Container', {
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
                    html: '<h4>Annotations</h4>',
                    margin: '5 0 10 10'
                },
                this.annotContainer
            ],
            height: this.height
        });
        return this.panel;
    },
    _createAnnotPanel: function (data) {

        var _this = this;

        var annotData ='';
        if(!_.isUndefined(data)){
            annotData = data.consequenceTypes;
        }



        var annotColumns = {
            items:[
                {
                    text: "Ensembl<br /> Gene ID",
                    dataIndex: "ensemblGeneId",
                    flex: 1.2,
                    xtype: "templatecolumn",
                    tpl: '<tpl><a href="http://www.ensembl.org/Homo_sapiens/Gene/Summary?g={ensemblGeneId}" target="_blank">{ensemblGeneId}</a>',
                },
                {
                    text: "Ensembl <br /> Gene Symbol",
                    dataIndex: "geneName",
                    flex: 0.9
                },
                {
                    text: "Ensembl <br />Transcript ID",
                    dataIndex: "ensemblTranscriptId",
                    flex: 1.3,
                    xtype: "templatecolumn",
                    tpl: '<tpl><a href="http://www.ensembl.org/Homo_sapiens/transview?transcript={ensemblTranscriptId}" target="_blank">{ensemblTranscriptId}</a>',
                },
                {
                    text: "SO Terms",
                    dataIndex: "soTerms",
                    flex: 1.7,
                    renderer: function(value, meta, rec, rowIndex, colIndex, store){

                        if(!_.isUndefined(value)){

                            var tempArray = [];
                            _.each(_.keys(value), function(key){
                                tempArray.push(this[key].soName);
                            },value);

                            var groupedArr = _.groupBy(tempArray);
                            var so_array = [];
                            _.each(_.keys(groupedArr), function(key){
                                var index =  _.indexOf(consequenceTypesHierarchy, key);
//                                        so_array.splice(index, 0, key+' ('+this[key].length+')');
//                                        so_array.push(key+' ('+this[key].length+')')
                                so_array[index] = key;
                            },groupedArr);
                            so_array =  _.compact(so_array);
                            value =
                                meta.tdAttr = 'data-qtip="'+ so_array.join('\n')+'"';
                            return value ? Ext.String.format(
                                '<tpl>'+so_array.join('<br />')+'</tpl>',
                                value
                            ) : '';
                        }else{
                            return '';
                        }

                    }
                },
                {
                    text: "Biotype",
                    dataIndex: "biotype",
                    flex: 1.3
                },
                {
                    text: "Codon",
                    dataIndex: "codon",
                    flex: 0.6
                },
                {
                    text: "cDna <br />Position",
                    dataIndex: "cDnaPosition",
                    flex: 0.6
                },
                {
                    text: "AA <br />Change",
                    dataIndex: "aaChange",
                    flex: 0.6
                }

            ],
            defaults: {
                align:'left' ,
                sortable : true
            }
        };



        var store = Ext.create("Ext.data.Store", {
            //storeId: "GenotypeStore",
            pageSize: 20,
            fields: [
                {name: 'ensemblGeneId', type: 'string'},
                {name: "geneName", type: "string"},
                {name: "soTerms", type: "auto"}
            ],
            data: annotData,
            proxy: {
                type: 'memory',
                enablePaging: true
            },
            sorters:
            {
                property: 'id',
                direction: 'ASC'
            }
        });

        var paging = Ext.create('Ext.PagingToolbar', {
            store: store,
            id: _this.id + "_pagingToolbar",
            pageSize: 20,
            displayInfo: true,
            displayMsg: 'Transcripts {0} - {1} of {2}',
            emptyMsg: "No records to display"
        });


        var grid = Ext.create('Ext.grid.Panel', {
            store: store,
            loadMask: true,
            width: 970,
//            height: 370,
            maxHeight:550,
            autoHeight: true,
            cls:'genotype-grid',
            margin: 20,
            viewConfig: {
                emptyText: 'No records to display',
                enableTextSelection: true
            },
            columns: annotColumns,
            tbar: paging
        });

        var annotPanel = Ext.create('Ext.panel.Panel', {
//            header:{
//                titlePosition:1
//            },
//            title: 'Annotation',
            border: false,
            layout: {
                type: 'vbox',
                align: 'fit'
            },
            overflowX:true,
            items: [grid]
        });

//        paging.doRefresh();


        return annotPanel;
    }
};
