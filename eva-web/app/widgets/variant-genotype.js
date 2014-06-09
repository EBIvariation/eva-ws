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


        var parsedData = _this._parseData(_this.data);

        if(!parsedData){
            return;
        }

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
            title:  _this.study,
            renderTo: _this.render_id,
            viewConfig: {
                enableTextSelection: true,
                forceFit: true,
                emptyText: 'No Record to Display',
                deferEmptyText: false
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

    _parseOldData:function(){

        var _this = this;
        var data = [];
        var dataArray=[];

        var tmpData;
        if(_this.variantId){
            evaManager.get({
                category: 'variants',
                resource: 'info',
                params: {
                    of: 'json'
                },
                query: _this.variantId,
                async: false,
                success: function (data) {
                    tmpData = data;
                },
                error: function (data) {
                    console.log('Could not get variant info');
                }
            });

        }else{
            return;
        }
        var columnData = [];

        if(!tmpData.response.numResults){
           return;
        }
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

    _parseData:function(args){

        var _this = this;
        var data = [];
        var dataArray=[];


        var columnData = [];

        if(!args.samples){
            return;
        }
        var tmpSmplData = args.samples;



        for (var key in tmpSmplData) {
            var tempArray= new Array();
            var columnData = new Array();
            tempArray.push(tmpSmplData[key]);
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

    }


};