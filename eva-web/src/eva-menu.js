//TEMPLATE EvaMenu
function EvaMenu(args) {
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("EvaMenu");
    this.target;
    this.autoRender = true;

    //set instantiation args, must be last
    _.extend(this, args);

    this.on(this.handlers);


    this.rendered = false;
    if (this.autoRender) {
        this.render(this.targetId);
    }
}

EvaMenu.prototype = {
    render: function () {
        var _this = this;
        console.log("Initializing");


//        var navgationHtml = '' +
//
//            '</div>' +
//            '</div>' +
//            '';

        this._createMenuItems();

        //HTML skel
        this.target
    },
    draw: function () {
    },
    _createMenuItems: function () {
        var _this = this;
        var items = ['Variant browser', 'Genome viewer'];

        var lastLi = this.target.querySelector('li.last')
        $(lastLi).removeClass('last');

        for (var i = 0; i < items.length; i++) {
            var text = items[i];
            var newLi = document.createElement('li');
            newLi.innerHTML = '<a href="#' + text + '">' + text + '</a>';
            $(lastLi).after(newLi);
            lastLi = newLi;
        }

        $(this.target).click(function (e) {
            if ($(e.target).prop("tagName") == 'A') {
                _this._optionClickHandler(e.target);
            }
        });

    },
    _optionClickHandler: function (aEl) {
        $(this.target).find('.active').removeClass('active');
        $(aEl).parent().addClass('active');
        this.trigger('menu:click', {option: $(aEl).text(), sender: this})
    },
    select: function (optionName) {
        var aEl = this.target.querySelector('a[href="#' + optionName + '"]')
        this._optionClickHandler(aEl);
    }
}