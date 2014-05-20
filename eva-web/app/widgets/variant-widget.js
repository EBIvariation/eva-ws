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
                                _this._clearGrid();
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
                }else{

                    _this._clearGrid();

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
        if(jQuery( "#"+this.variantEffectTableID+" div").length > 0){
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
        if(jQuery( "#"+this.variantFilesTableID+" div").length > 0){
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
        if(jQuery( "#"+this.variantStatsViewID+" div").length > 0){
            jQuery( "#"+this.variantStatsViewID+" div").remove();
        }

        var _this = this;
        var statsPanel = Ext.create('Ext.Panel', {
            header:false,
            renderTo:  _this.variantStatsViewID,
            title: 'Stats',
            height:330,
            html: '<p><i></i></p>'
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
    },

    _clearGrid:function(){
         var _this = this;
        _this.gridFiles = _this._createFilesGrid();
        _this.gridEffect = _this._createEffectGrid();
        _this.gridStats = _this._createStatesGrid();
        _this._createGenotypeGrid();
        _this._statsPieChart('');
    }

};

