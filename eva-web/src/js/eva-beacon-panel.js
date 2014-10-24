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
        var evaBeacon= new EvaBeacon({
            target:'beaconForm'
        });

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

        var GA4GHTpl = new Ext.XTemplate([
                                          '<div>',
                                              '<h2>EBI GA4GH Beacon</h2>',
                                              '<p>Learn more about the Global Alliance for Genomics and Health (GA4GH) at <a href="http://genomicsandhealth.org" target="_blank">http://genomicsandhealth.org</a>as well as the GA4GH Beacon project: <a href="http://ga4gh.org/#/beacon" target="_blank">http://ga4gh.org/#/beacon</a> </p>',
                                              '<div class="row">',
                                                  '<div class="col-md-12"><p><b>Example queries:</b></p>',
                                                      '<div class="row">',
                                                          '<div class="col-md-12">',
                                                              '<tpl></tpl><div><p><span><a href="#"  onclick="loadEvaBeacon(this)" chrom="13" coordinate="32888799" allele="C" project="PRJEB7217">Chrom:13&nbsp;Coordinate:32888799 &nbsp;Allele:C&nbsp;Project:PRJEB7217 </a></span></p></div>',
                                                              '<div><p><span><a href="#"  onclick="loadEvaBeacon(this)" chrom="1" coordinate="46403" allele="TGT" project="PRJEB4019">Chrom:1&nbsp;Coordinate:46403 &nbsp;Allele:TGT&nbsp;Project:PRJEB4019</a></span></p></div>',
                                                              '<div><p> <span><a href="#"  onclick="loadEvaBeacon(this)" chrom="1" coordinate="1002921" allele="" project="PRJEB4019">Chrom:1&nbsp;Coordinate:1002921 &nbsp;Project:PRJEB4019</a></span></p><hr/></div>',
                                                          '</div>',
                                                      '</div>',
                                                      '<div id="beaconForm"></div>',
                                                  '</div>',
                                              '</div>',
                                          '</div>'
                                        ]);
        var GA4GHView = Ext.create('Ext.view.View', {
            tpl: GA4GHTpl
        });

        var beaconTpl = new Ext.XTemplate(['<div style="margin-left:10px;" id="beacon-form-test"></div>']);
        var beaconView = Ext.create('Ext.view.View', {
            tpl: beaconTpl
        });

        this.toolTabPanel = Ext.create("Ext.tab.Panel", {
                activeTab: 0,
                plain: true,
                items: [
                    {
                        title: 'GA4GH',
                        items:[GA4GHView],
                        height:900
                    },
                    {
                        title: 'Beacon',
                        items:[beaconView],
                        height:900
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
//            cls:'study-browser-panel',
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


