import Controller from '@ember/controller';
var data = ' ';
export default Controller.extend({
  ajax: Ember.inject.service(),
  sortProperties: ['acc_no:desc'],
  actions : {
    display() {
      var table_name = this.get('table_name');
      alert('hello');
      if(table_name !== null  && table_name != undefined && table_name != '') {
        var that  = this;
        Ember.$.ajax({
          url: "/get_page ",
          type: "POST",
          //contentType: "charset=utf-16",
          data: {
            "table_name" : this.get('table_name'),
            "start" : 0,
            "stop" : 5,
          }
        }).then(function(resp){
            alert(that.get('table_name'));
            alert(resp);
            var table = document.getElementById("table");
            if(table != null) {
              var rowCount = table.rows.length;
              for (var x=rowCount-1; x>0; x--) {
  					    table.deleteRow(x);
  				    }
            }
            if(resp.trim() == 'false') {
              that.set('message','No such table exists');
            }
            else {
              data = JSON.parse(resp);
              that.set('message','');
            }
            that.send('setData');
            that.send('getData');
        }).catch(function(error){
          alert(error);
        });
      }
    },
    setData() {
      var tableData = data;
      this.set('tableData',tableData);
    }
  }
});
