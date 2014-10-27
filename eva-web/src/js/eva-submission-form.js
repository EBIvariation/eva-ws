/*
 * Copyright (c) 2014 Francisco Salavert (SGL-CIPF)
 * Copyright (c) 2014 Alejandro Alemán (SGL-CIPF)
 * Copyright (c) 2014 Ignacio Medina (EBI-EMBL)
 * Copyright (c) 2014 Jag Kandasamy (EBI-EMBL)
 *
 * This file is part of EVA.
 *
 * EVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * EVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EVA. If not, see <http://www.gnu.org/licenses/>.
 */
function EvaSubmissionForm(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId("EVASubmissionForm");
    _.extend(this, args);
    this.rendered = false;
    this.render();
}

EvaSubmissionForm.prototype = {
    render: function () {
        var _this =  this;

        this.targetDiv = (this.target instanceof HTMLElement) ? this.target : document.querySelector('#' + this.target);
        if (!this.targetDiv) {
            console.log('EVASubmissionForm: target ' + this.target + ' not found');
            return;
        }

        this.targetDiv.innerHTML = _this._getSubmissionForm();

        _this.submissionForm = document.querySelector('#submission-form');
        _this.submissionForm.addEventListener('submit', function(eve) {
            eve.preventDefault();
            _this.name =  document.querySelector('#name');
            _this.email =  document.querySelector('#email');
            _this.companyName =  document.querySelector('#companyName');
            _this.webpage =  document.querySelector('#webpage');
            _this.country =  document.querySelector('#country');
            _this.acronym =  document.querySelector('#acronym');
            _this.comments =  document.querySelector('#comments');
            _this.submissionType =  document.querySelectorAll('input[name=submissionType]:checked');
            var submissionType = ''
            for(var i = 0; i < _this.submissionType.length; i++){
                submissionType += _this.submissionType[i].value + ",";
            }
            console.log(_this.name.value)
            console.log(_this.email.value)
            console.log(_this.companyName.value)
            console.log(_this.webpage.value)
            console.log(_this.country.value)
            console.log(_this.acronym.value)
            console.log(_this.comments.value)
            console.log(submissionType)

            _this.showResultDiv =  document.querySelector("#submissionResultShow");
            var resultElDiv =  document.createElement("div");
            resultElDiv.innerHTML = 'Your details has been submitted';
            _this.showResultDiv.appendChild(resultElDiv);



        }, true);


    },
    _getSubmissionForm:function(){
        var form =  '<div class="row">'+
                        '<div class="col-md-5">'+
                            '<div style="font-size:16px;" class="resultShow" id="submissionResultShow"></div><br />'+
                        '</div>'+
                    '</div>'+
                    '<div class="row">'+
                        '<div class="col-md-12">'+
                            '<form id="submission-form">'+
                                '<h4 style="margin-left:-10px;">User Details</h4>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group">Full Name</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="name"   type="text" required/></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group">Email</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="email"   type="email" required/></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group">Institute/Company Name   <span class="required"></span></div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="companyName"   type="text" /></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Webpage </div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="webpage"   type="text"/></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Country of Origin </div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="country"   type="text" required/></div>'+
                                '</div>'+
                                '<h4 style="margin-left:-10px;">Type of Submission </h4>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Genomic DNA</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input type="checkbox" name="submissionType" value="Genomic DNA"></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Exonic DNA</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input type="checkbox" name="submissionType" value="Exonic DNA"></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Transcribed RNA</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input type="checkbox" name="submissionType" value="Transcribed RNA"></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Clinical</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input type="checkbox" name="submissionType" value="Clinical"></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Unknown</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input type="checkbox" name="submissionType" value="Unknown"></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group"> Other</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input type="checkbox" name="submissionType" value="Other"></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group">Preferred Centre Acronym (subject to availability)</div>'+
                                    '<div class="col-md-4 form-group input-group-sm"><input class="form-control" id="acronym"  type="text"/></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-2 form-group">Comments</div>'+
                                    '<div class="col-md-4 form-group input-group-md"><textarea id="comments" class="form-control" rows="3"></textarea></div>'+
                                '</div>'+
                                '<div class="row">'+
                                    '<div class="col-md-4 form-group"><button class="btn btn-primary" type="submit">Submit</button></div>'+
                                '</div>'+
                            '</form>'+
                        '</div>'+
                    '</div>'
        return form;
    }
}




