/**
 * Created by jag on 17/03/2014.
 */

var evaMainCtrl = evaApp.controller('evaMainCtrl', ['$scope',  function ($scope) {

    $scope.searchboxValue;
    $scope.search = function(value){

        var url = window.location.origin+window.location.pathname+'?variantID='+$scope.searchboxValue;
        window.location.href = url;
    }

    function parseSummaryChartData(args){

        var data = [];
        var tempArray=[];
        for (key in args.response.result) {
            tempArray.push([args.response.result[key]._id,args.response.result[key].count]);
        }

        data['data'] = tempArray;
        data['title'] = 'Genes Ranking';

        return data;
    }


    $scope.statisticsClass = 'glyphicon glyphicon-chevron-right';
    $scope.showStatitsicsState;



    $scope.showStatitsics = function(){
        this.showStatitsicsState = !this.showStatitsicsState;
        if(!this.showStatitsicsState){
            this.statisticsClass = 'glyphicon glyphicon-chevron-right';
        }else{
            this.statisticsClass = 'glyphicon glyphicon-chevron-down';
           // createSummaryChart();
        }
    };

//    http://wwwint.ebi.ac.uk/eva/webservices/rest/v1/studies/list
    var studiesList;
    evaManager.get({
        category: 'studies',
        resource: 'list',
        params: {
            of: 'json'
        },
        query: '',
        async: false,
        success: function (data) {
            studiesList = data;
        },
        error: function (data) {
            console.log('Could not get list of studies');
        }
    });
    var studiesListData = studiesList.response.result;
    var studiesListObject = new Array();
    for (var key in studiesListData){
        studiesListObject.push({name:studiesListData[key],leaf: true, checked: false,  iconCls :'no-icon', qtip:studiesListData[key]})
    }


//    $scope.studies = [
//        {name: '1000g',leaf: true, checked: false,  iconCls :'no-icon' },
//        {name: 'gonl',leaf: true, checked: false,  iconCls :'no-icon' },
//        {name: 'evs',leaf: true, checked: false,  iconCls :'no-icon' },
//        {name: 'uk10k',leaf: true, checked: false,  iconCls :'no-icon' }
//    ]

    $scope.studies = studiesListObject;
    //console.log($scope.studies)



    $scope.toggleState = true;
    $scope.toggleShow = function(){
        this.toggleState = !this.toggleState;
    };

    $scope.drawPieChart = function(value,id){
        //alert('sadfdf')

        var chart1 = new Highcharts.Chart({
            chart: {
                renderTo:id,
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },

            title: {
                text: ''
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                    },
                    showInLegend: true
                }

            },
            series: [{
                type: 'pie',
                name: value.title,
                data: value.data
            }],
            credits: {
                enabled: false
            },
        });

    }

    $(function() {
        $("[data-toggle='tooltip']").tooltip();
    });


}]);

