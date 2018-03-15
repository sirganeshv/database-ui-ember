import Component from '@ember/component';

export default Component.extend({
  sortProperties: ['customer_name'],
  sortedModel: Ember.computed.sort("data.row", "sortProperties"),
  actions: {
    sortBy: function(property) {
      alert(property);
      //alert(this.get('data.row'));
      alert(this.get('sortedModel'));
      alert(this.get('sortProperties'));
      this.set('sortProperties',[property]);
      this.set('sortAscending',true);
      alert(this.get('sortedModel'));
      alert(this.get('sortProperties'));
      //this.sendAction('sortBy',property,ascending);
      //this.sendAction('transitionToRoute', "/index", property,ascending );
      //this.sendAction('action',property,ascending);
    }
  },
  getvalue(rowData) {
    alert(JSON.stringify(rowData));
  }
});
