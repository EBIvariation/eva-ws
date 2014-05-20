/*! EVA - v1.0 - 2014-05-20 14:58:13
* http://https://github.com/EBIvariation/eva.git/
* Copyright (c) 2014  Licensed GPLv2 */
function CellBaseAdapter(args) {

    _.extend(this, Backbone.Events);

    _.extend(this, args);

    this.on(this.handlers);

    this.cache = {};
}

CellBaseAdapter.prototype = {

    getData: function (args) {
        var _this = this;

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
                // Get CellBase data
                CellBaseManager.get({
                    host: this.host,
                    species: this.species,
                    category: this.category,
                    subCategory: this.subCategory,
                    query: adjustedRegions,
                    resource: this.resource,
                    params: params,
                    success: function (data) {
                        _this._cellbaseHistogramSuccess(data, dataType, histogramId);
                    }
                });
            } else {
                // Get chunks from cache
                var chunksByRegion = this.cache[histogramId].getCachedByRegion(region);
                var chunksCached = this.cache[histogramId].getByRegions(chunksByRegion.cached);
                this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});
            }

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
                    CellBaseManager.get({
                        host: this.host,
                        species: this.species,
                        category: this.category,
                        subCategory: this.subCategory,
                        query: queriesList[i],
                        resource: this.resource,
                        params: params,
                        success: function (data) {
                            _this._cellbaseSuccess(data, dataType);
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

    },

    _cellbaseSuccess: function (data, dataType) {
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


    },
    _cellbaseHistogramSuccess: function (data, dataType, histogramId) {
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
        /** time log **/
        console.timeEnd(this.resource + " get and save " + timeId);
    }
};


function SequenceAdapter(args) {

    _.extend(this, Backbone.Events);

    this.id = Utils.genId("TrackListPanel");

    //set default args
    this.host;
    this.gzip = true;

    //set instantiation args, must be last
    _.extend(this, args);

    this.sequence = {};
    this.start = {};
    this.end = {};

    this.on(this.handlers);
}

SequenceAdapter.prototype.clearData = function () {
    this.sequence = {};
    this.start = {};
    this.end = {};
};

SequenceAdapter.prototype.getData = function (args) {
    var _this = this;

    this.sender = args.sender;
    var region = args.region;
    var chromosome = region.chromosome;

    region.start = (region.start < 1) ? 1 : region.start;
    region.end = (region.end > 300000000) ? 300000000 : region.end;

    //clean when the new position is too far from current
    if (region.start < this.start[chromosome] - 5000 || region.end > this.end[chromosome] + 5000) {
        this.clearData();
    }

    var params = {};
    _.extend(params, this.params);


    var queryString = this._getSequenceQuery(region);

    if (queryString != "") {

        CellBaseManager.get({
            host: this.host,
            species: this.species,
            category: this.category,
            subCategory: this.subCategory,
            query: queryString,
            resource: this.resource,
            params: params,
            success: function (data) {
                _this._processSequenceQuery(data, true);
            }
        });


    } else {
        if (this.sender != "move") {
            this.trigger('data:ready', {
                items: {
                    sequence: this.sequence[chromosome],
                    start: this.start[chromosome],
                    end: this.end[chromosome]
                },
                params: params
            });
            this.trigger('data:ready', {
                items: {
                    sequence: this.sequence[chromosome],
                    start: this.start[chromosome],
                    end: this.end[chromosome]
                },
                params: params,
                sender: this
            });
        }
    }

};

SequenceAdapter.prototype._getSequenceQuery = function (region) {
    var _this = this;
    var chromosome = region.chromosome;

    var s, e, query, querys = [];
    if (_this.start[chromosome] == null && _this.end[chromosome] == null) {
        //args.start -= 100;
        //args.end += 100;
        _this.start[chromosome] = region.start;
        _this.end[chromosome] = region.end;
        s = region.start;
        e = region.end;
        query = chromosome + ":" + s + "-" + e;
        querys.push(query);
    } else {
        if (region.start <= _this.start[chromosome]) {
            s = region.start;
            e = _this.start[chromosome] - 1;
            e = (e < 1) ? region.end = 1 : e;
            _this.start[chromosome] = s;
            query = region.chromosome + ":" + s + "-" + e;
            querys.push(query);
        }
        if (region.end >= _this.end[chromosome]) {
            e = region.end;
            s = _this.end[chromosome] + 1;
            _this.end[chromosome] = e;
            query = region.chromosome + ":" + s + "-" + e;
            querys.push(query);
        }
    }
    return querys.toString();
};

SequenceAdapter.prototype._processSequenceQuery = function (data, throwNotify) {
    var _this = this;
    var params = data.params;


    for (var i = 0; i < data.response.length; i++) {
        var queryResponse = data.response[i];
        var splitDots = queryResponse.id.split(":");
        var splitDash = splitDots[1].split("-");
        var queryStart = parseInt(splitDash[0]);
        var queryEnd = parseInt(splitDash[1]);

        var queryId = queryResponse.id;
        var seqResponse = queryResponse.result;

        var chromosome = seqResponse.chromosome;
        if(typeof chromosome === 'undefined'){
            chromosome = seqResponse.seqName;
        }

        if (this.sequence[chromosome] == null) {
            this.sequence[chromosome] = seqResponse.sequence;
        } else {
            if (queryStart == this.start[chromosome]) {
                this.sequence[chromosome] = seqResponse.sequence + this.sequence[chromosome];
            } else {
                this.sequence[chromosome] = this.sequence[chromosome] + seqResponse.sequence;
            }
        }

        if (this.sender == "move" && throwNotify == true) {
            this.trigger('data:ready', {
                items: {
                    sequence: seqResponse.sequence,
                    start: queryStart,
                    end: queryEnd
                },
                params: params,
                sender: this
            });
        }
    }

    if (this.sender != "move" && throwNotify == true) {
        this.trigger('data:ready', {
            items: {
                sequence: this.sequence[chromosome],
                start: this.start[chromosome],
                end: this.end[chromosome]
            },
            params: params,
            sender: this
        });
    }
};

//Used by bam to get the mutations
SequenceAdapter.prototype.getNucleotidByPosition = function (args) {
    var _this = this;
    if (args.start > 0 && args.end > 0) {
        var queryString = this._getSequenceQuery(args);

        var chromosome = args.chromosome;

        if (queryString != "") {

            var data = CellBaseManager.get({
                host: this.host,
                species: this.species,
                category: this.category,
                subCategory: this.subCategory,
                query: queryString,
                resource: this.resource,
                params: this.params,
                async: false
            });
            _this._processSequenceQuery(data);

        }
        if (this.sequence[chromosome] != null) {
            var referenceSubStr = this.sequence[chromosome].substr((args.start - this.start[chromosome]), 1);
            return referenceSubStr;
        } else {
            console.log("SequenceRender: this.sequence[chromosome] is undefined");
            return "";
        }
    }
};

function OpencgaAdapter(args) {

    _.extend(this, Backbone.Events);

    _.extend(this, args);

    this.on(this.handlers);

    this.cache = {};
}

OpencgaAdapter.prototype = {
    getData: function (args) {
        var _this = this;
        /********/

        var region = args.region;
        if (region.start > 300000000 || region.end < 1) {
            return;
        }
        region.start = (region.start < 1) ? 1 : region.start;
        region.end = (region.end > 300000000) ? 300000000 : region.end;

        var params = {species: Utils.getSpeciesCode(this.species.text)};
        _.extend(params, this.params);
        _.extend(params, args.params);

        var dataType = args.dataType;
        if (_.isUndefined(dataType)) {
            console.log("dataType must be provided!!!");
        }
        var chunkSize;
        /********/

        if (dataType == 'histogram') {

        } else {
            //Create one FeatureChunkCache by datatype
            if (_.isUndefined(this.cache[dataType])) {
                this.cache[dataType] = new FeatureChunkCache(this.cacheConfig);
            }
            chunkSize = this.cache[dataType].chunkSize;

            var chunksByRegion = this.cache[dataType].getCachedByRegion(region);

            if (chunksByRegion.notCached.length > 0) {
                var queryRegionStrings = _.map(chunksByRegion.notCached, function (region) {
                    return new Region(region).toString();
                });

                //limit queries
                var n = 50;
                var lists = _.groupBy(queryRegionStrings, function (a, b) {
                    return Math.floor(b / n);
                });
                var queriesList = _.toArray(lists); //Added this to convert the returned object to an array.

                for (var i = 0; i < queriesList.length; i++) {
                    var cookie = $.cookie("bioinfo_sid");
                    cookie = ( cookie != '' && cookie != null ) ? cookie : 'dummycookie';
                    OpencgaManager.region({
                        accountId: this.resource.account,
                        sessionId: cookie,
                        bucketId: this.resource.bucketId,
                        objectId: this.resource.oid,
                        region: queriesList[i],
                        queryParams: params,
                        success: function (data) {
                            _this._opencgaSuccess(data, dataType);
                        }
                    });
//                    CellBaseManager.get({
//                        host: this.host,
//                        species: this.species,
//                        category: this.category,
//                        subCategory: this.subCategory,
//                        query: queriesList[i],
//                        resource: this.resource,
//                        params: params,
//                        success: function (data) {
//                            _this._cellbaseSuccess(data, dataType);
//                        }
//                    });
                }
            }
            if (chunksByRegion.cached.length > 0) {
                var chunksCached = this.cache[dataType].getByRegions(chunksByRegion.cached);
                this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});
            }
        }

    },
    _opencgaSuccess: function (data, dataType) {
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
    }
}


OpencgaAdapter.prototype.getDataOld = function (args) {
    debugger
    var _this = this;
    //region check

    this.params["histogram"] = args.histogram;
    this.params["histogramLogarithm"] = args.histogramLogarithm;
    this.params["histogramMax"] = args.histogramMax;
    this.params["interval"] = args.interval;
    this.params["transcript"] = args.transcript;


    if (args.start < 1) {
        args.start = 1;
    }
    if (args.end > 300000000) {
        args.end = 300000000;
    }

    var type = "data";
    if (args.histogram) {
        type = "histogram" + args.interval;
    }

    var firstChunk = this.featureCache._getChunk(args.start);
    var lastChunk = this.featureCache._getChunk(args.end);

    var chunks = [];
    var itemList = [];
    for (var i = firstChunk; i <= lastChunk; i++) {
        var key = args.chromosome + ":" + i;
        if (this.featureCache.cache[key] == null || this.featureCache.cache[key][type] == null) {
            chunks.push(i);
        } else {
            var items = this.featureCache.getFeatureChunk(key, type);
            itemList = itemList.concat(items);
        }
    }
////	//notify all chunks
//	if(itemList.length>0){
//		this.onGetData.notify({data:itemList, params:this.params, cached:true});
//	}


    //CellBase data process
    //TODO check host
    var calls = 0;
    var querys = [];
    regionSuccess = function (data) {
        console.timeEnd("dqs");
        console.time("dqs-cache");
        var type = "data";
        if (data.params.histogram) {
            type = "histogram" + data.params.interval;
        }
        _this.params["dataType"] = type;

        var splitDots = data.query.split(":");
        var splitDash = splitDots[1].split("-");
        var query = {chromosome: splitDots[0], start: splitDash[0], end: splitDash[1]};

        //check if features contains positon or start-end
        if (data.result[0] != null && data.result[0]['position'] != null) {
            for (var i = 0; i < data.result.length; i++) {
                data.result[i]['start'] = data.result[i].position;
                data.result[i]['end'] = data.result[i].position;
            }
        }

        _this.featureCache.putFeaturesByRegion(data.result, query, _this.category, type);
        var items = _this.featureCache.getFeatureChunksByRegion(query, type);
        console.timeEnd("dqs-cache");
        if (items != null) {
            itemList = itemList.concat(items);
        }
        if (calls == querys.length) {
//			_this.onGetData.notify({items:itemList, params:_this.params, cached:false});
            _this.trigger('data:ready', {items: itemList, params: _this.params, cached: false, sender: _this});
        }
    };

    var updateStart = true;
    var updateEnd = true;
    if (chunks.length > 0) {
//		console.log(chunks);

        for (var i = 0; i < chunks.length; i++) {

            if (updateStart) {
                var chunkStart = parseInt(chunks[i] * this.featureCache.chunkSize);
                updateStart = false;
            }
            if (updateEnd) {
                var chunkEnd = parseInt((chunks[i] * this.featureCache.chunkSize) + this.featureCache.chunkSize - 1);
                updateEnd = false;
            }

            if (chunks[i + 1] != null) {
                if (chunks[i] + 1 == chunks[i + 1]) {
                    updateEnd = true;
                } else {
                    var query = args.chromosome + ":" + chunkStart + "-" + chunkEnd;
                    querys.push(query);
                    updateStart = true;
                    updateEnd = true;
                }
            } else {
                var query = args.chromosome + ":" + chunkStart + "-" + chunkEnd;

                querys.push(query);
                updateStart = true;
                updateEnd = true;
            }
        }
//		console.log(querys)
        for (var i = 0, li = querys.length; i < li; i++) {
            console.time("dqs");
            calls++;
//			opencgaManager.region(this.category, this.resource, querys[i], this.params);
            var cookie = $.cookie("bioinfo_sid");
            cookie = ( cookie != '' && cookie != null ) ? cookie : 'dummycookie';
            OpencgaManager.region({
                accountId: this.resource.account,
                sessionId: cookie,
                bucketId: this.resource.bucketId,
                objectId: this.resource.oid,
                region: querys[i],
                queryParams: this.params,
                success: regionSuccess
            });
        }
    } else {
        if (itemList.length > 0) {
            this.trigger('data:ready', {items: itemList, params: this.params, cached: false, sender: this});
//			this.onGetData.notify({items:itemList, params:this.params});
        }
    }
};
function BamAdapter(args){

    _.extend(this, Backbone.Events);

    if(typeof args != 'undefined'){
        this.host = args.host || this.host;
        this.category = args.category || this.category;
		this.resource = args.resource || this.resource;
		this.params = args.params || this.params;
		this.filters = args.filters || this.filters;
		this.options = args.options || this.options;
        this.species = args.species || this.species;
        var argsFeatureCache = args.featureCache || {};
    }
	if (args != null){
		if(args.featureConfig != null){
			if(args.featureConfig.filters != null){
				this.filtersConfig = args.featureConfig.filters;
			}
			if(args.featureConfig.options != null){//apply only check boxes
				this.optionsConfig = args.featureConfig.options;
				for(var i = 0; i < this.optionsConfig.length; i++){
					if(this.optionsConfig[i].checked == true){
						this.options[this.optionsConfig[i].name] = true;
						this.params[this.optionsConfig[i].name] = true;
					}				
				}
			}
		}
	}

	this.featureCache = new BamCache(argsFeatureCache);
//	this.onGetData = new Event();
}

BamAdapter.prototype = {
    host : null,
    gzip : true,
    params : {}
};

BamAdapter.prototype.clearData = function(){
	this.featureCache.clear();
};

BamAdapter.prototype.setFilters = function(filters){
	this.clearData();
	this.filters = filters;
	for(filter in filters){
		var value = filters[filter].toString();
		delete this.params[filter];
		if(value != ""){
			this.params[filter] = value;
		}
	}
};
BamAdapter.prototype.setOption = function(opt, value){
	if(opt.fetch){
		this.clearData();
	}
	this.options[opt.name] = value;
	for(option in this.options){
		if(this.options[opt.name] != null){
			this.params[opt.name] = this.options[opt.name];
		}else{
			delete this.params[opt.name];
		}
	}
};


BamAdapter.prototype.getData = function(args){
	var _this = this;
	//region check
	this.params["histogram"] = args.histogram;
	this.params["histogramLogarithm"] = args.histogramLogarithm;
	this.params["histogramMax"] = args.histogramMax;
	this.params["interval"] = args.interval;
	this.params["transcript"] = args.transcript;
	this.params["chromosome"] = args.chromosome;
	this.params["resource"] = this.resource.id;
	this.params["category"] = this.category;
	this.params["species"] = Utils.getSpeciesCode(this.species.text);


	if(args.start<1){
		args.start=1;
	}
	if(args.end>300000000){
		args.end=300000000;
	}
	
	var dataType = "data";
	if(args.histogram){
		dataType = "histogram"+args.interval;
	}

	this.params["dataType"] = dataType;
	
	var firstChunk = this.featureCache._getChunk(args.start);
	var lastChunk = this.featureCache._getChunk(args.end);
	var chunks = [];
	var itemList = [];
	for(var i=firstChunk; i<=lastChunk; i++){
		var key = args.chromosome+":"+i;
		if(this.featureCache.cache[key] == null || this.featureCache.cache[key][dataType] == null) {
			chunks.push(i);
		}else{
			var item = this.featureCache.getFeatureChunk(key);
			itemList.push(item);
		}
	}

    var regionSuccess = function (data) {
		var splitDots = data.query.split(":");
		var splitDash = splitDots[1].split("-");
		var query = {chromosome:splitDots[0],start:splitDash[0],end:splitDash[1]};


		var dataType = "data";
		if(data.params.histogram){
			dataType = "histogram"+data.params.interval;
		    _this.featureCache.putHistogramFeaturesByRegion(data.result, query, data.resource, dataType);
		}else{
		    _this.featureCache.putFeaturesByRegion(data.result, query, data.resource, dataType);
        }

		var items = _this.featureCache.getFeatureChunksByRegion(query, dataType);
		itemList = itemList.concat(items);
		if(itemList.length > 0){
            _this.trigger('data:ready',{items:itemList, params:_this.params, cached:false, sender:_this});
//			_this.onGetData.notify({items:itemList, params:_this.params, cached:false});
		}
	};

	var querys = [];
	var updateStart = true;
	var updateEnd = true;
	if(chunks.length > 0){//chunks needed to retrieve
//		console.log(chunks);
		
		for ( var i = 0; i < chunks.length; i++) {
			
			if(updateStart){
				var chunkStart = parseInt(chunks[i] * this.featureCache.chunkSize);
				updateStart = false;
			}
			if(updateEnd){
				var chunkEnd = parseInt((chunks[i] * this.featureCache.chunkSize) + this.featureCache.chunkSize-1);
				updateEnd = false;
			}
			
			if(chunks[i+1]!=null){
				if(chunks[i]+1==chunks[i+1]){
					updateEnd =true;
				}else{
					var query = args.chromosome+":"+chunkStart+"-"+chunkEnd;
					querys.push(query);
					updateStart = true;
					updateEnd = true;
				}
			}else{
				var query = args.chromosome+":"+chunkStart+"-"+chunkEnd;
				querys.push(query);
				updateStart = true;
				updateEnd = true;
			}
		}
//		console.log(querys);
		for ( var i = 0, li = querys.length; i < li; i++) {
			console.time("dqs");
			//accountId, sessionId, bucketname, objectname, region,
            var cookie = $.cookie("bioinfo_sid");
            cookie = ( cookie != '' && cookie != null ) ?  cookie : 'dummycookie';
            OpencgaManager.region({
                accountId: this.resource.account,
                sessionId: cookie,
                bucketId: this.resource.bucketId,
                objectId: this.resource.oid,
                region: querys[i],
                queryParams: this.params,
                success:regionSuccess
            });
		}
	}else{//no server call
		if(itemList.length > 0){
            _this.trigger('data:ready',{items:itemList, params:this.params, cached:false, sender:this});
//			this.onGetData.notify({items:itemList, params:this.params});
		}
	}
};

function FeatureDataAdapter(dataSource, args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.dataSource = dataSource;
    this.gzip = true;

    this.params = {};
    if (args != null) {
        if (args.gzip != null) {
            this.gzip = args.gzip;
        }
        if (args.species != null) {
            this.species = args.species;
        }
        if (args.params != null) {
            this.params = args.params;
        }
    }

    this.featureCache = new FeatureCache({chunkSize: 10000, gzip: this.gzip});

//	this.onLoad = new Event();
//	this.onGetData = new Event();

    //chromosomes loaded
    this.chromosomesLoaded = {};
}

FeatureDataAdapter.prototype.getData = function (args) {
    console.log("TODO comprobar histograma");
    console.log(args.region);
    this.params["dataType"] = "data";
    this.params["chromosome"] = args.region.chromosome;

    //check if the chromosome has been already loaded
    if (this.chromosomesLoaded[args.region.chromosome] != true) {
        this._fetchData(args.region);
        this.chromosomesLoaded[args.region.chromosome] = true;
    }

    var itemList = this.featureCache.getFeatureChunksByRegion(args.region);
    if (itemList != null) {
        this.trigger('data:ready', {items: itemList, params: this.params, chunkSize:this.featureCache.chunkSize, cached: true, sender: this});
    }
};

FeatureDataAdapter.prototype._fetchData = function (region) {
    var _this = this;
    if (this.dataSource != null) {//could be null in expression genomic attributer widget 59
        if (this.async) {
            this.dataSource.on('success', function (data) {
                _this.parse(data, region);
//				_this.onLoad.notify();
                _this.trigger('file:load', {sender: _this});


                var itemList = _this.featureCache.getFeatureChunksByRegion(region);
                if (itemList != null) {
                    _this.trigger('data:ready', {items: itemList, params: _this.params, chunkSize:_this.featureCache.chunkSize, cached: true, sender: _this});
                }

            });
            this.dataSource.fetch(this.async);
        } else {
            var data = this.dataSource.fetch(this.async);
            this.parse(data, region);
        }
    }
}

FeatureDataAdapter.prototype.addFeatures = function (features) {
    this.featureCache.putFeatures(features, "data");
};

VCFDataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
VCFDataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function VCFDataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};
	
	this.header = "";
	this.samples = [];

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
}

VCFDataAdapter.prototype.parse = function(data, region){
//	console.log(data);
	var _this = this;
	var dataType = "value";
	var lines = data.split("\n");
//    debugger
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
//        debugger
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			if(fields[0]==region.chromosome){// load only one chromosome on the cache
			
				if(line.substr(0,1)==="#"){
					if(line.substr(1,1)==="#"){
						this.header+=line.replace(/</gi,"&#60;").replace(/>/gi,"&#62;")+"<br>";
					}else{
						this.samples = fields.slice(9);
					}
				}else{
	//				_this.addQualityControl(fields[5]);
					var feature = {
							"chromosome": 	fields[0],
							"position": 	parseInt(fields[1]), 
							"start": 		parseInt(fields[1]),//added
							"end": 			parseInt(fields[1]),//added
							"id":  			fields[2],
							"reference": 			fields[3],
							"alternate": 			fields[4],
							"quality": 		fields[5], 
							"filter": 		fields[6], 
							"info": 		fields[7].replace(/;/gi,"<br>"), 
							"format": 		fields[8],
							"sampleData":	line,
	//						"record":		fields,
	//						"label": 		fields[2] + " " +fields[3] + "/" + fields[4] + " Q:" + fields[5],
							"featureType":	"vcf"
					};
					
					this.featureCache.putFeatures(feature, dataType);
					
					if (this.featuresByChromosome[fields[0]] == null){
						this.featuresByChromosome[fields[0]] = 0;
					}
					this.featuresByChromosome[fields[0]]++;
					this.featuresCount++;
				}
			}
		}
	}
};

GFF2DataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
GFF2DataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function GFF2DataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

GFF2DataAdapter.prototype.parse = function(data, region){
	var _this = this;
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache

				//NAME  SOURCE  TYPE  START  END  SCORE  STRAND  FRAME  GROUP
				var feature = {
						"chromosome": chromosome, 
						"label": fields[2], 
						"start": parseInt(fields[3]), 
						"end": parseInt(fields[4]), 
						"score": fields[5],
						"strand": fields[6], 
						"frame": fields[7],
						"group": fields[8],
						"featureType":	"gff2"
				} ;

				this.featureCache.putFeatures(feature, dataType);
				
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;
			}
		}
	}
};

GFF3DataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
GFF3DataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function GFF3DataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;

	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

GFF3DataAdapter.prototype.parse = function(data, region){
	var _this = this;
	
	//parse attributes column
	var getAttr = function(column){
		var obj = {};
        if(typeof column !== 'undefined'){
            var arr = column.replace(/ /g,'').split(";");
            for (var i = 0, li = arr.length; i<li ; i++){
                var item = arr[i].split("=");
                obj[item[0]] = item[1];
            }
        }
		return obj;
	};
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache

				//NAME  SOURCE  TYPE  START  END  SCORE  STRAND  FRAME  GROUP
				var feature = {
						"chromosome": chromosome, 
						"label": fields[2], 
						"start": parseInt(fields[3]), 
						"end": parseInt(fields[4]), 
						"score": fields[5],
						"strand": fields[6], 
						"frame": fields[7],
						"attributes": getAttr(fields[8]),
						"featureType":	"gff3"
				} ;

				this.featureCache.putFeatures(feature, dataType);
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;

			}
		}
	}
};

BEDDataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
BEDDataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function BEDDataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

BEDDataAdapter.prototype.parse = function(data, region){
	var _this = this;
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache
			
				var feature = {
						"label":fields[3],
						"chromosome": chromosome, 
						"start": parseFloat(fields[1]), 
						"end": parseFloat(fields[2]), 
						"score":fields[4],
						"strand":fields[5],
						"thickStart":fields[6],
						"thickEnd":fields[7],
						"itemRgb":fields[8],
						"blockCount":fields[9],
						"blockSizes":fields[10],
						"blockStarts":fields[11],
						"featureType":	"bed"
				};

				this.featureCache.putFeatures(feature, dataType);
				
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;
			}
		}
	}
};

function DasAdapter(args){

    var _this=this;

    _.extend(this, Backbone.Events);

    this.id = Utils.genId('DasAdapter');

	this.gzip = true;
	this.proxy = CELLBASE_HOST+"/latest/utils/proxy?url=";
	this.url;
	this.species;
	this.featureCache;
	this.params = {};

    _.extend(this, args);
    
    this.on(this.handlers);

	this.featureCache =  new FeatureCache(this.featureCache);
};

DasAdapter.prototype.getData = function(args){
//	console.time("all");
	var _this = this;
	//region check
	
	this.params["histogram"] = args.histogram;
	this.params["interval"] = args.interval;
	this.params["transcript"] = args.transcript;
	
	if(args.start<1){
		args.start=1;
	}
	if(args.end>300000000){
		args.end=300000000;
	}
	
	var dataType = "data";
	if(args.histogram){
		dataType = "histogram"+args.interval;
	}

	this.params["dataType"] = dataType;
	
	var firstChunk = this.featureCache._getChunk(args.start);
	var lastChunk = this.featureCache._getChunk(args.end);

	var chunks = [];
	var itemList = [];
	for(var i=firstChunk; i<=lastChunk; i++){
		var key = args.chromosome+":"+i;
		if(this.featureCache.cache[key] == null || this.featureCache.cache[key][dataType] == null) {
			chunks.push(i);
		}else{
			var item = this.featureCache.getFeatureChunk(key);
//			console.time("concat");
			itemList.push(item);
//			console.timeEnd("concat");
		}
	}
//	//notify all chunks
	if(itemList.length>0){
		this.trigger('data:ready',{items:itemList, params:this.params, cached:true});
	}
	
	
	//data process
	var updateStart = true;
	var updateEnd = true;
	if(chunks.length > 0){
//		console.log(chunks);
		
		for ( var i = 0; i < chunks.length; i++) {
			var query = null;
			
			if(updateStart){
				var chunkStart = parseInt(chunks[i] * this.featureCache.chunkSize);
				updateStart = false;
			}
			if(updateEnd){
				var chunkEnd = parseInt((chunks[i] * this.featureCache.chunkSize) + this.featureCache.chunkSize-1);
				updateEnd = false;
			}
			
			if(chunks[i+1]!=null){
				if(chunks[i]+1==chunks[i+1]){
					updateEnd =true;
				}else{
					query = args.chromosome+":"+chunkStart+","+chunkEnd;
					updateStart = true;
					updateEnd = true;
				}
			}else{
				query = args.chromosome+":"+chunkStart+","+chunkEnd;
				updateStart = true;
				updateEnd = true;
			}

			if(query){
				var fullURL = this.proxy + this.url + "?segment=" + query;
				console.log("fullURL: "+fullURL);

				$.ajax({
					url: fullURL,
					type: 'GET',
					dataType:"xml",
					error: function(){
						alert("error");
						_this.trigger('error',"It is not allowed by Access-Control-Allow-Origin " );
					},

					success: function(data){
						_this.xml =   (new XMLSerializer()).serializeToString(data);
						var xmlStringified =  (new XMLSerializer()).serializeToString(data); //data.childNodes[2].nodeValue;
						var data = xml2json.parser(xmlStringified);

						if(data.dasgff != null){//Some times DAS server does not respond
							var result = new Array();
								
							if (typeof(data.dasgff.gff.segment)  != 'undefined'){
								if (typeof(data.dasgff.gff.segment.feature)  != 'undefined'){	  
									result = data.dasgff.gff.segment.feature;	
								}
								else if (typeof(data.dasgff.gff.segment[0])  != 'undefined'){
									if (data.dasgff.gff.segment[0].feature != null){
										for ( var i = 0; i < data.dasgff.gff.segment.length; i++) {
											for ( var j = 0; j < data.dasgff.gff.segment[i].feature.length; j++) {
												data.dasgff.gff.segment[i].feature[j]["chromosome"] = args.chromosome;
												result.push(data.dasgff.gff.segment[i].feature[j]);
											}
										}
									}
									else{
										result.push([]);
									}
								}
							}
							var region = {chromosome:args.chromosome, start:chunkStart, end:chunkEnd};
							var resource = "das";
							_this.featureCache.putFeaturesByRegion(result, region, resource, dataType);
							console.log(_this.featureCache.cache);
							var items = _this.featureCache.getFeatureChunksByRegion(region);
							if(items != null){
								_this.trigger('data:ready',{items:items, params:_this.params, cached:false});
							}
						}
					}
				});
			}
		}
	}
};

DasAdapter.prototype.checkUrl = function(){
	var _this = this;
	var fullURL = this.proxy + this.url + "?segment=1:1,1";
	console.log("Checking URL: "+fullURL);

	$.ajax({
		url: fullURL,
		type: 'GET',
		dataType:"xml",
		error: function(){
			alert("error");
			_this.trigger('error',"It is not allowed by Access-Control-Allow-Origin " );
		},
		success: function(data){
			_this.xml = (new XMLSerializer()).serializeToString(data);
			_this.trigger('url:check',{data:_this.xml});
		}
	});
};

function DataSource() {
	
};

DataSource.prototype.fetch = function(){

};

StringDataSource.prototype.fetch = DataSource.prototype.fetch;

function StringDataSource(str) {
	DataSource.prototype.constructor.call(this);

    _.extend(this, Backbone.Events);
	this.str = str;
};

