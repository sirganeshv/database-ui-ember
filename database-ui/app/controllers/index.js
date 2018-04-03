import Controller from '@ember/controller';
var data = ' ';
export default Controller.extend({
  ajax: Ember.inject.service(),

  sortProperties: Ember.computed("model.col", function(){
    if(this.get('model.col') == null) {
      return [''];
    }
    else {
      return [this.get('model.col')[0]];
    }
  }),
  prop: Ember.computed("model.col", function(){
    if(this.get('model.col') == null) {
      return [''];
    }
    else {
      return [this.get('model.col')[0]];
    }
  }),
  actions : {
    display() {
      var table_name = this.get('table_name');
      if(table_name !== null  && table_name != undefined && table_name != '') {
        var that  = this;
        Ember.$.ajax({
          url: "/get_page ",
          type: "POST",
          //contentType: "charset=utf-8",
          //contentType: 'application/json; charset=utf-16',
          data: {
            "table_name" : this.get('table_name'),
            "start" : 0,
            "stop" : 5,
            "sortProperties" : that.get('sortProperties')
          }
        }).then(function(resp){
          alert(resp);
            var table = document.getElementById("table");
            if(table != null) {
              var rowCount = table.rows.length;
              for (var x=rowCount-1; x>0; x--) {
  					    table.deleteRow(x);
  				    }
            }
            if(resp.trim() == '') {
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
    },
    sortBy: function(property) {
      this.set('prop',property);
      this.set('sortProperties',[property]);
    }
  }
});
