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
function VariantRawDataPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("VariantRawDataPanel");

    this.target;
    this.title = "Raw Data";
    this.height = 500;
    this.autoRender = true;

    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render();
    }
}

VariantRawDataPanel.prototype = {
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

        for (var key in data) {
            var study = data[key];
            var studyPanel = this._createStudyPanel(study);
            panels.push(studyPanel);

        }

        this.studiesContainer.add(panels);
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

        var panel = Ext.create('Ext.container.Container', {
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
                    html: '<h4>VCF data</h4>',
                    margin: '5 0 10 10'
                },
                this.studiesContainer
            ],
            height: this.height
        });
        return panel;
    },
    _createStudyPanel: function (data) {

        var stats = (data.stats) ? data.stats : {};
        var attributes = (data.attributes) ? data.attributes : {};
        // removing src from attributes
        var attributesData = {};
        _.extend(attributesData,attributes);
        delete attributesData['src'];
        delete attributesData['ACC'];

        //TO BE REMOVED
        var study_title;
        if(projects){
            for (var i = 0; i < projects.length; i++) {
                if (projects[i].studyId === data.studyId) {
                    study_title = '<a href="?eva-study='+projects[i].studyId+'" target="_blank">'+projects[i].studyName+'</a> ('+ projects[i].studyId +')';
                }
            }
        }else{
            study_title = '<a href="?eva-study='+data.studyId+'" target="_blank">'+data.studyId+'</a>';
        }

        var vcfHeader = attributes['src'];
        var vcfHeaderId = Utils.genId("vcf-header");

        var rawDataPanel = Ext.create('Ext.panel.Panel', {
            title: study_title,
            border: false,
            layout: 'vbox',
            overflowX: true,
            items: [
                {
                    xtype: 'container',
                    cls: 'ocb-header-5',
                    margin: '5 5 10 10',
                    layout: 'vbox',
                    items: [
                        {
                            xtype: 'container',
                            id:vcfHeaderId,
                            data: vcfHeader,
                            tpl: new Ext.XTemplate('<div>{vcfHeader}</div>'),
                            margin: '5 5 5 10'
//                            hidden: true
                        }
                    ]
                }

            ]
        });

        return rawDataPanel;
    }

};
