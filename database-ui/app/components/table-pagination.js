import Ember from 'ember';
import DS from 'ember-data';

var n = 0;

export default Ember.Component.extend({
  tagName: 'section',
  page: 1,
  paginateBy: 10,
  pageCount: 0,
  isPresent: false,
  startPageNumber: 1,
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
      var that  = this;
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
          n = parseInt(resp);
          var c = parseInt(that.get('paginateBy'));
          var r = Math.floor(n/c);
          if(n % c > 0)
            r += 1;
          that.set('pageCount',parseInt(r));
      }).catch(function(error){
        alert(error);
      });
    }
  }),

  pageNumbers: Ember.computed('pageCount','startPageNumber', function(){
    var start = this.get('startPageNumber');
    var total = this.get('pageCount');
    var diff = total - start;
    var size;
    if(diff < 9 && start < total)
      size = diff;
    else {
      size = 10;
    }
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
    nextClicked() {
      if(this.get('page') + 1 <= this.get('pageCount')) {
        this.set('page', this.get('page') + 1);
      }
      if((this.get('page') % 10) == 1)
        this.set('startPageNumber',this.get('startPageNumber')+10);

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
    }
  }
});
