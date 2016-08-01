indesign.createNs("indesign.webdraw.ace");

$(function() {

	console.log("Init ACE....");
	
	$(".indesign-ace-editor").each(function(i,e) {
		
		console.log("Found Editor....");
		
		//-- Create Editor
		var editor = ace.edit(e);
        editor.setTheme("ace/theme/monokai");
        
        //-- Set language if defined
        if ($(e).data("language")) {
        	editor.getSession().setMode("ace/mode/"+$(e).data("language"));
        }
        
        //-- Default parameters
        editor.setHighlightActiveLine(true);
        
        //-- Errors
        if ($(e).data("error")) {
        	
        	var err = $(e).data("error");
        	console.log("Found Error...."+(err.column));
        	//var err = $.parseJSON(err);
        	
        	editor.getSession().addGutterDecoration(err.line,"vui-popup indesign-ace-errorLine");
        	
        	var aceRange = require('ace/range').Range
        	editor.getSession().addMarker(new aceRange(err.line ,err.column-1,err.line,err.column),"indesign-ace-errorLine","text",true);
        	
        	//editor.getSession().addGutterDecoration(err.line,"warning sign icon");
        	
        	//-- Get line and add tooltip
        	$(e).find(".indesign-ace-errorLine").each(function(i,errLine) {
        		
        		$(errLine).data("content","Error here");
        		
        	});
        	
        }
        
        //-- Store editor to element for future usage
        $(e).data("editor",editor);
	})
	;
	
	$('.vui-popup').popup({
		inline : true,
		hoverable : true

	});
	
	
});
