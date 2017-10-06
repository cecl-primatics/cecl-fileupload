$(document).ready(function() {

	$("#btnCalc").click(function(event) {

		//stop submit the form, we will post it manually.
		event.preventDefault();
		fire_ajax_calc();

	});

});

function fire_ajax_calc() {

	// Get form
	var form = $('#calcForm')[0];
	
	var data = new FormData(form);

	$("#btnCalc").prop("disabled", true);
	
	$.ajax({
		type : "POST",
		enctype : 'form-data',
		url : "/api/calculate",
		data : data,
		//http://api.jquery.com/jQuery.ajax/
		//https://developer.mozilla.org/en-US/docs/Web/API/FormData/Using_FormData_Objects
		processData : false, //prevent jQuery from automatically transforming the data into a query string
		contentType : false,
		cache : false,
		timeout : 600000,
		success : function(data) {

			$("#result1").text(data);
			$('#myCal').remove();
			console.log("SUCCESS : ", data);
			$("#btnCalc").prop("disabled", true);

		},
		error : function(e) {

			$("#result1").text(e.responseText);
			$('#myCal').remove();
			console.log("ERROR : ", e);
			$("#btnCalc").prop("disabled", false);

		}
	});
}