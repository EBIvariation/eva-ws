/**
 * Created by jag on 17/03/2014.
 */
evaApp.directive('variationBrowser', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variation-browser-view.html',
        controller: function($scope) {

        }
    };
}).directive('variantEffect', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variant-effect-view.html',
        controller: function($scope) {

        }
    };
}).directive('metaData', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/project-view.html',
        controller: function($scope) {

        }
    };
});

