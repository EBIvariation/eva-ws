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
}).directive('templates', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template: '<div>' +
                        '<h3>Templates</h3>'+
                        '<div>'+
                        '<p>EVA submissions are accompanied by an Excel spreadsheet and/or tab-delimited text files which describe the meta-data. We encourage our users to submit as much metadata as possible.</p>'+
                        '<p>Please download our submission template via this <a href="files/EVA_Submission_template.EVA_.V1.0.1_live.xlsx" target="_blank">link</a>.</p>'+
                        '<p>A completed template for a fictional submission can be downloaded <a href="files/EVA_Submission_template.EVA_.V1.0.1_live_mockup.xlsx" target="_blank">here</a>.</p>'+
                        '</div>' +
                      '</div>'

        }

}).directive('about', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            template: '<div>' +
                        '<h3>About EVA</h3>'+
                        '<div>'+
                        '<p>The European Variation Archive (EVA) accepts submission of, and provides access to, all types of genetic variations from any species, observed germline or somatic sources, ranging from SNVs to large structural variation.</p>'+
                        '<p>All of our data is open access via direct query through web and/or programmatic interfaces.</p>'+
                        '<p>The main submission format is a VCF file. VCF files should provide either genotypes from individual samples or aggregate summary information, such as allele frequencies, and be accompanyed by descriptive metadata. Examples of such include: study description, sample origins, experimental details, methodology for variant and/or genotype calls, etc. We strongly encourage the submission of as much metadata possible as this is information is extremely useful for downstream analysis.</p>'+
                        '<p>In addition to archiving VCF data, each submission is also standardized to permit variant accessioning, global quering, re-mapping across genome builds and calculation of aggregate summary information based on the individual level genotypes.</p>'+
                        '</div>'+
                      '</div>'

        }

});