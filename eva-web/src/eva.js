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


//        /* genome browser option*/
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

        this.select('Genome Browser');
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
        this.formPanelVariantFilter.draw();
        this.genomeViewer.draw();
        this.formPanelGenomeFilter.draw();
//        var EXAMPLE_DATA = [
//            {
//                "type": "SNV",
//                "chromosome": "1",
//                "start": 1650807,
//                "end": 1650807,
//                "length": 1,
//                "reference": "T",
//                "alternate": "C",
//                "id": "rs1137005",
//                "hgvs": {
//                    "genomic": [
//                        "1:g.1650807T>C"
//                    ]
//                },
//                "files": {
//                    "test2": {
//                        "fileId": "test2",
//                        "studyId": "test",
//                        "format": "GT:DS:GL",
//                        "samplesData": {
//                            "NA19600": {
//                                "GT": "0|1"
//                            },
//                            "NA19660": {
//                                "GT": "0|1"
//                            },
//                            "NA19661": {
//                                "GT": "1|1"
//                            },
//                            "NA19685": {
//                                "GT": "0|0"
//                            }
//                        },
//                        "stats": {
//                            "chromosome": null,
//                            "position": -1,
//                            "refAllele": null,
//                            "altAllele": null,
//                            "refAlleleCount": -1,
//                            "altAlleleCount": -1,
//                            "genotypesCount": {
//                                "0|1": 2,
//                                "1|1": 1,
//                                "0|0": 1
//                            },
//                            "missingAlleles": 0,
//                            "missingGenotypes": 0,
//                            "refAlleleFreq": -1,
//                            "altAlleleFreq": -1,
//                            "genotypesFreq": { },
//                            "maf": 0.5,
//                            "mgf": 1,
//                            "mafAllele": "T",
//                            "mgfGenotype": "0|1",
//                            "pedigreeStatsAvailable": false,
//                            "mendelianErrors": -1,
//                            "casesPercentDominant": -1,
//                            "controlsPercentDominant": -1,
//                            "casesPercentRecessive": -1,
//                            "controlsPercentRecessive": -1,
//                            "transitionsCount": -1,
//                            "transversionsCount": -1,
//                            "quality": 0,
//                            "numSamples": 0
//                        },
//                        "attributes": {
//                            "QUAL": "100.0",
//                            "FILTER": "PASS"
//                        }
//                    }
//                },
//                "effect": [ ]
//            }
//
//        ];
        var EXAMPLE_DATA = [];
        $.ajax({
            url: this.host,
            dataType: 'json',
            async: false,
            success: function (response, textStatus, jqXHR) {
                if (response != undefined && response.response.numResults > 0) {
                    for (var i = 0; i < response.response.result.length; i++) {
                        var elem = response.response.result[i];
                        EXAMPLE_DATA.push(elem);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log('Error loading Phenotypes');
            }
        });
        this.variantWidget.variantBrowserGrid.load(EXAMPLE_DATA);
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
                        variant.hgvs_name= variant.hgvs.genomic[0];
                    }
                }

                console.log("DATA");
                console.log(data);
            }
        }); //the div must exist

        return variantWidget;
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
            height: 1000,
            handlers: {
                'search': function (e) {
                    console.log(e.filterParams);
                    var regions = [];
                    if (e.filterParams.region !== "") {
                        regions = e.filterParams.region.split(",");
                    }
                    delete  e.filterParams.region;


                    if(e.filterParams.studies !== undefined){
                    
                        e.filterParams.studies = e.filterParams.studies.join(",");
                    }
                    if (e.filterParams.gene !== "") {
                        CellBaseManager.get({
                            species: 'hsapiens',
                            category: 'feature',
                            subCategory: 'gene',
                            query: e.filterParams.gene,
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

                    if (e.filterParams.snp !== "") {
                        CellBaseManager.get({
                            species: 'hsapiens',
                            category: 'feature',
                            subCategory: 'snp',
                            query: e.filterParams.snp,
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
                        //host: 'http://172.22.70.2:8080/eva/webservices/rest',
                        //host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
                        host: 'http://ves-ebi-f8:8080/eva/webservices/rest',
                        category: 'segments',
                        resource: 'variants',
                        query: regions
                    });
                    _this.variantWidget.retrieveData(url, e.filterParams)
                }
            }
        });

        return formPanel;
    },
    _createFormPanelGenomeFilter: function (target) {
        var positionFilter = new PositionFilterFormPanel();
        var studyFilter = new StudyFilterFormPanel({
            urlStudies: "http://www.ebi.ac.uk/eva/webservices/rest/v1/studies/list"
        });

        var conseqType = new ConsequenceTypeFilterFormPanel();


        var formPanel = new FormPanel({
            title:'Create Tracks',
            target: target,
            filters: [studyFilter, conseqType],
            width: 300,
            height: 500
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


//        var eva = new FeatureTrack({
//            targetId: null,
//            id: 4,
//            title: 'Eva',
//            featureType: 'variant',
//            minHistogramRegionSize: 10000,
//            maxLabelRegionSize: 3000,
//            height: 100,
//
//            renderer: new FeatureRenderer(FEATURE_TYPES.undefined),
//
//            dataAdapter: new EvaAdapter({
//                host: 'http://www.ebi.ac.uk/eva/webservices/rest',
//                version: 'v1',
//                category: "segments",
//                resource: "variants",
//                params: {
////                    exclude: ''
//                },
//                cacheConfig: {
//                    chunkSize: 10000
//                }
//            })
//        });

        genomeViewer.addOverviewTrack(geneOverview);
        genomeViewer.addTrack([sequence, gene, snp]);

//        genomeViewer.addTrack(eva);

        return genomeViewer;
    }
}
