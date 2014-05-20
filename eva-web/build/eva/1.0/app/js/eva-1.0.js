/*! EVA - v1.0 - 2014-05-20 10:09:14
* http://https://github.com/EBIvariation/eva.git/
* Copyright (c) 2014  Licensed GPLv2 */
var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','highcharts-ng','ebiVar.Services.Metadata','homeWidgetModule','variantWidgetModule','checklist-model', 'geneWidgetModule', 'ui.router', 'duScroll']);


METADATA_HOST = "http://www.ebi.ac.uk/eva/webservices/rest";
METADATA_VERSION = 'v1';

CELLBASE_HOST = "http://www.ebi.ac.uk/cellbase/webservices/rest";
CELLBASE_VERSION = "v3";

if(window.location.host.indexOf("www.ebi.ac.uk") === -1){

    METADATA_HOST = "http://wwwint.ebi.ac.uk/eva/webservices/rest";
    METADATA_VERSION = 'v1';

    CELLBASE_HOST = "http://ws-beta.bioinfo.cipf.es/cellbase-staging/rest";
    CELLBASE_VERSION = "latest";
}










angular.module('ebiApp', []).directive('ebiHeader', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: 'views/header.html',
        controller: function($scope) {

        }
    }
}).directive('ebiFooter', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: 'views/footer.html',
        controller: function($scope) {

        }
    }
});
var variationCtrl = evaApp.controller('variationBrowserCtrl', ['$scope', '$rootScope', 'ebiVarMetadataService', function ($scope, $rootScope, ebiVarMetadataService) {

    $scope.searchboxValue;
    $scope.search = function(value){

        var url = window.location.origin+window.location.pathname+'?variantID='+$scope.searchboxValue;
        window.location.href = url;
    }

    eventManager.on("gene:search variant:search" , function(e) {
       updateRegion( $scope.gene);
    });

    function createSummaryChart(){

        var summaryUrl = METADATA_HOST+'/'+METADATA_VERSION+'/genes/ranking';
        var summaryData = ebiVarMetadataService.fetchData(summaryUrl);
        var summaryChartData = parseSummaryChartData(summaryData);
        console.log(summaryChartData.data)

        $scope.summaryPieChartConfig = {
            options: {
                chart: {
                    type: 'pie'
                },

                plotOptions: {

                    series: {
                        cursor: 'pointer',
                        // size: 80,
                        point: {
                            events: {
                                click: function() {
                                    console.log(this)
                                }
                            }
                        }

                    }
                }
            },
            series: [{
                data:   summaryChartData.data
            }],
            title: {
                text:  summaryChartData.title
            },
            loading: false,
            credits: {
                enabled: false
            }
        }
    }

    function parseSummaryChartData(args){

        var data = [];
        var tempArray=[];
        for (key in args.response.result) {
            tempArray.push([args.response.result[key]._id,args.response.result[key].count]);
        }

        data['data'] = tempArray;
        data['title'] = 'Genes Ranking';

        return data;
    }

    $scope.statistics = '+';
    $scope.showStatitsicsState;

    $scope.searchVariants = function(){
        eventManager.trigger("variant:search");
    }
    $scope.reloadVariants = function(){
        $scope.location = location;
        $scope.gene = gene;
        //eventManager.trigger("variant:search");
    }
    $scope.searchGenes = function(){
        eventManager.trigger("gene:search");
    }



    $scope.showStatitsics = function(){
        this.showStatitsicsState = !this.showStatitsicsState;
        if(!this.showStatitsicsState){
            this.statistics = '+';
        }else{
            this.statistics = '-';
            createSummaryChart();
        }
    };

    var location = '21:9411240-9411260';
    var gene = 'TMEM51';
    //$scope.location = '1:5000-3500000';
    $scope.location = location;
    //$scope.gene = gene;

    function updateRegion(args){

        if(args){
                var geneInfoURL = CELLBASE_HOST+'/'+CELLBASE_VERSION+'/hsa/feature/gene/'+$scope.gene+'/info?of=json';
                var regionData = ebiVarMetadataService.fetchData(geneInfoURL);
                var region = regionData[0][0].chromosome+':'+regionData[0][0].start+'-'+regionData[0][0].end;
                //sconsole.log(region)
                $scope.location = region;
        }
    }



    $scope.studies = [
//        {id: "1000g", name: "1000 genomes", description: "..."},
//        {id: "gonl", name: "GoNL", description: "..."},
//        {id: "evs", name: "EVS", description: "..."},
        {name: '1000g',leaf: true, checked: false,  iconCls :'no-icon' },
        {name: 'GoNL',leaf: true, checked: false,  iconCls :'no-icon' },
        {name: 'EVS',leaf: true, checked: false,  iconCls :'no-icon' }
    ]




//    $scope.selectedCT = {
//        filter: []
//    };
//
//    $scope.checkAll_CT = function() {
//        $scope.selectedCT.filter = $scope.consequenceTypes.map(function(item) { return item.name; });
//    };
//    $scope.uncheckAll_CT = function() {
//        $scope.selectedCT.filter = [];
//    };


    $scope.consequenceTypes = [
        {
            name:'UTR Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001623', name: '5_prime_UTR_variant', description: 'A UTR variant of the 5\' UTR',leaf: true,checked: false,  iconCls :'no-icon'},
                        {acc: 'SO:0001624', name: '3_prime_UTR_variant', description: 'A UTR variant of the 3\' UTR',leaf: true, checked: false,  iconCls :'no-icon'},
                {acc: 'SO:0001819', name: 'synonymous_variant', description: 'A sequence variant where there is no resulting change to the encoded amino acid',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001567', name: 'stop_retained_variant', description: 'A sequence variant where at least one base in the terminator codon is changed, but the terminator remains',leaf: true,checked: false,  iconCls :'no-icon' },



            ]
        },
        {
            name:'Feature Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001907', name: 'feature_elongation', description: 'A sequence variant that causes the extension of a genomic feature, with regard to the reference sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001906', name: 'feature_truncation', description: 'A sequence variant that causes the reduction of a genomic feature, with regard to the reference sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001627', name: 'intron_variant', description: 'A transcript variant occurring within an intron',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001628', name: 'intergenic_variant', description: 'A sequence variant located in the intergenic region, between genes',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001566', name: 'regulatory_region_variant', description: 'A sequence variant located within a regulatory region',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001894', name: 'regulatory_region_ablation', description: 'A feature ablation whereby the deleted region includes a regulatory region',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001891', name: 'regulatory_region_amplification', description: 'A feature amplification of a region containing a regulatory region',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001587', name: 'stop_gained', description: 'A sequence variant whereby at least one base of a codon is changed, resulting in a premature stop codon, leading to a shortened transcript',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001589', name: 'frameshift_variant', description: 'A sequence variant which causes a disruption of the translational reading frame, because the number of nucleotides inserted or deleted is not a multiple of three',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001578', name: 'stop_lost', description: 'A sequence variant where at least one base of the terminator codon (stop) is changed, resulting in an elongated transcript',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001782', name: 'TF_binding_site_variant', description: 'A sequence variant located within a transcription factor binding site',leaf: true,checked: false,  iconCls :'no-icon' }
            ]
        },
        {
            name:'Inframe Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001821', name: 'inframe_insertion', description: 'An inframe non synonymous variant that inserts bases into in the coding sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001822', name: 'inframe_deletion', description: 'An inframe non synonymous variant that deletes bases from the coding sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001626', name: 'incomplete_terminal_codon_variant', description: 'A sequence variant where at least one base of the final codon of an incompletely annotated transcript is changed',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001582', name: 'initiator_codon_variant', description: 'A codon variant that changes at least one base of the first codon of a transcript',leaf: true,checked: false,  iconCls :'no-icon' }
                ]
        },
        {
            name:'Transcript Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001620', name: 'mature_miRNA_variant', description: 'A transcript variant located with the sequence of the mature miRNA',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001792', name: 'non_coding_exon_variant', description: 'A sequence variant that changes non-coding exon sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001619', name: 'nc_transcript_variant', description: 'A transcript variant of a non coding RNA',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001621', name: 'NMD_transcript_variant', description: 'A variant in a transcript that is the target of NMD',leaf: true,checked: false,  iconCls :'no-icon' }
                ]
        },
        {
            name:'Splice Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001575', name: 'splice_donor_variant', description: 'A splice variant that changes the 2 base region at the 5\' end of an intron',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001574', name: 'splice_acceptor_variant', description: 'A splice variant that changes the 2 base region at the 3\' end of an intron',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001630', name: 'splice_region_variant', description: 'A sequence variant in which a change has occurred within the region of the splice site, either within 1-3 bases of the exon or 3-8 bases of the intron',leaf: true,checked: false,  iconCls :'no-icon' }
                ]
        },
        {
            name:'Structural Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001889', name: 'transcript_amplification', description: 'A feature amplification of a region containing a transcript',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001893', name: 'transcript_ablation', description: 'A feature ablation whereby the deleted region includes a transcript feature',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001895', name: 'TFBS_ablation', description: 'A feature ablation whereby the deleted region includes a transcription factor binding site',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001892', name: 'TFBS_amplification', description: 'A feature amplification of a region containing a transcription factor binding site',leaf: true,checked: false,  iconCls :'no-icon' }
                ]
        },
        {
            name:'Intergenic Variant',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: 'SO:0001631', name: 'upstream_gene_variant', description: 'A sequence variant located 5\' of a gene',leaf: true,checked: false,  iconCls :'no-icon' },
                        {acc: 'SO:0001632', name: 'downstream_gene_variant', description: 'A sequence variant located 3\' of a gene',leaf: true,checked: false,  iconCls :'no-icon' }
                 ]
        },

        {acc: 'SO:0001583', name: 'missense_variant', description: 'A sequence variant, that changes one or more bases, resulting in a different amino acid sequence but where the length is preserved',leaf: true,checked: false,  iconCls :'no-icon' },
        {acc: 'SO:0001580', name: 'coding_sequence_variant', description: 'A sequence variant that changes the coding sequence',leaf: true,checked: false,  iconCls :'no-icon' }

    ];



