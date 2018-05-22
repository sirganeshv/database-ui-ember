import Ember from 'ember';
import DS from 'ember-data';
//var ProgressBar = require('progressbar.js');
//import progressbar from 'progressbar';
//import {worker} from 'ember-multithread';
import {
  next,
  cancel,
  later,
  scheduleOnce,
  once,
  throttle,
  debounce
} from '@ember/runloop';
var  progress = 0.0;
var n = 0;
//var w;
var i = 0;
var myTimer ;
var _progress = document.getElementById('_progress');
var export_finished = false;
function checkProgress() {
  //if(flag === false)
  Ember.$.ajax({
    url: "/getProgress",
    type: "POST",
    data: {
    },success : function(resp){
        progress = (resp);
        document.getElementById('_progress').style.width = Math.ceil(resp* 100) + '%';
        if(document.getElementById('_progress').style.width === '100%') {
          document.getElementById('_progress').style.background = "#ffffff";
          document.getElementById('_progress').innerHTML = "Finishing.....";
          document.getElementById('_progress').style.width = '100%';
        }
        else {
          document.getElementById('_progress').innerHTML = "";
          document.getElementById('_progress').style.background = "#DEDEDE";
        }
    },error : function(error){
      alert(error);
    }
  });
  if(export_finished === true) {
    clearInterval(myTimer);
  }
};
/*function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
};*/
export default Ember.Component.extend({
  tagName: 'section',
  page: 1,
  paginateBy: 10,
  pageCount: 0,
  isPresent: false,
  startPageNumber: 1,
  start : 0,
  end : 0,
  isExporting: false,
  loading: false,

  paginatedItems: Ember.computed('items', 'page','sortProperties','filterValue', function(){
    this.set('val',this.get('filterValue'));
    var i = (parseInt(this.get('page')) - 1) * parseInt(this.get('paginateBy'));
    var j = i + parseInt(this.get('paginateBy'));
    if('items' == null)
      return false;
    else {
      var that  = this;
      var result = new Ember.RSVP.Promise(function(resolve, reject) {
        Ember.$.ajax({
          url: "/get_page",
          //contentType: "application/json; charset=utf-8",
          //Content-Type : "application/json",
          type: "POST",
          data: {
            "table_name" : that.get('table_name'),
            "prop" : that.get('prop'),
            'isAscending' : that.get('sortAscending'),
            "filterCol" : that.get('filterCol'),
            "filterValue" : that.get('filterValue'),
            "start" : i,
            "stop" : j,
          },
          success: function(resp){
            if(JSON.parse(resp).row == null) {
              //alert(JSON.parse(resp).row);
              that.set('isPresent',false);
            }
            else {
              resolve(JSON.parse(resp).row);
              that.set('isPresent',true);
            }
          },
          error: function(reason) {
            reject(reason);
          }
        });
      });
    }
    return DS.PromiseObject.create({ promise: result });
  }),

  isPaginated:Ember.computed('items','paginatedItems','pageCount', function(){
    if((this.get('page') > this.get('numberOfPages'))) {
      this.set('page',1);
    }
    return ((this.get('items')!==null) && (this.get('items')!==undefined) && (this.get('items')!==""));
  }),

  numberOfPages: Ember.computed('items','paginateBy','filterValue',function(){
    table_name = this.get('table_name');
    if(table_name !== null && table_name !== undefined && table_name !== "" && this.get('items') !== null) {
      this.set('page',1);
      this.set('startPageNumber',1);
      var that  = this;
      this.set('loading',true);
      Ember.$.ajax({
        url: "/no_of_records",
        type: "POST",
        data: {
          "table_name" : that.get('table_name'),
          "prop" : that.get('prop'),
          "filterCol" : that.get('filterCol'),
          "filterValue" : that.get('filterValue'),
        }
      }).then(function(resp){
          //alert(resp);
          n = parseInt(resp);
          var c = parseInt(that.get('paginateBy'));
          var r = Math.floor(n/c);
          if(n % c > 0)
            r += 1;
          that.set('pageCount',parseInt(r));
          that.set('loading',false);
      }).catch(function(error){
        alert(error);
      });
    }
  }),

  pageNumbers: Ember.computed('pageCount','startPageNumber', function(){
    //var num = this.get('pageCount');
    //alert(this.get('startPageNumber'));
    var start = this.get('startPageNumber');
    var total = this.get('pageCount');
    var diff = total - start;
    //alert("start = "+start+"  total = "+total);
    var size;
    if(diff < 9 && start <= total)
      size = diff;
    else {
      size = 10;
    }
    //alert(size);
    var n = Array(size);
    var j = 0;
    for(var i = start;j < 10 && i <= total;i++) {
      n[j] = i;
      j++;
    }
    return n;
  }),

  showNext: Ember.computed('page','pageCount', function(){
    return (this.get('page') < this.get('pageCount'));
  }),

  showPrevious: Ember.computed('page','pageCount', function(){
    return (this.get('page') > 1);
  }),

  nextText: 'Next page',
  previousText: 'Previous page',

  actions: {

    export() {
      //var _progress = document.getElementById('_progress');
      //_progress.style.width = '0%';
      var start = parseInt(prompt("Enter start eventID"));
      while(isNaN(start)) {
        start = parseInt(prompt("Enter start eventID as number"));
      }
      this.set('start',start);
      var end = parseInt(prompt("From "+ this.get('start') +" to ?"));
      while(isNaN(end) || start >= end) {
        end = parseInt(prompt("From "+ this.get('start') +" to ?(number)"));
      }
      this.set('end',end);
      var i = (parseInt(this.get('page')) - 1) * parseInt(this.get('paginateBy'));
      var j = i + parseInt(this.get('paginateBy'));
      var isConfirmed = confirm("Do you want to export from event ID "+this.get('start')+" to "+this.get('end'));
      if(isConfirmed) {
        var that  = this;
        this.set('isExporting',true);
        progress = 0.0;
        export_finished = false;
        Ember.$.ajax({
          url: "/exportPdf",
          type: "POST",
          data: {
            "table_name" : that.get('table_name'),
            "prop" : that.get('prop'),
            'isAscending' : that.get('sortAscending'),
            "start" : that.get('start'),
            "stop" : that.get('end'),
            "filterCol" : that.get('filterCol'),
            "filterValue" : that.get('filterValue'),
          },success : function(resp){
              alert(resp);
              export_finished = true;
              that.set('isExporting',false);
          },error : function(error){
            alert(error);
          }
        });
        myTimer = setInterval(function(){checkProgress() },2);
      }
    },


    exportEmail() {
      var start = parseInt(prompt("Enter start eventID"));
      while(isNaN(start)) {
        start = parseInt(prompt("Enter start eventID as number"));
      }
      this.set('start',start);
      var end = parseInt(prompt("From "+ this.get('start') +" to ?"));
      while(isNaN(end) || start >= end) {
        end = parseInt(prompt("From "+ this.get('start') +" to ?(number)"));
      }
      this.set('end',end);
      var i = (parseInt(this.get('page')) - 1) * parseInt(this.get('paginateBy'));
      var j = i + parseInt(this.get('paginateBy'));
      var receiverMailID = prompt("Enter your Email ID");
      var isConfirmed = confirm("Do you want to export from event ID "+this.get('start')+" to "+this.get('end'));
      if(isConfirmed) {
        var that  = this;
        this.set('isExporting',true);
        progress = 0.0;
        export_finished = false;
        Ember.$.ajax({
          url: "/exportEmail",
          type: "POST",
          data: {
            "table_name" : that.get('table_name'),
            "prop" : that.get('prop'),
            'isAscending' : that.get('sortAscending'),
            "start" : that.get('start'),
            "stop" : that.get('end'),
            "filterCol" : that.get('filterCol'),
            "filterValue" : that.get('filterValue'),
            "receiverMailID" : receiverMailID,
          },success : function(resp){
              alert(resp);
              export_finished = true;
              that.set('isExporting',false);
          },error : function(error){
            alert(error);
          }
        });
        myTimer = setInterval(function(){checkProgress() },2);
      }
    },


    nextClicked() {
      if(this.get('page') + 1 <= this.get('pageCount')) {
        this.set('page', this.get('page') + 1);
      }
      //alert("page num is "+this.get('page'));
      if((this.get('page') % 10) == 1)
        this.set('startPageNumber',this.get('startPageNumber')+10);
      //alert(this.get('startPageNumber'));

    },

    previousClicked() {
      if(this.get('page') > 0) {
        this.set('page', this.get('page') - 1);
      }
      if((this.get('page') % 10) == 0)
        this.set('startPageNumber',this.get('startPageNumber') - 10);
    },

    pageClicked(pageNumber){
      this.set('page', pageNumber);
    },


  }
});
