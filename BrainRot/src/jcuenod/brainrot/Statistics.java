package jcuenod.brainrot;

import java.util.ArrayList;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class Statistics extends Activity {
	private static final String LOG_TAG = "BrainRot Statistics";
	protected DBHelper db;
	GraphicalView mChartView = null;
	/*private DefaultRenderer mRenderer = new DefaultRenderer();
	private CategorySeries mSeries = new CategorySeries("Rotten Pie");
	private XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
	private XYMultipleSeriesDataset xySeries = new XYMultipleSeriesDataset();
	*/
	private final int[] COLORS = new int[] { 
	    Color.GREEN, Color.BLUE, Color.MAGENTA, Color.YELLOW, Color.RED, Color.DKGRAY, Color.BLACK};
	
	
	public static class ScatterChartCoords
	{
		double x;
		double y;
	}
	
	@Override  
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		
		Spinner spnGraphPicker = (Spinner) findViewById(R.id.spn_graphpicker);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.arr_str_graphoptions, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnGraphPicker.setAdapter(adapter);
		
		
		
        
        
		/*String html_value = "<html>	<head>	<script>	function draw()	{	var canvas = document.getElementById(\"can\");var ctx = canvas.getContext(\"2d\");	boxsize =   ctx.canvas.width  = window.innerWidth;  ctx.canvas.height = window.innerHeight;var lastend = 0;var datapie = [20,60,25,45,150,75];var myTotal = 0;var myColor = [\"485252\", \"697878\", \"9e9a99\", \"af9189\", \"ca806d\", \"dcaa78\", \"f4c190\", \"f6d6a1\", \"f8e5bd\", \"faedcd\", \"bdd179\"];for(var e = 0; e < datapie.length; e++){  myTotal += datapie[e];}for (var i = 0; i < datapie.length; i++) {ctx.fillStyle = myColor[i];ctx.beginPath();ctx.moveTo(canvas.width/2,canvas.height/2);ctx.arc(canvas.width/2,canvas.height/2,canvas.height/2,lastend,lastend+(Math.PI*2*(datapie[i]/myTotal)),false);ctx.lineTo(canvas.width/2,canvas.height/2);ctx.fill();lastend += Math.PI*2*(datapie[i]/myTotal);}}	</script>	</head>		<body onLoad=\"draw()\">	<canvas id=\"can\"></canvas>		</body></html>";
		WebView browser = (WebView) findViewById(R.id.wv_chart);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(html_value, "text/html", "UTF-8");*/
		
		/*spnGraphPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        //Object item = parent.getItemAtPosition(pos);
				switch (pos)
				{
				case 0://pie
					draw_rottenPie();
					break;
				case 1://dot
					draw_DotsOfRot();
					break;
				}
		    }
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});*/
		
		db = new DBHelper(getApplicationContext());
		
		Map<Integer, Integer> stats = db.getPieChartStats();

		String data = "";
		String colors = "";
		for (int key : stats.keySet())
		{
			data += stats.get(key) + ",";
			colors += key + ",";
		}
		Log.i(LOG_TAG, "data: " + data + " :: colors: " + colors);
		String html_value = "<html><head><script>function draw(){var canvas = document.getElementById(\"can\");var ctx = canvas.getContext(\"2d\");boxsize = window.innerWidth < window.innerHeight ? window.innerWidth : window.innerHeight;boxsize *= 0.8;  ctx.canvas.width  = boxsize;ctx.canvas.height = boxsize;var lastend = 0;var datapie = [" + data + "];var colors = [" + colors + "];var myTotal = 0;var myColor = [\"bdd179\", \"faedcd\", \"f8e5bd\", \"f6d6a1\", \"f4c190\", \"dcaa78\", \"d69c62\", \"ca806d\", \"af9189\", \"9e9a99\", \"697878\", \"485252\"];for(var e = 0; e < datapie.length; e++){myTotal += datapie[e];}for (var i = 0; i < datapie.length; i++) {ctx.fillStyle = myColor[colors[i]];ctx.beginPath();ctx.moveTo(canvas.width/2,canvas.height/2);ctx.arc(canvas.width/2,canvas.height/2,canvas.height/2,lastend,lastend+(Math.PI*2*(datapie[i]/myTotal)),false);ctx.lineTo(canvas.width/2,canvas.height/2);ctx.fill();lastend += Math.PI*2*(datapie[i]/myTotal);}}</script><style>canvas {margin: auto;padding: 0;}</style></head><body onLoad=\"draw()\"><canvas id=\"can\"></canvas></body></html>";
		
		WebView browser = (WebView) findViewById(R.id.wv_chart);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(html_value, "text/html", "UTF-8");
		//this.draw_rottenPie();
	}

	@Override
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}
	
	private void draw_rottenPie()
	{
//		mRenderer = new DefaultRenderer();
//		mRenderer.setApplyBackgroundColor(true);  
//		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));  
//		mRenderer.setChartTitleTextSize(20);  
//		mRenderer.setLabelsTextSize(15);  
//		mRenderer.setLegendTextSize(15);  
//		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });  
//		mRenderer.setZoomButtonsVisible(true);  
//		mRenderer.setStartAngle(90);  
//
//		for (int i = 0; i < VALUES.length; i++) {  
//			mSeries.add(NAME_LIST[i] + " " + VALUES[i], VALUES[i]);  
//			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();  
//			renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);  
//			mRenderer.addSeriesRenderer(renderer);  
//		}  
//
//		
		Map<Integer, Integer> stats = db.getPieChartStats();
		//String [] numberStrings = {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve"};

		String data = "";
		String colors = "";
		for (int key : stats.keySet())
		{
			data += stats.get(key) + ",";
			colors += key + ",";
		}
		Log.i(LOG_TAG, "data: " + data + " :: colors: " + colors);
		String html_value = "<html><head><script>function draw(){var canvas = document.getElementById(\"can\");ar ctx = canvas.getContext(\"2d\");boxsize = window.innerWidth < window.innerHeight ? window.innerWidth : window.innerHeight  ctx.canvas.width  = window.innerWidth;ctx.canvas.height = window.innerHeight;var lastend = 0;var datapie = ["+ data +"];var colors = ["+ colors +"];var myTotal = 0;var myColor = [\"485252\", \"697878\", \"9e9a99\", \"af9189\", \"ca806d\", \"dcaa78\", \"f4c190\", \"f6d6a1\", \"f8e5bd\", \"faedcd\", \"bdd179\"];for(var e = 0; e < datapie.length; e++){myTotal += datapie[e];}for (var i = 0; i < datapie.length; i++) {ctx.fillStyle = myColor[colors[i]];ctx.beginPath();ctx.moveTo(canvas.width/2,canvas.height/2);ctx.arc(canvas.width/2,canvas.height/2,canvas.height/2,lastend,lastend+(Math.PI*2*(datapie[i]/myTotal)),false);ctx.lineTo(canvas.width/2,canvas.height/2);ctx.fill();lastend += Math.PI*2*(datapie[i]/myTotal);}}</script></head><body onLoad=\"draw()\"><canvas id=\"can\"></canvas></body></html>";
		
		WebView browser = (WebView) findViewById(R.id.wv_chart);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(html_value, "text/html", "UTF-8");
		
		/*int total = 0;
		mSeries.clear();

		for (int key : stats.keySet())
		{
			total += stats.get(key);
		}
		for (int key : stats.keySet())
		{
		    mSeries.add(numberStrings[key] + " " + Math.round(((double)stats.get(key)/total)*100) + "%", stats.get(key));
		    
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
			renderer.setDisplayChartValues(true);
			mRenderer.addSeriesRenderer(renderer);
		}  
		mRenderer.setLabelsTextSize(25);
		mRenderer.setShowLegend(false);
		mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		mChartView.setLayoutParams(params);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rl_chart);
		layout.removeAllViews();
		layout.addView(mChartView);
		*/
	}
	public void draw_DotsOfRot()
	{
		//TODO: support packs here (each pack needs a title and the db will need to return info grouped by pack somehow...) c.f. later todo for more info
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5F);
	    renderer.setMargins(new int[] { 20, 30, 15, 20 });

		String[] titles = new String[] { "Display Count vs Ranking" };
		
	    XYSeriesRenderer r = new XYSeriesRenderer();
	    for (int i = 0; i < titles.length; i++)
	    {
	    	r.setColor(COLORS[i % COLORS.length]);
	    	r.setPointStyle(PointStyle.CIRCLE);
	    	renderer.addSeriesRenderer(r);
	    }
			    
	    renderer.setXLabels(10);
	    renderer.setYLabels(10);
	    for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
	      ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
	    }

		ArrayList<ScatterChartCoords> values = db.getScatterChartStats();	    
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    
	    int length = titles.length;
	    double xMax = 0;
	    double yMax = 0;
	    for (int i = 0; i < length; i++) {
	    	XYSeries series = new XYSeries(titles[i]);
			for (int j = 0; j < values.size(); j++)
	    	{
	    		xMax = xMax <  values.get(j).x ? values.get(j).x : xMax;
	    		yMax = yMax <  values.get(j).y ? values.get(j).y : yMax;
	    		series.add(values.get(j).x, values.get(j).y);
	    	}
	    	dataset.addSeries(series);
	    	Log.v(LOG_TAG, "series added (" + series.getItemCount() + ")");
	    }
	    

	    renderer.setXTitle("Ranking");
	    renderer.setYTitle("Display Count");
	    renderer.setXAxisMin(0);
	    renderer.setXAxisMax(xMax);
	    renderer.setYAxisMin(0);
	    renderer.setYAxisMax(yMax + 1);
	    renderer.setAxesColor(Color.GRAY);
	    renderer.setLabelsColor(Color.LTGRAY);
	    
		mChartView = ChartFactory.getScatterChartView(this, dataset, renderer);