//    $scope.selectedVC = {
//        filter: []
//    };
//
//    $scope.checkAll_VC = function() {
//        $scope.selectedVC.filter = $scope.variationClasses.map(function(item) { return item.name; });
//    };
//    $scope.uncheckAll_VC = function() {
//        $scope.selectedVC.filter = [];
//    };

    $scope.variationClasses = [
        {
            name:'Variation',
            cls: "folder",
            expanded: true,
            leaf: false,
            children: [
                        {acc: "SO:0001483", name: "SNV", description: "SNVs are single nucleotide positions in genomic DNA at which different sequence alternatives exist.", call: "Variation",leaf: true,checked: false,iconCls :'no-icon'},
                        {acc: "SO:1000032", name: "indel", description: "A sequence alteration which included an insertion and a deletion, affecting 2 or more bases.", call: "Variation",leaf: true,checked: false,  iconCls :'no-icon'},
                        {acc: "SO:1000002", name: "substitution", description: "A sequence alteration where the length of the change in the variant is the same as that of the reference.", call: "Variation",leaf: true,checked: false,  iconCls :'no-icon'},
                        {acc: "SO:0000667", name: "insertion", description: "The sequence of one or more nucleotides added between two adjacent nucleotides in the sequence.", call: "Variation", leaf: true,checked: false,  iconCls :'no-icon'},
                        {acc: "SO:0001059", name: "sequence_alteration", description: "A sequence_alteration is a sequence_feature whose extent is the deviation from another sequence.", call: "Variation", leaf: true,checked: false,  iconCls :'no-icon'},
                        {acc: "SO:0000705", name: "tandem_repeat", description: "Two or more adjcent copies of a region (of length greater than 1).", call: "Variation", leaf: true,checked: false,  iconCls :'no-icon'},
                        {acc: "SO:0000159", name: "deletion", description: "The point at which one or more contiguous nucleotides were excised.", call: "Variation" , leaf: true,checked: false,  iconCls :'no-icon'}
                  ]
        },
//        {
//            name:'Structural variation',
//            cls: "folder",
//            expanded: true,
//            leaf: false,
//            children: [
//                {acc: "SO:0001784", name: "complex_structural_alteration", description: "A structural sequence alteration or rearrangement encompassing one or more genome fragments.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001742", name: "copy_number_gain", description: "A sequence alteration whereby the copy number of a given regions is greater than the reference sequence.", call: "Structural variation" , leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001743", name: "copy_number_loss", description: "A sequence alteration whereby the copy number of a given region is less than the reference sequence.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001019", name: "copy_number_variation", description: "A variation that increases or decreases the copy number of a given region.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:1000035", name: "duplication", description: "One or more nucleotides are added between two adjacent nucleotides in the sequence; the inserted sequence derives from, or is identical in sequence to, nucleotides adjacent to insertion point.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001873", name: "interchromosomal_breakpoint", description: "A rearrangement breakpoint between two different chromosomes.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001874", name: "intrachromosomal_breakpoint", description: "A rearrangement breakpoint within the same chromosome.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:1000036", name: "inversion", description: "A continuous nucleotide sequence is inverted in the same position.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001837", name: "mobile_element_insertion", description: "A kind of insertion where the inserted sequence is a mobile element.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0001838", name: "novel_sequence_insertion", description: "An insertion the sequence of which cannot be mapped to the reference genome.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:1000173", name: "tandem_duplication", description: "A duplication consisting of 2 identical adjacent regions.", call: "Structural variation", leaf: true,checked: false,  iconCls :'no-icon'},
//                {acc: "SO:0000199", name: "translocation", description: "A region of nucleotide sequence that has translocated to a new position.", call: "Structural variation" , leaf: true,checked: false,  iconCls :'no-icon'}
//            ]
//        },
        {acc: "SO:0000051", name: "probe", description: "A DNA sequence used experimentally to detect the presence or absence of a complementary nucleic acid.", call: "CNV probe", leaf: true,checked: false,  iconCls :'no-icon'}
    ];

    $scope.barChart = {
        options: {
            chart: {
                type: 'bar',
            },
            plotOptions: {
                series: {
                    cursor: 'pointer',
                    point: {
                        events: {
                            click: function() {
                                console.log(this.series.color)
                            }
                        }
                    }

                }
            }

        },

        series: [{
            data:   $scope.data
        }],
        title: {
            text:  $scope.message
        },
        loading: false,
        credits: {
            enabled: false
        }
    }


}]);


