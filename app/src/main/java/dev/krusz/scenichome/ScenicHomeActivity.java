package dev.krusz.scenichome;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** Temporary entry point for the build scaffold; the home UI replaces this in Task 5. */
public final class ScenicHomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView placeholder = new TextView(this);
        placeholder.setGravity(Gravity.CENTER);
        placeholder.setText(R.string.scaffold_message);
        setContentView(placeholder);
    }
}
