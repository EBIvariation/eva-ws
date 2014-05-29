

METADATA_HOST = "http://www.ebi.ac.uk/eva/webservices/rest";
METADATA_VERSION = 'v1';

CELLBASE_HOST = "http://www.ebi.ac.uk/cellbase/webservices/rest";
CELLBASE_VERSION = "v3";

if(window.location.host.indexOf("www.ebi.ac.uk") === -1){

    METADATA_HOST = "http://wwwint.ebi.ac.uk/eva/webservices/rest";
    METADATA_VERSION = 'v1';

//    CELLBASE_HOST = "http://ws-beta.bioinfo.cipf.es/cellbase-staging/rest";
//    CELLBASE_VERSION = "latest";
}

/**
 * Created by jag on 17/03/2014.
 */
//var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','highcharts-ng','ebiVar.Services.Metadata','homeWidgetModule','variantWidgetModule','checklist-model', 'geneWidgetModule', 'ui.router', 'duScroll']);
var evaApp = angular.module('evaApp', ['ui.bootstrap','ebiApp','homeWidgetModule','variantWidgetModule','geneWidgetModule', 'duScroll']);