function EventManager (){
    _.extend(this, Backbone.Events);
};

angular.module('homeWidgetModule', []).directive('homeWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/home.html',
        link: function($scope, element, attr) {
            $scope.hometest = 'bla bla';
            //twitter widget
            !function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");



            }
    }
});
var genomeViewer;


angular.module('variantWidgetModule', []).directive('variantWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variation-browser-view.html',
        link: function($scope, element, attr) {
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

                jQuery('#topMenuTab a[data-toggle="tab"]').on('shown.bs.tab', function (e) {

                    var variantTreeWidget;
                    variantTreeWidget = new VariantWidget({});

                    var studiesTreeArgs = [];
                    studiesTreeArgs.id =  $scope.studiesTreeId;
                    studiesTreeArgs.data = $scope.studies;


                    var consequenceTypeTreeArgs = [];
                    consequenceTypeTreeArgs.id =  $scope.consequenceTypeTreeId;
                    consequenceTypeTreeArgs.data = $scope.consequenceTypes;


                    var variationClassesTreeArgs = [];
                    variationClassesTreeArgs.id =  $scope.variationClassesTreeId;
                    variationClassesTreeArgs.data = $scope.variationClasses;

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
                       // eventManager.trigger("variant:search");
                    }

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

                    $scope.filters =  $scope.CTfilter;

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


        }

    };

}).directive('variantView', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variant-view.html',
        controller: function($scope,ebiVarMetadataService) {


           var data = getUrlParameters("");

           if(data.value){
               var variantInfoUrl = METADATA_HOST+'/'+METADATA_VERSION+'/variants/'+data.value+'/info';
               var tmpData = ebiVarMetadataService.fetchData(variantInfoUrl);
           }else{
               return;
           }

            if(!tmpData.response.numResults){
                return;
            }

            $scope.variant = data.value;

            $scope.variantInfoData  = tmpData.response.result[0];
            $scope.variantFilesData = tmpData.response.result[0].files[0];

            console.log($scope.variantInfoData)

            var position = $scope.variantInfoData.chr + ":" + $scope.variantInfoData.start + ":" + $scope.variantInfoData.ref + ":" + $scope.variantInfoData.alt;
            var url = CELLBASE_HOST+'/'+CELLBASE_VERSION+'/hsa/genomic/variant/'+position+'/consequence_type?of=json';
            var effectsTempData = ebiVarMetadataService.fetchData(url);
            var effectsTempDataArray = [];

            $.each(effectsTempData, function(key, value) {
                var consequenceType = value.consequenceType;
                if(!effectsTempDataArray[consequenceType]) effectsTempDataArray[consequenceType] = [];
                effectsTempDataArray[consequenceType].push({'featureId':value.featureId,'featureType':value.featureType,'featureChromosome':value.featureChromosome,'featureStart':value.featureStart,'featureEnd':value.featureEnd,'consequenceTypeType':value.consequenceTypeType});
            });


            var effectsDataArray = new Array();
            for (key in effectsTempDataArray){
                console.log(effectsTempDataArray[key][0].consequenceTypeType)
                effectsDataArray.push({id:key,name:effectsTempDataArray[key][0].consequenceTypeType,data:effectsTempDataArray[key]});
            }
            $scope.effectsData = effectsDataArray;

            $scope.effects = '-';
            $scope.showEffectsState = true;



            console.log($scope.effectsData );

            $scope.showEffects = function(){
                this.showEffectsState = !this.showEffectsState;
                if(!this.showEffectsState){
                    this.effects = '+';
                }else{
                    this.effects = '-';
                }
            };

        },
        link: function($scope, element, attr) {


        }

    };

});
angular.module('geneWidgetModule', []).directive('geneWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/gene-browser-view.html',
        link: function($scope, element, attr) {
            $scope.geneView = 'sdasfd';
            $scope.geneTableId       = 'geneBrowserTable';
            eventManager.on("gene:search", function(e) {
                var geneWidget;
                geneWidget = new GeneWidget({
                    geneTableID    : $scope.geneTableId,
                    gene           : $scope.gene,

                });
                geneWidget.draw();
            });
        }

    };

});

