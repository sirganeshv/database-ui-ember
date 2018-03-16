import Component from '@ember/component';


export default Component.extend({

  sortProperties: Ember.computed("data.col", function(){
    if(this.get('data.col') == null) {
      return [''];
    }
    else {
      return [this.get('data.col')[0]];
    }
  }),

  sortAscending: true,
  sortedModel: Ember.computed.sort("data.row", "sortProperties"),
  theFilter: "",

  isDisplayed: Ember.computed("data.row", function(){
    if(this.get('data.row') == null)
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
      return this.get("sortedModel").filter((function(_this) {
      return function(theObject,index,enumerable) {
        if (_this.get("theFilter")) {
          return _this.checkFilterMatch(theObject, _this.get("theFilter"));
        } else {
          return true;
        }
      };
    })(this));
  }).property("theFilter", "sortProperties","sortedModel"),

  actions: {
    sortBy: function(property) {
      if(this.get('sortProperties')[0]==property) {
        this.toggleProperty('sortAscending');
        let sortOrd = this.get('sortOrde') ? 'asc' : 'desc' ;
        this.set('sortProperties',[`${property}:${sortOrd}`]);
      }
      else {
        this.set('sortAscending',true);
        this.set('sortProperties',[property]);
      }
    }
  }
});
