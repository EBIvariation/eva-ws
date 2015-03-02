/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
 * Copyright (c) 2014 Jag Kandasamy (EBI-EMBL)
 *
 * This file is part of EVA.
 *
 * EVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * EVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EVA. If not, see <http://www.gnu.org/licenses/>.
 */
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

        $(this.target).click(function (e) {
            if ($(e.target).prop("tagName") == 'A') {
                _this._optionClickHandler(e.target);
            }
        });

        //HTML skel
        this.target
    },
    draw: function () {
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