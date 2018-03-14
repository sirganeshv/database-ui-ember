import Component from '@ember/component';

export default Component.extend({
  sortProperties: ['acc_no:desc'],
  sortedModel: Ember.computed.sort("model.row", "sortProperties"),
  actions: {
    sort: function(property,ascending) {
      alert(property);
      this.set('sortProperties',[property]);
      //this.sendAction('sortBy',property,ascending);
      //this.sendAction('transitionToRoute', "/index", property,ascending );
      //this.sendAction('sortBy',property,ascending);
      this.sendAction('action',property,ascending);
    }
  }
});
