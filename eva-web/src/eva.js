/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
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
function Eva(args) {
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("Eva");
    this.target;
    this.targetMenu;
    this.autoRender = true;

    //set instantiation args, must be last
    _.extend(this, args);

    this.on(this.handlers);

    this.childDivMenuMap = {};


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


    this.rendered = false;
    if (this.autoRender) {
        this.render(this.targetId);
    }
}

Eva.prototype = {
    render: function () {
        var _this = this;
        console.log("Initializing");

        //HTML skel
        this.div = document.createElement('div');
        this.div.setAttribute('class', 'eva-app');
        this.div.setAttribute('id', this.id);

        this.targetMenuUl = (this.targetMenu instanceof HTMLElement ) ? this.targetMenu : document.querySelector('#' + this.targetMenu);
        this.evaMenu = this._createEvaMenu(this.targetMenuUl);


        /* Home */
        $(this.homeDiv).addClass('eva-child');
        this.childDivMenuMap['Home'] = this.homeDiv;

        /* Submit */
        $(this.submitDiv).addClass('eva-child');
        this.childDivMenuMap['Submit Data'] = this.submitDiv;

        /* About */
        $(this.aboutDiv).addClass('eva-child');
        this.childDivMenuMap['About'] = this.aboutDiv;

        /* Contact */
        $(this.contactDiv).addClass('eva-child');
        this.childDivMenuMap['Contact'] = this.contactDiv;

        /* Study Browser Panel*/
        this.variantStudyBrowserDiv = document.createElement('div')
        $(this.variantStudyBrowserDiv).addClass('eva-child variant-study-browser-div');
        this.div.appendChild(this.variantStudyBrowserDiv);
        this.childDivMenuMap['Study Browser'] = this.variantStudyBrowserDiv;
        this.variantStudyBrowser = this._createVariantStudyBrowser(this.variantStudyBrowserDiv);

        /* variant browser option*/
        this.variantBrowserOptionDiv = document.createElement('div');
        $(this.variantBrowserOptionDiv).addClass('eva-child variant-browser-option-div');
        this.div.appendChild(this.variantBrowserOptionDiv);
        this.childDivMenuMap['Variant Browser'] = this.variantBrowserOptionDiv;

        this.formPanelVariantFilterDiv = document.createElement('div');
        $(this.formPanelVariantFilterDiv).addClass('form-panel-variant-filter-div');
        this.variantBrowserOptionDiv.appendChild(this.formPanelVariantFilterDiv);

        this.variantWidgetDiv = document.createElement('div');
        $(this.variantWidgetDiv).addClass('variant-widget-div');
        this.variantBrowserOptionDiv.appendChild(this.variantWidgetDiv);

        this.formPanelVariantFilter = this._createFormPanelVariantFilter(this.formPanelVariantFilterDiv);
        this.variantWidget = this._createVariantWidget(this.variantWidgetDiv);

        /* genome browser option*/
        this.genomeBrowserOptionDiv = document.createElement('div');
        $(this.genomeBrowserOptionDiv).addClass('eva-child genome-browser-option-div');
        this.div.appendChild(this.genomeBrowserOptionDiv);
        this.childDivMenuMap['Genome Browser'] = this.genomeBrowserOptionDiv;

        this.formPanelGenomeFilterDiv = document.createElement('div');
        $(this.formPanelGenomeFilterDiv).addClass('form-panel-genome-filter-div');
        this.genomeBrowserOptionDiv.appendChild(this.formPanelGenomeFilterDiv);

        this.genomeViewerTitleDiv = document.createElement('div');
        $(this.genomeViewerTitleDiv).addClass('genome-viewer-title-div eva-header-1');
        this.genomeBrowserOptionDiv.appendChild(this.genomeViewerTitleDiv);
        this.genomeViewerTitleDiv.innerHTML = 'Genome browser';

        this.genomeViewerDiv = document.createElement('div');
        $(this.genomeViewerDiv).addClass('genome-viewer-div');
        this.genomeBrowserOptionDiv.appendChild(this.genomeViewerDiv);

        this.genomeViewer = this._createGenomeViewer(this.genomeViewerDiv);
        this.formPanelGenomeFilter = this._createFormPanelGenomeFilter(this.formPanelGenomeFilterDiv);


//        this.genomeViewer.leftSidebarDiv.appendChild(this.formPanelGenomeFilterDiv);
//        this.panel = this._createPanel();
    },
    draw: function () {
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (this.targetDiv === 'undefined') {
            console.log('target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.evaMenu.draw();
        this.variantWidget.draw();
        this.variantStudyBrowser.draw();

        this.formPanelVariantFilter.draw();
        this.genomeViewer.draw();
        this.formPanelGenomeFilter.draw();

        this._loadStudies();

//        this.select('Study Browser');
        this.select('Variant Browser');
//        this.panel.render(this.div);
    },
    select: function (option) {
        this.evaMenu.select(option);
        this._selectHandler(option);
    },
    _selectHandler: function (option) {
        var _this = this;
        $('body').find('.eva-child').each(function (index, el) {
            _this.div.removeChild(el)
        });
        if (this.childDivMenuMap[option]) {
            this.div.appendChild(this.childDivMenuMap[option]);
        }

        switch (option) {
            case 'Study Browser':
                this.variantStudyBrowser.update();
                break;
            case 'Variant Browser':
                this.formPanelVariantFilter.update();
                break;
            case 'Genome Browser':
                this.formPanelGenomeFilter.update();
                break;
        }
        this.studiesStore.clearFilter();

    },
    _createEvaMenu: function (target) {
        var _this = this;
        var evaMenu = new EvaMenu({
            target: target,
            handlers: {
                'menu:click': function (e) {
                    _this._selectHandler(e.option);
                }
            }
        });
        return evaMenu;
    },
    _createVariantWidget: function (target) {
//        var width = this.width - parseInt(this.div.style.paddingLeft) - parseInt(this.div.style.paddingRight);

        var columns = [
            {
                text: "SNP Id",
                dataIndex: 'id'
            },
            {
                text: "Chromosome",
                dataIndex: 'chromosome'
            },
            {
                text: 'Position',
                dataIndex: 'start'
            },
            //{
            //text: 'End',
            //dataIndex: 'end'
            //},
            {
                text: 'Aleles',
                xtype: "templatecolumn",
                tpl: "{reference}>{alternate}"
            },
            {
                text: 'Class',
                dataIndex: 'type'
            },
            {
                text: '1000G MAF',
                dataIndex: ''
            },
            {
                text: 'Consequence Type',
                dataIndex: 'ct'
            },
            {
                text: 'Gene',
                dataIndex: 'gene'
            },
            {
                text: 'HGVS Names',
                dataIndex: 'hgvs_name'
            },
            {
                text: 'View',
                //dataIndex: 'id',
                xtype: 'templatecolumn',
                tpl: '<tpl if="id"><a href="?variantID={id}" target="_blank"><img class="eva-grid-img" src="img/eva_logo.png"/></a>&nbsp;' +
                    '<a href="http://www.ensembl.org/Homo_sapiens/Variation/Explore?vdb=variation;v={id}" target="_blank"><img alt="" src="http://static.ensembl.org/i/search/ensembl.gif"></a>' +
                    '&nbsp;<a href="http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&type=rs&rs={id}" target="_blank"><span>dbSNP</span></a>' +
                    '<tpl else><a href="?variantID={chromosome}:{start}:{ref}:{alt}" target="_blank"><img class="eva-grid-img" src="img/eva_logo.png"/></a>&nbsp;<img alt="" class="in-active" src="http://static.ensembl.org/i/search/ensembl.gif">&nbsp;<span  style="opacity:0.2" class="in-active">dbSNP</span></tpl>'
            }

            //
        ];

        var attributes = [
            {name: 'id', type: 'string'},
            {name: "chromosome", type: "string"},
            {name: "start", type: "int"},
            {name: "end", type: "int"},
            {name: "type", type: "string"},
            {name: "ref", type: "string"},
            {name: "alt", type: "string"},
            {name: 'hgvs_name', type: 'string'}
        ];

        var variantWidget = new VariantWidget({
            width: 1020,
            target: target,
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            border: true,
            browserGridConfig: {
                title: 'Variant Browser',
                border: true
            },
            toolPanelConfig: {
                title: 'Variant Data'
            },
            defaultToolConfig: {
                headerConfig: {
                    baseCls: 'eva-header-2'
                }
            },
            columns: columns,
            attributes: attributes,
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

        return variantWidget;
    },
    _createVariantStudyBrowser: function (target) {
        var _this = this;

        var variantStudyBrowser = new VariantStudyBrowserPanel({
            target: target,
            title: 'Study Browser',
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            width: 1300,
            studiesStore: this.studiesStore

        });
//        this.on('studies:change', function (e) {
//            variantStudyBrowser.setStudies(e.studies);
//        });


        return variantStudyBrowser;
    },
    _createFormPanelVariantFilter: function (target) {
        var _this = this;
        var positionFilter = new PositionFilterFormPanel({
            testRegion: '1:14000-20000'
        });
        var studyFilter = new StudyFilterFormPanel({
            studiesStore: this.studiesStore
        });
//        this.on('studies:change', function (e) {
//            studyFilter.setStudies(e.studies);
//        });


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
            filters: [positionFilter, studyFilter, conseqType],
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
                            query: e.values.gene,
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
    _createFormPanelGenomeFilter: function (target) {
        var _this = this;
        var positionFilter = new PositionFilterFormPanel();
        var studyFilter = new StudyFilterFormPanel({
            studiesStore: this.studiesStore
        });
//        this.on('studies:change', function (e) {
//            studyFilter.setStudies(e.studies);
//        });

        var conseqType = new ConsequenceTypeFilterFormPanel({
            consequenceTypes: consequenceTypes,
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

        var trackNameField = Ext.create('Ext.form.field.Text', {
            xtype: 'textfield',
            emptyText: 'Track name',
            flex: 5
        });
        var formPanel = new FormPanel({
            title: 'Add tracks',
            border: false,
            headerConfig: {
                baseCls: 'eva-header-1'
            },
            submitButtonText: 'Add track',
            collapsible: false,
            titleCollapse: false,
            target: target,
            filters: [studyFilter, conseqType],
            width: 1318,
            mode: 'tabs',
            barItems: [
                trackNameField
            ],
            handlers: {
                'submit': function (e) {
                    var title = trackNameField.getValue();
                    if (title !== '') {
                        _this._addGenomeBrowserTrack(title, e.values);
                        trackNameField.setValue('');
                    }
                }
            }
        });

        return formPanel;
    },
    _addGenomeBrowserTrack: function (title, params) {
        var eva = new FeatureTrack({
            title: title,
            featureType: 'eva',
            minHistogramRegionSize: 10000,
            maxLabelRegionSize: 3000,
            height: 100,

            renderer: new FeatureRenderer({
                label: function (f) {
                    var title = f.id;
                    if (title == '') {
                        title = f.chromosome + ":" + f.start + "-" + f.end;
                    }
                    return title;
                },
                tooltipTitle: function (f) {
                    var title = f.id;
                    if (title == '') {
                        title = f.chromosome + ":" + f.start + "-" + f.end;
                    }
                    return f.type + ' - <span style="color:#399697">' + title + '</span>';
                },
                tooltipText: function (f) {
                    var str = [
                            'Location: ' + '<span style="color:#156765">' + f.chromosome + ":" + f.start + "-" + f.end + '</span>',
                            'Alt. allele: ' + '<span style="color:#156765">' + f.allele + '</span>',
                            'Variant class: ' + '<span style="color:#156765">' + f.type + '</span>'
                    ];
                    var hgvs = [];
                    for (var key in f.hgvs) {
                        hgvs.push('<span style="margin-left: 10px">' + Utils.camelCase(key) + '</span>' + ': <span style="color:#156765">' + f.hgvs[key] + '</span>');
                    }
                    if (hgvs.length > 0) {
                        str.push('HGVS' + '<br><span>' + hgvs.join('<br>') + '</span>');
                    }
                    var files = [];
                    for (var key in f.files) {
                        files.push('<span style="margin-left: 10px">' + f.files[key].fileId + '</span>' + ' (<span style="color:#156765">' + f.files[key].studyId + '</span>)');
                    }
                    if (files.length > 0) {
                        str.push('Files (study)' + '<br><span>' + files.join('<br>') + '</span>');
                    }
                    return str.join('<br>');
                },
                color: function (f) {
                    return "#399697";
                },
                infoWidgetId: "id",
                height: 10,
                histogramColor: "#399697",
                handlers: {
                    'feature:mouseover': function (e) {
//                        e.feature
                        console.log(e);
                    }
                }
            }),

            dataAdapter: new EvaAdapter({
                host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
                version: 'v1',
                category: "segments",
                resource: "variants",
                params: params,
                cacheConfig: {
                    chunkSize: 10000
                }
            })
        });

        this.genomeViewer.addTrack(eva);
    },
    _createGenomeViewer: function (target) {
        var _this = this;

        var region = new Region({
            chromosome: "13",
            start: 32889611,
            end: 32889611
        });

        var genomeViewer = new GenomeViewer({
            sidePanel: false,
            target: target,
            border: false,
            resizable: true,
            width: 1328,
            region: region,
//            trackListTitle: '',
            drawNavigationBar: true,
            drawKaryotypePanel: false,
            drawChromosomePanel: false,
            drawRegionOverviewPanel: true,
            overviewZoomMultiplier: 50,
            navigationBarConfig: {
                componentsConfig: {
                    leftSideButton: true,
                    restoreDefaultRegionButton: false,
                    regionHistoryButton: false,
                    speciesButton: false,
                    chromosomesButton: false,
                    karyotypeButton: false,
                    chromosomeButton: false,
                    regionButton: false,
//                    zoomControl: false,
                    windowSizeControl: false,
//                    positionControl: false,
//                    moveControl: false,
//                    autoheightButton: false,
//                    compactButton: false,
//                    searchControl: false
                }
            }
        }); //the div must exist

        genomeViewer.navigationBar.on('leftSideButton:click', function () {
            $(_this.formPanelGenomeFilterDiv).toggle();
        });

        var renderer = new FeatureRenderer(FEATURE_TYPES.gene);
        renderer.on({
            'feature:click': function (event) {
                // feature click event example
                console.log(event)
            }
        });
        var geneOverview = new FeatureTrack({
//        title: 'Gene overview',
            minHistogramRegionSize: 20000000,
            maxLabelRegionSize: 10000000,
            height: 100,

            renderer: renderer,

            dataAdapter: new CellBaseAdapter({
                category: "genomic",
                subCategory: "region",
                resource: "gene",
                params: {
                    exclude: 'transcripts,chunkIds'
                },
                species: genomeViewer.species,
                cacheConfig: {
                    chunkSize: 100000
                }
            })
        });


        var sequence = new SequenceTrack({
//        title: 'Sequence',
            height: 30,
            visibleRegionSize: 200,

            renderer: new SequenceRenderer(),

            dataAdapter: new SequenceAdapter({
                category: "genomic",
                subCategory: "region",
                resource: "sequence",
                species: genomeViewer.species
            })
        });


        var gene = new GeneTrack({
            title: 'Gene',
            minHistogramRegionSize: 20000000,
            maxLabelRegionSize: 10000000,
            minTranscriptRegionSize: 200000,
            height: 140,

            renderer: new GeneRenderer(),

            dataAdapter: new CellBaseAdapter({
                category: "genomic",
                subCategory: "region",
                resource: "gene",
                species: genomeViewer.species,
                params: {
                    exclude: 'transcripts.tfbs,transcripts.xrefs,transcripts.exons.sequence'
                },
                cacheConfig: {
                    chunkSize: 100000
                }
            })
        });

        var snp = new FeatureTrack({
            title: 'SNP',
            featureType: 'SNP',
            minHistogramRegionSize: 10000,
            maxLabelRegionSize: 3000,
            height: 100,

            renderer: new FeatureRenderer(FEATURE_TYPES.snp),

            dataAdapter: new CellBaseAdapter({
                category: "genomic",
                subCategory: "region",
                resource: "snp",
                params: {
                    exclude: 'transcriptVariations,xrefs,samples'
                },
                species: genomeViewer.species,
                cacheConfig: {
                    chunkSize: 10000
                }
            })
        });

        genomeViewer.addOverviewTrack(geneOverview);
        genomeViewer.addTrack([sequence, gene, snp]);

        return genomeViewer;
    },
    _loadStudies: function () {
        var _this = this;
        var studies = [];
        EvaManager.get({
            host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
            category: 'studies',
            resource: 'list',
            success: function (response) {
                try {
                    studies = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                _this.studiesStore.loadRawData(studies);
//                _this.trigger('studies:change', {studies: studies, sender: _this});
            }
        });
    }
}
