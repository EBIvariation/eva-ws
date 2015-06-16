/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
 *
 * This file is part of JSorolla.
 *
 * JSorolla is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSorolla is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSorolla. If not, see <http://www.gnu.org/licenses/>.
 */
function EvaVariantPopulationStatsPanel(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("VariantPopulationPanel");

    this.target;
    this.title = "Stats";
    this.height = 500;
    this.autoRender = true;
    this.statsTpl = new Ext.XTemplate(
        '<table class="ocb-stats-table">' +
            '<tr>' +
            '<td class="header">Minor Allele Frequency:</td>' +
            '<td>{maf} ({mafAllele})</td>' +
            '</tr>',
        '<tr>' +
            '<td class="header">Minor Genotype Frequency:</td>' +
            '<td>{mgf} ({mgfAllele})</td>' +
            '</tr>',
        '<tr>' +
            '<td class="header">Mendelian Errors:</td>' +
            '<td>{mendelianErrors}</td>' +
            '</tr>',
        '<tr>' +
            '<td class="header">Missing Alleles:</td>' +
            '<td>{missingAlleles}</td>' +
            '</tr>',
        '<tr>' +
            '<td class="header">Missing Genotypes:</td>' +
            '<td>{missingGenotypes}</td>' +
            '</tr>',
        '</table>'
    );

    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;

    if (this.autoRender) {
        this.render();
    }
}

