import Ember from 'ember';

export default Ember.Component.extend({
  tagName: 'section',
  page: 1,
  paginateBy: 10,
  paginatedItems: Ember.computed('items', 'page','numberOfPages', function(){
    var i = (parseInt(this.get('page')) - 1) * parseInt(this.get('paginateBy'));
    var j = i + parseInt(this.get('paginateBy'));
    if('items' == null)
      return false;
    else {
      return this.get('items').slice(i, j);
    }
  }),
  isPaginated:Ember.computed('items', function(){
    if(this.get('page') > this.get('numberOfPages'))
      this.set('page',1);
    return ((this.get('items')!==null) && (this.get('items')!==undefined) && (this.get('items')!==""));
  }),
  numberOfPages: Ember.computed('items','paginateBy',function(){
    var n = this.get('items.length');
    var c = parseInt(this.get('paginateBy'));
    var r = Math.floor(n/c);
    if(n % c > 0) {
      r += 1;
    }
    return parseInt(r);
  }),
  pageNumbers: Ember.computed('numberOfPages', function(){
    var num = this.get('numberOfPages');
    var n = Array(num);
    for(var i = 0;i < num;i++) {
      n[i] = i + 1;
    }
    return n;
  }),
  showNext: Ember.computed('page','numberOfPages', function(){
    return (this.get('page') < this.get('numberOfPages'));
  }),
  showPrevious: Ember.computed('page','numberOfPages', function(){
    return (this.get('page') > 1);
  }),
  nextText: 'Next page',
  previousText: 'Previous page',
  actions: {
    nextClicked() {
      if(this.get('page') + 1 <= this.get('numberOfPages')) {
        this.set('page', this.get('page') + 1);
      }
    },
    previousClicked() {
      if(this.get('page') > 0) {
        this.set('page', this.get('page') - 1);
      }
    },
    pageClicked(pageNumber){
      this.set('page', pageNumber);
    }
  }
});
