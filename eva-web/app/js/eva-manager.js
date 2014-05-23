/**
 * Created by jag on 23/05/2014.
 */
var evaManager = {
    get: function (args) {

        var success = args.success;
        var error = args.error;
        var async = (_.isUndefined(args.async) || _.isNull(args.async) ) ? true : args.async;
        var urlConfig = _.omit(args, ['success', 'error', 'async']);

        var url = evaManager.url(urlConfig);

        if(typeof url === 'undefined'){
            return;
        }

        var d;
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'json',//still firefox 20 does not auto serialize JSON, You can force it to always do the parsing by adding dataType: 'json' to your call.
            async: async,
            success: function (data, textStatus, jqXHR) {
                if($.isPlainObject(data) || $.isArray(data)){//
                    if (_.isFunction(success)) success(data);
                    d = data;
                }else{
                    console.log('EVA WS returned a non json object or list, please check the url.');
                    console.log(url);
                    console.log(data)
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("EVA WS: Ajax call returned : " + errorThrown + '\t' + textStatus + '\t' + jqXHR.statusText + " END");
                console.log(url);
                if (_.isFunction(error)) error(jqXHR, textStatus, errorThrown);
            }
        });
        return d;
    },
    url: function (args) {


        if (!$.isPlainObject(args)) args = {};
        if (!$.isPlainObject(args.params)) args.params = {};

        var version = 'latest';
        if(typeof METADATA_VERSION !== 'undefined'){
            version = METADATA_VERSION
        }
        if(typeof args.version !== 'undefined' && args.version != null){
            version = args.version
        }

        var host;
        if(typeof METADATA_HOST !== 'undefined'){
            host = METADATA_HOST
        }


        if (typeof args.host !== 'undefined' && args.version != null) {
            host =  args.host;
        }


        if(typeof host === 'undefined'){
            console.log("METADATA HOST is not configured");
            return;
        }

        delete args.host;
        delete args.version;



        var config = {
            host: host,
            version: version
        };

        var params = {
            of: 'json'
        };

        _.extend(config, args);
        _.extend(config.params, params);

        var query = '';
        if(typeof config.query !== 'undefined' && config.query != null){
            if ($.isArray(config.query)) {
                config.query = config.query.toString();
            }
            query = '/' + config.query;
        }

        var url = config.host + '/' + config.version +  '/' + config.category + '/'  + query + '/' + config.resource;
        url = Utils.addQueryParamtersToUrl(config.params, url);


        return url;
    }
};