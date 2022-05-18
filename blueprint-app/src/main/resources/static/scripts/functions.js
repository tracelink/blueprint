$(document).ready(function() {

   	// Configure code mirrors
   	$(".code-static").each(function(index, elem) {
   		const editor = CodeMirror.fromTextArea(elem, { lineNumbers: true, readOnly: "nocursor" });
   		editor.setSize(null, 260);
   	});
   	$(".code-edit").each(function(index, elem) {
   		const editor = CodeMirror.fromTextArea(elem, { lineNumbers: true });
       	editor.setSize(null, 260);
    });

	// Configure delete modal
//	$('#deletePolicyModal').on('show.bs.modal', function(e) {
//		let target = $(e.relatedTarget);
//		let id = target.data('id');
//		$("#delete-form").attr("action", "/policies/delete/" + id);
//    });

});