StringDataSource.prototype.fetch = function(async){
	if(async){
		this.trigger('success',this.str);
	}else{
		return this.str;
	}
};

FileDataSource.prototype.fetch = DataSource.prototype.fetch;

function FileDataSource(args) {
    DataSource.prototype.constructor.call(this);

    _.extend(this, Backbone.Events);

    this.file;
    this.maxSize = 500 * 1024 * 1024;
    this.type = 'text';

    //set instantiation args, must be last
    _.extend(this, args);
};

FileDataSource.prototype.error = function () {
    alert("File is too big. Max file size is " + this.maxSize + " bytes");
};

FileDataSource.prototype.fetch = function (async) {
    var _this = this;
    if (this.file.size <= this.maxSize) {
        if (async) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                _this.trigger('success', evt.target.result);
            };
            return this.readAs(this.type, reader);
        } else {
            // FileReaderSync web workers only
            var reader = new FileReaderSync();
            return this.readAs(this.type, reader);
        }
    } else {
        _this.error();
        _this.trigger('error', {sender: this});
    }
};


FileDataSource.prototype.readAs = function (type, reader) {
    switch (type) {
        case 'binary':
            return reader.readAsBinaryString(this.file);
            break;
        case 'text':
        default:
            return reader.readAsText(this.file, "UTF-8");
    }
};
function Region(args) {

    this.chromosome = null;
    this.start = null;
    this.end = null;

    if (_.isObject(args)) {
        this.load(args);
    } else if (_.isString(args)) {
        this.parse(args);
    }
}

Region.prototype = {
    load: function (obj) {
        if (_.isString(obj)) {
            return this.parse(obj);
        }
        this.chromosome = obj.chromosome || this.chromosome;
        this.chromosome = this.chromosome;

        (_.isUndefined(obj.start)) ? this.start = parseInt(this.start) : this.start = parseInt(obj.start);
        (_.isUndefined(obj.end)) ? this.end = parseInt(this.end) : this.end = parseInt(obj.end);
    },

    parse: function (str) {
        if (_.isObject(str)) {
            this.load(obj);
        }
        var pattern = /^([a-zA-Z0-9_])+\:([0-9])+\-([0-9])+$/;
        var pattern2 = /^([a-zA-Z0-9_])+\:([0-9])+$/;
        if (pattern.test(str) || pattern2.test(str)) {
            var splitDots = str.split(":");
            if (splitDots.length == 2) {
                var splitDash = splitDots[1].split("-");
                this.chromosome = splitDots[0];
                this.start = parseInt(splitDash[0]);
                if (splitDash.length == 2) {
                    this.end = parseInt(splitDash[1]);
                } else {
                    this.end = this.start;
                }
            }
            return true
        } else {
            return false;
        }
    },

    center: function () {
        return this.start + Math.floor((this.length()) / 2);
    },

    length: function () {
        return this.end - this.start + 1;
    },

    toString: function (formated) {
        var str;
        if (formated == true) {
            str = this.chromosome + ":" + Utils.formatNumber(this.start) + "-" + Utils.formatNumber(this.end);
        } else {
            str = this.chromosome + ":" + this.start + "-" + this.end;
        }
        return str;
    }
};



/**
 * A binary search tree implementation in JavaScript. This implementation
 * does not allow duplicate values to be inserted into the tree, ensuring
 * that there is just one instance of each value.
 * @class BinarySearchTree
 * @constructor
 */
function FeatureBinarySearchTree() {
    
    /**
     * Pointer to root node in the tree.
     * @property _root
     * @type Object
     * @private
     */
    this._root = null;
}

FeatureBinarySearchTree.prototype = {

    //restore constructor
    constructor: FeatureBinarySearchTree,
    
    //-------------------------------------------------------------------------
    // Private members
    //-------------------------------------------------------------------------
    
    /**
     * Appends some data to the appropriate point in the tree. If there are no
     * nodes in the tree, the data becomes the root. If there are other nodes
     * in the tree, then the tree must be traversed to find the correct spot
     * for insertion. 
     * @param {variant} value The data to add to the list.
     * @return {Void}
     * @method add
     */
    add: function (v){
        //create a new item object, place data in
        var node = { 
                value: v, 
                left: null,
                right: null 
            },
            
            //used to traverse the structure
            current;
    
        //special case: no items in the tree yet
        if (this._root === null){
            this._root = node;
            return true;
        } 
        	//else
            current = this._root;
            
            while(true){
            
                //if the new value is less than this node's value, go left
                if (node.value.end < current.value.start){
                
                    //if there's no left, then the new node belongs there
                    if (current.left === null){
                        current.left = node;
                        return true;
//                        break;
                    } 
                    	//else                  
                        current = current.left;
                    
                //if the new value is greater than this node's value, go right
                } else if (node.value.start > current.value.end){
                
                    //if there's no right, then the new node belongs there
                    if (current.right === null){
                        current.right = node;
                        return true;
//                        break;
                    } 
                    	//else
                        current = current.right;
 
                //if the new value is equal to the current one, just ignore
                } else {
                	return false;
//                    break;
                }
            }        
        
    },
    
    contains: function (v){
        var node = { 
                value: v, 
                left: null,
                right: null 
            },
    	found = false,
    	current = this._root;
          
      //make sure there's a node to search
      while(!found && current){
      
          //if the value is less than the current node's, go left
          if (node.value.end < current.value.start){
              current = current.left;
              
          //if the value is greater than the current node's, go right
          } else if (node.value.start > current.value.end){
              current = current.right;
              
          //values are equal, found it!
          } else {
              found = true;
          }
      }
      
      //only proceed if the node was found
      return found;   
        
    }
};
function FeatureCache(args) {
	this.args = args;
	this.id = Math.round(Math.random() * 10000000); // internal id for this class

	this.chunkSize = 50000;
	this.gzip = true;
	this.maxSize = 10*1024*1024;
	this.size = 0;
	
	if (args != null){
		if(args.chunkSize != null){
			this.chunkSize = args.chunkSize;
		}
		if(args.gzip != null){
			this.gzip = args.gzip;
		}
	}
	
	this.cache = {};
	this.chunksDisplayed = {};
	
	this.maxFeaturesInterval = 0;
	
	//XXX
	this.gzip = false;

};

FeatureCache.prototype._getChunk = function(position){
	return Math.floor(position/this.chunkSize);
};

FeatureCache.prototype.getChunkRegion = function(region){
	start = this._getChunk(region.start) * this.chunkSize;
	end = (this._getChunk(region.end) * this.chunkSize) + this.chunkSize-1;
	return {start:start,end:end};
};

FeatureCache.prototype.getFirstFeature = function(){
	var feature;
	if(this.gzip) {
		feature = JSON.parse(RawDeflate.inflate(this.cache[Object.keys(this.cache)[0]].data[0]));
	}else{
		feature = this.cache[Object.keys(this.cache)[0]].data[0];
	}
	return feature;
};


//new 
FeatureCache.prototype.getFeatureChunk = function(key){
	if(this.cache[key] != null) {
		return this.cache[key];
	}
	return null;
};
FeatureCache.prototype.getFeatureChunkByDataType = function(key,dataType){
	if(this.cache[key] != null) {
        if(this.cache[key][dataType] != null){
		    return this.cache[key][dataType];
        }
	}
	return null;
};
//new
FeatureCache.prototype.getFeatureChunksByRegion = function(region){
	var firstRegionChunk, lastRegionChunk,  chunks = [], key;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		// check if this key exists in cache (features from files)
		if(this.cache[key] != null ){
			chunks.push(this.cache[key]);
		}
		
	}
	//if(chunks.length == 0){
		//return null;
	//}
	return chunks;
};


FeatureCache.prototype.putFeaturesByRegion = function(featureDataList, region, featureType, dataType){
	var key, firstRegionChunk, lastRegionChunk, firstChunk, lastChunk, feature, gzipFeature;


	//initialize region
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);

	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		if(this.cache[key]==null){
			this.cache[key] = {};
			this.cache[key].key = key;
		}
//        else{
//            // TODO
//            console.log(region.chromosome+region.start+region.end+'-'+featureType+'-'+dataType);
////            return;
//        }
		if(this.cache[key][dataType]==null){
			this.cache[key][dataType] = [];
		}
	}

    //Check if is a single object
    if(featureDataList.constructor != Array){
        featureDataList = [featureDataList];
    }

    //loop over features and set on corresponding chunks
	for(var index = 0, len = featureDataList.length; index<len; index++) {
		feature = featureDataList[index];
		feature.featureType = featureType;
		firstChunk = this._getChunk(feature.start);
		lastChunk = this._getChunk(feature.end);
		
		if(this.gzip) {
			gzipFeature = RawDeflate.deflate(JSON.stringify(feature));
		}else{
			gzipFeature = feature;
		}
		
		for(var i=firstChunk; i<=lastChunk; i++) {
			if(i >= firstRegionChunk && i<= lastRegionChunk){//only if is inside the called region
				key = region.chromosome+":"+i;
				this.cache[key][dataType].push(gzipFeature);
			}
		}
	}
//        console.log(this.cache[region.chromosome+":"+firstRegionChunk][dataType].length)
};


//used by BED, GFF, VCF
FeatureCache.prototype.putFeatures = function(featureDataList, dataType){
	var feature, key, firstChunk, lastChunk;

	//Check if is a single object
	if(featureDataList.constructor != Array){
		featureDataList = [featureDataList];
	}

	for(var index = 0, len = featureDataList.length; index<len; index++) {
		feature = featureDataList[index];
		firstChunk = this._getChunk(feature.start);
		lastChunk = this._getChunk(feature.end);
		for(var i=firstChunk; i<=lastChunk; i++) {
			key = feature.chromosome+":"+i;
			if(this.cache[key]==null){
				this.cache[key] = [];
				this.cache[key].key = key;
			}
			if(this.cache[key][dataType]==null){
				this.cache[key][dataType] = [];
			}
			if(this.gzip) {
				this.cache[key][dataType].push(RawDeflate.deflate(JSON.stringify(feature)));
			}else{
				this.cache[key][dataType].push(feature);
			}

		}
	}
};



FeatureCache.prototype.putChunk = function(key, item){
	this.cache[key] = item;
};

FeatureCache.prototype.getChunk = function(key){
	return this.cache[key];
};

FeatureCache.prototype.putCustom = function(f){
	f(this);
};

FeatureCache.prototype.getCustom = function(f){
	f(this);
};



FeatureCache.prototype.remove = function(region){
	var firstChunk = this._getChunk(region.start);
	var lastChunk = this._getChunk(region.end);
	for(var i=firstChunk; i<=lastChunk; i++){
		var key = region.chromosome+":"+i;
		this.cache[key] = null;
	}
};

FeatureCache.prototype.clear = function(){
		this.size = 0;		
		this.cache = {};
};


//END



//THOSE METHODS ARE NOT USED



/*
FeatureCache.prototype.getFeaturesByChunk = function(key, dataType){
	var features =  [];
	var feature, firstChunk, lastChunk;
	
	if(this.cache[key] != null && this.cache[key][dataType] != null) {
		for ( var i = 0, len = this.cache[key][dataType].length; i < len; i++) {
			if(this.gzip) {
				feature = JSON.parse(RawDeflate.inflate(this.cache[key][dataType][i]));
			}else{
				feature = this.cache[key][dataType][i];
			}
			
			//check if any feature chunk has been already displayed 
			var displayed = false;
			firstChunk = this._getChunk(feature.start);
			lastChunk = this._getChunk(feature.end);
			for(var f=firstChunk; f<=lastChunk; f++){
				var fkey = feature.chromosome+":"+f;
				if(this.chunksDisplayed[fkey+dataType]==true){
					displayed = true;
					break;
				}
			}
			
			if(!displayed){
				features.push(feature);
				returnNull = false;
			}
		}
		this.chunksDisplayed[key+dataType]=true;
		return features;
	}
	
	return null;
};


FeatureCache.prototype.getFeaturesByRegion = function(region, dataType){
	var firstRegionChunk, lastRegionChunk, firstChunk, lastChunk, features = [], feature, key, returnNull = true, displayed;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		 //check if this key exists in cache (features from files)
		if(this.cache[key] != null && this.cache[key][dataType] != null){
			for ( var j = 0, len = this.cache[key][dataType].length; j < len; j++) {
				if(this.gzip) {
					try {
						feature = JSON.parse(RawDeflate.inflate(this.cache[key][dataType][j]));
					} catch (e) {
						//feature es "" 
						console.log(e)
						debugger
						
					}
					
				}else{
					feature = this.cache[key][dataType][j];
				}
				// we only get those features in the region AND check if chunk has been already displayed
				if(feature.end > region.start && feature.start < region.end){

			//		 check displayCheck argument 
					if(region.displayedCheck != false){
				//		check if any feature chunk has been already displayed 
						displayed = false;
						firstChunk = this._getChunk(feature.start);
						lastChunk = this._getChunk(feature.end);
						for(var f=firstChunk; f<=lastChunk; f++){
							var fkey = region.chromosome+":"+f;
							if(this.chunksDisplayed[fkey+dataType]==true){
								displayed = true;
								break;
							}
						}
						
						if(!displayed){
							features.push(feature);
							returnNull = false;
						}
					}else{
						features.push(feature);
						returnNull = false;
					}

					
				}
			}
		}
		 //check displayCheck argument 
		if(region.displayedCheck != false){
			this.chunksDisplayed[key+dataType]=true;//mark chunk as displayed
		}
	}
	if(returnNull){
		return null;
	}else{
		return features;
	}
};
*/




/*

FeatureCache.prototype.putChunk = function(featureDataList, chunkRegion, dataType){
	var feature, key, chunk;
	chunk = this._getChunk(chunkRegion.start);
	key = chunkRegion.chromosome+":"+chunk;

	if(this.cache[key]==null){
		this.cache[key] = [];
	}
	if(this.cache[key][dataType]==null){
		this.cache[key][dataType] = [];
	}

	if(featureDataList.constructor == Object){
		if(this.gzip) {
			this.cache[key][dataType].push(RawDeflate.deflate(JSON.stringify(featureDataList)));
		}else{
			this.cache[key][dataType].push(featureDataList);
		}
	}else{
		for(var index = 0, len = featureDataList.length; index<len; index++) {
			feature = featureDataList[index];
			if(this.gzip) {
				this.cache[key][dataType].push(RawDeflate.deflate(JSON.stringify(feature)));
			}else{
				this.cache[key][dataType].push(feature);
			}
		}
	}
	
};

*/


//NOT USED dev not tested
//FeatureCache.prototype.histogram = function(region, interval){
//
	//var intervals = (region.end-region.start+1)/interval;
	//var intervalList = [];
	//
	//for ( var i = 0; i < intervals; i++) {
		//var featuresInterval = 0;
		//
		//var intervalStart = i*interval;//deberia empezar en 1...
		//var intervalEnd = ((i+1)*interval)-1;
		//
		//var firstChunk = this._getChunk(intervalStart+region.start);
		//var lastChunk = this._getChunk(intervalEnd+region.start);
		//
		//console.log(this.cache);
		//for(var j=firstChunk; j<=lastChunk; j++){
			//var key = region.chromosome+":"+j;
			//console.log(key);
			//console.log(this.cache[key]);
			//for ( var k = 0, len = this.cache[key].length; k < len; k++) {
				//if(this.gzip) {
					//feature = JSON.parse(RawDeflate.inflate(this.cache[key][k]));
				//}else{
					//feature = this.cache[key][k];
				//}
				//if(feature.start > intervalStart && feature.start < intervalEnd);
				//featuresInterval++;
			//}
			//
		//}
		//intervalList[i]=featuresInterval;
		//
		//if(this.maxFeaturesInterval<featuresInterval){
			//this.maxFeaturesInterval = featuresInterval;
		//}
	//}
	//
	//for ( var inter in  intervalList) {
		//intervalList[inter]=intervalList[inter]/this.maxFeaturesInterval;
	//}
//};

BamCache.prototype.putHistogramFeaturesByRegion = FeatureCache.prototype.putFeaturesByRegion;

function BamCache(args) {
	this.args = args;
	this.id = Math.round(Math.random() * 10000000); // internal id for this class

	this.chunkSize = 50000;
	this.gzip = true;
	this.maxSize = 10*1024*1024;
	this.size = 0;
	
	if (args != null){
		if(args.chunkSize != null){
			this.chunkSize = args.chunkSize;
		}
		if(args.gzip != null){
			this.gzip = args.gzip;
		}
	}
	
	this.cache = {};

	//deprecated trackSvg has this object now
	//this.chunksDisplayed = {};
	
	this.maxFeaturesInterval = 0;//for local histogram
	
	//XXX
	this.gzip = false;
};

BamCache.prototype._getChunk = function(position){
	return Math.floor(position/this.chunkSize);
};

//new 
BamCache.prototype.getFeatureChunk = function(key){
	if(this.cache[key] != null) {
		return this.cache[key];
	}
	return null;
};
//new
BamCache.prototype.getFeatureChunksByRegion = function(region){
	var firstRegionChunk, lastRegionChunk,  chunks = [], key;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		// check if this key exists in cache (features from files)
		if(this.cache[key] != null ){
			chunks.push(this.cache[key]);
		}
		
	}
	//if(chunks.length == 0){
		//return null;
	//}
	return chunks;
};



BamCache.prototype.putFeaturesByRegion = function(resultObj, region, featureType, dataType){
	var key, firstChunk, lastChunk, firstRegionChunk, lastRegionChunk, read, gzipRead;
	var reads = resultObj.reads;
	var coverage = resultObj.coverage;
	
	//initialize region
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	
	var chunkIndex = 0;
	console.time("BamCache.prototype.putFeaturesByRegion1")
	//TODO the region for now is a chunk region, so this for is always 1 loop
	for(var i=firstRegionChunk, c=0; i<=lastRegionChunk; i++, c++){
		key = region.chromosome+":"+i;
		if(this.cache[key]==null || this.cache[key][dataType] == null){
			this.cache[key] = {};
			this.cache[key][dataType] = [];
			this.cache[key].key = key;
			this.cache[key].start = parseInt(region.start)+(c*this.chunkSize);
			this.cache[key].end = parseInt(region.start)+((c+1)*this.chunkSize)-1;
		}
        if(dataType === 'data'){
            //divide the coverage array in multiple arrays of chunksize length
    //		var chunkCoverage = coverage.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageAll = coverage.all.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageA = coverage.a.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageC = coverage.c.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageG = coverage.g.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageT = coverage.t.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverage = {
                "all":chunkCoverageAll,
                "a":chunkCoverageA,
                "c":chunkCoverageC,
                "g":chunkCoverageG,
                "t":chunkCoverageT
            };
        }

		if(this.gzip) {
			this.cache[key]["coverage"]=RawDeflate.deflate(JSON.stringify(chunkCoverage));
		}else{
			this.cache[key]["coverage"]=chunkCoverage;
		}
		chunkIndex+=this.chunkSize;
	}
	console.timeEnd("BamCache.prototype.putFeaturesByRegion1")
	console.time("BamCache.prototype.putFeaturesByRegion")
	var ssss = 0;


    if(dataType === 'data'){
        for(var index = 0, len = reads.length; index<len; index++) {
            read = reads[index];
            read.featureType = 'bam';
            firstChunk = this._getChunk(read.start);
            lastChunk = this._getChunk(read.end == 0?read.end=-1:read.end);//0 is not a position, i set to -1 to avoid enter in for
    //		Some reads has end = 0. So will not be drawn IGV does not draw those reads

            if(this.gzip) {
                gzipRead = RawDeflate.deflate(JSON.stringify(read));
                //ssss+= gzipRead.length;
            }else{
                gzipRead = read;
                //ssss+= JSON.stringify(gzipRead).length;
            }

            for(var i=firstChunk, c=0; i<=lastChunk; i++, c++) {
                if(i >= firstRegionChunk && i<= lastRegionChunk){//only if is inside the called region
                    key = read.chromosome+":"+i;
//                    if(this.cache[key].start==null){
//                        this.cache[key].start = parseInt(region.start)+(c*this.chunkSize);
//                    }
//                    if(this.cache[key].end==null){
//                        this.cache[key].end = parseInt(region.start)+((c+1)*this.chunkSize)-1;
//                    }
//                    if(this.cache[key][dataType] != null){
//                        this.cache[key][dataType] = [];
                        this.cache[key][dataType].push(gzipRead);
//                    }

                }
            }
        }
    }


	console.timeEnd("BamCache.prototype.putFeaturesByRegion");
	console.log("BamCache.prototype.putFeaturesByRegion"+ssss)
};

BamCache.prototype.clear = function(){
	this.size = 0;		
	this.cache = {};
	console.log("bamCache cleared")
};

/*
BamCache.prototype.getFeaturesByChunk = function(key, dataType){
	var features =  [];
	var feature, firstChunk, lastChunk, chunk;
	var chr = key.split(":")[0], chunkId = key.split(":")[1];
	var region = {chromosome:chr,start:chunkId*this.chunkSize,end:chunkId*this.chunkSize+this.chunkSize-1};
	
	if(this.cache[key] != null && this.cache[key][dataType] != null) {
		if(this.gzip) {
			coverage = JSON.parse(RawDeflate.inflate(this.cache[key]["coverage"]));
		}else{
			coverage = this.cache[key]["coverage"];
		}
		
		for ( var i = 0, len = this.cache[key]["data"].length; i < len; i++) {
			if(this.gzip) {
				feature = JSON.parse(RawDeflate.inflate(this.cache[key]["data"][i]));
			}else{
				feature = this.cache[key]["data"][i];
			}
			
			//check if any feature chunk has been already displayed 
			var displayed = false;
			firstChunk = this._getChunk(feature.start);
			lastChunk = this._getChunk(feature.end);
			for(var f=firstChunk; f<=lastChunk; f++){
				var fkey = feature.chromosome+":"+f;
				if(this.chunksDisplayed[fkey+dataType]==true){
					displayed = true;
					break;
				}
			}
			
			if(!displayed){
				features.push(feature);
				returnNull = false;
			}
		}
		this.chunksDisplayed[key+dataType]=true;
		chunk = {reads:features,coverage:coverage,region:region};
		return chunk;
	}
	
};

BamCache.prototype.getFeaturesByRegion = function(region, dataType){
	var firstRegionChunk, lastRegionChunk, firstChunk, lastChunk, chunks = [], feature, key, coverage, features = [], displayed;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		if(this.cache[key] != null){
			if(this.gzip) {
				coverage = JSON.parse(RawDeflate.inflate(this.cache[key]["coverage"]));
			}else{
				coverage = this.cache[key]["coverage"];
			}

			for ( var j = 0, len = this.cache[key]["data"].length; j < len; j++) {
				if(this.gzip) {
					feature = JSON.parse(RawDeflate.inflate(this.cache[key]["data"][j]));
				}else{
					feature = this.cache[key]["data"][j];
				}
				
				
//				check if any feature chunk has been already displayed 
				displayed = false;
				firstChunk = this._getChunk(feature.start);
				lastChunk = this._getChunk(feature.end);
				for(var f=firstChunk; f<=lastChunk; f++){
					var fkey = region.chromosome+":"+f;
					if(this.chunksDisplayed[fkey+dataType]==true){
						displayed = true;
						break;
					}
				}
				
				if(!displayed){
					features.push(feature);
				}
				
			}
		}
		this.chunksDisplayed[key+dataType]=true;//mark chunk as displayed
		chunks.push({reads:features,coverage:coverage,region:region});
	}
	return chunks;
};
*/



//BamCache.prototype.remove = function(region){
//	var firstChunk = this._getChunk(region.start);
//	var lastChunk = this._getChunk(region.end);
//	for(var i=firstChunk; i<=lastChunk; i++){
//		var key = region.chromosome+":"+i;
//		this.cache[key] = null;
//	}
//};
//

//
//BamCache.prototype.clearType = function(dataType){
//	this.cache[dataType] = null;
//};

function MemoryStore(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    // configurable parameters
//    this.limit = 500;

    // Now we set the args parameters
    _.extend(this, args);

    // internal parameters
    this.size = 0;
    this.store = {};
};

MemoryStore.prototype = {
    add: function (key, value) {
        if (typeof this.store === 'undefined') {
            this.store = {};
        }
        var item = {key: key, value: value};

        // a item can be overwritten
        this.store[key] = item;

        if (this.tail) {
            this.tail.newer = item;
            item.older = this.tail;
        } else {
            // the item is the first one
            this.head = item;
        }

        // add new item to the end of the linked list, it's now the freshest item.
        this.tail = item;

//        if (this.size === this.limit) {
//            // we hit the limit, remove the head
//            this.shift();
//        } else {
//            // increase the size counter
//            this.size++;
//        }
        this.size++;

    },
    shift: function () {
        // todo: handle special case when limit == 1
        var item = this.head;
        if (item) {
            if (this.head.newer) {
                this.head = this.head.newer;
                this.head.older = undefined;
            } else {
                this.head = undefined;
            }
            // Remove last strong reference to <item> and remove links from the purged
            // item being returned:
            item.newer = item.older = undefined;
            // delete is slow, but we need to do this to avoid uncontrollable growth:
            delete this.store[item.key];
        }
    },
    get : function(key) {
        // First, find our cache item
        var item = this.store[key];
        if (item === undefined) return; // Not cached. Sorry.
        // As <key> was found in the cache, register it as being requested recently
        if (item === this.tail) {
            // Already the most recenlty used item, so no need to update the list
            return item.value;
        }
        // HEAD--------------TAIL
        //   <.older   .newer>
        //  <--- add direction --
        //   A  B  C  <D>  E
        if (item.newer) {
            if (item === this.head){
                this.head = item.newer;
            }
            item.newer.older = item.older; // C <-- E.
        }
        if (item.older){
            item.older.newer = item.newer; // C. --> E
        }
        item.newer = undefined; // D --x
        item.older = this.tail; // D. --> E
        if (this.tail)
            this.tail.newer = item; // E. <-- D
        this.tail = item;
        return item.value;
    },

    init: function () {
        this.size = 0;
        this.store = {};
        this.head = undefined;
        this.tail = undefined;
    },
    clear: function () {
        this.store = null;
        this.init();
    }


//    get: function (key) {
//        if (typeof this.dataStore === 'undefined') {
//            return undefined;
//        } else {
//            var ms = this.counter++;
//            this.dataStore[key].ms = ms;
//            return this.dataStore[key].data;
//        }
//    },

//    addCollection: function (key, featureArray) {
//        // If 'featureArray' is an Array then we add all elements,
//        // otherwise we call to add()
//        if ($.isArray(featureArray)) {
//            if (typeof this.dataStore === 'undefined') {
//                this.dataStore = {};
//            }
//            for (var feature in featureArray) {
//                this.dataStore[key] = feature;
//                this.lru.push({key: key, ms: this.counter});
//            }
//        } else {
//            this.add(key, featureArray);
//        }
//    },

//    delete: function (key) {
//        if (typeof this.dataStore !== 'undefined') {
//            var aux = this.dataStore[key];
//            delete this.dataStore[key];
//            return aux;
//        }
//    },

//    free: function () {
//        this.lru = [];
//        for (var i in this.dataStore) {
//            this.lru.push({key: i, ms: this.dataStore[i].ms});
//        }
//        this.lru.sort(function (a, b) {
//            return a.ms - b.ms;
//        });
//        this.delete(this.lru[0].key);
//        this.lru.splice(0, 1);
//    },
//
//    close: function () {
//        this.dataStore = null;
//    }
};
function FeatureChunkCache(args) {
    _.extend(this, Backbone.Events);

    // Default values
    this.id = Utils.genId("FeatureChunkCache");

    this.chunkSize = 50000;
    this.limit;

    _.extend(this, args);

    this.store = new MemoryStore({});

    this.verbose = false;
}


