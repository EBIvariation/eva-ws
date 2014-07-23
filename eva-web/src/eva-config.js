
consequenceTypes = [
    {
        name:'Transcript Variant',
        cls: "parent",
        expanded: true,
        leaf: false,
        checked:false,
        iconCls :'no-icon',
        children:[{
            name: 'Coding Variant',
            cls: "parent",
            leaf: false ,
            iconCls :'no-icon',
            expanded: true,
            checked:false,
            children: [
                {acc: 'SO:0001587', name: 'stop_gained', qtip: 'A sequence variant whereby at least one base of a codon is changed, resulting in a premature stop codon, leading to a shortened transcript',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001578', name: 'stop_lost', qtip: 'A sequence variant where at least one base of the terminator codon (stop) is changed, resulting in an elongated transcript',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001821', name: 'inframe_insertion', qtip: 'An inframe non synonymous variant that inserts bases into in the coding sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001822', name: 'inframe_deletion', qtip: 'An inframe non synonymous variant that deletes bases from the coding sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001589', name: 'frameshift_variant', qtip: 'A sequence variant which causes a disruption of the translational reading frame, because the number of nucleotides inserted or deleted is not a multiple of three',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001621', name: 'NMD_transcript_variant', qtip: 'A variant in a transcript that is the target of NMD',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001582', name: 'initiator_codon_variant', qtip: 'A codon variant that changes at least one base of the first codon of a transcript',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001626', name: 'incomplete_terminal_codon_variant', qtip: 'A sequence variant where at least one base of the final codon of an incompletely annotated transcript is changed',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001583', name: 'missense_variant', qtip: 'A sequence variant, that changes one or more bases, resulting in a different amino acid sequence but where the length is preserved',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001819', name: 'synonymous_variant', qtip: 'A sequence variant where there is no resulting change to the encoded amino acid',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001567', name: 'stop_retained_variant', qtip: 'A sequence variant where at least one base in the terminator codon is changed, but the terminator remains',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001580', name: 'coding_sequence_variant', qtip: 'A sequence variant that changes the coding sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001907', name: 'feature_elongation', qtip: 'A sequence variant that causes the extension of a genomic feature, with regard to the reference sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                {acc: 'SO:0001906', name: 'feature_truncation', qtip: 'A sequence variant that causes the reduction of a genomic feature, with regard to the reference sequence',leaf: true,checked: false,  iconCls :'no-icon' }

            ]
        },
            {

                name: 'Non-coding Variant',
                cls: "parent",
                leaf: false ,
                iconCls :'no-icon',
                expanded: true,
                checked:false,
                children: [
                    {acc: 'SO:0001623', name: '5_prime_UTR_variant', qtip: 'A UTR variant of the 5\' UTR',leaf: true,checked: false,  iconCls :'no-icon'},
                    {acc: 'SO:0001624', name: '3_prime_UTR_variant', qtip: 'A UTR variant of the 3\' UTR',leaf: true, checked: false,  iconCls :'no-icon'},
                    {acc: 'SO:0001792', name: 'non_coding_exon_variant', qtip: 'A sequence variant that changes non-coding exon sequence',leaf: true,checked: false,  iconCls :'no-icon' },
                    {acc: 'SO:0001619', name: 'nc_transcript_variant', qtip: 'A transcript variant of a non coding RNA',leaf: true,checked: false,  iconCls :'no-icon' },
                    {acc: 'SO:0001627', name: 'intron_variant', qtip: 'A transcript variant occurring within an intron',leaf: true,checked: false,  iconCls :'no-icon' }

                ]

            },
            {

                name: 'Splice Variant',
                cls: "parent",
                leaf: false ,
                iconCls :'no-icon',
                expanded: true,
                checked:false,
                children: [
                    {acc: 'SO:0001575', name: 'splice_donor_variant', qtip: 'A splice variant that changes the 2 base region at the 5\' end of an intron',leaf: true,checked: false,  iconCls :'no-icon' },
                    {acc: 'SO:0001574', name: 'splice_acceptor_variant', qtip: 'A splice variant that changes the 2 base region at the 3\' end of an intron',leaf: true,checked: false,  iconCls :'no-icon' },
                    {acc: 'SO:0001630', name: 'splice_region_variant', qtip: 'A sequence variant in which a change has occurred within the region of the splice site, either within 1-3 bases of the exon or 3-8 bases of the intron',leaf: true,checked: false,  iconCls :'no-icon' }

                ]

            },

            {acc: 'SO:0001893', name: 'transcript_ablation', qtip: 'A feature ablation whereby the deleted region includes a transcript feature',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001889', name: 'transcript_amplification', qtip: 'A feature amplification of a region containing a transcript',leaf: true,checked: false,  iconCls :'no-icon' }

        ]

    },
    {
        name:'Regulatory Variant ',
        cls: "parent",
        expanded: true,
        leaf: false,
        checked:false,
        iconCls :'no-icon',
        children: [
            {acc: 'SO:0001782', name: 'TF_binding_site_variant', qtip: 'A sequence variant located within a transcription factor binding site',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001895', name: 'TFBS_ablation', qtip: 'A feature ablation whereby the deleted region includes a transcription factor binding site',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001892', name: 'TFBS_amplification', qtip: 'A feature amplification of a region containing a transcription factor binding site',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001620', name: 'mature_miRNA_variant', qtip: 'A transcript variant located with the sequence of the mature miRNA',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001566', name: 'regulatory_region_variant', qtip: 'A sequence variant located within a regulatory region',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001894', name: 'regulatory_region_ablation', qtip: 'A feature ablation whereby the deleted region includes a regulatory region',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001891', name: 'regulatory_region_amplification', qtip: 'A feature amplification of a region containing a regulatory region',leaf: true,checked: false,  iconCls :'no-icon' }
        ]
    },
    {
        name:'Intergenic Variant',
        cls: "parent",
        expanded: true,
        leaf: false,
        checked:false,
        iconCls :'no-icon',
        children: [
            {acc: 'SO:0001631', name: 'upstream_gene_variant', qtip: 'A sequence variant located 5\' of a gene',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001632', name: 'downstream_gene_variant', qtip: 'A sequence variant located 3\' of a gene',leaf: true,checked: false,  iconCls :'no-icon' },
            {acc: 'SO:0001628', name: 'intergenic_variant', qtip: 'A sequence variant located in the intergenic region, between genes',leaf: true,checked: false,  iconCls :'no-icon' }
        ]
    }

];
variationClasses = [
    {
        name:'Variation',
        cls: "parent",
        expanded: true,
        leaf: false,
        children: [
            {acc: "SO:0001483", name: "SNV", qtip: "SNVs are single nucleotide positions in genomic DNA at which different sequence alternatives exist.", call: "Variation",leaf: true,checked: false,iconCls :'no-icon'},
            {acc: "SO:1000032", name: "indel", qtip: "A sequence alteration which included an insertion and a deletion, affecting 2 or more bases.", call: "Variation",leaf: true,checked: false,  iconCls :'no-icon'},
            {acc: "SO:1000002", name: "substitution", qtip: "A sequence alteration where the length of the change in the variant is the same as that of the reference.", call: "Variation",leaf: true,checked: false,  iconCls :'no-icon'},
            {acc: "SO:0000667", name: "insertion", qtip: "The sequence of one or more nucleotides added between two adjacent nucleotides in the sequence.", call: "Variation", leaf: true,checked: false,  iconCls :'no-icon'},
            {acc: "SO:0001059", name: "sequence_alteration", qtip: "A sequence_alteration is a sequence_feature whose extent is the deviation from another sequence.", call: "Variation", leaf: true,checked: false,  iconCls :'no-icon'},
            {acc: "SO:0000705", name: "tandem_repeat", qtip: "Two or more adjcent copies of a region (of length greater than 1).", call: "Variation", leaf: true,checked: false,  iconCls :'no-icon'},
            {acc: "SO:0000159", name: "deletion", qtip: "The point at which one or more contiguous nucleotides were excised.", call: "Variation" , leaf: true,checked: false,  iconCls :'no-icon'}
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
    {acc: "SO:0000051", name: "probe", qtip: "A DNA sequence used experimentally to detect the presence or absence of a complementary nucleic acid.", call: "CNV probe", leaf: true,checked: false,  iconCls :'no-icon'}
];

var projects = [
    {projectId: "PRJEB4019", alias: "1000g", title: "1000 Genomes"},
    {projectId: "PRJEB5439", alias: "evs", title: "Exome Variant Server NHLBI Exome Sequencing Project"},
    {projectId: "PRJEB5829", alias: "gonl", title: "Genome of the Netherlands (GoNL) Release 5"},
    {projectId: "PRJEB6040", alias: "uk10k", title: "UK10K"},
    {projectId: "PRJEB6042", alias: "geuvadis", title: "GEUVADIS Genetic European Variation in Disease"}
   ];
