function EvaVariantWidgetPanel(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.id = Utils.genId("StudyBrowserPanel");

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
            console.log('target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.panel.render(this.div);
        this.variantPanelDiv = document.querySelector('#variant-widget-panel');

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
        var tpl = new Ext.XTemplate(['<div  id="variant-widget-panel" class="variant-browser-option-div"></div>']);
        var view = Ext.create('Ext.view.View', {
            tpl: tpl
        });

        this.panel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'vbox',
                align: 'center',
            },
            items: [view]
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
                title: 'EVA Browser',
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
                genomeViewer: false,
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
            collapsed: true,
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
                                    var region = new Region(queryResult.result[0]);
                                    regions.push(region.toString());
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
                        host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
                        category: 'segments',
                        resource: 'variants',
                        query: regions
                    });
                    _this.variantWidget.retrieveData(url, e.values)
                }
            }
        });

        return formPanel;
    },
    _loadListStudies: function (filter) {
        var _this = this;
        var studies = [];
        EvaManager.get({
            host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
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
                console.log('dfv')
                _this.trigger('studies:change', {studies: studies, sender: _this});
            }
        });
    }



};