FeatureChunkCache.prototype = {

    getChunk: function (chunkId) {
        return this.store.get(chunkId);
    },

    getAdjustedRegion: function (region) {
        var start = this.getChunkId(region.start) * this.chunkSize;
        var end = (this.getChunkId(region.end) * this.chunkSize) + this.chunkSize - 1;

        return new Region({chromosome: region.chromosome, start: start, end: end});
    },


    getAdjustedRegions: function (region) {
        var firstChunkId = this.getChunkId(region.start);
        var lastChunkId = this.getChunkId(region.end);

        var regions = [], updateStart = true, updateEnd = true, chunkStart, chunkEnd;
        for (var chunkId = firstChunkId; chunkId <= lastChunkId; chunkId++) {
            var chunkKey = this.getChunkKey(region.chromosome, chunkId);
            var nextChunkKey = this.getChunkKey(region.chromosome, chunkId + 1);
            var chunk = this.getChunk(chunkKey);
            var nextChunk = this.getChunk(nextChunkKey);
            if (updateStart) {
                chunkStart = parseInt(chunkId * this.chunkSize);
                updateStart = false;
            }
            if (updateEnd) {
                chunkEnd = parseInt((chunkId * this.chunkSize) + this.chunkSize - 1);
                updateEnd = false;
            }

            if (!chunk) {
                updateEnd = true;
                if (nextChunk && chunkId < lastChunkId) {
                    var r = new Region({chromosome: region.chromosome, start: chunkStart, end: chunkEnd})
                    regions.push(r);
                    updateStart = true;
                }
                if (chunkId == lastChunkId) {
                    var r = new Region({chromosome: region.chromosome, start: chunkStart, end: chunkEnd})
                    regions.push(r);
                }
            } else {
                updateStart = true;
                updateEnd = true;
            }
        }
        return regions;
    },

    getByRegions: function (regions) {
        var chunks = [];
        for (var i in regions) {
            var chunkId = this.getChunkId(regions[i].start);
            var chunkKey = this.getChunkKey(regions[i].chromosome, chunkId);
            chunks.push(this.getChunk(chunkKey));
        }
        return chunks;
    },


    getCachedByRegion: function (region) {
        var chunkRegions = {cached: [], notCached: []};

        var firstChunkId = this.getChunkId(region.start);
        var lastChunkId = this.getChunkId(region.end);

        for (var chunkId = firstChunkId; chunkId <= lastChunkId; chunkId++) {
            var chunkKey = this.getChunkKey(region.chromosome, chunkId);
            var chunk = this.getChunk(chunkKey);

            var chunkRegionStart = parseInt(chunkId * this.chunkSize) || 1;
            var chunkRegionEnd = parseInt(chunkId * this.chunkSize + this.chunkSize - 1);
            var chunkRegion = new Region({chromosome: region.chromosome, start: chunkRegionStart, end: chunkRegionEnd});

            if (_.isUndefined(chunk)) {
                chunkRegions.notCached.push(chunkRegion);
            } else {
                chunkRegions.cached.push(chunkRegion);
            }

            if (this.verbose) {
                console.log(chunkRegions);
            }
        }
        return chunkRegions;
    },

    putChunk: function (chunkKey, value) {
        var value = {value: value, chunkKey: chunkKey};
        this.store.add(chunkKey, value);
        return value;
    },

    putByRegion: function (region, value) {
        var chunkId = this.getChunkId(region.start);
        var chunkKey = this.getChunkKey(region.chromosome, chunkId);
        return this.putChunk(chunkKey, value);
    },

    getChunkKey: function (chromosome, chunkId) {
        return chromosome + ":" + chunkId;
    },

    getChunkId: function (position) {
        return Math.floor(position / this.chunkSize);
    },


    getChunkSize: function () {
        return this.chunkSize;
    }


}
function NavigationBar(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    var _this = this;

    this.id = Utils.genId("NavigationBar");

    this.species = 'Homo sapiens';
    this.increment = 3;

    //set instantiation args, must be last
    _.extend(this, args);

    //set new region object
    this.region = new Region(this.region);

    this.currentChromosomeList = [];

    this.on(this.handlers);

    this.zoomChanging = false;

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

NavigationBar.prototype = {

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        var navgationHtml = '' +
            '<div class="btn-toolbar" role="toolbar">' +
            '   <div class="btn-group">' +
            '       <button id="restoreDefaultRegionButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-repeat"></span></button>' +
            '   </div>' +
            '   <div class="btn-group">' +
            '       <button id="regionHistoryButton" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="glyphicon glyphicon-time"></span> <span class="caret"></button>' +
            '       <ul id="regionHistoryMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group">' +
            '       <button id="speciesButton" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"  type="button" >' +
            '           <span id="speciesText"></span>&nbsp;<span class="caret"></span>' +
            '       </button>' +
            '       <ul id="speciesMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group">' +
//            '       <div class="pull-left" style="height:22px;line-height: 22px;color:#708090">Chr&nbsp;</div>' +
            '       <button id="chromosomesButton" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"  type="button" >' +
            '           <span id="chromosomesText"></span>&nbsp;<span class="caret"></span>' +
            '       </button>' +
            '       <ul id="chromosomesMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group" data-toggle="buttons">' +
            '       <label id="karyotypeButton" class="btn btn-default btn-xs"><input type="checkbox"><span class="ocb-icon ocb-icon-karyotype"></span></label>' +
            '       <label id="chromosomeButton" class="btn btn-default btn-xs"><input type="checkbox"><span class="ocb-icon ocb-icon-chromosome"></span></label>' +
            '       <label id="regionButton" class="btn btn-default btn-xs"><input type="checkbox"><span class="ocb-icon ocb-icon-region"></span></label>' +
            '   </div>' +
            '   <div class="btn-group" style="margin:0px 0px 0px 15px;">' +
            '       <button id="zoomOutButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-minus"></span></button>' +
            '       <div id="progressBarCont" class="progress pull-left" style="width:120px;height:10px;margin:5px 2px 0px 2px;background-color: #d5d5d5">' +
            '           <div id="progressBar" class="progress-bar" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 100%">' +
            '           </div>' +
            '       </div>' +
            '       <button id="zoomInButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-plus"></span></button>' +
            '   </div>' +
            '   <div class="btn-group" style="margin:0px 0px 0px 10px;">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Window size:&nbsp;</div>' +
            '       <input id="windowSizeField" type="text" class="form-control pull-left" placeholder="Window size" style="padding:0px 4px;height:22px;width:60px">' +
            '   </div>' +
            '   <div class="btn-group" style="margin:0px 0px 0px 10px;">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Position:&nbsp;</div>' +
            '       <div class="input-group pull-left">' +
            '           <input id="regionField" type="text" class="form-control" placeholder="region..." style="padding:0px 4px;width:160px;height:22px">' +
            '       </div>' +
            '       <button id="goButton" class="btn btn-default btn-xs" type="button">Go!</button>' +
            '   </div>' +
            '   <div class="btn-group">' +
            '       <button id="moveFurtherLeftButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-arrow-w-bold"></span></button>' +
            '       <button id="moveLeftButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-arrow-w"></span></button>' +
            '       <button id="moveRightButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-arrow-e"></span></button>' +
            '       <button id="moveFurtherRightButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-arrow-e-bold"></span></button>' +
            '   </div>' +
            '   <div class="btn-group">' +
            '       <button id="autoheightButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-track-autoheight"></span></button>' +
            '   </div>' +
            '    <div class="btn-group">' +
            '       <button id="compactButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon glyphicon glyphicon-compressed"></span></button>' +
            '   </div>' +
            '   <div class="btn-group pull-right">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Search:&nbsp;</div>' +
            '       <div class="input-group pull-left">' +
            '           <input id="searchField" list="searchDataList" type="text" class="form-control" placeholder="gene, snp..." style="padding:0px 4px;height:22px;width:100px">' +
            '           <datalist id="searchDataList">' +
            '           </datalist>' +
            '       </div>' +
//            '       <ul id="quickSearchMenu" class="dropdown-menu" role="menu">' +
//            '       </ul>' +
            '       <button id="quickSearchButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-search"></span></button>' +
            '   </div>' +
            '</div>' +
            '';


        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="navigation-bar" class="gv-navigation-bar unselectable">' + navgationHtml + '</div>')[0];
        $(this.div).css({
            height: '33px'
        });
        $(this.targetDiv).append(this.div);


        this.restoreDefaultRegionButton = $(this.div).find('#restoreDefaultRegionButton')[0];

        this.regionHistoryButton = $(this.div).find('#regionHistoryButton')[0];
        this.regionHistoryMenu = $(this.div).find('#regionHistoryMenu')[0];

        this.speciesButton = $(this.div).find('#speciesButton')[0];
        this.speciesText = $(this.div).find('#speciesText')[0];
        this.speciesMenu = $(this.div).find('#speciesMenu')[0];

        this.chromosomesButton = $(this.div).find('#chromosomesButton')[0];
        this.chromosomesText = $(this.div).find('#chromosomesText')[0];
        this.chromosomesMenu = $(this.div).find('#chromosomesMenu')[0];

        this.karyotypeButton = $(this.div).find('#karyotypeButton')[0];
        this.chromosomeButton = $(this.div).find('#chromosomeButton')[0];
        this.regionButton = $(this.div).find('#regionButton')[0];

        this.progressBar = $(this.div).find('#progressBar')[0];
        this.progressBarCont = $(this.div).find('#progressBarCont')[0];
        this.zoomOutButton = $(this.div).find('#zoomOutButton')[0];
        this.zoomInButton = $(this.div).find('#zoomInButton')[0];

        this.regionField = $(this.div).find('#regionField')[0];
        this.goButton = $(this.div).find('#goButton')[0];

        this.moveFurtherLeftButton = $(this.div).find('#moveFurtherLeftButton');
        this.moveFurtherRightButton = $(this.div).find('#moveFurtherRightButton');
        this.moveLeftButton = $(this.div).find('#moveLeftButton');
        this.moveRightButton = $(this.div).find('#moveRightButton');

        this.autoheightButton = $(this.div).find('#autoheightButton');
        this.compactButton = $(this.div).find('#compactButton');

        this.searchField = $(this.div).find('#searchField')[0];
//        this.quickSearchMenu = $(this.div).find('#quickSearchMenu')[0];
        this.searchDataList = $(this.div).find('#searchDataList')[0];
        this.quickSearchButton = $(this.div).find('#quickSearchButton')[0];
        this.windowSizeField = $(this.div).find('#windowSizeField')[0];

        /*** ***/
        $(this.restoreDefaultRegionButton).click(function (e) {
            _this.trigger('restoreDefaultRegion:click', {clickEvent: e, sender: {}})
        });

        this._addRegionHistoryMenuItem(this.region);
        this._setChromosomeMenu();
        this._setSpeciesMenu();
        $(this.chromosomesText).text(this.region.chromosome);
        $(this.speciesText).text(this.species.text);


        $(this.karyotypeButton).click(function () {
            _this.trigger('karyotype-button:change', {selected: $(this).hasClass('active'), sender: _this});
        });
        $(this.chromosomeButton).click(function () {
            _this.trigger('chromosome-button:change', {selected: $(this).hasClass('active'), sender: _this});
        });
        $(this.regionButton).click(function () {
            _this.trigger('region-button:change', {selected: $(this).hasClass('active'), sender: _this});
        });


        $(this.zoomOutButton).click(function () {
            _this._handleZoomOutButton();
        });
        $(this.zoomInButton).click(function () {
            _this._handleZoomInButton();
        });
        $(this.progressBarCont).click(function (e) {
            var offsetX = e.clientX - $(this).offset().left;
            console.log('offsetX '+offsetX);
            console.log('e.offsetX '+ e.offsetX);
            var zoom = 100 / $(this).width() * offsetX;
            if (!_this.zoomChanging) {
                $(_this.progressBar).width(offsetX);
                _this.zoomChanging = true;
                setTimeout(function () {
                    _this._handleZoomSlider(zoom);
                    _this.zoomChanging = false;
                }, 500);
            }
        });
        $(this.regionField).val(this.region.toString());

        $(this.goButton).click(function () {
            _this._goRegion($(_this.regionField).val());
        });

        $(this.moveFurtherLeftButton).click(function () {
            _this._handleMoveRegion(10);
        });

        $(this.moveFurtherRightButton).click(function () {
            _this._handleMoveRegion(-10);
        });

        $(this.moveLeftButton).click(function () {
            _this._handleMoveRegion(1);
        });

        $(this.moveRightButton).click(function () {
            _this._handleMoveRegion(-1);
        });

        $(this.autoheightButton).click(function (e) {
            _this.trigger('autoHeight-button:click', {clickEvent: e, sender: _this});
        });

        $(this.compactButton).click(function (e) {
            _this.trigger('autoHeight-button:click', {clickEvent: e, sender: _this});
            $(".ocb-compactable").toggle();
        });


//        var speciesCode = Utils.getSpeciesCode(this.species.text).substr(0, 3);
//        var url = CellBaseManager.url({
//            host: 'http://ws.bioinfo.cipf.es/cellbase/rest',
//            species: speciesCode,
//            version: 'latest',
//            category: 'feature',
//            subCategory: 'id',
//            query: '%QUERY',
//            resource: 'starts_with',
//            params: {
//                of: 'json'
//            }
//        });

//        $(this.div).find('#searchField').typeahead({
//            remote: {
//                url: url,
//                filter: function (parsedResponse) {
//                    return parsedResponse[0];
//                }
//            },
//            valueKey: 'displayId',
//            limit: 20
//        }).bind('typeahead:selected', function (obj, datum) {
//                _this._goFeature(datum.displayId);
//            });
//
//        $(this.div).find('#searchField').parent().find('.tt-hint').addClass('form-control tt-query').css({
//            height: '22px'
//        });
//        $(this.div).find('.tt-dropdown-menu').css({
//            'font-size': '14px'
//        });

        var lastQuery = '';
        $(this.searchField).bind("keyup", function (event) {
            var query = $(this).val();
            if (query.length > 2 && lastQuery !== query && event.which !== 13) {
                _this._setQuickSearchMenu(query);
                lastQuery = query;
            }
            if (event.which === 13) {
                debugger
                var item = _this.quickSearchDataset[query];
                _this.trigger('quickSearch:select', {item: item, sender: _this});
            }
        });

        $(this.quickSearchButton).click(function () {
            var query = $(_this.searchField).val();
            var item = _this.quickSearchDataset[query];
            _this.trigger('quickSearch:go', {item: item, sender: _this});
        });

        $(this.windowSizeField).val(this.region.length());
        $(this.windowSizeField).bind("keyup", function (event) {
            var value = $(this).val();
            var pattern = /^([0-9])+$/;
            if (event.which === 13 && pattern.test(value)) {
                var regionSize = parseInt(value);
                var haflRegionSize = Math.floor(regionSize / 2);
                var start = _this.region.center() - haflRegionSize;
                var end = _this.region.center() + haflRegionSize;
                _this.region.start = start;
                _this.region.end = end;
                _this.trigger('region:change', {region: _this.region});
            }
        });
        this.rendered = true;
    },

    _addRegionHistoryMenuItem: function (region) {
        var _this = this;
        var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + region.toString() + '</a></li>')[0];
        $(this.regionHistoryMenu).append(menuEntry);
        $(menuEntry).click(function () {
            _this.region.parse($(this).text());
            $(_this.chromosomesText).text(_this.region.chromosome);
            $(_this.regionField).val(_this.region.toString());
            _this.trigger('region:change', {region: _this.region, sender: _this});
            console.log($(this).text());
        });
    },

    _setQuickSearchMenu: function (query) {
        if (typeof this.quickSearchResultFn === 'function') {
            $(this.searchDataList).empty();
            this.quickSearchDataset = {};
            var items = this.quickSearchResultFn(query);
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var itemKey = item;
                if ($.type(this.quickSearchDisplayKey) === "string") {
                    itemKey = item[this.quickSearchDisplayKey];
                }
                this.quickSearchDataset[itemKey] = item;
                var menuEntry = $('<option value="' + itemKey + '">')[0];
                $(this.searchDataList).append(menuEntry);
            }
        } else {
            console.log('the quickSearchResultFn function is not valid');
        }
    },

    _setChromosomeMenu: function () {
        var _this = this;

        $(this.chromosomesMenu).empty();

        //find species object
        var list = [];
        for (var i in this.availableSpecies.items) {
            for (var j in this.availableSpecies.items[i].items) {
                var species = this.availableSpecies.items[i].items[j];
                if (species.text === this.species.text) {
                    list = species.chromosomes;
                    break;
                }
            }
        }

        this.currentChromosomeList = list;
        //add bootstrap elements to the menu
        for (var i in list) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + list[i] + '</a></li>')[0];
            $(this.chromosomesMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.region.chromosome = $(this).text();
                $(_this.chromosomesText).text($(this).text());
                $(_this.regionField).val(_this.region.toString());
                _this._addRegionHistoryMenuItem(_this.region);
                _this.trigger('region:change', {region: _this.region, sender: _this});
                console.log($(this).text());
            });
        }
    },

    _setSpeciesMenu: function () {
        var _this = this;

        var createEntry = function (species) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + species.text + '</a></li>')[0];
            $(_this.speciesMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.species = species;
                $(_this.speciesText).text($(this).text());
                _this._setChromosomeMenu();
                _this.trigger('species:change', {species: species, sender: _this});
            });
        };
        //find species object
        var list = [];
        for (var i in this.availableSpecies.items) {
            for (var j in this.availableSpecies.items[i].items) {
                var species = this.availableSpecies.items[i].items[j];
                createEntry(species);
            }
        }
    },
    _goRegion: function (value) {
        var reg = new Region();
        if (!reg.parse(value) || reg.start < 0 || reg.end < 0 || _.indexOf(this.currentChromosomeList, reg.chromosome) == -1) {
            $(this.regionField).css({opacity: 0.0});
            $(this.regionField).animate({opacity: 1}, 700);
        } else {
            this.region.load(reg);
            $(this.windowSizeField).val(this.region.length());
            $(this.chromosomesText).text(this.region.chromosome);
            this._addRegionHistoryMenuItem(this.region);
            this.trigger('region:change', {region: this.region, sender: this});
        }
    },

    _handleZoomOutButton: function () {
        this._handleZoomSlider(Math.max(0, this.zoom - 1));
    },
    _handleZoomSlider: function (value) {
        this.zoom = value;
        this.trigger('zoom:change', {zoom: this.zoom, sender: this});
    },
    _handleZoomInButton: function () {
        this._handleZoomSlider(Math.min(100, this.zoom + 1));
    },

    _handleMoveRegion: function (positions) {
        var pixelBase = (this.width - this.svgCanvasWidthOffset) / this.region.length();
        var disp = Math.round((positions * 10) / pixelBase);
        this.region.start -= disp;
        this.region.end -= disp;
        $(this.regionField).val(this.region.toString());
        this.trigger('region:move', {region: this.region, disp: disp, sender: this});
    },

    setVisible: function (obj) {
        for (key in obj) {
            var query = $(this.div).find('#' + key);
            if (obj[key]) {
                query.show();
            } else {
                query.hide();
            }
        }
    },

    setRegion: function (region) {
        this.region.load(region);
        $(this.chromosomesText).text(this.region.chromosome);
        $(this.regionField).val(this.region.toString());
        $(this.windowSizeField).val(this.region.length());
        this._addRegionHistoryMenuItem(region);
    },
    moveRegion: function (region) {
        this.region.load(region);
        $(this.chromosomesText).text(this.region.chromosome);
        $(this.regionField).val(this.region.toString());
    },

    setWidth: function (width) {
        this.width = width;
    },
    setZoom: function (zoom) {
        this.zoom = zoom;
        $(this.progressBar).css("width", this.zoom + '%');
    },
    draw: function () {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
    }
}
function ChromosomePanel(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.id = Utils.genId('ChromosomePanel');

    this.pixelBase;
    this.species = 'hsapiens';
    this.width = 600;
    this.height = 75;
    this.collapsed = false;
    this.collapsible = false;

    //set instantiation args, must be last
    _.extend(this, args);

    //set own region object
    this.region = new Region(this.region);


    this.lastChromosome = "";
    this.data;

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

ChromosomePanel.prototype = {
    show: function () {
        $(this.div).css({display: 'block'});
    },
    hide: function () {
        $(this.div).css({display: 'none'});
    },
    showContent: function () {
        $(this.svg).css({display: 'inline'});
        this.collapsed = false;
        $(this.collapseDiv).removeClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-plus');
        $(this.collapseDiv).children().first().addClass('glyphicon-minus');
    },
    hideContent: function () {
        $(this.svg).css({display: 'none'});
        this.collapsed = true;
        $(this.collapseDiv).addClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-minus');
        $(this.collapseDiv).children().first().addClass('glyphicon-plus');
    },
    setVisible: function (bool) {
        if (bool) {
            $(this.div).css({display: 'block'});
        } else {
            $(this.div).css({display: 'none'});
        }
    },
    setTitle: function (title) {
        if ('titleDiv' in this) {
            $(this.titleDiv).first().html(title);
        }
    },
    setWidth: function (width) {
        this.width = width;
        this.svg.setAttribute("width", width);
//        this.tracksViewedRegion = this.width / Utils.getPixelBaseByZoom(this.zoom);

        if(typeof this.data !== 'undefined'){
            this.clean();
            this._drawSvg(this.data);
        }
    },

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="chromosome-panel"></div>')[0];
        $(this.targetDiv).append(this.div);

        if ('title' in this && this.title !== '') {
            this.titleDiv = $('<div id="tl-title" class="gv-panel-title unselectable"><span style="line-height: 24px;margin-left: 5px;">' + this.title + '</span></div>')[0];
            $(this.div).append(this.titleDiv);

            if (this.collapsible == true) {
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs pull-right" style="display:inline;margin:2px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
                $(this.titleDiv).dblclick(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.collapseDiv).click(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.titleDiv).append(this.collapseDiv);
            }

        }

        this.svg = SVG.init(this.div, {
            "width": this.width,
            "height": this.height
        });
        $(this.div).addClass('unselectable');

        this.colors = {gneg: "#eeeeee", stalk: "#666666", gvar: "#CCCCCC", gpos25: "silver", gpos33: "lightgrey", gpos50: "gray", gpos66: "dimgray", gpos75: "darkgray", gpos100: "black", gpos: "gray", acen: "blue", clementina: '#ffc967'};
        this.rendered = true;
    },

    setSpecies: function (species) {
        this.species = species;
    },
    clean: function () {
        $(this.svg).empty();
    },
    draw: function () {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
        var _this = this;

        this.clean();

        CellBaseManager.get({
            species: this.species,
            category: 'genomic',
            subCategory: 'chromosome',
            query: this.region.chromosome,
            resource: 'info',
            async:false,
            success: function (data) {
                _this.data = data.response[0].result.chromosomes;
                _this.data.cytobands.sort(function (a, b) {
                    return (a.start - b.start);
                });
                _this._drawSvg(_this.data);
            }
        });

        this.lastChromosome = this.region.chromosome;


        if (this.collapsed) {
            _this.hideContent();
        }
    },
    _drawSvg: function (chromosome) {
        // This method uses less svg elements
        var _this = this;
        var offset = 20;
        var group = SVG.addChild(_this.svg, "g", {"cursor": "pointer"});
        this.chromosomeLength = chromosome.size;
        this.pixelBase = (this.width - 40) / this.chromosomeLength;

        /**/
        /*Draw Chromosome*/
        /**/
        var backrect = SVG.addChild(group, 'rect', {
            'x': offset,
            'y': 4,
            'width': this.width - 40 + 1,
            'height': 22,
            'fill': '#555555'
        });

        var cytobandsByStain = {};
        var textDrawingOffset = offset;
        for (var i = 0; i < chromosome.cytobands.length; i++) {
            var cytoband = chromosome.cytobands[i];
            cytoband.pixelStart = cytoband.start * this.pixelBase;
            cytoband.pixelEnd = cytoband.end * this.pixelBase;
            cytoband.pixelSize = cytoband.pixelEnd - cytoband.pixelStart;

            if (typeof cytobandsByStain[cytoband.stain] == 'undefined') {
                cytobandsByStain[cytoband.stain] = [];
            }
            cytobandsByStain[cytoband.stain].push(cytoband);

            var middleX = textDrawingOffset + (cytoband.pixelSize / 2);
            var textY = 28;
            var text = SVG.addChild(group, "text", {
                "x": middleX,
                "y": textY,
                "font-size": 10,
                "transform": "rotate(90, " + middleX + ", " + textY + ")",
                "fill": "black"
            });
            text.textContent = cytoband.name;
            textDrawingOffset += cytoband.pixelSize;
        }

        for (var cytobandStain in cytobandsByStain) {
            var cytobands_d = '';
            if (cytobandStain != 'acen') {
                for (var j = 0; j < cytobandsByStain[cytobandStain].length; j++) {
                    var cytoband = cytobandsByStain[cytobandStain][j];
                    cytobands_d += 'M' + (cytoband.pixelStart + offset + 1) + ',15' + ' L' + (cytoband.pixelEnd + offset) + ',15 ';
                }
                var path = SVG.addChild(group, 'path', {
                    "d": cytobands_d,
                    "stroke": this.colors[cytobandStain],
//                "stroke": 'red',
                    "stroke-width": 20,
                    "fill": 'none'
                });
            }
        }

        if(typeof cytobandsByStain['acen'] !== 'undefined'){
            var firstStain = cytobandsByStain['acen'][0];
            var lastStain = cytobandsByStain['acen'][1];
            var backrect = SVG.addChild(group, 'rect', {
                'x': (firstStain.pixelStart + offset + 1),
                'y': 4,
                'width': (lastStain.pixelEnd + offset) - (firstStain.pixelStart + offset + 1),
                'height': 22,
                'fill': 'white'
            });
            var firstStainXStart = (firstStain.pixelStart + offset + 1);
            var firstStainXEnd = (firstStain.pixelEnd + offset);
            var lastStainXStart = (lastStain.pixelStart + offset + 1);
            var lastStainXEnd = (lastStain.pixelEnd + offset);
            var path = SVG.addChild(group, 'path', {
                'd': 'M' + firstStainXStart + ',4' + ' L' + (firstStainXEnd - 5) + ',4 ' + ' L' + firstStainXEnd + ',15 ' + ' L ' + (firstStainXEnd - 5) + ',26 ' + ' L ' + firstStainXStart + ',26 z',
                'fill': this.colors['acen']
            });
            var path = SVG.addChild(group, 'path', {
                'd': 'M' + lastStainXStart + ',15' + ' L' + (lastStainXStart + 5) + ',4 ' + ' L' + lastStainXEnd + ',4 ' + ' L ' + lastStainXEnd + ',26 ' + ' L ' + (lastStainXStart + 5) + ',26 z',
                'fill': this.colors['acen']
            });
        }


        /**/
        /* Resize elements and events*/
        /**/
        var status = '';
        var centerPosition = _this.region.center();
        var pointerPosition = (centerPosition * _this.pixelBase) + offset;
        $(this.svg).on('mousedown', function (event) {
            status = 'setRegion';
        });

        // selection box, will appear when selection is detected
        var selBox = SVG.addChild(this.svg, "rect", {
            "x": 0,
            "y": 2,
            "stroke-width": "2",
            "stroke": "deepskyblue",
            "opacity": "0.5",
            "fill": "honeydew"
        });


        var positionBoxWidth = _this.region.length() * _this.pixelBase;
        var positionGroup = SVG.addChild(group, 'g');
        this.positionBox = SVG.addChild(positionGroup, 'rect', {
            'x': pointerPosition - (positionBoxWidth / 2),
            'y': 2,
            'width': positionBoxWidth,
            'height': _this.height - 3,
            'stroke': 'orangered',
            'stroke-width': 2,
            'opacity': 0.5,
            'fill': 'navajowhite',
            'cursor': 'move'
        });
        $(this.positionBox).on('mousedown', function (event) {
            status = 'movePositionBox';
        });


        var resizeLeft = SVG.addChild(positionGroup, 'rect', {
            'x': pointerPosition - (positionBoxWidth / 2),
            'y': 2,
            'width': 5,
            'height': _this.height - 3,
            'opacity': 0.5,
            'fill': 'orangered',
            'visibility': 'hidden'
        });
        $(resizeLeft).on('mousedown', function (event) {
            status = 'resizePositionBoxLeft';
        });

        var resizeRight = SVG.addChild(positionGroup, 'rect', {
            'x': positionBoxWidth - 5,
            'y': 2,
            'width': 5,
            'height': _this.height - 3,
            'opacity': 0.5,
            'fill': 'orangered',
            'visibility': 'hidden'
        });
        $(resizeRight).on('mousedown', function (event) {
            status = 'resizePositionBoxRight';
        });

        $(this.positionBox).off('mouseenter');
        $(this.positionBox).off('mouseleave');

        var recalculateResizeControls = function () {
            var postionBoxX = parseInt(_this.positionBox.getAttribute('x'));
            var postionBoxWidth = parseInt(_this.positionBox.getAttribute('width'));
            resizeLeft.setAttribute('x', postionBoxX - 5);
            resizeRight.setAttribute('x', (postionBoxX + postionBoxWidth));
            $(resizeLeft).css({"cursor": "ew-resize"});
            $(resizeRight).css({"cursor": "ew-resize"});
        };

        var hideResizeControls = function () {
            resizeLeft.setAttribute('visibility', 'hidden');
            resizeRight.setAttribute('visibility', 'hidden');
        };

        var showResizeControls = function () {
            resizeLeft.setAttribute('visibility', 'visible');
            resizeRight.setAttribute('visibility', 'visible');
        };

        var recalculatePositionBox = function () {
            var genomicLength = _this.region.length();
            var pixelWidth = genomicLength * _this.pixelBase;
            var x = (_this.region.start * _this.pixelBase) + 20;//20 is the margin
            _this.positionBox.setAttribute("x", x);
            _this.positionBox.setAttribute("width", pixelWidth);
        };
        var limitRegionToChromosome = function (args) {
            args.start = (args.start < 1) ? 1 : args.start;
            args.end = (args.end > _this.chromosomeLength) ? _this.chromosomeLength : args.end;
            return args;
        };

        $(positionGroup).mouseenter(function (event) {
            recalculateResizeControls();
            showResizeControls();
        });
        $(positionGroup).mouseleave(function (event) {
            hideResizeControls();
        });


        /*Remove event listeners*/
        $(this.svg).off('contextmenu');
        $(this.svg).off('mousedown');
        $(this.svg).off('mouseup');
        $(this.svg).off('mousemove');
        $(this.svg).off('mouseleave');

        //Prevent browser context menu
        $(this.svg).contextmenu(function (e) {
            e.preventDefault();
        });
        var downY, downX, moveX, moveY, lastX, increment;

        $(this.svg).mousedown(function (event) {
//            downX = (event.pageX - $(_this.svg).offset().left);
            downX = (event.clientX - $(this).parent().offset().left); //using parent offset works well on firefox and chrome. Could be because it is a div instead of svg
            selBox.setAttribute("x", downX);
            lastX = _this.positionBox.getAttribute("x");
            if (status == '') {
                status = 'setRegion'
            }
            hideResizeControls();
            $(this).mousemove(function (event) {
//                moveX = (event.pageX - $(_this.svg).offset().left);
                moveX = (event.clientX - $(this).parent().offset().left); //using parent offset works well on firefox and chrome. Could be because it is a div instead of svg
                hideResizeControls();
                switch (status) {
                    case 'resizePositionBoxLeft' :
                        var inc = moveX - downX;
                        var newWidth = parseInt(_this.positionBox.getAttribute("width")) - inc;
                        if (newWidth > 0) {
                            _this.positionBox.setAttribute("x", parseInt(_this.positionBox.getAttribute("x")) + inc);
                            _this.positionBox.setAttribute("width", newWidth);
                        }
                        downX = moveX;
                        break;
                    case 'resizePositionBoxRight' :
                        var inc = moveX - downX;SVG
                        var newWidth = parseInt(_this.positionBox.getAttribute("width")) + inc;
                        if (newWidth > 0) {
                            _this.positionBox.setAttribute("width", newWidth);
                        }
                        downX = moveX;
                        break;
                    case 'movePositionBox' :
                        var inc = moveX - downX;
                        _this.positionBox.setAttribute("x", parseInt(_this.positionBox.getAttribute("x")) + inc);
                        downX = moveX;
                        break;
                    case 'setRegion':
                    case 'selectingRegion' :
                        status = 'selectingRegion';
                        if (moveX < downX) {
                            selBox.setAttribute("x", moveX);
                        }
                        selBox.setAttribute("width", Math.abs(moveX - downX));
                        selBox.setAttribute("height", _this.height - 3);
                        break;
                }

            });
        });


        $(this.svg).mouseup(function (event) {
            $(this).off('mousemove');
            if (downX != null) {

                switch (status) {
                    case 'resizePositionBoxLeft' :
                    case 'resizePositionBoxRight' :
                    case 'movePositionBox' :
                        if (moveX != null) {
                            var w = parseInt(_this.positionBox.getAttribute("width"));
                            var x = parseInt(_this.positionBox.getAttribute("x"));

                            var pixS = x;
                            var pixE = x + w;
                            var bioS = (pixS - offset) / _this.pixelBase;
                            var bioE = (pixE - offset) / _this.pixelBase;
                            var se = limitRegionToChromosome({start:bioS,end:bioE});// returns object with start and end
                            _this.region.start = Math.round(se.start);
                            _this.region.end = Math.round(se.end);
                            recalculatePositionBox();
                            recalculateResizeControls();
                            showResizeControls();
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                            recalculateResizeControls();
                            showResizeControls();
                        }
                        break;
                    case 'setRegion' :
                        if(downX > offset && downX < (_this.width - offset)){
                            var w = _this.positionBox.getAttribute("width");

                            _this.positionBox.setAttribute("x", downX - (w / 2));

                            var pixS = downX - (w / 2);
                            var pixE = downX + (w / 2);
                            var bioS = (pixS - offset) / _this.pixelBase;
                            var bioE = (pixE - offset) / _this.pixelBase;
                            var se = limitRegionToChromosome({start: bioS, end: bioE});// returns object with start and end
                            _this.region.start = Math.round(se.start);
                            _this.region.end = Math.round(se.end);
                            recalculatePositionBox();
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                        }
                        break;
                    case 'selectingRegion' :
                        var bioS = (downX - offset) / _this.pixelBase;
                        var bioE = (moveX - offset) / _this.pixelBase;
                        var start = Math.min(bioS,bioE);
                        var end = Math.max(bioS,bioE);
                        var se = limitRegionToChromosome({start:start,end:end});// returns object with start and end
                        _this.region.start = parseInt(se.start);
                        _this.region.end = parseInt(se.end);
                        recalculatePositionBox();
//                        var w = Math.abs(downX - moveX);
//                        _this.positionBox.setAttribute("width", w);
//                        _this.positionBox.setAttribute("x", Math.abs((downX + moveX) / 2) - (w / 2));
                        _this.trigger('region:change', {region: _this.region, sender: _this});
                        break;
                }
                status = '';

            }
            selBox.setAttribute("width", 0);
            selBox.setAttribute("height", 0);
            downX = null;
            moveX = null;
            lastX = _this.positionBox.getAttribute("x");
        });
        $(this.svg).mouseleave(function (event) {
            $(this).off('mousemove')
            if (lastX != null) {
                _this.positionBox.setAttribute("x", lastX);
            }
            selBox.setAttribute("width", 0);
            selBox.setAttribute("height", 0);
            downX = null;
            moveX = null;
            lastX = null;
            overPositionBox = false;
            movingPositionBox = false;
            selectingRegion = false;
        });
    },
    setRegion: function (region) {//item.chromosome, item.region
        this.region.load(region);
        var needDraw = false;

        if (this.lastChromosome != this.region.chromosome) {
            needDraw = true;
        }
        if (needDraw) {
            this.draw();
        }

        //recalculate positionBox
        var genomicLength = this.region.length();
        var pixelWidth = genomicLength * this.pixelBase;
        var x = (this.region.start * this.pixelBase) + 20;//20 is the margin
        this.positionBox.setAttribute("x", x);
        this.positionBox.setAttribute("width", pixelWidth);

    }
}
function KaryotypePanel(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events

    _.extend(this, Backbone.Events);

    this.id = Utils.genId('KaryotypePanel');

    this.pixelBase;
    this.species;
    this.width = 600;
    this.height = 75;
    this.collapsed = false;
    this.collapsible = true;


//set instantiation args, must be last
        _.extend(this, args);

    //set own region object
    this.region = new Region(this.region);

    this.lastSpecies = this.species;

    this.chromosomeList;
    this.data2;

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

KaryotypePanel.prototype = {
    show: function () {
        $(this.div).css({display: 'block'});
    },
    hide: function () {
        $(this.div).css({display: 'none'});
    },
    showContent: function () {
        $(this.svg).css({display: 'inline'});
        this.collapsed = false;
        $(this.collapseDiv).removeClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-plus');
        $(this.collapseDiv).children().first().addClass('glyphicon-minus');
    },
    hideContent: function () {
        $(this.svg).css({display: 'none'});
        this.collapsed = true;
        $(this.collapseDiv).addClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-minus');
        $(this.collapseDiv).children().first().addClass('glyphicon-plus');
    },
    setVisible: function (bool) {
        if (bool) {
            $(this.div).css({display: 'block'});
        } else {
            $(this.div).css({display: 'none'});
        }
    },
    setTitle: function (title) {
        if ('titleDiv' in this) {
            $(this.titleDiv).children().first().html(title);
        }
    },
    setWidth: function (width) {
        this.width = width;
        this.svg.setAttribute("width", width);


        if(typeof this.chromosomeList !== 'undefined'){
            this.clean();
            this._drawSvg(this.chromosomeList, this.data2);
        }
    },

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="karyotype-panel"></div>')[0];
        $(this.targetDiv).append(this.div);

        if ('title' in this && this.title !== '') {
            this.titleDiv = $('<div id="tl-title" class="gv-panel-title unselectable"><span style="line-height: 24px;margin-left: 5px;">' + this.title + '</span></div>')[0];
            $(this.div).append(this.titleDiv);

            if(this.collapsible == true){
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs pull-right" style="display:inline;margin:2px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
                $(this.titleDiv).dblclick(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.collapseDiv).click(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.titleDiv).append(this.collapseDiv);
            }
        }

        this.svg = SVG.init(this.div, {
            "width": this.width,
            "height": this.height
        });
        this.markGroup = SVG.addChild(this.svg, "g", {"cursor": "pointer"});
        $(this.div).addClass('unselectable');

        this.colors = {gneg: "white", stalk: "#666666", gvar: "#CCCCCC", gpos25: "silver", gpos33: "lightgrey", gpos50: "gray", gpos66: "dimgray", gpos75: "darkgray", gpos100: "black", gpos: "gray", acen: "blue"};

        this.rendered = true;
    },

    setSpecies: function (species) {
        this.lastSpecies = this.species;
        this.species = species;
    },
    clean: function () {
        $(this.svg).empty();
    },
    draw: function () {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
        var _this = this;

        this.clean();

        var sortfunction = function (a, b) {
            var IsNumber = true;
            for (var i = 0; i < a.name.length && IsNumber == true; i++) {
                if (isNaN(a.name[i])) {
                    IsNumber = false;
                }
            }
            if (!IsNumber) return 1;
            return (a.name - b.name);
        };

        CellBaseManager.get({
            species: this.species,
            category: 'genomic',
            subCategory: 'chromosome',
            resource: 'all',
            async:false,
            success: function (data) {
                _this.chromosomeList = data.response.result.chromosomes;
                _this.chromosomeList.sort(sortfunction);
                _this._drawSvg(_this.chromosomeList);
            }
        });

        if (this.collapsed) {
            _this.hideContent();
        }
    },

    _drawSvg: function (chromosomeList) {
        var _this = this;

        var x = 20;
        var xOffset = _this.width / chromosomeList.length;
        var yMargin = 2;

        ///////////
        var biggerChr = 0;
        for (var i = 0, len = chromosomeList.length; i < len; i++) {
            var size = chromosomeList[i].size;
            if (size > biggerChr) {
                biggerChr = size;
            }
        }
        _this.pixelBase = (_this.height - 10) / biggerChr;
        _this.chrOffsetY = {};
        _this.chrOffsetX = {};

        for (var i = 0, len = chromosomeList.length; i < len; i++) { //loop over chromosomes
            var chromosome = chromosomeList[i];
//		var chr = chromosome.name;
            var chrSize = chromosome.size * _this.pixelBase;
            var y = yMargin + (biggerChr * _this.pixelBase) - chrSize;
            _this.chrOffsetY[chromosome.name] = y;
            var firstCentromere = true;

            var centerPosition = _this.region.center();
            var pointerPosition = (centerPosition * _this.pixelBase);

            var group = SVG.addChild(_this.svg, "g", {"cursor": "pointer", "chr": chromosome.name});
            $(group).click(function (event) {
                var chrClicked = this.getAttribute("chr");
//			for ( var k=0, len=chromosomeList.length; k<len; k++) {
//			var offsetX = (event.pageX - $(_this.svg).offset().left);
//			if(offsetX > _this.chrOffsetX[chromosomeList[k]]) chrClicked = chromosomeList[k];
//			}

                var offsetY = (event.pageY - $(_this.svg).offset().top);
//			var offsetY = event.originalEvent.layerY - 3;

                _this.positionBox.setAttribute("x1", _this.chrOffsetX[chrClicked] - 10);
                _this.positionBox.setAttribute("x2", _this.chrOffsetX[chrClicked] + 23);
                _this.positionBox.setAttribute("y1", offsetY);
                _this.positionBox.setAttribute("y2", offsetY);

                var clickPosition = parseInt((offsetY - _this.chrOffsetY[chrClicked]) / _this.pixelBase);
                _this.region.chromosome = chrClicked;
                _this.region.start = clickPosition;
                _this.region.end = clickPosition;

                _this.trigger('region:change', {region: _this.region, sender: _this});
            });

            for (var j = 0, lenJ = chromosome.cytobands.length; j < lenJ; j++) { //loop over chromosome objects
                var cytoband = chromosome.cytobands[j];
                var height = _this.pixelBase * (cytoband.end - cytoband.start);
                var width = 13;

                var color = _this.colors[cytoband.stain];
                if (color == null) color = "purple";

                if (cytoband.stain == "acen") {
                    var points = "";
                    var middleX = x + width / 2;
                    var middleY = y + height / 2;
                    var endX = x + width;
                    var endY = y + height;
                    if (firstCentromere) {
                        points = x + "," + y + " " + endX + "," + y + " " + endX + "," + middleY + " " + middleX + "," + endY + " " + x + "," + middleY;
                        firstCentromere = false;
                    } else {
                        points = x + "," + endY + " " + x + "," + middleY + " " + middleX + "," + y + " " + endX + "," + middleY + " " + endX + "," + endY;
                    }
                    SVG.addChild(group, "polyline", {
                        "points": points,
                        "stroke": "black",
                        "opacity": 0.8,
                        "fill": color
                    });
                } else {
                    SVG.addChild(group, "rect", {
                        "x": x,
                        "y": y,
                        "width": width,
                        "height": height,
                        "stroke": "grey",
                        "opacity": 0.8,
                        "fill": color
                    });
                }

                y += height;
            }
            var text = SVG.addChild(_this.svg, "text", {
                "x": x + 1,
                "y": _this.height,
                "font-size": 9,
                "fill": "black"
            });
            text.textContent = chromosome.name;

            _this.chrOffsetX[chromosome.name] = x;
            x += xOffset;
        }
        _this.positionBox = SVG.addChild(_this.svg, "line", {
            "x1": _this.chrOffsetX[_this.region.chromosome] - 10,
            "y1": pointerPosition + _this.chrOffsetY[_this.region.chromosome],
            "x2": _this.chrOffsetX[_this.region.chromosome] + 23,
            "y2": pointerPosition + _this.chrOffsetY[_this.region.chromosome],
            "stroke": "orangered",
            "stroke-width": 2,
            "opacity": 0.5
        });

        _this.rendered = true;
        _this.trigger('after:render',{sender:_this});
    },


    setRegion: function (region) {//item.chromosome, item.position, item.species
        this.region.load(region);
        var needDraw = false;

        if (this.lastSpecies != this.species) {
            needDraw = true;
            this.lastSpecies = this.species;
        }

        //recalculate positionBox
        var centerPosition = this.region.center();
        var pointerPosition = centerPosition * this.pixelBase + this.chrOffsetY[this.region.chromosome];
        this.positionBox.setAttribute("x1", this.chrOffsetX[this.region.chromosome] - 10);
        this.positionBox.setAttribute("x2", this.chrOffsetX[this.region.chromosome] + 23);
        this.positionBox.setAttribute("y1", pointerPosition);
        this.positionBox.setAttribute("y2", pointerPosition);

        if (needDraw) {
            this.draw();
        }
    },


    updatePositionBox: function () {
        this.positionBox.setAttribute("x1", this.chrOffsetX[this.region.chromosome] - 10);
        this.positionBox.setAttribute("x2", this.chrOffsetX[this.region.chromosome] + 23);

        var centerPosition = Utils.centerPosition(this.region);
        var pointerPosition = centerPosition * this.pixelBase + this.chrOffsetY[this.region.chromosome];
        this.positionBox.setAttribute("y1", pointerPosition);
        this.positionBox.setAttribute("y2", pointerPosition);
    },

    addMark: function (item) {//item.chromosome, item.position
        var _this = this;

        var mark = function () {
            if (_this.region.chromosome != null && _this.region.start != null) {
                if (_this.chrOffsetX[_this.region.chromosome] != null) {
                    var x1 = _this.chrOffsetX[_this.region.chromosome] - 10;
                    var x2 = _this.chrOffsetX[_this.region.chromosome];
                    var y1 = (_this.region.start * _this.pixelBase + _this.chrOffsetY[_this.region.chromosome]) - 4;
                    var y2 = _this.region.start * _this.pixelBase + _this.chrOffsetY[_this.region.chromosome];
                    var y3 = (_this.region.start * _this.pixelBase + _this.chrOffsetY[_this.region.chromosome]) + 4;
                    var points = x1 + "," + y1 + " " + x2 + "," + y2 + " " + x1 + "," + y3 + " " + x1 + "," + y1;
                    SVG.addChild(_this.markGroup, "polyline", {
                        "points": points,
                        "stroke": "black",
                        "opacity": 0.8,
                        "fill": "#33FF33"
                    });
                }
            }
        };

        if (this.rendered) {
            mark();
        } else {
            _this.on('after:render',function (e) {
                mark();
            });
        }
    },

    unmark: function () {
        $(this.markGroup).empty();
    }
}

