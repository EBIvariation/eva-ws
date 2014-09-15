angular.module('fileBrowserModule', []).directive('fileBrowser', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/file-browser.html',
        link: function($scope, element, attr) {

            $scope.studyDiv = "studyList";

            $scope.showFiles = function(id){
                var studyArgs = {name:$scope.studies[id].name}
                var studyFilesData = getFiles(studyArgs);
                if(this.toggleState){
                    $scope.studies[id]['filesData']= studyFilesData;
                }

            }


            function getFiles(args){

                var studyfiles;
                evaManager.get({
                    category: 'studies',
                    resource: 'files',
                    params: {
                        of: 'json'
                    },
                    query: args.name,
                    async: false,
                    success: function (data) {
                        studyfiles = data;
                    },
                    error: function (data) {
                        console.log('Could not get list of files');
                    }
                });

                return studyfiles.response.result;

            }

        }
    }
});