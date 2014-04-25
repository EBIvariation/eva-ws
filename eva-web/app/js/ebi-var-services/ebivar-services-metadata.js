
angular.module('ebiVar.Services.Metadata', []).service('ebiVarMetadataService', function($http) {

    this.fetchData = function(args) {

        var url = args;
        $.ajax({
            url: url,
            //url: 'http://localhost:8080/ws-test/rest/test/study/estd199',
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                data = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //console.log(textStatus)
                data = '';
            }
        });

        return data;

    };

});