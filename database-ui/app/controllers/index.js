import Controller from '@ember/controller';
var data = ' ';
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
          data = JSON.parse(resp);
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
          }
            /*$('#tablediv').css("display","initial");
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
          }*/
      }).catch(function(error){
        alert(JSON.stringify(error));
      });
    },
  test() {
    /*var peop = [{
      title: 'Grand Old Mansion',
      body: 'it is a good mansion',
      rate: [
        4300,4500]
    }, {
      title: 'Urban Living',
      body: 'Not bad',
      rate: [4500,3231]
    }, {
      title: 'Downtown Charm',
      body: 'Ok ok',
      rate: [5000,3223]
    }];
    this.set('tableData',peop);*/
    //this.transitionToRoute('/index',model);
    //alert('test');
    var tableData = data;
    //alert(tableData.col[0]);
    this.set('tableData',tableData);
  }
}
});
