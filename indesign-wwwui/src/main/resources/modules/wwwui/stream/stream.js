$(function() {

	console.info("Loaded Stream...");
	
	localWeb.onPushData("StreamUpdate",function(streamUpdate) {
		
		var id   = "stream-"+localWeb.decodeHTML(streamUpdate.ID);
		var text = localWeb.decodeHTML(streamUpdate.Text);
		
		console.log("Received StreamUpdate for: "+id+" -> "+text+" -> "+streamUpdate._a_line);
		
		var streamArea = $("#"+id)
		if (streamArea) {
			if (streamArea.is("div")) {
				
				
				// Split lines to add them
				$(text.split("\n")).each(function(i,line) {
					streamArea.append(document.createTextNode(line));
					streamArea.append($("<br/>"));
				})
				
				//streamArea.text(streamArea.text()+text);
				
				
				if (streamUpdate._a_line) {
					//streamArea.append($("<br/>"));
				}
				
				
			} else if (streamArea.is("textarea")) {
				
				if (streamUpdate._a_line) {
					text = text + "\n"
				}
				
				
				streamArea.val(streamArea.val()+text);
			}
			
			
		}
		
		
	});
	
})
