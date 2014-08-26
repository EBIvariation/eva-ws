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
                    'src/eva.js',
//                    'src/eva-study-browser-panel.old.js',
                    'src/eva-variant-widget.js',
                    'src/eva-menu.js',
                    'src/eva-adapter.js',
                    'src/eva-manager.js',
                    'src/eva-config.js'
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
                    {   expand: true, src: ['src/files/*'], dest: 'build/<%= meta.version.eva %>/files', flatten: true},
                    {   expand: true, src: ['src/fonts/*'], dest: 'build/<%= meta.version.eva %>/fonts', flatten: true},
                    {   expand: true, src: ['src/css/*'], dest: 'build/<%= meta.version.eva %>/css', flatten: true},
                    {   expand: true, src: ['src/img/*'], dest: 'build/<%= meta.version.eva %>/img', flatten: true},
                    {   expand: true, src: ['src/*.html'], dest: 'build/<%= meta.version.eva %>/', flatten: true, filter: 'isFile'},
                    {   expand: true, src: ['lib/jsorolla/build/**'], dest: 'build/<%= meta.version.eva %>',flatten: false},
                    {   expand: true, src: ['lib/jsorolla/vendor/**'], dest: 'build/<%= meta.version.eva %>',flatten: false},
                    {   expand: true, src: ['lib/jsorolla/styles/**'], dest: 'build/<%= meta.version.eva %>',flatten: false},
                    {   expand: true, src: ['vendor/**'], dest: 'build/<%= meta.version.eva %>'}

                ]
            }

        },

        clean: {
            eva: ['build/<%= meta.version.eva %>/']
        },

        htmlbuild: {
            eva: {
                src: 'src/eva.html',
                dest: 'build/<%= meta.version.eva %>/',
                options: {
                    beautify: true,
                    scripts: {
                        'eva-js': '<%= uglify.eva.dest %>',
                        'lib': [
//                                'build/<%= meta.version.eva %>/lib/jsorolla/build/1.1.6/genome-viewer/gv-config.js',
//                                'build/<%= meta.version.eva %>/lib/jsorolla/build/1.1.6/genome-viewer/genome-viewer.js',
                                'build/<%= meta.version.eva %>/lib/jsorolla/build/1.1.6/lib.js'
                               ],
                        'vendor': [
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/underscore-min.js',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/backbone-min.js',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/jquery.min.js',
                            'build/<%= meta.version.eva %>/vendor/bootstrap-3.2.0/js/bootstrap.min.js',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/jquery.cookie.js',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/jquery.sha1.js',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/purl.min.js',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/jquery.qtip.min.js'
                        ],
                        'platform': [
                            'build/<%= meta.version.eva %>/vendor/platform.js'
                        ]
//                        'internal-dependencies': [
//                            'build/<%= meta.version.eva %>/gv-config.js'
//                        ]
                    },
                    styles: {
                        'css': [
                            'build/<%= meta.version.eva %>/lib/jsorolla/styles/css/style.css',
                            'build/<%= meta.version.eva %>/css/eva.css'
                        ],
                        'vendor': [
                            'build/<%= meta.version.eva %>/vendor/ext-5/theme-ebi-embl/theme-ebi-embl-all.css',
                            'build/<%= meta.version.eva %>/lib/jsorolla/vendor/jquery.qtip.min.css',
                            'build/<%= meta.version.eva %>/vendor/bootstrap-3.2.0/css/bootstrap.min.css'
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
