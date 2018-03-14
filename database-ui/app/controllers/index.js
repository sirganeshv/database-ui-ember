import Controller from '@ember/controller';
var data = ' ';
export default Controller.extend({
  ajax: Ember.inject.service(),
  sortProperties: ['acc_no:desc'],
  sortedModel: Ember.computed.sort("model.row", "sortProperties"),
  actions : {
    display() {
      var that  = this;
      Ember.$.ajax({
        url: "/get_table",
        type: "POST",
        data: {
          "table_name" : this.get('table_name')
        }
      }).then(function(resp){
          data = JSON.parse(resp);
          var table = document.getElementById("table");
          var rowCount = table.rows.length;
          for (var x=rowCount-1; x>0; x--) {
					  table.deleteRow(x);
					}
          if(resp.trim() == 'false') {
            that.set('message','No such table exists');
          }
          else {
            that.set('message','');
          }
          that.send('setData');
          that.send('getData');
      }).catch(function(error){
        alert(error);
      });
    },
    setData() {
      var tableData = data;
      this.set('tableData',tableData);
      sortedModel: Ember.computed.sort("model.row", "sortProperties");
    },
    sortBy: function(property,ascending) {
        alert(property+"received");
        sortedModel: Ember.computed.sort("model.row", "sortProperties");
        this.set('sortProperties',[property]);
        this.set('sortAscending',ascending);
      }
  }
});
