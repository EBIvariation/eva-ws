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
function ClinVarPositionFilterFormPanel(args) {
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("ClinVarPositionFilterFormPanel");
    this.target;
    this.autoRender = true;
    this.title = "Position";
    this.border = false;
    this.collapsible = true;
    this.titleCollapse = false;
    this.headerConfig;
    this.testRegion = "";
    this.emptyText = '1:1-1000000,2:1-1000000';

    //set instantiation args, must be last
    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render(this.targetId);
    }
}

ClinVarPositionFilterFormPanel.prototype = {
    render: function () {
        var _this = this;
        console.log("Initializing " + this.id);

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
    _createPanel: function () {
        var _this = this;
        var accessionId = Ext.create('Ext.form.field.TextArea', {
            id: this.id + "accessionId",
            name: "accessionId",
            margin: '0 0 0 5',
            //allowBlank: true,
            width: '100%',
            fieldLabel: 'ClinVar Accession',
            labelAlign: 'top',
//            regex: /^[R][C][V]\d+$/,
            listeners: {
                'change': function(field, newVal, oldVal){
                    if(newVal){
                        _this.disableFields(_this.id+"accessionId");
                    }else{
                        _this.enableFields();
                    }
                }
            }
        });
        var rsId = Ext.create('Ext.form.field.TextArea', {
            id: this.id + "rsId",
            name: "rs",
            margin: '0 0 0 5',
            //allowBlank: true,
            width: '100%',
            fieldLabel: 'dbSNP Accession',
            labelAlign: 'top',
//            regex: /^[rs]s\d+$/,
            listeners: {
                'change': function(field, newVal, oldVal){
                    if(newVal){
                        _this.disableFields(_this.id+"rsId");
                    }else{
                        _this.enableFields();
                    }
                }
            }
        });

        var regionList = Ext.create('Ext.form.field.TextArea', {
            id: this.id + "region",
            name: "region",
            emptyText:  this.emptyText,
            margin: '0 0 0 5',
            //allowBlank: true,
            width: '100%',
            fieldLabel: 'Chromosomal Location',
            labelAlign: 'top',
//            value: this.testRegion,
            listeners: {
                'change': function(field, newVal, oldVal){
                    if(newVal){
                        _this.disableFields(_this.id+"region");
                    }else{
                        _this.enableFields();
                    }
                }
            }
        });

        var gene = Ext.create('Ext.form.field.TextArea', {
            id: this.id + "gene",
            name: "gene",
            margin: '0 0 0 5',
            //allowBlank: true,
            width: '100%',
            fieldLabel: 'HGNC Gene Symbol',
            labelAlign: 'top',
            listeners: {
                'change': function(field, newVal, oldVal){
                    if(newVal){
                        _this.disableFields(_this.id+"gene");
                    }else{
                        _this.enableFields();
                    }
                }
            }

        });

        var assemblyText = {
            xtype: 'label',
            forId: 'myFieldId',
            html: '<span style="font-size:14px;color:#996a44;"> Assembly : GRCh37</span>',
            margin: '0 0 10 5',
            cls:'color:red'
        };

        this.panel =  Ext.create('Ext.form.Panel', {
                            bodyPadding: "5",
                            margin: "0 0 5 0",
                            buttonAlign: 'center',
                            layout: 'vbox',
                            title: this.title,
                            border: this.border,
                            collapsible: this.collapsible,
                            titleCollapse: this.titleCollapse,
                            header: this.headerConfig,
                            allowBlank: false,
                            items: [assemblyText,accessionId,regionList, gene]
                        });

        this.panel.getForm().findField('region').setValue(_this.testRegion);

        return this.panel;

    },
    getPanel: function () {
        return this.panel;
    },
    disableFields:function(id){
        this.panel.getForm().getFields().each(function(field) {
           if(id != field.id){
               field.disable(true);
           }
        });
    },
    enableFields:function(){
        this.panel.getForm().getFields().each(function(field) {
           field.enable(true);
        });
    },
    getValues: function () {
        var values = this.panel.getValues();
        for (key in values) {
            if(key == 'gene'){
                values['gene'] = values.gene.toUpperCase();
            }
            if (values[key] == '') {
                delete values[key]
            }else{
                values[key] = values[key].replace(/\s/g, "");
            }
        }
        return values;
    },
    clear: function () {
        this.panel.reset();
    }
}
