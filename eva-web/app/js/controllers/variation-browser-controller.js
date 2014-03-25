/**
 * Created by jag on 17/03/2014.
 */

var variationCtrl = evaApp.controller('variationBrowserCtrl', ['$scope', '$rootScope', 'ebiAppDomainHostService','ebiVarMetadataService', function ($scope, $rootScope, ebiAppDomainHostService, ebiVarMetadataService) {



    //$scope.events.trigger("clicked");


    var eventManager = new EventManager();
    eventManager.on("variant:select", function(e) {
        console.log(e);
    });



    $scope.statistics = '+';
    $scope.showStatitsicsState;

    $scope.showStatitsics = function(){
        //eventManager.trigger("variant:select", {hello: "world"});
        //$scope.events.trigger("clicked");
        this.showStatitsicsState = !this.showStatitsicsState;
        if(!this.showStatitsicsState){
            this.statistics = '+';
        }else{
            this.statistics = '-';
        }
    };
    $scope.message = ebiAppDomainHostService.message;

    $scope.infoColumns  = {
        AF: 'af',
        AC: 'ac',
        VF: 'vf',
        AA: 'aa',
        AN: 'an'
    };

    $scope.infoColumnsFilter = [];

    $scope.colors = [
        {name:'black', shade:'dark', state:false},
        {name:'white', shade:'light',state:false},
        {name:'red', shade:'dark',state:false},
        {name:'blue', shade:'dark',state:false},
        {name:'yellow', shade:'light',state:false}
    ];
    $scope.color = $scope.colors[2];

    $scope.data = ebiAppDomainHostService.data;

    $scope.highchartsNG = {
        options: {
            chart: {
                type: 'pie'
            },

            plotOptions: {

                series: {
                    cursor: 'pointer',
                   // size: 80,
                    point: {
                        events: {
                            click: function() {
                                console.log(this)
                            }
                        }
                    }

                }
            }
        },
        series: [{
            data:   $scope.data
        }],
        title: {
            text:  $scope.message
        },
        loading: false,
        credits: {
            enabled: false
        }
    }

    $scope.barChart = {
        options: {
            chart: {
                type: 'bar',
            },
            plotOptions: {
                series: {
                    cursor: 'pointer',
                    point: {
                        events: {
                            click: function() {
                                console.log(this.series.color)
                            }
                        }
                    }

                }
            }

        },

        series: [{
            data:   $scope.data
        }],
        title: {
            text:  $scope.message
        },
        loading: false,
        credits: {
            enabled: false
        }
    }

    $scope.infoColumnBtnClick = function(infoColumnId){

        this.color.state = !this.color.state;

        var pos = $scope.infoColumnsFilter.indexOf(infoColumnId);

        if (pos == -1) {
            $scope.infoColumnsFilter.push(infoColumnId);
        }
        else {
            $scope.infoColumnsFilter.splice(pos, 1);
        }

        if($('#'+infoColumnId).hasClass("btn-primary")){

            $('#'+infoColumnId).removeClass("btn-primary");
        }
        else{
            $('#'+infoColumnId).addClass("btn-primary");
        }

    }

    $scope.selectAllInfoColumn = function(){

        $('#infoColumnMultiSelect').children().addClass("btn-primary");

        for (var i in $scope.infoColumns) {

            var pos = $scope.infoColumnsFilter.indexOf($scope.infoColumns[i]);

            if (pos == -1) {
                $scope.infoColumnsFilter.push($scope.infoColumns[i]);
            }
        }

    }
    $scope.deselectAllInfoColumn = function(){
        $('#infoColumnMultiSelect').children().removeClass("btn-primary");
        $scope.infoColumnsFilter=[];
    }


     this.tempData =ebiVarMetadataService.testData();


   //<!---Datatable Data---->

   // $scope.testData = this.data.aaData;
    $scope.testColumns = [
                            {"sTitle": "Organism"},
                            {"sTitle": "StudyType"},
                            {"sTitle": "StudyAccession"},
                            {"sTitle": "StudyURL"},
                            {"sTitle": "DisplayName"},
                            {"sTitle": "ProjectId"},
                            {"sTitle": "Description"},
                            {"sTitle": "TaxID"},
                            {"sTitle": "Pubmed"}
                         ];
    $scope.columnDefs = [{ "bSortable": false, "aTargets": [1] }];

    // not mandatory, you can use defaults in directive
    $scope.overrideOptions = {
        "bStateSave": true,
        "bJQueryUI": true,
        "bLengthChange": false,
        "bFilter": false,
        "bInfo": false,
        "bDestroy": true,
        "sPaginationType": "full_numbers",

    };

   //<!---End of Datatable Data--->






    $scope.tblData = [];
    var columnData = [];
    var variantData = [];

    $scope.pagingOptions = {
        pageSizes: [5, 10, 20],
        pageSize: 5,
        currentPage: 1
    };

    $scope.searchVariants = function(){
        var getAllStudiesParams = {
            host:METADATA_HOST,
            domain:DOMAIN,
            options:'study/list'
        };

        $scope.studies = ebiVarMetadataService.getAllStudies(getAllStudiesParams);

        if($scope.studies.length > 0){
            for (var i = 0; i < $scope.studies.length; i++) {
                var test=1;
//            var callbackfn = 'showStatitsics('+test+')';
//            debugger
                var callbackfn = function() {
                    eventManager.trigger("variant:select", {hello: "world"});
                };
                var organism = '<a id="myLink" href="#" class="variant-selector" onClick='+callbackfn+'>'+ $scope.studies[i].organism +'</a>';
                var pubmed = '';
                var pubmed1 = [];
                for (var j = 0; j < $scope.studies[i].pubmedId.length; j++) {
                    var location  = 'http://europepmc.org/search?query='+$scope.studies[i].pubmedId[j]
                    pubmed += '<a href="' + location + '">'+ $scope.studies[i].pubmedId[j] +'</a><br />';
                    pubmed1.push($scope.studies[i].pubmedId[j]);
                }

                var taxId = '';
                for (var j = 0; j < $scope.studies[i].taxId.length; j++) {
                    taxId +=  $scope.studies[i].taxId[j] +'<br />';
                }

                //<!---ng-grid--data>
                variantData.push({
                    studyAccession:$scope.studies[i].studyAccession,
                    studyType:$scope.studies[i].studyType,
                    studyUrl:$scope.studies[i].studyUrl,
                    displayName:$scope.studies[i].displayName,
                    projectId:$scope.studies[i].projectId,
                    // description:$scope.studies[i].desctiption,
                    pubmed:pubmed1,
                });

                //datatables
                $scope.tblData.push([
                    organism,
                    $scope.studies[i].studyType,
                    $scope.studies[i].studyAccession,
                    $scope.studies[i].studyUrl,
                    $scope.studies[i].displayName,
                    $scope.studies[i].projectId,
                    //$scope.studies[i].desctiption,
                    $scope.studies[i].studyType,
                    taxId,
                    pubmed
                ]);

            }
            //<!---ng-grid--data>
            $scope.variantBowserTableData= variantData;
        }

    }
    //<!---Variants Main Table--->
    $scope.variantBowserTable = {
        data: 'variantBowserTableData',
        pagingOptions: $scope.pagingOptions,
        enableRowSelection:false,
        showFilter: true,
        columnDefs: [
            // {field:'studyAccession', displayName:'StudyAccession', cellTemplate: '<div class="ngCellText" ><a href="#" ng-click="setSelectedVariant(row.getProperty(col.field))">{{row.getProperty(col.field)}}</a></div>'},
            {field:'studyAccession', displayName:'StudyAccession', cellTemplate: '<div class="ngCellText" ><a href="#" ng-click="setSelectedVariant(row.getProperty(col.field))">{{row.getProperty(col.field)}}</a></div>'},
            {field: 'studyType', displayName: 'StudyType'},
            {field: 'studyUrl', displayName: 'StudyURL'},
            {field: 'displayName', displayName: 'DisplayName'},
            {field: 'projectId', displayName: 'ProjectID'},
            {field: 'pubmed', displayName: 'Pubmed',cellTemplate: '<div class="ngCellText" ng-class="col.colIndex()" ng-repeat="ids in row.getProperty(col.field)"><a href="http://europepmc.org/search?query={{ids}}">{{ids}}</a></div>'}
        ]

    };
    //<!----end of Variants Main Table-->











    $scope.setSelectedVariant = function(args){

        $scope.selectedVariant = true;
        //http://ws-beta.bioinfo.cipf.es/cellbase-staging/rest/latest/hsa/genomic/variant/3:169514585::T/consequence_type?of=json
        var getVariantEffectParams = {
            host:'http://ws-beta.bioinfo.cipf.es/cellbase-staging/rest',
            domain:'latest',
            options:'hsa/genomic/variant/3:169514585::T/consequence_type?of=json'
        };

        $scope.variantEffect = ebiVarMetadataService.getVariants(getVariantEffectParams);
        var variantEffectData=[];
        for (var i = 0; i < $scope.variantEffect.length; i++) {
            variantEffectData.push({
                position:$scope.variantEffect[i].chromosome+':'+$scope.variantEffect[i].position,
                snpId:$scope.variantEffect[i].snpId,
                consequenceType:$scope.variantEffect[i].consequenceType,
                aminoacidChange:$scope.variantEffect[i].aminoacidChange,
                geneId:$scope.variantEffect[i].geneId,
                transcriptId:$scope.variantEffect[i].transcriptId,
                featureId:$scope.variantEffect[i].featureId,
                featureName:$scope.variantEffect[i].featureName,
                featureType:$scope.variantEffect[i].featureType,
                featureBiotype:$scope.variantEffect[i].featureBiotype,
            });
        }
        $scope.variantEffectTableData = variantEffectData;
        //$rootScope.$broadcast('VariantSelected');
    };

    //<!---VariantsEffect Table--->
    $scope.variantEffectTable = {
        data: 'variantEffectTableData',
        pagingOptions: $scope.pagingOptions,
        showFilter: true,
        enableColumnResize: true
    };
    //<!----end of VariantsEffect Table-->


    //<!--------------Events---------------->
    $scope.speciesChange  = function(){
        ebiAppDomainHostService.speciesChangeBroadcast($scope.color.name);
    }

    //<!--------------Broadcast---------------->
    $rootScope.$on('broadcastSpeciesChange', function() {
            $scope.highchartsNG.series[0].data =  ebiAppDomainHostService.data;
            $scope.highchartsNG.title.text =  ebiAppDomainHostService.message;

            $scope.barChart.series[0].data =  ebiAppDomainHostService.data;
            $scope.barChart.title.text =  ebiAppDomainHostService.message;

    });

    //<!--------------Broadcast---------------->
    $rootScope.$on('VariantSelected', function() {
        //$scope.variantEffectTableData =  variantEffectData;
    });







}]);

