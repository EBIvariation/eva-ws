/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
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
        this.div.setAttribute('class', 'eva-app');
        this.div.setAttribute('id', this.id);

        this.targetMenuUl = (this.targetMenu instanceof HTMLElement ) ? this.targetMenu : document.querySelector('#' + this.targetMenu);
        this.evaMenu = this._createEvaMenu(this.targetMenuUl);


        /* Home */
        $(this.homeDiv).addClass('eva-child');
        this.childDivMenuMap['Home'] = this.homeDiv;

        /* Submit */
        $(this.submitDiv).addClass('eva-child');
        this.childDivMenuMap['Submit Data'] = this.submitDiv;

        /* About */
        $(this.aboutDiv).addClass('eva-child');
        this.childDivMenuMap['About'] = this.aboutDiv;

        /* Contact */
        $(this.contactDiv).addClass('eva-child');
        this.childDivMenuMap['Contact'] = this.contactDiv;

        /* api */
        $(this.apiDiv).addClass('eva-child');
        this.childDivMenuMap['API'] = this.apiDiv;

        /* Templates */
        $(this.templatesDiv).addClass('eva-child');
        this.childDivMenuMap['Templates'] = this.templatesDiv;

        /* VCF */
        $(this.vcfDiv).addClass('eva-child');
        this.childDivMenuMap['VCF'] = this.vcfDiv;

        /* studyView */
        $(this.studyView).addClass('eva-child');
        this.childDivMenuMap['eva-study'] = this.studyView;
        this.childDivMenuMap['dgva-study'] = this.studyView;

        /* variantView */
        $(this.variantView).addClass('eva-child');
        this.childDivMenuMap['variant'] = this.variantView;

        /* beacon */
        $(this.beacon).addClass('eva-child');
        this.childDivMenuMap['GA4GH'] = this.beacon;

        /* clinical */
        $(this.clinicalDiv).addClass('eva-child');
        this.childDivMenuMap['Clinical'] = this.clinicalDiv;

        /* submision-start */
        $(this.submissionForm).addClass('eva-child');
        this.childDivMenuMap['submission-start'] = this.submissionForm;


        /* Study Browser Panel*/
//        $(this.studyBrowserDiv).addClass('eva-child');
//        this.childDivMenuMap['Study Browser'] =  this.studyBrowserDiv;
//
//        /* variant browser option*/
//        $(this.evaBrowserDiv).addClass('eva-child');
//        this.childDivMenuMap['VCF Browser'] = this.evaBrowserDiv;

    },
    draw: function () {
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (this.targetDiv === 'undefined') {
            console.log('target not found');
            return;
        }
        this.targetDiv.appendChild(this.div);
        this.evaMenu.draw();
        var contentDiv = document.querySelector('#content');
        this.studyBrowserPanel  = this._createStudyBrowserPanel(contentDiv);
        this.variantWidgetPanel  = this._createVariantWidgetPanel(contentDiv);
        this.genomeViewerPanel  = this._createGenomeViewerPanel(contentDiv);
        this.beaconPanel  = this._createBeaconPanel(contentDiv);
        // this.formPanelVariantFilter.draw();
        // this.genomeViewer.draw();
        // this.formPanelGenomeFilter.draw();
//        this.select('Home');

    },
    select: function (option) {
        this.evaMenu.select(option);
        this._selectHandler(option);
    },
    _selectHandler: function (option) {
        var _this = this;
        this.studyBrowserPanel.hide();
        this.variantWidgetPanel.hide();
        this.genomeViewerPanel.hide();
        this.beaconPanel.hide()
        $('body').find('.show-div').each(function (index, el) {
            $(el).removeClass('show-div');
            $(el).addClass('hide-div');
//           _this.div.removeChild(el)
        });
        if (this.childDivMenuMap[option]) {
            $(this.childDivMenuMap[option]).removeClass('hide-div');
            $(this.childDivMenuMap[option]).addClass('show-div');
//            this.div.appendChild(this.childDivMenuMap[option]);
        }

        var pageArray = ['eva-study','dgva-study', 'variant'];
        if(_.indexOf(pageArray, option) < 0 && !_.isEmpty(option)  ){
            var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?'+option;
            window.history.pushState({path:newurl},'',newurl);
            $( "a:contains('"+option+"')").parent('li').addClass('active');
        }

        switch (option) {
            case 'Home':
                _this._twitterWidgetUpdate();
                _this._drawStatisticsChart();
                break;
            case 'Study Browser':
                this.studyBrowserPanel.show();
                break;
            case 'VCF Browser':
                this.variantWidgetPanel.show();
                this.variantWidgetPanel.formPanelVariantFilter.trigger('submit', {values: this.variantWidgetPanel.formPanelVariantFilter.getValues(), sender: _this});
                break;
            case 'Genome Browser':
                this.genomeViewerPanel.show();
                break;
            case 'GA4GH':
                this.beaconPanel.show();
                break;
        }
    },
    _createEvaMenu: function (target) {
        var _this = this;
        var evaMenu = new EvaMenu({
            target: target,
            handlers: {
                'menu:click': function (e) {
                    _this._selectHandler(e.option);
                }
            }
        });
        return evaMenu;
    },
    _createStudyBrowserPanel: function(target){
        var studyBrowser = new EvaStudyBrowserPanel({
            target: target
        });
        studyBrowser.draw();
        return studyBrowser;

    },
    _createVariantWidgetPanel: function(target){
        var variantWidget= new EvaVariantWidgetPanel({
            target: target
        });
        variantWidget.draw();
        return variantWidget;

    },
    _createGenomeViewerPanel: function(target){
        var genomeViewer = new EvaGenomeViewerPanel({
            target: target
        });
        genomeViewer.draw();
        return genomeViewer;

    },
    _createBeaconPanel: function(target){
        var evaBeacon = new EvaBeaconPanel({
            target: target
        });
        evaBeacon.draw();
//        var evaBeacon= new EvaBeacon({
//            target:'beaconForm'
//        });

        return evaBeacon;

    },
    _twitterWidgetUpdate : function (){

        var twitterWidgetEl = document.getElementById('twitter-widget');
        twitterWidgetEl.innerHTML = "";
        twitterWidgetEl.innerHTML = '<a  class="twitter-timeline" height=100 href="https://twitter.com/EBIvariation"  data-widget-id="437894469380100096">Tweets by @EBIvariation</a>';
        $.getScript('//platform.twitter.com/widgets.js', function(){
            twttr.widgets.load();
        });
    },
    _drawStatisticsChart : function(){
        var evaStatistics = new EvaStatistics({
            targetId:'eva-statistics'
        });
        var dgvaStatistics = new DgvaStatistics({
            targetId:'dgva-statistics'
        });
    }
}
