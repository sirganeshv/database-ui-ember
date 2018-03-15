import { helper } from '@ember/component/helper';

export function displayHelper(params/*, hash*/) {
  return params;
}

export default helper(displayHelper);

Ember.Handlebars.registerHelper('json', function(context) {
  return JSON.stringify(context);
});
Ember.Handlebars.registerHelper('eachProperty', function(context, options) {
  var ret = "";
  var newContext = Ember.get(this, context);
  for(var prop in newContext)
  {
    if (newContext.hasOwnProperty(prop)) {
      ret = ret + options.fn({property:prop,value:newContext[prop]});
    }
  }
  return ret;
});
