indesign.pdfjs = {
			
	
	
	/*
	 * var canvas = document.getElementById('lecturepdf'); if (canvas) {
	 * 
	 * var url = "http://localhost:8585"+$(canvas).attr("data-url");
	 * 
	 * console.log("Loading pdf from "+url);
	 * 
	 * 
	 * PDFJS.getDocument(url).then(function(pdf) {
	 * 
	 * console.log("Got PDF..."); pdfDoc = pdf; changeToPage(1);
	 * 
	 * }); }
	 */
	
	
		
}
// EOF Namespace



indesign.pdfjs.init = function() {
	
	
	// Init
	// -----------------
	console.log("Init pdf js....");
	PDFJS.imageResourcesPath = '/resources/pdfjs/web/images/';
	PDFJS.workerSrc = '/resources/pdfjs/build/pdf.worker.js';
	PDFJS.cMapUrl = '/resources/pdfjs/web/cmaps/';
	PDFJS.cMapPacked = true;
			
	// -- get Canvas
	$("canvas[id*='pdfjs-'").each(function(i,canvas) {
		console.log("Found PDF Canvas..."+$(canvas).attr("id"));
		//"http://localhost:8585"+
		var url = "http://localhost:8585"+$(canvas).attr("data-url");
		PDFJS.getDocument(url).then(function(pdf) {
			indesign.pdfjs.pdfDoc[$(canvas).attr("id")] = pdf; 
			indesign.pdfjs.changeToPage($(canvas).attr("id"),$(canvas).attr("page"));
		});
	});
}


// Variables
//-----------

indesign.pdfjs.pdfDoc = [];
/*indesign.pdfjs.pdfPage
var  = 0;*/

// Functions
// -----------------------

indesign.pdfjs.changeToPage =  function(id,p) {
	
	p = parseInt(p);
	
	//-- Get Doc
	var doc = indesign.pdfjs.pdfDoc[id];
	console.log("Changing page of: "+id+" to "+p+ " with available "+doc.numPages);
	
	
	if (p<1 || p > doc.numPages) {
		return;
	}
	
	// Change Local Page
	// --------------------
	doc.getPage(p).then(function(page) {
		
		// Update Page
		//----------
		var pdfPage = p;
		console.log("PDF Page...");
		var canvas = document.getElementById(id);
		$(canvas).attr("page",p);
		
		// Remote Update
		//-------------
		$.get("/h2dl/action/pdfjs.updatePage?page="+p);
		
		/*
		 * var desiredWidth = 100; var viewport = page.getViewport(1);
		 * var scale = desiredWidth / viewport.width; var scaledViewport =
		 * page.getViewport(scale);
		 */
		
		var scale = 1.0;
		var viewport = page.getViewport(scale);
		var context = canvas.getContext('2d');
		canvas.height = viewport.height;
		canvas.width = viewport.width;
		
		var renderContext = {
		  canvasContext: context,
		  viewport: viewport
		};
		page.render(renderContext);
	});
	
}

indesign.pdfjs.nextPage =  function(id) {
	
	// -- Get Current Page
	indesign.pdfjs.changeToPage("pdfjs-"+id,parseInt($("#pdfjs-"+id).attr("page"))+1);
}
indesign.pdfjs.previousPage =  function(id) {
	
	// -- Get Current Page
	indesign.pdfjs.changeToPage("pdfjs-"+id,parseInt($("#pdfjs-"+id).attr("page"))-1);
}

	

$(function() {
	
	indesign.pdfjs.init();
	
});