function TrackListPanel(args) {//parent is a DOM div element
    var _this = this;

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("TrackListPanel");
    this.collapsed = false;
    this.collapsible = false;

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-14';

    this.trackSvgList = [];
    this.swapHash = {};

    this.parentLayout;
    this.mousePosition;
    this.windowSize;

    this.zoomMultiplier = 1;
    this.showRegionOverviewBox = false;


    this.height = 0;

    //set instantiation args, must be last
    _.extend(this, args);

    //set new region object
    this.region = new Region(this.region);
    this.width -= 18;


    this.status;

    //this region is used to do not modify original region, and will be used by trackSvg
    this.visualRegion = new Region(this.region);

    /********/
    this._setPixelBase();
    /********/

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }

};

TrackListPanel.prototype = {
    show: function () {
        $(this.div).css({display: 'block'});
    },

    hide: function () {
        $(this.div).css({display: 'none'});
    },
    setVisible: function (bool) {
        if (bool) {
            $(this.div).css({display: 'block'});
        } else {
            $(this.div).css({display: 'none'});
        }
    },
    setTitle: function (title) {
        if ('titleDiv' in this) {
            $(this.titleDiv).html(title);
        }
    },
    showContent: function () {
        $(this.tlHeaderDiv).css({display: 'block'});
        $(this.panelDiv).css({display: 'block'});
        this.collapsed = false;
        $(this.collapseDiv).removeClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-plus');
        $(this.collapseDiv).children().first().addClass('glyphicon-minus');
    },
    hideContent: function () {
        $(this.tlHeaderDiv).css({display: 'none'});
        $(this.panelDiv).css({display: 'none'});
        this.collapsed = true;
        $(this.collapseDiv).addClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-minus');
        $(this.collapseDiv).children().first().addClass('glyphicon-plus');
    },
    render: function (targetId) {
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        var _this = this;

        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="tracklist-panel" style="height:100%;position: relative;"></div>')[0];
        $(this.targetDiv).append(this.div);

        if ('title' in this && this.title !== '') {
            var titleDiv = $('<div id="tl-title" class="gv-panel-title unselectable"><div style="display:inline-block;line-height: 24px;margin-left: 5px;width:120px">' + this.title + '</div></div>')[0];
            $(this.div).append(titleDiv);
            var windowSizeDiv = $('<div style="display:inline;margin-left:35%" id="windowSizeSpan"></div>');
            $(titleDiv).append(windowSizeDiv);

            if (this.collapsible == true) {
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs pull-right" style="display:inline;margin:2px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
                $(titleDiv).dblclick(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.collapseDiv).click(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(titleDiv).append(this.collapseDiv);
            }

        }

        var tlHeaderDiv = $('<div id="tl-header" class="unselectable"></div>')[0];

        var panelDiv = $('<div id="tl-panel"></div>')[0];
        $(panelDiv).css({position: 'relative', width: '100%'});


        this.tlTracksDiv = $('<div id="tl-tracks"></div>')[0];
        $(this.tlTracksDiv).css({ position: 'relative', 'z-index': 3});


        $(this.div).append(tlHeaderDiv);
        $(this.div).append(panelDiv);

        $(panelDiv).append(this.tlTracksDiv);


        //Main SVG and his events
        this.svgTop = SVG.init(tlHeaderDiv, {
            "width": this.width,
            "height": 12
        });

        var mid = this.width / 2;
        var yOffset = 11;
        this.positionText = SVG.addChild(this.svgTop, 'text', {
            'x': mid - 30,
            'y': yOffset,
            'fill': 'steelblue',
            'class': this.fontClass
        });
        this.nucleotidText = SVG.addChild(this.svgTop, 'text', {
            'x': mid + 35,
            'y': yOffset,
            'class': this.fontClass
        });
        this.firstPositionText = SVG.addChild(this.svgTop, 'text', {
            'x': 0,
            'y': yOffset,
            'fill': 'steelblue',
            'class': this.fontClass
        });
        this.lastPositionText = SVG.addChild(this.svgTop, 'text', {
            'x': this.width - 70,
            'y': yOffset,
            'fill': 'steelblue',
            'class': this.fontClass
        });
//        this.viewNtsArrow = SVG.addChild(this.svgTop, 'rect', {
//            'x': 2,
//            'y': 6,
//            'width': this.width - 4,
//            'height': 2,
//            'opacity': '0.5',
//            'fill': 'black'
//        });
//        this.viewNtsArrowLeft = SVG.addChild(this.svgTop, 'polyline', {
//            'points': '0,1 2,1 2,13 0,13',
//            'opacity': '0.5',
//            'fill': 'black'
//        });
//        this.viewNtsArrowRight = SVG.addChild(this.svgTop, 'polyline', {
//            'points': this.width + ',1 ' + (this.width - 2) + ',1 ' + (this.width - 2) + ',13 ' + this.width + ',13',
//            'opacity': '0.5',
//            'fill': 'black'
//        });
        this.windowSize = 'Window size: ' + Utils.formatNumber(this.region.length()) + ' nts';
//        this.viewNtsTextBack = SVG.addChild(this.svgTop, 'rect', {
//            'x': mid - 40,
//            'y': 0,
//            'width': 0,
//            'height': 13,
//            'fill': 'white'
//        });
        this.viewNtsText = SVG.addChild(this.svgTop, 'text', {
            'x': mid - (this.windowSize.length * 7 / 2),
            'y': 11,
            'fill': 'black',
            'class': this.fontClass
        });
        this.viewNtsText.setAttribute('visibility', 'hidden');
//        this.viewNtsTextBack.setAttribute('width', $(this.viewNtsText).width() + 15);
        this.viewNtsText.textContent = this.windowSize;
        $(this.div).find('#windowSizeSpan').html(this.windowSize);
        this._setTextPosition();


        this.centerLine = $('<div id="' + this.id + 'centerLine"></div>')[0];
        $(panelDiv).append(this.centerLine);
        $(this.centerLine).css({
            'z-index': 2,
            'position': 'absolute',
            'left': mid - 1,
            'top': 0,
            'width': this.pixelBase,
//            'height': '100%',
            'height': 'calc(100% - 8px)',
            'opacity': 0.5,
            'border': '1px solid orangered',
            'background-color': 'orange'
        });


        this.mouseLine = $('<div id="' + this.id + 'mouseLine"></div>')[0];
        $(panelDiv).append(this.mouseLine);
        $(this.mouseLine).css({
            'z-index': 1,
            'position': 'absolute',
            'left': -20,
            'top': 0,
            'width': this.pixelBase,
            'height': 'calc(100% - 8px)',
            'border': '1px solid lightgray',
            'opacity': 0.7,
            'visibility': 'hidden',
            'background-color': 'gainsboro'
        });

        //allow selection in trackSvgLayoutOverview


        var selBox = $('<div id="' + this.id + 'selBox"></div>')[0];
        $(panelDiv).append(selBox);
        $(selBox).css({
            'z-index': 0,
            'position': 'absolute',
            'left': 0,
            'top': 0,
            'height': '100%',
            'border': '2px solid deepskyblue',
            'opacity': 0.5,
            'visibility': 'hidden',
            'background-color': 'honeydew'
        });

        if (this.showRegionOverviewBox) {
            var regionOverviewBoxLeft = $('<div id="' + this.id + 'regionOverviewBoxLeft"></div>')[0];
            var regionOverviewBoxRight = $('<div id="' + this.id + 'regionOverviewBoxRight"></div>')[0];
            $(panelDiv).append(regionOverviewBoxLeft);
            $(panelDiv).append(regionOverviewBoxRight);
            var regionOverviewBoxWidth = this.region.length() * this.pixelBase;
            var regionOverviewDarkBoxWidth = (this.width - regionOverviewBoxWidth) / 2
            $(regionOverviewBoxLeft).css({
                'z-index': 0,
                'position': 'absolute',
                'left': 1,
                'top': 0,
                'width': regionOverviewDarkBoxWidth,
                'height': 'calc(100% - 8px)',
                'border': '1px solid gray',
                'opacity': 0.5,
                //            'visibility': 'hidden',
                'background-color': 'lightgray'
            });
            $(regionOverviewBoxRight).css({
                'z-index': 0,
                'position': 'absolute',
                'left': (regionOverviewDarkBoxWidth + regionOverviewBoxWidth),
                'top': 0,
                'width': regionOverviewDarkBoxWidth,
                'height': 'calc(100% - 8px)',
                'border': '1px solid gray',
                'opacity': 0.5,
                //            'visibility': 'hidden',
                'background-color': 'lightgray'
            });
        }


        $(this.div).mousemove(function (event) {
            var centerPosition = _this.region.center();
            var mid = _this.width / 2;
            var mouseLineOffset = _this.pixelBase / 2;
            var offsetX = (event.clientX - $(_this.tlTracksDiv).offset().left);
            var cX = offsetX - mouseLineOffset;
            var rcX = (cX / _this.pixelBase) | 0;
            var pos = (rcX * _this.pixelBase) + (mid % _this.pixelBase) - 1;
            $(_this.mouseLine).css({'left': pos});
//
            var posOffset = (mid / _this.pixelBase) | 0;
            _this.mousePosition = centerPosition + rcX - posOffset;
            _this.trigger('mousePosition:change', {mousePos: _this.mousePosition, baseHtml: _this.getMousePosition(_this.mousePosition)});
        });

        $(this.tlTracksDiv).dblclick(function (event) {
            var halfLength = _this.region.length() / 2;
            var mouseRegion = new Region({chromosome: _this.region.chromosome, start: _this.mousePosition - halfLength, end: _this.mousePosition + halfLength})
            _this.trigger('region:change', {region: mouseRegion, sender: _this});
        });

        var downX, moveX;
        $(this.tlTracksDiv).mousedown(function (event) {
            $('html').addClass('unselectable');
//                            $('.qtip').qtip('hide').qtip('disable'); // Hide AND disable all tooltips
            $(_this.mouseLine).css({'visibility': 'hidden'});

            var mouseState = event.which;
            if (event.ctrlKey) {
                mouseState = 'ctrlKey' + event.which;
            }
            switch (mouseState) {
                case 1: //Left mouse button pressed
                    $(this).css({"cursor": "move"});
                    downX = event.clientX;
                    var lastX = 0;
                    $(this).mousemove(function (event) {
                        var newX = (downX - event.clientX) / _this.pixelBase | 0;//truncate always towards zero
                        if (newX != lastX) {
                            var disp = lastX - newX;
                            var centerPosition = _this.region.center();
                            var p = centerPosition - disp;
                            if (p > 0) {//avoid 0 and negative positions
                                _this.region.start -= disp;
                                _this.region.end -= disp;
                                _this._setTextPosition();
                                //						_this.onMove.notify(disp);
                                _this.trigger('region:move', {region: _this.region, disp: disp, sender: _this});
                                _this.trigger('trackRegion:move', {region: _this.region, disp: disp, sender: _this});
                                lastX = newX;
                                _this.setNucleotidPosition(p);
                            }
                        }
                    });

                    break;
                case 2: //Middle mouse button pressed
                case 'ctrlKey1': //ctrlKey and left mouse button
                    $(selBox).css({'visibility': 'visible'});
                    $(selBox).css({'width': 0});
                    downX = (event.pageX - $(_this.tlTracksDiv).offset().left);
                    $(selBox).css({"left": downX});
                    $(this).mousemove(function (event) {
                        moveX = (event.pageX - $(_this.tlTracksDiv).offset().left);
                        if (moveX < downX) {
                            $(selBox).css({"left": moveX});
                        }
                        $(selBox).css({"width": Math.abs(moveX - downX)});
                    });


                    break;
                case 3: //Right mouse button pressed
                    break;
                default: // other button?
            }


        });

        $(this.tlTracksDiv).mouseup(function (event) {
            $('html').removeClass("unselectable");
            $(this).css({"cursor": "default"});
            $(_this.mouseLine).css({'visibility': 'visible'});
            $(this).off('mousemove');

            var mouseState = event.which;
            if (event.ctrlKey) {
                mouseState = 'ctrlKey' + event.which;
            }
            switch (mouseState) {
                case 1: //Left mouse button pressed

                    break;
                case 2: //Middle mouse button pressed
                case 'ctrlKey1': //ctrlKey and left mouse button
                    $(selBox).css({'visibility': 'hidden'});
                    $(this).off('mousemove');
                    if (downX != null && moveX != null) {
                        var ss = downX / _this.pixelBase;
                        var ee = moveX / _this.pixelBase;
                        ss += _this.visualRegion.start;
                        ee += _this.visualRegion.start;
                        _this.region.start = parseInt(Math.min(ss, ee));
                        _this.region.end = parseInt(Math.max(ss, ee));
                        _this.trigger('region:change', {region: _this.region, sender: _this});
                        moveX = null;
                    } else if (downX != null && moveX == null) {
                        var mouseRegion = new Region({chromosome: _this.region.chromosome, start: _this.mousePosition, end: _this.mousePosition})
                        _this.trigger('region:change', {region: mouseRegion, sender: _this});
                    }
                    break;
                case 3: //Right mouse button pressed
                    break;
                default: // other button?
            }

        });

        $(this.tlTracksDiv).mouseleave(function (event) {
            $(this).css({"cursor": "default"});
            $(_this.mouseLine).css({'visibility': 'hidden'});
            $(this).off('mousemove');
            $("body").off('keydown.genomeViewer');

            $(selBox).css({'visibility': 'hidden'});
            downX = null;
            moveX = null;
        });

        $(this.tlTracksDiv).mouseenter(function (e) {
//            $('.qtip').qtip('enable'); // To enable them again ;)
            $(_this.mouseLine).css({'visibility': 'visible'});
            $("body").off('keydown.genomeViewer');
            enableKeys();
        });

        var enableKeys = function () {
            //keys
            $("body").bind('keydown.genomeViewer', function (e) {
                var disp = 0;
                switch (e.keyCode) {
                    case 37://left arrow
                        if (e.ctrlKey) {
                            disp = Math.round(100 / _this.pixelBase);
                        } else {
                            disp = Math.round(10 / _this.pixelBase);
                        }
                        break;
                    case 39://right arrow
                        if (e.ctrlKey) {
                            disp = Math.round(-100 / _this.pixelBase)
                        } else {
                            disp = Math.round(-10 / _this.pixelBase);
                        }
                        break;
                }
                if (disp != 0) {
                    _this.region.start -= disp;
                    _this.region.end -= disp;
                    _this._setTextPosition();
//					_this.onMove.notify(disp);
                    _this.trigger('region:move', {region: _this.region, disp: disp, sender: _this});
                    _this.trigger('trackRegion:move', {region: _this.region, disp: disp, sender: _this});
                }
            });
        };

        this.tlHeaderDiv = tlHeaderDiv;
        this.panelDiv = panelDiv;

        this.rendered = true;
    },

    setHeight: function (height) {
//        this.height=Math.max(height,60);
//        $(this.tlTracksDiv).css('height',height);
//        //this.grid.setAttribute("height",height);
//        //this.grid2.setAttribute("height",height);
//        $(this.centerLine).css("height",parseInt(height));//25 es el margen donde esta el texto de la posicion
//        $(this.mouseLine).css("height",parseInt(height));//25 es el margen donde esta el texto de la posicion
    },

    setWidth: function (width) {
        console.log(width);
        this.width = width - 18;
        var mid = this.width / 2;
        this._setPixelBase();

        $(this.centerLine).css({'left': mid - 1, 'width': this.pixelBase});
        $(this.mouseLine).css({'width': this.pixelBase});

        this.svgTop.setAttribute('width', this.width);
        this.positionText.setAttribute("x", mid - 30);
        this.nucleotidText.setAttribute("x", mid + 35);
        this.lastPositionText.setAttribute("x", this.width - 70);
//        this.viewNtsArrow.setAttribute("width", this.width - 4);
//        this.viewNtsArrowRight.setAttribute("points", this.width + ",1 " + (this.width - 2) + ",1 " + (this.width - 2) + ",13 " + this.width + ",13");
        this.viewNtsText.setAttribute("x", mid - (this.windowSize.length * 7 / 2));
//        this.viewNtsTextBack.setAttribute("x", mid - 40);
        this.trigger('trackWidth:change', {width: this.width, sender: this})

        this._setTextPosition();
    },

    highlight: function (event) {
        this.trigger('trackFeature:highlight', event)
    },


    moveRegion: function (event) {
        this.region.load(event.region);
        this.visualRegion.load(event.region);
        this._setTextPosition();
        this.trigger('trackRegion:move', event);
    },

    setSpecies: function (species) {
        this.species = species;
        this.trigger('trackSpecies:change', {species: species, sender: this})
    },

    setRegion: function (region) {//item.chromosome, item.position, item.species
        var _this = this;
        this.region.load(region);
        this.visualRegion.load(region);
        this._setPixelBase();
        //get pixelbase by Region


        $(this.centerLine).css({'width': this.pixelBase});
        $(this.mouseLine).css({'width': this.pixelBase});

        this.windowSize = "Window size: " + Utils.formatNumber(this.region.length()) + " nts";
        this.viewNtsText.textContent = this.viewNtsText.textContent;
        $(this.div).find('#windowSizeSpan').html(this.windowSize);
        this._setTextPosition();
        this.trigger('window:size', {windowSize: this.windowSize});

//        if (region.species != null) {
//            //check species and modify CellBaseAdapter, clean cache
//            for (i in this.trackSvgList) {
//                if (this.trackSvgList[i].trackData.adapter instanceof CellBaseAdapter ||
//                    this.trackSvgList[i].trackData.adapter instanceof SequenceAdapter
//                    ) {
//                    this.trackSvgList[i].trackData.adapter.species = region.species;
//                    //this.trackSvgList[i].trackData.adapter.featureCache.clear();
//
//                    this.trackSvgList[i].trackData.adapter.clearData();
//                }
//            }
//        }
        this.trigger('trackRegion:change', {region: this.visualRegion, sender: this})

        this.nucleotidText.textContent = "";//remove base char, will be drawn later if needed

        this.status = 'rendering';

//        this.onRegionChange.notify();

        //this.minRegionRect.setAttribute("width",this.minRectWidth);
        //this.minRegionRect.setAttribute("x",(this.width/2)-(this.minRectWidth/2)+6);
    },

    draw: function () {
        this.trigger('track:draw', {sender: this});
    },
    checkTracksReady: function () {
        var _this = this;
        /************ Loading ************/
        var checkAllTrackStatus = function (status) {
            for (i in _this.trackSvgList) {
                if (_this.trackSvgList[i].status != status) return false;
            }
            return true;
        };
        if (checkAllTrackStatus('ready')) {
//            console.log('all ready')
            this.status = 'ready';
            _this.trigger('tracks:ready', {sender: _this});
        }
//        var checkStatus = function () {
//            if (checkAllTrackStatus('ready')) {
//                _this.trigger('tracks:ready', {sender: _this});
//            } else {
//                setTimeout(checkStatus, 100);
//            }
//        };
//        setTimeout(checkStatus, 10);
        /***************************/
    },
    addTrack: function (track) {
        if (_.isArray(track)) {
            for (var i in track) {
                this._addTrack(track[i]);
            }
        } else {
            this._addTrack(track);
        }
    },
    _addTrack: function (track) {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
        var _this = this;

        var i = this.trackSvgList.push(track);
        this.swapHash[track.id] = {index: i - 1, visible: true};

        track.set('pixelBase', this.pixelBase);
        track.set('region', this.visualRegion);
        track.set('width', this.width);

        // Track must be initialized after we have created
        // de DIV element in order to create the elements in the DOM
        if (!track.rendered) {
            track.render(this.tlTracksDiv);
        }

        // Once tack has been initialize we can call draw() function
        track.draw();


        //trackEvents
        track.set('track:draw', function (event) {
            track.draw();
        });


        track.set('trackSpecies:change', function (event) {
            track.setSpecies(event.species);
        });


        track.set('trackRegion:change', function (event) {
            track.set('pixelBase', _this.pixelBase);
            track.set('region', event.region);
            track.draw();
        });


        track.set('trackRegion:move', function (event) {
            track.set('region', event.region);
            track.set('pixelBase', _this.pixelBase);
            track.move(event.disp);
        });


        track.set('trackWidth:change', function (event) {
            track.setWidth(event.width);
            track.set('pixelBase', _this.pixelBase);
            track.draw();
        });


        track.set('trackFeature:highlight', function (event) {


            var attrName = event.attrName || 'feature_id';
            if ('attrValue' in event) {
                event.attrValue = ($.isArray(event.attrValue)) ? event.attrValue : [event.attrValue];
                for (var key in event.attrValue) {
                    var queryStr = attrName + '~=' + event.attrValue[key];
                    var group = $(track.svgdiv).find('g[' + queryStr + ']')
                    $(group).each(function () {
                        var animation = $(this).find('animate');
                        if (animation.length == 0) {
                            animation = SVG.addChild(this, 'animate', {
                                'attributeName': 'opacity',
                                'attributeType': 'XML',
                                'begin': 'indefinite',
                                'from': '0.0',
                                'to': '1',
                                'begin': '0s',
                                'dur': '0.5s',
                                'repeatCount': '5'
                            });
                        } else {
                            animation = animation[0];
                        }
                        var y = $(group).find('rect').attr("y");
                        $(track.svgdiv).scrollTop(y);
                        animation.beginElement();
                    });
                }
            }
        });

        this.on('track:draw', track.get('track:draw'));
        this.on('trackSpecies:change', track.get('trackSpecies:change'));
        this.on('trackRegion:change', track.get('trackRegion:change'));
        this.on('trackRegion:move', track.get('trackRegion:move'));
        this.on('trackWidth:change', track.get('trackWidth:change'));
        this.on('trackFeature:highlight', track.get('trackFeature:highlight'));

        track.on('track:ready', function () {
            _this.checkTracksReady();
        });
    },

    removeTrack: function (trackId) {
        // first hide the track
        this._hideTrack(trackId);

        var i = this.swapHash[trackId].index;

        // remove track from list and hash data
        var track = this.trackSvgList.splice(i, 1)[0];
        delete this.swapHash[trackId];

        // delete listeners
        this.off('track:draw', track.get('track:draw'));
        this.off('trackSpecies:change', track.get('trackSpecies:change'));
        this.off('trackRegion:change', track.get('trackRegion:change'));
        this.off('trackRegion:move', track.get('trackRegion:move'));
        this.off('trackWidth:change', track.set('trackWidth:change'));
        this.off('trackFeature:highlight', track.get('trackFeature:highlight'));


        //uddate swapHash with correct index after splice
        for (var i = 0; i < this.trackSvgList.length; i++) {
            this.swapHash[this.trackSvgList[i].id].index = i;
        }
        return track;
    },

    restoreTrack: function (track, index) {
        var _this = this;

        this.addTrack(track);

        if (index != null) {
            this.setTrackIndex(track.id, index);
        }
//        this._showTrack(track.id);
    },

    enableAutoHeight: function () {
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            track.enableAutoHeight();
        }
    },
    updateHeight: function () {
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            track.updateHeight(true);
        }
    },

    _redraw: function () {
        $(this.tlTracksDiv)
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            $(track.div).detach();
            if (this.swapHash[track.id].visible) {
                $(this.tlTracksDiv).append(track.div);
            }
        }
    },

    //This routine is called when track order is modified
    _reallocateAbove: function (trackId) {
        var i = this.swapHash[trackId].index;
        console.log(i + " wants to move up");
        if (i > 0) {
            var aboveTrack = this.trackSvgList[i - 1];
            var underTrack = this.trackSvgList[i];

            var y = parseInt(aboveTrack.main.getAttribute("y"));
            var h = parseInt(underTrack.main.getAttribute("height"));
            aboveTrack.main.setAttribute("y", y + h);
            underTrack.main.setAttribute("y", y);

            this.trackSvgList[i] = aboveTrack;
            this.trackSvgList[i - 1] = underTrack;
            this.swapHash[aboveTrack.id].index = i;
            this.swapHash[underTrack.id].index = i - 1;
        } else {
            console.log("is at top");
        }
    },

    //This routine is called when track order is modified
    _reallocateUnder: function (trackId) {
        var i = this.swapHash[trackId].index;
        console.log(i + " wants to move down");
        if (i + 1 < this.trackSvgList.length) {
            var aboveTrack = this.trackSvgList[i];
            var underTrack = this.trackSvgList[i + 1];

            var y = parseInt(aboveTrack.main.getAttribute("y"));
            var h = parseInt(underTrack.main.getAttribute("height"));
            aboveTrack.main.setAttribute("y", y + h);
            underTrack.main.setAttribute("y", y);

            this.trackSvgList[i] = underTrack;
            this.trackSvgList[i + 1] = aboveTrack;
            this.swapHash[underTrack.id].index = i;
            this.swapHash[aboveTrack.id].index = i + 1;

        } else {
            console.log("is at bottom");
        }
    },

    setTrackIndex: function (trackId, newIndex) {
        var oldIndex = this.swapHash[trackId].index;

        //remove track from old index
        var track = this.trackSvgList.splice(oldIndex, 1)[0]

        //add track at new Index
        this.trackSvgList.splice(newIndex, 0, track);

        //uddate swapHash with correct index after slice
        for (var i = 0; i < this.trackSvgList.length; i++) {
            this.swapHash[this.trackSvgList[i].id].index = i;
        }

        //update track div positions
        this._redraw();
    },

    scrollToTrack: function (trackId) {
        var swapTrack = this.swapHash[trackId];
        if (swapTrack != null) {
            var i = swapTrack.index;
            var track = this.trackSvgList[i];
            var y = $(track.div).position().top;
            $(this.tlTracksDiv).scrollTop(y);

//            $(this.svg).parent().parent().scrollTop(track.main.getAttribute("y"));
        }
    },


    _hideTrack: function (trackId) {
        this.swapHash[trackId].visible = false;
        var i = this.swapHash[trackId].index;
        var track = this.trackSvgList[i];

        track.hide();

//        this.setHeight(this.height - track.getHeight());

        this._redraw();
    },

    _showTrack: function (trackId) {
        this.swapHash[trackId].visible = true;
        var i = this.swapHash[trackId].index;
        var track = this.trackSvgList[i];

        track.show();

//        this.svg.appendChild(track.main);

//        this.setHeight(this.height + track.getHeight());

        this._redraw();
    },
    _setPixelBase: function () {
        this.pixelBase = this.width / this.region.length();
        this.pixelBase = this.pixelBase / this.zoomMultiplier;
        this.halfVirtualBase = (this.width * 3 / 2) / this.pixelBase;
    },

    _setTextPosition: function () {
        var centerPosition = this.region.center();
        var baseLength = parseInt(this.width / this.pixelBase);//for zoom 100
        var aux = Math.ceil((baseLength / 2) - 1);
        this.visualRegion.start = Math.floor(centerPosition - aux);
        this.visualRegion.end = Math.floor(centerPosition + aux);

        this.positionText.textContent = Utils.formatNumber(centerPosition);
        this.firstPositionText.textContent = Utils.formatNumber(this.visualRegion.start);
        this.lastPositionText.textContent = Utils.formatNumber(this.visualRegion.end);


        this.windowSize = "Window size: " + Utils.formatNumber(this.visualRegion.length()) + " nts";
        this.viewNtsText.textContent = this.windowSize;
        $(this.div).find('#windowSizeSpan').html(this.windowSize);

//        this.viewNtsTextBack.setAttribute("width", this.viewNtsText.textContent.length * 7);
//        this.viewNtsTextBack.setAttribute('width', $(this.viewNtsText).width() + 15);
    },

    getTrackSvgById: function (trackId) {
        if (this.swapHash[trackId] != null) {
            var position = this.swapHash[trackId].index;
            return this.trackSvgList[position];
        }
        return null;
    },
    getSequenceTrack: function () {
        //if multiple, returns the first found
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            if (track instanceof SequenceTrack) {
                return track;
            }
        }
        return;
    },

    getMousePosition: function (position) {
        var base = '';
        var colorStyle = '';
        if (position > 0) {
            base = this.getSequenceNucleotid(position);
            colorStyle = 'color:' + SEQUENCE_COLORS[base];
        }
//        this.mouseLine.setAttribute('stroke',SEQUENCE_COLORS[base]);
//        this.mouseLine.setAttribute('fill',SEQUENCE_COLORS[base]);
        return '<span style="' + colorStyle + '">' + base + '</span>';
    },

    getSequenceNucleotid: function (position) {
        var seqTrack = this.getSequenceTrack();
        if (seqTrack != null && this.visualRegion.length() <= seqTrack.visibleRegionSize) {
            var nt = seqTrack.dataAdapter.getNucleotidByPosition({start: position, end: position, chromosome: this.region.chromosome})
            return nt;
        }
        return '';
    },

    setNucleotidPosition: function (position) {
        var base = this.getSequenceNucleotid(position);
        this.nucleotidText.setAttribute("fill", SEQUENCE_COLORS[base]);
        this.nucleotidText.textContent = base;
    }
};
function StatusBar(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    var _this = this;

    this.id = Utils.genId("StatusBar");

    //set instantiation args, must be last
    _.extend(this, args);

    //set new region object
    this.region = new Region(this.region);

    this.rendered=false;
    if(this.autoRender){
        this.render();
    }
};

