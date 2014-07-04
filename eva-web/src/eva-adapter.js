/**
 * Created by fsalavert on 03/07/14.
 */
function EvaAdapter(args) {

    _.extend(this, Backbone.Events);

    this.host;
    this.version;

    _.extend(this, args);

    this.on(this.handlers);

    this.cache = {};
}

EvaAdapter.prototype = {

    getData: function (args) {
        var _this = this;

        args.webServiceCallCount = 0;

        /** Check region and parameters **/
        var region = args.region;
        if (region.start > 300000000 || region.end < 1) {
            return;
        }
        region.start = (region.start < 1) ? 1 : region.start;
        region.end = (region.end > 300000000) ? 300000000 : region.end;


        var params = {};
        _.extend(params, this.params);
        _.extend(params, args.params);

        var dataType = args.dataType;
        if (_.isUndefined(dataType)) {
            console.log("dataType must be provided!!!");
        }
        var chunkSize;


        /** Check dataType histogram  **/
        if (dataType == 'histogram') {
            // Histogram chunks will be saved in different caches by interval size
            // The chunkSize will be the histogram interval
            var histogramId = dataType + '_' + params.interval;
            if (_.isUndefined(this.cache[histogramId])) {
                this.cache[histogramId] = new FeatureChunkCache({chunkSize: params.interval});
            }
            chunkSize = this.cache[histogramId].chunkSize;

            // Extend region to be adjusted with the chunks
            //        --------------------             -> Region needed
            // |----|----|----|----|----|----|----|    -> Logical chunk division
            //      |----|----|----|----|----|         -> Chunks covered by needed region
            //      |------------------------|         -> Adjusted region
            var adjustedRegions = this.cache[histogramId].getAdjustedRegions(region);
            if (adjustedRegions.length > 0) {
                args.webServiceCallCount++;
                // Get CellBase data
                EvaManager.get({
                    host: this.host,
                    version: this.version,
                    species: this.species,
                    category: this.category,
                    query: adjustedRegions,
                    resource: this.resource,
                    params: params,
                    success: function (data) {
                        _this._histogramSuccess(data, dataType, histogramId, args);
                    }
                });
            }
            // Get chunks from cache
            var chunksByRegion = this.cache[histogramId].getCachedByRegion(region);
            var chunksCached = this.cache[histogramId].getByRegions(chunksByRegion.cached);
            this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});


            /** Features: genes, snps ... **/
        } else {
            // Features will be saved using the dataType features
            if (_.isUndefined(this.cache[dataType])) {
                this.cache[dataType] = new FeatureChunkCache(this.cacheConfig);
            }
            chunkSize = this.cache[dataType].chunkSize;

            // Get cached chunks and not cached chunk regions
            //        --------------------             -> Region needed
            // |----|----|----|----|----|----|----|    -> Logical chunk division
            //      |----|----|----|----|----|         -> Chunks covered by needed region
            //      |----|++++|++++|----|----|         -> + means the chunk is cached so its region will not be retrieved
            var chunksByRegion = this.cache[dataType].getCachedByRegion(region);

            if (chunksByRegion.notCached.length > 0) {
                var queryRegionStrings = _.map(chunksByRegion.notCached, function (region) {
                    return new Region(region).toString();
                });

                // Multiple CellBase calls will be performed, each one will
                // query 50 or less chunk regions
                var n = 50;
                var lists = _.groupBy(queryRegionStrings, function (a, b) {
                    return Math.floor(b / n);
                });
                // Each element on queriesList contains and array of 50 or less regions
                var queriesList = _.toArray(lists); //Added this to convert the returned object to an array.

                for (var i = 0; i < queriesList.length; i++) {
                    args.webServiceCallCount++;
                    EvaManager.get({
                        host: this.host,
                        version: this.version,
                        species: this.species,
                        category: this.category,
                        query: queriesList[i],
                        resource: this.resource,
                        params: params,
                        success: function (data) {
                            _this._success(data, dataType, args);
                        }
                    });
                }
            }
            // Get chunks from cache
            if (chunksByRegion.cached.length > 0) {
                var chunksCached = this.cache[dataType].getByRegions(chunksByRegion.cached);
                this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});
            }
        }
        if (args.webServiceCallCount === 0) {
            args.done();
        }
    },

    _success: function (data, dataType, args) {
        args.webServiceCallCount--;
        var timeId = this.resource + " save " + Utils.randomString(4);
        console.time(timeId);
        /** time log **/

        var chunkSize = this.cache[dataType].chunkSize;

        var chunks = [];
        for (var i = 0; i < data.response.length; i++) {
            var queryResult = data.response[i];

            var region = new Region(queryResult.id);
            var features = queryResult.result;
            var chunk = this.cache[dataType].putByRegion(region, features);
            chunks.push(chunk);
        }

        /** time log **/
        console.timeEnd(timeId);


        if (chunks.length > 0) {
            this.trigger('data:ready', {items: chunks, dataType: dataType, chunkSize: chunkSize, sender: this});
        }
        if (args.webServiceCallCount === 0) {
            args.done();
        }


    },
    _histogramSuccess: function (data, dataType, histogramId, args) {
        args.webServiceCallCount--;
        var timeId = Utils.randomString(4);
        console.time(this.resource + " save " + timeId);
        /** time log **/

        var chunkSize = this.cache[histogramId].chunkSize;

        var chunks = [];
        for (var i = 0; i < data.response.length; i++) {
            var queryResult = data.response[i];
            for (var j = 0; j < queryResult.result.length; j++) {
                var interval = queryResult.result[j];
                var region = new Region(queryResult.id);
                region.load(interval);
                chunks.push(this.cache[histogramId].putByRegion(region, interval));
            }
        }

        this.trigger('data:ready', {items: chunks, dataType: dataType, chunkSize: chunkSize, sender: this});
        if (args.webServiceCallCount === 0) {
            args.done();
        }

        /** time log **/
        console.timeEnd(this.resource + " get and save " + timeId);
    }
};