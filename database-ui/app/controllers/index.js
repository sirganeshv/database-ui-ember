import Controller from '@ember/controller';

export default Controller.extend({
  ajax: Ember.inject.service(),
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
          var data = JSON.parse(resp);
          var table = document.getElementById("table");
          var rowCount = table.rows.length;
          for (var x=rowCount-1; x>=0; x--) {
					  table.deleteRow(x);
					}
          if(resp.trim() == 'false') {
            //alert("No table");
            that.set('message','No such table exists');
          }
          else {
            that.set('message','');
            $('#tablediv').css("display","initial");
            var header = table.createTHead();
            var row = header.insertRow(0);
            for(var i = 0; i < data.col.length;i++) {
              var cell = row.insertCell(i);
              cell.innerHTML = data.col[i];
  					}
            for(var i = 0; i < data.row.length;i++) {
  						var row = table.insertRow(i+1);
              for(var j =0 ;j < data.row[i].length;j++) {
                var cell1 = row.insertCell(j);
                cell1.innerHTML = data.row[i][j];
              }
            }
          }
      }).catch(function(error){
        alert(JSON.stringify(error));
      });
    }
  }
});
