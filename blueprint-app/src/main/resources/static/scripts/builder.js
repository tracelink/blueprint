// Configure any event listeners or special elements at page load and after AJAX requests
$(document).ready(function() {
	configureElements();

	$('#importPolicyModal').on('show.bs.modal', function(e) {
        let target = $(e.relatedTarget);
        let type = target.data('type');
        $("#importFormType").attr("value", type);
    });

	$('#importPolicyModal').on('shown.bs.modal', function(e) {
    	const importEditor = CodeMirror.fromTextArea(document.getElementById("import"), {
    		lineNumbers: true
    	});
    	importEditor.setSize(null, 240);
    });

    $('#savePolicyModal').on('show.bs.modal', function(e) {
    	$("#export-form").attr("action", "/save");
    });

    $('#exportPolicyButton').on("click", function() {
    	$("#export-form").attr("action", "/export");
    	$("#export-form").trigger('submit');
    });

});
$(document).ajaxComplete(configureElements);

function configureElements() {
    // Configure button to add clause
    $(".add-clause").off("click").on("click", addClause);

	// Configure buttons to delete clauses
	$(".delete-clause").off("click").on("click", deleteClause);

    // Configure buttons to add statements
    $(".add-statement").off("click").on("click", addStatement);

    // Configure buttons to delete statements
    $(".delete-statement").off("click").on("click", deleteStatement);

    // Configure tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Configure selectpickers
	$(".selectpicker").selectpicker("refresh");

	configureDynamicSelect();
}

// Clause event listeners
function addClause(event) {
	const index = $(".clauses-list li").length;
	// Hide no clauses message
   	if (index === 0) {
       	$(".clauses-none").hide();
    }
	// Append a new clause
	$.get("fragments/clause?index=" + index + "&type=" + getPolicyType(), function(clause) {
        $(".clauses-list").append(clause);
    });
}

function deleteClause(event) {
	// Get clause of delete button
	const clause = $(event.target).closest("li");
	// Update indices of clauses after this one
	clause.nextAll("li").each(updateClauseIndices);
	// Delete clause
	clause.remove();
	// Show no clauses message
	if ($(".clauses-list li").length === 0) {
		$(".clauses-none").show();
	}
}

function updateClauseIndices(i, clause) {
	$(clause).find(".indexed-element").each(updateClauseIndexedElement);
	$(clause).find(".negated-toggle").each(updateClauseNegated);
}

function updateClauseIndexedElement(i, element) {
	const name = element.name;
	if (name === undefined) {
		return;
	}
    const oldIndex = name.split("clauses[")[1].split("]")[0];
    const newIndex = oldIndex - 1;
    $(element).attr("name", name.replace("clauses["+oldIndex+"]", "clauses["+newIndex+"]"));
}

function updateClauseNegated(i, element) {
	const id = $(element).find("input").attr("id");
    const oldIndex = id.split("-")[1];
    const newIndex = oldIndex - 1;
    const newId = id.split("-")[0] + "-" + newIndex + "-" + id.split("-")[2];
    $(element).find("input").attr("id", newId);
    $(element).find("label").attr("for", newId);
}

// Statement event listeners
function addStatement(event) {
	const clauseIndex = $(event.target).closest("li").index();
	const statementsList = $(event.target).closest(".card").find(".statements-list");
	const statementIndex = $(statementsList).find(".configured-statement").length;
	baseStatement = $(event.target).closest(".card-header").find("select").val();
	// Append a new statement
	const endpoint = "fragments/statement?clauseIndex=" + clauseIndex + "&statementIndex="
		+ statementIndex + "&baseStatement=" + baseStatement;
	$.get(endpoint, function(statement) {
        $(statementsList).append(statement);
    });
}

function deleteStatement(event) {
	// Get clause of delete button
	const clause = $(event.target).closest(".configured-statement");
	// Update indices of clauses after this one
	clause.nextAll(".configured-statement").each(updateStatementIndices);
	// Delete clause
	clause.remove();
	// Show no clauses message
	if ($(".clauses-list li").length === 0) {
		$(".clauses-none").show();
	}
}

function updateStatementIndices(i, statement) {
	$(statement).find(".indexed-element").each(updateStatementIndexedElement);
	$(statement).find(".negated-toggle").each(updateStatementNegated);
}

function updateStatementIndexedElement(i, element) {
	const name = element.name;
    if (name === undefined) {
    	return;
    }
    const oldIndex = name.split("statements[")[1].split("].")[0];
    const newIndex = oldIndex - 1;
    $(element).attr("name", name.replace("statements["+oldIndex+"]", "statements["+newIndex+"]"));
}

function updateStatementNegated(i, element) {
	const id = $(element).find("input").attr("id");
    const oldIndex = id.split("-")[2];
    const newIndex = oldIndex - 1;
    const newId = id.split("-")[0] + "-" + id.split("-")[1] + "-" + newIndex;
    $(element).find("input").attr("id", newId);
    $(element).find("label").attr("for", newId);
}

// Gets the current policy type from the DOM
function getPolicyType() {
	const type = $(".btn-group-toggle").find(".active").text();
	return type;
}


// Dynamic selectpickers
function configureDynamicSelect() {
	const content = "<div class='input-group col-lg-12'><input type='text' class='form-control bss-input' onKeyDown='event.stopPropagation();' onKeyPress='addSelectInpKeyPress(this,event)' onClick='event.stopPropagation()' placeholder='Add item'><div class='input-group-append'><button class='btn btn-outline-secondary p-0' type='button' onClick='addSelectItem(this,event,1);'><i class='material-icons md-18 p-1'>add</i></button></div></div>";

  	const divider = $("<option/>")
  		.addClass("divider")
  		.attr("data-divider", true);

  	const addOption = $("<option/>")
  		.addClass("addItem")
  		.attr("data-content", content);

  	$("select.select-dynamic.new-select")
  		.append(divider)
  		.append(addOption)
  		.selectpicker("refresh");

  	$(".select-dynamic.new-select").removeClass("new-select");

  	$(".addItem.dropdown-item").on("click", function(event) {
  		event.preventDefault();
  		event.stopPropagation();
  	});

}

function addSelectItem(t, ev) {
   	ev.stopPropagation();
   	var bs = $(t).closest('.bootstrap-select')
   	var txt=bs.find(".bss-input").val().replace(/[|]/g,"");
   	var txt=$(t).closest(".input-group").find(".bss-input").val().replace(/[|]/g,"");
   	if ($.trim(txt)=='') return;

   	var p=bs.find("select");
   	var o=$("option", p).eq(-2);
   	o.before( $("<option>", { "selected": true, "text": txt}) );
   	p.selectpicker("refresh");
}

function addSelectInpKeyPress(t, ev) {
   	ev.stopPropagation();
   	// do not allow pipe character
   	if (ev.which==124) ev.preventDefault();

   	// enter character adds the option
   	if (ev.which==13)
   	{
      	ev.preventDefault();
      	addSelectItem($(t).next(),ev);
   	}
}
