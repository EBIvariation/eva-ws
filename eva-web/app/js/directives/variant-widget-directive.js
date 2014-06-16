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
        controller: function($scope) {

        },
        link: function($scope, element, attr) {


            var location = '21:9411240-9711260';
            //var gene = 'TMEM51';
            $scope.location = location;
            $scope.gene = gene;

            var conTypeTree;
            var studiesTree;
            var varClassesTree;

            $scope.variantTableId            = 'variantBrowserTable';
            $scope.variantEffectTableId      = 'variantEffectTable';
            $scope.variantFilesTableId       = 'variantFilesTable';
            $scope.variantStatsViewId        = 'variantStatsView';
            $scope.variantStatsChartId       = 'variantStatsChart';
            $scope.variantGenoTypeTableId    = 'variantGenoTypeTable';
            $scope.variantGenomeViewerId     = 'variant-browser-gv';
            $scope.variantBrowserSubTabsId   = 'variantSubTabs';
            $scope.studiesTreeId             = 'studiesTreeID';
            $scope.consequenceTypeTreeId     = 'consequenceTypeTreeID';
            $scope.variationClassesTreeId    = 'variationClassesTreeID';





            $scope.searchVariants = function(){
                eventManager.trigger("variant:search");
            }

            $scope.reloadVariants = function(){
                $scope.location = location;
                // $scope.gene = gene;
                eventManager.trigger("variant:search");
            }

            $scope.clearVariants = function(){
                $scope.location = '';
                $scope.gene = '';
                clearCheckedFilters(studiesTree);
                clearCheckedFilters(conTypeTree);
                clearCheckedFilters(varClassesTree);
                eventManager.trigger("variant:search");
            }



            jQuery('#topMenuTab a[data-toggle="tab"]').on('shown.bs.tab', function (e) {

                var variantTreeWidget;
                variantTreeWidget = new VariantWidget({});

                var studiesTreeArgs = [];
                studiesTreeArgs.id =  $scope.studiesTreeId;
                studiesTreeArgs.data = $scope.studies;
                studiesTreeArgs.link = false;


                var consequenceTypeTreeArgs = [];
                consequenceTypeTreeArgs.id =  $scope.consequenceTypeTreeId;
                consequenceTypeTreeArgs.data = consequenceTypes;
                consequenceTypeTreeArgs.link = true;


                var variationClassesTreeArgs = [];
                variationClassesTreeArgs.id =  $scope.variationClassesTreeId;
                variationClassesTreeArgs.data = variationClasses;
                variationClassesTreeArgs.link = true;

                if(e.target.parentElement.id === 'variationLi'){

                    if(jQuery("#"+$scope.studiesTreeId).contents().length === 0 ){
                        studiesTree = variantTreeWidget.createTreePanel(studiesTreeArgs);
                    }
                    if(jQuery("#"+$scope.consequenceTypeTreeId).contents().length === 0 ){
                        conTypeTree = variantTreeWidget.createTreePanel(consequenceTypeTreeArgs);
                    }

                    if(jQuery("#"+$scope.variationClassesTreeId).contents().length === 0 ){
                        varClassesTree = variantTreeWidget.createTreePanel(variationClassesTreeArgs);
                    }
                    eventManager.trigger("variant:search");
                }

            });



            eventManager.on("gene:search variant:search" , function(e) {
                updateRegion( $scope.gene);
            });


            eventManager.on("variant:search", function(e) {

                var studyFilter  = getCheckedFilters(studiesTree.getView().getChecked());
                var conTypeFilter  = getCheckedFilters(conTypeTree.getView().getChecked());
                var varClassesFilter  = getCheckedFilters(varClassesTree.getView().getChecked());

//                    console.log(studyFilter)
//                    console.log(conTypeFilter)
//                    console.log(varClassesFilter)

                if(conTypeFilter){
                    $scope.CTfilter = '&effect='+conTypeFilter;
                }else{
                    $scope.CTfilter = '';
                }

                if(studyFilter){
                    $scope.studyFilter = '&studies='+studyFilter;
                }else{
                    $scope.studyFilter = '';
                }

                $scope.filters =  $scope.CTfilter+$scope.studyFilter;

                var variantWidget;
                variantWidget = new VariantWidget({
                    variantTableID          : $scope.variantTableId,
                    variantEffectTableID    : $scope.variantEffectTableId,
                    variantFilesTableID     : $scope.variantFilesTableId,
                    variantStatsViewID      : $scope.variantStatsViewId,
                    variantStatsChartID     : $scope.variantStatsChartId,
                    variantGenoTypeTableID  : $scope.variantGenoTypeTableId,
                    location                : $scope.location,
                    filters                 : $scope.filters,
                    variantGenomeViewerID   : $scope.variantGenomeViewerId,
                    variantSubTabsID        : $scope.variantBrowserSubTabsId,
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


            //function to update region by geneId
            function updateRegion(args){
                if(args){
                    var geneInfoURL = CELLBASE_HOST+'/'+CELLBASE_VERSION+'/hsa/feature/gene/'+$scope.gene+'/info?of=json';

                    var regionData;
                    CellBaseManager.get({
                        species: 'hsa',
                        category: 'feature',
                        subCategory: 'gene',
                        resource: 'info',
                        query: $scope.gene,
                        async: false,
                        success: function (data) {
                            regionData = data;
                        },
                        error: function (data) {
                            console.log('Could not get variant effects list');
                        }
                    });

                    var region = regionData[0][0].chromosome+':'+regionData[0][0].start+'-'+regionData[0][0].end;
                    $scope.$apply(function(){
                        $scope.location = region;
                    })

                }
            }


            //Function to get checked filters
            function getCheckedFilters(args){
                if(args.length > 0){
                    var filters = [];
                    Ext.Array.each(args , function(rec){
                        filters.push(rec.get('name'));
                    });
                    return filters.join();
                }
            }

            //Function to clear checked filters
            function clearCheckedFilters(args){
                args.store.getRootNode().cascadeBy(function(){
                    this.set( 'checked', false );
                });
            }

        }

    };

}).directive('variantView', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variant-view.html',
        controller: function($scope) {
        },
        link: function($scope, element, attr) {

            var data = getUrlParameters("");

            if(data.value){
                var variantData;
                evaManager.get({
                    category: 'variants',
                    resource: 'info',
                    params: {
                        of: 'json'
                    },
                    query: data.value,
                    async: false,
                    success: function (data) {
                        variantData = data;
                    },
                    error: function (data) {
                        console.log('Could not get variant info');
                    }
                });
                var tmpData = variantData;

            }else{
                return;
            }
            if(!tmpData || !tmpData.response.numResults){
                return;
            }

            $scope.variant = data.value;

            $scope.variantInfoData  = tmpData.response.result[0];

            var position = $scope.variantInfoData.chr + ":" + $scope.variantInfoData.start + ":" + $scope.variantInfoData.ref + ":" + $scope.variantInfoData.alt;
            var effectsTempData;
            CellBaseManager.get({
                species: 'hsa',
                category: 'genomic',
                subCategory: 'variant',
                resource: 'consequence_type',
                query: position,
                async: false,
                success: function (data) {
                    effectsTempData = data;
                },
                error: function (data) {
                    console.log('Could not get variant effects list');
                }
            });
            var effectsTempDataArray = [];

            $.each(effectsTempData, function(key, value) {
                var consequenceType = value.consequenceType;
                if(!effectsTempDataArray[consequenceType]) effectsTempDataArray[consequenceType] = [];
                effectsTempDataArray[consequenceType].push({'featureId':value.featureId,'featureType':value.featureType,'featureChromosome':value.featureChromosome,'featureStart':value.featureStart,'featureEnd':value.featureEnd,'consequenceTypeType':value.consequenceTypeType});
            });

            var effectsDataArray = new Array();
            for (key in effectsTempDataArray){
                effectsDataArray.push({id:key,name:effectsTempDataArray[key][0].consequenceTypeType,data:effectsTempDataArray[key]});
            }

            $scope.effectsData = effectsDataArray;


            var variantStudyWidget;
            variantStudyWidget = new VariantStudyWidget({
                render_id    : 'variantViewStudyID',
                variantId    :  data.value
            });

            variantStudyWidget.draw('files');

        }

    };

});