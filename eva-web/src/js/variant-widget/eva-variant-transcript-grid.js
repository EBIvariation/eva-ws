/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
 *
 * This file is part of JSorolla.
 *
 * JSorolla is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSorolla is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSorolla. If not, see <http://www.gnu.org/licenses/>.
 */
function EvaVariantTranscriptGrid(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("VariantTranscriptGrid");

    this.autoRender = true;
    this.storeConfig = {};
    this.gridConfig = {};
    this.height = 500;
    this.target;
    this.margin = '5 0 0 20';
//    this.title = 'Transcripts';
    this.headerConfig = {
        baseCls: 'eva-header-1'
    };
    this.columns = [
        {
            text: "ID",
            dataIndex: "id",
            flex: 1
        },
        {
            text: "Name",
            dataIndex: "name",
            flex: 1
        },
        {
            text: "Biotype",
            dataIndex: "biotype",
            flex: 1
        },
        {
            text: "ProteinID",
            dataIndex: "proteinID",
            flex: 1
        }
    ];
    this.attributes = [
        {name: 'id', type: 'string'},
        {name: "chromosome", type: "string"}
    ];

    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render(this.targetId);
    }
}

EvaVariantTranscriptGrid.prototype = {
    render: function () {
        var _this = this;

        //HTML skel
        this.div = document.createElement('div');
        this.div.setAttribute('id', this.id);

        this.chartDiv = document.createElement('div');
        $(this.chartDiv).css({
            'height': '200px'
        });

        this.panel = this._createPanel();

    },
    draw: function () {
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);
        this.panel.render(this.div);
    },

    clear: function () {
        this.store.removeAll();
    },
    load: function (data) {

        var _this = this;


        _this.grid.setLoading(true);
        _this.clear();
        this.store.loadData(data.transcripts);



        this.trigger("load:finish", {sender: _this})
        this.grid.setLoading(false);
    },
    _createPanel: function () {
        var _this = this;

        this.model = Ext.define('Variant', {
            extend: 'Ext.data.Model',
            idProperty: 'iid',
            fields: this.attributes
        });



        this.store = Ext.create('Ext.data.Store', {
//                pageSize: this.pageSize,
                model: this.model,
                remoteSort: true,
                proxy: {
                    type: 'memory',
                    enablePaging: true
                },
                listeners: {
                    beforeload: function (store, operation, eOpts) {
                        _this.trigger("variant:clear", {sender: _this});
                    }
                }

            }
        );


        this.grid = Ext.create('Ext.grid.Panel', {
                title: this.title,
                store: this.store,
//                cls:'studybrowser',
                header: this.headerConfig,
                margin:this.margin,
                loadMask: true,
                columns: this.columns,
                animCollapse: false,
                collapsible:false,
                features: [
                    {ftype: 'summary'}
                ],
                viewConfig: {
                    emptyText: 'No records to display',
                    enableTextSelection: true,
                },
//                tbar: this.paging
            }
        );


//        this.grid = Ext.create('Ext.grid.Panel', gridArgs);

//        var panel = Ext.create('Ext.container.Container', {
//            layout: {
//                type: 'vbox',
//                align: 'stretch'
//            },
//            overflowY: true,
//            padding: 10,
//            items: [
//                this.grid
//            ]
//        });
        return this.grid;


    }
};
