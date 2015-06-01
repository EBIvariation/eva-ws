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

        this.variantWidgetDiv = document.querySelector('.variant-widget-div');
        this.variantWidget = this._createVariantWidget(this.variantWidgetDiv);
        this.variantWidget.draw();

        this.formPanelVariantFilterDiv = document.querySelector('.form-panel-variant-filter-div');
        this.formPanelVariantFilter = this._createFormPanelVariantFilter(this.formPanelVariantFilterDiv);
        this.formPanelVariantFilter.draw();
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
        var tpl = new Ext.XTemplate(['<div class="variant-browser-option-div form-panel-variant-filter-div"></div><div class="variant-browser-option-div variant-widget-div"></div>']);
        var view = Ext.create('Ext.view.View', {
            tpl: tpl
        });

        this.panel = Ext.create('Ext.panel.Panel', {
            overflowX:true,
            border:false,
            layout: {
                type: 'vbox',
                align: 'left'
            },
            items: [view],
//            height:1200,
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
//                title: 'Variant Browser <span class="assembly">Assembly:GRCh37</span>',
                title: 'Variant Browser',
                border: true
            },
            toolPanelConfig: {
                title: 'Variant Data',
                headerConfig: {
                    baseCls: 'eva-header-2'
                },
                border:false
            },
            defaultToolConfig: {
                headerConfig: {
                    baseCls: 'eva-header-2'
                },
                genomeViewer: true,
                effect:false,
                rawData:false,
                populationStats:true
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
        console.log(_this.position)
        var positionFilter = new EvaPositionFilterFormPanel({//
            testRegion: _this.position,
            emptyText: ''

        });


        var speciesFilter = new SpeciesFilterFormPanel({
            defaultValue: _this.species
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

        var studyFilter = new EvaStudyFilterFormPanel({
            border:false,
            collapsed: false,
            height: 790,
            studiesStore: this.studiesStore,
            studyFilterTpl:'<tpl if="studyId"><div class="ocb-study-filter"><a href="?eva-study={studyId}" target="_blank">{studyName}</a> (<a href="http://www.ebi.ac.uk/ena/data/view/{studyId}" target="_blank">{studyId}</a>) </div><tpl else><div class="ocb-study-filter"><a href="?eva-study={studyId}" target="_blank">{studyName}</a></div></tpl>'
        });

//        _this._loadListStudies(studyFilter, '');

        studyFilter.on('studies:change', function (e) {
            var studies = _this.formPanelVariantFilter.getValues().studies;
            var submitButton = Ext.getCmp(_this.formPanelVariantFilter.submitButtonId);
            if(_.isUndefined(studies)){
                Ext.Msg.alert('','Please Select at least one study');
                submitButton.disable();

            }else{
                submitButton.enable();

            }

        });


        speciesFilter.on('species:change', function (e) {
            _this._loadListStudies(studyFilter, e.species);
            var plantSpecies = ['slycopersicum_sl240'];

            if(e.species =='hsapiens_grch37' || e.species =='hsapiens_grch38'){
                _this.variantWidget.variantBrowserGrid.grid.getView().getHeaderAtIndex(2).setText('dbSNP ID')
            }else if(_.indexOf(plantSpecies, e.species) > -1){
                _this.variantWidget.variantBrowserGrid.grid.getView().getHeaderAtIndex(2).setText('Transplant ID')
            }else{
                _this.variantWidget.variantBrowserGrid.grid.getView().getHeaderAtIndex(2).setText('Submitted ID')
            }

            EvaManager.get({
                category: 'meta/studies',
                resource: 'list',
                params:{species:e.species},
                success: function (response) {
                    try {
                         projects = response.response[0].result;
                    } catch (e) {
                        console.log(e);
                    }
                }
            });

        });

        //<!-------To be removed------>
        consequenceTypes[0].children[0].children[4].checked = false;

        var conseqTypeFilter = new EvaConsequenceTypeFilterFormPanel({
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
        var populationFrequencyFilter = new EvaPopulationFrequencyFilterFormPanel({
            collapsed:true
        });
        var proteinSubScoreFilter = new EvaProteinSubstitutionScoreFilterFormPanel({
            collapsed:true
        });
        var conservationScoreFilter = new EvaConservationScoreFilterFormPanel({
            collapsed:true
        });



        var formPanel = new FormPanel({
            title: 'Filter',
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            mode: 'accordion',
            target: target,
            submitButtonText: 'Submit',
            submitButtonId: 'vb-submit-button',
            filters: [speciesFilter,positionFilter, conseqTypeFilter,proteinSubScoreFilter,conservationScoreFilter,studyFilter],
//            filters: [speciesFilter,positionFilter,studyFilter],
            width: 300,
            height: 1443,
            border: false,
            handlers: {
                'submit': function (e) {
                    _this.variantWidget.setLoading(true);
                    //POSITION CHECK
                    var regions = [];
                    if (typeof e.values.region !== 'undefined') {
                        if (e.values.region !== "") {
                            regions = e.values.region.split(",");
                        }
                        delete  e.values.region;
                    }
                    if(!_.isUndefined(e.values.species)){
                        var cellBaseSpecies = e.values.species.split("_")[0];
                    }

                    if (typeof e.values.gene !== 'undefined') {
                        CellBaseManager.get({
                            species: cellBaseSpecies,
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
//                        e.values.gene = e.values.gene;
                    }

                    if (typeof e.values.snp !== 'undefined') {
                        CellBaseManager.get({
                            species: cellBaseSpecies,
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
//                        e.values.id = e.values.snp;
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


                    var category = 'segments';
                    var query = regions;
                    //<!--------Query by GENE ----->
//                    if(e.values.gene){
//                        category = 'genes';
//                        query =  e.values.gene;
//                    }

                    var url = EvaManager.url({
                        category: category,
                        resource: 'variants',
                        query: query,
//                        params:{merge:true}
//                        params:{merge:true,exclude:'files'}
                        params:{merge:true,exclude:'sourceEntries'}
                    });

                    if(!_.isEmpty(e.values.studies)){
                        e.values.studies = e.values.studies.join(',');
                    }

                    var limitExceeds = false;
                    _.each(regions, function(region){
                        var start = region.split(':')[1].split('-')[0];
                        var end = region.split(':')[1].split('-')[1]
                        if(end-start > 1000000){
                            Ext.Msg.alert('Limit Exceeds','Please enter the region no more than 1000000 range');
                            limitExceeds = true;
                        }
                    });

                    if(!_.isEmpty(e.values["annot-ct"])){
                        e.values["annot-ct"] = e.values["annot-ct"].join(',');
                    }

                    if(!limitExceeds){
                        _this.variantWidget.retrieveData(url, e.values)
                    }else{
                        _this.variantWidget.retrieveData('', '')
                    }


                    _this.variantWidget.values = e.values;

                    var speciesArray = ['hsapiens','hsapiens_grch37','mmusculus_grcm38'];
                    if(e.values.species && speciesArray.indexOf( e.values.species ) > -1){
                        var ensemblSepciesName = _.findWhere(speciesList, {taxonomyCode:e.values.species.split('_')[0]}).taxonomyScientificName;
                        ensemblSepciesName =  ensemblSepciesName.split(' ')[0]+'_'+ ensemblSepciesName.split(' ')[1];
                        var ensemblURL = 'http://www.ensembl.org/'+ensemblSepciesName+'/Variation/Explore?vdb=variation;v={id}';
                        var ncbiURL = 'http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs={id}';

                        var updateTpl = Ext.create('Ext.XTemplate', '<tpl if="id"><a href="?variant={chromosome}:{start}:{reference}:{alternate}&species='+ e.values.species+'" target="_blank"><img class="eva-grid-img-active" src="img/eva_logo.png"/></a>&nbsp;' +
                            '<a href="'+ensemblURL+'" target="_blank"><img alt="" src="http://static.ensembl.org/i/search/ensembl.gif"></a>' +
                            '&nbsp;<a href="'+ncbiURL+'" target="_blank"><span>dbSNP</span></a>' +
                            '<tpl else><a href="?variant={chromosome}:{start}:{reference}:{alternate}&species='+ e.values.species+'" target="_blank"><img class="eva-grid-img-active" src="img/eva_logo.png"/></a>&nbsp;<img alt="" class="eva-grid-img-inactive " src="http://static.ensembl.org/i/search/ensembl.gif">&nbsp;<span  style="opacity:0.2" class="eva-grid-img-inactive ">dbSNP</span></tpl>');
                    }else{
                        var updateTpl = Ext.create('Ext.XTemplate', '<tpl><a href="?variant={chromosome}:{start}:{reference}:{alternate}&species='+ e.values.species+'" target="_blank"><img class="eva-grid-img-active" src="img/eva_logo.png"/></a>&nbsp;<img alt="" class="eva-grid-img-inactive " src="http://static.ensembl.org/i/search/ensembl.gif">&nbsp;<span  style="opacity:0.2" class="eva-grid-img-inactive ">dbSNP</span></tpl>');
                    }

                    Ext.getCmp('variant-grid-view-column').tpl = updateTpl;
                }
            }
        });

        _this.on('studies:change', function (e) {

            var formValues = _this.formPanelVariantFilter.getValues();
            var params = {id:positionFilter.id,species:formValues.species}
            var speciesArray = ['hsapiens','hsapiens_grch37','mmusculus_grcm38'];
            if(speciesArray.indexOf( formValues.species ) > -1){
                _.extend(params, {disable:false});
//               this._disableFields(params);
            }else{
                _.extend(params, {disable:true});
//                this._disableFields(params);
            }
            _this.variantWidget.trigger('species:change', {values: formValues, sender: _this});
            _this.formPanelVariantFilter.trigger('submit', {values: formValues, sender: _this});
        });



//        formPanel.panel.addDocked({
//                                    xtype: 'toolbar',
//                                    dock: 'top',
//                                    height: 45,
//                                    html: '<h5>Assembly: GRCh37</h5>',
//                                    margin:-10
//                                });

        return formPanel;
    },
    _loadListStudies: function (filter, species) {
        var _this = this;
//        alert(species)
        var studies = [];
        var resource = '';
        if(_.isNull(species) ){
            resource = 'all'
        }else{
            resource = 'list'
        }
        EvaManager.get({
            category: 'meta/studies',
            resource: resource,
            params:{species:species},
            success: function (response) {
                try {
                    studies = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                filter.studiesStore.loadRawData(studies);
                //set all records checked default
                filter.studiesStore.each(function(rec){
                    if(!_.isNull(species) ){
                        rec.set('uiactive', true)
                    }
                })
                _this.trigger('studies:change', {studies: studies, sender: _this});
            }
        });
    },
    _disableFields: function (params) {

        var snpIdField = params.id+'snp';
        var geneField  = params.id+'gene';
        if(params.disable){

             Ext.getCmp(snpIdField).disable();
             Ext.getCmp(snpIdField).emptyText = 'This option will be available soon for this species';
             Ext.getCmp(snpIdField).applyEmptyText();
    //             Ext.getCmp(snpIdField).hide();

            Ext.getCmp(geneField).emptyText = 'This option will be available soon for this species ';
            Ext.getCmp(geneField).applyEmptyText();
            Ext.getCmp(geneField).disable();
//             Ext.getCmp(geneField).hide();
        }else{

            if(params.species != 'hsapiens_grch37' || params.species != 'hsapiens'){
                Ext.getCmp(snpIdField).enable();
                Ext.getCmp(snpIdField).emptyText = '';
                Ext.getCmp(snpIdField).applyEmptyText();
//             Ext.getCmp(snpIdField).hide();

                Ext.getCmp(geneField).enable();
                Ext.getCmp(geneField).emptyText = ' ';
                Ext.getCmp(geneField).applyEmptyText();
            }else{
                Ext.getCmp(snpIdField).enable();
                Ext.getCmp(snpIdField).emptyText = ' ';
                Ext.getCmp(snpIdField).applyEmptyText();
//            Ext.getCmp(snpIdField).show();
                Ext.getCmp(geneField).enable();
                Ext.getCmp(geneField).emptyText = ' ';
                Ext.getCmp(geneField).applyEmptyText();
            }

//            Ext.getCmp(geneField).show();
        }

    }

};

