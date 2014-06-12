/**
 * Created by jag on 23/05/2014.
 */
function VariantEffectsWidget(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.location = '';

    //set instantiation args, must be last
    _.extend(this, args);



}

VariantEffectsWidget.prototype = {

    draw: function () {
        var _this = this;
        this._createEffectsGrid();
    },

    _createEffectsGrid:function(){

        var _this = this;

        _this.targetId = (_this.render_id) ? _this.render_id : _this.targetId;
        _this.targetDiv = (_this.targetId instanceof HTMLElement ) ? _this.targetId : $('#' + _this.targetId)[0];

        if (_this.targetDiv === "undefined" || _this.targetDiv == null ) {
            console.log('targetId not found');
            return;
        }

        _this._clear();



        Ext.require([
            'Ext.grid.*',
            'Ext.data.*',
            'Ext.util.*',
            'Ext.state.*'
        ]);

        Ext.Ajax.useDefaultXhrHeader = false;
        // Can also be specified in the request options
        Ext.Ajax.cors = true;

        Ext.define(_this.render_id, {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'chromosome'},
                {name: 'position'},
                {name: 'snpId'},
                {name: 'consequenceType'},
                {name: 'consequenceTypeObo'},
                {name: 'aminoacidChange'},
                {name: 'geneId'},
                {name: 'transcriptId'},
                {name: 'featureId'},
                {name: 'featureName'},
                {name: 'featureType'},
                {name: 'featureBiotype'}
            ]

        });



        var url = CELLBASE_HOST+'/'+CELLBASE_VERSION+'/hsa/genomic/variant/'+_this.position+'/consequence_type?of=json';

        //alert('sdf')

        // create the data store
        _this.veStore = Ext.create('Ext.data.JsonStore', {
            autoLoad: true,
            autoSync: true,
            model: _this.render_id,
            proxy: {
                type: 'ajax',
                url: url,
                reader: {
                    type: 'json',
                    root: '',
                }
            }

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
                        text     : 'consequenceTypeObo',
                        sortable : true,
                        dataIndex: 'consequenceTypeObo'

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
                    align:'center',
                    baseCls:'customHeader'
                }
            },
            height: 350,
//                width: 800,
            title: 'Effects',
            deferredRender: false,
            renderTo:  _this.render_id,
            viewConfig: {
                enableTextSelection: true,
                emptyText: 'No Record to Display',
                deferEmptyText: false
            }
        });


        return _this.veGrid;

    },

    _clear:function(){
        var _this = this;
        $( "#"+_this.render_id).empty();
    },



};
