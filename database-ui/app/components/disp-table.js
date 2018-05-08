import Component from '@ember/component';

//var sortAscending = true
//var sortProperties = 'a'
export default Component.extend({
  actions: {
    sortAscending : true,
    sortProperties : 'a',
    sortBy: function(property) {
      if(property === this.get('sortProperties')) {
        this.toggleProperty('sortAscending');
      }
      else {
        this.set('sortAscending',true)
      }
      this.set('sortProperties',property);
      this.sendAction('sortBy',property,this.get('sortAscending'));
    }
  }
});
