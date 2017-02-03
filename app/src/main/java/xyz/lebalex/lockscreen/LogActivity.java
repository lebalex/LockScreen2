package xyz.lebalex.lockscreen;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LogActivity extends AppCompatActivity {
    private  LogActivity ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView logstring = (TextView) findViewById(R.id.logstring);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        logstring.setText(sp.getString("logs",""));



        TextView logtitle = (TextView) findViewById(R.id.logtitle);
        logtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
                String str = sp.getString("logs", "");
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("logs", "");
                editor.commit();
                TextView logstring = (TextView) findViewById(R.id.logstring);
                logstring.setText("");

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