StatusBar.prototype = {
    render: function (targetId) {
        this.targetId = (targetId) ? targetId : this.targetId;
        if($('#' + this.targetId).length < 1){
            console.log('targetId not found in DOM');
            return;
        }
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="' + this.id + '" class="gv-status-bar" align="right"></div>')[0];
        $(this.targetDiv).append(this.div);

        this.mousePositionDiv = $('<div id="' + this.id + 'position" style="display: inline">&nbsp;</div>')[0];
        $(this.mousePositionDiv).css({
            'margin-left': '5px',
            'margin-right': '5px',
            'font-size':'12px'
        });

        this.versionDiv = $('<div id="' + this.id + 'version" style="display: inline">' + this.version + '</div>')[0];
        $(this.versionDiv).css({
            'margin-left': '5px',
            'margin-right': '5px'
        });


        $(this.div).append(this.mousePositionDiv);
        $(this.div).append(this.versionDiv);

        this.rendered = true;
    },
    setRegion: function (event) {
        this.region.load(event.region);
        $(this.mousePositionDiv).html(Utils.formatNumber(event.region.center()));
    },
    setMousePosition: function (event) {
        $(this.mousePositionDiv).html(event.baseHtml+' '+this.region.chromosome+':'+Utils.formatNumber(event.mousePos));
    }

}
function LegendPanel(args){
	this.width = 200;
	this.height = 250;
	
	if (args != null){
        if (args.title!= null){
        	this.title = args.title;       
        }
        if (args.targetId!= null){
        	this.targetId = args.targetId;       
        }
        if (args.width!= null){
        	this.width = args.width;       
        }
        if (args.height!= null){
        	this.height = args.height;       
        }
    }
	
	
};

LegendPanel.prototype.getColorItems = function(legend){
	panelsArray = new Array();
	
	for ( var item in legend) {
//		var color = legend[item].toString().replace("#", "");
//		var cp = new Ext.picker.Color();
//		cp.width = 20;
//		cp.colors = [color];
		var size=15;
		var color = Ext.create('Ext.draw.Component', {
        width: size,
        height: size,
        items:[{
				type: 'rect',
				fill: legend[item],
				x:0,y:0,
				width: size,
				height : size
				}]
		});
		
		var name = Utils.formatText(item, "_");
		
		var panel = Ext.create('Ext.panel.Panel', {
			height:size,
			border:false,
			flex:1,
			margin:"1 0 0 1",
		    layout: {type: 'hbox',align:'stretch' },
		    items: [color, {xtype: 'tbtext',text:name, margin:"1 0 0 3"} ]
		});
		
		panelsArray.push(panel);
	}
	
	return panelsArray;
};




LegendPanel.prototype.getPanel = function(legend){
	var _this=this;
	
	if (this.panel == null){
		
		var items = this.getColorItems(legend);
		
		this.panel  = Ext.create('Ext.panel.Panel', {
			bodyPadding:'0 0 0 2',
			border:false,
			layout: {
		        type: 'vbox',
		        align:'stretch' 
		    },
			items:items,
			width:this.width,
			height:items.length*20
		});		
	}	
	
	return this.panel;
};

LegendPanel.prototype.getButton = function(legend){
	var _this=this;
	
	if (this.button == null){
		
		this.button = Ext.create('Ext.button.Button', {
			text : this.title,
			menu : {
                plain:true,
                items: [this.getPanel(legend)]
            }
		});
	}	
	return this.button;
	
};

function LegendWidget(args){
	
	this.width = 300;
	this.height = 300;
	this.title = "Legend";
	
	if (args != null){
        if (args.title!= null){
        	this.title = args.title;       
        }
        if (args.targetId!= null){
        	this.targetId = args.targetId;       
        }
        if (args.width!= null){
        	this.width = args.width;       
        }
        if (args.height!= null){
        	this.height = args.height;       
        }
    }
	
	this.legendPanel = new LegendPanel();
	
};

LegendWidget.prototype.draw = function(legend){
	var _this = this;
	if(this.panel==null){
		
		var item = this.legendPanel.getPanel(legend);
	
		this.panel = Ext.create('Ext.ux.Window', {
			title : this.title,
			resizable: false,
			constrain:true,
			closable:true,
			width: item.width+10,
			height: item.height+70,
			items : [item],
			buttonAlign:'right',
			 layout: {
		        type: 'hbox',
		        align:'stretch' 
		    },
			buttons:[
					{text:'Close', handler: function(){_this.panel.close();}}
			]
		});
	}
	this.panel.show();
	
	
};
function UrlWidget(args) {
    var _this = this;

    _.extend(this, Backbone.Events);

    this.id = Utils.genId("UrlWidget");

    this.targetId = null;
    this.title = "Custom url";
    this.width = 500;
    this.height = 400;

    _.extend(this, args);
    this.on(this.handlers);

};

UrlWidget.prototype.draw = function () {
    if (this.panel == null) {
        this.render();
    }
    this.panel.show();
};

UrlWidget.prototype.render = function () {
    var _this = this;

    this.urlField = Ext.create('Ext.form.field.Text', {
        margin: "0 2 2 0",
        labelWidth: 30,
        width: this.width - 55,
        fieldLabel: 'URL',
        emptyText: 'enter a valid url',
//		value : "http://das.sanger.ac.uk/das/grc_region_GRCh37/features",
        value: "http://www.ensembl.org/das/Homo_sapiens.GRCh37.gene/features",
        listeners: { change: {fn: function () {
            var dasName = this.value.split('/das/')[1].split('/')[0];
            _this.trackNameField.setValue(dasName);
        }}
        }
    });
    this.checkButton = Ext.create('Ext.button.Button', {
        text: 'Check',
        handler: function () {
            _this.form.setLoading();
//			var dasDataAdapter = new DasRegionDataAdapter({
//				url : _this.urlField.getValue()
//			});
//			dasDataAdapter.successed.addEventListener(function() {
//				_this.contentArea.setValue(dasDataAdapter.xml);
//				_this.form.setLoading(false);
//			});
//
//			dasDataAdapter.onError.addEventListener(function() {
//				_this.contentArea.setValue("XMLHttpRequest cannot load. This server is not allowed by Access-Control-Allow-Origin");
//				_this.form.setLoading(false);
//			});
//			dasDataAdapter.fill(1, 1, 1);

            var dasAdapter = new DasAdapter({
                url: _this.urlField.getValue(),
                featureCache: {
                    gzip: false,
                    chunkSize: 10000
                },
                handlers: {
                    'url:check': function (event) {
                        console.log(event.data);
                        _this.contentArea.setValue(event.data);
                        _this.form.setLoading(false);

                    },
                    'error': function () {
                        _this.contentArea.setValue("XMLHttpRequest cannot load. This server is not allowed by Access-Control-Allow-Origin");
                        _this.form.setLoading(false);

                    }
                }
            });

            dasAdapter.checkUrl();
        }
    });
    this.trackNameField = Ext.create('Ext.form.field.Text', {
        name: 'file',
//        fieldLabel: 'Track name',
        allowBlank: false,
        value: _this.urlField.value.split('/das/')[1].split('/')[0],
        emptyText: 'Choose a name',
        flex: 1
    });
    this.panelSettings = Ext.create('Ext.panel.Panel', {
        layout: 'hbox',
        border: false,
        title: 'Track name',
        cls: "panel-border-top",
        bodyPadding: 10,
        width: this.width - 2,
        items: [this.trackNameField]
    });
    this.contentArea = Ext.create('Ext.form.field.TextArea', {
        margin: "-1",
        width: this.width,
        height: this.height
    });
    this.infobar = Ext.create('Ext.toolbar.Toolbar', {
        height: 28,
        cls: "bio-border-false",
        items: [this.urlField, this.checkButton]
    });
    this.form = Ext.create('Ext.panel.Panel', {
        border: false,
        items: [this.infobar, this.contentArea, this.panelSettings]
    });

    this.panel = Ext.create('Ext.ux.Window', {
        title: this.title,
        layout: 'fit',
        resizable: false,
        items: [this.form],
        buttons: [
            {
                text: 'Add',
                handler: function () {
                    _this.trigger('addButton:click', {name: _this.trackNameField.getValue(), url: _this.urlField.getValue()});
                    _this.panel.close();
                }
            },
            {text: 'Cancel', handler: function () {
                _this.panel.close();
            }}
        ],
        listeners: {
            destroy: function () {
                delete _this.panel;
            }
        }
    });
};
function FileWidget(args){
	var _this=this;

    _.extend(this, Backbone.Events);

    this.id = Utils.genId("FileWidget");
	this.targetId;
	this.wum = true;
	this.tags = [];
    this.viewer;
    this.title;
	this.dataAdapter;

    this.args = args;

    _.extend(this, args);


    this.on(this.handlers);

//	this.browserData = new BrowserDataWidget();
	/** Events i listen **/
//	this.browserData.onSelect.addEventListener(function (sender, data){
//		_this.trackNameField.setValue(data.filename);
//		_this.fileNameLabel.setText('<span class="emph">'+ data.filename +'</span> <span class="info">(server)</span>',false);
//		_this.panel.setLoading();
//	});
//    this.browserData.adapter.onReadData.addEventListener(function (sender, data){
//    	console.log(data)
//    	_this.trackNameField.setValue(data.filename);
//    	_this.fileNameLabel.setText('<span class="emph">'+ data.filename +'</span> <span class="info">(server)</span>',false);
//    	_this.loadFileFromServer(data);
//    	_this.panel.setLoading(false);
//	});
    
//    this.chartWidgetByChromosome = new ChartWidget({height:200,width:570});
};

FileWidget.prototype.getTitleName = function(){
	return this.trackNameField.getValue();
};


FileWidget.prototype.getFileFromServer = function(){
	//abstract method
};

FileWidget.prototype.loadFileFromLocal = function(){
	//abstract method
};

//FileWidget.prototype.getChartItems = function(){
//	return [this.chartWidgetByChromosome.getChart(["features","chromosome"])];
//};

FileWidget.prototype.getFileUpload = function(){
	var _this = this;
	this.uploadField = Ext.create('Ext.form.field.File', {
		msgTarget : 'side',
		flex:1,
        padding:1,
//		width:75,
		emptyText: 'Choose a file',
        allowBlank: false,
        anchor: '100%',
		buttonText : 'Browse local',
//		buttonOnly : true,
		listeners : {
			change : {
				fn : function() {
					_this.panel.setLoading();
					var file = document.getElementById(_this.uploadField.fileInputEl.id).files[0];

					_this.trackNameField.setValue(file.name);
					_this.fileNameLabel.setText('<span class="emph">'+ file.name +'</span> <span class="info">(local)</span>',false);
					_this.loadFileFromLocal(file);
					_this.panel.setLoading(false);

				}
			}
		}
	});
	return this.uploadField;
};


FileWidget.prototype.draw = function(){
	var _this = this;
	
	if (this.openDialog == null){
	
		/** Bar for the chart **/
		var featureCountBar = Ext.create('Ext.toolbar.Toolbar');
		this.featureCountLabel = Ext.create('Ext.toolbar.TextItem', {
			text:'<span class="dis">No file loaded</span>'
		});
		featureCountBar.add([this.featureCountLabel]);
		
		/** Bar for the file upload browser **/
		var browseBar = Ext.create('Ext.toolbar.Toolbar',{cls:'bio-border-false'});
		browseBar.add(this.getFileUpload());
		
		this.panel = Ext.create('Ext.panel.Panel', {
			border: false,
			cls:'panel-border-top panel-border-bottom',
	//		padding: "0 0 10 0",
			height:230,
			title: "Previsualization",
//		    items : this.getChartItems(),
		    bbar:featureCountBar
		});
		
	//	var colorPicker = Ext.create('Ext.picker.Color', {
	//	    value: '993300',  // initial selected color
	//	    listeners: {
	//	        select: function(picker, selColor) {
	//	            alert(selColor);
	//	        }
	//	    }
	//	});
		this.trackNameField = Ext.create('Ext.form.field.Text',{
			name: 'file',
            fieldLabel: 'Track Name',
            allowBlank: false,
            value: 'New track from '+this.title+' file',
            emptyText: 'Choose a name',
            flex:1
		});
		
		var panelSettings = Ext.create('Ext.panel.Panel', {
			border: false,
			layout: 'hbox',
			bodyPadding: 10,
		    items : [this.trackNameField]	 
		});
		
		
		if(this.wum){
//			this.btnBrowse = Ext.create('Ext.button.Button', {
//		        text: 'Browse server',
//		        disabled:true,
////		        iconCls:'icon-local',
////		        cls:'x-btn-default-small',
//		        handler: function (){
//	    	   		_this.browserData.draw($.cookie('bioinfo_sid'),_this.tags);
//	       		}
//			});
			
//			browseBar.add(this.btnBrowse);
			
			if($.cookie('bioinfo_sid') != null){
				this.sessionInitiated();
			}else{
				this.sessionFinished();
			}
		}
		
		this.fileNameLabel = Ext.create('Ext.toolbar.TextItem', {
//			text:'<span class="emph">Select a <span class="info">local</span> file or a <span class="info">server</span> file from your account.</span>'
		});
//		browseBar.add(['->',this.fileNameLabel]);
		
		
		
		this.btnOk = Ext.create('Ext.button.Button', {
			text:'Ok',
			disabled:true,
			handler: function(){
				_this.trigger('okButton:click',{fileName:_this.file.name, adapter:_this.adapter});
				_this.openDialog.close();
			}
		});
		
		this.openDialog = Ext.create('Ext.window.Window', {
			title : 'Open '+this.title+' file',
//			taskbar:Ext.getCmp(this.args.viewer.id+'uxTaskbar'),
			width : 600,
	//		bodyPadding : 10,
			resizable:false,
			items : [browseBar, /*this.panel,*/ panelSettings],
			buttons:[this.btnOk, 
			         {text:'Cancel', handler: function(){_this.openDialog.close();}}],
			listeners: {
			    	scope: this,
			    	minimize:function(){
						this.openDialog.hide();
			       	},
			      	destroy: function(){
			       		delete this.openDialog;
			      	}
		    	}
		});
		
	}
	this.openDialog.show();
};

//FileWidget.prototype._loadChartInfo = function(){
//
//	var datastore = new Array();
// 	for ( var chromosome in this.adapter.featuresByChromosome) {
//		datastore.push({ features: this.adapter.featuresByChromosome[chromosome], chromosome: chromosome });
//	}
// 	this.chartWidgetByChromosome.getStore().loadData(datastore);
//
// 	this.panel.setLoading(false);
// 	this.featureCountLabel.setText("Features count: " + this.adapter.featuresCount, false);
//};



FileWidget.prototype.sessionInitiated = function (){
//	if(this.btnBrowse!=null){
//		this.btnBrowse.enable();
//	}
};
FileWidget.prototype.sessionFinished = function (){
//	if(this.btnBrowse!=null){
//		this.btnBrowse.disable();
//	}
};
VCFFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
VCFFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
VCFFileWidget.prototype.draw = FileWidget.prototype.draw;
VCFFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
VCFFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
VCFFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
VCFFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function VCFFileWidget(args){
	if (args == null){
		args = new Object();
	}
	args.title = "VCF";
	args.tags = ["vcf"];
	FileWidget.prototype.constructor.call(this, args);
};

VCFFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	this.adapter = new VCFDataAdapter(new FileDataSource({file:file}),{species:this.viewer.species});
	this.adapter.on('file:load',function(sender){
		console.log(_this.adapter.featuresByChromosome);
//		_this._loadChartInfo();
	});
	_this.btnOk.enable();
};

VCFFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	this.adapter = new VCFDataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
//	this._loadChartInfo();
	this.btnOk.enable();
};


GFFFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
GFFFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
GFFFileWidget.prototype.draw = FileWidget.prototype.draw;
GFFFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
GFFFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
GFFFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
GFFFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function GFFFileWidget(args){
	if (args == null){
		args = {};
	}
	this.version = "2";
    if (args.version!= null){
    	this.version = args.version;       
    }
	args.title = "GFF"+this.version;
	args.tags = ["gff"];
	FileWidget.prototype.constructor.call(this, args);
};



GFFFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	switch(this.version){
	case "2":
	case 2:
		this.adapter = new GFF2DataAdapter(new FileDataSource({file:file}),{species:this.viewer.species});
		break;
	case "3":
	case 3:
		this.adapter = new GFF3DataAdapter(new FileDataSource({file:file}),{species:this.viewer.species});
		break;
	default :
		this.adapter = new GFF2DataAdapter(new FileDataSource({file:file}),{species:this.viewer.species});
		break;
	}
	
	this.adapter.on('file:load',function(e){
//		_this._loadChartInfo();
	});
	_this.btnOk.enable();
};


GFFFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	switch(this.version){
	case "2":
	case 2:
		this.adapter = new GFF2DataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
		break;
	case "3":
	case 3:
		this.adapter = new GFF3DataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
		break;
	default :
		this.adapter = new GFF2DataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
		break;
	}
	
	this._loadChartInfo();
	this.btnOk.enable();
};

BEDFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
BEDFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
BEDFileWidget.prototype.draw = FileWidget.prototype.draw;
BEDFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
BEDFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
BEDFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
BEDFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function BEDFileWidget(args){
	if (args == null){
		args = new Object();
	}
	args.title = "BED";
	args.tags = ["bed"];
	FileWidget.prototype.constructor.call(this, args);
	
};


BEDFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	this.adapter = new BEDDataAdapter(new FileDataSource({file:file}),{species:this.viewer.species});
    this.adapter.on('file:load',function(e){
//		_this._loadChartInfo();
    });
	_this.btnOk.enable();
};


BEDFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	this.adapter = new BEDDataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
	this._loadChartInfo();
	this.btnOk.enable();
};

