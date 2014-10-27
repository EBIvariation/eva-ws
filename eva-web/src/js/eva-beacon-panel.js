function EvaBeaconPanel(args) {
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


EvaBeaconPanel.prototype = {
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

        var evaBeaconForm = new EvaBeaconForm();

        this.toolTabPanel = Ext.create("Ext.tab.Panel", {
                activeTab: 0,
                plain: true,
                items: [
                    {
                        title: 'Beacon',
                        items:[evaBeaconForm.getPanel()],
                        height:950
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
            cls:'eva-beacon-panel',
            items: [this.toolTabPanel]
        });

        return  this.panel;
    }


};
function loadEvaBeacon(el) {
    var chrom = el.getAttribute('chrom');
    var coordinate = el.getAttribute('coordinate');
    var allele = el.getAttribute('allele');
    var project = el.getAttribute('project');
    var chromEl = document.querySelector('#beacon-chrom');
    var coordinateEl = document.querySelector('#beacon-coordinate');
    var alleleEl = document.querySelector('#beacon-allele');
    var projectEl =  document.querySelector('#beacon-project');
    chromEl.value = chrom;
    coordinateEl.value = coordinate;
    alleleEl.value = allele;
    projectEl.value = project;
}


