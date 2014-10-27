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

function EvaVariantWidgetPanel(args) {
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


EvaVariantWidgetPanel.prototype = {
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
        this.variantPanelDiv = document.querySelector('#variant-widget');

        this.formPanelVariantFilterDiv = document.createElement('div');
        $(this.formPanelVariantFilterDiv).addClass('form-panel-variant-filter-div');
        this.variantPanelDiv.appendChild(this.formPanelVariantFilterDiv);
        this.formPanelVariantFilter = this._createFormPanelVariantFilter(this.formPanelVariantFilterDiv);
        this.formPanelVariantFilter.draw();

        this.variantWidgetDiv = document.createElement('div');
        $(this.variantWidgetDiv).addClass('variant-widget-div');
        this.variantPanelDiv.appendChild(this.variantWidgetDiv);
        this.variantWidget = this._createVariantWidget(this.variantWidgetDiv);
        this.variantWidget.draw();
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
        var tpl = new Ext.XTemplate(['<div  id="variant-widget" class="variant-browser-option-div"></div>']);
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
            cls: 'variant-widget-panel'
        });


        return  this.panel;
    },
    _createVariantWidget: function (target) {
//        var width = this.width - parseInt(this.div.style.paddingLeft) - parseInt(this.div.style.paddingRight);
        var evaVariantWidget = new EvaVariantWidget({
            width: 1020,
            target: target,
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            border: true,
            browserGridConfig: {
//                title: 'VCF Browser <span class="assembly">Assembly:GRCh37</span>',
                title: 'VCF Browser',
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
        }); //the div must exist

        return evaVariantWidget;
    },
    _createFormPanelVariantFilter: function (target) {
        var _this = this;
        var positionFilter = new PositionFilterFormPanel({
            testRegion: '1:14000-200000',
            emptyText: ''

        });


        this.studiesStore = Ext.create('Ext.data.Store', {
            pageSize: 50,
            proxy: {
                type: 'memory'
            },
            fields: [
                {name: 'studyName', type: 'string'},
                {name: 'studyId', type: 'string'}
            ],
            autoLoad: false
        });

        var studyFilter = new StudyFilterFormPanel({
            collapsed: false,
            height: 550,
            studiesStore: this.studiesStore,
            studyFilterTpl:'<tpl><div class="ocb-study-filter"><a href="?eva-study={studyId}" target="_blank">{studyName}</a> (<a href="http://www.ebi.ac.uk/ena/data/view/{studyId}" target="_blank">{studyId}</a>) </div></tpl>'
        });
        this.on('studies:change', function (e) {
//            studyFilter.setStudies(e.studies);
        });

        _this._loadListStudies(studyFilter);

        var conseqType = new ConsequenceTypeFilterFormPanel({
            consequenceTypes: consequenceTypes,
            collapsed: true,
            fields: [
                {name: 'name', type: 'string'}
            ],
            columns: [
                {
                    xtype: 'treecolumn',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'name'
                }
            ]
        });

        var formPanel = new FormPanel({
            title: 'Filter',
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            mode: 'accordion',
            target: target,
            submitButtonText: 'Submit',
            filters: [positionFilter, studyFilter],
            width: 300,
//            height: 1043,
            border: false,
            handlers: {
                'submit': function (e) {
                    console.log(e.values);
                    _this.variantWidget.setLoading(true);

                    //POSITION CHECK
                    var regions = [];
                    if (typeof e.values.region !== 'undefined') {
                        if (e.values.region !== "") {
                            regions = e.values.region.split(",");
                        }
                        delete  e.values.region;
                    }

                    if (typeof e.values.gene !== 'undefined') {
                        CellBaseManager.get({
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

                    if (typeof e.values.snp !== 'undefined') {
                        CellBaseManager.get({
                            species: 'hsapiens',
                            category: 'feature',
                            subCategory: 'snp',
                            query: e.values.snp,
                            resource: "info",
                            async: false,
                            params: {
                                include: 'chromosome,start,end'
                            },
                            success: function (data) {
                                for (var i = 0; i < data.response.length; i++) {
                                    var queryResult = data.response[i];
                                    var region = new Region(queryResult.result[0]);
                                    var fields2 = (""+region).split(/[:-]/);
                                    if(parseInt(fields2[1]) > parseInt(fields2[2])) {
                                        var swap = fields2[1];
                                        region.start = fields2[2];
                                        region.end = swap;
                                    }
                                    regions.push(region.toString());
                                }
                            }
                        });
                        delete  e.values.snp;
                    }


                    //CONSEQUENCE TYPES CHECK
                    if (typeof e.values.ct !== 'undefined') {
                        if (e.values.ct instanceof Array) {
                            e.values.ct = e.values.ct.join(",");
                        }
                    }


                    if (regions.length > 0) {
                        e.values['region'] = regions.join(',');
                    }

                    var url = EvaManager.url({
                        category: 'segments',
                        resource: 'variants',
                        query: regions,
                        params:{merge:true}
                    });

                    if(e.values.studies){
                        e.values.studies = e.values.studies.join(',');
                    }

                    _this.variantWidget.retrieveData(url, e.values)
                }
            }
        });


        formPanel.panel.addDocked({
                                    xtype: 'toolbar',
                                    dock: 'top',
                                    height: 45,
                                    html: '<h5>Assembly: GRCh37</h5>',
                                    margin:-10
                                });

        return formPanel;
    },
    _loadListStudies: function (filter) {
        var _this = this;
        var studies = [];
        EvaManager.get({
            category: 'meta/studies',
            resource: 'list',
            success: function (response) {
                try {
                    studies = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                filter.studiesStore.loadRawData(studies);
                //set all records checked default
                filter.studiesStore.each(function(rec){
                    rec.set('uiactive', true)
                })
                _this.trigger('studies:change', {studies: studies, sender: _this});
            }
        });
    }

};

