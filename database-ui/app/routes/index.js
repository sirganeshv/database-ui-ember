import Route from '@ember/routing/route';

export default Route.extend({
  model() {
      //return this.get('store').findAll('post');
      /*return [{
        title: 'Grand Old Mansion',
        body: 'it is a good mansion',
        rate: 4300
      }, {
        title: 'Urban Living',
        body: 'Not bad',
        rate: 4500
      }, {
        title: 'Downtown Charm',
        body: 'Ok ok',
        rate: 5000
      }];*/
    var data = this.controllerFor('index').get('tableData');
    return this.controllerFor('index').get('tableData');
  },
  actions : {
    tesst() {
      var data =this.controllerFor('index').get('tableData');
      this.refresh();
    }
  }
});
