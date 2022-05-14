$(document).ready(function() {

	// Configure tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Configure selectpickers
   	$(".selectpicker").selectpicker();

	// Configure delete modal
	$('#deletePolicyModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let id = target.data('id');
		$("#delete-form").attr("action", "/policies/delete/" + id);
    });

});
