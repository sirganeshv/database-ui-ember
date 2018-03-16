import Route from '@ember/routing/route';

export default Route.extend({
  sortProperties: ['acc_no:desc'],
  model() {
    return this.controllerFor('index').get('tableData');
  },
  actions : {
    getData() {
      this.refresh();
    }
  }
});
