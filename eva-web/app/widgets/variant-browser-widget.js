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
//                    renderer:function (value, meta, record) {
//                        var link = "http://www.sequenceontology.org/miso/current_release/term/"+record.data.acc;
//                        return value+' <a href='+link+' target="_blank">'+record.data.acc+'</a>';
//                    }
                },
                {
                    text: '',
                    flex: 1,
                    dataIndex: 'acc',
                    renderer:function (value, meta, record) {
                        var link = "http://www.sequenceontology.org/miso/current_release/term/"+value;
                        return ' <a href='+link+' target="_blank">'+value+'</a>';
                    }

                }

            ],

//            dockedItems: [{
//                xtype: 'toolbar',
//                ui: 'footer',
//                frame: false,
//                items: [
//                    {
//                        xtype: 'button',
//                        text: 'Select All',
//                        handler: function(){
//                            tree.getRootNode().cascadeBy(function(){
//                                if(this.hasChildNodes()){
//                                    this.set('checked', null);
//                                }else{
//                                    this.set( 'checked', true );
//                                }
//                            });
//                        }
//                    },
//                    {
//                        xtype: 'button',
//                        text: 'Clear',
//                        handler: function(){
//                            tree.getRootNode().cascadeBy(function(){
//                                if(this.hasChildNodes()){
//                                    this.set('checked', null);
//                                }else{
//                                    this.set( 'checked', false );
//                                }
//                            });
//                        }
//                    }
//                ]
//            }],


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
                columns: {
                    items:[
                        {
                            text     : 'ID',
                            sortable : false,
                            dataIndex: 'id',
                            xtype: 'templatecolumn',
                            tpl: '<tpl if="id"><a href="?variantID={id}" target="_blank">{id}</a><tpl else>-</tpl>'

                        },
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
                        },
                        {
                            text     : 'View',
                            sortable : true,
                            dataIndex: 'id',
                            xtype: 'templatecolumn',
                            tpl: '<tpl><a href="?home"><img class="grid-img" src="img/eva_logo.png"/></a>&nbsp;<a href="?home"><img alt="" src="http://static.ensembl.org/i/search/ensembl.gif"></a></tpl>'
                        }

                    ],
                    defaults: {
                        flex: 1,
                        align:'center'
                    }
                },
//                height: 350,
                //width: 800,
                autoHeight:true,
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

                        var variantId = data.variantId;
                        if (!variantId) {
                            variantId = data.position;
                        }

                        eventManager.trigger("variant:files", variantId);
                        _this._createGenotypeGrid(variantId);
                        _this._createEffectsGrid(data.position);
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
                               // _this.gridEffect = _this._createEffectGrid();
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
                    if(e.target.parentElement.id === _this.variantEffectTableID+'Li' ){
                        _this._createEffectsGrid(data.position);
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
                   // _this._clearGrid();

                }

           });

        return _this.vbGrid;
    },

    _createGenotypeGrid:function(args){
        var _this = this;
        var variantGenotype = new VariantGenotypeWidget({
            variantId       : args,
            render_id : _this.variantGenoTypeTableID,
            title: 'Genotypes',
            pageSize:10
        });
        variantGenotype.draw();

    },
    _createEffectsGrid:function(args){
        var _this = this;
        var variantEffects = new VariantEffectsWidget({
            position : args,
            render_id : _this.variantEffectTableID,
            title     : 'Effects'
        });
        variantEffects.draw();

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


    _clearGrid:function(){
         var _this = this;
        _this._createGenotypeGrid();
        _this._createEffectsGrid();

    }

};