function VariantWidget(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.location = '';

    //set instantiation args, must be last
    _.extend(this, args);



}

VariantWidget.prototype = {

    draw: function () {
        var _this = this;
        this._createVariantPanel();

    },

    createTreePanel:function(args){

        Ext.require([
            'Ext.tree.*',
            'Ext.data.*',
            'Ext.window.MessageBox'
        ]);

        Ext.define('Tree Model', {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'name',type: 'string'},
                {name: 'acc',type: 'string'}
            ]
        });

        var store = Ext.create('Ext.data.TreeStore', {
            model: 'Tree Model',
            proxy: {
                type: 'memory',
                data:args.data,
                reader: {
                    type: 'json'
                }
            }
        });

        var tree = Ext.create('Ext.tree.Panel', {
             header:false,
            border :false,
            autoWidth: true,
            autoHeight: true,
            renderTo: args.id,
            collapsible: false,
            useArrows: true,
            rootVisible: false,
            store: store,
            frame: false,
            hideHeaders: true,
            bodyBorder:false,

            //the 'columns' property is now 'headers'
            columns: [
                {
                    xtype: 'treecolumn', //this is so we know which column will show the tree
                    //text: 'Task',
                    flex: 2,
                    sortable: false,
                    dataIndex: 'name',
                    renderer:function (value, meta, record) {
                        var link = "http://www.sequenceontology.org/miso/current_release/term/"+record.data.acc;
                        return value+' <a href='+link+' target="_blank">'+record.data.acc+'</a>';
                    }
                }

            ],

            dockedItems: [{
                xtype: 'toolbar',
                ui: 'footer',
                frame: false,
                items: [
                    {
                        xtype: 'button',
                        text: 'Select All',
                        handler: function(){
                            tree.getRootNode().cascadeBy(function(){
                                if(this.hasChildNodes()){
                                    this.set('checked', null);
                                }else{
                                    this.set( 'checked', true );
                                }
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        text: 'Clear',
                        handler: function(){
                            tree.getRootNode().cascadeBy(function(){
                                if(this.hasChildNodes()){
                                    this.set('checked', null);
                                }else{
                                    this.set( 'checked', false );
                                }
                            });
                        }
                    }
                ]
            }],


            listeners: {
                itemclick : function(view, record, item, index, event) {

                    if(record)
                    {
                        if(record.hasChildNodes()){
                            var is_checked = record.firstChild.data.checked;
                            record.cascadeBy(function(){
                                if(is_checked == false){
                                    this.set( 'checked', true );
                                }else{
                                    this.set( 'checked', false );
                                }
                                record.set('checked', null);
                            });
                        }
                    }
                }
            }

        });

        return tree;

    },


    _createVariantPanel:function(){
       var _this = this;
       this.grid = this._createBrowserGrid();
//       this.gridFiles = _this._createFilesGrid();
//       this.gridEffect = _this._createEffectGrid();
//       this.gridStats = _this._createStatesGrid();

    },
    _createBrowserGrid:function(){

        if(jQuery("#"+this.variantTableID+" div").length){
            jQuery( "#"+this.variantTableID+" div").remove();
        }

        var _this = this;

            Ext.require([
                'Ext.grid.*',
                'Ext.data.*',
                'Ext.util.*',
                'Ext.state.*'
            ]);


            Ext.define(this.variantTableID, {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'id',type: 'string'},
                    {name: 'type',type: 'string'},
                    {name: 'chr',type: 'int'},
                    {name: 'start',type: 'int'},
                    {name: 'end',type: 'int'},
                    {name: 'length',type: 'int'},
                    {name: 'ref',type: 'string'},
                    {name: 'alt',type: 'string'},
                    {name: 'chunkIds',type: 'auto'},
                    {name: 'hgvsType',type: 'auto', mapping:'hgvs[0].type'},
                    {name: 'hgvsName',type: 'auto', mapping:'hgvs[0].name'}
                ],
                idProperty: 'id'
            });



            Ext.QuickTips.init();

            // setup the state provider, all state information will be saved to a cookie
            Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));

            Ext.Ajax.useDefaultXhrHeader = false;
            // Can also be specified in the request options
            Ext.Ajax.cors = true;


            var url = METADATA_HOST+'/'+METADATA_VERSION+'/segments/'+_this.location+'/variants?exclude=files,chunkIds'+_this.filters;
             //console.log(url)
            // create the data store
            _this.vbStore = Ext.create('Ext.data.JsonStore', {
                autoLoad: true,
                autoSync: true,
                autoLoad: {start: 0, limit: 10},
                pageSize: 10,
                remoteSort: true,
                model: this.variantTableID,
                proxy: {
                    type: 'ajax',
                    url: url,
                    startParam:"skip",
                    limitParam:"limit",
                    reader: {
                        type: 'json',
                        root: 'response.result',
                        totalProperty: 'response.numTotalResults'
                    }
                }

            });




            var variant_present = '';
            // create the Grid
            _this.vbGrid = Ext.create('Ext.grid.Panel', {
                header:false,
                store:  _this.vbStore,
                stateful: true,
                collapsible: true,
                multiSelect: true,
                //stateId: 'stateGrid',
                columns: {
                    items:[

                        {
                            text     : 'Chromosome',
                            sortable : true,
                            dataIndex: 'chr'
                        },
                        {
                            text     : 'Start',
                            sortable : true,
                            dataIndex: 'start',
                        },
                        {
                            text     : 'End',
                            sortable : true,
                            dataIndex: 'end'
                        },
                        {
                            text     : 'ID',
                            sortable : false,
                            dataIndex: 'id',
                            xtype: 'templatecolumn',
                            tpl: '<a href="?variantID={id}" target="_blank">{id}</a>'

                        },
                        {
                            text     : 'Type',
                            sortable : true,
                            //renderer : createLink,
                            dataIndex: 'type'

                        },

                        {
                            text     : 'REF/ALT',
                            sortable : true,
                            dataIndex: 'ref',
                            xtype: 'templatecolumn',
                            tpl: '<tpl if="ref">{ref}<tpl else>-</tpl>/<tpl if="alt">{alt}<tpl else>-</tpl>'

                        },
                        {
                            text     : 'HGVS Name',
                            sortable : true,
                            dataIndex: 'hgvsName',
                        }
//                        {
//                            text     : 'ChunkIDs',
//                            sortable : true,
//                            dataIndex: 'chunkIds',
//                            renderer:function(value){
//                                var chunkids ='';
//                                for (var i = 0; i < value.length; i++) {
//                                    chunkids += value[i]+'<br />';
//                                }
//                                return chunkids;
//                            }
//
//                        },

                    ],
                    defaults: {
                        flex: 1,
                        align:'center'
                    }
                },
                height: 350,
                //width: 800,
                autoWidth: true,
                autoScroll:false,
                //title: 'Variant Data',
                renderTo: this.variantTableID,
                viewConfig: {
                    enableTextSelection: true,
                    forceFit: true
                },
                deferredRender: false,
                dockedItems: [{
                    xtype: 'pagingtoolbar',
                    store: _this.vbStore,   // same store GridPanel is using
                    dock: 'bottom',
                    displayInfo: true
                }],
                listeners: {
                    itemclick : function() {
                        var data = _this._getSelectedData();
                        _this._updateEffectGrid(data.position);
                        _this._updateFilesGrid(data.variantId);
                        _this._createGenotypeGrid(data.variantId);
                        var activeTab = jQuery('#'+_this.variantSubTabsID+' .active').attr('id');
                        if(activeTab  === _this.variantGenomeViewerID+'Li'){
                            var region = new Region();
                            region.parse(data.location);
                            genomeViewer.setRegion(region);
                        }
                    },
                    render : function(grid){
                        grid.store.on('load', function(store, records, options){
                            if(_this.vbStore.getCount() > 0){
                                variant_present = 1;
                                var variantGenotypeArgs = [];

                                grid.getSelectionModel().select(0);
                                _this.gridFiles = _this._createFilesGrid();
                                _this.gridEffect = _this._createEffectGrid();
                                _this.gridStats = _this._createStatesGrid();
                                grid.fireEvent('itemclick', grid, grid.getSelectionModel().getLastSelected());
                            }else{
                                variant_present = 0;
                            }
                        });
                    }
                }
            });


            jQuery('#'+_this.variantSubTabsID+' a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
               // console.log(e)
                if(variant_present === 1){
                    var grid = _this.vbGrid
                    var data = _this._getSelectedData();
                    console.log(e.target.parentElement.id)
                    if(e.target.parentElement.id === _this.variantEffectTableID+'Li' ){
                        _this.gridEffect = _this._createEffectGrid();
                        _this._updateEffectGrid(data.position);
                    }
                    else if(e.target.parentElement.id === _this.variantFilesTableID+'Li'){
                        _this.gridFiles = _this._createFilesGrid();
                        _this.gridStats = _this._createStatesGrid();
                        _this._updateFilesGrid(data.variantId);
                    }
                    else if(e.target.parentElement.id === _this.variantGenoTypeTableID+'Li'){
                        _this._createGenotypeGrid(data.variantId);
                    }
                    else if(e.target.parentElement.id  === _this.variantGenomeViewerID+'Li'){
                        var region = new Region();
                        region.parse(data.location);
                        genomeViewer.setRegion(region);
                    }
                }

           });

        return _this.vbGrid;
    },

    _createGenotypeGrid:function(args){
        var _this = this;
        var variantGenotype = new VariantGenotypeWidget({
            url       :  METADATA_HOST+'/'+METADATA_VERSION+'/variants/'+args+'/info',
            render_id : _this.variantGenoTypeTableID,
            title: 'Genotypes',
            pageSize:10
        });
        variantGenotype.draw();

    },


    _updateFilesGrid:function(args){

        var _this = this;
        var variantId = args;
        var url = METADATA_HOST+'/'+METADATA_VERSION+'/variants/'+variantId+'/info';
        var data = this._fetchData(url);

        if(data.response.result){
           _this.gridFiles.getStore().loadData(data.response.result);
           _this.gridFiles.setHeight(150);
           _this._updateStatsGrid(data.response.result);
        }else{
          _this.gridFiles.getStore().removeAll();
        }

    },

    _updateEffectGrid:function(args){
        var _this = this;
        var position = args;
        var url = CELLBASE_HOST+'/'+CELLBASE_VERSION+'/hsa/genomic/variant/'+position+'/consequence_type?of=json';
        var data = _this._fetchData(url);

        if(data.length > 0){
            this.gridEffect.getStore().loadData(data);
        }else{
            this.gridEffect.getStore().removeAll();
        }

    },

    _createEffectGrid:function(){
        if(jQuery( "#"+this.variantEffectTableID+" div").length){
            jQuery( "#"+this.variantEffectTableID+" div").remove();
        }

        var _this = this;

        Ext.define(_this.variantEffectTableID, {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'chromosome'},
                {name: 'position'},
                {name: 'snpId'},
                {name: 'consequenceType'},
                {name: 'aminoacidChange'},
                {name: 'geneId'},
                {name: 'transcriptId'},
                {name: 'featureId'},
                {name: 'featureName'},
                {name: 'featureType'},
                {name: 'featureBiotype'}
            ]

        });


        // create the data store
        _this.veStore = Ext.create('Ext.data.JsonStore', {
            model: _this.variantEffectTableID,
            data: [],
        });

        // create the Grid
        _this.veGrid = Ext.create('Ext.grid.Panel', {
            header:false,
            store:  _this.veStore,
            stateful: true,
            collapsible: true,
            multiSelect: true,
            //stateId: 'stateGrid',
            columns:{
                items:[
                    {
                        text     : 'position',
                        //flex     : 1,
                        sortable : false,
                        dataIndex: 'position',
                        xtype: 'templatecolumn',
                        tpl: '{chromosome}:{position}'
                    },
                    {
                        text     : 'snpId',
                        sortable : true,
                        dataIndex: 'snpId'

                    },
                    {
                        text     : 'consequenceType',
                        sortable : true,
                        dataIndex: 'consequenceType',
                        renderer:function(value){
                            var soID = '<a href="http://www.sequenceontology.org/miso/current_release/term/'+value+'" target="_blank">'+value+'</a>';
                            return soID;
                        }

                    },
                    {
                        text     : 'aminoacidChange',
                        sortable : true,
                        dataIndex: 'aminoacidChange'
                    },
                    {
                        text     : 'geneId',
                        sortable : true,
                        dataIndex: 'geneId',
                        renderer:function(value){
                            var geneID = '<a href="http://www.ensembl.org/Homo_sapiens/Location/View?g='+value+'" target="_blank">'+value+'</a>';
                            return geneID;
                        }
                    },
                    {
                        text     : 'transcriptId',
                        sortable : true,
                        //renderer : createPubmedLink,
                        dataIndex: 'transcriptId',
                        renderer:function(value){
                            var transcriptID = '<a href="http://www.ensembl.org/Homo_sapiens/Location/View?t='+value+'" target="_blank">'+value+'</a>';
                            return transcriptID;
                        }

                    },
                    {
                        text     : 'featureId',
                        sortable : true,
                        //renderer : createPubmedLink,
                        dataIndex: 'featureId'

                    },
                    {
                        text     : 'featureName',
                        sortable : true,
                        //renderer : createPubmedLink,
                        dataIndex: 'featureName'

                    },
                    {
                        text     : 'featureType',
                        sortable : true,
                        //renderer : createPubmedLink,
                        dataIndex: 'featureType'

                    },
                    {
                        text     : 'featureBiotype',
                        sortable : true,
                        //renderer : createPubmedLink,
                        dataIndex: 'featureBiotype',
                        resizable: false

                    }
                ],
                defaults: {
                    flex: 1,
                    align:'center'
                }
            },
            height: 350,
//                width: 800,
            title: 'Effects',
            deferredRender: false,
            renderTo: this.variantEffectTableID,
            viewConfig: {
                enableTextSelection: true
            }
        });

        return _this.veGrid;
    },
    _createFilesGrid:function(){
        if(jQuery( "#"+this.variantFilesTableID+" div").length){
            jQuery( "#"+this.variantFilesTableID+" div").remove();
        }



        var _this = this;

        Ext.define(_this.variantFilesTableID, {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'files', type:'auto' }
            ]

        });

        var url = METADATA_HOST+'/'+METADATA_VERSION+'/segments/'+_this.location+'/variants?exclude=effects,chunkIds';

        // create the data store
        _this.vfStore = Ext.create('Ext.data.JsonStore', {
            model: _this.variantFilesTableID,
            data: [],
        });



        // create the Grid
        _this.vfGrid = Ext.create('Ext.grid.Panel', {
            header:false,
            store:  _this.vfStore,
            stateful: true,
            collapsible: true,
            multiSelect: true,
            //stateId: 'stateGrid',
            columns:{
                items:[
                    {
                        text     : 'FileID',
                        //flex     : 1,
                        sortable : false,
                        dataIndex: 'files',
                        renderer:function(value){
                            //console.log(value)
                            return value[0].fileId;
                        }
                    },
                    {
                        text     : 'StudyID',
                        //flex     : 1,
                        sortable : false,
                        dataIndex: 'files',
                        renderer:function(value){
                            return value[0].studyId;
                        }
                    },
                    {
                        text     : 'Attributes',
                        columns:[
                                    {
                                                text     : 'QUAL',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        renderer:function(value){
                                            return value[0].attributes.QUAL;
                                        }
                                    },
                                    {
                                        text     : 'FILTER',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        renderer:function(value){
                                            return value[0].attributes.FILTER;
                                        }
                                    },
                                    {
                                        text     : 'AVGPOST',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AVGPOST;
                                        }
                                    },
                                    {
                                        text     : 'RSQ',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.RSQ;
                                        }
                                    },
                                    {
                                        text     : 'LDAF',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.LDAF;
                                        }
                                    },
                                    {
                                        text     : 'ERATE',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        renderer:function(value){
                                            return value[0].attributes.ERATE;
                                        }
                                    },
                                    {
                                        text     : 'AN',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
//                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AN;
                                        }
                                    },
                                    {
                                        text     : 'VT',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.VT;
                                        }
                                    },
                                    {
                                        text     : 'AA',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
//                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AA;
                                        }
                                    },
                                    {
                                        text     : 'THETA',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.THETA;
                                        }
                                    },
                                    {
                                        text     : 'AC',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
//                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AC;
                                        }
                                    },
                                    {
                                        text     : 'SNPSOURCE',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
//                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.SNPSOURCE;
                                        }
                                    },
                                    {
                                        text     : 'AF',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
//                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AF;
                                        }
                                    },
                                    {
                                        text     : 'ASN_AF',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.ASN_AF;
                                        }
                                    },
                                    {
                                        text     : 'AMR_AF',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AMR_AF;
                                        }
                                    },
                                    {
                                        text     : 'AFR_AF',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        renderer:function(value){
                                            return value[0].attributes.AFR_AF;
                                        }
                                    },
                                    {
                                        text     : 'EUR_AF',
                                        //flex     : 1,
                                        sortable : false,
                                        dataIndex: 'files',
                                        hidden: true,
                                        //resizable: false,
                                        renderer:function(value){
                                            return value[0].attributes.EUR_AF;
                                        }
                                    }

                                ],
                                defaults: {
                                    //flex: 1,
                                    align:'center'
                                }

                    }



                ],
                defaults: {
                    flex: 1,
                    align:'center'
                }
            },
            height: 350,
            //width: 800,
            title: 'Files',
            renderTo: this.variantFilesTableID,
            viewConfig: {
                enableTextSelection: true
            },
            deferredRender: false
        });

        return _this.vfGrid;
    },

    _createStatesGrid:function(){
        jQuery( "#"+this.variantStatsViewID+" div").remove();
        var _this = this;
        var statsPanel = Ext.create('Ext.Panel', {
            header:false,
            renderTo:  _this.variantStatsViewID,
            title: 'Stats',
            height:330,
            html: '<p><i>Click the Variant to see results here</i></p>'
        });
        return statsPanel;
    },

    _updateStatsGrid:function(args){
        var _this = this;
        tpl =Ext.create('Ext.XTemplate',
            '<tpl for="files">',
                '<tpl for="stats">',
                    '<table class="grid-table">',
                        '<tr>','<td>MAF</td>', '<td>{maf}</td>','</tr>',
                        '<tr>','<td>MGF</td>','<td>{mgf}</td>','</tr>',
                        '<tr>','<td>Allele MAF</td>','<td>{alleleMaf}</td>','</tr>',
                        '<tr>','<td>Genotype MAF</td>','<td>{genotypeMaf}</td>','</tr>',
                        '<tr>','<td>miss Allele</td>','<td>{missAllele}</td>','</tr>',
                        '<tr>','<td>miss Genotypes</td>','<td>{missGenotypes}</td>','</tr>',
                        '<tr>','<td>Mendel Err</td>','<td>{missGenotypes}</td>', '</tr>',
                        '<tr>','<td>Cases Percent Dominant</td>','<td>{casesPercentDominant}</td>','</tr>',
                        '<tr>','<td>Controls Percent Dominant</td>','<td>{controlsPercentDominant}</td>', '</tr>',
                        '<tr>','<td>Cases Percent Recessive</td>','<td>{controlsPercentDominant}</td>','</tr>',
                        '<tr>','<td>Controls Percent Recessive</td>','<td>{controlsPercentRecessive}</td>','</tr>',
                    '</table>',
                    '<p>{[this._drawChart(values.genotypeCount)]}</p>',
                '</tpl>',
            '</tpl>',
            {
                _drawChart: function(args) {
                    var chartData=[];
                    for (key in args) {
                        chartData.push([key, args[key]]);
                    }
                    args['title'] = 'Genotype Count';
                    args['data'] = chartData;
                   _this._statsPieChart(args);
                }
            }
         );
         tpl.overwrite(_this.gridStats.body,args[0]);
        _this.gridStats.doComponentLayout();
    },



    _statsPieChart:function(args){
        var _this = this;
        var chart1 = new Highcharts.Chart({
            chart: {
                renderTo: _this.variantStatsChartID,
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },

            title: {
                text: args.title
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                    }
                }
            },
            series: [{
                type: 'pie',
                name: args.title,
                data: args.data
            }],
            credits: {
                enabled: false
            },
        });

    },

    _getSelectedData:function(){
        var _this = this;
        var parseData = [];
        var data =  _this.vbGrid.getSelectionModel().selected.items[0].data;
        var data = _this.vbGrid.getSelectionModel().selected.items[0].data;

         parseData['variantId'] = data.id;
         parseData['position']  = data.chr + ":" + data.start + ":" + data.ref + ":" + data.alt;
         parseData['location']  = data.chr + ":" + data.start + "-" + data.end;


        return parseData;
    },

    _fetchData:function(args){
        var data;
        $.ajax({
            url: args,
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                data = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                data = '';
            }
        });
        return data;
    }

};


