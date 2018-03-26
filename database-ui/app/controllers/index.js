import Controller from '@ember/controller';
var data = ' ';
export default Controller.extend({
  ajax: Ember.inject.service(),

  sortProperties: Ember.computed("model.col", function(){
    if(this.get('model.col') == null) {
      return [''];
    }
    else {
      return [this.get('model.col')[0]];
    }
  }),
  prop: Ember.computed("model.col", function(){
    if(this.get('model.col') == null) {
      return [''];
    }
    else {
      return [this.get('model.col')[0]];
    }
  }),
  sortAscending: true,
  sortedModel: Ember.computed.sort("model.row", "sortProperties"),
  theFilter: "",

  isDisplayed: Ember.computed("model.row", function(){
    if(this.get('model.row') == null)
      return false;
    else return true;
  }),

  checkFilterMatch: function(theObject, str) {
    var field, match;
    match = false;
    for (field in theObject) {
      if (theObject[field].toString().slice(0, str.length) === str) {
        match = true;
      }
    }
    return match;
  },

  filterData: (function() {
      return this.get("model.row").filter((function(_this) {
      return function(theObject,index,enumerable) {
        if (_this.get("model.row")) {
          return _this.checkFilterMatch(theObject, _this.get("theFilter"));
        } else {
          return true;
        }
      };
    })(this));
  }).property("theFilter", "sortProperties","sortedModel"),


  actions : {
    display() {
      var table_name = this.get('table_name');
      //alert('hello');
      if(table_name !== null  && table_name != undefined && table_name != '') {
        var that  = this;
        Ember.$.ajax({
          url: "/get_page ",
          type: "POST",
          //contentType: "charset=utf-8",
          //contentType: 'application/json; charset=utf-16',
          data: {
            "table_name" : this.get('table_name'),
            "start" : 0,
            "stop" : 5,
            "sortProperties" : that.get('sortProperties')
          }
        }).then(function(resp){
            //alert(that.get('table_name'));
            //alert(resp);
            var table = document.getElementById("table");
            if(table != null) {
              var rowCount = table.rows.length;
              for (var x=rowCount-1; x>0; x--) {
  					    table.deleteRow(x);
  				    }
            }
            if(resp.trim() == '') {
              that.set('message','No such table exists');
            }
            else {
              data = JSON.parse(resp);
              that.set('message','');
            }
            that.send('setData');
            that.send('getData');
        }).catch(function(error){
          alert(error);
        });
      }
    },
    setData() {
      var tableData = data;
      this.set('tableData',tableData);
    },
    sortBy: function(property) {
      if(this.get('sortProperties')[0].includes(property)) {
        this.toggleProperty('sortAscending');
        let sortOrd = this.get('sortOrder') ? 'asc' : 'desc' ;
        this.set('sortProperties',[`${property}:${sortOrd}`]);
      }
      else {
        this.set('sortAscending',true);
        this.set('sortProperties',[property]);
        //alert('fresh');
      }
      this.set('prop',property);
      this.set('sortProperties',[property]);
    }
  }
});
