import Component from '@ember/component';

//var sortAscending = true
//var sortProperties = 'a'
export default Component.extend({
  actions: {
    sortAscending : true,
    sortProperties : 'a',
    sortBy: function(property) {
      alert(property);
      alert(this.get('sortProperties'));
      if(property === this.get('sortProperties')) {
        alert('dfs');
        this.toggleProperty('sortAscending');
      }
      else {
        this.set('sortAscending',true)
      }
      //alert(this.get('sortAscending'),sortAscending);
      this.set('sortProperties',property);
      this.sendAction('sortBy',property,this.get('sortAscending'));
      //this.sendAction('sortBy',property);
    }
  }
});
