import Controller from '@ember/controller';
var data = ' ';
export default Controller.extend({
  ajax: Ember.inject.service(),
  sortProperties: ['acc_no:desc'],
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
          alert(JSON.stringify(data.row));
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
    }
  }
});
