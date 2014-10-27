/**
 * Created by jag on 20/10/2014.
 */

function EvaBeacon(args) {
    _.extend(this, Backbone.Events);

    _.extend(this, args);
    this.rendered = false;
    this.render();
}

EvaBeacon.prototype = {
    render: function () {
        var _this =  this;

        this.targetDiv = (this.target instanceof HTMLElement) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVABeacon: target ' + this.target + ' not found');
            return;
        }

        this.targetDiv.innerHTML = _this._getBeaconForm();

        this.projectDiv = document.querySelector("#project-list-beacon");
        this.chrSelectDiv = document.querySelector("#chromosome-select-beacon");
        this.showResultDiv =  document.querySelector("#beacon-result-show");
        this.beaconForm = document.querySelector('#beacon-form');
        EvaManager.get({
            category: 'meta/studies',
            resource: 'all',
            success: function (response) {
                try {
                    project_ids = response.response[0].result;
                    _this._createProjectElement(project_ids);
                } catch (e) {
                    console.log(e);
                }

            }
        });

        var chrSelectList = document.createElement("select");
        chrSelectList.setAttribute("id", "beacon-chrom");
        chrSelectList.setAttribute("required" ,"");
        this.chrSelectDiv.appendChild(chrSelectList);
        for (var i = 1; i < 25; i++) {
            var option = document.createElement("option");
            if(i == 23){
                option.setAttribute("value", i);
                option.text = 'Chr X';
            }
            else if(i == 24){
                option.setAttribute("value", i);
                option.text = 'Chr Y';
            }else{
                option.setAttribute("value", i);
                option.text = 'Chr '+i;
            }

            chrSelectList.appendChild(option);
        }

        this.beaconForm.addEventListener('submit', function(eve) {
            eve.preventDefault();
            var data= {};
            _this.chromEl = document.querySelector('#beacon-chrom');
            _this.coordinateEl = document.querySelector('#beacon-coordinate');
            _this.alleleEl = document.querySelector('#beacon-allele');
            _this.formatTypeEl = document.querySelectorAll('input[type="radio"][name="beacon-formatType"]');
            _this.projectEl =  document.querySelector('#beacon-project');

            for( key in _this.formatTypeEl ){
                if(_this.formatTypeEl[key].checked === true){
                    data.formatType = _this.formatTypeEl[key].value;
                }
            }
            var region =  _this.chromEl.value+':'+ _this.coordinateEl.value+'::'+_this.alleleEl.value;
            var params = {studies:_this.projectEl.value}
            EvaManager.get({
                category: 'variants',
                resource: 'exists',
                query:region,
                params:params,
                success: function (response) {
                    try {
                        data.result = response.response[0].result;
                    } catch (e) {
                        console.log(e);
                    }
                    _this._renderResultData(data, region,_this.projectEl.value);
                }
            });

        }, true);
    },
    _createProjectElement: function(data){
        var _this = this;
        var projectList = document.createElement("select");
        projectList.setAttribute("id", "beacon-project");
        _this.projectDiv.appendChild(projectList);
        var projectOption = document.createElement("option");
        projectOption.setAttribute("value", '');
        projectOption.text ='';
        projectList.appendChild(projectOption);

        for (var i = 0; i < data.length; i++) {
            projectOption = document.createElement("option");
            var studyId = this._getStudyId(data[i].id);
            if(studyId){
                projectOption.setAttribute("value", data[i].id);
                projectOption.text = data[i].id+' - '+data[i].name;
                projectList.appendChild(projectOption);
            }

        }
    },
    _getStudyId: function(data){
        var _this = this;
        for (var i = 0; i < projects.length; i++) {
            if (projects[i].studyId === data) {
                return projects[i].studyName;
            }
        }
    },
    _renderResultData: function(data,region, studyId){
        var _this = this;
        var resultData;
        var cssClass;

        if(data.result == 'true'){
            cssClass = 'valid';
        }else{
            cssClass = 'invalid';
        }
        var _region = region.split(":");
        if(data.formatType == 'json'){
            resultData = {query:{chrom:_region[0],pos:_region[1],allele:_region[2]},exist:data.result};
            resultData ='<h4>Result:</h4>'+ JSON.stringify(resultData);
        }else{
            resultData = '<h4>Result:</h4><table  class="table table-bordered">' +
                '<tr><td>Project</td><td>'+this._getStudyId(studyId)+'</td></tr>'+
                '<tr><td>Chromosome</td><td>'+_region[0]+'</td></tr>'+
                '<tr><td>Coordinate</td><td>'+_region[1]+'</td></tr>'+
                '<tr><td>Allele</td><td>'+_region[3]+'</td></tr>'+
                '<tr><td>Exist</td><td class="'+cssClass+'">'+data.result+'</td></tr>'+
                '</table>'
        }

        _this.showResultDiv.innerHTML = "";
        var resultElDiv =  document.createElement("div");
        resultElDiv.innerHTML = resultData;
        _this.showResultDiv.appendChild(resultElDiv);

        var refreshBtnDiv =  document.querySelector("#refresh-button");
        refreshBtnDiv.innerHTML = "";
        var refreshBtnEl =  document.createElement("button");
        refreshBtnEl.type = "submit";
        refreshBtnEl.textContent = "Reset";
        refreshBtnEl.className = "btn btn-primary eva-button";
        refreshBtnEl.id = "refresh";
        refreshBtnDiv.appendChild(refreshBtnEl);

        var refresh = document.querySelector('#refresh');
        refresh.addEventListener('click', function(eve) {
            eve.preventDefault();
            _this.showResultDiv.innerHTML = "";
            _this.beaconForm.reset();
        }, true);

    },
    _getBeaconForm:function(){
        var form ='<div class="row" class="eva">'+
                        '<div class="col-md-12">'+
                            '<form id="beacon-form">'+
                               '<div class="row">'+
                                    '<div class="col-md-2 form-group"><p>Project</p></div>'+
                                    '<div class="col-md-2 form-group input-group-sm"><div id="project-list-beacon"></div></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"><p>Chromosome</p></div>'+
                                    '<div class="col-md-2 form-group input-group-sm"><div id="chromosome-select-beacon"></div></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"><p>Coordinate</p></div>'+
                                    '<div class="col-md-2 form-group input-group-sm"><input class="form-control" id="beacon-coordinate"  name=coordinate type="text" required/></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group input-group-sm"    "><p>Allele</p></div>'+
                                    '<div class="col-md-2 form-group input-group-sm"><input class="form-control" id="beacon-allele" name=allele type="text"/></div>'+
                                '</div>'+
                                '<h5>Format Type</h5>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"><p>Text</p></div>'+
                                    '<div class="col-md-4 form-group input-group-sm"> <input type="radio"  name="beacon-formatType" value="text" checked="true"> </div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"><p>JSON</p></div>'+
                                    '<div class="col-md-4 form-group input-group-sm"> <input type="radio"  name="beacon-formatType" value="json"> </div>'+
                                    '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-4 form-group"><span id="refresh-button"></span> <input id="submit" value="Submit" class="btn btn-primary eva-button" type="submit"></div>'+
                                '</div>'+
                            '</form>'+
                        '</div>'+
                    '</div>'+
                    '<div class="row">'+
                        '<div class="col-md-5">'+
                            '<div id="beacon-result-show" class="resultShow"></div><br />'+
                        '</div>'+
                   '</div>'
        return form;
    }
}








