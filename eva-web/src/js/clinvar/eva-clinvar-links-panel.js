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
function ClinvarLinksPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("ClinVarLinksDataPanel");

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

ClinvarLinksPanel.prototype = {
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
        this.linksContainer.removeAll(true);
    },
    load: function (data) {
        this.clear();
        var panels = [];
//        var summaryPanel = this._createSummaryPanel(data.clinvarList);
//        var clinvarList = data.clinvarList;
//        for (var key in clinvarList) {
//            var linksData = clinvarList[key];
//            var linksPanel = this._createLinksPanel(linksData);
//            panels.push(linksPanel);
//        }
//        this.linksContainer.removeAll();
//        this.linksContainer.add(panels);

        var linksData = data;
        var panel = this._createLinksPanel(linksData);
        this.linksContainer.removeAll();
        this.linksContainer.add(panel);
    },
    _createPanel: function () {
        this.linksContainer = Ext.create('Ext.container.Container', {
            layout: {
//                type: 'accordion',
                type: 'vbox',
                titleCollapse: true,
//                fill: false,
                multi: true
            }
        });

      this.panel = Ext.create('Ext.container.Container', {
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
                    html: '<h4>External Links</h4>',
                    margin: '5 0 10 10'
                },
                this.linksContainer
            ],
            height: this.height
        });
        return this.panel;
    },
    _createLinksPanel: function (data) {
        var chromosome = data.chromosome;
        data = data.clinvarSet.referenceClinVarAssertion;
        var measure = data.measureSet.measure;
        var linksTable  = '<div class="row"><div class="col-md-8"><table class="table ocb-attributes-table">'
        linksTable += '<tr><td class="header">ID</td><td class="header">DB</td><td class="header">Type</td><td class="header">Status</td></tr>'
        _.each(_.keys(measure), function(key){
            var xref = this[key].xref;
            if(xref){
                _.each(_.keys(xref), function(key){
                    var id = this[key].id;
                    if(this[key].type == 'rs'){
                        id = '<a href="http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&type=rs&rs='+id+'" target="_blank">rs'+id+'</a>'
                    }else if(this[key].db == 'OMIM'){
                        var OMIMId = id.split('.');
                        id = '<a href="http://www.omim.org/entry/'+OMIMId[0]+'#'+OMIMId[1]+'" target="_blank">'+id+'</a>'
                    }
                    linksTable += '<tr><td>'+id+'</td><td>'+this[key].db+'</td><td>'+this[key].type+'</td><td>'+this[key].status+'</td></tr>'
                },xref);
            }
        },measure);

        linksTable += '</table></div></div>'

        var lovd_link = 'http://databases.ebi.lovd.nl/shared/variants#order=VariantOnGenome%2FDNA%2CASC&skip[allele_]=allele_&skip[screeningids]=screeningids&skip[created_by]=created_by&skip[created_date]=created_date&search_chromosome='+chromosome+'&page_size=100&page=1';

        linksTable += '<br /><div><a href="'+lovd_link+'" target="_blank">LOVD</a></div>'




        var linksPanel = Ext.create('Ext.panel.Panel', {
//            title: data.clinVarAccession.acc,
            border: false,
            layout: 'vbox',
            overflowX: true,
            items: [  {
                xtype: 'container',
                data: data,
                width:970,
                tpl: new Ext.XTemplate(linksTable),
                margin: '10 5 5 10'
            }]
        });

        return linksPanel;
    }
};
