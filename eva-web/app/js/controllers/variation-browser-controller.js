/**
 * Created by jag on 17/03/2014.
 */

var variationCtrl = evaApp.controller('variationBrowserCtrl', ['$scope', '$rootScope', 'ebiAppDomainHostService','ebiVarMetadataService', function ($scope, $rootScope, ebiAppDomainHostService, ebiVarMetadataService) {



    $scope.message = ebiAppDomainHostService.message;

    $scope.infoColumns  = {
        AF: 'af',
        AC: 'ac',
        VF: 'vf',
        AA: 'aa',
        AN: 'an'
    };

    $scope.infoColumnsFilter = [];

    $scope.colors = [
        {name:'black', shade:'dark', state:false},
        {name:'white', shade:'light',state:false},
        {name:'red', shade:'dark',state:false},
        {name:'blue', shade:'dark',state:false},
        {name:'yellow', shade:'light',state:false}
    ];
    $scope.color = $scope.colors[2];

    $scope.data = ebiAppDomainHostService.data;

    $scope.highchartsNG = {
        options: {
            chart: {
                type: 'pie'
            },

            plotOptions: {
                series: {
                    cursor: 'pointer',
                    point: {
                        events: {
                            click: function() {
                                console.log(this)
                            }
                        }
                    }

                }
            }
        },
        series: [{
            data:   $scope.data
        }],
        title: {
            text:  $scope.message
        },
        loading: false,
        credits: {
            enabled: false
        }
    }

    $scope.barChart = {
        options: {
            chart: {
                type: 'bar',
            },
            plotOptions: {
                series: {
                    cursor: 'pointer',
                    point: {
                        events: {
                            click: function() {
                                console.log(this.series.color)
                            }
                        }
                    }

                }
            }

        },

        series: [{
            data:   $scope.data
        }],
        title: {
            text:  $scope.message
        },
        loading: false,
        credits: {
            enabled: false
        }
    }








    $scope.infoColumnBtnClick = function(infoColumnId){

        this.color.state = !this.color.state;

        var pos = $scope.infoColumnsFilter.indexOf(infoColumnId);

        if (pos == -1) {
            $scope.infoColumnsFilter.push(infoColumnId);
        }
        else {
            $scope.infoColumnsFilter.splice(pos, 1);
        }

        if($('#'+infoColumnId).hasClass("btn-primary")){

            $('#'+infoColumnId).removeClass("btn-primary");
        }
        else{
            $('#'+infoColumnId).addClass("btn-primary");
        }

    }

    $scope.selectAllInfoColumn = function(){

        $('#infoColumnMultiSelect').children().addClass("btn-primary");

        for (var i in $scope.infoColumns) {

            var pos = $scope.infoColumnsFilter.indexOf($scope.infoColumns[i]);

            if (pos == -1) {
                $scope.infoColumnsFilter.push($scope.infoColumns[i]);
            }
        }

    }
    $scope.deselectAllInfoColumn = function(){
        $('#infoColumnMultiSelect').children().removeClass("btn-primary");
        $scope.infoColumnsFilter=[];
    }


     this.tempData =ebiVarMetadataService.testData();
     console.log(this.tempData.aaData);


   // $scope.testData = this.data.aaData;
    $scope.testColumns = [
                            { "sTitle": "Engine" },
                            { "sTitle": "Browser" },
                            { "sTitle": "Platform" },
                            { "sTitle": "Version", "sClass": "center" },
                            { "sTitle": "Grade", "sClass": "center" }
                         ];
//    $scope.options.aaData =  $scope.testdata.aaData;



    //<!--------------Events---------------->
    $scope.speciesChange  = function(){
        ebiAppDomainHostService.speciesChangeBroadcast($scope.color.name);
    }

    //<!--------------Broadcast---------------->
    $rootScope.$on('broadcastSpeciesChange', function() {
            $scope.highchartsNG.series[0].data =  ebiAppDomainHostService.data;
            $scope.highchartsNG.title.text =  ebiAppDomainHostService.message;

            $scope.barChart.series[0].data =  ebiAppDomainHostService.data;
            $scope.barChart.title.text =  ebiAppDomainHostService.message;

    });

    $scope.tblData = this.tempData.aaData;

    console.log($scope.tblData)
    // not mandatory, here as an example
    $scope.tblColumns = [
        { "sTitle": "Surname" },
        { "sTitle": "First Name" }
    ];

    // not mandatory, here as an example
    $scope.columnDefs = [{ "bSortable": false, "aTargets": [1] }];

    // not mandatory, you can use defaults in directive
    $scope.overrideOptions = {
        "bStateSave": true,
        "iCookieDuration": 2419200, /* 1 month */
        "bJQueryUI": true,
//        "bPaginate": false,
        "bLengthChange": false,
        "bFilter": false,
        "bInfo": false,
        "bDestroy": true,
        "sPaginationType": "full_numbers",
    };

    // we pretend that we have received new data from somewhere (eg a search)
    $scope.addData = function(){
        //$scope.tblData.push(["jones", "henry"]); // BUG? Angular doesn't pick this up
        $scope.counter = $scope.counter+1;
        var existing = $scope.tblData.slice();
        existing.push([$scope.counter, $scope.counter*2]);
        $scope.tblData = existing;
    }
    $scope.counter = 0




}]);

