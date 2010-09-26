jQuery(function($){//on document ready
  //autocomplete
  $('input.autocomplete').each(function(){
    var input = $(this);
    input.autocomplete(input.attr('data-autocomplete-url'),{
      matchContains:1,//also match inside of strings when caching
      //    mustMatch:1,//allow only values from the list
      //    selectFirst:1,//select the first item on tab/enter
      removeInitialValue:0//when first applying $.autocomplete
    });
  }); 
});
