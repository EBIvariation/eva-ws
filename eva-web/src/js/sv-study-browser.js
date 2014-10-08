function SvStudyBrowserPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("DgvaStudyBrowserPanel");

    this.target;
    this.title = "Study Browser";
    this.height = 800;
    this.autoRender = true;
    this.pageSize = 20;
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

SvStudyBrowserPanel.prototype = {
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
            console.log('DGVAStudyBrowserPanel: target ' + this.target + ' not found');
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
                _.extend(values, {species:species, type:type, structural:true});
            }else{
                values = {structural:true};
            }
        }

        for (key in values) {
            if (values[key] == '') {
                delete values[key]
            }
//            else{
//                // TO Be Removed
//                if(key === 'species'){
//                    var tempArray = [];
//                    // changing values to lower case
//                    for (var i = 0; i < values[key].length; i++) {
//                        tempArray.push( values[key][i].toLowerCase());
//                    }
//                    values[key] = tempArray;
//                }
//
//            }
        }

//        this.studiesStore.clearFilter();

//            EvaManager.get({
//                host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
//                category: 'meta/studies',
//                resource: 'all',
//                params: values,
//                success: function (response) {
//                    var studies = [];
//                    try {
//                        studies = response.response[0].result;
//                    } catch (e) {
//                        console.log(e);
//                    }
//                   _this.studiesStore.loadRawData(studies);
//                }
//            });

        if(_this.grid){
            var studies = [];
            EvaManager.get({
                host: 'http://wwwdev.ebi.ac.uk/eva/webservices/rest',
                category: 'meta/studies',
                resource: 'all',
                params: values,
                success: function (response) {
                    var studies = [];
                    try {
                        studies = response.response[0].result;
                        this.studiesStore = Ext.create('Ext.data.Store', {
                            fields: [
                                {name: 'id', type: 'string'},
                                {name: 'name', type: 'string'}
                            ],
                            remoteSort: true,
                            pageSize:_this.pageSize,
                            proxy: {
                                type: 'memory',
                                data: studies,
                                reader: {
                                    type: 'json'
                                },
                                enablePaging: true
                            }
                        });

                        var searchValue = values.search;
                        if (searchValue == "") {
                            this.studiesStore.clearFilter();
                        } else {
                            var regex = new RegExp(searchValue, "i");
                            this.studiesStore.filterBy(function (e) {//
                                return regex.test(e.get('id')) || regex.test(e.get('name')) || regex.test(e.get('description'));
                            });
                        }
                        _this.grid.reconfigure(this.studiesStore);
                        _this.paging.bindStore(this.studiesStore);
                        _this.paging.doRefresh();
                    } catch (e) {
                        console.log(e);
                    }
                }
            });
        }


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
                    _this.load()
                }
            }

        });

        this.columns =[
            {
                text: "ID",
                dataIndex: 'id',
                flex: 2,
                // To render a link to FTP
                renderer: function (value, meta, rec, rowIndex, colIndex, store) {
                    meta.tdAttr = 'data-qtip="Click to see  more detailed information"';
                    return value ? Ext.String.format(
                        '<a href="?dgva-study='+value+'" target="_blank">'+value+'</a>',
                        value
                    ) : '';
                }
            },
            {
                text: "Name",
                dataIndex: 'name',
                flex: 3
            },
            {
                text: "Organism",
                dataIndex: 'speciesScientificName',
                flex: 3,
                renderer: function(value, p, record) {
                    return value ? Ext.String.format(
                        '<div data-toggle="popover" title="Organism" data-content="And her...">{0}</div>',
                        value
                    ) : '';
                }
            },
            {
                text: "Study Type",
                dataIndex: 'typeName',
                flex: 2
            },
//            {
//                text: "Number of Variants",
//                dataIndex: 'numVariants',
//                flex: 3
//            },
            {
                text: 'Download',
                //dataIndex: 'id',
                xtype: 'templatecolumn',
                tpl: '<tpl><a href="ftp://ftp.ebi.ac.uk/pub/databases/dgva/{id}_{name}" target="_blank">FTP</a></tpl>',
                flex:1.5
            }

        ];


        this.paging = Ext.create('Ext.PagingToolbar', {
            store: _this.studiesStore,
            id: _this.id + "_pagingToolbar",
            pageSize: _this.pageSize,
            displayInfo: true,
            displayMsg: 'Studies {0} - {1} of {2}',
            emptyMsg: "No Studies to display"
        });

        this.grid = Ext.create('Ext.grid.Panel', {
                tbar: this.paging,
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
//                                    '<p style="padding: 2px 2px 2px 15px"><b>Centre:</b> {center}</p>',
                        '<p style="padding: 2px 2px 5px 15px"><b>Description:</b> {description}</p>',
                        {}
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
                columns:this.columns,
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
            title: 'Structural Variations Browser',
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
            params: {structural:true},
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
        this.load();
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