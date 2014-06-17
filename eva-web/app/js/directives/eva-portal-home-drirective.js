angular.module('homeWidgetModule', []).directive('homeWidget', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/home.html',
        link: function($scope, element, attr) {
            $scope.hometest = 'bla bla';
            //twitter widget
            !function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");

            }
    }
}).directive('vcfFormat', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        templateUrl: 'views/vcf-format.html'

    }
})
.directive('submission', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        template: '<div><h3>European Variation Archive submissions</h3>'+
                   '<div>' +
                        '<p>EVA follows the infrastructure of fellow EMBL-EBI resources European Nucleotide Archive (<a href="http://www.ebi.ac.uk/ena/" target="_blank">ENA</a>) and European Genome-phenome Archive (<a href="http://www.ebi.ac.uk/ega/" target="_blank">EGA</a>)  to accept, archive, and accession <a href="?vcf">VCF files</a>. Submissions consist of VCF file(s) and metadata that describe sample(s), experiment(s), and analysis that produced the variant and/or genotype call(s).</p>' +
                   '</div>'+
                   '<h3>Key stages of EVA submissions</h3>'+
                   '<h4>Contact</h4>'+
                   '<div>'+
                        '<p><img src="img/Contact.png" alt="" width="48" height="48"> Contact the EVA Helpdesk via this webform in order to provide details of your submission.</p>'+
                   '</div>'+
                   '<h4>Receive</h4>'+
                   '<div>'+
                        '<p><img src="img/Receive.png" alt="" width="48" height="48"> &nbsp;Receive your submission pack, which will include:'+
                            '<div style="margin-left:70px;">'+
                            '<ul>'+
                                '<li>Details for your submission uploads </li>'+
                                '<li> <a href="?templates">Templates</a> to capture your associated metadata </li>'+
                                '<li>Key stages for your submission</li>'+
                            '</ul>'+
                            '</div>'+
                        '</p>'+
                    '</div>'+
                    '<h4>Submit</h4>'+
                    '<div>'+
                        '<p><img src="img/Upload.png" alt="" width="48" height="48"> Upload your data files to your private submission upload account or directly to the <a href="mailto:eva-helpdesk@ebi.ac.uk?subject=Sequence submissionto EVA&amp;body=**We will provide you with a submission package metadata templates guides for completing templates**" target="_top">EVA Helpdesk</a>.</p>'+
                    '</div>'+
                    '<h4>Document</h4>'+
                    '<div>'+
                        '<p><img src="img/Document.png" alt="" width="48" height="48"> Provide details of your study, samples, experiments, runs/analysis, policy and datasets </p>'+
                    '</div></div>'

    }
}).directive('clinical', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template: '<div><h3>EVA Clinical</h3>'+
                '<div>' +
                '<p>EVA is working with the <a href="http://www.ncbi.nlm.nih.gov/clinvar/">ClinVar project</a>&nbsp;as well as with collaborators such as <a href="http://lovd.nl/3.0/home">LOVD</a>&nbsp;to build a global database of genetic variants that are associated with clincal significance interpretation and detailed phenotypic data.&nbsp;If you are a clinical diagnostic or research lab who would like to submit data to our resource, please follow the instructions below or submit <a href="http://www.ncbi.nlm.nih.gov/clinvar/docs/submit/" style="line-height: 1.538em;">directly to ClinVar</a> at NCBI.</p>' +
                '</div>'+
                '<h3>Key stages of EVA Clincal submissions</h3>'+
                '<h4>Contact</h4>'+
                '<div>'+
                '<p><img src="img/Contact.png" alt="" width="48" height="48"> Contact the EVA Helpdesk via this webform in order to provide details of your submission.</p>'+
                '</div>'+
                '<h4>Receive</h4>'+
                '<div>'+
                '<p><img src="img/Receive.png" alt="" width="48" height="48"> &nbsp;Receive your submission pack, which will include:'+
                '<div style="margin-left:70px;">'+
                '<ul>'+
                '<li>Details for your submission uploads </li>'+
                '<li> <a href="?templates">Templates</a> to capture your associated metadata </li>'+
                '<li>Key stages for your submission</li>'+
                '</ul>'+
                '</div>'+
                '</p>'+
                '</div>'+
                '<h4>Submit</h4>'+
                '<div>'+
                '<p><img src="img/Upload.png" alt="" width="48" height="48"> Upload your data files to your private submission upload account or directly to the <a href="mailto:eva-helpdesk@ebi.ac.uk?subject=Sequence submissionto EVA&amp;body=**We will provide you with a submission package metadata templates guides for completing templates**" target="_top">EVA Helpdesk</a>.</p>'+
                '</div>'+
                '<h4>Document</h4>'+
                '<div>'+
                '<p><img src="img/Document.png" alt="" width="48" height="48"> Provide details of your study, samples, experiments, runs/analysis, policy and datasets </p>'+
                '</div></div>'

        }
}).directive('templates', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template: '<div>' +
                        '<h3>Templates</h3>'+
                        '<div>'+
                        '<h4>EVA Submissions</h4>'+
                        '<p>EVA submissions are accompanied by an Excel spreadsheet and/or tab-delimited text files which describe the meta-data. We encourage our users to submit as much metadata as possible.</p>'+
                        '<p>Please download our submission template via this <a href="files/EVA_Submission_template.EVA_.V1.0.1_live.xlsx" target="_blank">link</a>.</p>'+
                        '<p>A completed template for a fictional submission can be downloaded <a href="files/EVA_Submission_template.EVA_.V1.0.1_live_mockup.xlsx" target="_blank">here</a>.</p>'+
                        '</div>' +
                        '<h4>EVA Clincal Submissions</h4>'+
                        '<p>EVA Clinical submissions are described via an Excel spreadsheet and/or tab-delimited text files. Our submissions templates are fully compliant with the data required for ClinVar allowing us to broker the submission of all EVA Clincal data to our collaborating NCBI resource.</p>'+
                        '<p>Please download our EVA Clinical submission template via this link..</p>'+
                        '<p>A completed EVA Clinical template for a fictional submission can be downloaded here.</p>'+
                        '</div>' +
                      '</div>'

        }

}).directive('about', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template: '<div>' +
                        '<h2>About EVA</h2>'+
                        '<div>'+
                        '<p>The European Variation Archive (EVA) accepts submission of, and provides access to, all types of genetic variations from any species, observed germline or somatic sources, ranging from SNVs to large structural variation.</p>'+
                        '<p>All of our data is open access via direct query through web and/or programmatic interfaces.</p>'+
                        '<p>The main submission format is a VCF file. VCF files should provide either genotypes from individual samples or aggregate summary information, such as allele frequencies, and be accompanyed by descriptive metadata. Examples of such include: study description, sample origins, experimental details, methodology for variant and/or genotype calls, etc. We strongly encourage the submission of as much metadata possible as this is information is extremely useful for downstream analysis.</p>'+
                        '<p>In addition to archiving VCF data, each submission is also standardized to permit variant accessioning, global quering, re-mapping across genome builds and calculation of aggregate summary information based on the individual level genotypes.</p>'+
                        '</div>'+
                        '<div><h2>How to submit Data</h2><h3>European Variation Archive submissions</h3>'+
                        '<div>' +
                        '<p>EVA follows the infrastructure of fellow EMBL-EBI resources European Nucleotide Archive (<a href="http://www.ebi.ac.uk/ena/" target="_blank">ENA</a>) and European Genome-phenome Archive (<a href="http://www.ebi.ac.uk/ega/" target="_blank">EGA</a>)  to accept, archive, and accession <a href="?vcf">VCF files</a>. Submissions consist of VCF file(s) and metadata that describe sample(s), experiment(s), and analysis that produced the variant and/or genotype call(s).</p>' +
                        '</div>'+
                        '<h3>Key stages of EVA submissions</h3>'+
                        '<h4>Contact</h4>'+
                        '<div>'+
                        '<p><img src="img/Contact.png" alt="" width="48" height="48"> Contact the EVA Helpdesk via this webform in order to provide details of your submission.</p>'+
                        '</div>'+
                        '<h4>Receive</h4>'+
                        '<div>'+
                        '<p><img src="img/Receive.png" alt="" width="48" height="48"> &nbsp;Receive your submission pack, which will include:'+
                        '<div style="margin-left:70px;">'+
                        '<ul>'+
                        '<li>Details for your submission uploads </li>'+
                        '<li> <a href="?templates">Templates</a> to capture your associated metadata </li>'+
                        '<li>Key stages for your submission</li>'+
                        '</ul>'+
                        '</div>'+
                        '</p>'+
                        '</div>'+
                        '<h4>Submit</h4>'+
                        '<div>'+
                        '<p><img src="img/Upload.png" alt="" width="48" height="48"> Upload your data files to your private submission upload account or directly to the <a href="mailto:eva-helpdesk@ebi.ac.uk?subject=Sequence submissionto EVA&amp;body=**We will provide you with a submission package metadata templates guides for completing templates**" target="_top">EVA Helpdesk</a>.</p>'+
                        '</div>'+
                        '<h4>Document</h4>'+
                        '<div>'+
                        '<p><img src="img/Document.png" alt="" width="48" height="48"> Provide details of your study, samples, experiments, runs/analysis, policy and datasets </p>'+
                        '</div></div>'+
                      '</div>'

        }

}).directive('contactUs', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template: '<div>' +
                        '<h2>Contact Us</h2>'+
                        '<div>'+
                            '<p>If you have any questions related to the European Variation Archive resource, please <a href="mailto:eva-helpdesk@ebi.ac.uk">contact us</a>.</p>'+
                            '<p>Follow us on Twitter using <a href="https://twitter.com/EBIvariation" target="_blank">@EBIvariation</a></p>'+
                        '</div>' +
                     '</div>'


        }

}).directive('submissionForm', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            templateUrl: 'views/submission-form.html',
            controller: function($scope) {
              $scope.name = '';
              $scope.email = '';
              $scope.company = '';
              $scope.acronym = '';
              $scope.webpage = '';
              $scope.genomicDNA = '';
              $scope.exonicDNA = '';
              $scope.transcribedRNA = '';
              $scope.other = '';
              $scope.taxID = '';
//              $scope.subSubmit = function(){
//                  console.log($scope.name);
//                  console.log($scope.email);
//                  console.log($scope.company);
//                  console.log($scope.acronym);
//                  console.log($scope.webpage);
//                  console.log($scope.genomicDNA);
//                  console.log($scope.exonicDNA);
//                  console.log($scope.transcribedRNA);
//                  console.log($scope.other);
//              }

            },
            link: function($scope, element, attr) {
                element.bind('change', function( evt ) {

                    if (window.File && window.FileReader && window.FileList && window.Blob) {
                        var fileSelected = document.getElementById('vcf_file').files;
                        var fileTobeRead = fileSelected[0];
                        var fileReader = new FileReader();
                        fileReader.readAsText(fileTobeRead);
                        fileReader.onload = function (e) {
                           var content   = fileReader.result;
                           processFileData(content);

                        }

                    }



                    function processFileData(content) {
                        var allTextLines = content.split(/\r\n|\n/);
                        //console.log(allTextLines)
                        var lines = [];
                        var tarr = [];
                        for (var i=0; i<allTextLines.length; i++) {
                            if(allTextLines[i].substring(0,2) == '##'){

                                allTextLines[i] = allTextLines[i].substring(2);
                                console.log(allTextLines[i].substring(0,3))
                                if(allTextLines[i].substring(0,3) !== 'INF' && allTextLines[i].substring(0,3) !== 'FOR' && allTextLines[i].substring(0,3) !== 'ALT'){
                                    allTextLines[i] = allTextLines[i].split("=");
                                    console.log(allTextLines[i])
                                    var attr = allTextLines[i][0];
                                    var value = allTextLines[i][1];
                                    tarr.push({'+attr+':value});
                                }

                            }
                        }
                        lines.push(tarr);
                        console.log(lines);
                    }

                });




            }

        }

}).directive('submissionStartForm', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template:  '<form id="contact" ng-submit="subSubmit()">'+
                        '<h4>User Details</h4>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6>Full Name <span class="required">*</span></h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name" ng-model=name  type="text" required/></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6>Email <span class="required">*</span></h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name" ng-model=email  type="email" required/></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6>Institution/Company Name   <span class="required"></span></h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name" ng-model=company  type="text"/></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Webpage  <span class="required">*</span></h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name" ng-model=webpage  type="text"/></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Country of Origin  <span class="required">*</span></h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name" ng-model=country  type="text" required/></div>'+
                        '</div>'+
                        '<h4>Type of Submission  <span class="required">*</span></h4>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Genomic DNA</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input type="checkbox"  ng-model=genomicDNA ng-true-value="Genomic DNA"  ></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Exonic DNA</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input type="checkbox"  ng-model=exonicDNA  ng-true-value="Exonic DNA" ></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Transcribed RNA</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input type="checkbox"  ng-model=transcribedRNA  ng-true-value="Transcribed RNA" ></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Clinical</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input type="checkbox"  ng-model=clinical ng-true-value="Clinical"  ></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Unknown</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input type="checkbox"  ng-model=unknown ng-true-value="Unknown" ></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6> Other</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input type="checkbox"  ng-model=other ng-true-value="Other" ></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-2 form-group"><h6>Preferred Centre Acronym (subject to availability)</h6></div>'+
                        '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name" ng-model=acronym  type="text"/></div>'+
                        '</div>'+
                        '<div class="row">'+
                        '<div class="col-md-8 form-group"><p><h6>Comments</h6></p><textarea class="form-control" rows="3" ng-model=comments></textarea></div>'+
                        '</div>'+
                        '<div class="row">'+
                            '<div class="col-md-4 form-group"><button class="btn btn-primary" type="submit">Submit</button></div>'+
                        '</div>'+
                        '</form>',
            controller: function($scope) {

                $scope.submissionTypes = [
                                            {name:'Genomic DNA', id:'genomicDNA'},
                                            {name:'Exonic DNA ', id:'exonicDNA'},
                                            {name:'Transcribed RNA', id:'transcribedRNA'},
                                            {name:'Clinical', id:'clinical'},
                                            {name:'Unknown', id:'unknown'},
                                            {name:'Other', id:'other'}
                                         ];
                $scope.name = '';
                $scope.email = '';
                $scope.company = '';
                $scope.acronym = '';
                $scope.webpage = '';
                $scope.genomicDNA = '';
                $scope.exonicDNA = '';
                $scope.transcribedRNA = '';
                $scope.clinical = '';
                $scope.unknown = '';
                $scope.other = '';
                $scope.comments = '';
                $scope.country = '';

                $scope.subSubmit = function(){

                   var submissionTypeValue = [$scope.genomicDNA,$scope.exonicDNA,$scope.transcribedRNA,$scope.clinical,$scope.unknown,$scope.other];

                   var data =[{
                           name:$scope.name,
                           email:$scope.email,
                           company:$scope.company,
                           country:$scope.country,
                           acronym:$scope.acronym,
                           webpage:$scope.webpage,
                           comments:$scope.comments,
                           submissionType:submissionTypeValue,
                           }]
                    console.log(data)

                }
            }
        }
});