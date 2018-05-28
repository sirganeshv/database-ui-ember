import Controller from '@ember/controller';

export default Controller.extend({
  actions : {
    //exportEmail(table_name,sortProperties,isAscending,filterCol,filterValue) {
    exportEmail() {
      var val;
      var radios = document.getElementById('freq').children;
      for (var i=0, len=radios.length; i<len; i++) {
          if ( radios[i].checked ) { // radio checked?
              val = radios[i].value; // if so, hold its value in val
              break; // and break out of for loop
          }
      }
      //alert(val);
      var that = this;
      Ember.$.ajax({
        url: "/exportEmail",
        type: "POST",
        data: {
          //"table_name" : that.get('table_name'),
          "start" : that.get('start'),
          "stop" : that.get('end'),
          "frequency" : val,
          "receiverMailID" : that.get('receiverMailID'),
        },success : function(resp){
            alert(resp);
            that.transitionToRoute("index");
            //export_finished = true;
            //that.set('isExporting',false);
        },error : function(error){
          alert(error);
        }
      });
      //alert(val);
    }
  }
});
