/**
 * Created by jag on 17/03/2014.
 */
evaApp.directive('variation', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/variation-view.html',
        controller: function($scope) {

        }
    };
});