//		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rl_chart);
	//	layout.removeAllViews();
		//layout.addView(mChartView);
		
		
		
//		mRenderer.setPanLimits(new double[] { 00, 600 , 00, 300 });     //xmin,xmax,ymin,ymax  bars/grids limit
//		mRenderer.setZoomLimits(new double[]{00, 200, 00, 30});     //xmin,xmax,ymin,ymax  zoom limit
//        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
	}

	
	/*
	
	@Override  
	protected void onResume() {  
		super.onResume();  
		if (mChartView == null) {  
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);  
			mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);  
			mRenderer.setClickEnabled(true);  
			mRenderer.setSelectableBuffer(10);  

			mChartView.setOnClickListener(new View.OnClickListener() {  
				@Override  
				public void onClick(View v) {  
					SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();  

					if (seriesSelection == null) {  
						Toast.makeText(AChartEnginePieChartActivity.this,"No chart element was clicked",Toast.LENGTH_SHORT).show();  
					} else {  
						Toast.makeText(AChartEnginePieChartActivity.this,"Chart element data point index "+ (seriesSelection.getPointIndex()+1) + " was clicked" + " point value="+ seriesSelection.getValue(), Toast.LENGTH_SHORT).show();  
					}  
				}  
			});  

			mChartView.setOnLongClickListener(new View.OnLongClickListener() {  
				@Override  
				public boolean onLongClick(View v) {  
					SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();  
					if (seriesSelection == null) {  
						Toast.makeText(AChartEnginePieChartActivity.this,"No chart element was long pressed", Toast.LENGTH_SHORT);  
						return false;   
					} else {  
						Toast.makeText(AChartEnginePieChartActivity.this,"Chart element data point index "+ seriesSelection.getPointIndex()+ " was long pressed",Toast.LENGTH_SHORT);  
						return true;         
					}  
				}  
			});  
			layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
		}  
		else {  
			mChartView.repaint();  
		}  
	}  
	*/
}
