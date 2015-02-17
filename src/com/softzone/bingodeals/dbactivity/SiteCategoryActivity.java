package com.softzone.bingodeals.dbactivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.softzone.bingodeals.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/*
 * show the category list for sites
 * cafe,food, etc.
 */

public class SiteCategoryActivity extends ListActivity {

	private static final String TAG_TYPE = "type";

	// categories of sites
	String type[] = { "Cafe Deals", "Food Deals", "Movie Theater Deals",
			"Shopping Deals" };

	// Array of integers points to images stored in /res/drawable/
	int[] image = new int[] { R.drawable.cafe, R.drawable.food,
			R.drawable.movie, R.drawable.shopping };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_list);

		// Each row in the list stores category and image
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i < 4; i++) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("type", type[i]);
			hm.put("image", Integer.toString(image[i]));
			list.add(hm);
		}

		// Keys used in Hashmap
		String[] keys = { "type", "image" };

		// Ids of views in listview_layout
		int[] ids = { R.id.name, R.id.list_image };

		// adapter to store each items
		// R.layout.list_category defines the layout of each item
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list,
				R.layout.list_category, keys, ids);

		setListAdapter(adapter);
	}

	/*
	 * get the position when click
	 */
	@Override
	protected void onListItemClick(ListView l, View view, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, view, position, id);

		// getting values from selected ListItem
		String type = null;

		System.out.println("type is: " + position);

		if (position == 0) {
			type = "cafe";
		} else if (position == 1) {
			type = "food";
		} else if (position == 2) {
			type = "movie_theater";
		} else if (position == 3) {
			type = "shopping_mall";
		}

		// Starting new intent (ListtypeSitesActivity)
		Intent in = new Intent(getApplicationContext(),
				ListCategorySitesActivity.class);
		// sending lid to next activity
		in.putExtra(TAG_TYPE, type);

		// starting new activity and expecting some response back
		startActivityForResult(in, 100);

	}

}
