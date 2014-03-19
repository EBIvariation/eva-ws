/**
 * Created by jag on 17/03/2014.
 */

var variationCtrl = evaApp.controller('variationCtrl', ['$scope', '$rootScope', 'variationService', function ($scope, $rootScope, variationService) {

    $scope.message = variationService.message;

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

    $scope.data = variationService.data;

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


    //<!--------------Events---------------->
    $scope.speciesChange  = function(){
        variationService.speciesChangeBroadcast($scope.color.name);
    }

    //<!--------------Broadcast---------------->
    $rootScope.$on('broadcastSpeciesChange', function() {
            $scope.highchartsNG.series[0].data =  variationService.data;
            $scope.highchartsNG.title.text =  variationService.message;

            $scope.barChart.series[0].data =  variationService.data;
            $scope.barChart.title.text =  variationService.message;

    });




}]);

variationCtrl.$inject = ['$scope', 'variationService'];