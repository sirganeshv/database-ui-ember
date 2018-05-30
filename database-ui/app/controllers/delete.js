import Controller from '@ember/controller';

export default Controller.extend({
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
        url: "/delete",
        type: "POST",
        data: {
          "table_name" : 'id',
          "eventID" : that.get('deleteID'),
        },success : function(resp){
          alert("Deleted");
          that.set('deleteInitiated',true);
          that.set('deleted',false);
          that.transitionToRoute("index");
        },error : function(error){
          alert(error);
        }
      });
    },
    back() {
      this.set('deleteInitiated',true);
      this.set('deleted',false);
      this.transitionToRoute("index");
    }
  }
});
