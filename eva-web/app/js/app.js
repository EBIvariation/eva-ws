/**
 * Created by jag on 17/03/2014.
 */
var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','ngGrid','highcharts-ng','ebiVar.Services.Metadata','ebiVar.Services.Metadata.Study','dataTablePlugin']);

//http://wwwint.ebi.ac.uk/eva/webservices/rest/v1/segment/1:5000-35000/variants

var METADATA_HOST = "http://wwwint.ebi.ac.uk/eva/webservices/rest";
var VERSION = 'v1';

evaApp.factory('ebiAppDomainHostService', function($rootScope) {
    var ebiAppDomainHostService = {};


    ebiAppDomainHostService.data = [30, 15, 2, 8, 27];

    ebiAppDomainHostService.speciesChangeBroadcast = function(msg) {
        if(msg === 'blue'){
          this.data = [1, 15, 32, 4, 17];
        }else{
            this.data = [14, 22, 18, 9, 55];
        }
        this.message = msg;
        $rootScope.$broadcast('broadcastSpeciesChange');
    };


    return ebiAppDomainHostService;
});






