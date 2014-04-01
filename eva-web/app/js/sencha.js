Ext.require([
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.util.*',
    'Ext.state.*'
]);

// Define Company entity
// Null out built in convert functions for performance *because the raw data is known to be valid*
// Specifying defaultValue as undefined will also save code. *As long as there will always be values in the data, or the app tolerates undefined field values*
Ext.define('Company', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'company'},
        {name: 'price',      type: 'float', convert: null,     defaultValue: undefined},
        {name: 'change',     type: 'float', convert: null,     defaultValue: undefined},
        {name: 'pctChange',  type: 'float', convert: null,     defaultValue: undefined},
        {name: 'lastChange', type: 'date',  dateFormat: 'n/j h:ia', defaultValue: undefined}
    ],
    idProperty: 'company'
});

Ext.onReady(function() {
    Ext.QuickTips.init();

    // setup the state provider, all state information will be saved to a cookie
    Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));

    // sample static data for the store
    var myData = [
        ['3m Co',                               71.72, 0.02,  0.03,  '9/1 12:00am'],
        ['Alcoa Inc',                           29.01, 0.42,  1.47,  '9/1 12:00am'],
        ['Altria Group Inc',                    83.81, 0.28,  0.34,  '9/1 12:00am'],
        ['American Express Company',            52.55, 0.01,  0.02,  '9/1 12:00am'],
        ['American International Group, Inc.',  64.13, 0.31,  0.49,  '9/1 12:00am'],
        ['AT&T Inc.',                           31.61, -0.48, -1.54, '9/1 12:00am'],
        ['Boeing Co.',                          75.43, 0.53,  0.71,  '9/1 12:00am'],
        ['Caterpillar Inc.',                    67.27, 0.92,  1.39,  '9/1 12:00am'],
        ['Citigroup, Inc.',                     49.37, 0.02,  0.04,  '9/1 12:00am'],
        ['E.I. du Pont de Nemours and Company', 40.48, 0.51,  1.28,  '9/1 12:00am'],
        ['Exxon Mobil Corp',                    68.1,  -0.43, -0.64, '9/1 12:00am'],
        ['General Electric Company',            34.14, -0.08, -0.23, '9/1 12:00am'],
        ['General Motors Corporation',          30.27, 1.09,  3.74,  '9/1 12:00am'],
        ['Hewlett-Packard Co.',                 36.53, -0.03, -0.08, '9/1 12:00am'],
        ['Honeywell Intl Inc',                  38.77, 0.05,  0.13,  '9/1 12:00am'],
        ['Intel Corporation',                   19.88, 0.31,  1.58,  '9/1 12:00am'],
        ['International Business Machines',     81.41, 0.44,  0.54,  '9/1 12:00am'],
        ['Johnson & Johnson',                   64.72, 0.06,  0.09,  '9/1 12:00am'],
        ['JP Morgan & Chase & Co',              45.73, 0.07,  0.15,  '9/1 12:00am'],
        ['McDonald\'s Corporation',             36.76, 0.86,  2.40,  '9/1 12:00am'],
        ['Merck & Co., Inc.',                   40.96, 0.41,  1.01,  '9/1 12:00am'],
        ['Microsoft Corporation',               25.84, 0.14,  0.54,  '9/1 12:00am'],
        ['Pfizer Inc',                          27.96, 0.4,   1.45,  '9/1 12:00am'],
        ['The Coca-Cola Company',               45.07, 0.26,  0.58,  '9/1 12:00am'],
        ['The Home Depot, Inc.',                34.64, 0.35,  1.02,  '9/1 12:00am'],
        ['The Procter & Gamble Company',        61.91, 0.01,  0.02,  '9/1 12:00am'],
        ['United Technologies Corporation',     63.26, 0.55,  0.88,  '9/1 12:00am'],
        ['Verizon Communications',              35.57, 0.39,  1.11,  '9/1 12:00am'],
        ['Wal-Mart Stores, Inc.',               45.45, 0.73,  1.63,  '9/1 12:00am']
    ];

    /**
     * Custom function used for column renderer
     * @param {Object} val
     */
    function change(val) {
        if (val > 0) {
            return '<span style="color:green;">' + val + '</span>';
        } else if (val < 0) {
            return '<span style="color:red;">' + val + '</span>';
        }
        return val;
    }

    /**
     * Custom function used for column renderer
     * @param {Object} val
     */
    function pctChange(val) {
        if (val > 0) {
            return '<span style="color:green;">' + val + '%</span>';
        } else if (val < 0) {
            return '<span style="color:red;">' + val + '%</span>';
        }
        return val;
    }

    // create the data store
    var store = Ext.create('Ext.data.ArrayStore', {
        model: 'Company',
        data: myData
    });

    // create the Grid
    var grid = Ext.create('Ext.grid.Panel', {
        store: store,
        stateful: true,
        collapsible: true,
        multiSelect: true,
        stateId: 'stateGrid',
        columns: [
            {
                text     : 'Company',
                flex     : 1,
                sortable : false,
                dataIndex: 'company'
            },
            {
                text     : 'Price',
                width    : 75,
                sortable : true,
                renderer : 'usMoney',
                dataIndex: 'price'
            },
            {
                text     : 'Change',
                width    : 75,
                sortable : true,
                renderer : change,
                dataIndex: 'change'
            },
            {
                text     : '% Change',
                width    : 75,
                sortable : true,
                renderer : pctChange,
                dataIndex: 'pctChange'
            },
            {
                text     : 'Last Updated',
                width    : 85,
                sortable : true,
                renderer : Ext.util.Format.dateRenderer('m/d/Y'),
                dataIndex: 'lastChange'
            },
            {
                menuDisabled: true,
                sortable: false,
                xtype: 'actioncolumn',
                width: 50,
                items: [{
                    icon   : '../shared/icons/fam/delete.gif',  // Use a URL in the icon config
                    tooltip: 'Sell stock',
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = store.getAt(rowIndex);
                        alert("Sell " + rec.get('company'));
                    }
                }, {
                    getClass: function(v, meta, rec) {          // Or return a class from a function
                        if (rec.get('change') < 0) {
                            return 'alert-col';
                        } else {
                            return 'buy-col';
                        }
                    },
                    getTip: function(v, meta, rec) {
                        if (rec.get('change') < 0) {
                            return 'Hold stock';
                        } else {
                            return 'Buy stock';
                        }
                    },
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = store.getAt(rowIndex);
                        alert((rec.get('change') < 0 ? "Hold " : "Buy ") + rec.get('company'));
                    }
                }]
            }
        ],
        height: 350,
        width: 600,
        title: 'Array Grid',
        renderTo: 'grid-example',
        viewConfig: {
            stripeRows: true,
            enableTextSelection: true
        }
    });


        Ext.Loader.setConfig({enabled: true});

        //Ext.Loader.setPath('Ext.ux', '../ux/');
        Ext.require([
            'Ext.grid.*',
            'Ext.data.*',
            'Ext.util.*',
            'Ext.toolbar.Paging',
            //'Ext.ux.PreviewPlugin',
            'Ext.ModelManager',
            'Ext.tip.QuickTipManager'
        ]);



        Ext.onReady(function(){
            Ext.tip.QuickTipManager.init();

            Ext.define('ForumThread', {
                extend: 'Ext.data.Model',
                fields: [
                    'title', 'forumtitle', 'forumid', 'username',
                    {name: 'replycount', type: 'int'},
                    {name: 'lastpost', mapping: 'lastpost', type: 'date', dateFormat: 'timestamp'},
                    'lastposter', 'excerpt', 'threadid'
                ],
                idProperty: 'threadid'
            });

            // create the Data Store
            var store = Ext.create('Ext.data.Store', {
                pageSize: 50,
                model: 'ForumThread',
                remoteSort: true,
                proxy: {
                    // load using script tags for cross domain, if the data in on the same domain as
                    // this page, an HttpProxy would be better
                    type: 'jsonp',
                    url: 'http://www.sencha.com/forum/topics-browse-remote.php',
                    reader: {
                        root: 'topics',
                        totalProperty: 'totalCount'
                    },
                    // sends single sort as multi parameter
                    simpleSortMode: true
                },
                sorters: [{
                    property: 'lastpost',
                    direction: 'DESC'
                }]
            });

            // pluggable renders
            function renderTopic(value, p, record) {
                return Ext.String.format(
                    '<b><a href="http://sencha.com/forum/showthread.php?t={2}" target="_blank">{0}</a></b><a href="http://sencha.com/forum/forumdisplay.php?f={3}" target="_blank">{1} Forum</a>',
                    value,
                    record.data.forumtitle,
                    record.getId(),
                    record.data.forumid
                );
            }

            function renderLast(value, p, r) {
                return Ext.String.format('{0}<br/>by {1}', Ext.Date.dateFormat(value, 'M j, Y, g:i a'), r.get('lastposter'));
            }


            var pluginExpanded = true;
            var grid = Ext.create('Ext.grid.Panel', {
                width: 700,
                height: 500,
                title: 'ExtJS.com - Browse Forums',
                store: store,
                disableSelection: true,
                loadMask: true,
                viewConfig: {
                    id: 'gv',
                    trackOver: false,
                    stripeRows: false

                },
                // grid columns
                columns:[{
                    // id assigned so we can apply custom css (e.g. .x-grid-cell-topic b { color:#333 })
                    // TODO: This poses an issue in subclasses of Grid now because Headers are now Components
                    // therefore the id will be registered in the ComponentManager and conflict. Need a way to
                    // add additional CSS classes to the rendered cells.
                    id: 'topic',
                    text: "Topic",
                    dataIndex: 'title',
                    flex: 1,
                    renderer: renderTopic,
                    sortable: false
                },{
                    text: "Author",
                    dataIndex: 'username',
                    width: 100,
                    hidden: true,
                    sortable: true
                },{
                    text: "Replies",
                    dataIndex: 'replycount',
                    width: 70,
                    align: 'right',
                    sortable: true
                },{
                    id: 'last',
                    text: "Last Post",
                    dataIndex: 'lastpost',
                    width: 150,
                    renderer: renderLast,
                    sortable: true
                }],
                // paging bar on the bottom
                bbar: Ext.create('Ext.PagingToolbar', {
                    store: store,
                    displayInfo: true,
                    displayMsg: 'Displaying topics {0} - {1} of {2}',
                    emptyMsg: "No topics to display"
                }),
                renderTo: 'topic-grid'
            });

            // trigger the data store load
            store.loadPage(1);
        });


});

