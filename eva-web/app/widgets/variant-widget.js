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

    _createVariantPanel:function(){
        var _this = this;
       this.grid = this._createBrowserGrid();
       this.gridEffect = this._createEffectGrid();
       this.gridFiles = this._createFilesGrid();
       this.gridStats = this._createStatesGrid();

    },
    _createBrowserGrid:function(){
        jQuery( "#"+this.variantTableID+" div").remove();
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

            var url = METADATA_HOST+'/'+VERSION+'/segments/'+_this.location+'/variants?exclude=files,effects';

            // create the data store
            _this.vbStore = Ext.create('Ext.data.JsonStore', {
                autoLoad: true,
                autoSync: true,
                model: this.variantTableID,
                proxy: {
                    type: 'ajax',
                    url:url,
                    reader: {
                        type: 'json',
                        root: 'response.result'
                    }
                }

            });



            // create the Grid
            _this.vbGrid = Ext.create('Ext.grid.Panel', {
                store:  _this.vbStore,
                stateful: true,
                collapsible: true,
                multiSelect: true,
                //stateId: 'stateGrid',
                columns: {
                    items:[
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
                            text     : 'REF',
                            sortable : true,
                            dataIndex: 'ref'

                        },
                        {
                            text     : 'ALT',
                            sortable : true,
                            dataIndex: 'alt'

                        },
                        {
                            text     : 'ChunkIDs',
                            sortable : true,
                            dataIndex: 'chunkIds',
                            renderer:function(value){
                                var chunkids ='';
                                for (var i = 0; i < value.length; i++) {
                                    chunkids += value[i]+'<br />';
                                }
                                return chunkids;
                            }

                        },
                        {
                            text     : 'HGVS',
                            columns:[
                                    {
                                        text     : 'Type',
                                        sortable : true,
                                        dataIndex: 'hgvsType'
                                    },
                                    {
                                        text     : 'Name',
                                        sortable : true,
                                        dataIndex: 'hgvsName',
                                        resizable: false

                                    }
                            ]

                        }
                    ],
                    defaults: {
                        flex: 1
                    }
                },
                height: 350,
                //width: 800,
                autoWidth: true,
                autoScroll:false,
                title: 'Variant Browser',
                renderTo: this.variantTableID,
                viewConfig: {
                    enableTextSelection: true,
                    forceFit: true
                },
                listeners: {
                    itemclick : function() {
                        var data =  _this.vbGrid.getSelectionModel().selected.items[0].data;
                        var variantId = data.id;
                        var position = data.chr + ":" + data.start + ":" + data.ref + ":" + data.alt;
                        _this._updateEffectGrid(position);
                        _this._updateFilesGrid(variantId);
                    }
                }
            });

        return _this.vbGrid;
    },

    _updateFilesGrid:function(args){
        var _this = this;
        var variantId = args;
        var url = METADATA_HOST+'/'+VERSION+'/variants/'+variantId+'/info';
        var data = this._fetchData(url);

        if(data.response.result){
           _this.gridFiles.getStore().loadData(data.response.result);
        }else{
          _this.gridFiles.getStore().removeAll();
        }

    },

    _updateEffectGrid:function(args){
        var _this = this;
        var position = args;
        var url = 'http://ws-beta.bioinfo.cipf.es/cellbase-staging/rest/latest/hsa/genomic/variant/'+position+'/consequence_type?of=json';
        var data = _this._fetchData(url);



        if(data.length > 0){
            this.gridEffect.getStore().loadData(data);
        }else{
            this.gridEffect.getStore().removeAll();
        }

    },

    _createEffectGrid:function(){

        jQuery( "#"+this.variantEffectTableID+" div").remove();
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
                        dataIndex: 'consequenceType'
                    },
                    {
                        text     : 'aminoacidChange',
                        sortable : true,
                        dataIndex: 'aminoacidChange'
                    },
                    {
                        text     : 'geneId',
                        sortable : true,
                        dataIndex: 'geneId'
                    },
                    {
                        text     : 'transcriptId',
                        sortable : true,
                        //renderer : createPubmedLink,
                        dataIndex: 'transcriptId'

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
                    flex: 1
                }
            },
            height: 350,
//                width: 800,
            title: 'VariantEffect',
            renderTo: this.variantEffectTableID,
            viewConfig: {
                enableTextSelection: true
            }
        });

        return _this.veGrid;
    },
    _createFilesGrid:function(){

        jQuery( "#"+this.variantFilesTableID+" div").remove();
        var _this = this;

        Ext.define(_this.variantFilesTableID, {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'files', type:'auto' }
            ]

        });

        var url = METADATA_HOST+'/'+VERSION+'/segments/'+_this.location+'/variants';

        // create the data store
        _this.vfStore = Ext.create('Ext.data.JsonStore', {
            model: _this.variantFilesTableID,
            autoLoad: true,
            autoSync: true,
            proxy: {
                type: 'ajax',
                url:url,
                reader: {
                    type: 'json',
                    root: 'response.result'
                }
            }

        });



        // create the Grid
        _this.vfGrid = Ext.create('Ext.grid.Panel', {
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

                                ]

                    }



                ],
                defaults: {
                    flex: 1
                }
            },
            height: 350,
            //width: 800,
            title: 'VariantFiles',
            renderTo: this.variantFilesTableID,
            viewConfig: {
                enableTextSelection: true
            },
            tbar: [{
                text: 'Apply Template',
                listeners: {
                    click: function() {
                        _this._updateStatsGrid();
                    }
                }
            }]
        });

        return _this.vfGrid;
    },

    _createStatesGrid:function(){
        jQuery( "#"+this.variantStatsViewID+" div").remove();
        var _this = this;
        var statsPanel = Ext.create('Ext.Panel', {
            renderTo:  _this.variantStatsViewID,
            title: 'Variant Stats',
            height:350,
            html: '<p><i>Click the Variant to see results here</i></p>'
        });
        return statsPanel;
    },

    _updateStatsGrid:function(){
        var data = {
            name: 'Abe Elias',
            company: 'Sencha Inc',
            address: '525 University Ave',
            city: 'Palo Alto',
            state: 'California',
            zip: '44102',
            kids: [{
                name: 'Solomon',
                age:3
            },{
                name: 'Rebecca',
                age:2
            },{
                name: 'Rebecca 1',
                age:0
            }]
        };

        var _this = this;
        tpl =Ext.create('Ext.XTemplate',
            '<p>Name: {name}</p>',
            '<p>Company: {company}</p>',
            '<p>Location: {city}, {state}</p>',
            '<p>Kids: ',
            '<tpl for="kids">',
                '<p> kid - {name}</p>',
            '</tpl></p>',
//            '<div id="container1"></div>',
            '<div>{[this.formatScore()]}</div>',
            {
                formatScore: function() {
                   _this._statsPieChart();
                }
            }
         );
         tpl.overwrite(_this.gridStats.body,data);
        _this.gridStats.doComponentLayout();
    },



    _statsPieChart:function(){
        var _this = this;
        var chart1 = new Highcharts.Chart({
            chart: {
                renderTo: _this.variantStatsChartID,
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },

            title: {
                text: 'Browser market shares at a specific website, 2010'
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
                name: 'Browser share',
                data: [
                    ['Firefox',   45.0],
                    ['IE',       26.8],
                    {
                        name: 'Chrome',
                        y: 12.8,
                        sliced: true,
                        selected: true
                    },
                    ['Safari',    8.5],
                    ['Opera',     6.2],
                    ['Others',   0.7]
                ]
            }]
        });

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