function Track(args) {
    this.width = 200;
    this.height = 200;


    this.dataAdapter;
    this.renderer;
    this.resizable = true;
    this.autoHeight = false;
    this.targetId;
    this.id;
    this.title;
    this.minHistogramRegionSize = 300000000;
    this.maxLabelRegionSize = 300000000;
    this.height = 100;
    this.visibleRegionSize;
    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-14';

    _.extend(this, args);

    this.pixelBase;
    this.svgCanvasWidth = 500000;//mesa
    this.pixelPosition = this.svgCanvasWidth / 2;
    this.svgCanvasOffset;
    this.svgCanvasFeatures;
    this.status;
    this.histogram;
    this.histogramLogarithm;
    this.histogramMax;
    this.interval;

    this.svgCanvasLeftLimit;
    this.svgCanvasRightLimit;


    this.invalidZoomText;

    this.renderedArea = {};//used for renders to store binary trees
    this.chunksDisplayed = {};//used to avoid painting multiple times features contained in more than 1 chunk

    if ('handlers' in this) {
        for (eventName in this.handlers) {
            this.on(eventName, this.handlers[eventName]);
        }
    }

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

Track.prototype = {

    get: function (attr) {
        return this[attr];
    },

    set: function (attr, value) {
        this[attr] = value;
    },
    hide: function () {
        $(this.div).css({display: 'hidden'});
    },
    show: function () {
        $(this.div).css({display: 'auto'});
    },
    hideContent: function () {
        $(this.svgdiv).css({display: 'hidden'});
        $(this.titlediv).css({display: 'hidden'});
    },
    showContent: function () {
        $(this.svgdiv).css({display: 'auto'});
        $(this.titlediv).css({display: 'auto'});
    },
    toggleContent: function () {
        $(this.svgdiv).toggle('hidden');
        $(this.resizeDiv).toggle('hidden');
        $(this.configBtn).toggle('hidden');
    },
    setSpecies: function (species) {
        this.species = species;
        this.dataAdapter.species = this.species
    },

    setWidth: function (width) {
        this.width = width;
        this.main.setAttribute("width", width);
    },
    _updateDIVHeight: function () {
//        $(this.rrr).remove();
//        delete this.rrr;
//        this.rrr = SVG.addChild(this.svgCanvasFeatures, "rect", {
//            'x': 0,
//            'y': 0,
//            'width': 0,
//            'height': 18,
//            'stroke': '#3B0B0B',
//            'stroke-width': 1,
//            'stroke-opacity': 1,
//            'fill': 'black',
//            'cursor': 'pointer'
//        });
        if (this.resizable) {
            if (this.histogram) {
                $(this.svgdiv).css({'height': this.height + 10});
            } else {
                var x = this.pixelPosition;
                var width = this.width;
                var lastContains = 0;
                for (var i in this.renderedArea) {
                    if (this.renderedArea[i].contains({start: x, end: x + width })) {
                        lastContains = i;
                    }
                }
                var divHeight = parseInt(lastContains) + 20;
                $(this.svgdiv).css({'height': divHeight + 25});
//                this.rrr.setAttribute('x', x);
//                this.rrr.setAttribute('y', divHeight);
//                this.rrr.setAttribute('width', width);
            }
        }
    },
    _updateSVGHeight: function () {
        if (this.resizable && !this.histogram) {
            var renderedHeight = Object.keys(this.renderedArea).length * 20;//this must be passed by config, 20 for test
            this.main.setAttribute('height', renderedHeight);
            this.svgCanvasFeatures.setAttribute('height', renderedHeight);
            this.hoverRect.setAttribute('height', renderedHeight);
        }
    },
    updateHeight: function (ignoreAutoHeight) {
        this._updateSVGHeight();
        if (this.autoHeight || ignoreAutoHeight) {
            this._updateDIVHeight();
        }
    },
    enableAutoHeight: function () {
        this.autoHeight = true;
        this.updateHeight();
    },
    setTitle: function (title) {
        $(this.titlediv).html(title);
    },

    setLoading: function (bool) {
        if (bool) {
            this.svgLoading.setAttribute("visibility", "visible");
            this.status = "rendering";
        } else {
            this.svgLoading.setAttribute("visibility", "hidden");
            this.status = "ready";
            this.trigger('track:ready', {sender: this});
        }
    },

    updateHistogramParams: function () {
        if (this.region.length() > this.minHistogramRegionSize) {
            this.histogram = true;
            this.histogramLogarithm = true;
            this.histogramMax = 500;
            this.interval = Math.ceil(10 / this.pixelBase);//server interval limit 512
        } else {
            this.histogram = undefined;
            this.histogramLogarithm = undefined;
            this.histogramMax = undefined;
            this.interval = undefined;
        }

//        if (this.histogramRenderer) {
//            if (this.zoom <= this.histogramZoom) {
//                this.histogramGroup.setAttribute('visibility', 'visible');
//            } else {
//                this.histogramGroup.setAttribute('visibility', 'hidden');
//            }
//        }
    },

    cleanSvg: function (filters) {//clean
//        console.time("-----------------------------------------empty");
        while (this.svgCanvasFeatures.firstChild) {
            this.svgCanvasFeatures.removeChild(this.svgCanvasFeatures.firstChild);
        }
//        console.timeEnd("-----------------------------------------empty");
        this.chunksDisplayed = {};
        this.renderedArea = {};
    },

    initializeDom: function (targetId) {

        var _this = this;
        var div = $('<div id="' + this.id + '-div"></div>')[0];
        var titleBardiv = $('' +
            '<div class="btn-toolbar ocb-compactable">' +
            '   <div class="btn-group btn-group-xs">' +
            '   <button id="configBtn" type="button" class="btn btn-xs btn-primary"><span class="glyphicon glyphicon-cog"></span></button>' +
            '   <button id="titleBtn" type="button" class="btn btn-xs btn-default" data-toggle="button"><span id="titleDiv">' + this.title + '</span></button>' +
            '   </div>' +
            '</div>')[0];

        if (_.isUndefined(this.title)) {
            $(titleBardiv).addClass("hidden");
        }

        var titlediv = $(titleBardiv).find('#titleDiv')[0];
        var titleBtn = $(titleBardiv).find('#titleBtn')[0];
        var configBtn = $(titleBardiv).find('#configBtn')[0];


        var svgdiv = $('<div id="' + this.id + '-svgdiv"></div>')[0];
        var resizediv = $('<div id="' + this.id + '-resizediv" class="ocb-track-resize"></div>')[0];

        $(targetId).addClass("unselectable");
        $(targetId).append(div);
        $(div).append(titleBardiv);
        $(div).append(svgdiv);
        $(div).append(resizediv);


        /** title div **/
        $(titleBardiv).css({'padding': '4px'})
            .on('dblclick', function (e) {
                e.stopPropagation();
            });
        $(titleBtn).click(function (e) {
            _this.toggleContent();
        });

        /** svg div **/
        $(svgdiv).css({
            'z-index': 3,
            'height': this.height,
            'overflow-y': (this.resizable) ? 'auto' : 'hidden',
            'overflow-x': 'hidden'
        });

        var main = SVG.addChild(svgdiv, 'svg', {
            'id': this.id,
            'class': 'trackSvg',
            'x': 0,
            'y': 0,
            'width': this.width,
            'height': this.height
        });


        if (this.resizable) {
            $(resizediv).mousedown(function (event) {
                $('html').addClass('unselectable');
                event.stopPropagation();
                var downY = event.clientY;
                $('html').bind('mousemove.genomeViewer', function (event) {
                    var despY = (event.clientY - downY);
                    var actualHeight = $(svgdiv).outerHeight();
                    $(svgdiv).css({height: actualHeight + despY});
                    downY = event.clientY;
                    _this.autoHeight = false;
                });
            });
            $('html').bind('mouseup.genomeViewer', function (event) {
                $('html').removeClass('unselectable');
                $('html').off('mousemove.genomeViewer');
            });
            $(svgdiv).closest(".trackListPanels").mouseup(function (event) {
                _this.updateHeight();
            });


            $(resizediv).mouseenter(function (event) {
                $(this).css({'cursor': 'ns-resize'});
                $(this).css({'opacity': 1});
            });
            $(resizediv).mouseleave(function (event) {
                $(this).css({'cursor': 'default'});
                $(this).css({'opacity': 0.3});
            });

        }

        this.svgGroup = SVG.addChild(main, "g", {
        });

        var text = this.title;
        var hoverRect = SVG.addChild(this.svgGroup, 'rect', {
            'x': 0,
            'y': 0,
            'width': this.width,
            'height': this.height,
            'opacity': '0.6',
            'fill': 'transparent'
        });

        this.svgCanvasFeatures = SVG.addChild(this.svgGroup, 'svg', {
            'class': 'features',
            'x': -this.pixelPosition,
            'width': this.svgCanvasWidth,
            'height': this.height
        });


        this.fnTitleMouseEnter = function () {
            hoverRect.setAttribute('opacity', '0.1');
            hoverRect.setAttribute('fill', 'lightblue');
        };
        this.fnTitleMouseLeave = function () {
            hoverRect.setAttribute('opacity', '0.6');
            hoverRect.setAttribute('fill', 'transparent');
        };

        $(this.svgGroup).off('mouseenter');
        $(this.svgGroup).off('mouseleave');
        $(this.svgGroup).mouseenter(this.fnTitleMouseEnter);
        $(this.svgGroup).mouseleave(this.fnTitleMouseLeave);


        this.invalidZoomText = SVG.addChild(this.svgGroup, 'text', {
            'x': 154,
            'y': 18,
            'opacity': '0.6',
            'fill': 'black',
            'visibility': 'hidden',
            'class': this.fontClass
        });
        this.invalidZoomText.textContent = "Zoom in to view the sequence";


        var loadingImg = '<?xml version="1.0" encoding="utf-8"?>' +
            '<svg version="1.1" width="22px" height="22px" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">' +
            '<defs>' +
            '<g id="pair">' +
            '<ellipse cx="7" cy="0" rx="4" ry="1.7" style="fill:#ccc; fill-opacity:0.5;"/>' +
            '<ellipse cx="-7" cy="0" rx="4" ry="1.7" style="fill:#aaa; fill-opacity:1.0;"/>' +
            '</g>' +
            '</defs>' +
            '<g transform="translate(11,11)">' +
            '<g>' +
            '<animateTransform attributeName="transform" type="rotate" from="0" to="360" dur="1.5s" repeatDur="indefinite"/>' +
            '<use xlink:href="#pair"/>' +
            '<use xlink:href="#pair" transform="rotate(45)"/>' +
            '<use xlink:href="#pair" transform="rotate(90)"/>' +
            '<use xlink:href="#pair" transform="rotate(135)"/>' +
            '</g>' +
            '</g>' +
            '</svg>';

        this.svgLoading = SVG.addChildImage(main, {
            "xlink:href": "data:image/svg+xml," + encodeURIComponent(loadingImg),
            "x": 10,
            "y": 0,
            "width": 22,
            "height": 22,
            "visibility": "hidden"
        });

        this.div = div;
        this.svgdiv = svgdiv;
        this.titlediv = titlediv;
        this.resizeDiv = resizediv;
        this.configBtn = configBtn;

        this.main = main;
        this.hoverRect = hoverRect;
//        this.titleText = titleText;


//        if (this.histogramRenderer) {
//            this._drawHistogramLegend();
//        }

        this.rendered = true;
        this.status = "ready";

    },
    _drawHistogramLegend: function () {
        var histogramHeight = this.histogramRenderer.histogramHeight;
        var multiplier = this.histogramRenderer.multiplier;

        this.histogramGroup = SVG.addChild(this.svgGroup, 'g', {
            'class': 'histogramGroup',
            'visibility': 'hidden'
        });
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 21,
            "y": histogramHeight + 4,
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "0-";
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 14,
            "y": histogramHeight + 4 - (Math.log(10) * multiplier),
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "10-";
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 7,
            "y": histogramHeight + 4 - (Math.log(100) * multiplier),
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "100-";
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 0,
            "y": histogramHeight + 4 - (Math.log(1000) * multiplier),
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "1000-";
    },

//    showInfoWidget: function (args) {
//        if (this.dataAdapter.species == "orange") {
//            //data.resource+="orange";
//            if (args.featureType.indexOf("gene") != -1)
//                args.featureType = "geneorange";
//            if (args.featureType.indexOf("transcript") != -1)
//                args.featureType = "transcriptorange";
//        }
//        switch (args.featureType) {
//            case "gene":
//                new GeneInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "geneorange":
//                new GeneOrangeInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "transcriptorange":
//                new TranscriptOrangeInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "transcript":
//                new TranscriptInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "snp" :
//                new SnpInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "vcf" :
//                new VCFVariantInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            default:
//                break;
//        }
//    },

    draw: function () {

    },

    getFeaturesToRenderByChunk: function (response, filters) {
        //Returns an array avoiding already drawn features in this.chunksDisplayed

        var getChunkId = function (position) {
            return Math.floor(position / response.chunkSize);
        };
        var getChunkKey = function (chromosome, chunkId) {
            return chromosome + ":" + chunkId;
        };

        var chunks = response.items;
        var features = [];


        var feature, displayed, featureFirstChunk, featureLastChunk, features = [];
        for (var i = 0, leni = chunks.length; i < leni; i++) {
            if (this.chunksDisplayed[chunks[i].chunkKey] != true) {//check if any chunk is already displayed and skip it

                for (var j = 0, lenj = chunks[i].value.length; j < lenj; j++) {
                    feature = chunks[i].value[j];

                    //check if any feature has been already displayed by another chunk
                    displayed = false;
                    featureFirstChunk = getChunkId(feature.start);
                    featureLastChunk = getChunkId(feature.end);
                    for (var chunkId = featureFirstChunk; chunkId <= featureLastChunk; chunkId++) {
                        var chunkKey = getChunkKey(feature.chromosome, chunkId);
                        if (this.chunksDisplayed[chunkKey] == true) {
                            displayed = true;
                            break;
                        }
                    }
                    if (!displayed) {
                        //apply filter
                        // if(filters != null) {
                        //		var pass = true;
                        // 		for(filter in filters) {
                        // 			pass = pass && filters[filter](feature);
                        //			if(pass == false) {
                        //				break;
                        //			}
                        // 		}
                        //		if(pass) features.push(feature);
                        // } else {
                        features.push(feature);
                    }
                }
                this.chunksDisplayed[chunks[i].chunkKey] = true;
            }
        }
        return features;
    }
};

FeatureTrack.prototype = new Track({});

function FeatureTrack(args) {
    Track.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args

    //save default render reference;
    this.defaultRenderer = this.renderer;
//    this.histogramRenderer = new FeatureClusterRenderer();
    this.histogramRenderer = new HistogramRenderer(args);

    this.featureType = 'Feature';
    //set instantiation args, must be last
    _.extend(this, args);


    this.resource = this.dataAdapter.resource;
    this.species = this.dataAdapter.species;

    this.dataType = 'features';
};

FeatureTrack.prototype.render = function (targetId) {
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.dataAdapter.on('data:ready', function (event) {
        var features;
        if (event.dataType == 'histogram') {
            _this.renderer = _this.histogramRenderer;
            features = event.items;
        } else {
            _this.renderer = _this.defaultRenderer;
            features = _this.getFeaturesToRenderByChunk(event);
        }
        _this.renderer.render(features, {
            svgCanvasFeatures: _this.svgCanvasFeatures,
            featureTypes: _this.featureTypes,
            renderedArea: _this.renderedArea,
            pixelBase: _this.pixelBase,
            position: _this.region.center(),
            regionSize: _this.region.length(),
            maxLabelRegionSize: _this.maxLabelRegionSize,
            width: _this.width,
            pixelPosition: _this.pixelPosition,
            resource:_this.resource,
            species:_this.species,
            featureType:_this.featureType
        });
        _this.updateHeight();
        _this.setLoading(false);
    });
};

FeatureTrack.prototype.draw = function () {
    var _this = this;

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2;

    this.updateHistogramParams();
    this.cleanSvg();

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        this.dataAdapter.getData({
            dataType: this.dataType,
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            }),
            params: {
                histogram: this.histogram,
                histogramLogarithm: this.histogramLogarithm,
                histogramMax: this.histogramMax,
                interval: this.interval
            }
        });

        this.invalidZoomText.setAttribute("visibility", "hidden");
    } else {
        this.invalidZoomText.setAttribute("visibility", "visible");
    }
    _this.updateHeight();
};


FeatureTrack.prototype.move = function (disp) {
    var _this = this;

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    _this.region.center();
    var pixelDisplacement = disp * _this.pixelBase;
    this.pixelPosition -= pixelDisplacement;

    //parseFloat important
    var move = parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x", move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {

        if (disp > 0 && virtualStart < this.svgCanvasLeftLimit) {
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if (disp < 0 && virtualEnd > this.svgCanvasRightLimit) {
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset)
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset);
        }

    }

};

SequenceTrack.prototype = new Track({});

function SequenceTrack(args) {
    args.resizable = false;
    Track.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args

    _.extend(this, args);
};

SequenceTrack.prototype.render = function (targetId) {
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.dataAdapter.on('data:ready', function (event) {
        _this.renderer.render(event, {
            svgCanvasFeatures: _this.svgCanvasFeatures,
            pixelBase: _this.pixelBase,
            position: _this.region.center(),
            width: _this.width,
            pixelPosition: _this.pixelPosition
        });
        _this.setLoading(false);
    });
};

SequenceTrack.prototype.draw = function () {
    var _this = this;
    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.cleanSvg();

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        var data = this.dataAdapter.getData({
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            })
        });
        this.invalidZoomText.setAttribute("visibility", "hidden");
    } else {
        this.invalidZoomText.setAttribute("visibility", "visible");
    }


};


SequenceTrack.prototype.move = function (disp) {
    var _this = this;
    var pixelDisplacement = disp * _this.pixelBase;
    this.pixelPosition -= pixelDisplacement;

    //parseFloat important
    var move = parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x", move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);

    // check if track is visible in this region size
    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        if (disp > 0 && virtualStart < this.svgCanvasLeftLimit) {
            this.dataAdapter.getData({
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                sender: 'move'
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if (disp < 0 && virtualEnd > this.svgCanvasRightLimit) {
            this.dataAdapter.getData({
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset),
                }),
                sender: 'move'
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset);
        }

    }

};
GeneTrack.prototype = new Track({});

function GeneTrack(args) {
    Track.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.minTranscriptRegionSize;

    //save default render reference;
    this.defaultRenderer = this.renderer;
//    this.histogramRenderer = new FeatureClusterRenderer();
    this.histogramRenderer = new HistogramRenderer(args);


    //set instantiation args, must be last
    _.extend(this, args);

    this.exclude;

};

GeneTrack.prototype.render = function (targetId) {
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.dataAdapter.on('data:ready', function (event) {
        var features;
        if (event.dataType == 'histogram') {
            _this.renderer = _this.histogramRenderer;
            features = event.items;
        } else {
            _this.renderer = _this.defaultRenderer;
            features = _this.getFeaturesToRenderByChunk(event);
        }
        _this.renderer.render(features, {
            svgCanvasFeatures: _this.svgCanvasFeatures,
            featureTypes: _this.featureTypes,
            renderedArea: _this.renderedArea,
            pixelBase: _this.pixelBase,
            position: _this.region.center(),
            regionSize: _this.region.length(),
            maxLabelRegionSize: _this.maxLabelRegionSize,
            width: _this.width,
            pixelPosition: _this.pixelPosition

        });
        _this.updateHeight();
        _this.setLoading(false);
    });

    this.renderer.on('feature:click', function (event) {
        _this.showInfoWidget(event);
    });
};

GeneTrack.prototype.updateTranscriptParams = function () {
    if (this.region.length() < this.minTranscriptRegionSize) {
        this.exclude = this.dataAdapter.params.exclude;
    } else {
        this.exclude = 'transcripts';
    }
};

GeneTrack.prototype.draw = function () {
    var _this = this;

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2;

    this.updateTranscriptParams();
    this.updateHistogramParams();
    this.cleanSvg();

    var dataType = 'features';

    if (!_.isUndefined(this.exclude)) {
        dataType = 'features' + this.exclude;
    }

    if (this.histogram) {
        dataType = 'histogram';
    }


    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        var data = this.dataAdapter.getData({
            dataType: dataType,
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            }),
            params: {
                histogram: this.histogram,
                histogramLogarithm: this.histogramLogarithm,
                histogramMax: this.histogramMax,
                interval: this.interval,
                exclude: this.exclude
            }
        });

        this.invalidZoomText.setAttribute("visibility", "hidden");
    } else {
        this.invalidZoomText.setAttribute("visibility", "visible");
    }
    _this.updateHeight();
};


GeneTrack.prototype.move = function (disp) {
    var _this = this;

    this.dataType = 'features';

    if (!_.isUndefined(this.exclude)) {
        dataType = 'features' + this.exclude;
    }

    if (this.histogram) {
        this.dataType = 'histogram';
    }

//    trackSvg.position = _this.region.center();
    _this.region.center();
    var pixelDisplacement = disp * _this.pixelBase;
    this.pixelPosition -= pixelDisplacement;

    //parseFloat important
    var move = parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x", move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);
    // check if track is visible in this zoom

//    console.log(virtualStart+'  ----  '+virtualEnd)
//    console.log(this.svgCanvasLeftLimit+'  ----  '+this.svgCanvasRightLimit)
//    console.log(this.svgCanvasOffset)

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {

        if (disp > 0 && virtualStart < this.svgCanvasLeftLimit) {
            console.log('left')
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval,
                    exclude: this.exclude
                }
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if (disp < 0 && virtualEnd > this.svgCanvasRightLimit) {
            console.log('right')
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset)
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval,
                    exclude: this.exclude
                }
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset);
        }
    }
};

GeneTrack.prototype.showInfoWidget = function (args) {
    switch (args.featureType) {
        case "gene":
            new GeneInfoWidget(null, this.dataAdapter.species).draw(args);
            break;
        case "transcript":
            new TranscriptInfoWidget(null, this.dataAdapter.species).draw(args);
            break;
        default:
            break;
    }
};

BamTrack.prototype = new Track({});

function BamTrack(args) {
    Track.call(this,args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args

    //save default render reference;
    this.defaultRenderer = this.renderer;
    this.histogramRenderer = new HistogramRenderer();


    this.chunksDisplayed = {};

    //set instantiation args, must be last
    _.extend(this, args);

    this.dataType = 'features';
};

BamTrack.prototype.render = function(targetId){
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset*2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset*2

    this.dataAdapter.on('data:ready',function(event){
        var features;
        if (event.dataType == 'histogram') {
            _this.renderer = _this.histogramRenderer;
            features = event.items;
        } else {
            _this.renderer = _this.defaultRenderer;
            features = _this._removeDisplayedChunks(event);
        }
        _this.renderer.render(features, {
            svgCanvasFeatures : _this.svgCanvasFeatures,
            featureTypes:_this.featureTypes,
            renderedArea:_this.renderedArea,
            pixelBase : _this.pixelBase,
            position : _this.region.center(),
            region : _this.region,
            width : _this.width,
            regionSize: _this.region.length(),
            maxLabelRegionSize: _this.maxLabelRegionSize,
            pixelPosition : _this.pixelPosition
        });

        _this.updateHeight();
        _this.setLoading(false);
    });

};

BamTrack.prototype.draw = function(){
    var _this = this;

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset*2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset*2

    this.updateHistogramParams();
    this.cleanSvg();

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        this.dataAdapter.getData({
            dataType: this.dataType,
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            }),
            params: {
                histogram: this.histogram,
                histogramLogarithm: this.histogramLogarithm,
                histogramMax: this.histogramMax,
                interval: this.interval
            }
        });

        this.invalidZoomText.setAttribute("visibility", "hidden");
    }else{
        this.invalidZoomText.setAttribute("visibility", "visible");
    }
    _this.updateHeight();
};


BamTrack.prototype.move = function(disp){
    var _this = this;

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    _this.region.center();
    var pixelDisplacement = disp*_this.pixelBase;
    this.pixelPosition-=pixelDisplacement;

    //parseFloat important
    var move =  parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x",move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {

        if(disp>0 && virtualStart < this.svgCanvasLeftLimit){
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if(disp<0 && virtualEnd > this.svgCanvasRightLimit){
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset)
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit+this.svgCanvasOffset);
        }

    }

};

BamTrack.prototype._removeDisplayedChunks = function(response){
    //Returns an array avoiding already drawn features in this.chunksDisplayed
    var chunks = response.items;
    var dataType = response.dataType;
    var newChunks = [];
//    var chromosome = response.params.chromosome;

    var feature, displayed, featureFirstChunk, featureLastChunk, features = [];
    for ( var i = 0, leni = chunks.length; i < leni; i++) {//loop over chunks
        if(this.chunksDisplayed[chunks[i].chunkKey] != true){//check if any chunk is already displayed and skip it

            features = []; //initialize array, will contain features not drawn by other drawn chunks
            for ( var j = 0, lenj =  chunks[i].value.reads.length; j < lenj; j++) {
                feature = chunks[i].value.reads[j];
                var chrChunkCache = this.dataAdapter.cache[dataType];

                //check if any feature has been already displayed by another chunk
                displayed = false;
                featureFirstChunk = chrChunkCache.getChunkId(feature.start);
                featureLastChunk = chrChunkCache.getChunkId(feature.end);
                for(var chunkId=featureFirstChunk; chunkId<=featureLastChunk; chunkId++){//loop over chunks touched by this feature
                    var chunkKey = chrChunkCache.getChunkKey(feature.chromosome, chunkId);
                    if(this.chunksDisplayed[chunkKey]==true){
                        displayed = true;
                        break;
                    }
                }
                if(!displayed){
                    features.push(feature);
                }
            }
            this.chunksDisplayed[chunks[i].chunkKey]=true;
            chunks[i].value.reads = features;//update features array
            newChunks.push(chunks[i]);
        }
    }
    response.items = newChunks;
    return response;
};
//Parent class for all renderers
function Renderer(args) {


};

Renderer.prototype = {

    render: function (items) {

    },

    getFeatureX: function (feature, args) {//returns svg feature x value from feature genomic position
        var middle = args.width / 2;
        var x = args.pixelPosition + middle - ((args.position - feature.start) * args.pixelBase);
        return x;
    },

    getDefaultConfig: function (type) {
        return FEATURE_TYPES[type];
    },
    getLabelWidth: function (label, args) {
        /* insert in dom to get the label width and then remove it*/
        var svgLabel = SVG.create("text", {
            'font-weight': 400,
            'class':this.fontClass
        });
        svgLabel.textContent = label;
        $(args.svgCanvasFeatures).append(svgLabel);
        var svgLabelWidth = $(svgLabel).width();
        $(svgLabel).remove();
        return svgLabelWidth;
    }
}
;
//any item with chromosome start end
FeatureRenderer.prototype = new Renderer({});

function FeatureRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

     if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};


FeatureRenderer.prototype.render = function (features, args) {
    var _this = this;
    var draw = function (feature, svgGroup) {

        if (typeof feature.featureType === 'undefined') {
            feature.featureType = args.featureType;
        }
        //get feature render configuration
        var color = _.isFunction(_this.color) ? _this.color(feature) : _this.color;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;

        //get feature genomic information
        var start = feature.start;
        var end = feature.end;
        var length = (end - start) + 1;

        //check genomic length
        length = (length < 0) ? Math.abs(length) : length;
        length = (length == 0) ? 1 : length;

        //transform to pixel position
        var width = length * args.pixelBase;

//        var svgLabelWidth = _this.getLabelWidth(label, args);
        var svgLabelWidth = label.length * 6.4;

        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);

        var maxWidth = Math.max(width, 2);
        var textHeight = 0;
        if (args.maxLabelRegionSize > args.regionSize) {
            textHeight = 9;
            maxWidth = Math.max(width, svgLabelWidth);
        }


        var rowY = 0;
        var textY = textHeight + height;
        var rowHeight = textHeight + height + 2;

        while (true) {
            if (!(rowY in args.renderedArea)) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }
            var foundArea = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});

            if (foundArea) {
                var featureGroup = SVG.addChild(svgGroup, "g", {'feature_id': feature.id});
                var rect = SVG.addChild(featureGroup, "rect", {
                    'x': x,
                    'y': rowY,
                    'width': width,
                    'height': height,
                    'stroke': '#3B0B0B',
                    'stroke-width': 1,
                    'stroke-opacity': 0.7,
                    'fill': color,
                    'cursor': 'pointer'
                });
                if (args.maxLabelRegionSize > args.regionSize) {
                    var text = SVG.addChild(featureGroup, "text", {
                        'i': i,
                        'x': x,
                        'y': textY,
                        'font-weight': 400,
                        'opacity': null,
                        'fill': 'black',
                        'cursor': 'pointer',
                        'class': _this.fontClass
                    });
                    text.textContent = label;
                }

                if ('tooltipText' in _this) {
                    $(featureGroup).qtip({
                        content: {text: tooltipText, title: tooltipTitle},
//                        position: {target: "mouse", adjust: {x: 15, y: 0}, effect: false},
                        position: {target: "mouse", adjust: {x: 25, y: 15}},
                        style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                    });
                }

                $(featureGroup).mouseover(function (event) {
                    _this.trigger('feature:mouseover', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, mouseoverEvent: event})
                });

                $(featureGroup).click(function (event) {
                    _this.trigger('feature:click', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, clickEvent: event})
                });
                break;
            }
            rowY += rowHeight;
            textY += rowHeight;
        }
    };


    /****/
    var timeId = "write dom " + Utils.randomString(4);
    console.time(timeId);
    console.log(features.length);
    /****/


    var svgGroup = SVG.create('g');
    for (var i = 0, leni = features.length; i < leni; i++) {
        draw(features[i], svgGroup);
    }
    args.svgCanvasFeatures.appendChild(svgGroup);


    /****/
    console.timeEnd(timeId);
    /****/
};

SequenceRenderer.prototype = new Renderer({});

function SequenceRenderer(args){
    Renderer.call(this,args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-ubuntumono ocb-font-size-16';
    this.toolTipfontClass = 'ocb-font-default';

    _.extend(this, args);

};


SequenceRenderer.prototype.render = function(features, args) {

    console.time("Sequence render "+features.items.sequence.length);
    var middle = args.width/2;

    var start = features.items.start;
    var seqStart = features.items.start;
    var seqString = features.items.sequence;

    for ( var i = 0; i < seqString.length; i++) {
        var x = args.pixelPosition+middle-((args.position-start)*args.pixelBase);
        start++;

        var text = SVG.addChild(args.svgCanvasFeatures,"text",{
            'x':x+1,
            'y':12,
            'fill':SEQUENCE_COLORS[seqString.charAt(i)],
            'class': this.fontClass
        });
        text.textContent = seqString.charAt(i);
        $(text).qtip({
            content:seqString.charAt(i)+" "+(seqStart+i).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,")/*+'<br>'+phastCons[i]+'<br>'+phylop[i]*/,
            position: {target: 'mouse', adjust: {x:15, y:0}, viewport: $(window), effect: false},
            style: { width:true, classes: this.toolTipfontClass+' qtip-light qtip-shadow'}
        });
    }

    console.timeEnd("Sequence render "+features.items.sequence.length);
//    this.trackSvgLayout.setNucleotidPosition(this.position);

};

FeatureClusterRenderer.prototype = new Renderer({});

function FeatureClusterRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.histogramHeight = 75;
    this.multiplier = 7;



//    this.maxValue = 100;
//    if (args != null) {
//        if (args.height != null) {
//            this.histogramHeight = args.height * 0.95;
//        }
//        if (args.histogramMaxFreqValue != null) {
//            this.maxValue = args.histogramMaxFreqValue;
//        }
//    }
//    this.multiplier = this.histogramHeight / this.maxValue;

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    //set instantiation args
    _.extend(this, args);

};


