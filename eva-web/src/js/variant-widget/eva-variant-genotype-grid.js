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
function EvaVariantGenotypeGrid(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("VariantGenotypeGrid");

    this.autoRender = true;
    this.storeConfig = {};
    this.gridConfig = {};
    this.height = 500;
    this.target;
    this.columns = [
        {
            text: "Sample",
            dataIndex: "sample",
            flex: 1
        },
        {
            text: "Genotype",
            dataIndex: "genotype",
            flex: 1
        },
        {
            text: "Sex",
            dataIndex: "sex",
            flex: 1
        },
        {
            text: "Phenotype",
            dataIndex: "phenotype",
            flex: 1
        }
    ];

    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render(this.targetId);
    }
}

EvaVariantGenotypeGrid.prototype = {
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
        this.studiesContainer.removeAll(true);
    },
    load: function (data) {
        this.clear();
        var panels = [];
        var genotypeData = [];
        for (var key in data) {
            var study = data[key];
            if (Object.keys(study.samplesData).length > 0) {
                genotypeData.push(study);
            }
//            if (Object.keys(study.samplesData).length > 0) {
//                panels.push(this._createStudyPanel(study));
//            }

        }
        this.panel.removeAll()
        this.panel.add(this._createGenotypePanel(genotypeData));

    },
    _createPanel: function () {
        this.studiesContainer = Ext.create('Ext.container.Container', {
            layout: {
                type: 'accordion',
                titleCollapse: true,
//                fill: false,
                multi: true
            }
        });

        this.panel = Ext.create('Ext.container.Container', {
            layout: {
                type: 'vbox',
                align: 'fit'
            },
            overflowY: true,
            padding: 10,
            items: [
                {
                    xtype: 'box',
                    cls: 'ocb-header-4',
                    html: '<h4>Genotypes </h4>',
                    margin: '5 0 10 10'
                }
            ],
            height: this.height
        });
        return this.panel;
    },
    _createGenotypePanel: function (data) {

     if(_.isEmpty(data)){
         var grid = Ext.create('Ext.view.View', {
             tpl: new Ext.XTemplate(['<div>No Genotypes data available</div>'])
         });
     }else{
         var genotypeColumns = {
             items:[
             {
                 text: "Study",
                 dataIndex: "studyId",
                 flex: 4,
                 renderer: function(value, metaData, record, row, col, store, gridView){
                     console.log(value)
                     var studyName =  _.findWhere(projects, {studyId:value}).studyName;
                     return '<a href="?eva-study='+value+'" target="_blank">'+studyName+'</a> ('+ value+')';
                 }
             },
             {
                 text: "Samples Count",
                 dataIndex: "samplesData",
                 flex: 2,
                 renderer: function(value, metaData, record, row, col, store, gridView){
                     return _.keys(value).length;
                 }
             }],
             defaults: {
             align:'center' ,
                 sortable : true
             }
         };

         var store = Ext.create("Ext.data.Store", {
             //storeId: "GenotypeStore",
             pageSize: 10,
             fields: [
                 {name: "studyId", type: "string" },
                 {name: "samplesData", type: "auto"}
             ],
             data: data,
             proxy: {type: 'memory'}
         });



         var plugins =  [{
             ptype: 'rowexpander',
             rowBodyTpl : new Ext.XTemplate(),
         }];

         var grid = Ext.create('Ext.grid.Panel', {
             store: store,
             loadMask: true,
             width: 800,
//            height: 300,
             cls:'genotype-grid',
             margin: 20,
             viewConfig: {
                 emptyText: 'No records to display',
                 enableTextSelection: true
             },
             columns: genotypeColumns,
             plugins:plugins


         });

         grid.view.on('expandbody', function(rowNode, record, body, rowIndex){

             var samples = record.data.samplesData;
             var finalData = [];
             for (var key in samples) {
                 var s = samples[key];
                 finalData.push({
                     sample: key,
                     genotype: s.GT
                 });
             }
             var divID = 'genotype-grid-'+record.data.studyId;
             body.innerHTML = '<div id="'+divID+'"></div>';


             var store = Ext.create('Ext.data.Store', {
                 fields: [
                     {name: 'sample'},
                     {name: 'genotype'}
                 ],
                 data: finalData
             });
             var grid = Ext.create('Ext.grid.Panel', {
                 store: store,
                 stateful: true,
                 stateId: 'stateGrid',
                 columns: {
                     items:[{
                         text     : 'Sample',
                         flex     : 1,
                         sortable : false,
                         dataIndex: 'sample'
                        },
                         {
                             text     : 'Genotype',
                             flex     : 1,
                             sortable : false,
                             dataIndex: 'genotype'
                         }],
                         defaults: {
                             align:'center' ,
                             sortable : true
                         }

                 },
                 height: 350,
                 width: 600,
                 renderTo: divID,
                 viewConfig: {
                     stripeRows: true
                 }
             });

         });
         //suppress Uncaught TypeError: Cannot read property 'isGroupHeader' of null
         Ext.define('SystemFox.overrides.view.Table', {
             override: 'Ext.view.Table',
             checkThatContextIsParentGridView: function(e){
                 var target = Ext.get(e.target);
                 var parentGridView = target.up('.x-grid-view');
                 if (this.el != parentGridView) {
                     /* event of different grid caused by grids nesting */
                     return false;
                 } else {
                     return true;
                 }
             },
             processItemEvent: function(record, row, rowIndex, e) {
                 if (e.target && !this.checkThatContextIsParentGridView(e)) {
                     return false;
                 } else {
                     return this.callParent([record, row, rowIndex, e]);
                 }
             }
         });
     }



        return grid;


    },
    _getGenotypeCount: function (gc) {
        var res = [];
        for (var key in gc) {
            res.push({
                genotype: key,
                count: gc[key]
            })
        }
        return res;
    }
};