function GeneWidget(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.location = '';

    //set instantiation args, must be last
    _.extend(this, args);



}

GeneWidget.prototype = {

    draw: function () {
        var _this = this;
        this._createGenePanel();

    },

    _createGenePanel:function(){
        var _this = this;
        this.grid = this._createBrowserGrid();
    },
    _createBrowserGrid:function(){

        if(jQuery("#"+this.geneTableID+" div").length){
            jQuery( "#"+this.geneTableID+" div").remove();
        }

        var _this = this;

        Ext.require([
            'Ext.grid.*',
            'Ext.data.*',
            'Ext.util.*',
            'Ext.state.*'
        ]);


        Ext.define(this.geneTableID, {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'id',type: 'string'},
                {name: 'type',type: 'string'},
                {name: 'chr',type: 'int'},
                {name: 'start',type: 'int'},
                {name: 'end',type: 'int'},
                {name: 'length',type: 'int'},
                {name: 'ref',type: 'string'},
                {name: 'alt',type: 'string'},
                {name: 'chunkIds',type: 'auto'},
                {name: 'hgvsType',type: 'auto', mapping:'hgvs[0].type'},
                {name: 'hgvsName',type: 'auto', mapping:'hgvs[0].name'},
                {name: 'geneName',type: 'string'}
            ],
            idProperty: 'id'
        });



        Ext.QuickTips.init();

        // setup the state provider, all state information will be saved to a cookie
        Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));

        Ext.Ajax.useDefaultXhrHeader = false;
        // Can also be specified in the request options
        Ext.Ajax.cors = true;

        //console.log(_this.filters);

        var url = METADATA_HOST+'/'+VERSION+'/genes/'+_this.gene+'/variants';
        console.log(url)
        // create the data store



        _this.gbStore = Ext.create('Ext.data.JsonStore', {
            model: this.geneTableID,
            data: [],
            groupField: 'geneName',

        });




        var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: '{name} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
        });


        var variant_present = '';
        // create the Grid
        _this.gbGrid = Ext.create('Ext.grid.Panel', {
            store:  _this.gbStore,
            stateful: true,
            collapsible: true,
            multiSelect: true,
            //stateId: 'stateGrid',
            features: [groupingFeature],
            columns: {
                items:[

                    {
                        text     : 'Chromosome',
                        sortable : true,
                        dataIndex: 'chr'
                    },
                    {
                        text     : 'Start',
                        sortable : true,
                        dataIndex: 'start',
                    },
                    {
                        text     : 'End',
                        sortable : true,
                        dataIndex: 'end'
                    },
                    {
                        text     : 'Length',
                        sortable : true,
                        dataIndex: 'length'

                    },
                    {
                        text     : 'ID',
                        sortable : false,
                        dataIndex: 'id'

                    },
                    {
                        text     : 'Type',
                        sortable : true,
                        //renderer : createLink,
                        dataIndex: 'type'

                    },

                    {
                        text     : 'REF/ALT',
                        sortable : true,
                        dataIndex: 'ref',
                        xtype: 'templatecolumn',
                        tpl: '{ref}/{alt}'

                    },
                    {
                        text     : 'HGVS Name',
                        sortable : true,
                        dataIndex: 'hgvsName',
                    }
//                        {
//                            text     : 'ChunkIDs',
//                            sortable : true,
//                            dataIndex: 'chunkIds',
//                            renderer:function(value){
//                                var chunkids ='';
//                                for (var i = 0; i < value.length; i++) {
//                                    chunkids += value[i]+'<br />';
//                                }
//                                return chunkids;
//                            }
//
//                        },

                ],
                defaults: {
                    flex: 1,
                    align:'center'
                }
            },
            height: 350,
            //width: 800,
            autoWidth: true,
            autoScroll:false,
            title: 'Gene Data',
            renderTo: this.geneTableID,
            viewConfig: {
                enableTextSelection: true,
                forceFit: true
            },
            deferredRender: false

        });



        var data =  _this._fetchData(url);

        var groupData = _this._groupData(data);

        console.log(groupData)

        _this.gbGrid.getStore().loadData(groupData.response.result);


        return _this.gbGrid;
    },

    _fetchData:function(args){
        var data;
        $.ajax({
            url: args,
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                data = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                data = '';
            }
        });
        return data;
    },

    _groupData:function(args){
        var data = args;

        var groupData = '';

        for (var i=0;i<data.response.result.length;i++)
        {
            data.response.result[i].geneName = data.response.result[i].effects[0].geneName;
        }
        return data;
    }



};


