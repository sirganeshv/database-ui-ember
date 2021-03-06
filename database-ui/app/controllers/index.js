import Controller from '@ember/controller';
var data = ' ';
var image_url = "pdf_icon.png";
$(window).on("keydown", function(evt){
    if (evt.keyCode===8 && evt.target.nodeName==="BODY") {
        evt.preventDefault();
    }
});
export default Controller.extend({
  ajax: Ember.inject.service(),
  loading : false,
  progress: 0.5,
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
      this.set('archived',false);
      var table_name = this.get('table_name');
      if(table_name !== null  && table_name != undefined && table_name != '') {
        var that  = this;
        this.set('loading',true);
        this.set('loadingMessage',"Loadingggg...Please wait");
        var that = this;
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
            //alert("resp is "+ resp);
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
            that.set('loading',false);
            that.send('setData');
            that.send('getData');
        }).catch(function(error){
          alert(error);
          that.set('loading',false);
        });
      }
    },
    setData() {
      var tableData = data;
      this.set('tableData',tableData);
    },
    sortBy: function(property,sortAscending) {
      this.set('prop',property);
      this.set('sortProperties',[property]);
      this.set('sortAscending',sortAscending);
    },
    exportEmail(table_name,sortProperties,isAscending,filterCol,filterValue) {
      this.transitionToRoute('export-mail');
    },
    deleteEvent(resp) {
      this.transitionToRoute('delete');
    },
    restore() {
      var that = this;
      Ember.$.ajax({
        url: "/restore",
        type: "POST",
        data: {
        },success : function(resp) {
          alert('data successfully Restored');
          that.set('archived',false);
          that.set('isPresent',true);
          //that.refresh();
        },error : function(error){
          alert(error);
        }
      });
    },
  }
});
