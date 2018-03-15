import Component from '@ember/component';


export default Component.extend({
  sortProperties: ['customer_name'],
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
        //alert(theObject[field].toString().slice(0, str.length).equalsIgnoreCase(str));
      //if (theObject[field].toString().slice(0, str.length).equalsIgnoreCase(str)) {
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
      this.set('sortAscending',false);
      this.set('sortProperties',[property]);
    }
  }
});
