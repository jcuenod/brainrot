package jcuenod.brainrot;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FontifyArrayAdapter extends ArrayAdapter<String> {
	private Typeface font;

    public FontifyArrayAdapter(Context context, int layoutResourceId, int textViewResourceId, String [] menuItems) 
    {
        super(context, layoutResourceId, textViewResourceId, menuItems);
        font = Typeface.createFromAsset(context.getAssets(), "fonts/FreeSerif.otf"); 
    }

	@Override  
	public View getView(int position, View view, ViewGroup viewGroup)
	{
		View v = super.getView(position, view, viewGroup);
		((TextView)v).setTypeface(font);
		return v;
	}
}
