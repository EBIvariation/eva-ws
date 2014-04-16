/**
 * Created by jag on 14/04/2014.
 */
angular.module('variantWidgetModule', []).directive('variantionWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variation-browser-view.html',
        link: function($scope, element, attr) {

                $scope.variantTableId       = 'VariantBrowserTable';
                $scope.variantEffectTableId = 'VariantEffectTable';
                $scope.variantFilesTableId  = 'VariantFilesTable';
                $scope.variantStatsViewId   = 'VariantStatsView';
                $scope.variantStatsChartId  = 'VariantStatsChart';
                eventManager.on("variant:select", function(e) {
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
                });

            }

    };
}).config(function($stateProvider, $urlRouterProvider) {
//    $stateProvider
//        .state('variant', {
//            url: "/variant",
//            templateUrl: "views/variation-browser-view.html",
//        })
//        .state('variant.view', {
//            url: "/view",
//            templateUrl: "views/variant-view.html",
//        })
//        .state('variant.browser', {
//            url: "/browser",
//            templateUrl: "views/variant-browser.html",
//
//        })

});
