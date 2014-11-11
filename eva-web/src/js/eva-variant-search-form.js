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
function EvaVariantSearchForm(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("EVAVariantSearchForm");
    this.target;
    this.title = "Beacon";
    this.height = 800;
    this.autoRender = true;
    this.border = false;
    if (this.autoRender) {
        this.render();
    }

}

EvaVariantSearchForm.prototype = {
    render: function () {
        if(!this.rendered) {
            this.div = document.createElement('div');
            this.div.setAttribute('id', this.id);
            this.panel = this._createPanel();
            this.rendered = true;
        }
    },

    load:function(){
        var _this = this;
        EvaManager.get({
            category: 'meta/studies',
            resource: 'list',
            success: function (response) {
                try {
                    studies = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                _this.projectStore.loadRawData(studies);
            }
        });
    },


    draw: function () {
        if(!this.rendered) {
            this.render();
        }
        // Checking whether 'this.target' is a HTMLElement or a string.
        // A DIV Element is needed to append others HTML Elements
        this.targetDiv = (this.target instanceof HTMLElement) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVAVariantSearchForm: target ' + this.target + ' not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.panel.render(this.div);
    },

    _createPanel: function () {
        var _this = this;

        this.projectStore = Ext.create('Ext.data.Store', {
            autoLoad: true,
            proxy: {
                type: 'memory',
                data: [],
                reader: {
                    type: 'json'
                }
            },
            fields: [
                {name: 'studyId', type: 'string'},
                {name: 'studyName', type: 'string'},

            ]
        });

        this.chromosomeStore = Ext.create('Ext.data.Store', {
            fields: ['text', 'value'],
            data:this._chromosomeValues()

        });


        var vSearchView = Ext.create('Ext.view.View', {
            width:1200,
            itemSelector: 'a.serviceLink',
            tpl: new Ext.XTemplate([
                '<div>',
                '<h2> GA4GH Variant Search</h2>',
//                '<p>Learn more about the Global Alliance for Genomics and Health (GA4GH) at <a href="http://genomicsandhealth.org" target="_blank">http://genomicsandhealth.org</a>as well as the GA4GH Beacon project: <a href="http://ga4gh.org/#/beacon" target="_blank">http://ga4gh.org/#/beacon</a> </p>',
                '<div class="row">',
                '<div class="col-md-12"><p><b>Example queries:</b></p>',
                '<div><p> <span><a href="#"  class="loadForm"  project="PRJEB4019">DatasetID:PRJEB4019</a></span></p></div>',
                '<div><p><span><a href="#"  class="loadForm" variantsetid="ERF218634">VariantSetId:ERF218634 </a></span></p></div>',
                '<div><p><span><a href="#"  class="loadForm" chrom="22" start="25662282" end="25662332">Reference:22&nbsp;Start:25662282 &nbsp;End:25662332&nbsp;</a></span></p><hr/></div>',
                '</div>',
                '</div>',
                '</div>'
                ]),
            listeners: {
                click: {
                    element: 'el', //bind to the underlying el property on the panel
                    delegate : 'a.loadForm',
                    fn: function(record,link){
                        _this._resetForm();
                        var project = link.getAttribute('project');
                        var variantSetId = link.getAttribute('variantsetid');
                        var chrom = link.getAttribute('chrom');
                        var start = link.getAttribute('start');
                        var end = link.getAttribute('end');
                        _this.formPanel.getForm().setValues({
                            vSearchProject:project,
                            vSearchVariantsetId:variantSetId,
                            vSearchChromosome:chrom,
                            vSearchStart:start,
                            vSearchEnd:end
                        })

                    }
                }
            }
        });

        this.project = Ext.create('Ext.form.ComboBox', {
            id: 'vSearchDatasetId',
            fieldLabel: 'Dataset ID',
            store: this.projectStore,
            queryMode: 'local',
            valueField: 'studyId',
            name: 'vSearchProject',
            width:650,
            tpl: Ext.create('Ext.XTemplate', '<tpl for=".">', '<div class="x-boundlist-item">{studyId} - {studyName}</div>', '</tpl>'),
            displayTpl: Ext.create('Ext.XTemplate', '<tpl for=".">', '{studyId} - {studyName}', '</tpl>'),
            labelWidth: 120
        });

        this.variantName = {
            xtype: 'textfield',
            id: 'vSearchVariantsetId',
            name: 'vSearchVariantsetId',
            fieldLabel: 'Variant Set ID',
//            allowBlank: false,
            labelWidth: 120
        };

        this.chromosome = Ext.create('Ext.form.ComboBox', {
            id: 'vSearchChromosome',
            fieldLabel: 'Reference Name',
            store: this.chromosomeStore,
            queryMode: 'local',
            valueField: 'value',
            displayField: 'text',
            name: 'vSearchChromosome',
            labelWidth: 120,
//            allowBlank: false
        });


        this.start = {
            xtype: 'textfield',
            id: 'vSearchStart',
            name: 'vSearchStart',
            fieldLabel: 'Start',
//            allowBlank: false,
            labelWidth: 120
          };

        this.end = {
            xtype: 'textfield',
            id: 'vSearchEnd',
            name: 'vSearchEnd',
            fieldLabel: 'End',
//            allowBlank: false,
            labelWidth: 120
           };
        this.formatType = {
                            xtype      : 'radiogroup',
                            id: 'vSearchFormatType',
                            fieldLabel : 'Format Type',
                            defaultType: 'radiofield',
                            allowBlank: false,
                            width:300,
                            labelWidth: 120,
                            defaults: {
                                flex: 1
                            },
//                            layout: 'hbox',
                            items: [
                                {
                                    boxLabel  : 'Text',
                                    name      : 'vSearchFormatType',
                                    inputValue: 'text',
                                    id        : 'vSearchFormatTypeText'
                                }, {
                                    boxLabel  : 'JSON',
                                    name      : 'vSearchFormatType',
                                    inputValue: 'json',
                                    id        : 'vSearchFormatTypeJson'
                                }
                            ]
                         };

        this.resultPanel = {
            xtype: 'panel',
            id: 'variant-search-result-panel',
            title: 'Result',
            region: 'center',
            height:225,
            width:530,
            hidden: true
        };

        this.formPanel = Ext.create('Ext.form.Panel', {
            border:false,
            layout: 'vbox',
//            labelWidth: 100,
            defaults: {
                margin: 5
            },
//            width:650,
            items: [
                vSearchView,
                this.project,
                this.variantName,
                this.chromosome,
                this.start,
                this.end,
                this.resultPanel
            ],
            buttons: [{
                text: 'Reset',
                handler: function() {
                    _this._resetForm();
                }
            }, {
                text: 'Search by Variantset ID',
                formBind: true,
                //only enabled once the form is valid
                disabled: true,
                handler: function() {
                    var form = this.up('form').getForm();
                    if (form.isValid()) {
                        var region =  form.getValues().vSearchChromosome+':'+ form.getValues().coordinate+'::'+form.getValues().allele;
                        var params = form.getValues().project;
                        var resultPanel = Ext.getCmp('variant-search-result-panel');
                        var vSearchVariantsetId = form.getValues().vSearchVariantsetId;
                        if(_.isEmpty(vSearchVariantsetId)){
                            Ext.Msg.alert('','Please Enter a value in \'Variant Set ID\' ');
                        }else{
                            EvaManager.get({
                                category: 'ga4gh',
                                resource: 'callsets/search',
                                params:{variantSetIds:vSearchVariantsetId,pageSize:1},
                                success: function (response) {
                                    try {
                                        var resultTplMarkup = '<br/>'+JSON.stringify(response.callSets[0]);
                                        resultPanel.setVisible(true);
                                        resultPanel.update(resultTplMarkup);
                                        resultPanel.setWidth(530);

                                    } catch (e) {
                                        console.log(e);
                                    }
                                }
                            });
                        }

                    }
                }

            }, {
                text: 'Search by Dataset ID',
                formBind: true,
                //only enabled once the form is valid
                disabled: true,
                handler: function() {
                    var form = this.up('form').getForm();
                    if (form.isValid()) {
                        var region =  form.getValues().vSearchChromosome+':'+ form.getValues().coordinate+'::'+form.getValues().allele;
                        var params = form.getValues().project;
                        var resultPanel = Ext.getCmp('variant-search-result-panel');
                        var vSearchProject = form.getValues().vSearchProject;
                        if(_.isEmpty(vSearchProject)){
                            Ext.Msg.alert('','Please Enter a value in \'Dataset ID\' ');
                        }else{
                            EvaManager.get({
                                category: 'ga4gh',
                                resource: 'variantsets/search',
                                params:{datasetIds:vSearchProject,pageSize:1},
                                success: function (response) {
                                    try {
                                        var resultTplMarkup = '<br/>'+JSON.stringify(response.variantSets[0]);
                                        resultPanel.setVisible(true);
                                        resultPanel.update(resultTplMarkup);
                                        resultPanel.setWidth(1230);

                                    } catch (e) {
                                        console.log(e);
                                    }
                                }
                            });
                        }
                    }
                }

            },
            {
                text: 'Search by Reference',
                formBind: true,
                //only enabled once the form is valid
                disabled: true,
                handler: function() {
                    var form = this.up('form').getForm();
                    if (form.isValid()) {
                        var region =  form.getValues().vSearchChromosome+':'+ form.getValues().coordinate+'::'+form.getValues().allele;
                        var params = form.getValues().project;
                        var resultPanel = Ext.getCmp('variant-search-result-panel');
                        var vSearchChromosome = form.getValues().vSearchChromosome;
                        var vSearchStart = form.getValues().vSearchStart;
                        var vSearchEnd = form.getValues().vSearchEnd;

                        if(_.isEmpty(vSearchChromosome)||_.isEmpty(vSearchStart)||_.isEmpty(vSearchEnd)){
                            Ext.Msg.alert('','Please do not allow blank in \'Reference Name\' or \'Start\' or \'End Field\'');
                        }else{
                            EvaManager.get({
                                category: 'ga4gh',
                                resource: 'variants/search',
                                params:{referenceName:vSearchChromosome,start:vSearchStart,end:vSearchEnd,pageSize:1},
                                success: function (response) {
                                    try {
                                        var resultTplMarkup = '<br/>'+JSON.stringify(response.variants[0]);
                                        resultPanel.setVisible(true);
                                        resultPanel.update(resultTplMarkup);
                                        resultPanel.setWidth(1230);

                                    } catch (e) {
                                        console.log(e);
                                    }
                                }
                            });
                        }
                    }
                }

            }

            ],
            buttonAlign:'left'
        });
        this.load();

        return  this.formPanel;
    },

    setLoading: function (loading) {
        this.panel.setLoading(loading);
    },

    update: function () {
        if (this.panel) {
            this.panel.update();
        }
    },
    getPanel: function(){
        this.load();
        return this.panel;
    },
    _resetForm:function(){
        var _this = this;
        var resultPanel = Ext.getCmp('variant-search-result-panel');
        resultPanel.setVisible(false);
        _this.formPanel.getForm().reset();
    },
    _chromosomeValues : function(){
        var chrmArr = [];
        for (var i = 1; i < 25; i++) {
            if(i == 23){
                chrmArr.push({
                    "text": "Chr X",
                    "value": 23
                });
            }
            else if(i == 24){
                chrmArr.push({
                    "text": "Chr Y",
                    "value": 24
                });
            }else{
                chrmArr.push({
                    "text": i,
                    "value":i
                });
            }

        }
        return chrmArr;
    }

};
