var now = new Date(),
    // minimum date the user can choose, in this case now and in the future
    minDate = now.toISOString().substring(0,10);

//$('#form_date').prop('min', minDate);
$(function(){
//$('.carousel').carousel();

$('#form-create-news').on('submit', function () {
  var fd = new FormData($(this)[0]);    

  if (window.filesize > 1024*1024*10) {
    alert('max upload size is 10mb');
return false;
  }
  $.ajax({
    // Your server script to process the upload
    url: $(this).attr("action"),
    type: $(this).attr("method"),
    //request [:headers "x-forgery-token"]))
    beforeSend: function(request) {
         //$('.loader').show();
      var hey= $("[name='__anti-forgery-token']").val();
      console.log("how are you there" + hey);
      request.setRequestHeader("x-csrf-protection", hey);
    },
    // Form data
    data: fd,
    // Tell jQuery not to process data or worry about content-type
    // You *must* include these options!
    cache: false,
    contentType: false,

    processData: false,
    // Custom XMLHttpRequest
    success: function (data) {
	    console.log("HEY")
	    console.log(JSON.stringify(data))
	    console.log(JSON.stringify(data.redirect))
	    if (data.redirect){
	    window.location=data.redirect;
	    }else{
	    alert(String(data));
	    }
},
	  complete: function(){
		         $('.loader').hide();
		    },
    xhr: function () {
      var myXhr = $.ajaxSettings.xhr();
      if (myXhr.upload) {
        // For handling the progress of the upload
        myXhr.upload.addEventListener('progress', function (e) {
          if (e.lengthComputable) {
            $('progress').attr({
              value: e.loaded,
              max: e.total,
            });
          }
        }, false);
      }
      return myXhr;
    }
  });
	return false;
  });
/*$('#form-create-news').on('submit', function (e) {
  e.preventDefault(); // évite le submit classique

  var file = $("[name=photo]")[0].files[0];
  if (file.size > 1024 * 5) {
    alert('max upload size is 5k');
    return false;
  }

  var fd = new FormData(this); // pas besoin de append si le champ est dans le form

  $.ajax({
    url: $(this).attr("action"),
    type: $(this).attr("method"),
    beforeSend: function(request) {
      var token = $("[name='__anti-forgery-token']").val();
      request.setRequestHeader("x-csrf-protection", token); // adapte selon ton backend
    },
    data: fd,
    cache: false,
    contentType: false,
    processData: false,
    success: function (data) {
      if (data.redirect) {
        window.location = data.redirect;
      } else {
        alert(String(data));
      }
    },
    complete: function () {
      $('.loader').hide();
    },
    xhr: function () {
      var myXhr = $.ajaxSettings.xhr();
      if (myXhr.upload) {
        myXhr.upload.addEventListener('progress', function (e) {
          if (e.lengthComputable) {
            $('progress').attr({ value: e.loaded, max: e.total });
          }
        }, false);
      }
      return myXhr;
    }
  });
});*/


  
});
