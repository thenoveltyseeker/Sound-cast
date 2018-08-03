package com.talview.soundcast.view.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.talview.soundcast.R;
import com.talview.soundcast.view.ui.fragment.SongCollectionFragment;

/**
 * Main entry point of the app.
 *
 * @author Rolbin.
 */
public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		//Load initial page
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
				SongCollectionFragment.instantiate(this, SongCollectionFragment.class.getName()), null).commit();
	}
}
