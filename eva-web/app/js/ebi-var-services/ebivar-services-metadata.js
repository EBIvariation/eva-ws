
angular.module('ebiVarServices', []).service('EbiVarMetadataService', function($http) {

    this.test = 'sdf';
    this.getAllStudies = function(args) {

        var studyData;


        $http({method: 'GET', url: 'http://localhost:8080/ws-test/rest/test/study/list'})

        .success(function(data, status, headers, config) {
                studyData = data;

        })
        .error(function(data, status, headers, config) {
                studyData= '';
        });

//        $.ajax({
//            url: 'http://localhost:8080/ws-test/rest/test/study/list',
//            //url: 'http://localhost:8080/ws-test/rest/test/study/estd199',
//            async: false,
//            dataType: 'json',
//            success: function (response, textStatus, jqXHR) {
//                studyData = response;
//            },
//            error: function (jqXHR, textStatus, errorThrown) {
//                alert('error');
//            }
//        });

        console.log(studyData)
        return studyData;

    };
});