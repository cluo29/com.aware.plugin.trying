// a plugin adapted from com.aware.plugin.ambient_noise.provider.ambient_noise

package com.aware.plugin.trying;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }
}