Ext.require([
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.panel.*',
    'Ext.layout.container.Border'
]);
Ext.Loader.onReady(function() {
    Ext.define('Book',{
        extend: 'Ext.data.Model',
        proxy: {
            type: 'ajax',
            reader: 'xml'
        },
        fields: [
            // set up the fields mapping into the xml doc
            // The first needs mapping, the others are very basic
            {name: 'Author', mapping: '@author.name'},
            'Title',
            'Manufacturer',
            'ProductGroup',
            'DetailPageURL'
        ]
    });


    /**
     * App.BookStore
     * @extends Ext.data.Store
     * @cfg {String} url This will be a url of a location to load the BookStore
     * This is a specialized Store which maintains books.
     * It already knows about Amazon's XML definition and will expose the following
     * Record defintion:
     *  - Author
     *  - Manufacturer
     *  - ProductGroup
     *  - DetailPageURL
     */
    Ext.define('App.BookStore', {
        extend: 'Ext.data.Store',
        constructor: function(config) {
            config = config || {};

            config.model = 'Book';
            config.proxy = {
                type: 'ajax',
                url: 'sheldon.xml',
                reader: Ext.create('Ext.data.reader.Xml', {
                    // records will have an "Item" tag
                    record: 'Item',
                    id: 'ASIN',
                    totalRecords: '@total'
                })
            };

            // call the superclass's constructor
            this.callParent([config]);
        }
    });


    /**
     * App.BookGrid
     * @extends Ext.grid.Panel
     * This is a custom grid which will display book information. It is tied to
     * a specific record definition by the dataIndex properties.
     *
     * It follows a very custom pattern used only when extending Ext.Components
     * in which you can omit the constructor.
     *
     * It also registers the class with the Component Manager with an xtype of
     * bookgrid. This allows the application to take care of the lazy-instatiation
     * facilities provided in Ext's Component Model.
     */
    Ext.define('App.BookGrid', {
        extend: 'Ext.grid.Panel',
        // This will associate an string representation of a class
        // (called an xtype) with the Component Manager
        // It allows you to support lazy instantiation of your components
        alias: 'widget.bookgrid',

        // override
        initComponent : function() {
            // Pass in a column model definition
            // Note that the DetailPageURL was defined in the record definition but is not used
            // here. That is okay.
            this.columns = [
                {text: "Author", width: 120, dataIndex: 'Author', sortable: true},
                {text: "Title", flex: 1, dataIndex: 'Title', sortable: true},
                {text: "Manufacturer", width: 125, dataIndex: 'Manufacturer', sortable: true},
                {text: "Product Group", width: 125, dataIndex: 'ProductGroup', sortable: true}
            ];
            // Note the use of a storeId, this will register thisStore
            // with the StoreManager and allow us to retrieve it very easily.
            this.store = new App.BookStore({
                storeId: 'gridBookStore',
                url: 'sheldon.xml'
            });
            // finally call the superclasses implementation
            this.callParent();
        }
    });


    /**
     * App.BookDetail
     * @extends Ext.Panel
     * This is a specialized Panel which is used to show information about
     * a book.
     *
     * This demonstrates adding 2 custom properties (tplMarkup and
     * startingMarkup) to the class. It also overrides the initComponent
     * method and adds a new method called updateDetail.
     *
     * The class will be registered with an xtype of 'bookdetail'
     */
    Ext.define('App.BookDetail', {
        extend: 'Ext.Panel',
        // register the App.BookDetail class with an xtype of bookdetail
        alias: 'widget.bookdetail',
        // add tplMarkup as a new property
        tpl: [
            'Title: <a href="{DetailPageURL}" target="_blank">{Title}</a><br/>',
            'Author: {Author}<br/>',
            'Manufacturer: {Manufacturer}<br/>',
            'Product Group: {ProductGroup}<br/>'
        ],
        // startingMarup as a new property
        startingMarkup: 'Please select a book to see additional details',

        bodyPadding: 7,
        // override initComponent to create and compile the template
        // apply styles to the body of the panel and initialize
        // html to startingMarkup
        initComponent: function() {
            this.html = this.startingMarkup;
            // call the superclass's initComponent implementation
            this.callParent();
        }
    });


    /**
     * App.BookMasterDetail
     * @extends Ext.Panel
     *
     * This is a specialized panel which is composed of both a bookgrid
     * and a bookdetail panel. It provides the glue between the two
     * components to allow them to communicate. You could consider this
     * the actual application.
     *
     */
    Ext.define('App.BookMasterDetail', {
        extend: 'Ext.Panel',
        alias: 'widget.bookmasterdetail',

        frame: true,
        title: 'Book List',
        width: 580,
        height: 400,
        layout: 'border',

        // override initComponent
        initComponent: function() {
            this.items = [{
                xtype: 'bookgrid',
                itemId: 'gridPanel',
                region: 'north',
                height: 210,
                split: true
            },{
                xtype: 'bookdetail',
                itemId: 'detailPanel',
                region: 'center'
            }];
            // call the superclass's initComponent implementation
            this.callParent();
        },
        // override initEvents
        initEvents: function() {
            // call the superclass's initEvents implementation
            this.callParent();

            // now add application specific events
            // notice we use the selectionmodel's rowselect event rather
            // than a click event from the grid to provide key navigation
            // as well as mouse navigation
            var bookGridSm = this.getComponent('gridPanel').getSelectionModel();
            bookGridSm.on('selectionchange', this.onRowSelect, this);
        },
        // add a method called onRowSelect
        // This matches the method signature as defined by the 'rowselect'
        // event defined in Ext.selection.RowModel
        onRowSelect: function(sm, rs) {
            // getComponent will retrieve itemId's or id's. Note that itemId's
            // are scoped locally to this instance of a component to avoid
            // conflicts with the ComponentManager
            if (rs.length) {
                var detailPanel = this.getComponent('detailPanel');
                detailPanel.update(rs[0].getData());
            }

        }
    });
// do NOT wait until the DOM is ready to run this
}, false);


// Finally now that we've defined all of our classes we can instantiate
// an instance of the app and renderTo an existing div called 'binding-example'
// Note now that classes have encapsulated this behavior we can easily create
// an instance of this app to be used in many different contexts, you could
// easily place this application in an Ext.Window for example
Ext.onReady(function() {
    // create an instance of the app
    var bookApp = new App.BookMasterDetail({
        renderTo: 'binding-example'
    });
    // We can retrieve a reference to the data store
    // via the StoreManager by its storeId
    Ext.data.StoreManager.get('gridBookStore').load();
});
