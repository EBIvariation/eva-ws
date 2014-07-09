//TEMPLATE Eva
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

        /* File Browser Panel*/
        this.variantFileBrowserDiv = document.createElement('div')
        $(this.variantFileBrowserDiv).addClass('eva-child variant-file-browser-div');
        this.div.appendChild(this.variantFileBrowserDiv);
        this.childDivMenuMap['File Browser'] = this.variantFileBrowserDiv;
        this.variantFileBrowser = this._createVariantFileBrowser(this.variantFileBrowserDiv);

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

        this.genomeViewerDiv = document.createElement('div');
        $(this.genomeViewerDiv).addClass('genome-viewer-div');
        this.genomeBrowserOptionDiv.appendChild(this.genomeViewerDiv);

        this.genomeViewer = this._createGenomeViewer(this.genomeViewerDiv);
        this.formPanelGenomeFilter = this._createFormPanelGenomeFilter(this.formPanelGenomeFilterDiv);
        this.genomeViewer.leftSidebarDiv.appendChild(this.formPanelGenomeFilterDiv);



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
        this.variantFileBrowser.draw();
        this.formPanelVariantFilter.draw();
        this.genomeViewer.draw();
        this.formPanelGenomeFilter.draw();

        this.select('File Browser');
//        this.panel.render(this.div);
    },
    select: function (option) {
        this.evaMenu.select(option);
        this._selectHandler(option);
    },
    _selectHandler: function (option) {
        $(this.div).children('.eva-child').each(function (index, el) {
            $(el).css({display: 'none'});
        });
        $(this.childDivMenuMap[option]).css({display: 'inherit'});
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

        var variantWidget = new VariantWidget({
            width: 1020,
            target: target,
            title: 'Variant Widget',
//            data: EXAMPLE_DATA,
            filters: {},
            defaultToolConfig: {},
            tools: [],
            dataParser: function (data) {
                for (var i = 0; i < data.length; i++) {
                    var variant = data[i];
                    variant.chromosome = variant.chr;
                    variant.alternate = variant.alt;
                    variant.reference = variant.ref;

                    if (variant.hgvs && variant.hgvs.genomic > 0) {
                        variant.hgvs_name = variant.hgvs.genomic[0];
                    }
                }
            }
        }); //the div must exist

        return variantWidget;
    },
    _createVariantFileBrowser: function(target){
        var _this = this;
        var studiesInfo = this._getStudiesInfo();

        var variantFileBrowser = new VariantFileBrowserPanel({
            target: target,
            title: 'File Browser',
            width: 1300,
            studies: studiesInfo

        });

        return variantFileBrowser;
    },
    _createFormPanelVariantFilter: function (target) {
        var _this = this;
        var positionFilter = new PositionFilterFormPanel();
        var studyFilter = new StudyFilterFormPanel({
            urlStudies: "http://wwwdev.ebi.ac.uk/eva/webservices/rest/v1/studies/list"
        });

        var conseqType = new ConsequenceTypeFilterFormPanel();

        var formPanel = new FormPanel({
            target: target,
            title: 'Filter',
            filters: [positionFilter, studyFilter, conseqType],
            width: 300,
            height: 1043,
            handlers: {
                'submit': function (e) {
                    console.log(e.values);
                    _this.variantWidget.setLoading(true);
                    var regions = [];
                    if (e.values.region !== "") {
                        regions = e.values.region.split(",");
                    }
                    delete  e.values.region;

                    if (e.values.studies !== undefined) {
                        if (e.values.studies instanceof Array) {
                            e.values.studies = e.values.studies.join(",");
                        }
                    }

                    if (e.values.ct !== undefined) {
                        if (e.values.ct instanceof Array) {
                            e.values.ct = e.values.ct.join(",");
                        }
                    }

                    if (e.values.gene !== "") {
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
                    }

                    if (e.values.snp !== "") {
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
        var positionFilter = new PositionFilterFormPanel();
        var studyFilter = new StudyFilterFormPanel({
            urlStudies: "http://wwwdev.ebi.ac.uk/eva/webservices/rest/v1/studies/list"
        });

        var conseqType = new ConsequenceTypeFilterFormPanel();


        var formPanel = new FormPanel({
//            title: 'Create Tracks',
            border: false,
            submitButtonText: 'Add track',
            target: target,
            filters: [studyFilter, conseqType],
            width: 300,
            height: 500,
            handlers: {
                'submit': function () {

                }
            }
        });

        return formPanel;
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
            trackListTitle: '',
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
            targetId: null,
            id: 2,
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
            targetId: null,
            id: 1,
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
            targetId: null,
            id: 2,
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
            targetId: null,
            id: 4,
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


        var eva = new FeatureTrack({
            targetId: null,
            id: 4,
            title: 'Eva',
            featureType: 'variant',
            minHistogramRegionSize: 10000,
            maxLabelRegionSize: 3000,
            height: 100,

            renderer: new FeatureRenderer(FEATURE_TYPES.undefined),

            dataAdapter: new EvaAdapter({
                host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
                version: 'v1',
                category: "segments",
                resource: "variants",
                params: {
//                    exclude: ''
                },
                cacheConfig: {
                    chunkSize: 10000
                }
            })
        });

        genomeViewer.addOverviewTrack(geneOverview);
//        genomeViewer.addTrack([sequence, gene, snp]);

        genomeViewer.addTrack(eva);

        return genomeViewer;
    },
    _getStudiesInfo: function(){
        var _this = this;
        var studyNames = [];
        var studies = [];

        $.ajax({
                url: "http://wwwdev.ebi.ac.uk/eva/webservices/rest/v1/studies/list",
                dataType: 'json',
                async: false,
                success: function (response, textStatus, jqXHR) {
                    var data = (response !== undefined && response.response.length > 0 && response.response[0].numResults > 0)? response.response[0].result : [];

                    for (var i = 0; i < data.length; i++) {
                        var study = data[i];
                        //studies.push(study);
                        studyNames.push(study.studyId);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log('Error loading studies');
                }
            });


            for (var i = 0, l = studyNames.length; i < l; i ++) {
                var study = studyNames[i];

                var url =  "http://wwwdev.ebi.ac.uk/eva/webservices/rest/v1/studies/" + study + "/files"
                $.ajax({
                    url: url,
                    dataType: 'json',
                    async: false,
                    success: function (response, textStatus, jqXHR) {
                        var data = (response !== undefined && response.response.length > 0 && response.response[0].numResults > 0)? response.response[0].result : [];

                        for (var i = 0; i < data.length; i++) {
                            var study = data[i];
                            _this._addFileToStudy(studies, study);
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log('Error loading final studies');
                    }
                });
            }

            return studies;
    },
    _addFileToStudy: function(studies, file){
        var b = false;
        for (var i = 0, l = studies.length; i < l && !b; i ++) {
            var study= studies[i];

            if(study.studyId == file.studyId){
                study.files.push(file);
                b = true;
            }
        }

        if(!b){
            studies.push({
                studyId: file.studyId,
                files: [file]
            })
        }
    }
}
