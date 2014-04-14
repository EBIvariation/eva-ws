/**
 * Created by jag on 14/04/2014.
 */
evaApp.directive('variantWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        template: '<div  class="col-sm-8 col-md-9 col-lg-9">' +

                    '<ul id="variantBrowserTabs" class="variantBrowserTabs nav nav-tabs nav-pills">' +
                        '<li><a href="#summary" data-toggle="tab">Summary</a></li>' +
                        '<li class="active"><a href="#vBrowser" data-toggle="tab">Variants and Effect</a></li>' +
                        '<li><a href="#genomeViewer" data-toggle="tab">Genome Viewer</a></li>' +
                    '</ul>' +

                    '<div class="tab-content">'+

                         '<div class="tab-pane" id="summary">' +
                            '<div style= "margin-left:150px;">' +
                                '<highchart id="chart1" config="barChart" style= "width: 30%"></highchart>' +
                            '</div>' +
                         '</div>' +

                        '<div class="tab-pane active" id="vBrowser">'+

                            '<div>' +
                                '<div>' +
                                    '<h3><small><span class="label label-primary">Statistics </span>&nbsp;<button type="button" ng-click="showStatitsics()" class="btn  btn-default btn-xs">{{statistics}}</button></small></h3>' +
                                    '<highchart ng-show="showStatitsicsState" id="chart1" config="highchartsNG" style="width: 250px; height: 200px;" ></highchart>' +
                                '</div>' +
                            '</div>' +

                            '<div class="variantTable">' +

                                '<div id={{variantTableId}}></div>' +

                                '<ul id="variantBrowserSubTabs" class="variantBrowserTabs nav nav-tabs nav-pills">' +
                                    '<li class="active"><a href="#vEffect" data-toggle="tab">Variant Effects</a></li>' +
                                    '<li><a href="#vFiles" data-toggle="tab">Variant Files & Stats</a></li>' +
                                '</ul>' +

                                '<div class="tab-content">' +

                                    '<div class="tab-pane active" id="vEffect">' +
                                        '<div id={{variantEffectTableId}}></div>' +
                                    '</div>' +

                                    '<div class="tab-pane" id="vFiles">' +

                                        '<div id={{variantFilesTableId}}></div>' +

                                        '<div  class="row">'+

                                            '<div class="col-md-8">' +
                                                '<div id={{variantStatsViewId}}></div>' +
                                            '</div>' +

                                            '<div class="col-md-4">' +
                                                '<div id={{variantStatsChartId}} style="width: 250px;"></div>' +
                                            '</div>' +

                                        '</div>' +

                                    '</div>' +

                                '</div>' +

                            '</div>' +

                        '</div>' +

                    '<div>'+

                  '</div>',
        link: function($scope, element, attr) {
                $scope.variantTableId       = 'VariantBrowserTable';
                $scope.variantEffectTableId = 'VariantEffectTable';
                $scope.variantFilesTableId  = 'VariantFilesTable';
                $scope.variantStatsViewId   = 'VariantStatsView';
                $scope.variantStatsChartId   = 'VariantStatsChart';

                $scope.searchVariants = function(){
                    var variantWidget;
                    variantWidget = new VariantWidget({
                        variantTableID       : $scope.variantTableId,
                        variantEffectTableID : $scope.variantEffectTableId,
                        variantFilesTableID  : $scope.variantFilesTableId,
                        variantStatsViewID   : $scope.variantStatsViewId,
                        variantStatsChartID  : $scope.variantStatsChartId,
                        location             : $scope.location
                    });
                    variantWidget.draw();

                }
            }

    };
})