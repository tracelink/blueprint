$(document).ready(function() {
	// Configure tooltip
	$('[data-toggle="tooltip"]').tooltip();

	// Configure CodeMirror blocks
    const regoEditor = CodeMirror.fromTextArea(document.getElementById('rego'), {
        lineNumbers: true
    });

	const policyEditor = CodeMirror.fromTextArea(document.getElementById('policy'), {
		lineNumbers: true
	});
});
