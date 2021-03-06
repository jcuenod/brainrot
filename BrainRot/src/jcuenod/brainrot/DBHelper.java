package jcuenod.brainrot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String LOG_TAG = "BrainRot DBH";
	
    // If you change the database schema, you must increment the database version.
	private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "BrainRot.db";
    
    private static final String TBL_CARDS = "cards";
    private static final String COL_CARD_ID = "card_id";
    private static final String COL_SIDE_ONE = "side_one";
    private static final String COL_SIDE_TWO = "side_two";
    private static final String COL_SIDE_ONE_TRANSLITERATION = "side_one_transliteration";
    private static final String COL_DISPLAY_COUNT = "display_count";
    private static final String COL_RANKING = "ranking"; //from 1 to 11 - based on pimsleur's 11 ranks
    private static final String COL_LAST_SEEN = "last_seen";
    private static final String COL_NEXT_DUE = "next_due";
    //private static final String COL_PACK_ID = "pack_id"; //listed under tbl_packs

    private static final String TBL_PACKS = "packs";
    private static final String COL_PACK_ID = "pack_id";
    private static final String COL_PACK_NAME = "pack_name";

    private static final String TBL_PACK_CARDS = "packcards";
//    private static final String COL_PACK_ID = "pack_id"; //listed under tbl_packs
//    private static final String COL_CARD_ID = "card_id"; //listed under tbl_cards
    
    
    private static final String [] SQL_CREATE_ENTRIES = { 
    		"CREATE TABLE " + TBL_CARDS + " (" +
    		COL_CARD_ID + " INTEGER PRIMARY KEY," +
    		COL_SIDE_ONE + " TEXT, " +
    		COL_SIDE_TWO + " TEXT, " +
    		COL_SIDE_ONE_TRANSLITERATION + " TEXT, " +
    		COL_DISPLAY_COUNT + " INTEGER, " +
    		COL_RANKING + " INTEGER, " +
    		COL_LAST_SEEN + " INTEGER," +
    		COL_NEXT_DUE + " INTEGER" +
    		")",
    		"CREATE TABLE " + TBL_PACKS + " (" +
    		COL_PACK_ID + " INTEGER PRIMARY KEY," +
    		COL_PACK_NAME + " TEXT" +
    		")",
    		"CREATE TABLE " + TBL_PACK_CARDS + " (" +
					COL_PACK_ID + " INTEGER ," +
					COL_CARD_ID + " INTEGER, " +
    				"PRIMARY KEY (" +
    					COL_PACK_ID + ", " +
    					COL_CARD_ID +
    				")" +
    		")"
    };
    
    private static final String [] SQL_DELETE_ENTRIES = {
    	    "DROP TABLE IF EXISTS " + TBL_CARDS,
    	    "DROP TABLE IF EXISTS " + TBL_PACKS
    };

    
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
    	for (String entry : SQL_CREATE_ENTRIES)
    	{
    		db.execSQL(entry);
    	}
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if (oldVersion <= 7)
    	{
    		SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-hhmmss");
    		this.copyDB(new File(db.getPath()), new File(Environment.getExternalStorageDirectory().getPath() + "/brainrot-bck-" + s.format(new Date()) + ".db"));
    		db.execSQL(SQL_CREATE_ENTRIES[2]);
//    		"CREATE TABLE " + TBL_PACK_CARDS + " (" +
//					COL_PACK_ID + " INTEGER ," +
//					COL_CARD_ID + " INTEGER, " +
//    				"PRIMARY KEY (" +
//    					COL_PACK_ID + " INTEGER ," +
//    					COL_CARD_ID + " INTEGER" +
//    				")" +
//    		")"
    		
        	ContentValues values = new ContentValues();
        	values.put(COL_PACK_NAME, "First Pack");
        	Log.v(LOG_TAG, "adding first pack...");
        	long packId = db.insert(
        	         TBL_PACKS,
        	         COL_PACK_ID,
        	         values);
        	
    		db.execSQL("INSERT INTO " + TBL_PACK_CARDS + " (" + COL_PACK_ID + ", " + COL_CARD_ID + ") SELECT " + packId + ", " + COL_CARD_ID + " FROM " + TBL_CARDS); 
    	}
    	if (oldVersion <= 8)
    	{
    		SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-hhmmss");
    		this.copyDB(new File(db.getPath()), new File(Environment.getExternalStorageDirectory().getPath() + "/brainrot-bck-" + s.format(new Date()) + ".db"));
    		db.execSQL("ALTER TABLE " + TBL_CARDS + 
    				" ADD COLUMN " + COL_SIDE_ONE_TRANSLITERATION + " TEXT"
    		);
        	// Define a projection that specifies which columns from the database
        	// you will actually use after this query.
        	String[] projection = {
        	    COL_CARD_ID,
        	    COL_SIDE_ONE,
        	    };
        
        	Cursor c = db.query(
        	    TBL_CARDS,  // The table to query
        	    projection, // The columns to return
        	    null,       // The columns for the WHERE clause
        	    null,       // The values for the WHERE clause
        	    null,       // don't group the rows
        	    null,       // don't filter by row groups
        	    null		// The sort order
        	    );
        	c.moveToFirst();

        	int i = 0;
    		Log.v(LOG_TAG, "Running update...");
        	while (!c.isAfterLast())
        	{
        		ContentValues values = new ContentValues();
            	values.put(COL_SIDE_ONE_TRANSLITERATION, LanguageUtils.domagic(c.getString(c.getColumnIndexOrThrow(COL_SIDE_ONE))));

            	// Which row to update, based on the ID
            	String selection = COL_CARD_ID + " LIKE ?";
            	String[] selectionArgs = { String.valueOf(c.getInt(c.getColumnIndexOrThrow(COL_CARD_ID))) };

            	//int count = 
            	db.update(
            	    TBL_CARDS,
            	    values,
            	    selection,
            	    selectionArgs);
        		c.moveToNext();
        		if (i++ % 100 == 0)
        		{
            		Log.v(LOG_TAG, "(still busy running update)");
        		}
        	}
    	}
    	
    }
    public void truncateTables()
    {
    	SQLiteDatabase db = getWritableDatabase();
    	for (String entry : SQL_DELETE_ENTRIES)
    	{
    		db.execSQL(entry);
    	}
        onCreate(db);
    }
    
    public int getPackId(String pack)
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    COL_PACK_ID
    	    };
    	// Define 'where' part of query.
    	String selection = COL_PACK_NAME + " LIKE ( ? )";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { pack };
    	Cursor c = db.query(
    	    TBL_PACKS,  // The table to query
    	    projection,                               // The columns to return
    	    selection,
    	    selectionArgs,
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    null                                 // The sort order
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();
    	Log.v(LOG_TAG, "packid for " + pack + ": " + c.getCount());
    	if (c.getCount() > 0)
    	{
    		int packId = c.getInt(c.getColumnIndexOrThrow(COL_PACK_ID));
        	return packId;
    	}
    	else
    	{
    		return -1;
    		//TODO: create new pack with this name
    	}
    }
    public int getPackId(FlashCard card)
    {
    	return 0;
    }
    public long createPack(String packName)
    {
    	SQLiteDatabase db = this.getWritableDatabase();//getReadableDatabase();

    	ContentValues values = new ContentValues();
    	values.put(COL_PACK_NAME, packName);
    	Log.v(LOG_TAG, "adding Question with pack id: values prepared");

    	// Insert the new row, returning the primary key value of the new row
    	long newRowId = db.insert(
    	         TBL_PACKS,
    	         COL_PACK_ID,
    	         values);
    	Log.v(LOG_TAG, "New Pack: " + String.valueOf(newRowId));
    	return newRowId;
    }

    
    public long addCard(FlashCard card, int packId)
    {
    	Log.v(LOG_TAG, "adding Question with pack id");
    	// Gets the data repository in write mode
    	SQLiteDatabase db = getWritableDatabase();

    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(COL_SIDE_ONE, card.getSideOne());
    	values.put(COL_SIDE_TWO, card.getSideTwo());
    	values.put(COL_SIDE_ONE_TRANSLITERATION, LanguageUtils.domagic(card.getSideOne()));
    	values.put(COL_DISPLAY_COUNT, card.getDisplayCount());
    	values.put(COL_RANKING, card.getRanking());
    	values.put(COL_LAST_SEEN, card.getLastSeen());
    	values.put(COL_NEXT_DUE, card.getLastSeen());
    	Log.v(LOG_TAG, "adding Question with pack id: values prepared");

    	// Insert the new row, returning the primary key value of the new row
    	return db.insert(
    	         TBL_CARDS,
    	         COL_CARD_ID,
    	         values);
    }
    public long addCard(FlashCard card, String pack)
    {
    	return this.addCard(card, this.getPackId(pack));
    }
    
    public void storeCard(FlashCard card)
    {
    	SQLiteDatabase db = this.getWritableDatabase();

    	// New value for one column
    	ContentValues values = new ContentValues();
    	values.put(COL_SIDE_ONE, card.getSideOne()); //figure out effective way of getting changed values (note calls of "FC.setX"
    	values.put(COL_SIDE_TWO, card.getSideTwo());
    	values.put(COL_DISPLAY_COUNT, card.getDisplayCount());
    	values.put(COL_RANKING, card.getRanking());
    	values.put(COL_LAST_SEEN, card.getLastSeen());
    	values.put(COL_NEXT_DUE, card.getNextDue());

    	// Which row to update, based on the ID
    	String selection = COL_CARD_ID + " LIKE ?";
    	String[] selectionArgs = { String.valueOf(card.getCardId()) };

    	//int count = 
    	db.update(
    	    TBL_CARDS,
    	    values,
    	    selection,
    	    selectionArgs);
    }
    public FlashCard queryForCard()
    {
    	return queryForCard(false);
	}
    public FlashCard queryForCard(boolean showNewCards)
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    COL_CARD_ID,
    	    COL_SIDE_ONE,
    	    COL_SIDE_TWO,
    	    COL_DISPLAY_COUNT,
    	    COL_RANKING,
    	    COL_LAST_SEEN,
    	    COL_NEXT_DUE
    	    };

    	// How you want the results sorted in the resulting Cursor
    	String sortOrder = "(CASE WHEN (" + COL_NEXT_DUE + " < " + String.valueOf(System.currentTimeMillis()) + " AND " + COL_NEXT_DUE + " != 0 ) THEN 1 WHEN " + COL_DISPLAY_COUNT + " == 0 THEN 2 ELSE 3 END) ASC, "
    			+ COL_NEXT_DUE + " ASC, " + COL_LAST_SEEN + " ASC LIMIT 1";

    	//we must prioritise overdue cards and then, if none are overdue: show, if enabled, a new card or, as a last resort, the next due (but that should not be promoted)
    	//TODO:should not be promoted (see previous line - the end)
    	//String selection = "(" + COL_NEXT_DUE + " < ? AND " + COL_NEXT_DUE + " != ? )"
    	//		+ "OR " + (showNewCards ? null : COL_DISPLAY_COUNT + " > ?");
    	// Specify arguments in placeholder order.
    	//String[] selectionArgs = { String.valueOf(System.currentTimeMillis()), "0" };
    	
    	String selectionWithoutNew = COL_DISPLAY_COUNT + " != ?";
    	String [] selectionArgsWithoutNew  = { String.valueOf(0) };
    
    	Cursor c = db.query(
    	    TBL_CARDS,  // The table to query
    	    projection,                               // The columns to return
    	    showNewCards ? null : selectionWithoutNew, // The columns for the WHERE clause
    	    showNewCards ? null : selectionArgsWithoutNew,  // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    sortOrder                                 // The sort order
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();

    	if (c.getCount() > 0)
    	{
    		int cardId = c.getInt(c.getColumnIndexOrThrow(COL_CARD_ID));
    		String sideOne = c.getString(c.getColumnIndexOrThrow(COL_SIDE_ONE));
    		String sideTwo = c.getString(c.getColumnIndexOrThrow(COL_SIDE_TWO));
    		int displayCount = c.getInt(c.getColumnIndexOrThrow(COL_DISPLAY_COUNT));
    		long lastSeen = c.getLong(c.getColumnIndexOrThrow(COL_LAST_SEEN));
    		long nextDue = c.getLong(c.getColumnIndexOrThrow(COL_NEXT_DUE));
    		int ranking = c.getInt(c.getColumnIndexOrThrow(COL_RANKING));
    		
        	return new FlashCard(cardId, sideOne, sideTwo, displayCount, lastSeen, nextDue, ranking);
    	}
    	else
    	{
    		return null;
    	}
    }
    public FlashCard queryForCard(int cardID) //by Index
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    COL_CARD_ID,
    	    COL_SIDE_ONE,
    	    COL_SIDE_TWO,
    	    COL_DISPLAY_COUNT,
    	    COL_RANKING,
    	    COL_LAST_SEEN,
    	    COL_NEXT_DUE
    	    };

    	String selection = COL_CARD_ID + " = ?";
    	String [] selectionArgs  = { String.valueOf(cardID) };
    
    	Cursor c = db.query(
    	    TBL_CARDS,   // The table to query
    	    projection,  // The columns to return
    	    selection,   // The columns for the WHERE clause
    	    selectionArgs, // The values for the WHERE clause
    	    null,        // don't group the rows
    	    null,        // don't filter by row groups
    	    null         // The sort order
    	    );
    	c.moveToFirst();

    	if (c.getCount() > 0)
    	{
    		int cardId = c.getInt(c.getColumnIndexOrThrow(COL_CARD_ID));
    		String sideOne = c.getString(c.getColumnIndexOrThrow(COL_SIDE_ONE));
    		String sideTwo = c.getString(c.getColumnIndexOrThrow(COL_SIDE_TWO));
    		int displayCount = c.getInt(c.getColumnIndexOrThrow(COL_DISPLAY_COUNT));
    		long lastSeen = c.getLong(c.getColumnIndexOrThrow(COL_LAST_SEEN));
    		long nextDue = c.getLong(c.getColumnIndexOrThrow(COL_NEXT_DUE));
    		int ranking = c.getInt(c.getColumnIndexOrThrow(COL_RANKING));

        	return new FlashCard(cardId, sideOne, sideTwo, displayCount, lastSeen, nextDue, ranking);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    public void query()
    {
    	
    }
    
    public void deleteCard(int cardId)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	// Define 'where' part of query.
    	String selection = COL_CARD_ID + " LIKE ?";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { String.valueOf(cardId) };
    	// Issue SQL statement.
    	db.delete(TBL_CARDS, selection, selectionArgs);
    }
    
    public long getSoonestDueMilliseconds()
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = { COL_NEXT_DUE };
    	// Define 'where' part of query.
    	String selection = COL_NEXT_DUE + " != ?";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { "0" };

    	// How you want the results sorted in the resulting Cursor
    	String sortOrder = COL_NEXT_DUE + " ASC LIMIT 1";

    	Cursor c = db.query(
    	    TBL_CARDS,  // The table to query
    	    projection,                               // The columns to return
    	    selection,                                // The columns for the WHERE clause
    	    selectionArgs,                            // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    sortOrder                                 // The sort order
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();
    	Log.v(LOG_TAG, "returned values: " + c.getCount());
    	if (c.getCount() > 0)
    	{
    		long nextDue = c.getLong(c.getColumnIndexOrThrow(COL_NEXT_DUE));
        	return nextDue;
    	}
    	else
    	{
        	Log.v(LOG_TAG, "None due");
    		return 0;
    	}
    }
    public int getDueInMilliseconds(long milliseconds)
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = { "COUNT(*) as counter" };
    	// Define 'where' part of query.
    	String selection = COL_NEXT_DUE + " != ? AND " + COL_NEXT_DUE + " < ?";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { "0",  Long.toString(milliseconds)};

    	Cursor c = db.query(
    	    TBL_CARDS,  // The table to query
    	    projection,                               // The columns to return
    	    selection,                                // The columns for the WHERE clause
    	    selectionArgs,                            // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    null                                 // The sort order
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();
    	Log.v(LOG_TAG, "returned values: " + c.getCount());
    	if (c.getCount() > 0)
    	{
    		int nextDue = c.getInt(c.getColumnIndexOrThrow("counter"));
        	return nextDue;
    	}
    	else
    	{
        	Log.v(LOG_TAG, "None due");
    		return 0;
    	}
    }
    public int getOverdueCount()
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = { COL_CARD_ID };
    	// Define 'where' part of query.
    	String selection = COL_NEXT_DUE + " < ? AND " + COL_NEXT_DUE + " != ?";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { String.valueOf(System.currentTimeMillis()), "0" };
    	
    	Cursor c = db.query(
    	    TBL_CARDS,  // The table to query
    	    projection,                               // The columns to return
    	    selection,                                // The columns for the WHERE clause
    	    selectionArgs,                            // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    null                                 // The sort order
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();
    	return c.getCount();
    }
    
    public ArrayList<PieChartDetails> getPieChartStats()
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = { COL_RANKING, "COUNT(*) as counter" };
    	
    	Cursor c = db.query(
    	    TBL_CARDS, // The table to query
    	    projection, // The columns to return
    	    null,
    	    null,
    	    COL_RANKING,
    	    null,
    	    null
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();
    	ArrayList<PieChartDetails> ret = new ArrayList<PieChartDetails>();
    	while (!c.isAfterLast())
    	{
    		ret.add(new PieChartDetails(
    			c.getInt(c.getColumnIndexOrThrow(COL_RANKING)),
    			c.getInt(c.getColumnIndexOrThrow("counter"))
    		));
    		c.moveToNext();
    	}
    	return ret;
    }
    public ArrayList<BubbleChartDetails> getScatterChartStats()
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// you will actually use after this query.
    	String[] projection = { COL_RANKING, COL_DISPLAY_COUNT, "COUNT(*) as counter" };
    	// Define 'where' part of query.
    	String selection = COL_RANKING + " != ? ";
    	// Specify arguments in placeholder order.
    	String[] selectionArgs = { "0" };
    	String groupby = COL_RANKING + ", " + COL_DISPLAY_COUNT;
    	
    	Cursor c = db.query(
    	    TBL_CARDS, // The table to query
    	    projection, // The columns to return
    	    selection,
    	    selectionArgs,
    	    groupby,
    	    null,
    	    null
    	    );
    	//Log.w(LOG_TAG, msg)
    	c.moveToFirst();
    	//ArrayList<ScatterChartCoords> ret = new ArrayList<ScatterChartCoords>();
    	ArrayList<BubbleChartDetails> ret = new ArrayList<BubbleChartDetails>();
    	while (!c.isAfterLast())
    	{
    		ret.add(new BubbleChartDetails(
    			c.getInt(c.getColumnIndexOrThrow(COL_RANKING)),
    			c.getInt(c.getColumnIndexOrThrow(COL_DISPLAY_COUNT)),
    			c.getInt(c.getColumnIndexOrThrow("counter"))
    		));
    		c.moveToNext();
    	}
    	return ret;
    }
    
    public boolean backupRestoreDB(File externalDB)
    {
		File destinationDB;
		File sourceDB;
		
    	File internalDB = new File(getReadableDatabase().getPath());
    	
    	if (externalDB.isDirectory())
    	{
    		Log.v(LOG_TAG, "Doing Backup (export)");
    		SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-hhmmss");
    		destinationDB = new File(externalDB.getPath() + "/brainrot-bck-" + s.format(new Date()) + ".db");
    		sourceDB = internalDB;
    	}
    	else
    	{
    		Log.v(LOG_TAG, "Doing Restore (import)");
    		Log.e(LOG_TAG, "Logic error: importing(restoring) has not yet been implemented");
    		return false;
    		//destinationDB = internalDB;
    		//sourceDB = externalDB;
    	}
    	return copyDB(sourceDB, destinationDB);
    }
    private boolean copyDB(File sourceDB, File destinationDB)
    {
    	try
    	{
    		destinationDB.createNewFile();
            if (destinationDB.canWrite())
            {
            	FileInputStream finstream = new FileInputStream(sourceDB);
                FileChannel src = finstream.getChannel();
                FileOutputStream foutstream = new FileOutputStream(destinationDB);
                FileChannel dst = foutstream.getChannel();
            	Log.v(LOG_TAG, "transferring");
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                finstream.close();
                foutstream.close();
                Log.i(LOG_TAG, "backup/restore done: " + sourceDB.getPath() +" -> " + destinationDB.getPath());
            }
            else
            {
            	Log.e(LOG_TAG, "couldn't write...");
            	return false;
            }
        }
    	catch (Exception e)
        {
        	Log.e(LOG_TAG, "Some kind of error in backup/restore:\n" + sourceDB.getPath() +" -> " + destinationDB.getPath() + "\n" + e.toString());
        	return false;
        }
    	return true;
    }
    
    public ArrayList<FlashCard> getCardHistory(int startAt, int limit)
    {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    COL_CARD_ID,
    	    COL_SIDE_ONE,
    	    COL_SIDE_TWO,
    	    COL_DISPLAY_COUNT,
    	    COL_RANKING,
    	    COL_LAST_SEEN,
    	    COL_NEXT_DUE
    	    };

    	// How you want the results sorted in the resulting Cursor
    	String sortOrder = COL_LAST_SEEN + " DESC LIMIT " + String.valueOf(startAt) + ", "+ String.valueOf(limit);
    
    	Cursor c = db.query(
    	    TBL_CARDS,  // The table to query
    	    projection, // The columns to return
    	    null,       // The columns for the WHERE clause
    	    null,       // The values for the WHERE clause
    	    null,       // don't group the rows
    	    null,       // don't filter by row groups
    	    sortOrder   // The sort order
    	    );
    	c.moveToFirst();
    	
    	ArrayList<FlashCard> ret = new ArrayList<FlashCard>();
    	while (!c.isAfterLast())
    	{
    		int cardId = c.getInt(c.getColumnIndexOrThrow(COL_CARD_ID));
    		String sideOne = c.getString(c.getColumnIndexOrThrow(COL_SIDE_ONE));
    		String sideTwo = c.getString(c.getColumnIndexOrThrow(COL_SIDE_TWO));
    		int displayCount = c.getInt(c.getColumnIndexOrThrow(COL_DISPLAY_COUNT));
    		long lastSeen = c.getLong(c.getColumnIndexOrThrow(COL_LAST_SEEN));
    		long nextDue = c.getLong(c.getColumnIndexOrThrow(COL_NEXT_DUE));
    		int ranking = c.getInt(c.getColumnIndexOrThrow(COL_RANKING));
    		
    		ret.add(new FlashCard(cardId, sideOne, sideTwo, displayCount, lastSeen, nextDue, ranking));
    		c.moveToNext();
    	}
    	return ret;
    }
}