function StudyBrowser(args) {
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


StudyBrowser.prototype = {
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

         var dgvaStudyBrowser = new DgvaStudyBrowserPanel({
            pageSize:10
        });
        var evaStudyBrowser = new EvaStudyBrowserPanel({
            pageSize:10
        });

        this.toolTabPanel = Ext.create("Ext.tab.Panel", {
                activeTab: 0,
                plain: true,
                items: [
                    {
                        title: evaStudyBrowser.getPanel().title,
                        cls:'studybrowser ',
                        items:[evaStudyBrowser.getPanel()]
                    },
                    {
                        title:dgvaStudyBrowser.getPanel().title,
                        cls:'studybrowser',
                        items:[dgvaStudyBrowser.getPanel()]

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
            items: [this.toolTabPanel]
        });


        return  this.panel;
    }


};

