import Component from '@ember/component';


export default Component.extend({
  actions: {
    sortBy: function(property) {
      this.sendAction('sortBy',property);
    }
  }
});
