/**
 * Created by jag on 17/03/2014.
 */

var variationCtrl = evaApp.controller('variationBrowserCtrl', ['$scope', '$rootScope', 'ebiAppDomainHostService','ebiVarMetadataService', function ($scope, $rootScope, ebiAppDomainHostService, ebiVarMetadataService) {



    //$scope.events.trigger("clicked");


//    var eventManager = new EventManager();
//    eventManager.on("variant:select", function(e) {
//        console.log(e);
//    });


    $scope.statistics = '+';
    $scope.showStatitsicsState;

    $scope.searchVariants = function(){
        eventManager.trigger("variant:search");
    }

    $scope.showStatitsics = function(){
        //eventManager.trigger("variant:select", {hello: "world"});
        //$scope.events.trigger("clicked");
        this.showStatitsicsState = !this.showStatitsicsState;
        if(!this.showStatitsicsState){
            this.statistics = '+';
        }else{
            this.statistics = '-';
        }
    };

    $scope.location = '1:5000-35000';

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
                   // size: 80,
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











    //<!---VariantsEffect Table--->
    $scope.variantEffectTable = {
        data: 'variantEffectTableData',
        pagingOptions: $scope.pagingOptions,
        filterOptions: $scope.filterOptions,
        showFilter: true,
        enableColumnResize: true
    };
    //<!----end of VariantsEffect Table-->


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

    //<!--------------Broadcast---------------->
    $rootScope.$on('VariantSelected', function() {
        //$scope.variantEffectTableData =  variantEffectData;
    });



}]);

