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



        var tempData;
        evaManager.get({
            category: 'genes',
            resource: 'variants',
            params: {
                of: 'json'
            },
            query: _this.gene,
            async: false,
            success: function (data) {
                tempData = data;
            },
            error: function (data) {
                console.log('Could not get variant info');
            }
        });
        console.log(_this)

        var groupData = _this._groupData(tempData);

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

