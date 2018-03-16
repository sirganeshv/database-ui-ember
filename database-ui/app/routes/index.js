import Route from '@ember/routing/route';

export default Route.extend({
  beforeModel() {
    var that  = this;
    var tableList = '';
    Ember.$.ajax({
      url: "/get_tables",
      type: "POST",
      data: {
      }
    }).then(function(resp){
        //alert(resp);
        that.set('tableList',JSON.parse(resp));
        that.set('hel',"hello");
        alert(that.getTables());
    }).catch(function(error){
      alert(error);
    });
  },
  getTables() {
    return this.get('tableList');
  },
  model() {
    return this.controllerFor('index').get('tableData');
  },
  actions : {
    getData() {
      this.refresh();
    }
  }
});
