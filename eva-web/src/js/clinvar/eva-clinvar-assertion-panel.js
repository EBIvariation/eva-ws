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
function ClinvarAssertionPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("ClinVarAssertionDataPanel");

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

ClinvarAssertionPanel.prototype = {
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
        this.assertionContainer.removeAll(true);
    },
    load: function (data) {
        this.clear();
        var panels = [];
        for (var key in data) {
            var assertData = data[key];
            var asstPanel = this._createAssertPanel(assertData);
            panels.push(asstPanel);
        }
        this.assertionContainer.add(panels);
    },
    _createPanel: function () {
        this.assertionContainer = Ext.create('Ext.container.Container', {
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
                    html: '<h4>Clinical Assertions</h4>',
                    margin: '5 0 10 10'
                },
                this.assertionContainer
            ],
            height: this.height
        });
        return panel;
    },
    _createAssertPanel: function (data) {
        console.log(data)
        var lastEvaluated = new Date( data.clinicalSignificance.dateLastEvaluated ).toUTCString();
        var origin = data.observedIn[0].sample.origin;
        var citation= 'NA';
        if(!_.isEmpty(data.citation) && data.citation[0].id.source == 'PubMed'){
            var citation = '<a href="http://www.ncbi.nlm.nih.gov/pubmed/'+data.citation[0].id.value+'" target="_blank">Pubmed</a>';
        }

        var assertPanel = Ext.create('Ext.panel.Panel', {
            title: data.clinVarAccession.acc,
            border: false,
            layout: 'vbox',
            overflowX: true,
            items: [  {
                xtype: 'container',
                data: data,
                width:970,
                tpl: new Ext.XTemplate(
                    '<div class="col-md-12"><table class="table table-bordered ocb-attributes-table">',
                        '<tr>',
                            '<td class="header">Clinical Significance <br/> (Last evaluated)</td>',
                            '<td class="header">Review status</td>',
                            '<td class="header">Origin </td>',
                            '<td class="header">Citations</td>',
                            '<td class="header">Submitter</td>',
                        '</tr>',
                        '<tr>',
                            '<td>{clinicalSignificance.description}<br/>('+lastEvaluated+')</td>',
                            '<td>{clinicalSignificance.reviewStatus}</td>',
                            '<td>'+origin+'</td>',
                            '<td>'+citation+'</td>',
                            '<td>{clinVarSubmissionID.submitter}</td>',
                        '</tr>',
                        '</table></div>'
                ),
                margin: '10 5 5 10'
            }]
        });

        return assertPanel;
    }
};
