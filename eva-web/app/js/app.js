/**
 * Created by jag on 17/03/2014.
 */
var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','ngGrid','highcharts-ng','ebiVarServices']);

evaApp.factory('evaService', function($rootScope) {
    var evaService = {};

    evaService.message = 'test';

    evaService.data = [30, 15, 2, 8, 27];

    evaService.speciesChangeBroadcast = function(msg) {
        if(msg === 'blue'){
          this.data = [1, 15, 32, 4, 17];
        }else{
            this.data = [14, 22, 18, 9, 55];
        }
        this.message = msg;
        $rootScope.$broadcast('broadcastSpeciesChange');
    };


    return evaService;
});