function VariantGenotypeWidget(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.location = '';

    //set instantiation args, must be last
    _.extend(this, args);



}

VariantGenotypeWidget.prototype = {

    draw: function () {
        var _this = this;
        this._createGenotypeGrid();
    },

    _createGenotypeGrid:function(){
        var _this = this;

        if(jQuery("#"+_this.render_id+" div").length){
            jQuery( "#"+_this.render_id+" div").remove();
        }

        Ext.Loader.setConfig({enabled: true});

        Ext.Loader.setPath('Ext.ux', 'vendor/extJS/ux');

        Ext.require([
            'Ext.data.*',
            'Ext.grid.*',
            'Ext.util.*',
            'Ext.ux.data.PagingMemoryProxy',
            'Ext.toolbar.Paging',
            'Ext.ux.SlidingPager'
        ]);

        var parsedData = _this._parseData();

        Ext.define(_this.render_id, {
            extend: 'Ext.data.Model',
            fields: parsedData.fields
        });

        _this.vgStore = Ext.create('Ext.data.Store', {
            model: _this.render_id,
            remoteSort: true,
            pageSize: _this.pageSize,
            proxy: {
                type: 'pagingmemory',
                data: parsedData.data,
                reader: {
                    type: 'array'
                }
            }

        });

        _this.vgGrid = Ext.create('Ext.grid.Panel', {
            header:false,
            store:  _this.vgStore,
            stateful: true,
            collapsible: true,
            multiSelect: true,
            columns: parsedData.columns,
            height: 350,
            autoWidth: true,
            autoScroll:false,
            title:  _this.title,
            renderTo: _this.render_id,
            viewConfig: {
                enableTextSelection: true,
                forceFit: true
            },
            deferredRender: false,
            bbar: Ext.create('Ext.PagingToolbar', {
                pageSize: 10,
                store:  _this.vgStore,
                displayInfo: true,
                plugins: Ext.create('Ext.ux.SlidingPager', {})
            })
        });

        _this.vgStore.load();

        return _this.vgGrid;



    },

    _parseData:function(){

        var _this = this;
        var data = [];
        var dataArray=[];

        var tmpData =  _this._fetchData(_this.url);
//        var formatTempArr = tmpData.response.result[0].files[0].format.split(":").sort();
//        var formatArr = formatTempArr.reverse();
        var columnData = [];


        var tmpSmplData = tmpData.response.result[0].files[0].samples;

        for (key in tmpSmplData) {
            var tempArray= new Array();
            var columnData = new Array();
            tempArray.push(key);
            for(k in tmpSmplData[key]){
                columnData.push(k);
                tempArray.push(tmpSmplData[key][k]);
            }
            dataArray.push(tempArray);
        }
        var dataFields = [];
        var dataColumns = [];

        dataFields.push('Samples');
        var dataColumns = [{text:'Samples',flex:1,sortable:false,dataIndex:'Samples',align:'center'}];

        for(key in columnData ){
            dataFields.push(columnData[key]);
            dataColumns.push({text:columnData[key],flex:1,sortable:false,dataIndex:columnData[key],align:'center'})
        }

        data['data'] = dataArray;
        data['fields'] = dataFields;
        data['columns'] = dataColumns;

        return data;
    },

    _fetchData:function(args){
        var data;
        $.ajax({
            url: args,
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                data = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                data = '';
            }
        });
        return data;
    }


};

angular.module('ebiVar.Services.Metadata', []).service('ebiVarMetadataService', function($http) {

    this.fetchData = function(args) {

        var url = args;
        $.ajax({
            url: url,
            //url: 'http://localhost:8080/ws-test/rest/test/study/estd199',
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                data = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //console.log(textStatus)
                data = '';
            }
        });

        return data;

    };

});