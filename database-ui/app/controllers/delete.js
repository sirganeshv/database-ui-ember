import Controller from '@ember/controller';

export default Controller.extend({
  //queryParams: ['object'],
  //object: null,
  /*obj: Ember.computed("object", function() {
    return JSON.parse(this.get('object'));
  }),*/
  deleteInitiated: true,
  deleted: false,
  obj: null,
  actions : {
    delete() {
      var that = this;
      Ember.$.ajax({
        url: "/fetchEvent",
        type: "POST",
        data: {
          "table_name" : 'id',
          "eventID" : that.get('deleteID'),
        },success : function(resp){
          alert((resp));
          //that.set('deleted',true);
          that.set('deleteInitiated',false);
          that.set('deleted',true);
          that.set('obj',JSON.parse(resp));
        },error : function(error){
          alert(error);
        }
      });
    },
    deleteEvent() {
      var that = this;
      Ember.$.ajax({
        url: "/fetchEvent",
        type: "POST",
        data: {
          "table_name" : 'id',
          "eventID" : that.get('deleteID'),
        },success : function(resp){
          alert("Deleted");
          that.set('deleteInitiated',true);
          that.set('deleted',false);
          that.transitionToRoute("index");
          //that.set('deleted',true);
        },error : function(error){
          alert(error);
        }
      });
    },
    closeDelete() {
      this.set('deleteInitiated',false);
      this.set('deleted',false);
      this.transitionToRoute("index");
    },
    back() {
      //alert(JSON.parse(this.get('object')).col);
      this.transitionToRoute("index");
    }
  }
});
