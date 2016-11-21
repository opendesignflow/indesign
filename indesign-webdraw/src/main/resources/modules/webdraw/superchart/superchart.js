

$(function() {

	// Enable plugins like cursor and highlighter by default.
	$.jqplot.config.enablePlugins = true;
	// For these examples, don't show the to image button.
	$.jqplot._noToImageButton = false;

	console.log("Loaded Superchart");
	
	localWeb.onPushData("UpdateGraphRequest",function(updateGraph) {
		
		var id = updateGraph._a_targetId;
			
		// Create JSon Data
		console.log("Got XY Data for "+id);
		console.log("Graph: "+JSON.stringify(updateGraph));
		
		//
		
		var seriesDatas = [];
		$(updateGraph.XYGraph.Point).each(function(i,p) {
			seriesDatas.push([parseFloat(p.X),parseFloat(p.Y)]);
		});
		var series = [
		             {
		            	 key : "Test",
		            	 values: seriesDatas
		             }
		];
		
		indesign.superchart.doJQPlot(id,series);
		
		
		
	});
	
	// Superchart Namespace
	//-------------------------
	indesign.superchart = {
			
	};
			
		
	/**
	 * Format
	 */
	indesign.superchart.doJQPlot = function(finalId, series) {

		
		 console.log("Doing JQchart chart " + finalId); 
		 //console.log("Data: "+JSON.stringify(series));
		 
		
		
			/*renderer:$.jqplot.OHLCRenderer, 
			rendererOptions:{candleStick:true}*/
		 
		// Get Series Datas
		//------------
		var seriesDatas = [];
		var seriesOpts = [];
		$(series).each(function(i,serie) {
			seriesDatas.push(serie.values);
			var options = {
					label: serie.key,
					color: serie.color,
					showMarker: false,
					shadow: false,
					lineWidth: 1
				};
			
			seriesOpts.push(options);
		});
		
		 // Get Existing Chart
		 //-----------
		 //var finalId = "superchart-"+i;
		 $("#"+finalId).empty();
		 var plot = $("#"+finalId).data('plot');
		 if (!plot) {
			 
			
			
			
			// Find Title on element
			// -----------
			var chartDiv = $(finalId)
			var title = chartDiv.attr("data-title")
			if (!title) {
				title = "";
			}
			
			opts = {
				title : title,
				animate : false,
				series : seriesOpts,
				legend: {
			        show: true,
			        location: 'nw',     // compass direction, nw, n, ne, e, se, s,
										// sw, w.
			        xoffset: 12,        // pixel offset of the legend box from the x
										// (or x2) axis.
			        yoffset: 12,        // pixel offset of the legend box from the y
										// (or y2) axis.
			    },
			    
				axes : {
					xaxis : {
						label: "Points"
					},
					yaxis : {
						label:'Values'
					}
				},
				cursor : {
					zoom : true,
					looseZoom : true,
					showTooltip : true,
					followMouse : true,
					showTooltipOutsideZoom : true,
					constrainOutsideZoom : false
				}
			};

			plot = $.jqplot(finalId, seriesDatas, opts);
			$(finalId).data('plot',plot);
			
		 } else {
			
			 console.log("Reinit");
			 plot.reInitialize(seriesDatas);
			 
			 
		 }

		


	}
		

	
	
	
	
	
	
	
});