FeatureClusterRenderer.prototype.render = function (features, args) {
    var _this = this;
    var middle = args.width / 2;
    var maxValue = 0;

    var drawFeature = function (feature) {
        var d = '';

        feature.start = parseInt(feature.start);
        feature.end = parseInt(feature.end);
        var width = (feature.end - feature.start);

        width = width * args.pixelBase;
        var x = _this.getFeatureX(feature, args);

        if (feature.features_count == null) {
//            var height = Math.log(features[i].absolute);
            if (feature.absolute != 0) {
                feature.features_count = Math.log(features[i].absolute);
            } else {
                feature.features_count = 0;
            }
        }

        var height = feature.features_count * _this.multiplier;

        var rect = SVG.addChild(args.svgCanvasFeatures, "rect", {
            'x': x + 1,
            'y': 0,
            'width': width - 1,
            'height': height,
            'stroke': 'smokewhite',
            'stroke-width': 1,
            'fill': '#9493b1',
            'cursor': 'pointer'
        });

        var getInfo = function (feature) {
            var resp = '';
            return resp += Math.round(Math.exp(feature.features_count));
        };


        var url = CellBaseManager.url({
            species: args.species,
            category: 'genomic',
            subCategory: 'region',
            query: new Region(feature).toString(),
            resource: args.resource,
            params: {
                include: 'chromosome,start,end,id',
                limit: 20
            },
            async: false
//            success:function(data){
//                str+=data.response[0].result.length+' cb';
//            }
        });

        $(rect).qtip({
            content: {
                text: 'Loading...', // The text to use whilst the AJAX request is loading
                ajax: {
                    url: url, // URL to the local file
                    type: 'GET', // POST or GET
                    success: function (data, status) {
                        var items = data.response[0].result;
                        var ids = '';
                        for (var i = 0; i < items.length; i++) {
                            var f = items[i];
                            var r = new Region(f);
                            ids += '<span class="emph">' + f.id + '</span> <span class="info">' + r.toString() + '</span><br>';
                        }
                        var fc = Math.round(Math.exp(feature.features_count));
                        if (fc <= 20) {
                            this.set('content.title', 'Count: ' + items.length);
                            this.set('content.text', ids);
                        } else {
                            this.set('content.title', 'Count: ' + fc);
                            this.set('content.text', ids + '...');
                        }
                    }
                }
            },
            position: {target: 'mouse', adjust: {x: 25, y: 15}},
            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
        });

//        $(rect).qtip({
//            content: {text: getInfo(feature), title: 'Count'},
//
//        });

//        $(rect).mouseenter(function(){
//            var str = '';
////            $(rect).qtip({
////                content: {text: str, title: 'Info'},
//////                position: {target: "mouse", adjust: {x: 25, y: 15}},
////                style: { width: true, classes: 'ui-tooltip ui-tooltip-shadow'}
////            });
//        });

    };

    for (var i = 0, len = features.length; i < len; i++) {
        drawFeature(features[i].value);
    }
};

HistogramRenderer.prototype = new Renderer({});

function HistogramRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.histogramHeight = 75;
//    this.multiplier = 7;

    this.maxValue = 10;
    if (args != null) {
        if (args.height != null) {
            this.histogramHeight = args.height * 0.95;
        }
        if (args.histogramMaxFreqValue != null) {
            this.maxValue = args.histogramMaxFreqValue;
        }
    }
    //this.multiplier = 7;
    this.multiplier = this.histogramHeight / this.maxValue;

    //set instantiation args
    _.extend(this, args);

};


HistogramRenderer.prototype.render = function (features, args) {
    var middle = args.width / 2;
    var points = '';
    if (features.length > 0) {//Force first point at this.histogramHeight
        var firstFeature = features[0].value;
        var width = (firstFeature.end - firstFeature.start) * args.pixelBase;
        var x = args.pixelPosition + middle - ((args.position - parseInt(firstFeature.start)) * args.pixelBase);
        points = (x + (width / 2)) + ',' + this.histogramHeight + ' ';
    }

    var maxValue = 0;

    for (var i = 0, len = features.length; i < len; i++) {

        var feature = features[i].value;
        feature.start = parseInt(feature.start);
        feature.end = parseInt(feature.end);
        var width = (feature.end - feature.start);
        //get type settings object

        width = width * args.pixelBase;
        var x = args.pixelPosition + middle - ((args.position - feature.start) * args.pixelBase);

        if (feature.features_count == null) {
//            var height = Math.log(features[i].absolute);
            if (feature.absolute != 0) {
                feature.features_count = Math.log(features[i].absolute);
            } else {
                feature.features_count = 0;
            }
        }

//        var height = features[i].features_count;
//        if (height == null) {
//            height = features[i].value;
//            height = this.histogramHeight * height;
//        } else {
//        }
        var height = feature.features_count * this.multiplier;


        points += (x + (width / 2)) + "," + (this.histogramHeight - height) + " ";

    }
    if (features.length > 0) {//force last point at this.histogramHeight
        var lastFeature = features[features.length - 1].value;
        var width = (lastFeature.end - lastFeature.start) * args.pixelBase;
        var x = args.pixelPosition + middle - ((args.position - parseInt(lastFeature.start)) * args.pixelBase);
        points += (x + (width / 2)) + ',' + this.histogramHeight + ' ';

    }

    var pol = SVG.addChild(args.svgCanvasFeatures, "polyline", {
        "points": points,
        "stroke": "#000000",
        "stroke-width": 0.2,
        "fill": '#9493b1',
        "cursor": "pointer"
    });
};

//any item with chromosome start end
GeneRenderer.prototype = new Renderer({});

function GeneRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};

GeneRenderer.prototype.setFeatureConfig = function (configObject) {
    _.extend(this, configObject);
};

GeneRenderer.prototype.render = function (features, args) {
    var _this = this;
    var draw = function (feature) {
        //get feature render configuration

        //get feature render configuration
        _this.setFeatureConfig(FEATURE_TYPES.gene);
        var color = _.isFunction(_this.color) ? _this.color(feature) : _this.color;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;


        //get feature genomic information
        var start = feature.start;
        var end = feature.end;
        var length = (end - start) + 1;

        //transform to pixel position
        var width = length * args.pixelBase;


//        var svgLabelWidth = _this.getLabelWidth(label, args);
        var svgLabelWidth = label.length * 6.4;

        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);

        var maxWidth = Math.max(width, 2);
        var textHeight = 0;
        if (args.maxLabelRegionSize > args.regionSize) {
            textHeight = 9;
            maxWidth = Math.max(width, svgLabelWidth);
        }

        var rowY = 0;
        var textY = textHeight + height + 1;
        var rowHeight = textHeight + height + 5;

        while (true) {
            if (!(rowY in args.renderedArea)) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }

            var foundArea;//if true, i can paint

            //check if gene transcripts can be painted
            var checkRowY = rowY;
            var foundTranscriptsArea = true;
            if (!_.isEmpty(feature.transcripts)) {
                for (var i = 0, leni = feature.transcripts.length + 1; i < leni; i++) {
                    if (!(checkRowY in args.renderedArea)) {
                        args.renderedArea[checkRowY] = new FeatureBinarySearchTree();
                    }
                    if (args.renderedArea[checkRowY].contains({start: x, end: x + maxWidth - 1})) {
                        foundTranscriptsArea = false;
                        break;
                    }
                    checkRowY += rowHeight;
                }
                if (foundTranscriptsArea == true) {
                    foundArea = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});
                }
            } else {
                foundArea = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});
            }

            //paint genes
            if (foundArea) {
                var featureGroup = SVG.addChild(args.svgCanvasFeatures, "g", {'feature_id': feature.id});
                var rect = SVG.addChild(featureGroup, 'rect', {
                    'x': x,
                    'y': rowY,
                    'width': width,
                    'height': height,
                    'stroke': '#3B0B0B',
                    'stroke-width': 0.5,
                    'fill': color,
                    'cursor': 'pointer'
                });

                if (args.maxLabelRegionSize > args.regionSize) {
                    var text = SVG.addChild(featureGroup, 'text', {
                        'i': i,
                        'x': x,
                        'y': textY,
                        'fill': 'black',
                        'cursor': 'pointer',
                        'class': _this.fontClass
                    });
                    text.textContent = label;
                }

                $(featureGroup).qtip({
                    content: {text: tooltipText, title: tooltipTitle},
//                    position: {target: "mouse", adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                    position: {target: "mouse", adjust: {x: 25, y: 15}},
                    style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                });

                $(featureGroup).click(function (event) {
                    _this.trigger('feature:click', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, clickEvent: event});
                });


                //paint transcripts
                var checkRowY = rowY + rowHeight;
                var checkTextY = textY + rowHeight;
                if (!_.isEmpty(feature.transcripts)) {
                    for (var i = 0, leni = feature.transcripts.length; i < leni; i++) { /*Loop over transcripts*/
                        if (!(checkRowY in args.renderedArea)) {
                            args.renderedArea[checkRowY] = new FeatureBinarySearchTree();
                        }
                        var transcript = feature.transcripts[i];
                        var transcriptX = _this.getFeatureX(transcript, args);
                        var transcriptWidth = (transcript.end - transcript.start + 1) * ( args.pixelBase);

                        //get type settings object
                        _this.setFeatureConfig(FEATURE_TYPES.transcript);
                        var transcriptColor = _.isFunction(_this.color) ? _this.color(transcript) : _this.color;
                        var label = _.isFunction(_this.label) ? _this.label(transcript) : _this.label;
                        var height = _.isFunction(_this.height) ? _this.height(transcript) : _this.height;
                        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(transcript) : _this.tooltipTitle;
                        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(transcript) : _this.tooltipText;
                        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(transcript) : _this.infoWidgetId;

                        //se resta el trozo del final del gen hasta el principio del transcrito y se le suma el texto del transcrito
//                        var svgLabelWidth = _this.getLabelWidth(label, args);
                        var svgLabelWidth = label.length * 6.4;
                        var maxWidth = Math.max(width, width - ((feature.end - transcript.start) * ( args.pixelBase)) + svgLabelWidth);


                        //add to the tree the transcripts size
                        args.renderedArea[checkRowY].add({start: x, end: x + maxWidth - 1});


                        var transcriptGroup = SVG.addChild(args.svgCanvasFeatures, 'g', {
                            "widgetId": transcript[infoWidgetId]
                        });


                        var rect = SVG.addChild(transcriptGroup, 'rect', {//this rect its like a line
                            'x': transcriptX,
                            'y': checkRowY + 1,
                            'width': transcriptWidth,
                            'height': height,
                            'fill': 'gray',
                            'cursor': 'pointer',
                            'feature_id': transcript.id
                        });
                        var text = SVG.addChild(transcriptGroup, 'text', {
                            'x': transcriptX,
                            'y': checkTextY,
                            'opacity': null,
                            'fill': 'black',
                            'cursor': 'pointer',
                            'class': _this.fontClass
                        });
                        text.textContent = label;


                        $(transcriptGroup).qtip({
                            content: {text: tooltipText, title: tooltipTitle},
//                            position: {target: 'mouse', adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                            position: {target: "mouse", adjust: {x: 25, y: 15}},
                            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                        });
                        $(transcriptGroup).click(function (event) {
                            var query = this.getAttribute("widgetId");
                            _this.trigger('feature:click', {query: query, feature: transcript, featureType: transcript.featureType, clickEvent: event});
                        });

                        //paint exons
                        for (var e = 0, lene = feature.transcripts[i].exons.length; e < lene; e++) {/* loop over exons*/
                            var exon = feature.transcripts[i].exons[e];
                            var exonStart = parseInt(exon.start);
                            var exonEnd = parseInt(exon.end);
                            var middle = args.width / 2;

                            var exonX = args.pixelPosition + middle - ((args.position - exonStart) * args.pixelBase);
                            var exonWidth = (exonEnd - exonStart + 1) * ( args.pixelBase);


                            _this.setFeatureConfig(FEATURE_TYPES.exon);
                            var color = _.isFunction(_this.color) ? _this.color(exon) : _this.color;
                            var label = _.isFunction(_this.label) ? _this.label(exon) : _this.label;
                            var height = _.isFunction(_this.height) ? _this.height(exon) : _this.height;
                            var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(exon) : _this.tooltipTitle;
                            var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(exon, transcript) : _this.tooltipText;
                            var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(exon) : _this.infoWidgetId;

                            var exonGroup = SVG.addChild(args.svgCanvasFeatures, "g");

                            $(exonGroup).qtip({
                                content: {text: tooltipText, title: tooltipTitle},
//                                position: {target: 'mouse', adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                                position: {target: "mouse", adjust: {x: 25, y: 15}},
                                style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                            });

                            var eRect = SVG.addChild(exonGroup, "rect", {//paint exons in white without coding region
                                "i": i,
                                "x": exonX,
                                "y": checkRowY - 1,
                                "width": exonWidth,
                                "height": height,
                                "stroke": "gray",
                                "stroke-width": 1,
                                "fill": "white",
                                "cursor": "pointer"
                            });
                            //XXX now paint coding region
                            var codingStart = 0;
                            var codingEnd = 0;
                            // 5'-UTR
                            if (transcript.genomicCodingStart > exonStart && transcript.genomicCodingStart < exonEnd) {
                                codingStart = parseInt(transcript.genomicCodingStart);
                                codingEnd = exonEnd;
                            } else {
                                // 3'-UTR
                                if (transcript.genomicCodingEnd > exonStart && transcript.genomicCodingEnd < exonEnd) {
                                    codingStart = exonStart;
                                    codingEnd = parseInt(transcript.genomicCodingEnd);
                                } else
                                // all exon is transcribed
                                if (transcript.genomicCodingStart < exonStart && transcript.genomicCodingEnd > exonEnd) {
                                    codingStart = exonStart;
                                    codingEnd = exonEnd;
                                }
//									else{
//										if(exonEnd < transcript.genomicCodingStart){
//
//									}
                            }
                            var coding = codingEnd - codingStart;
                            var codingX = args.pixelPosition + middle - ((args.position - codingStart) * args.pixelBase);
                            var codingWidth = (coding + 1) * ( args.pixelBase);

                            if (coding > 0) {
                                var cRect = SVG.addChild(exonGroup, "rect", {
                                    "i": i,
                                    "x": codingX,
                                    "y": checkRowY - 1,
                                    "width": codingWidth,
                                    "height": height,
                                    "stroke": transcriptColor,
                                    "stroke-width": 1,
                                    "fill": transcriptColor,
                                    "cursor": "pointer"
                                });
                                //XXX draw phase only at zoom 100, where this.pixelBase=10
                                for (var p = 0, lenp = 3 - exon.phase; p < lenp && Math.round(args.pixelBase) == 10 && exon.phase != -1 && exon.phase != null; p++) {//==10 for max zoom only
                                    SVG.addChild(exonGroup, "rect", {
                                        "i": i,
                                        "x": codingX + (p * 10),
                                        "y": checkRowY - 1,
                                        "width": args.pixelBase,
                                        "height": height,
                                        "stroke": color,
                                        "stroke-width": 1,
                                        "fill": 'white',
                                        "cursor": "pointer"
                                    });
                                }
                            }


                        }

                        checkRowY += rowHeight;
                        checkTextY += rowHeight;
                    }
                }// if transcrips != null
                break;
            }
            rowY += rowHeight;
            textY += rowHeight;
        }
    };

    //process features
    for (var i = 0, leni = features.length; i < leni; i++) {
        draw(features[i]);
    }
};
//any item with chromosome start end
BamRenderer.prototype = new Renderer({});

function BamRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};


BamRenderer.prototype.render = function (response, args) {
    var _this = this;


    //CHECK VISUALIZATON MODE
    if (_.isUndefined(response.params)) {
        response.params = {};
    }

    var viewAsPairs = false;
    if (response.params["view_as_pairs"] != null) {
        viewAsPairs = true;
    }
    console.log("viewAsPairs " + viewAsPairs);
    var insertSizeMin = 0;
    var insertSizeMax = 0;
    var variantColor = "orangered";
    if (response.params["insert_size_interval"] != null) {
        insertSizeMin = response.params["insert_size_interval"].split(",")[0];
        insertSizeMax = response.params["insert_size_interval"].split(",")[1];
    }
    console.log("insertSizeMin " + insertSizeMin);
    console.log("insertSizeMin " + insertSizeMax);

    //Prevent browser context menu
    $(args.svgCanvasFeatures).contextmenu(function (e) {
        console.log("click derecho")
        e.preventDefault();
    });

    console.time("BamRender " + response.params.resource);

    var chunkList = response.items;

//    var middle = this.width / 2;

    var bamCoverGroup = SVG.addChild(args.svgCanvasFeatures, "g", {
        "class": "bamCoverage",
        "cursor": "pointer"
    });
    var bamReadGroup = SVG.addChild(args.svgCanvasFeatures, "g", {
        "class": "bamReads",
        "cursor": "pointer"
    });

    var drawCoverage = function (chunk) {
        //var coverageList = chunk.coverage.all;
        var coverageList = chunk.coverage.all;
        var coverageListA = chunk.coverage.a;
        var coverageListC = chunk.coverage.c;
        var coverageListG = chunk.coverage.g;
        var coverageListT = chunk.coverage.t;
        var start = parseInt(chunk.start);
        var end = parseInt(chunk.end);
        var pixelWidth = (end - start + 1) * args.pixelBase;

        var middle = args.width / 2;
        var points = "", pointsA = "", pointsC = "", pointsG = "", pointsT = "";
        var baseMid = (args.pixelBase / 2) - 0.5;//4.5 cuando pixelBase = 10

        var x, y, p = parseInt(chunk.start);
        var lineA = "", lineC = "", lineG = "", lineT = "";
        var coverageNorm = 200, covHeight = 50;
        for (var i = 0; i < coverageList.length; i++) {
            //x = _this.pixelPosition+middle-((_this.position-p)*_this.pixelBase)+baseMid;
            x = args.pixelPosition + middle - ((args.position - p) * args.pixelBase);
            xx = args.pixelPosition + middle - ((args.position - p) * args.pixelBase) + args.pixelBase;

            lineA += x + "," + coverageListA[i] / coverageNorm * covHeight + " ";
            lineA += xx + "," + coverageListA[i] / coverageNorm * covHeight + " ";
            lineC += x + "," + (coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineC += xx + "," + (coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineG += x + "," + (coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineG += xx + "," + (coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineT += x + "," + (coverageListT[i] + coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineT += xx + "," + (coverageListT[i] + coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";

            p++;
        }

        //reverse to draw the polylines(polygons) for each nucleotid
        var rlineC = lineC.split(" ").reverse().join(" ").trim();
        var rlineG = lineG.split(" ").reverse().join(" ").trim();
        var rlineT = lineT.split(" ").reverse().join(" ").trim();

        var firstPoint = args.pixelPosition + middle - ((args.position - parseInt(chunk.start)) * args.pixelBase) + baseMid;
        var lastPoint = args.pixelPosition + middle - ((args.position - parseInt(chunk.end)) * args.pixelBase) + baseMid;

        var polA = SVG.addChild(bamCoverGroup, "polyline", {
            "points": firstPoint + ",0 " + lineA + lastPoint + ",0",
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"gray",
            "fill": "green"
        });
        var polC = SVG.addChild(bamCoverGroup, "polyline", {
            "points": lineA + " " + rlineC,
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"black",
            "fill": "blue"
        });
        var polG = SVG.addChild(bamCoverGroup, "polyline", {
            "points": lineC + " " + rlineG,
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"black",
            "fill": "gold"
        });
        var polT = SVG.addChild(bamCoverGroup, "polyline", {
            "points": lineG + " " + rlineT,
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"black",
            "fill": "red"
        });

        var dummyRect = SVG.addChild(bamCoverGroup, "rect", {
            "x": args.pixelPosition + middle - ((args.position - start) * args.pixelBase),
            "y": 0,
            "width": pixelWidth,
            "height": covHeight,
            "opacity": "0.5",
            "fill": "lightgray",
            "cursor": "pointer"
        });


        $(dummyRect).qtip({
            content: " ",
            position: {target: 'mouse', adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip-shadow'}
        });


//        args.trackSvgLayout.onMousePosition.addEventListener(function (sender, obj) {
//            var pos = obj.mousePos - parseInt(chunk.start);
//            //if(coverageList[pos]!=null){
//            var str = 'depth: <span class="ssel">' + coverageList[pos] + '</span><br>' +
//                '<span style="color:green">A</span>: <span class="ssel">' + chunk.coverage.a[pos] + '</span><br>' +
//                '<span style="color:blue">C</span>: <span class="ssel">' + chunk.coverage.c[pos] + '</span><br>' +
//                '<span style="color:darkgoldenrod">G</span>: <span class="ssel">' + chunk.coverage.g[pos] + '</span><br>' +
//                '<span style="color:red">T</span>: <span class="ssel">' + chunk.coverage.t[pos] + '</span><br>';
//            $(dummyRect).qtip('option', 'content.text', str);
//            //}
//        });
    };

    var drawSingleRead = function (feature) {
        //var start = feature.start;
        //var end = feature.end;
        var start = feature.unclippedStart;
        var end = feature.unclippedEnd;
        var length = (end - start) + 1;
        var diff = feature.diff;

        //get feature render configuration
        var color = _.isFunction(_this.color) ? _this.color(feature, args.region.chromosome) : _this.color;
        var strokeColor = _.isFunction(_this.strokeColor) ? _this.strokeColor(feature, args.region.chromosome) : _this.strokeColor;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var strand = _.isFunction(_this.strand) ? _this.strand(feature) : _this.strand;
        var mateUnmappedFlag = _.isFunction(_this.mateUnmappedFlag) ? _this.mateUnmappedFlag(feature) : _this.mateUnmappedFlag;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;

        if (insertSizeMin != 0 && insertSizeMax != 0 && !mateUnmappedFlag) {
            if (Math.abs(feature.inferredInsertSize) > insertSizeMax) {
                color = 'maroon';
            }
            if (Math.abs(feature.inferredInsertSize) < insertSizeMin) {
                color = 'navy';
            }
        }

        //transform to pixel position
        var width = length * args.pixelBase;
        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);
//		try{
//			var maxWidth = Math.max(width, /*settings.getLabel(feature).length*8*/0); //XXX cuidado : text.getComputedTextLength()
//		}catch(e){
//			var maxWidth = 72;
//		}
        maxWidth = width;

        var rowHeight = 12;
        var rowY = 70;
//		var textY = 12+settings.height;
        while (true) {
            if (args.renderedArea[rowY] == null) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }
            var enc = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});
            if (enc) {
                var featureGroup = SVG.addChild(bamReadGroup, "g", {'feature_id': feature.name});
                var points = {
                    "Reverse": x + "," + (rowY + (height / 2)) + " " + (x + 5) + "," + rowY + " " + (x + width - 5) + "," + rowY + " " + (x + width - 5) + "," + (rowY + height) + " " + (x + 5) + "," + (rowY + height),
                    "Forward": x + "," + rowY + " " + (x + width - 5) + "," + rowY + " " + (x + width) + "," + (rowY + (height / 2)) + " " + (x + width - 5) + "," + (rowY + height) + " " + x + "," + (rowY + height)
                }
                var poly = SVG.addChild(featureGroup, "polygon", {
                    "points": points[strand],
                    "stroke": strokeColor,
                    "stroke-width": 1,
                    "fill": color,
                    "cursor": "pointer"
                });

                //var rect = SVG.addChild(featureGroup,"rect",{
                //"x":x+offset[strand],
                //"y":rowY,
                //"width":width-4,
                //"height":settings.height,
                //"stroke": "white",
                //"stroke-width":1,
                //"fill": color,
                //"clip-path":"url(#"+_this.id+"cp)",
                //"fill": 'url(#'+_this.id+'bamStrand'+strand+')',
                //});
                //readEls.push(rect);

                if (diff != null && args.regionSize < 400) {
                    //var	t = SVG.addChild(featureGroup,"text",{
                    //"x":x+1,
                    //"y":rowY+settings.height-1,
                    //"fill":"darkred",
                    //"textLength":width,
                    //"cursor": "pointer"
                    //});
                    //t.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space","preserve");
                    //t.textContent = diff;
                    //readEls.push(t);
                    var path = SVG.addChild(featureGroup, "path", {
                        "d": Utils.genBamVariants(diff, args.pixelBase, x, rowY),
                        "fill": variantColor
                    });
                }
                $(featureGroup).qtip({
                    content: {text: tooltipText, title: tooltipTitle},
                    position: {target: "mouse", adjust: {x: 25, y: 15}},
                    style: { width: 300, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'},
                    show: 'click',
                    hide: 'click mouseleave'
                });


//                $(featureGroup).click(function (event) {
//                    console.log(feature);
//                    _this.trigger('feature:click', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, clickEvent: event})
////                    _this.showInfoWidget({query: feature[settings.infoWidgetId], feature: feature, featureType: feature.featureType, adapter: _this.trackData.adapter});
//                });
                break;
            }
            rowY += rowHeight;
//			textY += rowHeight;
        }
    };

    var drawPairedReads = function (read, mate) {
        var readStart = read.unclippedStart;
        var readEnd = read.unclippedEnd;
        var mateStart = mate.unclippedStart;
        var mateEnd = mate.unclippedEnd;
        var readDiff = read.diff;
        var mateDiff = mate.diff;
        /*get type settings object*/
        var readSettings = _this.types[read.featureType];
        var mateSettings = _this.types[mate.featureType];
        var readColor = readSettings.getColor(read, _this.region.chromosome);
        var mateColor = mateSettings.getColor(mate, _this.region.chromosome);
        var readStrand = readSettings.getStrand(read);
        var matestrand = mateSettings.getStrand(mate);

        if (insertSizeMin != 0 && insertSizeMax != 0) {
            if (Math.abs(read.inferredInsertSize) > insertSizeMax) {
                readColor = 'maroon';
                mateColor = 'maroon';
            }
            if (Math.abs(read.inferredInsertSize) < insertSizeMin) {
                readColor = 'navy';
                mateColor = 'navy';
            }
        }

        var pairStart = readStart;
        var pairEnd = mateEnd;
        if (mateStart <= readStart) {
            pairStart = mateStart;
        }
        if (readEnd >= mateEnd) {
            pairEnd = readEnd;
        }

        /*transform to pixel position*/
        var pairWidth = ((pairEnd - pairStart) + 1) * _this.pixelBase;
        var pairX = _this.pixelPosition + middle - ((_this.position - pairStart) * _this.pixelBase);

        var readWidth = ((readEnd - readStart) + 1) * _this.pixelBase;
        var readX = _this.pixelPosition + middle - ((_this.position - readStart) * _this.pixelBase);

        var mateWidth = ((mateEnd - mateStart) + 1) * _this.pixelBase;
        var mateX = _this.pixelPosition + middle - ((_this.position - mateStart) * _this.pixelBase);

        var rowHeight = 12;
        var rowY = 70;
//		var textY = 12+settings.height;

        while (true) {
            if (args.renderedArea[rowY] == null) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }
            var enc = args.renderedArea[rowY].add({start: pairX, end: pairX + pairWidth - 1});
            if (enc) {
                var readEls = [];
                var mateEls = [];
                var readPoints = {
                    "Reverse": readX + "," + (rowY + (readSettings.height / 2)) + " " + (readX + 5) + "," + rowY + " " + (readX + readWidth - 5) + "," + rowY + " " + (readX + readWidth - 5) + "," + (rowY + readSettings.height) + " " + (readX + 5) + "," + (rowY + readSettings.height),
                    "Forward": readX + "," + rowY + " " + (readX + readWidth - 5) + "," + rowY + " " + (readX + readWidth) + "," + (rowY + (readSettings.height / 2)) + " " + (readX + readWidth - 5) + "," + (rowY + readSettings.height) + " " + readX + "," + (rowY + readSettings.height)
                }
                var readPoly = SVG.addChild(bamReadGroup, "polygon", {
                    "points": readPoints[readStrand],
                    "stroke": readSettings.getStrokeColor(read),
                    "stroke-width": 1,
                    "fill": readColor,
                    "cursor": "pointer"
                });
                readEls.push(readPoly);
                var matePoints = {
                    "Reverse": mateX + "," + (rowY + (mateSettings.height / 2)) + " " + (mateX + 5) + "," + rowY + " " + (mateX + mateWidth - 5) + "," + rowY + " " + (mateX + mateWidth - 5) + "," + (rowY + mateSettings.height) + " " + (mateX + 5) + "," + (rowY + mateSettings.height),
                    "Forward": mateX + "," + rowY + " " + (mateX + mateWidth - 5) + "," + rowY + " " + (mateX + mateWidth) + "," + (rowY + (mateSettings.height / 2)) + " " + (mateX + mateWidth - 5) + "," + (rowY + mateSettings.height) + " " + mateX + "," + (rowY + mateSettings.height)
                }
                var matePoly = SVG.addChild(bamReadGroup, "polygon", {
                    "points": matePoints[matestrand],
                    "stroke": mateSettings.getStrokeColor(mate),
                    "stroke-width": 1,
                    "fill": mateColor,
                    "cursor": "pointer"
                });
                mateEls.push(matePoly);

                var line = SVG.addChild(bamReadGroup, "line", {
                    "x1": (readX + readWidth),
                    "y1": (rowY + (readSettings.height / 2)),
                    "x2": mateX,
                    "y2": (rowY + (readSettings.height / 2)),
                    "stroke-width": "1",
                    "stroke": "gray",
                    //"stroke-color": "black",
                    "cursor": "pointer"
                });

                if (args.regionSize < 400) {
                    if (readDiff != null) {
                        var readPath = SVG.addChild(bamReadGroup, "path", {
                            "d": Utils.genBamVariants(readDiff, _this.pixelBase, readX, rowY),
                            "fill": variantColor
                        });
                        readEls.push(readPath);
                    }
                    if (mateDiff != null) {
                        var matePath = SVG.addChild(bamReadGroup, "path", {
                            "d": Utils.genBamVariants(mateDiff, _this.pixelBase, mateX, rowY),
                            "fill": variantColor
                        });
                        mateEls.push(matePath);
                    }
                }

                $(readEls).qtip({
                    content: {text: readSettings.getTipText(read), title: readSettings.getTipTitle(read)},
                    position: {target: "mouse", adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                    style: { width: 280, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'},
                    show: 'click',
                    hide: 'click mouseleave'
                });
                $(readEls).click(function (event) {
                    console.log(read);
                    _this.showInfoWidget({query: read[readSettings.infoWidgetId], feature: read, featureType: read.featureType, adapter: _this.trackData.adapter});
                });
                $(mateEls).qtip({
                    content: {text: mateSettings.getTipText(mate), title: mateSettings.getTipTitle(mate)},
                    position: {target: "mouse", adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                    style: { width: 280, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'},
                    show: 'click',
                    hide: 'click mouseleave'
                });
                $(mateEls).click(function (event) {
                    console.log(mate);
                    _this.showInfoWidget({query: mate[mateSettings.infoWidgetId], feature: mate, featureType: mate.featureType, adapter: _this.trackData.adapter});
                });
                break;
            }
            rowY += rowHeight;
//			textY += rowHeight;
        }
    };

    var drawChunk = function (chunk) {
        drawCoverage(chunk.value);
        var readList = chunk.value.reads;
        for (var i = 0, li = readList.length; i < li; i++) {
            var read = readList[i];
            if (viewAsPairs) {
                var nextRead = readList[i + 1];
                if (nextRead != null) {
                    if (read.name == nextRead.name) {
                        drawPairedReads(read, nextRead);
                        i++;
                    } else {
                        drawSingleRead(read);
                    }
                }
            } else {
                drawSingleRead(read);
            }
        }
    };

    //process features
    if (chunkList.length > 0) {
        for (var i = 0, li = chunkList.length; i < li; i++) {
            drawChunk(chunkList[i]);
        }
//        var newHeight = Object.keys(this.renderedArea).length * 24;
//        if (newHeight > 0) {
//            this.setHeight(newHeight + /*margen entre tracks*/10 + 70);
//        }
        //TEST
//        this.setHeight(200);
    }
    console.timeEnd("BamRender " + response.params.resource);
};

//any item with chromosome start end
VcfMultisampleRenderer.prototype = new Renderer({});

function VcfMultisampleRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};


VcfMultisampleRenderer.prototype.render = function (features, args) {
    var _this = this;
    var draw = function (feature) {
        //get feature render configuration
        var color = _.isFunction(_this.color) ? _this.color(feature) : _this.color;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;

        //get feature genomic information
        var start = feature.start;
        var end = feature.end;
        var length = (end - start) + 1;

        //check genomic length
        length = (length < 0) ? Math.abs(length) : length;
        length = (length == 0) ? 1 : length;

        //transform to pixel position
        var width = length * args.pixelBase;

        var svgLabelWidth = _this.getLabelWidth(label, args);

        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);

        var maxWidth = Math.max(width, 2);
        var textHeight = 0;
        if (args.regionSize < args.maxLabelRegionSize) {
            textHeight = 9;
            maxWidth = Math.max(width, svgLabelWidth);
        }


        var rowY = 0;
        var textY = textHeight + height;
        var rowHeight = textHeight + height + 2;


//        azul osucuro: 0/0
//        negro: ./.
//        rojo: 1/1
//        naranja 0/1

        var d00 = '';
        var dDD = '';
        var d11 = '';
        var d01 = '';
        var xs = x; // x start
        var xe = x + width; // x end
        var ys = 1; // y
        var yi = 6; //y increment
        var yi2 = 10; //y increment
        for (var i = 0, leni = feature.samples.length; i < leni; i++) {
            args.renderedArea[ys] = new FeatureBinarySearchTree();
            args.renderedArea[ys].add({start: xs, end: xe});
            var genotype = feature.samples[i].split(':')[0];
            switch (genotype) {
                case '0|0':
                case '0/0':
                    d00 += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    d00 += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
                case '.|.':
                case './.':
                    dDD += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    dDD += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
                case '1|1':
                case '1/1':
                    d11 += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    d11 += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
                case '0|1':
                case '0/1':
                case '1|0':
                case '1/0':
                    d01 += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    d01 += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
            }
            ys += yi2;
        }
        var featureGroup = SVG.addChild(args.svgCanvasFeatures, "g", {'feature_id': feature.id});
        var dummyRect = SVG.addChild(featureGroup, "rect", {
            'x': xs,
            'y': 1,
            'width': width,
            'height': ys,
            'fill': 'transparent',
            'cursor': 'pointer'
        });
        if (d00 != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': d00,
                'fill': 'blue',
                'cursor': 'pointer'
            });
        }
        if (dDD != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': dDD,
                'fill': 'black',
                'cursor': 'pointer'
            });
        }
        if (d11 != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': d11,
                'fill': 'red',
                'cursor': 'pointer'
            });
        }
        if (d01 != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': d01,
                'fill': 'orange',
                'cursor': 'pointer'
            });
        }


        var lastSampleIndex = 0;
        $(featureGroup).qtip({
            content: {text: tooltipText + '<br>' + feature.samples[lastSampleIndex], title: tooltipTitle},
//                        position: {target: "mouse", adjust: {x: 15, y: 0}, effect: false},
            position: {target: "mouse", adjust: {x: 25, y: 15}},
            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
        });
        $(featureGroup).mousemove(function (event) {
            var sampleIndex = parseInt(event.offsetY / yi2);
            if (sampleIndex != lastSampleIndex) {
                console.log(sampleIndex);
                $(featureGroup).qtip('option', 'content.text', tooltipText + '<br>' + feature.samples[sampleIndex]);
            }
            lastSampleIndex = sampleIndex;
        });
    };

    //process features
    for (var i = 0, leni = features.length; i < leni; i++) {
        var feature = features[i];
        draw(feature);
    }
};

