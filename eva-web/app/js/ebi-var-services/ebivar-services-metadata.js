
angular.module('ebiVar.Services.Metadata', []).service('ebiVarMetadataService', function($http) {



    this.getAllStudies = function(args) {

        var studyData;
        var url =args.host+'/'+args.domain+'/'+args.options;


        $.ajax({
            url: url,
            //url: 'http://localhost:8080/ws-test/rest/test/study/estd199',
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                studyData = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //console.log(textStatus)
                studyData = '';
            }
        });

        return studyData;

    };

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

    this.getVariants = function(args) {

        var variantData;
        var url =args.host+'/'+args.domain+'/'+args.options;
        $.ajax({
            url: url,
            async: false,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                variantData = response;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //console.log(textStatus)
                variantData = '';
            }
        });

        return variantData;
    };

});