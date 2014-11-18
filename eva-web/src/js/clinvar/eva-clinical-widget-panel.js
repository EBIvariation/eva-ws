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

function EvaClinicalWidgetPanel(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.id = Utils.genId("VariantWidgetPanel");

    this.target;
    this.tools = [];
    _.extend(this, args);
    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};


EvaClinicalWidgetPanel.prototype = {
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
            console.log('EVAVariantWidgetPanel target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.panel.render(this.div);
        this.formPanelVariantFilterDiv = document.querySelector('.form-panel-clinical-filter-div');
        this.formPanelVariantFilter = this._createFormPanelVariantFilter(this.formPanelVariantFilterDiv);
        this.formPanelVariantFilter.draw();

        this.clinvarWidgetDiv = document.querySelector('.clinical-widget-div');
        this.clinvarWidget = this._createClinVarWidget(this.clinvarWidgetDiv);
        this.clinvarWidget.draw();
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
        var tpl = new Ext.XTemplate(['<div  id="variant-widget1" class="variant-browser-option-div form-panel-clinical-filter-div"></div><div  id="variant-widget1" class="variant-browser-option-div clinical-widget-div"></div>']);
        var view = Ext.create('Ext.view.View', {
            tpl: tpl
        });

        this.panel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'vbox',
                align: 'center',
            },
            overflowX:'auto',
            items: [view],
            height:1200,
            cls: 'variant-widget-panel'
        });


        return  this.panel;
    },
    _createClinVarWidget: function (target) {
//        var width = this.width - parseInt(this.div.style.paddingLeft) - parseInt(this.div.style.paddingRight);
        var evaClinVarWidget = new EvaClinVarWidget({
            width: 1020,
            target: target,
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            border: true,
            browserGridConfig: {
//                title: 'VCF Browser <span class="assembly">Assembly:GRCh37</span>',
                title: 'ClinVar Browser',
                border: true
            },
            toolPanelConfig: {
                title: 'Variant Data',
                headerConfig: {
                    baseCls: 'eva-header-2'
                }
            },
            defaultToolConfig: {
                headerConfig: {
                    baseCls: 'eva-header-2'
                },
                genomeViewer: true,
                effect:false
            },
            responseParser: function (response) {
                var res = [];
                try {
                    res = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                return  res;
            },
            dataParser: function (data) {
                for (var i = 0; i < data.length; i++) {
                    var variant = data[i];
                    if (variant.hgvs && variant.hgvs.genomic > 0) {
                        variant.hgvs_name = variant.hgvs.genomic[0];
                    }
                }
            }
        });

        return evaClinVarWidget;
    },
    _createFormPanelVariantFilter: function (target) {
        var _this = this;
        var positionFilter = new ClinVarPositionFilterFormPanel({
//            testRegion: '1:14000-200000',
            testRegion: '3:550000-1166666',
            emptyText: ''

        });



        var formPanel = new FormPanel({
            title: 'Filter',
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            mode: 'accordion',
            target: target,
            submitButtonText: 'Submit',
            filters: [positionFilter],
            width: 300,
//            height: 1043,
            border: false,
            handlers: {
                'submit': function (e) {
//                    _this.clinvarWidget.setLoading(true);
                    //POSITION CHECK
                    var regions = [];
                    if (typeof e.values.region !== 'undefined') {
                        if (e.values.region !== "") {
                            regions = e.values.region.split(",");
                        }
                        delete  e.values.region;
                    }
                    var gene = e.values.gene;
                    if (typeof e.values.gene !== 'undefined') {
                        CellBaseManager.get({
                            host:'http://wwwdev.ebi.ac.uk/cellbase/webservices/rest',
                            species: 'hsapiens',
                            category: 'feature',
                            subCategory: 'gene',
                            query: e.values.gene.toUpperCase(),
                            resource: "info",
                            async: false,
                            params: {
                                include: 'chromosome,start,end'
                            },
                            success: function (data) {
                                for (var i = 0; i < data.response.length; i++) {
                                    var queryResult = data.response[i];
                                    if(!_.isEmpty(queryResult.result[0])){
                                        var region = new Region(queryResult.result[0]);
                                        regions.push(region.toString());
                                    }
                                }
                            }
                        });
                        delete  e.values.gene;
                    }




                    //CONSEQUENCE TYPES CHECK
                    if (typeof e.values.ct !== 'undefined') {
                        if (e.values.ct instanceof Array) {
                            e.values.ct = e.values.ct.join(",");
                        }
                    }


                    if (regions.length > 0) {
//                       e.values['region'] = regions.join(',');
                         e.values['region'] = _.last(regions);
                        regions = _.last(regions);
                    }



                    var url = EvaManager.url({
                        host:'http://wwwdev.ebi.ac.uk/cellbase/webservices/rest',
                        version:'v3',
                        category: 'hsapiens/genomic/region',
                        resource: 'clinvar',
                        query: regions,
                        params:{merge:true}
                    });

                    if (typeof e.values.accessionId !== 'undefined') {
                        regions = e.values.accessionId;
                         url = EvaManager.url({
                            host:'http://wwwdev.ebi.ac.uk/cellbase/webservices/rest',
                            version:'v3',
                            category: 'hsapiens/feature/clinvar',
                            resource: 'info',
                            query: regions,
                            params:{merge:true}
                        });
                    }

                    _this.clinvarWidget.retrieveData(url, e.values)
                     var geneColumn = Ext.getCmp('clinvar-grid-gene-column');
                     var viewColumn = Ext.getCmp('clinvar-grid-view-column');
                     viewColumn.tpl =  Ext.create('Ext.XTemplate', '<tpl><a href="?Genome Browser&position='+ regions+'" target="_blank">Genome Viewer</a></tpl>')
                    if(!_.isUndefined(gene)){
                        var updateTpl = Ext.create('Ext.XTemplate', '<tpl>'+gene+'</tpl>');
                        geneColumn.tpl = updateTpl;
                        geneColumn.setVisible(true);
                    }else{
                        geneColumn.setVisible(false);
                    }

                }
            }
        });




        return formPanel;
    }


};