function GenomeViewer(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    var _this = this;
    this.id = Utils.genId("GenomeViewer");

    //set default args
    this.version = 'Genome Viewer';
    this.targetId;

    this.quickSearchResultFn;
    this.quickSearchDisplayKey;

    this.drawNavigationBar = true;
    this.drawKaryotypePanel = true;
    this.drawChromosomePanel = true;
    this.drawRegionOverviewPanel = true;
    this.overviewZoomMultiplier = 8;
    this.karyotypePanelConfig = {
        collapsed: false,
        collapsible: true
    }
    this.chromosomePanelConfig = {
        collapsed: false,
        collapsible: true
    }
    this.RegionPanelConfig = {
        collapsed: false,
        collapsible: true
    }
    this.drawStatusBar = true;
    this.border = true;
    this.resizable = true;
    this.sidePanel = true;//enable or disable sidePanel at construction
    this.trackListTitle = 'Detailed information';//enable or disable sidePanel at construction
    this.trackPanelScrollWidth = 18;
    this.availableSpecies = {
        "text": "Species",
        "items": [
            {
                "text": "Vertebrates",
                "items": [
                    {"text": "Homo sapiens", "assembly": "GRCh37.p10", "region": {"chromosome": "13", "start": 32889611, "end": 32889611}, "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"], "url": "ftp://ftp.ensembl.org/pub/release-71/"},
                    {"text": "Mus musculus", "assembly": "GRCm38.p1", "region": {"chromosome": "1", "start": 18422009, "end": 18422009}, "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "X", "Y", "MT"], "url": "ftp://ftp.ensembl.org/pub/release-71/"}
                ]
            }
        ]
    };
    this.species = this.availableSpecies.items[0].items[0];
    this.zoom;

    this.chromosomes;
    this.chromosomeList;

    //set instantiation args, must be last
    _.extend(this, args);

    this.defaultRegion = new Region(this.region);

    this.width;
    this.height;
    this.sidePanelWidth = (this.sidePanel) ? 25 : 0;


    //events attachments
    this.on(this.handlers);

    this.fullscreen = false;
    this.resizing = false;


    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
}

GenomeViewer.prototype = {

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div class="bootstrap" id="' + this.id + '" class="ocb-gv ocb-box-vertical"></div>')[0];
        $(this.targetDiv).append(this.div);


        if (typeof this.width === 'undefined') {
            var width = Math.max($(this.div).width(), $(this.targetDiv).width())
            if (width == 0) {
                console.log('target div width is zero');
                return
            }
            this.width = width;
        } else {
            $(this.div).width(this.width);
            $(this.targetDiv).width(this.width);
        }

        if (this.border) {
            var border = (_.isString(this.border)) ? this.border : '1px solid lightgray';
            $(this.div).css({border: border});
        }


        this.navigationbarDiv = $('<div id="navigation-' + this.id + '" class="ocb-gv-navigation"></div>')[0];
        $(this.div).append(this.navigationbarDiv);

        this.centerPanelDiv = $('<div id="center-' + this.id + '" class="ocb-gv-center"></div>')[0];
        $(this.div).append(this.centerPanelDiv);

        this.statusbarDiv = $('<div id="statusbar-' + this.id + '" class="ocb-gv-status"></div>');
        $(this.div).append(this.statusbarDiv);


        this.rightSidebarDiv = $('<div id="rightsidebar-' + this.id + '" style="position:absolute; z-index:50;right:0px;"></div>')[0];
        this.leftSidebarDiv = $('<div id="leftsidebar-' + this.id + '" style="position:absolute; z-index:50;left:0px;"></div>')[0];
        $(this.centerPanelDiv).append(this.rightSidebarDiv);
        $(this.centerPanelDiv).append(this.leftSidebarDiv);


        this.karyotypeDiv = $('<div id="karyotype-' + this.id + '"></div>');
        $(this.centerPanelDiv).append(this.karyotypeDiv);

        this.chromosomeDiv = $('<div id="chromosome-' + this.id + '"></div>');
        $(this.centerPanelDiv).append(this.chromosomeDiv);

        this.trackListPanelsDiv = $('<div id="trackListPanels-' + this.id + '" class="trackListPanels" ></div>');
        $(this.centerPanelDiv).append(this.trackListPanelsDiv);

        this.regionDiv = $('<div id="region-' + this.id + '" ></div>');
        $(this.trackListPanelsDiv).append(this.regionDiv);

        this.tracksDiv = $('<div id="tracks-' + this.id + '" ></div>');
        $(this.trackListPanelsDiv).append(this.tracksDiv);

        this.rendered = true;
    },
    draw: function () {
        if (!this.rendered) {
            console.info('Genome Viewer is not rendered yet');
            return;
        }
        var _this = this;

        this.chromosomes = this.getChromosomes();

        this._setWidth(this.width);
        this.setMinRegion(this.region, this.getSVGCanvasWidth());
        this.zoom = this._calculateZoomByRegion(this.region);

        // Resize
        if (this.resizable) {
            $(window).resize(function (event) {
                if (event.target == window) {
                    if (!_this.resizing) {//avoid multiple resize events
                        _this.resizing = true;
                        _this._setWidth($(_this.targetDiv).width());
                        setTimeout(function () {
                            _this.resizing = false;
                        }, 400);
                    }
                }
            });
//            $(this.targetDiv).resizable({
//                handles: 'e',
//                ghost: true,
//                stop: function (event, ui) {
//                    _this._setWidth($(_this.targetDiv).width());
//                }
//            });
        }


        /* Navigation Bar */
        if (this.drawNavigationBar) {
            this.navigationBar = this._createNavigationBar($(this.navigationbarDiv).attr('id'));
            this.navigationBar.setZoom(this.zoom);
        }


        /*karyotype Panel*/
        if (this.drawKaryotypePanel) {
            this.karyotypePanel = this._drawKaryotypePanel($(this.karyotypeDiv).attr('id'));
        }

        /* Chromosome Panel */
        if (this.drawChromosomePanel) {
            this.chromosomePanel = this._drawChromosomePanel($(this.chromosomeDiv).attr('id'));
        }

        /* Region Panel, is a TrackListPanel Class */
        if (this.drawRegionOverviewPanel) {
            this.regionOverviewPanel = this._createRegionOverviewPanel($(this.regionDiv).attr('id'));
        }
        /*TrackList Panel*/
        this.trackListPanel = this._createTrackListPanel($(this.tracksDiv).attr('id'));

        /*Status Bar*/
        if (this.drawStatusBar) {
            this.statusBar = this._createStatusBar($(this.statusbarDiv).attr('id'));
        }


        this.on('region:change region:move', function (event) {
            if (event.sender != _this) {
                _this._setRegion(event.region);
            }
        });

        this.on('species:change', function (event) {
            _this.species = event.species;
            _this.chromosomes = _this.getChromosomes();
        });

        $("html").bind('keydown.genomeViewer', function (e) {
            switch (e.keyCode) {
                case 40://down arrow
                case 109://minus key
                    if (e.shiftKey) {
                        _this.increaseZoom(-10);
                    }
                    break;
                case 38://up arrow
                case 107://plus key
                    if (e.shiftKey) {
                        _this.increaseZoom(10);
                    }
                    break;
            }
        });

    },

    destroy: function () {
        $(this.div).remove();
        this.off();
        this.rendered = false;
        $("html").unbind(".genomeViewer");
        $("body").unbind(".genomeViewer");
        delete this;
    },
    getChromosomes: function () {
        var saveChromosomes = function (chromsomeList) {
            var chromosomes = {};
            for (var i = 0; i < chromsomeList.length; i++) {
                var chromosome = chromsomeList[i];
                chromosomes[chromosome.name] = chromosome;
            }
            return chromosomes;
        }

        var chromosomes;
        if (typeof this.chromosomeList !== 'undefined') {
            chromosomes = saveChromosomes(this.chromosomeList);
        } else {
            CellBaseManager.get({
                species: this.species,
                category: 'genomic',
                subCategory: 'chromosome',
                resource: 'all',
                async: false,
                success: function (data) {
                    chromosomes = saveChromosomes(data.response.result.chromosomes);
                },
                error: function (data) {
                    console.log('Could not get chromosome list');
                }
            });
        }
        return chromosomes;
    },
    /**/
    /*Components*/
    /**/

    _createNavigationBar: function (targetId) {
        var _this = this;

        if (!$.isFunction(this.quickSearchResultFn)) {
            this.quickSearchResultFn = function (query) {
                var results = [];
                var speciesCode = Utils.getSpeciesCode(this.species.text).substr(0, 3);

                CellBaseManager.get({
                    host: 'http://ws.bioinfo.cipf.es/cellbase/rest',
                    species: speciesCode,
                    version: 'latest',
                    category: 'feature',
                    subCategory: 'id',
                    query: query,
                    resource: 'starts_with',
                    params: {
                        of: 'json'
                    },
                    async: false,
                    success: function (data, textStatus, jqXHR) {
                        for (var i in data[0]) {
                            results.push(data[0][i].displayId);
                        }
                    }
                });
                return results;
            };
        }

        var goFeature = function (featureName) {
            if (featureName != null) {
                if (featureName.slice(0, "rs".length) == "rs" || featureName.slice(0, "AFFY_".length) == "AFFY_" || featureName.slice(0, "SNP_".length) == "SNP_" || featureName.slice(0, "VAR_".length) == "VAR_" || featureName.slice(0, "CRTAP_".length) == "CRTAP_" || featureName.slice(0, "FKBP10_".length) == "FKBP10_" || featureName.slice(0, "LEPRE1_".length) == "LEPRE1_" || featureName.slice(0, "PPIB_".length) == "PPIB_") {
                    this.openSNPListWidget(featureName);
                } else {
                    console.log(featureName);
                    CellBaseManager.get({
                        species: _this.species,
                        category: 'feature',
                        subCategory: 'gene',
                        query: featureName,
                        resource: 'info',
                        params: {
                            include: 'chromosome,start,end'
                        },
                        success: function (data) {
                            var feat = data.response[0].result[0];
                            var regionStr = feat.chromosome + ":" + feat.start + "-" + feat.end;
                            var region = new Region();
                            region.parse(regionStr);
                            region = _this._checkRegion(region);
                            _this.setMinRegion(region, _this.getSVGCanvasWidth());
                            _this.region = region;
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                        }
                    });
                }
            }
        };

        var navigationBar = new NavigationBar({
            targetId: targetId,
            availableSpecies: this.availableSpecies,
            species: this.species,
            region: this.region,
            width: this.width,
            svgCanvasWidthOffset: this.trackPanelScrollWidth + this.sidePanelWidth,
            autoRender: true,
            quickSearchResultFn: this.quickSearchResultFn,
            quickSearchDisplayKey: this.quickSearchDisplayKey,
            handlers: {
                'region:change': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth());
                    _this.trigger('region:change', event);
                },
                'zoom:change': function (event) {
                    _this.trigger('zoom:change', event);
                },
                'karyotype-button:change': function (event) {
                    if (event.selected) {
                        _this.karyotypePanel.show();
                    } else {
                        _this.karyotypePanel.hide();
                    }
                },
                'chromosome-button:change': function (event) {
                    if (event.selected) {
                        _this.chromosomePanel.show();
                    } else {
                        _this.chromosomePanel.hide();
                    }
                },
                'region-button:change': function (event) {
                    if (event.selected) {
                        _this.regionOverviewPanel.show();
                    } else {
                        _this.regionOverviewPanel.hide();
                    }
                },
                'region:move': function (event) {
                    _this.trigger('region:move', event);
                },
                'species:change': function (event) {
                    _this.trigger('species:change', event);
                    _this.setRegion(event.species.region);
                },
                'fullscreen:click': function (event) {
                    if (_this.fullscreen) {
                        $(_this.div).css({width: 'auto'});
                        Utils.cancelFullscreen();//no need to pass the dom object;
                        _this.fullscreen = false;
                    } else {
                        $(_this.div).css({width: screen.width});
                        Utils.launchFullScreen(_this.div);
                        _this.fullscreen = true;
                    }
                },
                'restoreDefaultRegion:click': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(_this.defaultRegion, _this.getSVGCanvasWidth());
                    event.region = _this.defaultRegion;
                    _this.trigger('region:change', event);
                },
                'autoHeight-button:click': function (event) {
                    _this.enableAutoHeight();
                },
                'quickSearch:select': function (event) {
                    goFeature(event.item);
                    _this.trigger('quickSearch:select', event);
                },
                'quickSearch:go': function (event) {
                    goFeature(event.item);
                }
            }
        });

        this.on('region:change', function (event) {
//            if (event.sender != navigationBar) {
            _this.navigationBar.setRegion(event.region);
//            }
            _this.zoom = _this._calculateZoomByRegion(event.region);
            _this.navigationBar.setZoom(_this.zoom);
        });
        this.on('zoom:change', function (event) {
            _this.navigationBar.setZoom(event.zoom);
            _this.region.load(_this._calculateRegionByZoom(event.zoom));
            if (event.sender != navigationBar) {
                _this.navigationBar.setRegion(_this.region);
            }
            _this.setRegion(_this.region);
        });
        this.on('region:move', function (event) {
            if (event.sender != navigationBar) {
                _this.navigationBar.moveRegion(event.region);
            }
        });
        this.on('width:change', function (event) {
            _this.navigationBar.setWidth(event.width);
        });

        navigationBar.draw();

        return navigationBar;
    },

    _drawKaryotypePanel: function (targetId) {
        var _this = this;
        karyotypePanel = new KaryotypePanel({
            targetId: targetId,
            width: this.width - this.sidePanelWidth,
            height: 125,
            species: this.species,
            title: 'Karyotype',
            collapsed: this.karyotypePanelConfig.collapsed,
            collapsible: this.karyotypePanelConfig.collapsible,
            region: this.region,
            autoRender: true,
            handlers: {
                'region:change': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth());
                    _this.trigger('region:change', event);
                }
            }
        });

        this.on('region:change region:move', function (event) {
            if (event.sender != karyotypePanel) {
                karyotypePanel.setRegion(event.region);
            }
        });

        this.on('width:change', function (event) {
            karyotypePanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            karyotypePanel.setSpecies(event.species);
        });

        karyotypePanel.draw();

        return karyotypePanel;
    },

    _drawChromosomePanel: function (targetId) {
        var _this = this;


        var chromosomePanel = new ChromosomePanel({
            targetId: targetId,
            autoRender: true,
            width: this.width - this.sidePanelWidth,
            height: 65,
            species: this.species,
            title: 'Chromosome',
            collapsed: this.chromosomePanelConfig.collapsed,
            collapsible: this.chromosomePanelConfig.collapsible,
            region: this.region,
            handlers: {
                'region:change': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.trigger('region:change', event);
                }
            }
        });

        this.on('region:change region:move', function (event) {
            if (event.sender != chromosomePanel) {
                chromosomePanel.setRegion(event.region);
            }
        });

        this.on('width:change', function (event) {
            chromosomePanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            chromosomePanel.setSpecies(event.species);
        });

        chromosomePanel.draw();

        return chromosomePanel;
    },

    _createRegionOverviewPanel: function (targetId) {
        var _this = this;
        var trackListPanel = new TrackListPanel({
            targetId: targetId,
            autoRender: true,
            width: this.width - this.sidePanelWidth,
            zoomMultiplier: this.overviewZoomMultiplier,
            title: 'Region overview',
            showRegionOverviewBox: true,
            collapsible: this.RegionPanelConfig.collapsible,
            region: this.region,
            handlers: {
                'region:change': function (event) {
                    event.sender = {};
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth())
                    _this.trigger('region:change', event);
                },
                'region:move': function (event) {
                    _this.trigger('region:move', event);
                },
                'tracks:ready': function () {
                    _this.checkTrackListReady();
                }
            }
        });

        this.on('region:change', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.setRegion(event.region);
            }
        });

        this.on('region:move', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.moveRegion(event);
            }
        });

        this.on('width:change', function (event) {
            trackListPanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            trackListPanel.setSpecies(event.species);
        });

        return  trackListPanel;
    },

    _createTrackListPanel: function (targetId) {
        var _this = this;
        var trackListPanel = new TrackListPanel({
            targetId: targetId,
            autoRender: true,
            width: this.width - this.sidePanelWidth,
            title: this.trackListTitle,
            region: this.region,
            handlers: {
                'region:change': function (event) {
                    event.sender = {};
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth());
                    _this.trigger('region:change', event);
                },
                'region:move': function (event) {
                    _this.trigger('region:move', event);
                },
                'tracks:ready': function () {
                    _this.checkTrackListReady();
                }
            }
        });

        this.on('feature:highlight', function (event) {
            trackListPanel.highlight(event);
        });

        this.on('region:change', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.setRegion(event.region);
            }
        });

        this.on('region:move', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.moveRegion(event);
            }
        });
        this.on('width:change', function (event) {
            trackListPanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            trackListPanel.setSpecies(event.species);
        });

        return  trackListPanel;
    },

    _createStatusBar: function (targetId) {
        var _this = this;
        var statusBar = new StatusBar({
            targetId: targetId,
            autoRender: true,
            region: this.region,
            width: this.width,
            version: this.version
        });

        this.trackListPanel.on('mousePosition:change', function (event) {
            statusBar.setMousePosition(event);
        });
        this.on('region:change', function (event) {
            statusBar.setRegion(event);
        });

        return  statusBar;
    },

    checkTrackListReady: function () {
        var _this = this;
        var checkAllTrackListStatus = function (status) {
            if (_this.regionOverviewPanel && _this.regionOverviewPanel.status != status) {
                return false;
            }
            if (_this.trackListPanel.status != status) {
                return false;
            }
            return true;
        };
        if (checkAllTrackListStatus('ready')) {
//            console.log('-------------all tracklist ready')
            _this.trigger('tracks:ready', {sender: _this});
        }
//        var checkStatus = function () {
//            if (checkAllTrackStatus('ready')) {
//                _this.trigger('tracks:ready', {sender: _this});
//            } else {
//                setTimeout(checkStatus, 100);
//            }
//        };
//        setTimeout(checkStatus, 10);
    },

    getRightSidePanelId: function () {
        return $(this.rightSidebarDiv).attr('id');
    },
    getLeftSidePanelId: function () {
        return $(this.leftSidebarDiv).attr('id');
    },
    getNavigationPanelId: function () {
        return $(this.navigationbarDiv).attr('id');
    },
    getStatusPanelId: function () {
        return $(this.statusbarDiv).attr('id');
    },
    setNavigationBar: function (navigationBar) {
        this.navigationBar = navigationBar;
        var config = {
            availableSpecies: this.availableSpecies,
            species: this.species,
            region: this.region,
            width: this.width,
            svgCanvasWidthOffset: this.trackPanelScrollWidth + this.sidePanelWidth
        };
        _.extend(this.navigationBar, config);
        navigationBar.render(this.getNavigationPanelId());
    },
    _setWidth: function (width) {
        this.width = width;
        this.trigger('width:change', {width: this.width, sender: this});
    },
    setWidth: function (width) {
        $(this.div).width(width);
        this._setWidth(width);
    },
    getSVGCanvasWidth: function () {
        return this.width - this.trackPanelScrollWidth - this.sidePanelWidth;
    },
    _setRegion: function (region) {
        //update internal parameters
        this.region.load(region);
    },
    setRegion: function (region) {
        this.region.load(region);
        this.setMinRegion(this.region, this.getSVGCanvasWidth());
        this.trigger('region:change', {region: this.region, sender: this});
    },
    _checkRegion: function (newRegion) {
        var newChr = this.chromosomes[newRegion.chromosome];
        if (newRegion.chromosome !== this.region.chromosome) {
            newRegion.start = Math.round(newChr.size / 2);
            newRegion.end = Math.round(newChr.size / 2);
        }
        return newRegion;
    },
    setMinRegion: function (region, width) {
        var minLength = Math.floor(width / 10);
        if (region.length() < minLength) {
            var centerPosition = region.center();
            var aux = Math.ceil((minLength / 2) - 1);
            region.start = Math.floor(centerPosition - aux);
            region.end = Math.floor(centerPosition + aux);
        }
    },
    setZoom: function (zoom) {
        this.zoom = zoom;
        this.zoom = Math.min(100, this.zoom);
        this.zoom = Math.max(0, this.zoom);
        this.trigger('zoom:change', {zoom: this.zoom, sender: this});
    },
    increaseZoom: function (zoomToIncrease) {
        this.zoom += zoomToIncrease;
        this.setZoom(this.zoom);
    },
    _calculateRegionByZoom: function (zoom) {
        // mrl = minimum region length
        // zlm = zoom level multiplier

        // mrl * zlm ^ 100 = chr.size
        // zlm = (chr.size/mrl)^(1/100)
        // zlm = (chr.size/mrl)^0.01

        var minNtPixels = 10; // 10 is the minimum pixels per nt
        var chr = this.chromosomes[this.region.chromosome];
        var minRegionLength = this.getSVGCanvasWidth() / minNtPixels;
        var zoomLevelMultiplier = Math.pow(chr.size / minRegionLength, 0.01); // 0.01 = 1/100  100 zoom levels

//      regionLength = mrl * (Math.pow(zlm,ZOOM))
        var regionLength = minRegionLength * (Math.pow(zoomLevelMultiplier, 100 - zoom)); // 100 - zoom to change direction

        var centerPosition = this.region.center();
        var aux = Math.ceil((regionLength / 2) - 1);
        var start = Math.floor(centerPosition - aux);
        var end = Math.floor(centerPosition + aux);

        return {start: start, end: end};
    },
    _calculateZoomByRegion: function (region) {
        var minNtPixels = 10; // 10 is the minimum pixels per nt
        var chr = this.chromosomes[this.region.chromosome];
        var minRegionLength = this.getSVGCanvasWidth() / minNtPixels;
        var zoomLevelMultiplier = Math.pow(chr.size / minRegionLength, 0.01); // 0.01 = 1/100  100 zoom levels

        var regionLength = region.length();

//      zoom = Math.log(REGIONLENGTH/mrl) / Math.log(zlm);
        var zoom = Math.log(regionLength / minRegionLength) / Math.log(zoomLevelMultiplier);
        return 100 - zoom;
    },
    move: function (disp) {
//        var pixelBase = (this.width-this.svgCanvasWidthOffset) / this.region.length();
//        var disp = Math.round((disp*10) / pixelBase);
        this.region.start += disp;
        this.region.end += disp;
        this.trigger('region:move', {region: this.region, disp: -disp, sender: this});
    },
    mark: function (args) {
        var attrName = args.attrName || 'feature_id';
        var cssClass = args.class || 'feature-emph';
        if ('attrValues' in args) {
            args.attrValues = ($.isArray(args.attrValues)) ? args.attrValues : [args.attrValues];
            for (var key in args.attrValues) {
                $('rect[' + attrName + '~=' + args.attrValues[key] + ']').attr('class', cssClass);
            }

        }
    },
    unmark: function (args) {
        var attrName = args.attrName || 'feature_id';
        if ('attrValues' in args) {
            args.attrValues = ($.isArray(args.attrValues)) ? args.attrValues : [args.attrValues];
            for (var key in args.attrValues) {
                $('rect[' + attrName + '~=' + args.attrValues[key] + ']').attr('class', '');
            }

        }
    },

    highlight: function (args) {
        this.trigger('feature:highlight', args);
    },

    enableAutoHeight: function () {
        this.trackListPanel.enableAutoHeight();
        this.regionOverviewPanel.enableAutoHeight();
    },
    updateHeight: function () {
        this.trackListPanel.updateHeight();
        this.regionOverviewPanel.updateHeight();
    },


    setSpeciesVisible: function (bool) {
        this.navigationBar.setSpeciesVisible(bool);
    },

    setChromosomesVisible: function (bool) {
        this.navigationBar.setChromosomeMenuVisible(bool);
    },

    setKaryotypePanelVisible: function (bool) {
        this.karyotypePanel.setVisible(bool);
        this.navigationBar.setVisible({'karyotype': bool});
    },

    setChromosomePanelVisible: function (bool) {
        this.chromosomePanel.setVisible(bool);
        this.navigationBar.setVisible({'chromosome': bool});
    },

    setRegionOverviewPanelVisible: function (bool) {
        this.regionOverviewPanel.setVisible(bool);
        this.navigationBar.setVisible({'region': bool});
    },
    setRegionTextBoxVisible: function (bool) {
        this.navigationBar.setRegionTextBoxVisible(bool);
    },
    setSearchVisible: function (bool) {
        this.navigationBar.setSearchVisible(bool);
    },
    setFullScreenVisible: function (bool) {
        this.navigationBar.setFullScreenButtonVisible(bool);
    },

    /*Track management*/
    addOverviewTrack: function (trackData, args) {
        this.regionOverviewPanel.addTrack(trackData, args);
    },

    addTrack: function (trackData, args) {
        this.trackListPanel.addTrack(trackData, args);
    },

    getTrackSvgById: function (trackId) {
        return this.trackListPanel.getTrackSvgById(trackId);
    },

    removeTrack: function (trackId) {
        return this.trackListPanel.removeTrack(trackId);
    },

    restoreTrack: function (trackSvg, index) {
        return this.trackListPanel.restoreTrack(trackSvg, index);
    },

    setTrackIndex: function (trackId, newIndex) {
        return this.trackListPanel.setTrackIndex(trackId, newIndex);
    },

    scrollToTrack: function (trackId) {
        return this.trackListPanel.scrollToTrack(trackId);
    },

    showTrack: function (trackId) {
        this.trackListPanel._showTrack(trackId);
    },

    hideTrack: function (trackId) {
        this.trackListPanel._hideTrack(trackId);
    },

    checkRenderedTrack: function (trackId) {
        if (this.trackListPanel.swapHash[trackId]) {
            return true;
        }
        return false;
    }
};


