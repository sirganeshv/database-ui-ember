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
            //alert(resp);
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
      //alert(table_name);
      //this.transitionToRoute('export-mail', { queryParams: { showDetails: true }});
      this.transitionToRoute('export-mail');
      //this.get('router').transitionTo('export-mail');
      /*var start = parseInt(prompt("Enter start eventID"));
      while(isNaN(start)) {
        start = parseInt(prompt("Enter start eventID as number"));
      }
      this.set('start',start);
      var end = parseInt(prompt("From "+ this.get('start') +" to ?"));
      while(isNaN(end) || start >= end) {
        end = parseInt(prompt("From "+ this.get('start') +" to ?(number)"));
      }
      this.set('end',end);
      var i = (parseInt(this.get('page')) - 1) * parseInt(this.get('paginateBy'));
      var j = i + parseInt(this.get('paginateBy'));
      var receiverMailID = prompt("Enter your Email ID");
      while(!(/[a-zA-Z0-9\.]+@[a-zA-Z0-9]+\.[[a-zA-Z0-9\.]*[a-zA-z]$/.test(receiverMailID))) {
        receiverMailID = prompt("Enter correct mail ID (abc@xyz.com)");
      }
      var minute =  parseInt(prompt("Which minute you  want to get mail (mm)"));
      while(minute > 59 || minute < 0 || isNaN(minute)) {
        minute = parseInt(prompt("Enter minute (0 to 59)"));
      }
      var isConfirmed = confirm("Do you want to export from event ID "+this.get('start')+" to "+this.get('end'));
      if(isConfirmed) {
        var that  = this;
        //this.set('isExporting',true);
        //progress = 0.0;
        //export_finished = false;
        Ember.$.ajax({
          url: "/exportEmail",
          type: "POST",
          data: {
            "table_name" : that.get('table_name'),
            "prop" : that.get('prop'),
            'isAscending' : that.get('sortAscending'),
            "start" : that.get('start'),
            "stop" : that.get('end'),
            "filterCol" : that.get('filterCol'),
            "filterValue" : that.get('filterValue'),
            "minute" : minute,
            "receiverMailID" : receiverMailID,
          },success : function(resp){
              //alert(resp);
              //export_finished = true;
              //that.set('isExporting',false);
          },error : function(error){
            alert(error);
          }
        });
        //myTimer = setInterval(function(){checkProgress() },2);
      }*/
    },
  }
});
