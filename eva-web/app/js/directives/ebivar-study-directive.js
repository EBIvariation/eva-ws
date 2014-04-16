/**
 * Created by jag on 21/03/2014.
 */
    angular.module('ebiVar.Services.Metadata.Study', []).directive('studyData', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/study-view.html',
        controller: function($scope) {

        }
    };

}).controller('studyDataCtrl', ['$scope', '$rootScope', 'ebiAppDomainHostService','ebiVarMetadataService', function ($scope, $rootScope, ebiAppDomainHostService, ebiVarMetadataService) {

//    var getAllStudiesParams = {
//                                host:METADATA_HOST,
//                                domain:DOMAIN,
//                                options:'study/list'
//                              };


    //$scope.studies = ebiVarMetadataService.getAllStudies(getAllStudiesParams);



//    $scope.options = {
//        "sPaginationType": "full_numbers",
//        aoColumns: [
//            {"sTitle": "Organism"},
//            {"sTitle": "StudyType"},
//            {"sTitle": "StudyAccession"},
//            {"sTitle": "StudyURL"},
//            {"sTitle": "DisplayName"},
//            {"sTitle": "ProjectId"},
//            {"sTitle": "Description"},
//            {"sTitle": "TaxID"},
//            {"sTitle": "Pubmed"}
//        ],
//        aoColumnDefs: [{
//            "bSortable": true,
//            "aTargets": [0]
//        }],
//        bJQueryUI: true,
//        bDestroy: true,
//        aaData: [],
//
//    };
//    var columnData = [];
//    if($scope.studies.length > 0){
//        for (var i = 0; i < $scope.studies.length; i++) {
//
//            var pubmed = '';
//            for (var j = 0; j < $scope.studies[i].pubmedId.length; j++) {
//                var location  = 'http://europepmc.org/search?query='+$scope.studies[i].pubmedId[j]
//                pubmed += '<a href="' + location + '">'+ $scope.studies[i].pubmedId[j] +'</a><br />';
//            }
//
//            var taxId = '';
//            for (var j = 0; j < $scope.studies[i].taxId.length; j++) {
//                taxId +=  $scope.studies[i].taxId[j] +'<br />';
//            }
//           //datatables
//            $scope.options.aaData.push([
//                $scope.studies[i].organism,
//                $scope.studies[i].studyType,
//                $scope.studies[i].studyAccession,
//                $scope.studies[i].studyUrl,
//                $scope.studies[i].displayName,
//                $scope.studies[i].projectId,
//                $scope.studies[i].desctiption,
//                taxId,
//                pubmed
//            ]);
//
//        }
//    }

}]);
