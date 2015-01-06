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
function SpeciesFilterFormPanel(args) {
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("SpeciesFilterFormPanel");
    this.target;
    this.autoRender = true;
    this.title = "Species";
    this.border = false;
    this.collapsible = true;
    this.titleCollapse = false;
    this.headerConfig;
    

    //set instantiation args, must be last
    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render(this.targetId);
    }
}

SpeciesFilterFormPanel.prototype = {
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

        Ext.define('SpeciesListModel', {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'taxonomyCommonName', type: 'string'},
                {name: 'taxonomyCode',  type: 'string'},
                {name: 'assemblyName',       type: 'string'},
                {name: 'assemblyCode',  type: 'string'},
                {
                    name: 'displayName',
                    type: 'string',
                    convert: function( v, record ) {
                        return record.get( 'taxonomyCommonName' ) + ' / ' + record.get( 'assemblyName' )
                    }
                },
                {
                    name: 'value',
                    type: 'string',
                    convert: function( v, record ) {
                        console.log( record.get( 'taxonomyCommonName' ))
                        return record.get( 'taxonomyCode' ) + '_' + record.get( 'assemblyCode' )
                    }
                }
            ]
        });
        var speciesStore = Ext.create('Ext.data.Store', {
            model: 'SpeciesListModel',
            data : speciesList
        });

        var speciesFormField  =  Ext.create('Ext.form.ComboBox', {
            fieldLabel: 'Species / Genome Assembly',
//            id:'species',
            name:'species',
            labelAlign: 'top',
            store: speciesStore,
            queryMode: 'local',
            displayField: 'displayName',
            valueField: 'value',
            width: '100%',
            listeners: {
                afterrender: function (field) {
                    field.setValue('hsapiens_grch37');
                },
                change: function (field, newValue, oldValue) {
                    _this.trigger('species:change', {species: newValue, sender: _this});
                }

            }
        });

        return Ext.create('Ext.form.Panel', {
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
            items: [speciesFormField]
        });

    },
    getPanel: function () {
        return this.panel;
    },
    getValues: function () {
        var values = this.panel.getValues();
        for (key in values) {
            if (values[key] == '') {
                delete values[key]
            }
        }
        return values;
    },
    clear: function () {
        this.panel.reset();
    }
}