EvaVariantPopulationStatsPanel.prototype = {
    render: function () {
        var _this = this;

        //HTML skel
        this.div = document.createElement('div');
        this.div.setAttribute('id', this.id);

        this.panel = this._createPanel();

    },
    draw: function () {
        this.targetDiv = (this.target instanceof HTMLElement ) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('target not found');
            return;
        }

        this.targetDiv.appendChild(this.div);
        this.panel.render(this.div);

    },
    clear: function () {
        this.studiesContainer.removeAll(true);
    },
    load: function (data,params) {
        var _this = this;
        this.clear();

        var panels = [];

        var availableStudies = ['301','8616'];

        for (var key in data) {
            console.log(data)
            var study = data[key];
            if(params.species == 'hsapiens_grch37'){
                if(_.indexOf(availableStudies, study.studyId) > -1){
                    var studyPanel = this._createPopulationGridPanel(study,params);
                }
            }else{
                var studyPanel = this._createPopulationGridPanel(study,params);
            }


            panels.push(studyPanel);

        }

        this.studiesContainer.add(panels);
    },
    _createPanel: function () {
        this.studiesContainer = Ext.create('Ext.container.Container', {
            layout: {
                type: 'accordion',
                titleCollapse: true,
//                fill: false,
                multi: true
            }

        });

        var panel = Ext.create('Ext.container.Container', {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            overflowY: true,
            overflowX: true,
            padding: 10,
            items: [
                {
                    xtype: 'box',
                    id:'populationStats',
                    cls: 'ocb-header-4',
                    html: '<h4>Population Statistics</h4>',
                    margin: '5 0 10 10'
                },
                this.studiesContainer
            ],
            height: this.height
        });
        return panel;
    },
    _createPopulationGridPanel: function (data,params) {
        var _this = this;

        var populationData = [];
        _.each(_.keys(data.cohortStats), function(key){
            console.log(this[key])
            var tempObj =  _.extend(this[key], {id:key})
            populationData.push(tempObj);

        },data.cohortStats);

        if(params.species == 'hsapiens_grch37'){
            Ext.getCmp('populationStats').update('<h4>Population Statistics</h4><h5 style="color:#436883;margin-left:-15px;font-size:14px;">Population frequencies from 1000 Genomes</h5>')
        }else{
            Ext.getCmp('populationStats').update('<h4>Population Statistics</h4>')
        }




        //TO BE REMOVED
        var study_title;
        var projectList = '';
        EvaManager.get({
            category: 'meta/studies',
            resource: 'list',
            params:{species:params.species},
            async: false,
//            params:{species:params.species},
            success: function (response) {
                try {
                    projectList = response.response[0].result;
//                    console.log(projectList)
                } catch (e) {
                    console.log(e);
                }
            }
        });

        if(projectList){
            for (var i = 0; i < projectList.length; i++) {
                if (projectList[i].studyId === data.studyId) {
                    study_title = '<a href="?eva-study='+projectList[i].studyId+'" target="_blank">'+projectList[i].studyName+'</a> ('+ projectList[i].studyId +')';
                }
            }
        }else{
            study_title = '<a href="?eva-study='+data.studyId+'" target="_blank">'+data.studyId+'</a>';
        }

        var populationStatsColumns = {
            items:[
                {
                    text: "Population",
                    dataIndex: "id",
                    flex: 0.4
                },
                {
                    text: "Minor Allele Frequency",
                    dataIndex: "maf",
                    xtype: "templatecolumn",
//                    tpl: '<tpl if="maf == -1 || maf == 0">NA <tpl else>{maf:number( "0.000" )} </tpl>',
                    tpl: '<tpl if="maf == -1">NA <tpl else>{maf:number( "0.000" )} </tpl>',
                    flex: 0.75
                },
                {
                    text: "MAF Allele",
                    dataIndex: "mafAllele",
                    xtype: "templatecolumn",
//                    tpl: '<tpl if="mafAllele">{mafAllele} <tpl else>NA</tpl>',
                    tpl: '<tpl if="mafAllele">{mafAllele} <tpl else>-</tpl>',
                    flex: 0.5
                },
                {
                    text: "Mendelian Errors",
                    dataIndex: "mendelianErrors",
                    xtype: "templatecolumn",
                    tpl:'<tpl if="mendelianErrors == -1">NA <tpl else>{mendelianErrors}</tpl>',
                    flex: 0.5
                },
                {
                    text: "Missing Alleles",
                    dataIndex: "missingAlleles",
                    xtype: "templatecolumn",
                    tpl:'<tpl if="missingAlleles == -1">NA <tpl else>{missingAlleles}</tpl>',
                    flex: 0.6
                },
                {
                    text: "Missing Genotypes",
                    dataIndex: "missingGenotypes",
                    xtype: "templatecolumn",
                    tpl:'<tpl if="missingGenotypes == -1">NA <tpl else>{missingGenotypes}</tpl>',
                    flex: 0.7
                },
//                {
//                    text: "Genotypes Count",
//                    dataIndex: "genotypesCount",
//                    tpl:'<tpl if="missingGenotypes == -1">NA <tpl else>{missingGenotypes}</tpl>',
//                    flex: 1
//                }
            ],
            defaults: {
                align:'center' ,
                sortable : true
            }
        };

        console.log(populationData)

        var store = Ext.create("Ext.data.Store", {
            //storeId: "GenotypeStore",
            pageSize: 10,
            fields: [
                {name: "altAllele", type: "string" },
                {name: "altAlleleCount", type: "string"}
            ],
            data: populationData,
            proxy: {
                    type: 'memory'
            },
            sorters:
            {
                property: 'id',
                direction: 'ASC'
            }
        });



        var plugins =  [{
            ptype: 'rowexpander',
            rowBodyTpl : new Ext.XTemplate(),
        }];

        var grid = Ext.create('Ext.grid.Panel', {
            store: store,
            loadMask: true,
            width: 900,
            height: 600,
            cls:'genotype-grid',
            margin: 20,
            viewConfig: {
                emptyText: 'No records to display',
                enableTextSelection: true,
                deferEmptyText:false
            },
            columns: populationStatsColumns,
            plugins:plugins


        });

        grid.view.on('expandbody', function(rowNode, record, body, rowIndex){

            var genotypesCount = record.data.genotypesCount;
            var divID = 'population-stats-grid-'+record.data.id;
            if(!_.isEmpty(genotypesCount)){
                var divID = 'population-stats-grid-'+record.data.id;
                body.innerHTML = '<div style="width:800px;" id="'+divID+'"></div>';
                var genotypesCountArray=[];
                _.each(_.keys(genotypesCount), function(key){
                    genotypesCountArray.push([key,  this[key]]);

                },genotypesCount);
                var genotypesCountChartData = {id:divID,title:'Genotype Count',chartData:genotypesCountArray};
                _this._drawChart(genotypesCountChartData);
            }else{
                body.innerHTML = '<div style="width:800px;">No Genotypes Count available</div>';
            }

        });

        var studyPanel = Ext.create('Ext.panel.Panel', {
            header:{
                titlePosition:1
            },
            title: study_title,
            border: false,
            layout: {
                type: 'vbox',
                align: 'fit'
            },
            overflowX:true,
            items: [grid]
        });


        return studyPanel;
    },
    _getGenotypeCount: function (gc) {
        var res = [];
        for (var key in gc) {
            res.push({
                genotype: key,
                count: gc[key]
            })
        }
        return res;
    },
    _drawChart:function(data){
        var _this = this;
        var height = 290;
        var width = 250;
        var id = '#'+data.id;
        var render_id = document.querySelector(id);
        var dataArray  = data.chartData;
        var title = data.title;

        $(function () {
            Highcharts.setOptions({
                colors: ['#207A7A', '#2BA32B','#2E4988','#54BDBD', '#5DD15D','#6380C4', '#70BDBD', '#7CD17C','#7D92C4','#295C5C', '#377A37','#344366','#0A4F4F', '#0E6A0E','#0F2559' ],
                chart: {
                    style: {
                        fontFamily: 'sans-serif;'
                    }
                }
            });
            $(render_id).highcharts({
                chart: {
                    plotBackgroundColor: null,
                    plotBorderWidth: null,
                    plotShadow: false,
                    height: height,
//                    width: width,
                    marginLeft:50,
                    marginTop:50

                },
                legend: {
                    enabled: true,
//                    width: 50,
                    margin: 0,
                    labelFormatter: function() {
                        return '<div>' + this.name + '('+ this.y + ')</div>';
                    },
                    layout:'horizontal',
                    useHTML:true,
                    align:'center'

                },
                title: {
                    text: title,
                    style: {
//                                    display: 'none'
                    },
                    align: 'center'
                },
                tooltip: {
                    pointFormat: '<b>{point.y}</b>'
    //                                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                },
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: false
                        },
                        showInLegend: true
                    }
                },
                series: [{
                    type: 'pie',
                    name: 'Studies by '+title,
                    data: dataArray
                }],
                credits: {
                    enabled: false
                }
            });

        });

    }
};

String.prototype.escapeHTML = function () {
    return(
        this.replace(/>/g,'&gt;').
            replace(/</g,'&lt;').
            replace(/"/g,'&quot;')
        );
};

