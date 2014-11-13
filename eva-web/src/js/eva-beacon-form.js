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
function EvaBeaconForm(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("BeaconForm");
    this.target;
    this.title = "Beacon";
    this.height = 800;
    this.autoRender = true;
    this.border = false;
    if (this.autoRender) {
        this.render();
    }

}

EvaBeaconForm.prototype = {
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
            console.log('EVABeaconForm: target ' + this.target + ' not found');
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

        var beaconView = Ext.create('Ext.view.View', {
            width:1200,
            itemSelector: 'a.serviceLink',
            tpl: new Ext.XTemplate([
                '<div>',
                '<h2>EBI GA4GH Beacon</h2>',
                '<p>Learn more about the Global Alliance for Genomics and Health (GA4GH) at <a href="http://genomicsandhealth.org" target="_blank">http://genomicsandhealth.org</a>as well as the GA4GH Beacon project: <a href="http://ga4gh.org/#/beacon" target="_blank">http://ga4gh.org/#/beacon</a> </p>',
                '<div class="row">',
                '<div class="col-md-12"><p><b>Example queries:</b></p>',
                '<div><p> <span><a href="#"  class="loadForm" chrom="11" coordinate="110993" allele="INDEL" project="PRJEB4019">Reference:11&nbsp;Coordinate:110993 &nbsp;Allele:INDEL&nbsp;Project:PRJEB4019</a></span></p></div>',
                '<div><p><span><a href="#"  class="loadForm" chrom="15" coordinate="20025703" allele="<DEL>" project="PRJEB4019">Reference:15&nbsp;Coordinate:20025703 &nbsp;Allele:&lt;DEL&gt;&nbsp;Project:PRJEB4019 </a></span></p></div>',
                '<div><p><span><a href="#"  class="loadForm" chrom="15" coordinate="20025703" allele="<DEL>" project="PRJEB5439">Reference:15&nbsp;Coordinate:20025703 &nbsp;Allele:&lt;DEL&gt;&nbsp;Project:PRJEB5439</a></span></p><hr/></div>',
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
                        var chrom = link.getAttribute('chrom');
                        var coordinate = link.getAttribute('coordinate');
                        var allele = link.getAttribute('allele');
                        _this.project.setValue(project);

                        _this.formPanel.getForm().setValues({
                            beaconChromosome:chrom,
                            beaconCoordinate:coordinate,
                            beaconAllele:allele
                        })

                    }
                }
            }
        });

        this.project =  Ext.create('Ext.form.ComboBox', {
            id: 'beacon-project',
            fieldLabel: 'Dataset',
            store: this.projectStore,
            queryMode: 'local',
            valueField: 'studyId',
            name: 'beaconProject',
            width:650,
            labelWidth: 120,
            tpl: Ext.create('Ext.XTemplate', '<tpl for=".">', '<div class="x-boundlist-item">{studyId} - {studyName}</div>', '</tpl>'),
            displayTpl: Ext.create('Ext.XTemplate', '<tpl for=".">', '{studyId} - {studyName}', '</tpl>')
        });

        this.chromosome = Ext.create('Ext.form.ComboBox', {
            id: 'beaconChromosome',
            fieldLabel: 'Reference Name',
            store: this.chromosomeStore,
            queryMode: 'local',
            valueField: 'value',
            displayField: 'text',
            name: 'beaconChromosome',
            allowBlank: false,
            labelWidth: 120
        });

        this.coordinate = {
                            xtype: 'textfield',
                            id: 'beaconCoordinate',
                            name: 'beaconCoordinate',
                            fieldLabel: 'Start',
                            allowBlank: false,
                            labelWidth: 120,
                            regex: /^[1-9]+$/
                          };
        this.allele =     {
                            xtype: 'textfield',
                            id: 'beaconAllele',
                            name: 'beaconAllele',
                            fieldLabel: 'Allele',
                            allowBlank: true,
                            labelWidth: 120
                          };
        this.formatType = {
                            xtype      : 'radiogroup',
                            id: 'beaconFormatType',
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
                                    name      : 'formatType',
                                    inputValue: 'text',
                                    id        : 'text'
                                }, {
                                    boxLabel  : 'JSON',
                                    name      : 'formatType',
                                    inputValue: 'json',
                                    id        : 'json'
                                }
                            ]
                         };

        this.resultPanel = {
            xtype: 'panel',
            id: 'beacon-result-panel',
            title: 'Result',
            region: 'center',
            height:225,
            width:630,
            hidden: true
        };

        this.formPanel = Ext.create('Ext.form.Panel', {
            border:false,
            defaults: {
                margin: 5
            },
//            width:650,
            items: [

                beaconView,
                this.project,
                this.chromosome,
                this.coordinate,
                this.allele,
                this.formatType,
                this.resultPanel
            ],
            buttons: [{
                text: 'Reset',
                handler: function() {
                    _this._resetForm();
                }
            }, {
                text: 'Submit',
                formBind: true,
                //only enabled once the form is valid
                disabled: true,
                handler: function() {
                    var form = this.up('form').getForm();
                    if (form.isValid()) {
                        var region =  form.getValues().beaconChromosome+':'+ form.getValues().beaconCoordinate+'::'+form.getValues().beaconAllele;
//                        var params ={studies:form.getValues().beaconProject};
                        var params ={referenceName:form.getValues().beaconChromosome,start:form.getValues().beaconCoordinate,allele:form.getValues().beaconAllele,datasetIds:form.getValues().beaconProject};
                        var resultPanel = Ext.getCmp('beacon-result-panel');
                        var studyName = Ext.getCmp('beacon-project').getRawValue();
                        EvaManager.get({
                            category: 'ga4gh',
                            resource: 'beacon',
//                            query:region,
                            params:params,
                            success: function (response) {
                                try {
                                    var exists = response.exists;
                                    var resultTplMarkup;
                                    var cssClass;
                                    if(form.getValues().formatType == 'text'){
                                        if(exists){
                                            cssClass = 'valid';
                                        }else{
                                            cssClass = 'invalid';
                                        }
                                        var projectName = '<a href="?eva-study='+form.getValues().beaconProject+'" target="_blank">'+studyName+'</a>';
                                        resultTplMarkup ='<table  class="table table-bordered">' +
                                                            '<tr><td>Project</td><td>'+projectName+'</td></tr>'+
                                                            '<tr><td>Chromosome</td><td>'+form.getValues().beaconChromosome+'</td></tr>'+
                                                            '<tr><td>Coordinate</td><td>'+form.getValues().beaconCoordinate+'</td></tr>'+
                                                            '<tr><td>Allele</td><td>'+_.escape(form.getValues().beaconAllele)+'</td></tr>'+
                                                            '<tr><td>Exist</td><td class="'+cssClass+'">'+exists+'</td></tr>'+
                                                          '</table>'
                                        resultPanel.setHeight(225);
                                        resultPanel.setWidth(630);
                                    }else{
                                        console.log()
                                        resultTplMarkup = {query:{Project:studyName,Chromosome:form.getValues().beaconChromosome,Coordinate:form.getValues().beaconCoordinate,Allele:_.escape(form.getValues().beaconAllele)},Exist:exists};
                                        resultTplMarkup = '<br/><pre><code>'+vkbeautify.json(resultTplMarkup)+'</code></pre>';
//                                        resultTplMarkup = '<br/>'+JSON.stringify(resultTplMarkup);
                                        resultPanel.setHeight(300);
                                        resultPanel.setWidth(900);
                                    }

                                    resultPanel.setVisible(true);
                                    resultPanel.update(resultTplMarkup);

                                } catch (e) {
                                    console.log(e);
                                }
                            }
                        });

                    }
                }
            }],
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
        var resultPanel = Ext.getCmp('beacon-result-panel');
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
