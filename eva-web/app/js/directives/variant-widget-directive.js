/**
 * Created by jag on 14/04/2014.
 */
var genomeViewer;
angular.module('variantWidgetModule', []).directive('variantWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variation-browser-view.html',
        link: function($scope, element, attr) {



                $scope.variantTableId       = 'variantBrowserTable';
                $scope.variantEffectTableId = 'variantEffectTable';
                $scope.variantFilesTableId  = 'variantFilesTable';
                $scope.variantStatsViewId   = 'variantStatsView';
                $scope.variantStatsChartId  = 'variantStatsChart';
                $scope.variantGenomeViewerId  = 'variant-browser-gv';
                $scope.variantBrowserSubTabsId  = 'variantSubTabs';


                eventManager.on("variant:search", function(e) {

                    console.log( $scope.selectedCT.filter);
                    console.log( $scope.selectedVC.filter);
                    if($scope.selectedCT.filter.length > 0){
                        $scope.CTfilter = '&effect='+$scope.selectedCT.filter.join();
                    }else{
                        $scope.CTfilter = '';
                    }

                    $scope.filters =  $scope.CTfilter;
                    var variantWidget;
                    variantWidget = new VariantWidget({
                        variantTableID       : $scope.variantTableId,
                        variantEffectTableID : $scope.variantEffectTableId,
                        variantFilesTableID  : $scope.variantFilesTableId,
                        variantStatsViewID   : $scope.variantStatsViewId,
                        variantStatsChartID  : $scope.variantStatsChartId,
                        location             : $scope.location,
                        filters              : $scope.filters,
                        variantGenomeViewerID: $scope.variantGenomeViewerId,
                        variantSubTabsID     : $scope.variantBrowserSubTabsId,

                    });
                    variantWidget.draw();


                    if(!genomeViewer.rendered) {
                        genomeViewer.render();
                        genomeViewer.draw();
                        genomeViewer.addTrack(tracks);
                        genomeViewer.addOverviewTrack(geneOverview);
                    }

                    var region = new Region();
                    region.parse($scope.location);
                    genomeViewer.setRegion(region);

                });





            CELLBASE_HOST = "http://ws-beta.bioinfo.cipf.es/cellbase/rest";
            CELLBASE_VERSION = "v3";
            var region = new Region({chromosome: "13", start: 32889611, end: 32889611});
            var availableSpecies = {
                "text": "Species",
                "items": [
                    {
                        "text": "Vertebrates",
                        "items": [
                            {"text": "Homo sapiens", "assembly": "GRCh37.p10", "region": {"chromosome": "13", "start": 32889611, "end": 32889611}, "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"], "url": "ftp://ftp.ensembl.org/pub/release-71/"},
                            {"text": "Mus musculus", "assembly": "GRCm38.p1", "region": {"chromosome": "1", "start": 18422009, "end": 18422009}, "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "X", "Y", "MT"], "url": "ftp://ftp.ensembl.org/pub/release-71/"}
                        ]
                    }
                ]
            };
            var species = availableSpecies.items[0].items[0];


            genomeViewer = new GenomeViewer({
                targetId: 'variant-browser-gv',
                region: region,
                availableSpecies: availableSpecies,
                species: species,
                sidePanel: false,
                autoRender: false,
                border: true,
                resizable: true,
                karyotypePanelConfig: {
                    collapsed: false,
                    collapsible: true
                },
                chromosomePanelConfig: {
                    collapsed: false,
                    collapsible: true
                },
                handlers:{
                    'region:change':function(e){
                        console.log(e)
                    }
                }
            }); //the div must exist


            var tracks = [];
            var sequence = new SequenceTrack({
                targetId: null,
                id: 1,
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
            tracks.push(sequence);

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
            tracks.push(gene);


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
//        title: 'Gene',
                minHistogramRegionSize: 20000000,
                maxLabelRegionSize: 10000000,
                height: 100,

                renderer: renderer,

                dataAdapter: new CellBaseAdapter({
                    category: "genomic",
                    subCategory: "region",
                    resource: "gene",
                    params: {
                        exclude: 'transcripts'
                    },
                    species: genomeViewer.species,
                    cacheConfig: {
                        chunkSize: 100000
                    }
                })
            });

            this.snp = new FeatureTrack({
                targetId: null,
                id: 4,
                title: 'SNP',
                featureType:'SNP',
                minHistogramRegionSize: 12000,
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

            tracks.push(this.snp);




//            genomeViewer.draw();

            }

    };
}).config(function($stateProvider, $urlRouterProvider) {
//    $stateProvider
//        .state('variant', {
//            url: "/variant",
//            templateUrl: "views/variation-browser-view.html",
//        })
//        .state('variant.view', {
//            url: "/view",
//            templateUrl: "views/variant-view.html",
//        })
//        .state('variant.browser', {
//            url: "/browser",
//            templateUrl: "views/variant-browser.html",
//
//        })

});
