package jcuenod.brainrot;

import java.io.InputStream;
import java.util.Map;

import jcuenod.brainrot.R.array;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

@SuppressLint("SetJavaScriptEnabled")
public class Statistics extends Activity {
	private static final String LOG_TAG = "BrainRot Statistics";
	protected DBHelper db;
		
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
		this.draw_rottenPie();
	}

	@Override
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}
	
	private void draw_rottenPie()
	{
		Map<Integer, Integer> stats = db.getPieChartStats();

		String data = "";
		String columns = "";
		String columnnames = "";
		for (int key : stats.keySet())
		{
			data += stats.get(key) + ",";
			columns += key + ",";
			columnnames += "\"" + FlashCard.PIMSLEUR_TIMINGS_STRING[key] + "\",";
		}
	
		String content = assetToString("www/piechart.html")
			.replaceAll("%VAR_DATA%", data)
			.replaceAll("%VAR_COLUMNS%", columns)
			.replaceAll("%VAR_COLUMNNAMES%", columnnames);
		
		WebView browser = (WebView) findViewById(R.id.wv_chart);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(content, "text/html", "UTF-8");
	}
	
//	public void draw_DotsOfRot()
//	{
		//TODO: support packs here (each pack needs a title and the db will need to return info grouped by pack somehow...) c.f. later todo for more info
		/*XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
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
	    
		mChartView = ChartFactory.getScatterChartView(this, dataset, renderer);*/
//		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rl_chart);
	//	layout.removeAllViews();
		//layout.addView(mChartView);
		
		
		
//		mRenderer.setPanLimits(new double[] { 00, 600 , 00, 300 });     //xmin,xmax,ymin,ymax  bars/grids limit
//		mRenderer.setZoomLimits(new double[]{00, 200, 00, 30});     //xmin,xmax,ymin,ymax  zoom limit
//        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
//	}

	
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
	private String assetToString(String assetURI)
	{
		try {
			InputStream input = getAssets().open(assetURI);
	        // myData.txt can't be more than 2 gigs.
	        int size = input.available();
	        byte[] buffer = new byte[size];
	        input.read(buffer);
	        input.close();
	        return new String(buffer);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Failed: " + e);
			return "Error, check Logcat";
		}
	}
}
