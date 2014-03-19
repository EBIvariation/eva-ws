/**
 * Created by jag on 17/03/2014.
 */
var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','ngGrid','highcharts-ng']);

evaApp.factory('variationService', function($rootScope) {
    var variationService = {};

    variationService.message = 'test';

    variationService.data = [30, 15, 2, 8, 27];

    variationService.speciesChangeBroadcast = function(msg) {
        if(msg === 'blue'){
          this.data = [1, 15, 32, 4, 17];
        }else{
            this.data = [14, 22, 18, 9, 55];
        }
        this.message = msg;
        $rootScope.$broadcast('broadcastSpeciesChange');
    };


    return variationService;
});

$('#accordion').click(function(){
    alert('sdf')
});

