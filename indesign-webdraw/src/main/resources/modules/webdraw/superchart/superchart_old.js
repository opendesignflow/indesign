

$(function() {

	// Superchart Namespace
	//-------------------------
	indesign.superchart = {
			
	};
	
	
	
	
	
	
	
	
	
	// Enable plugins like cursor and highlighter by default.
	$.jqplot.config.enablePlugins = true;
	// For these examples, don't show the to image button.
	$.jqplot._noToImageButton = false;
	
	$('.ui.checkbox')
	  .checkbox()
	;
	
	$('select.dropdown')
	  .dropdown()
	;
});
var plots = []

function sleepFor( sleepDuration ){
    var now = new Date().getTime();
    while(new Date().getTime() < now + sleepDuration){ /* do nothing */ } 
}

var doPlot = doJQPlot
doPlot = doNVD3Plot

function updatePlots() {
	
	// Clear Plots
	plots = []
	//$(function() {
		
	var allCharts = $(".chart");
	
	var progress = $("#chart-plot-progress");
	progress.progress({
		  value: 0,
		  total: allCharts.length,
		  label: 'ratio',
		  text: {
		      success : 'Done!',
		      ratio: '{value} of {total}'
		   }
	});
		
		//var allCharts = $("div[id^=chart-]");
		
		
	allCharts.each(function(i, e) {
			
		// Clear
		$(e).find(".ui").remove();
		
		// Set Dimmer
		$(e).append('<div class="ui segment chart-loading">  <div class="ui active dimmer"> <div class="ui text loader">Loading</div>  </div> <p></p> </div></div>');
		
	});
		
	console.log("Doing Charts");
		//var progressIncrement = 100 / allCharts.length;
		
	var i=0;
	
	var doChart = function(ts) {
		
		var e = $("#chart-"+i)
		
		// Clear
		$(e).find(".ui").remove();
		
		//console.log("Doing chart: "+i+" -> "+$(e).attr("data-series")+"->"+$(e).attr("id"));
		console.log("Doing chart: "+i);
		
		var plot = doPlot(i,eval($(e).attr("data-series")));
		plots.push(plot);
		
		$("#chart-plot-progress").progress('increment');
		
		i++;
		if (i<allCharts.length) {
			window.requestAnimFrame(doChart);
			//doChart(0);
		}
		
		// Update Progress
		//var prog = (i+1) * 100 /allCharts.length;
		
		//console.log("Done chart: "+prog);
		// sleepFor(2000);
		 /*window.requestAnimFrame(function() {
			 $("#chart-plot-progress").progress('increment');
		 });*/
	};
	//doChart(0);
	window.requestAnimFrame(doChart);
		
		/*allCharts.each(function(i, e) {
				setTimeout(function() {
				
				
				
				},100);
			
				
	
		});*/
		
	//});
	

}

function updateJQPlots() {
	
	// Clear Plots
	plots = []
	//$(function() {
		
		var allCharts = $(".chart");
		
		var progress = $("#chart-plot-progress");
		progress.progress({
			  value: 0,
			  total: allCharts.length,
			  label: 'ratio',
			  text: {
			      success : 'Done!',
			      ratio: '{value} of {total}'
			   }
		});
		
		//var allCharts = $("div[id^=chart-]");
		
		
		allCharts.each(function(i, e) {
			
			// Clear
			$(e).find(".ui").remove();
			
			// Set Dimmer
			$(e).append('<div class="ui segment chart-loading">  <div class="ui active dimmer"> <div class="ui text loader">Loading</div>  </div> <p></p> </div></div>');
			
		});
		
		console.log("Doing Charts");
		//var progressIncrement = 100 / allCharts.length;
		
		var i=0;
		
		var doChart = function(ts) {
			
			var e = $("#chart-"+i)
			
			// Clear
			$(e).find(".ui").remove();
			
			//console.log("Doing chart: "+i+" -> "+$(e).attr("data-series")+"->"+$(e).attr("id"));
			console.log("Doing chart: "+i);
			
			var plot = jqPlotChart(i,eval($(e).attr("data-series")));
			plots.push(plot);
			
			$("#chart-plot-progress").progress('increment');
			
			i++;
			if (i<allCharts.length) {
				window.requestAnimFrame(doChart);
			}
			
			// Update Progress
			//var prog = (i+1) * 100 /allCharts.length;
			
			//console.log("Done chart: "+prog);
			// sleepFor(2000);
			 /*window.requestAnimFrame(function() {
				 $("#chart-plot-progress").progress('increment');
			 });*/
		};
		window.requestAnimFrame(doChart);
		
		/*allCharts.each(function(i, e) {
				setTimeout(function() {
				
				
				
				},100);
			
				
	
		});*/
		
	//});
	

}


function doJQPlot(i, series) {

	/*
	 * console.log("Doing JQchart chart " + i); console.log("Data:
	 * "+JSON.stringify(series));
	 */

	
	//$(function() {

		
		// Get Series Datas
		var seriesDatas = [];
		var seriesOpts = [];
		$(series).each(function(i,serie) {
			seriesDatas.push(serie.values);
			seriesOpts.push({
				label: serie.key,
				color: serie.color,
				showMarker: false,
				shadow: false,
				lineWidth: 1
			});
		});
		
		
		// Find Title on element
		// -----------
		var chartDiv = $("#superchart-"+i)
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

		var plot = $.jqplot("chart-"+i, seriesDatas, opts);
		return plot;
	//});

}

function doNVD3Plot(i, data) {

	console.log("Doing chart " + i);

	$(data).each (function(i,serie) {
		
		var oldValues = serie.values;
		serie.values = []
		$(oldValues).each(function(i,pair) {
			serie.values.push({x: pair[0],y: pair[1]});
		});
		/*serie.values = $(serie.values).map(function(e) {
			return {
				x:e[0],
				y: e[1]
			}
		});*/
	});
	
	nv.addGraph(function() {
		
		var chart = nv.models.lineChart().margin({
			left : 100
		}) // Adjust chart margins to give the x-axis some breathing room.
		.useInteractiveGuideline(true) // We want nice looking tooltips and a
										// guideline!
		.showLegend(true) // Show the legend, allowing users to turn on/off
							// line series.
		.showYAxis(true) // Show the y-axis
		.showXAxis(true) // Show the x-axis
		;

		chart.xAxis // Chart x-axis settings
		.axisLabel('Points').tickFormat(d3.format(',r'));

		chart.yAxis // Chart y-axis settings
		.axisLabel('Values').tickFormat(d3.format('.02f'));

		/* Done setting the chart up? Time to render it! */
		var container = d3.select('#chart-'+i);
		var svg = container.append('svg');
		svg // Select the <svg> element you want to render the chart in.
		.datum(data) // Populate the <svg> element with chart data...
		.call(chart); // Finally, render the chart!

		// Update the chart when window resizes.
		nv.utils.windowResize(function() {
			chart.update()
		});
		return chart;
	});
}

function updated() {

	console.log("updated");
	$("div[id^=chart-]").each(function(i, e) {
		console.log("Found Chart: " + i);
		$(e).find("script").each(function(i, s) {
			console.log('Evaluating new script');
			// eval($$(s).text());
		});
	});

}