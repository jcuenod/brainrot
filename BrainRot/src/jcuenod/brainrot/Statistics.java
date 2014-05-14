package jcuenod.brainrot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jcuenod.brainrot.R.array;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;

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
			
		spnGraphPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
		});
		
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
		ArrayList<PieChartDetails> stats = db.getPieChartStats();

		Map<String, String> variableInjection = new HashMap<String, String>();
		String data = "";
		String columns = "";
		String columnnames = "";
		for (PieChartDetails tempDetails : stats)
		{
			data += tempDetails.getCount() + ",";
			columns += tempDetails.getRanking() + ",";
			columnnames += "\"" + FlashCard.PIMSLEUR_TIMINGS_STRING[tempDetails.getRanking()] + "\",";
		}
		variableInjection.put("%VAR_DATA%", data);
		variableInjection.put("%VAR_COLUMNS%", columns);
		variableInjection.put("%VAR_COLUMNNAMES%", columnnames);
	
		String content = assetToString("www/piechart.html", variableInjection);
		
		WebView browser = (WebView) findViewById(R.id.wv_chart);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(content, "text/html", "UTF-8");
	}
	
	public void draw_DotsOfRot()
	{
		//TODO: Support packs here (each pack needs a title and the db will need to return info grouped by pack somehow...) c.f. later todo for more info
		//TODO: Add axis labels (for position and field)

		ArrayList<BubbleChartDetails> values = db.getScatterChartStats();
		
		String data = "";
		for (BubbleChartDetails tempDetails : values)
		{
			data += "{x:" + tempDetails.getRanking() + ", y:" + tempDetails.getDisplayCount() + ", z:" + tempDetails.getCounter() + "}, ";
		}

		Map<String, String> variableInjection = new HashMap<String, String>();
		variableInjection.put("%VAR_DATA%", data);
		
		String content = assetToString("www/bubblechart.html", variableInjection);
		
		
		WebView browser = (WebView) findViewById(R.id.wv_chart);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(content, "text/html", "UTF-8");
	}

//	@Override  
//	protected void onResume() {  
//    	mWebView.loadUrl("file:///android_asset/graphs/graph1.html");
//		super.onResume();  
//		  
//	}
	
	private String assetToString(String assetURI, Map<String, String> variableSubstitutions)
	{
		//TODO: does not support comments (double backslash), apparently... (probably because the string comes through as a single line)
		try {
			InputStream input = getAssets().open(assetURI);
	        // myData.txt can't be more than 2 gigs.
	        int size = input.available();
	        byte[] buffer = new byte[size];
	        input.read(buffer);
	        input.close();
	        String retval = new String(buffer);
	        
	        for (String key : variableSubstitutions.keySet())
	        {
	        	retval = retval.replaceAll(key, variableSubstitutions.get(key));
	        }
	        return retval;
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Failed: " + e);
			return "Error, check Logcat";
		}
	}
}
