package me.mamiiblt.instafel.updater;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.mamiiblt.instafel.updater.utils.LocalizationUtils;
import me.mamiiblt.instafel.updater.utils.LogUtils;
import me.mamiiblt.instafel.updater.utils.RootManager;

public class MainActivity extends AppCompatActivity {

    private TextView titleView;
    SharedPreferences prefsApp;
    SharedPreferences.Editor prefsEditor;
    LogUtils logUtils;
    LocalizationUtils localizationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsApp = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = prefsApp.edit();
        logUtils = new LogUtils(this);
        localizationUtils = new LocalizationUtils(getApplicationContext());
        localizationUtils.updateAppLanguage();

        if (!prefsApp.getBoolean("init", false)) {
            prefsEditor.putString("checker_arch", "NULL");
            prefsEditor.putString("checker_type", "NULL");
            prefsEditor.putBoolean("root_request_complete", false);
            prefsEditor.putBoolean("init", true);
            prefsEditor.apply();
        }

        if (prefsApp.getBoolean("material_you", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DynamicColors.applyToActivityIfAvailable(this);
            }
        } else {
            setTheme(R.style.Base_Theme_InstafelUpdater);
        }

        if (prefsApp.getString("checker_arch", "NULL").equals("NULL") || prefsApp.getString("checker_type", "NULL").equals("NULL")) {
            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(intent);
            finish();
        } else {

            EdgeToEdge.enable(this);

            setContentView(R.layout.activity_main);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(this.getString(R.string.dialog_title))
                            .setMessage(this.getString(R.string.dialog_desc))
                            .setPositiveButton(this.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 105);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            }

            titleView = findViewById(R.id.title);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            Context ctx = getApplicationContext();

            navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                @Override
                public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                    if (destination.getId() == R.id.nav_info) {
                        titleView.setText(ctx.getString(R.string.status));
                    } else if (destination.getId() == R.id.nav_logs) {
                        titleView.setText(ctx.getString(R.string.logs));
                    } else if (destination.getId() == R.id.nav_settings) {
                        titleView.setText(ctx.getString(R.string.settings));
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 105) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (RootManager.isDeviceRooted()) {
                    if (RootManager.requestRootPermission()) {
                        Toast.makeText(MainActivity.this, "Root access granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Root access rejected, please allow from manager.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "SU binary doesn't found in device, please root your device.", Toast.LENGTH_SHORT).show();
                }
                prefsEditor.putBoolean("root_request_complete", true);
                prefsEditor.apply();
                recreate();
            } else {
                Toast.makeText(this, "Please allow notification permission from App Info", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }
}