import Component from '@ember/component';


export default Component.extend({
  sortProperties: Ember.computed("colData", function(){
    if(this.get('colData') == null) {
      return [''];
    }
    else {
      return [this.get('colData')[0]];
    }
  }),

  sortAscending: true,
  sortedModel: Ember.computed.sort("rowData", "sortProperties"),
  theFilter: "",

  isDisplayed: Ember.computed("rowData", function(){
    if(this.get('rowData') == null)
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
      return this.get("rowData").filter((function(_this) {
      return function(theObject,index,enumerable) {
        if (_this.get("rowData")) {
          return _this.checkFilterMatch(theObject, _this.get("theFilter"));
        } else {
          return true;
        }
      };
    })(this));
  }).property("theFilter", "sortProperties","sortedModel"),

  actions: {
    /*sortBy: function(property) {
      alert("hi");
      this.sendAction('sortBy',property);
    }*/
    sortBy: function(property) {
      /*alert("hellooo");
      alert(property + " "+this.get('sortProperties')[0]);
      if(this.get('sortProperties')[0].includes(property)) {
        this.toggleProperty('sortAscending');
        let sortOrd = this.get('sortOrder') ? 'asc' : 'desc' ;
        this.set('sortProperties',[`${property}:${sortOrd}`]);
        alert('old');
      }
      else {
        this.set('sortAscending',true);
        this.set('sortProperties',[property]);
        alert('fresh');
      }
      alert(JSON.stringify(this.get('sortedModel')));
    }*/
      this.sendAction('sortBy',property);
    }
  }
});
