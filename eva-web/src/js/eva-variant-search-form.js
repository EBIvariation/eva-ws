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


        var vSearchView = Ext.create('Ext.view.View', {
            width:1200,
            itemSelector: 'a.serviceLink',
            tpl: new Ext.XTemplate([
                '<div>',
                '<h2>EBI GA4GH Beacon</h2>',
                '<p>Learn more about the Global Alliance for Genomics and Health (GA4GH) at <a href="http://genomicsandhealth.org" target="_blank">http://genomicsandhealth.org</a>as well as the GA4GH Beacon project: <a href="http://ga4gh.org/#/beacon" target="_blank">http://ga4gh.org/#/beacon</a> </p>',
                '<div class="row">',
                '<div class="col-md-12"><p><b>Example queries:</b></p>',
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
                            vSearchChromosome:chrom,
                            vSearchCoordinate:coordinate,
                            vSearchAllele:allele
                        })

                    }
                }
            }
        });


        this.chromosome = {
                            xtype: 'numberfield',
                            id:'vSearchChromosome',
                            name: 'chromosome',
                            fieldLabel: 'Chromosome',
                            minValue: 1,
                            maxValue: 24,
                            allowBlank: false
                          };



        this.start = {
                        xtype: 'textfield',
                        id: 'vSearchStart',
                        name: 'start',
                        fieldLabel: 'Start',
                        allowBlank: false
                      };

        this.end = {
                    xtype: 'textfield',
                    id: 'vSearchEnd',
                    name: 'end',
                    fieldLabel: 'End',
                    allowBlank: false
                   };
        this.reference =     {
                            xtype: 'textfield',
                            id: 'vSearchReferenceName',
                            name: 'referenceName',
                            fieldLabel: 'Reference Name',
                            allowBlank: true
                          };
        this.formatType = {
                            xtype      : 'radiogroup',
                            id: 'vSearchFormatType',
                            fieldLabel : 'Format Type',
                            defaultType: 'radiofield',
                            allowBlank: false,
                            width:300,
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
            defaults: {
                margin: 5
            },
//            width:650,
            items: [

                vSearchView,
                this.chromosome,
                this.start,
                this.end,
                this.reference,
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
                        var region =  form.getValues().chromosome+':'+ form.getValues().coordinate+'::'+form.getValues().allele;
                        var params = form.getValues().project;
                        var resultPanel = Ext.getCmp('variant-search-result-panel');
                        var studyName = Ext.getCmp('vSearchProject').getRawValue();

                        EvaManager.get({
                            category: 'variants',
                            resource: 'exists',
                            query:region,
                            params:params,
                            success: function (response) {
                                try {
                                    var exists = response.response[0].result[0];
                                    var resultTplMarkup;
                                    var cssClass;
                                    if(form.getValues().vSearchFormatType == 'text'){
                                        if(exists){
                                            cssClass = 'valid';
                                        }else{
                                            cssClass = 'invalid';
                                        }
                                        var projectName = '<a href="?eva-study='+form.getValues().project+'" target="_blank">'+studyName+'</a>';
                                        resultTplMarkup ='<table  class="table table-bordered">' +
                                                            '<tr><td>Project</td><td>'+projectName+'</td></tr>'+
                                                            '<tr><td>Chromosome</td><td>'+form.getValues().chromosome+'</td></tr>'+
                                                            '<tr><td>Coordinate</td><td>'+form.getValues().coordinate+'</td></tr>'+
                                                            '<tr><td>Allele</td><td>'+form.getValues().allele+'</td></tr>'+
                                                            '<tr><td>Exist</td><td class="'+cssClass+'">'+exists+'</td></tr>'+
                                                          '</table>'

                                    }else{
                                        resultTplMarkup = {query:{Project:studyName,Chromosome:form.getValues().chromosome,Coordinate:form.getValues().coordinate,Allele:form.getValues().allele},Exist:exists};
                                        resultTplMarkup = '<br/>'+JSON.stringify(resultTplMarkup);
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
        return this.panel;
    },
    _resetForm:function(){
        var _this = this;
        var resultPanel = Ext.getCmp('variant-search-result-panel');
        resultPanel.setVisible(false);
        _this.formPanel.getForm().reset();
    }

};
