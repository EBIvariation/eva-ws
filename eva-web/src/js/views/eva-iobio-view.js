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

var url = '';

function EvaIobioView(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("EVAIobioView");
    _.extend(this, args);
    this.rendered = false;
    this.render();


}
EvaIobioView.prototype = {
    render: function () {
        var _this = this

        this.targetDiv = (this.target instanceof HTMLElement) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVAv-GeneView: target ' + this.target + ' not found');
            return;
        }
        _this.draw(_this.url);

    },
    draw: function (data) {
        var _this = this;
        if(!_.isUndefined(data)){
            var iobioViewDiv = document.querySelector("#evaIobioView");
            $(iobioViewDiv).addClass('show-div');

            var elDiv = document.createElement("div");
            elDiv.innerHTML = '<iframe id="iobio" frameBorder="0"  scrolling="no" style="width:1330px;height:1200px;" src="'+data+'"></iframe>';
            iobioViewDiv.appendChild(elDiv);

        }
    }
}




