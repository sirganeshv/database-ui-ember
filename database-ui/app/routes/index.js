import Route from '@ember/routing/route';

export default Route.extend({
  sortProperties: ['acc_no:desc'],
  sortedModel: Ember.computed.sort("model.row", "sortProperties"),
  model() {
    //alert(this.get('sortedModel'));
    return this.controllerFor('index').get('tableData');
  },
  actions : {
    getData() {
      this.refresh();
      sortedModel: Ember.computed.sort("model.row", "sortProperties");
    }
  }
});
