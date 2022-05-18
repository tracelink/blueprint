$(document).ready(function() {
	// Configure function argument replacement
	$("#function").on("change", updateArguments);
});

function updateArguments() {
	const saveForm = $("#save-form");
	// We are not trying to edit, so return
	if (saveForm === null) {
		return;
	}
	// Get selected function name
	const functionName = $("#function").val();
	// Parse base statement id
	const action = saveForm.attr("action");
	const actionComponents = action.split("/");
	if (actionComponents.length === 4) {
		const baseStatementId = actionComponents[2];
		$.get("/fragments/arguments?baseStatementId=" + baseStatementId + "&functionName=" + functionName, replaceArguments);
	} else {
		$.get("/fragments/arguments?functionName=" + functionName, replaceArguments);
	}
}

function replaceArguments(arguments) {
	$("#arguments").replaceWith(arguments);
}
