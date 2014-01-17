package jcuenod.brainrot;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
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
	private Typeface unicodeface;

	private ArrayList<FlashCard> cardHistory;
	private int previousCardDialogPagination = 0;
	private int previousCardDialogPageSize = 10;
	
	public static boolean isInForeground = false;
	
	class DialogData
	{
		String title;
		Map<String, Response> options;
	}
	
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
		//TODO: onboot - restore alarm (or just show notification)
		//TODO: revise mode vs test mode {i.e. just go over the due cards and exit or let me practice everything}
		//TODO: make promotion dependent on time gap allocation (so that I don't see one card 30 times in a morning and never see it for two years)
		//TODO: stats (graph showing [display_count || group by word(count)] & [ranking{from 0?} || group by word(count)] & [due date || word(count)] & single word stats
		//TODO: show number of words overdue (before notification goes away)
		//TODO: support packs
		//TODO: search cards (either side)
		//TODO: fix font for unicode menus
		/*
		 * currentCard menu:
		 * - set ranking
		 * - edit?
		 * - delete
		 * - stats
		 * */
		super.onCreate(savedInstanceState);
		
		unicodeface = Typeface.createFromAsset(this.getAssets(), "fonts/FreeSerif.otf");

		setupAlarmStuff();
		setContentView(R.layout.activity_main);
		View.OnClickListener vortclick = new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				do_flip(arg0);
			}
		};
		View.OnLongClickListener vlongclick = new View.OnLongClickListener() {
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
		text.setOnClickListener(vortclick);
		text.setOnLongClickListener(vlongclick);
		text = (TextView) findViewById(R.id.txt_sidetwo);
		text.setTypeface(tf);
		text.setOnLongClickListener(vlongclick);
		text.setOnClickListener(vortclick);
		
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
		menu.findItem(R.id.action_learnnew).setChecked(learnNew);
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
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Confirm");
					builder.setMessage("Do you really want to delete this card?");
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							db.deleteCard(currentCard.getCardId());
							Log.v(LOG_TAG, "card deleted");
							showCard();
						}
					});
					builder.setNegativeButton("No", null);
					builder.create();
					builder.show();
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
        		previousCardDialogPagination = 0; 
        		previousCardDialog();
        		return true;
        	case R.id.action_statistics:
				Intent statintent = new Intent(getApplicationContext(), Statistics.class);
			    startActivity(statintent);
        		return true;
        	case R.id.action_nextdue:
        		DateFormat formatter = SimpleDateFormat.getDateTimeInstance();//new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
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
        		showCard();
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
		if (db.backupRestoreDB(new File(Environment.getExternalStorageDirectory().getPath())))
		{
    		Toast.makeText(this, "Database successfully backed up to /sdcard/brainrot-bck-xxxx.db", Toast.LENGTH_SHORT).show();
		}
		else
		{
    		Toast.makeText(this, "Sorry, something went wrong with the backup.\nTry checking Logcat", Toast.LENGTH_SHORT).show();
    		Log.e(LOG_TAG, "something went wrong with the backup (return value=false)");
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
		Log.v(LOG_TAG, "card displayed: " + LanguageUtils.domagic(currentCard.getSideOne()));
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
	
	private void previousCardDialog()
	{
		cardHistory = db.getCardHistory(previousCardDialogPagination * previousCardDialogPageSize, previousCardDialogPageSize);
		String [] previousCards = new String[cardHistory.size() + 1];
		for (int i = 0; i < cardHistory.size(); i++)
		{
			previousCards[i] = String.valueOf(i + 1) + ": " + cardHistory.get(i).getSideOne();
		}
		Response [] r = new Response[1];
		r[0] = new Response() {
			public void respond(int which)
			{
				if (which == previousCardDialogPageSize)
				{
					previousCardDialogPagination++;
					previousCardDialog();
				}
				else
				{
					do_previousCard(which);
				}
			}
		};
		previousCards[cardHistory.size()] = String.valueOf("Show Previous " + String.valueOf(previousCardDialogPageSize));
		
		build_dialog("Card History", previousCards, r, true);
	}

	private void build_dialog(String title, String [] options, Response [] responses)
	{
		build_dialog(title, options, responses, false);
	}
	private void build_dialog(String title, String [] options, Response [] responses, boolean needUnicode)
	{
		final Response[] responses_g = responses;
		Log.v(LOG_TAG, "AlertDialog options: " + options.toString());
				
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(title);			
		builder.setItems(options, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.v(LOG_TAG, "AlertDialog option: which=" + which);
				responses_g[responses_g.length > 1 ? which : 0].respond(which);
			}
		});
		builder.setInverseBackgroundForced(true);
		AlertDialog al = builder.create();
		if (needUnicode)
		{
			al.setOnShowListener(new OnShowListener() {

	            @Override
	            public void onShow(DialogInterface alert) {
	                ListView listView = ((AlertDialog)alert).getListView();
	                final ListAdapter originalAdapter = listView.getAdapter();

	                listView.setAdapter(new ListAdapter()
	                {

	                    @Override
	                    public int getCount() {
	                        return originalAdapter.getCount();
	                    }

	                    @Override
	                    public Object getItem(int id) {
	                        return originalAdapter.getItem(id);
	                    }

	                    @Override
	                    public long getItemId(int id) {
	                        return originalAdapter.getItemId(id);
	                    }

	                    @Override
	                    public int getItemViewType(int id) {
	                        return originalAdapter.getItemViewType(id);
	                    }

	                    @Override
	                    public View getView(int position, View convertView, ViewGroup parent) {
	                        View view = originalAdapter.getView(position, convertView, parent);
	                        TextView textView = (TextView)view;
	                        textView.setTypeface(unicodeface);
	                        return view;
	                    }

	                    @Override
	                    public int getViewTypeCount() {
	                        return originalAdapter.getViewTypeCount();
	                    }

	                    @Override
	                    public boolean hasStableIds() {
	                        return originalAdapter.hasStableIds();
	                    }

	                    @Override
	                    public boolean isEmpty() {
	                        return originalAdapter.isEmpty();
	                    }

	                    @Override
	                    public void registerDataSetObserver(DataSetObserver observer) {
	                        originalAdapter.registerDataSetObserver(observer);

	                    }

	                    @Override
	                    public void unregisterDataSetObserver(DataSetObserver observer) {
	                        originalAdapter.unregisterDataSetObserver(observer);

	                    }

	                    @Override
	                    public boolean areAllItemsEnabled() {
	                        return originalAdapter.areAllItemsEnabled();
	                    }

	                    @Override
	                    public boolean isEnabled(int position) {
	                        return originalAdapter.isEnabled(position);
	                    }
	                });
	            }
	        });
		}
		al.show();
	}
	
	public static PendingIntent getSyncPendingIntent(Context context)
	{
		Intent i = new Intent("jcuenod.brainrot.ACTION_ALARM");
		PendingIntent pi = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, i, 0);
        return pi;
	}
}