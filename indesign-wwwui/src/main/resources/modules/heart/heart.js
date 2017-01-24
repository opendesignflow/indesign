$(function() {
	
	// REgister Messqge
	//--------------------
	console.log("Init Heart");
	localWeb.onPushData("HeartTaskStatus",function(status) {
		
		var id = localWeb.decodeHTML(status.ID);
		console.log("Received Status done for: "+id);
		location.reload();
		
		
	});
	
});
