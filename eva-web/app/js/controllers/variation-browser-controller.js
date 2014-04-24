/**
 * Created by jag on 17/03/2014.
 */

var variationCtrl = evaApp.controller('variationBrowserCtrl', ['$scope', '$rootScope', 'ebiAppDomainHostService','ebiVarMetadataService', function ($scope, $rootScope, ebiAppDomainHostService, ebiVarMetadataService) {



    //$scope.events.trigger("clicked");


//    var eventManager = new EventManager();
//    eventManager.on("variant:select", function(e) {
//        console.log(e);
//    });

//    var summaryUrl = METADATA_HOST+'/'+VERSION+'/genes/ranking';
//    var summaryData = ebiVarMetadataService.fetchData(summaryUrl);
//    var summaryChartData = parseSummaryChartData(summaryData);
//    console.log(summaryChartData.data)
//
//
//    function parseSummaryChartData(args){
//
//        var data = [];
//        var tempArray=[];
//        for (key in args.response.result) {
//            tempArray.push([args.response.result[key]._id,args.response.result[key].count]);
//        }
//
//        data['data'] = tempArray;
//        data['data'] = tempArray;
//        return data;
//    }

//    $scope.summaryPieChartConfig = {
//        options: {
//            chart: {
//                type: 'pie'
//            },
//
//            plotOptions: {
//
//                series: {
//                    cursor: 'pointer',
//                    // size: 80,
//                    point: {
//                        events: {
//                            click: function() {
//                                console.log(this)
//                            }
//                        }
//                    }
//
//                }
//            }
//        },
//        series: [{
//            data:   summaryChartData.data
//        }],
//        title: {
//            text:  ''
//        },
//        loading: false,
//        credits: {
//            enabled: false
//        }
//    }

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

    $scope.gene = 'BRCA1';

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


}]);

