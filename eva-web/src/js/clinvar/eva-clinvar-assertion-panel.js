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
//        var clinvarList = data.clinvarList;
//        console.log(data)
//        console.log('------')
//        for (var key in clinvarList) {
//            var clinVarAssertion = clinvarList[key].clinVarAssertion;
//            for (var key in clinVarAssertion) {
//                var assertData =  clinVarAssertion[key];
//                var asstPanel = this._createAssertPanel(assertData);
//            }
//            panels.push(asstPanel);
//        }
        var panels = [];
        var clinVarAssertion = data.clinvarSet.clinVarAssertion;
        for (var key in clinVarAssertion) {
                var assertData =  clinVarAssertion[key];
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
//        var lastEvaluated = new Date( data.clinicalSignificance.dateLastEvaluated ).toUTCString();
        var submittedDate = new Date( data.clinVarSubmissionID.submitterDate ).toUTCString();
        var origin = data.observedIn[0].sample.origin;
        var collectionMethod = data.observedIn[0].method[0].methodType;
        var alleOriginArray = [];
        var methodTypeArray = [];
        _.each(_.keys(data.observedIn), function(key){
            alleOriginArray.push(this[key].sample.origin);
            var method = this[key].method;
            _.each(_.keys(method), function(key){
                methodTypeArray.push(this[key].methodType);
            },method);

        },data.observedIn);

        var alleOrigin = '-';
        if(!_.isEmpty(alleOriginArray)){
            alleOriginArray = _.groupBy(alleOriginArray);
            alleOrigin = _.keys(alleOriginArray).join('<br />');
        }

        var methodType = '-';
        if(!_.isEmpty(methodTypeArray)){
            methodTypeArray = _.groupBy(methodTypeArray);
            methodType =  _.keys(methodTypeArray).join('<br />');
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
                    '<div class="col-md-12"><table class="table table-bordered eva-attributes-table">',
                        '<tr>',
                            '<td class="header">Submission Accession</td>',
                            '<td class="header">Clinical Significance</td>',
                            '<td class="header">Review status</td>',
                            '<td class="header">Date of Submission</td>',
//                            '<td class="header">Origin </td>',
//                            '<td class="header">Citations</td>',
                            '<td class="header">Submitter</td>',
                            '<td class="header">Method Type</td>',
                            '<td class="header">Allele origin</td>',
                            '<td class="header">Assertion Method</td>',
                        '</tr>',
                        '<tr>',
                            '<td>{clinVarAccession.acc}</td>',
                            '<td>{clinicalSignificance.description}</td>',
                            '<td>{clinicalSignificance.reviewStatus}</td>',
                            '<td>'+submittedDate+'</td>',
//                            '<td>'+origin+'</td>',
//                            '<td>'+alleOrigin+'</td>',
                            '<td>{clinVarSubmissionID.submitter}</td>',
                            '<td>'+methodType+'</td>',
                            '<td>'+alleOrigin+'</td>',
                            '<td>{assertion.type}</td>',
                        '</tr>',
                        '</table></div>'
                ),
                margin: '10 5 5 10'
            }]
        });

        return assertPanel;
    }
};
