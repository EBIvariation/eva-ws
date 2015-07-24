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
        var tpl = new Ext.XTemplate(['<div   class="variant-browser-option-div form-panel-clinical-filter-div"></div><div class="variant-browser-option-div clinical-widget-div"></div>']);
        var view = Ext.create('Ext.view.View', {
            tpl: tpl
        });

        this.panel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'vbox',
                align: 'left'
            },
            border:false,
            overflowY:'auto',
            items: [view],
//            height:1200,
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
//                title: 'Variant Browser <span class="assembly">Assembly:GRCh37</span>',
                title: 'ClinVar Browser',
                border: true
            },
            toolPanelConfig: {
                title: 'ClinVar Data',
                headerConfig: {
                    baseCls: 'eva-header-2'
                }
            },
            defaultToolConfig: {
                headerConfig: {
                    baseCls: 'eva-header-2'
                },
                assertion: true

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

            }
        });

        return evaClinVarWidget;
    },
    _createFormPanelVariantFilter: function (target) {
        var _this = this;
        var clinvarPositionFilter = new ClinVarPositionFilterFormPanel({
//            testRegion: '1:14000-200000',
//            testRegion: '3:550000-1166666',
            testRegion: '2:48000000-49000000',
            emptyText: ''

        });


        var clinvarSpeciesFilter = new SpeciesFilterFormPanel({
            defaultValue:'hsapiens_grch37',
            speciesList:clinVarSpeciesList
        });

        var phenotypeFilter = new ClinVarTraitFilterFormPanel({
            collapsed: false,
            defaultValue:'lynch syndrome'
        });

        clinvarSpeciesFilter.on('species:change', function (e) {
           clinvarSelectedSpecies = e.species;
        });

        var clinVarConsequenceTypes = consequenceTypes;
        clinVarConsequenceTypes[0].children[0].children[4].checked = true;

        var clinvarConseqTypeFilter = new EvaConsequenceTypeFilterFormPanel({
            consequenceTypes: clinVarConsequenceTypes,
            filterType:'clinVar',
            collapsed: false,
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

        var variationType = [
            {
                name:'Deletion',
                cls: "parent",
                value:'Deletion',
                leaf: true,
                checked:true,
                iconCls :'no-icon'
            },
            {
                name:'Duplication',
                cls: "parent",
                leaf: true,
                checked:false,
                value:'Duplication',
                iconCls :'no-icon'
            },
            {
                name:'Indel',
                cls: "parent",
                value:'Indel',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Insertion',
                cls: "parent",
                value:'Insertion',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Single Nucleotide',
                cls: "parent",
                value:'single_nucleotide',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            }];

        var variationTypeFilter = new EvaClinVarFilterFormPanel({
            data: _.sortBy(variationType, 'name'),
            filterType:'type',
            title:'Variation Type',
            height:200,
            collapsed: false,
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

        var reviewStatusType = [
            {
                name:'Professional society',
                cls: "parent",
                value:'REVIEWED_BY_PROFESSIONAL_SOCIETY',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Expert panel',
                cls: "parent",
                leaf: true,
                checked:true,
                value:'REVIEWED_BY_EXPERT_PANEL',
                iconCls :'no-icon'
            },
            {
                name:'Multiple submitters',
                cls: "parent",
                value:'CLASSIFIED_BY_MULTIPLE_SUBMITTERS',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Single submitter',
                cls: "parent",
                value:'CLASSIFIED_BY_SINGLE_SUBMITTER',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            }];

        var reviewStatusFilter = new EvaClinVarFilterFormPanel({
            data: _.sortBy(reviewStatusType, 'name'),
            filterType:'review',
            title:'Review Status',
            height:200,
            collapsed: false,
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

        var clinicalSignfcType = [
            {
                name:'Confers Sensitivity',
                cls: "parent",
                value:'confers_sensitivity',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Benign',
                cls: "parent",
                leaf: true,
                checked:false,
                value:'Benign',
                iconCls :'no-icon'
            },
            {
                name:'Protective',
                cls: "parent",
                leaf: true,
                checked:false,
                value:'protective',
                iconCls :'no-icon'
            },
            {
                name:'Association',
                cls: "parent",
                leaf: true,
                checked:false,
                value:'association',
                iconCls :'no-icon'
            },
            {
                name:'Likely benign',
                cls: "parent",
                value:'Likely_benign',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Uncertain significance',
                cls: "parent",
                value:'Uncertain_significance',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Likely pathogenic',
                cls: "parent",
                value:'Likely_pathogenic',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Pathogenic',
                cls: "parent",
                value:'Pathogenic',
                leaf: true,
                checked:true,
                iconCls :'no-icon'
            },
            {
                name:'Drug Response',
                cls: "parent",
                value:'drug_response',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            },
            {
                name:'Risk factor',
                cls: "parent",
                value:'risk_factor',
                leaf: true,
                checked:false,
                iconCls :'no-icon'
            }];

        var  clinicalSignfcFilter = new EvaClinVarFilterFormPanel({
            data:_.sortBy(clinicalSignfcType, 'name'),
            filterType:'significance',
            title:'Clinical Significance',
            height:320,
            collapsed: false,
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


        var formPanel = new EvaFormPanel({
            title: 'Filter',
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            mode: 'accordion',
            target: target,
            submitButtonText: 'Submit',
            filters: [clinvarPositionFilter,clinvarConseqTypeFilter,phenotypeFilter,variationTypeFilter,clinicalSignfcFilter,reviewStatusFilter],
            width: 300,
            height: 1400,
            border: false,
            handlers: {
                'submit': function (e) {
                    console.log(e.values)
                    _this.clinvarWidget.clinvarBrowserGrid.setLoading(true);
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
//                        CellBaseManager.get({
//                            host:CELLBASE_HOST,
//                            version:CELLBASE_VERSION,
//                            species: 'hsapiens',
//                            category: 'feature',
//                            subCategory: 'gene',
//                            query: e.values.gene.toUpperCase(),
//                            resource: "info",
//                            async: false,
//                            params: {
//                                include: 'chromosome,start,end'
//                            },
//                            success: function (data) {
//                                for (var i = 0; i < data.response.length; i++) {
//                                    var queryResult = data.response[i];
//                                    if(!_.isEmpty(queryResult.result[0])){
//                                        var region = new Region(queryResult.result[0]);
//                                        regions.push(region.toString());
//                                    }
//                                }
//                            }
//                        });
//                        delete  e.values.gene;

                    }




                    //CONSEQUENCE TYPES CHECK
                    if (typeof e.values['annot-ct'] !== 'undefined') {
                        if (e.values['annot-ct'] instanceof Array) {
                            e.values['so'] = e.values['annot-ct'].join(",");
                        }
                        delete  e.values['annot-ct'];
                    }

                    if (typeof e.values.accessionId !== 'undefined') {
                        e.values['rcv'] = e.values.accessionId;
                        delete  e.values['accessionId'];
                    }


                    if (regions.length > 0) {
                       e.values['region'] = regions.join(',');
//                         e.values['region'] = _.last(regions);
//                        regions = _.last(regions);
                    }


                    var params = _.extend(e.values,{merge:true,source:'clinvar',species:'hsapiens_grch37'});

                    _this.clinvarWidget.formValues = e.values;

                    var url = EvaManager.url({
                        host:CELLBASE_HOST,
                        version:CELLBASE_VERSION,
                        category: 'hsapiens/feature/clinical',
                        resource: 'all',
//                        query: regions,
                        params:params
                    });


//                    _this.clinvarWidget.clinvarBrowserGrid.setLoading(true);
////                    _this.clinvarWidget.clinvarBrowserGrid.load();
//                    EvaManager.get({
//                        host:CELLBASE_HOST,
//                        version:CELLBASE_VERSION,
//                        category: 'hsapiens/feature',
//                        resource: 'all',
//                        query:'clinical',
//                        params:params,
//                        success: function (response) {
//                            try {
//                                var data = response.response[0].result;
//                                _this.clinvarWidget.clinvarBrowserGrid.load(data);
//                                _this.clinvarWidget.clinvarBrowserGrid.setLoading(false);
//                            } catch (e) {
//                                console.log(e);
//                            }
//                        }
//                    });



//                    if (typeof e.values.accessionId !== 'undefined') {
//                        regions = e.values.accessionId;
//                         url = EvaManager.url({
//                            host:CELLBASE_HOST,
//                            version:CELLBASE_VERSION,
//                            category: 'hsapiens/feature/clinvar',
//                            resource: 'info',
//                            query: regions,
//                            params:{merge:true}
//                        });
//                    }

                    _this.clinvarWidget.retrieveData(url,e.values)

//                     var geneColumn = Ext.getCmp('clinvar-grid-gene-column');
//                     var viewColumn = Ext.getCmp('clinvar-grid-view-column');
//                     viewColumn.tpl =  Ext.create('Ext.XTemplate', '<tpl><a href="?Genome Browser&position='+ regions+'" target="_blank">Genome Viewer</a></tpl>')
//                    if(!_.isUndefined(gene)){
//                        var updateTpl = Ext.create('Ext.XTemplate', '<tpl>'+gene+'</tpl>');
//                        geneColumn.tpl = updateTpl;
//                        geneColumn.setVisible(true);
//                    }else{
//                        geneColumn.setVisible(false);
//                    }


                }
            }
        });




        return formPanel;
    }


};

