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
function SgvStudyBrowserPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("EvaStudyBrowserPanel");

    this.target;
    this.title = "Study Browser";
    this.height = 800;
    this.autoRender = true;
//    this.studies = [];
//    this.studiesStore;
    this.border = false;
    this.speciesList = [
        {
            assembly: "GRCh37.p7",
            common: "human",
            id: "extModel256-1",
            sciAsembly: "Homo sapiens (GRCh37.p7)",
            scientific: "Homo sapiens",
            species: "hsa"
        }
    ];

    this.studiesStore = Ext.create('Ext.data.Store', {
        pageSize: 20,
        proxy: {
            type: 'memory'
        },
        fields: [
            {name: 'id', type: 'string'},
            {name: 'name', type: 'string'}
        ],
        autoLoad: false
    });

    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render();
    }

    this.load();
}

SgvStudyBrowserPanel.prototype = {
    render: function () {
        if(!this.rendered) {
            this.div = document.createElement('div');
            this.div.setAttribute('id', this.id);
            this.panel = this._createPanel();
            this.rendered = true;
        }
    },

    draw: function () {
        if(!this.rendered) {
            this.render();
        }
        // Checking whether 'this.target' is a HTMLElement or a string.
        // A DIV Element is needed to append others HTML Elements
        this.targetDiv = (this.target instanceof HTMLElement) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVAStudyBrowserPanel: target ' + this.target + ' not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.panel.render(this.div);
    },

    load: function (values) {
        var _this = this;

        if(!values){
            if(_this.formPanel){
                values = _this.formPanel.getValues();
                var species = this._getValues(this.speciesFieldTag);
                var type = this._getValues(this.typeFieldTag);
                _.extend(values, {species:species, type:type});
            }
        }

        for (key in values) {
            if (values[key] == '') {
                delete values[key]
            }
        }

//        this.studiesStore.clearFilter();

        EvaManager.get({
            host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
            category: 'meta/studies',
            resource: 'all',
            params: values,
            success: function (response) {
                var studies = [];
                try {
                    studies = response.response[0].result;
                } catch (e) {
                    console.log(e);
                }
                _this.studiesStore.loadRawData(studies);
            }
        });


    },
    _createPanel: function () {
        var _this = this;
        var stores = {
            species: Ext.create('Ext.data.TreeStore', {
                autoLoad: true,
                proxy: {
                    type: 'memory',
                    data: [],
                    reader: {
                        type: 'json'
                    }
                },
                fields: [
                    {name: 'display', type: 'string'}
                ]
            }),
            type: Ext.create('Ext.data.TreeStore', {
                autoLoad: true,
                proxy: {
                    type: 'memory',
                    data: [],
                    reader: {
                        type: 'json'
                    }
                },
                fields: [
                    {name: 'display', type: 'string'}
                ]
            })
        };

        var platformStore = Ext.create('Ext.data.TreeStore', {
            autoLoad: true,
            fields: [
                {name: 'display', type: 'string'}
            ],
            proxy: {
                type: 'memory',
                data: [
                    {display: 'Illumina', value: 'ngs',leaf: true,checked: false,  iconCls :'no-icon'},
                    {display: 'Roche', value: 'array',leaf: true,checked: false,  iconCls :'no-icon'},
                    {display: 'ABI', value: 'array',leaf: true,checked: false,  iconCls :'no-icon'}
                ],
//                    root:'',
                reader: {
                    type: 'json'
                }
            }
        });

        this.speciesFieldTag = Ext.create('Ext.tree.Panel', {
            fieldLabel: 'Organisms',
            store: stores.species,
            multiSelect: true,
            useArrows: true,
            rootVisible: false,
            columns: [
                {
                    xtype: 'treecolumn',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'display',
                    text:'Organism'
                }
            ],
            listeners: {
                'checkchange': function (node, checked) {
                    _this.load()
                }
            }
        });

        this.typeFieldTag = Ext.create('Ext.tree.Panel', {
            fieldLabel: 'Type',
            store: stores.type,
            multiSelect: true,
            useArrows: true,
            rootVisible: false,
            columns: [
                {
                    xtype: 'treecolumn',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'display',
                    text:'Type'
                }
            ],
            listeners: {
                'checkchange': function (node, checked) {
                    _this.load()
                }
            }
        });

        this.platformFieldTag = Ext.create('Ext.tree.Panel', {
            fieldLabel: 'Platform',
            store: platformStore,
            multiSelect: true,
            useArrows: true,
            rootVisible: false,
            columns: [
                {
                    xtype: 'treecolumn',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'display',
                    text:'Platform'
                }
            ],
            listeners: {
                'checkchange': function (node, checked) {
                    _this.load()
                }
            }
        });


        var studySearchField = Ext.create('Ext.form.field.Text', {
            fieldLabel: 'Search',
            labelAlign: 'top',
            emptyText: 'search',
            name: 'search',
            listeners: {
                change: function () {
                    var value = this.getValue();
                    if (value == "") {
                        _this.studiesStore.clearFilter();
                    } else {
                        var regex = new RegExp(value, "i");
                        _this.studiesStore.filterBy(function (e) {
                            return regex.test(e.get('name')) || regex.test(e.get('description'));
                        });
                    }
                }
            }

        });

        this.columns =[
            {
                text: "ID",
                dataIndex: 'id',
                flex: 1.6,
                // To render a link to FTP
                renderer: function (value, meta, rec, rowIndex, colIndex, store) {
                    meta.tdAttr = 'data-qtip="Click to see  more detailed information"';
                    return value ? Ext.String.format(
                        '<a href="?eva-study='+value+'" target="_blank">'+value+'</a>',
                        value
                    ) : '';
                }
            },
            {
                text: "Name",
                dataIndex: 'name',
                flex: 7
            },
            {
                text: "Organism",
                dataIndex: 'speciesScientificName',
                flex: 2,
                renderer: function(value, p, record) {
                    return value ? Ext.String.format(
                        '<div data-toggle="popover" title="Organism" data-content="And her...">{0}</div>',
                        value
                    ) : '';
                }
            },
            {
                text: "Type",
                dataIndex: 'experimentTypeAbbreviation',
                flex: 1.5,
                renderer: function (value, meta, rec, rowIndex, colIndex, store) {
                    console.log(rec)
                    meta.tdAttr = 'data-qtip="'+rec.data.experimentType+'"';
                    return value ? Ext.String.format(
                        '<tpl>'+value+'</tpl>',
                        value
                    ) : '';
                }
            },
//                            {
//                                text: "Scope",
//                                dataIndex: 'scope',
//                                flex: 3
//                            },
//            {
//                text: "Number of Variants",
//                dataIndex: 'numVariants',
//                flex: 3
//            },
            {
                text: "Platform",
                dataIndex: 'platform',
                flex: 3
            },
            {
                text: "Download",
//                        xtype: 'checkcolumn',
                dataIndex: 'id',
                flex: 3,
                renderer: function (value, p, record) {
                    return value ? Ext.String.format(
                        '<a href="ftp://ftp.ebi.ac.uk/pub/databases/eva/{0}" target="_blank">FTP Download</a>',
                        value,
                        record.data.threadid
                    ) : '';
                }
            }

        ];


        this.grid = Ext.create('Ext.grid.Panel', {
                title: 'Studies found',
                cls:'studybrowser',
                store: this.studiesStore,
                header: this.headerConfig,
                loadMask: true,
//                hideHeaders: true,
//                plugins: 'bufferedrenderer',
                plugins: [{
                    ptype: 'rowexpander',
                    rowBodyTpl : new Ext.XTemplate(
                        '<p style="padding: 2px 2px 2px 15px"><b>Platform:</b> {platform}</p>',
                        '<p style="padding: 2px 2px 2px 15px"><b>Centre:</b> {center}</p>',
                        '<p style="padding: 2px 2px 5px 15px"><b>Description:</b> {description}</p>'
                    )
                }],
                height: 420,
                features: [
                    {ftype: 'summary'}
                ],
                viewConfig: {
                    emptyText: 'No studies found',
                    enableTextSelection: true,
                    markDirty: false,
                    listeners: {
                        itemclick: function (este, record) {

                        },
                        itemcontextmenu: function (este, record, item, index, e) {

                        }
                    }
                },
                selModel: {
                    listeners: {
                        'selectionchange': function (sm, selectedRecord) {
                            if (selectedRecord.length) {
                                var row = selectedRecord[0].data;
                                _this.trigger("study:select", {sender: _this, args: row});
                            }
                        }
                    }
                },
                columns: this.columns
            }
        );


        var submitButton = Ext.create('Ext.button.Button', {
            text: 'Submit',
            handler: function (btn) {
                console.log(">>>>>>>>>"+panel);
                var values = panel.getValues();
                _this.load(values);
            }
        });


        this.leftPanel = Ext.create('Ext.container.Container', {
            flex: 1.1,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                margin: 5
            },
            items: [
//              submitButton,
                studySearchField,
                this.speciesFieldTag,
                this.typeFieldTag
            ]
        });


        this.rightPanel = Ext.create('Ext.container.Container', {
            flex: 5,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                margin: 5
            },
            items: [this.grid]
        });

        this.formPanel = Ext.create('Ext.form.Panel', {
            title: 'Short Genetic Variations Browser',
            border: this.border,
            header: this.headerConfig,
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            defaults: {
                margin: 5
            },
            items: [
                this.leftPanel,
                this.rightPanel
            ]
        });


        EvaManager.get({
            host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
            category: 'meta/studies',
            resource: 'stats',
            params: {},
            success: function (response) {
                try {
                    var statsData = {};
                    var responseStatsData = response.response[0].result[0];
                    for (key in responseStatsData) {
                        var stat = responseStatsData[key];
                        var arr = [];
                        for (key2 in stat) {
                            var obj = {};
                            // TODO We must take care of the types returned
                            if(key2.indexOf(',') == -1) {
                                obj['display'] = key2;
                                obj['leaf'] = true;
                                obj['checked'] = false;
                                obj['iconCls'] = "no-icon";
                                obj['count'] = stat[key2];
                            }
//                                obj['display'] = key2;
                          if(!_.isEmpty(obj)){
                              arr.push(obj);
                          }
                        }
                        statsData[key] = arr;
                        if (typeof stores[key] !== 'undefined') {
                            stores[key].loadRawData(statsData[key]);
                        }

                    }
                } catch (e) {
                    console.log(e);
                }
            }
        });

        return  this.formPanel;
    },
    _getValues:function(panel){
        var nodes = panel.store.data.items;
        var values = [];
        for (i = 0; i < nodes.length; i++) {
                if(nodes[i].data.checked){
                    values.push(nodes[i].data.display)
                }
        }
        return values;
    },

    setLoading: function (loading) {
        this.panel.setLoading(loading);
    },

    update: function () {
        if (this.panel) {
            this.panel.update();
        }
    },
    getPanel: function(){
        return this.panel;
    }

};