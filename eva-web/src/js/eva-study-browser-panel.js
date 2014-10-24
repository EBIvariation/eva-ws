function EvaStudyBrowserPanel(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.id = Utils.genId("StudyBrowserPanel");

    this.target;
    this.tools = [];
    _.extend(this, args);
    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};


EvaStudyBrowserPanel.prototype = {
    render: function () {
        var _this = this;
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
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.panel.render(this.div);
    },
    show: function () {
        this.panel.show()
    },
    hide: function () {
        this.panel.hide();
    },
    toggle: function () {
        if (this.panel.isVisible()) {
            this.panel.hide();
        } else {
            this.panel.show();
        }
    },
    _createPanel: function () {

         var svStudyBrowser = new SvStudyBrowser({
            pageSize:20
        });
        var snvStudyBrowser = new SnvStudyBrowser({
            pageSize:10
        });

        this.toolTabPanel = Ext.create("Ext.tab.Panel", {
                activeTab: 0,
                plain: true,
                items: [
                    {
                        title: snvStudyBrowser.getPanel().title.replace('Browser',''),
                        cls:'studybrowser ',
                        items:[snvStudyBrowser.getPanel()]
                    },
                    {
                        title:svStudyBrowser.getPanel().title.replace('Browser',''),
                        cls:'studybrowser',
                        items:[svStudyBrowser.getPanel()]

                    }
                ]
            });

        this.toolTabPanel.setActiveTab(0);

        this.panel = Ext.create('Ext.container.Container', {
            flex: 1,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                margin: 5
            },
            cls:'study-browser-panel',
            items: [this.toolTabPanel]
        });


        return  this.panel;
    }


};

