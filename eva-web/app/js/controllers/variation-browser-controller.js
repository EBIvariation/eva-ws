/**
 * Created by jag on 17/03/2014.
 */

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

    var location = '1:5000-3500000';
    var location = '21:9411240-9411260';
    var gene = 'TMEM51';

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

