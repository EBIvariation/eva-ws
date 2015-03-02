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
function EvaPopulationFrequencyFilterFormPanel(args) {
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("PopulationFrequencyFilterFormPanel");
    this.target;
    this.autoRender = true;
    this.title = "Population Frequency";
    this.border = false;
    this.collapsible = true;
    this.titleCollapse = false;
    this.collapsed = false;
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

EvaPopulationFrequencyFilterFormPanel.prototype = {
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

        var genomesitems = {
            xtype:'fieldset',
            title: '1000Genomes',
            collapsible: false,
            height:240,
            width :280,
            defaultType: 'textfield',
            items :[
                {
                    fieldLabel: 'All MAF \< ',
                    name: 'ALL',
                    width  : 240,
                    labelWidth:120
                },
                {
                    fieldLabel: 'African MAF \< ',
                    name: 'AFR',
                    width  : 240,
                    labelWidth:120
                },
                {
                    fieldLabel: 'American MAF \< ',
                    name: 'AMR',
                    width  : 240,
                    labelWidth:120
                },
                {
                    fieldLabel: 'Asian MAF \< ',
                    name: 'ASN',
                    width  : 240,
                    labelWidth:120
                },
                {
                    fieldLabel: 'European MAF \<',
                    name: 'EUR',
                    width  : 240,
                    labelWidth:120
                }
            ]
        }
        var ESP6500 = {
            xtype:'fieldset',
            title: 'ESP6500',
            collapsible: false,
            height:180,
            width :280,
            defaultType: 'textfield',
            items :[
                {
                    fieldLabel: 'African-American MAF \<',
                    name: 'AA_AF',
                    width  : 240,
                    labelWidth:120
                },
                {
                    fieldLabel: ' European-American MAF \< ',
                    name: 'EA_AF',
                    width  : 240,
                    labelWidth:120
                }

            ]
        }

        return Ext.create('Ext.form.Panel', {
            id:this.id,
            bodyPadding: "5",
            margin: "0 0 5 0",
            buttonAlign: 'center',
            layout: 'vbox',
            title: this.title,
            border: this.border,
            collapsible: this.collapsible,
            titleCollapse: this.titleCollapse,
            header: this.headerConfig,
            collapsed: this.collapsed,
            allowBlank: false,
            items: [genomesitems,ESP6500]
        });

    },
    getPanel: function () {
        return this.panel;
    },
    getValues: function () {
        var values = this.panel.getValues();
        var valuesArray = {};
        for (key in values) {
            if (values[key] == '') {
                delete values[key]
            }else{
                valuesArray[key] = values[key];
            }
        }

//        return {pop_freq:valuesArray};
    },
    clear: function () {
        this.panel.reset();
    }
}
