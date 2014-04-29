angular.module('geneWidgetModule', []).directive('geneWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/gene-browser-view.html',
        link: function($scope, element, attr) {
            $scope.geneView = 'sdasfd';
        }

    };

});
