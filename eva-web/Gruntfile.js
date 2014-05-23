/*global module:false*/
module.exports = function (grunt) {

    // Project configuration.
    grunt.initConfig({

        // Metadata.
        meta: {
            version: {
                eva: '1.0.0'
            }
        },

        bannereva: '/*! EVA - v<%= meta.version.eva %> - ' +
            '<%= grunt.template.today("yyyy-mm-dd HH:MM:ss") %>\n' +
            '* http://https://github.com/EBIvariation/eva.git/\n' +
            '* Copyright (c) <%= grunt.template.today("yyyy") %> ' +
            ' ' +
            'Licensed GPLv2 */\n',
        // Task configuration.

        concat: {
            options: {
                banner: '<%= bannereva %>',
                stripBanners: true
            },


            eva: {
                src: [
                    /** eva app js **/
                    'app/js/eva-manager.js',
                    'app/js/app.js',

                    /** ebi-compliance **/
                    'lib/ebi-js-commons/ebi-compliance/js/ebi-complaince-directive.js',

                    /** eva controllers **/
                    'app/js/controllers/eva-main-controller.js',
                    'app/js/controllers/backbone-event-manager.js',

                    /** eva-directives **/
                    'app/js/directives/eva-portal-home-drirective.js',
                    'app/js/directives/variant-widget-directive.js',
                    'app/js/directives/gene-view-directive.js',

                     /** eva-widgets **/
                    'app/widgets/variant-browser-widget.js',
                    'app/widgets/gene-widget.js',
                    'app/widgets/variant-genotype.js',
                    'app/widgets/variant-effects.js',

                    /** eva-services **/
                    'app/js/ebi-var-services/ebivar-services-metadata.js'

                ],
                dest: 'build/<%= meta.version.eva %>/eva-<%= meta.version.eva %>.js'
            }

        },
        uglify: {

            eva: {
                src: '<%= concat.eva.dest %>',
                dest: 'build/<%= meta.version.eva %>/eva-<%= meta.version.eva %>.min.js'
            }
        },
        jshint: {
            options: {
                curly: true,
                eqeqeq: true,
                immed: true,
                latedef: true,
                newcap: true,
                noarg: true,
                sub: true,
                undef: true,
                unused: true,
                boss: true,
                eqnull: true,
                browser: true,
                globals: {}
            },
            gruntfile: {
                src: 'Gruntfile.js'
            },
            lib_test: {
                src: ['lib/**/*.js', 'test/**/*.js']
            }
        },
        qunit: {
            files: ['test/**/*.html']
        },

        watch: {
            gruntfile: {
                files: '<%= jshint.gruntfile.src %>',
                tasks: ['jshint:gruntfile']
            },
            lib_test: {
                files: '<%= jshint.lib_test.src %>',
                tasks: ['jshint:lib_test', 'qunit']
            }
        },

        copy: {

            eva: {
                files: [
                    {   expand: true, src: ['app/views/*'], dest: 'build/<%= meta.version.eva %>/views', flatten: true},
                    {   expand: true, src: ['vendor/**'], dest: 'build/<%= meta.version.eva %>'},
                    {   expand: true, src: ['app/css/*'], dest: 'build/<%= meta.version.eva %>/css', flatten: true},
                    {   expand: true, src: ['app/js/gv-config.js'], dest: 'build/<%= meta.version.eva %>/', flatten: true}
                ]
            }

        },

        clean: {
            eva: ['build/<%= meta.version.eva %>/']
        },

        htmlbuild: {
            eva: {
                src: 'app/index.html',
                dest: 'build/<%= meta.version.eva %>/',
                options: {
                    beautify: true,
                    scripts: {
                        'eva-js': '<%= concat.eva.dest %>',
                        'vendor': [
                            'build/<%= meta.version.eva %>/vendor/jquery.min.js',
                            'build/<%= meta.version.eva %>/vendor/angular-1.2.16.min.js',
                            'build/<%= meta.version.eva %>/vendor/underscore-1.5.2.min.js',
                            'build/<%= meta.version.eva %>/vendor/backbone-1.1.2-min.js',
                            'build/<%= meta.version.eva %>/vendor/bootstrap/js/bootstrap.min.js',
                            'build/<%= meta.version.eva %>/vendor/ui-bootstrap-tpls-0.10.0.min.js',
                            'build/<%= meta.version.eva %>/vendor/highcharts-4.0.1.min.js',
                            'build/<%= meta.version.eva %>/vendor/extJS/js/ext-all.js',
                            'build/<%= meta.version.eva %>/vendor/angular-ui-router.min.js',
                            'build/<%= meta.version.eva %>/vendor/checklist-model.js',
                            'build/<%= meta.version.eva %>/vendor/angular-scroll.min.js',
                            'build/<%= meta.version.eva %>/vendor/extJS/ux/SlidingPager.js',
                            'build/<%= meta.version.eva %>/vendor/extJS/ux/data/PagingMemoryProxy.js',
//                            'build/<%= meta.version.eva %>/vendor/gv-config.js',
                            'build/<%= meta.version.eva %>/vendor/genome-viewer-1.0.3.min.js'
                        ],
                        'internal-dependencies': [
                            'build/<%= meta.version.eva %>/gv-config.js'
                        ]
                    },
                    styles: {
                        'eva-css': [
                            'build/<%= meta.version.eva %>/app/css/eva-portal-colours.css',
                            'build/<%= meta.version.eva %>/app/css/eva-portal-visual.css',
                            'build/<%= meta.version.eva %>/app/css/eva.css'
                        ],
                        'vendor': [
                            'build/<%= meta.version.eva %>/vendor/bootstrap/css/bootstrap.min.css',
                            'build/<%= meta.version.eva %>/vendor/bootstrap/css/bootstrap-theme.min.css',
                            'build/<%= meta.version.eva %>/vendor/extJS/css/resources/css/ext-all.css'


                        ]
                    }
                }
            }
        },

        'curl-dir': {
            long: {
                src: [
                    'http://ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.js',
                    'http://ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.map',
                    'http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.4.4/underscore-1.5.2.min.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.0.0/backbone-min.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.0.0/backbone-min.map',
                    'http://hub.chemdoodle.com/cwc/5.1.0/ChemDoodleWeb.css',
                    'http://hub.chemdoodle.com/cwc/5.1.0/ChemDoodleWeb.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/jquery-mousewheel/3.0.6/jquery.mousewheel.min.js',
                    'https://raw.github.com/toji/gl-matrix/master/dist/gl-matrix-min.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.3.1/jquery.cookie.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/jquery-url-parser/2.2.1/purl.min.js',
                    'http://jsapi.bioinfo.cipf.es/ext-libs/jquery-plugins/jquery.sha1.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/qtip2/2.1.1/jquery.qtip.min.js',
                    'http://cdnjs.cloudflare.com/ajax/libs/qtip2/2.1.1/jquery.qtip.min.css',
//                    'http://jsapi.bioinfo.cipf.es/ext-libs/qtip2/jquery.qtip.min.js',
//                    'http://jsapi.bioinfo.cipf.es/ext-libs/qtip2/jquery.qtip.min.css',
                    'http://jsapi.bioinfo.cipf.es/ext-libs/rawdeflate.js'
                ],
                dest: 'vendor'
            }
        },

        watch: {
            src: {
                files: ['app/**'],
                tasks: ['default'],
                options: {spawn: false}
            }

        }
    });


    // These plugins provide necessary tasks.
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
//    grunt.loadNpmTasks('grunt-contrib-qunit');
//    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-rename');
    grunt.loadNpmTasks('grunt-html-build');
    grunt.loadNpmTasks('grunt-curl');


    grunt.registerTask('vendor', ['curl-dir']);

    // Default task.
    grunt.registerTask('default', ['clean:eva','concat:eva','uglify:eva', 'copy:eva', 'htmlbuild:eva'])


//    grunt.registerTask('clean', ['clean:eva']);

};
