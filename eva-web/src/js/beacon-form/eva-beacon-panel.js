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
function EvaBeaconPanel(args) {
    var _this = this;
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("EVABeaconPanel");
    this.target;
    this.tools = [];
    _.extend(this, args);
    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};


EvaBeaconPanel.prototype = {
    render: function () {
        var _this = this;
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
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVABeaconPanel target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.panel.render(this.div);

    },
    show: function () {
        this.panel.show()
    },
    hide: function () {
        this.panel.hide();
    },
    toggle: function () {
        if (this.panel.isVisible()) {
            this.panel.hide();
        } else {
            this.panel.show();
        }
    },
    _createPanel: function () {

        var evaBeaconForm = new EvaBeaconForm();
        var evaVariantSearchForm = new EvaVariantSearchForm();

        this.toolTabPanel = Ext.create("Ext.tab.Panel", {
                activeTab: 0,
                plain: true,
                items: [
                    {
                        title: 'Beacon',
                        items:[evaBeaconForm.getPanel()],
                        height:1200
                    },
                    {
                        title: 'Variant Search',
                        items:[evaVariantSearchForm.getPanel()],
                        height:1200
                    }
                ]
            });

        this.toolTabPanel.setActiveTab(0);

        this.panel = Ext.create('Ext.container.Container', {
            flex: 1,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                margin: 5
            },
            cls:'eva-beacon-panel',
            items: [this.toolTabPanel]
        });

        return  this.panel;
    }


};
function loadEvaBeacon(el) {
    var chrom = el.getAttribute('chrom');
    var coordinate = el.getAttribute('coordinate');
    var allele = el.getAttribute('allele');
    var project = el.getAttribute('project');
    var chromEl = document.querySelector('#beacon-chrom');
    var coordinateEl = document.querySelector('#beacon-coordinate');
    var alleleEl = document.querySelector('#beacon-allele');
    var projectEl =  document.querySelector('#beacon-project');
    chromEl.value = chrom;
    coordinateEl.value = coordinate;
    alleleEl.value = allele;
    projectEl.value = project;
}


