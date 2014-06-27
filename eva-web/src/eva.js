//TEMPLATE Eva
function Eva(args) {
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("Eva");
    this.target;
    this.targetMenu;
    this.autoRender = true;

    //set instantiation args, must be last
    _.extend(this, args);


    this.on(this.handlers);

    this.childDivMenuMap = {};

    this.rendered = false;
    if (this.autoRender) {
        this.render(this.targetId);
    }
}

Eva.prototype = {
    render: function () {
        var _this = this;
        console.log("Initializing");

        //HTML skel
        this.div = document.createElement('div');
        this.div.setAttribute('class', 'eva-div');
        this.div.setAttribute('id', this.id);

        this.variantWidgetdiv = document.createElement('div');
        this.variantWidgetdiv.setAttribute('class', 'eva-child');
        this.div.appendChild(this.variantWidgetdiv);
        this.childDivMenuMap['Variant browser'] = this.variantWidgetdiv;


        this.targetMenuUl = (this.targetMenu instanceof HTMLElement ) ? this.targetMenu : document.querySelector('#' + this.targetMenu);
        this.evaMenu = this._createEvaMenu(this.targetMenuUl);

        this.variantWidget = this._createVariantWidget(this.variantWidgetdiv);

        this.evaMenu.select('Variant browser');
//        this.panel = this._createPanel();
    },
    draw: function () {
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (this.targetDiv === 'undefined') {
            console.log('target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);

        this.evaMenu.draw();
        this.variantWidget.draw();


        var EXAMPLE_DATA = [];
        $.ajax({
            url: "http://www.ebi.ac.uk/eva/webservices/rest/v1/segments/1:5000-35000/variants",
            dataType: 'json',
            async: false,
            success: function (response, textStatus, jqXHR) {
                if (response != undefined && response.response.numResults > 0) {
                    for (var i = 0; i < response.response.result.length; i++) {
                        var elem = response.response.result[i];
                        EXAMPLE_DATA.push(elem);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log('Error loading Phenotypes');
            }
        });
        this.variantWidget.variantBrowserGrid.load(EXAMPLE_DATA);
//        this.panel.render(this.div);
    },
    _createEvaMenu: function (target) {
        var _this = this;
        var evaMenu = new EvaMenu({
            target: target,
            handlers: {
                'menu:click': function (e) {
                    $(_this.div).children('.eva-child').each(function (index, el) {
                        $(el).css({display: 'none'});
                    });
                    $(_this.childDivMenuMap[e.option]).css({display: 'inherit'});
                }
            }
        });
        return evaMenu;
    },
    _createVariantWidget: function (target) {
//        var width = this.width - parseInt(this.div.style.paddingLeft) - parseInt(this.div.style.paddingRight);


        var variantWidget = new VariantWidget({
            width: this.width,
            target: target,
            title: 'Variant Widget',
//            data: EXAMPLE_DATA,
//            url: url,
            filters: {},
            defaultToolConfig: {},
            tools: []
        }); //the div must exist

        return variantWidget;
    }
}