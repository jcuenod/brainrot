package jcuenod.brainrot;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import bin.classes.com.ipaulpro.afilechooser.utils.*;

public class MainActivity extends Activity {
	private static final String LOG_TAG = "BrainRot Main";
	private static final int FILECHOOSER_REQUEST_IMPORT_CODE = 1;
	private static final int FILECHOOSER_REQUEST_BACKUPRESTORE_CODE = 2;
	private static final int ALARM_REQUEST_CODE = 2;
	public static final String FROM_NOTIFICATION = "fromnotification";

	protected BroadcastReceiver broadcastReceiver;
	protected AlarmManager alarmManager;
	protected FlashCard currentCard;
	protected DBHelper db;
	protected boolean learnNew;

	private ArrayList<FlashCard> cardHistory;
	
	public static boolean isInForeground = false;
	
	interface Response
	{
		void respond(int which);
	}

	@Override
	protected void onResume(){
		isInForeground = true;
		 
		super.onResume();
	}
	@Override
	protected void onPause(){
		isInForeground = false;
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		alarmManager.set(AlarmManager.RTC_WAKEUP, db.getSoonestDueMilliseconds(), getSyncPendingIntent(this));
		super.onPause();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//TODO: manual set ranking
		//TODO: onboot - restore alarm (or just show notification)
		//TODO: retain mode vs expand mode (newcards: true or false) "Learn new words [checkbox]"
		//TODO: revise mode vs test mode {i.e. just go over the due cards and exit or let me practice everything}
		//TODO: make promotion dependent on time gap allocation (so that I don't see one card 30 times in a morning and never see it for two years)
		//TODO: stats (graph showing [display_count || group by word(count)] & [ranking{from 0?} || group by word(count)] & [due date || word(count)] & single word stats
		//TODO: show number of words overdue (before notification goes away)
		//TODO: splash screen that prevents user error when no cards (or cards imported)
		//TODO: mp3 ping notification (ala sms)
		//TODO: previous card (history)
		//TODO: support packs
		//TODO: search cards (either side)
		/*
		 * currentCard menu:
		 * - set ranking
		 * - edit?
		 * - delete
		 * - stats
		 * */
		super.onCreate(savedInstanceState);

		setupAlarmStuff();
		setContentView(R.layout.activity_main);
		View.OnLongClickListener vonclick = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view)
			{
				showCardMenu();
				return false;
			}
		};
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/FreeSerif.otf");
		TextView text;
		text = (TextView) findViewById(R.id.txt_sideone);
		text.setTypeface(tf);
		text.setOnLongClickListener(vonclick);
		text = (TextView) findViewById(R.id.txt_sidetwo);
		text.setTypeface(tf);
		text.setOnLongClickListener(vonclick);
		
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		learnNew = app_preferences.getBoolean("learnNew", false);
		
		db = new DBHelper(getApplicationContext());
	}
	
	protected void setupAlarmStuff()
	{
        //registerReceiver(new DueCardBroadcastReceiver(), new IntentFilter("jcuenod.brainrot.ACTION_ALARM"));
     	alarmManager = (AlarmManager)(this.getSystemService( ALARM_SERVICE ));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showCardMenu() {
		// Inflate the menu; this adds items to the action bar if it is present.
		String [] menuoptions = {"Card Stats", "Set Ranking", "Edit Card", "Delete Card"};
		Response [] r = new Response[4];
		r[0] = new Response() {
			public void respond(int which) //Show Stats
			{
				Toast.makeText(getApplicationContext(), "Individual card stats not yet implemented", Toast.LENGTH_SHORT).show();
			}
		};
		r[1] = new Response() {
				public void respond(int which) //Set Ranking
				{
					String [] timings = new String[FlashCard.PIMSLEUR_TIMINGS.length -1];  
            		for (int i = 0; i < FlashCard.PIMSLEUR_TIMINGS.length -1; i++)
            		{
            			timings[i] = String.valueOf(i + 1) + ": " + FlashCard.PIMSLEUR_TIMINGS_STRING[i+1];
            		}
            		Response [] r = new Response[1];
            		r[0] = new Response() {
    	   				public void respond(int which)
    	   				{
    	   					Log.v(LOG_TAG, "rank selected: which=" + String.valueOf(which));
    	   					do_promote(which + 1);
    	   					Toast.makeText(getApplicationContext(), "Rank for '" + currentCard.getSideOne() + "' set: " + currentCard.getRanking(), Toast.LENGTH_SHORT).show();
    	   					showCard();
    	   				}
    	   			};
    	   			build_dialog("Set Rank", timings, r);
				}
			};
		r[2] = new Response() {
				public void respond(int which) //Edit Card
				{
					Toast.makeText(getApplicationContext(), "Edit Card not yet implemented", Toast.LENGTH_SHORT).show();
				}
			};
		r[3] = new Response() {
				public void respond(int which) //Delete Card
				{
					db.deleteCard(currentCard.getCardId());
					showCard();
				}
			};
			
		build_dialog("Card Options", menuoptions, r);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
        	case R.id.action_search:
        		Toast.makeText(this, "Search not yet implemented...", Toast.LENGTH_SHORT).show();
        		return true;
        	case R.id.action_cardhistory:
        		cardHistory = db.getCardHistory();
        		String [] previousCards = new String[cardHistory.size()];
        		Log.v(LOG_TAG, "here we go: 0-" + cardHistory.size());
        		for (int i = 0; i < cardHistory.size(); i++)
        		{
        			previousCards[i] = String.valueOf(i) + ": " + cardHistory.get(i).getSideOne();
        		}
        		Response [] r = new Response[1];
        		r[0] = new Response() {
	   				public void respond(int which)
	   				{
	   					do_previousCard(which);
	   				}
	   			};
	   			build_dialog("Card History", previousCards, r);
        		return true;
        	case R.id.action_statistics:
				Intent statintent = new Intent(getApplicationContext(), Statistics.class);
			    startActivity(statintent);
        		return true;
        	case R.id.action_nextdue:
        		DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        		// Create a calendar object that will convert the date and time value in milliseconds to date.
        		Calendar calendar = Calendar.getInstance();
        		calendar.setTimeInMillis(db.getSoonestDueMilliseconds());
        		Toast.makeText(this, "Next Due Time:\n" + formatter.format(calendar.getTime()), Toast.LENGTH_LONG).show();
        		return true;
        	case R.id.action_learnnew:
        		item.setChecked(!item.isChecked());
        		learnNew = item.isChecked();
        		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        		SharedPreferences.Editor editor = app_preferences.edit();
        		editor.putBoolean("learnNew", learnNew).commit();
        		return true;
        	case R.id.action_import:
        		do_import();
        		return true;
        	case R.id.action_export:
        		do_backup();
        		return true;
        	case R.id.action_emptydb:
        		db.truncateTables();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case FILECHOOSER_REQUEST_IMPORT_CODE:
				// If the file selection was successful
				if (resultCode == RESULT_OK) {		
					if (data != null) {
						// Get the URI of the selected file
						final Uri uri = data.getData();

						try {
							// Create a file instance from the URI
							final File file = FileUtils.getFile(uri);
							Log.v(LOG_TAG, "got file from uri... now continuing import");
							continue_import(file);
						} catch (Exception e) {
							Log.e(LOG_TAG, "File select error: " + e.toString(), e);
						}
					}
				}
				break;
			case FILECHOOSER_REQUEST_BACKUPRESTORE_CODE:
				Toast.makeText(this, "weird - check logcat, shouldn't have made it here. What were you doing?", Toast.LENGTH_SHORT).show();
				Log.i(LOG_TAG, "Shouldn't have made it here (FILECHOOSER_REQUEST_BACKUPRESTORE_CODE)");
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
		
	@Override
	protected void onNewIntent (Intent intent)
	{
		if (intent.hasExtra(FROM_NOTIFICATION))
		{
			showCard();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		alarmManager.set(AlarmManager.RTC_WAKEUP, db.getSoonestDueMilliseconds(), getSyncPendingIntent(this));
		db.close();
		super.onDestroy();
	}
	
	public void do_flip (View view)
	{
		ViewSwitcher vs_buttonrow = (ViewSwitcher) findViewById(R.id.vs_buttonrow);
		vs_buttonrow.showNext();
		ViewSwitcher vs_cardsides = (ViewSwitcher) findViewById(R.id.vs_cardsides);
		vs_cardsides.showNext();
	}
	
	public void btn_beginlearning_clicked(View view)
	{
		showCard();
	}
	
	public void btn_flipback_clicked (View view)
	{
		do_flip(view);
	}
	public void btn_flipcard_clicked (View view)
	{
		do_flip(view);
	}
	
	public void btn_wrong_clicked (View view)
	{
		do_wrong();
	}
	public void btn_right_clicked (View view)
	{
		do_right();
	}
	
	public void do_import()
	{
		// Use the GET_CONTENT intent from the utility class
		Intent target = FileUtils.createGetContentIntent();
		// Create the chooser Intent
		Intent intent = Intent.createChooser(
				target, getString(R.string.choose_file));
		try {
			startActivityForResult(intent, FILECHOOSER_REQUEST_IMPORT_CODE);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}
	}
	public void continue_import(File file)
	{
		//TODO: if type = .db then it's a restore not an import...??
		Log.v(LOG_TAG, "continuing import with " + file.toString() + "...");
		
		ArrayList<FlashCard> result;
		try
		{
			ImportAsyncTask task = new ImportAsyncTask(this);
			Log.v(LOG_TAG, "task created, now to execute...");
			task.execute(file);
		}
		catch (Exception e)
		{
			Log.v(LOG_TAG, "error with asynctask: " + e.toString());
		}
	}
	public void do_backup()
	{
		if (db.backupRestoreDB(new File("/sdcard/")))
		{
    		Toast.makeText(this, "Database successfully backed up to /sdcard/brainrot-bck-xxxx.db", Toast.LENGTH_SHORT).show();
		}
		else
		{
    		Toast.makeText(this, "Sorry, something went wrong with the backup.\nTry checking Logcat", Toast.LENGTH_SHORT).show();
		}
	}
	public void do_delete()
	{
		db.deleteCard(currentCard.getCardId());
		showCard();
	}
	
	protected void do_wrong()
	{
		do_promote(false);
		showCard();
	}
	protected void do_right()
	{
		do_promote(true);
		showCard();
	}
	protected void do_previousCard(int index)
	{
		Log.v(LOG_TAG, cardHistory.get(index).getSideOne() + " (of " + cardHistory.size() + ")");
		showCard(cardHistory.get(index).getCardId());		
	}

	protected FlashCard getCard(boolean includeNew)
	{
		return db.queryForCard(includeNew);
	}
	protected void showCard(int cardID)
	{
		currentCard = db.queryForCard(cardID);
		if (currentCard == null)
		{
			Toast.makeText(getApplicationContext(), "No such card", Toast.LENGTH_LONG).show();
			ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.vs_welcome);
			vs.setDisplayedChild(0);
			return;
		}
		do_actualDisplay();
	}
	protected void showCard()
	{
		currentCard = getCard(learnNew);
		if (currentCard == null)
		{
			if (learnNew)
			{
				Toast.makeText(getApplicationContext(), "No Cards: Try importing", Toast.LENGTH_LONG).show();
				ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.vs_welcome);
				vs.setDisplayedChild(0);
				return;
			}
			else
			{
				Toast.makeText(getApplicationContext(), "No cards to display (try learning new cards)", Toast.LENGTH_LONG).show();
				ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.vs_welcome);
				vs.setDisplayedChild(0);
				return;
			}
		}
		do_actualDisplay();
	}
	public void do_actualDisplay()
	{
		TextView txtSideOne = (TextView) findViewById(R.id.txt_sideone);
		TextView txtSideTwo = (TextView) findViewById(R.id.txt_sidetwo);
		txtSideOne.setText(Html.fromHtml(currentCard.getSideOne()));
		txtSideTwo.setText(Html.fromHtml(currentCard.getSideTwo()));
		
		TextView txtOverdueCount = (TextView) findViewById(R.id.txt_overdue_count);
		int overdue = db.getOverdueCount();
		txtOverdueCount.setText(overdue > 0 ? "Overdue: " + overdue : "");
		
		ViewSwitcher vs;
		vs = (ViewSwitcher) findViewById(R.id.vs_cardsides);
		vs.setDisplayedChild(0);
		vs = (ViewSwitcher) findViewById(R.id.vs_buttonrow);
		vs.setDisplayedChild(0);
		vs = (ViewSwitcher) findViewById(R.id.vs_welcome);
		vs.setDisplayedChild(1);
		Log.v(LOG_TAG, "card displayed");
	}
	
	public void do_promote(int ranking)
	{
		currentCard.setRanking(ranking);
		finish_promote();
	}
	public void do_promote(boolean promotion)
	{
		currentCard.promote(promotion);
		finish_promote();
		
	}
	private void finish_promote()
	{
		Log.v(LOG_TAG, "Card: '" + currentCard.getSideOne() + "' promoted to rank " + currentCard.getRanking());
		currentCard.incrementDisplayCount();
		currentCard.setLastSeen(System.currentTimeMillis());
		String f = "";
		for (long j : FlashCard.PIMSLEUR_TIMINGS)
		{
			f += " :: " + j;
		}
		Log.v(LOG_TAG, "timings" + f);
		currentCard.setNextDue(System.currentTimeMillis() + FlashCard.PIMSLEUR_TIMINGS[currentCard.getRanking()]);
		db.storeCard(currentCard);
		//alarmManager.cancel(getSyncPendingIntent(this));
		long soonestDueMilliseconds = db.getSoonestDueMilliseconds();
		alarmManager.set(AlarmManager.RTC_WAKEUP, soonestDueMilliseconds, getSyncPendingIntent(this));
		if (System.currentTimeMillis() < soonestDueMilliseconds)
		{
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		}
	}
	
	private void build_dialog(String title, String [] options, Response [] responses)
	{
		final Response[] responses_g = responses;
		Log.v(LOG_TAG, "AlertDialog options: " + options.toString());
				
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(title);
/*		builder.setAdapter(new FontifyArrayAdapter(this, R.layout.statistics, R.id.textview_unidlg, options), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
				Log.v(LOG_TAG, "AlertDialog option: which=" + which);
				responses_g[responses_g.length > 1 ? which : 0].respond(which);

            }
        });*/
			
		builder.setItems(options, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.v(LOG_TAG, "AlertDialog option: which=" + which);
				responses_g[responses_g.length > 1 ? which : 0].respond(which);
			}
		});
		builder.setInverseBackgroundForced(true);
		builder.create();		
		builder.show();
		
//		TextView tv = (TextView) findViewById(R.id.textview_unidlg);
//		Log.v(LOG_TAG, "face:" + face.toString());
//		Log.v(LOG_TAG, "tv:" + tv.toString());
//		tv.setTypeface(face);
		
		
//		AlertDialog thedialog = 
//		
//		ListView lv = (ListView) thedialog.findViewById(android.R.id.text1);
//		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/FreeSerif.otf");
//		for (int i = 0; i < options.length; i++)
//		{
//			TextView tv = (TextView) lv.getItemAtPosition(i);
//			tv.setTypeface(face);
//		}
	}
	
	public static PendingIntent getSyncPendingIntent(Context context)
	{
		Intent i = new Intent("jcuenod.brainrot.ACTION_ALARM");
		PendingIntent pi = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, i, 0);
        return pi;
	}
}