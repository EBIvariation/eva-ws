/**
 * Created by jag on 17/03/2014.
 */
var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','highcharts-ng','ebiVar.Services.Metadata','homeWidgetModule','variantWidgetModule','checklist-model', 'geneWidgetModule', 'ui.router', 'duScroll']);



//evaApp.config(function($stateProvider, $urlRouterProvider){
//
//
//    $stateProvider
//        .state('home', {
//            url: "/home",
//            templateUrl: 'views/home.html'
//        })
//        .state('variant', {
//            url: "/variant-browser",
////            templateUrl: "views/variation-browser-view.html"
//            templateUrl: "eva-index.html?variant"
//        })
//
//        .state('genes', {
//            url: "/genes",
//            templateUrl: 'views/gene-browser-view.html'
//        })
//        .state('route2', {
//            url: "/route2",
//            templateUrl: "views/variant-view.html"
//        })
//});

//http://wwwint.ebi.ac.uk/eva/webservices/rest/v1/segment/1:5000-35000/variants

var METADATA_HOST = "http://wwwint.ebi.ac.uk/eva/webservices/rest";
//var METADATA_HOST = "http://localhost:8080/eva/webservices/rest";

//var METADATA_HOST = "http://172.22.69.133:8080/eva/webservices/rest";
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








