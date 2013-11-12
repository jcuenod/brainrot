package jcuenod.brainrot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class ImportAsyncTask extends AsyncTask<File, Integer, Integer> {
	protected DBHelper db;
	public static final int DIALOG_IMPORT_PROGRESS = 0;
	private static final String LOG_TAG = "BrainRot ImportAsyncTask";
	private ProgressDialog pd;
	private Activity parentActivity;
	
	public ImportAsyncTask(Activity act)
	{
		super();
		this.parentActivity = act;
		Log.v(LOG_TAG, "preexecute constructed");
	}
	
	@Override
    protected void onPreExecute() {
		super.onPreExecute();

		pd = new ProgressDialog(parentActivity);
        pd.setTitle("Importing Flash Cards");
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.setMax(0);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();
		db = new DBHelper(parentActivity);
    }
	
    @Override
    protected Integer doInBackground(File... file) {
		try {
        	try {
    			
    		    BufferedReader br = new BufferedReader(new FileReader(file[0]));
    		    FlashCard f;
    		    ArrayList<FlashCard> cards = new ArrayList<FlashCard>();
    		    ArrayList<String> packs = new ArrayList<String>();
    		    String line;

    		    while ((line = br.readLine()) != null)
    		    {
    		        String [] flashcarddata = line.split("::"); //structured as "sideone::sidetwo::str_packname"
    		        f = new FlashCard(0,flashcarddata[0],flashcarddata[1],0,0,0,0);
    		        cards.add(f);
    		        packs.add(new String(flashcarddata[2]));
    			}
    		    br.close();
    		    
    		    pd.setMax(cards.size());
    			for (int i = 0; i < cards.size(); i++)
    			{
        			db.addCard(cards.get(i), packs.get(i));
    				publishProgress(i);
    			}
    		}
    		catch (IOException e) {
    		    //You'll need to add proper error handling here
    			//can't imagine how I'd end up here... and best thing is just to carry on without doing anything
    			Log.i(LOG_TAG, "continue_import() failed because of an IOException: " + e.toString());
    		}
        } catch (Exception e) {
               // TODO Auto-generated catch block
        	Log.v(LOG_TAG, "Error over filereading : " + e.toString());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
    	db.close();
    	pd.dismiss();
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
    	pd.setProgress(values[0]);
    	super.onProgressUpdate(values);
    }
}
