import Controller from '@ember/controller';

export default Controller.extend({
  queryParams: ['object'],
  object: null,
  obj: Ember.computed("object", function() {
    return JSON.parse(this.get('object'));
  }),
  actions : {
    back() {
      //alert(JSON.parse(this.get('object')).col);
      this.transitionToRoute("index");
    }
  }
});
