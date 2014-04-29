angular.module('geneWidgetModule', []).directive('geneWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/gene-browser-view.html',
        link: function($scope, element, attr) {
            $scope.geneView = 'sdasfd';
            $scope.geneTableId       = 'geneBrowserTable';
            eventManager.on("gene:search", function(e) {
                var geneWidget;
                geneWidget = new GeneWidget({
                    geneTableID    : $scope.geneTableId,
                    gene           : $scope.gene,

                });
                geneWidget.draw();
            });
        }

    };

});
