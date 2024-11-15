package me.mamiiblt.instafel.updater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.color.DynamicColors;

public class SetupActivity extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        if (preferences.getBoolean("material_you", true) == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DynamicColors.applyToActivityIfAvailable(this);
            } else {
                Toast.makeText(this, "Your android version is below 12", Toast.LENGTH_SHORT).show();
            }
        } else {
            setTheme(R.style.Base_Theme_InstafelUpdater);
        }

        setContentView(R.layout.activity_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RadioGroup radioGroupArch = findViewById(R.id.architecture_radio_group);
        RadioGroup radioGroupInstallType = findViewById(R.id.install_type_radio_group);
        radioGroupArch.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_arm64) {
                editor.putString("checker_arch", "arm64-v8a (64-bit)");
            } else if (checkedId == R.id.radio_arm32) {
                editor.putString("checker_arch", "armeabi-v7a (32-bit)");
            }
            editor.apply();
        });

        radioGroupInstallType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_unclone) {
                editor.putString("checker_type", "Unclone");
            } else if (checkedId == R.id.radio_clone) {
                editor.putString("checker_type", "Clone");
            }
            editor.apply();
        });
    }

    public void next(View view) {

        if (!preferences.getString("checker_arch", "NULL").equals("NULL") && !preferences.getString("checker_type", "MULL").equals("NULL")) {

            // set app preferences too
            editor.putString("checker_interval", "4 hour");
            editor.apply();
            Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        } else {

            new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.warning))
                    .setMessage(this.getString(R.string.warning_desc))
                    .setNegativeButton(android.R.string.yes, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}