import Component from '@ember/component';


export default Component.extend({
  actions: {
    sortBy: function(property) {s
      this.sendAction('sortBy',property);
    }
  }
});
