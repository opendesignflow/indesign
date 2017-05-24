indesign.createNs("indesign.webdraw");
console.log("Created indesign webdraw...");

// WebDraw Layout
//------------------------

indesign.webdraw.layoutRow = function(target) {
	
	var availableWidth = $(target).width();
	var availableHeight = $(target).height();
	
	var spacing = $(target).data("spacing") || 0;
	
	//-- Prepare sizes
	var allContainers = $(target).find(".indesign-webdraw-area");
	
	console.log("Number of containers: "+allContainers.length+" for "+availableWidth);
	
	var widthPerContainer = (availableWidth - (allContainers.length * spacing)) / allContainers.length;
	
	//-- Reference Size is either widht or height
	if (widthPerContainer>availableHeight) {
		var refDimension = availableHeight;
	} else {
		var refDimension = widthPerContainer;
	}
	
	console.log("Width per container: "+widthPerContainer);
	
	//-- Take all the Containers
	$(target).find(".indesign-webdraw-area").each(function(i,area) {
		
		//-- Use width/height ration to determine how much from width we can use
		//-- > 1 then wider then higher
		//-- < 1 then higher than wider
		var ratio = parseFloat($(area).data("ratio")|| "1.0");
		if (ratio>=1) {
			var cheight = refDimension - (refDimension * (ratio-1));
			var cwidth = refDimension;
		} else {
			var cheight = refDimension;
			var cwidth = (refDimension * (ratio));
		}
		$(area).width(cwidth);
		$(area).height(cheight);
		
		//-- Calculate padding and position offset
		var padding = widthPerContainer - cwidth;
		var offsetLeft = (i*widthPerContainer+padding/2)+"px";
		//var offsetLeft = (padding/2)+"px";
		
		console.log("Placing "+i+" to "+offsetLeft);
		
		/*$(area).css( { 
			position: 'relative',
			display: 'inline-block',
			zIndex: 1,
			left: offsetLeft, 
			top: "0px"
		} )*/;
		$(area).css( { 
			display: 'inline-block',
			'margin-left':(padding/2)+"px",
			'margin-right':(padding/2)+"px"
		} )
		
		//-- Prepare Renderers
		if($(area).hasClass("indesign-webdraw-pixi")) {
			
			var renderer = PIXI.autoDetectRenderer(cwidth, cheight,{backgroundColor : 0x000000});
			area.appendChild(renderer.view);
			$(area).data("renderer",renderer);
			
			//-- create the root of the scene graph
			var stage = new PIXI.Container();
			stage.width = cwidth;
			stage.height = cheight;
			$(area).data("stage",stage);
		}
		
	});
	
};

indesign.webdraw.startAnimate = function() {
	indesign.webdraw.doAnimate();
};
indesign.webdraw.doAnimate = function() {
	
	
	$(".indesign-webdraw-area").each(function(i,area) {
		
		if($(area).hasClass("indesign-webdraw-pixi")) {
			
			var renderer = $(area).data("renderer");
			var stage = $(area).data("stage");
			if (renderer && stage) {
				renderer.render(stage);
			} else {
				console.warn("indesign webdraw area pixi "+$(area).attr("id")+" has no renderer or stage data!! Not rendering");
			}
			
		}
		
	});
	requestAnimationFrame(indesign.webdraw.doAnimate);
	
}

// Init
//----------------

$(function() {

	console.log("Init Webdraw....");
	
	$(".indesign-webdraw-consumeRemainingBodyHeight").each(function(i,e) {
	
		// Body height
		var bheight = $(window.document.body).height();
		console.log("Body height="+bheight);
		var currenty = $(e).position().top;
		var remainingHeight = bheight -currenty - parseInt($(window.document.body).css("marginTop"))-parseInt($(window.document.body).css("marginBottom"));
		console.log("Element height="+remainingHeight);
		$(e).outerHeight(remainingHeight);
	})
	;
	
	
});
