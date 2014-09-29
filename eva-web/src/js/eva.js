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

        /* Templates */
        $(this.templatesDiv).addClass('eva-child');
        this.childDivMenuMap['Templates'] = this.templatesDiv;

        /* VCF */
        $(this.vcfDiv).addClass('eva-child');
        this.childDivMenuMap['VCF'] = this.vcfDiv;

        /* studyView */
        $(this.studyView).addClass('eva-child');
        this.childDivMenuMap['eva-study'] = this.studyView;
        this.childDivMenuMap['dgva-study'] = this.studyView;

        /* variantView */
        $(this.variantView).addClass('eva-child');
        this.childDivMenuMap['variant'] = this.variantView;

        /* beacon */
        $(this.beacon).addClass('eva-child');
        this.childDivMenuMap['Beacon'] = this.beacon;

        /* clinical */
        $(this.clinicalDiv).addClass('eva-child');
        this.childDivMenuMap['EVA Clinical'] = this.clinicalDiv;

        /* submision-start */
        $(this.submissionForm).addClass('eva-child');
        this.childDivMenuMap['submission-start'] = this.submissionForm;


        /* Study Browser Panel*/
        $(this.studyBrowserDiv).addClass('eva-child');
        this.childDivMenuMap['Study Browser'] =  this.studyBrowserDiv;

        /* variant browser option*/
        $(this.evaBrowserDiv).addClass('eva-child');
        this.childDivMenuMap['EVA Browser'] = this.evaBrowserDiv;

        /* genome browser option*/
//        this.genomeBrowserOptionDiv = document.createElement('div');
//        $(this.genomeBrowserOptionDiv).addClass('eva-child genome-browser-option-div');
//        this.div.appendChild(this.genomeBrowserOptionDiv);
//        this.childDivMenuMap['Genome Browser'] = this.genomeBrowserOptionDiv;
//
//        this.formPanelGenomeFilterDiv = document.createElement('div');
//        $(this.formPanelGenomeFilterDiv).addClass('form-panel-genome-filter-div');
//        this.genomeBrowserOptionDiv.appendChild(this.formPanelGenomeFilterDiv);
//
//        this.genomeViewerTitleDiv = document.createElement('div');
//        $(this.genomeViewerTitleDiv).addClass('genome-viewer-title-div eva-header-1');
//        this.genomeBrowserOptionDiv.appendChild(this.genomeViewerTitleDiv);
//        this.genomeViewerTitleDiv.innerHTML = 'Genome browser';
//
//        this.genomeViewerDiv = document.createElement('div');
//        $(this.genomeViewerDiv).addClass('genome-viewer-div');
//        this.genomeBrowserOptionDiv.appendChild(this.genomeViewerDiv);
//
//        this.genomeViewer = this._createGenomeViewer(this.genomeViewerDiv);
//        this.formPanelGenomeFilter = this._createFormPanelGenomeFilter(this.formPanelGenomeFilterDiv);


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
        var contentDiv = document.querySelector('#content');
        this.studyBrowserPanel  = this._createStudyBrowserPanel(contentDiv);
        this.variantWidgetPanel  = this._createVariantWidgetPanel(contentDiv);

        // this.formPanelVariantFilter.draw();
        // this.genomeViewer.draw();
        // this.formPanelGenomeFilter.draw();
        this.select('Home');

    },
    select: function (option) {
        this.evaMenu.select(option);
        this._selectHandler(option);
    },
    _selectHandler: function (option) {
        var _this = this;
        this.studyBrowserPanel.hide();
        this.variantWidgetPanel.hide();
        $('body').find('.eva-child').each(function (index, el) {
            _this.div.removeChild(el)
        });
        if (this.childDivMenuMap[option]) {
            this.div.appendChild(this.childDivMenuMap[option]);
        }

        switch (option) {
            case 'Home':
                _this._twitterWidgetUpdate();
                break;
            case 'Study Browser':
                this.studyBrowserPanel.show();
                break;
            case 'VCF Browser':
                this.variantWidgetPanel.show();
                break;
//            case 'Genome Browser':
//                this.formPanelGenomeFilter.update();
//                break;
        }
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
    _createStudyBrowserPanel: function(target){
        var studyBrowser = new StudyBrowser({
            target: target
        });
        studyBrowser.draw();
        return studyBrowser;

    },
    _createVariantWidgetPanel: function(target){
        var variantWidget= new EvaVariantWidgetPanel({
            target: target
        });
        variantWidget.draw();
        return variantWidget;

    },
    _createFormPanelGenomeFilter: function (target) {
        var _this = this;
        var positionFilter = new PositionFilterFormPanel();
        var studyFilter = new StudyFilterFormPanel({
            studiesStore: this.studiesStore
        });
//        _this._loadListStudies();
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
                        _this.EvaAdapter(title, e.values);
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
    _twitterWidgetUpdate : function (){

        var twitterWidgetEl = document.getElementById('twitter-widget');
        twitterWidgetEl.innerHTML = "";
        twitterWidgetEl.innerHTML = '<a  class="twitter-timeline" height=100 href="https://twitter.com/EBIvariation"  data-widget-id="437894469380100096">Tweets by @EBIvariation</a>';
        $.getScript('//platform.twitter.com/widgets.js', function(){
            twttr.widgets.load();
        });
    }
}
