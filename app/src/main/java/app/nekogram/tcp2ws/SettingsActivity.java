package app.nekogram.tcp2ws;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import rikka.material.app.MaterialActivity;

public class SettingsActivity extends MaterialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
        theme.applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true);
    }

    @Override
    public void onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars();
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private SettingsActivity mActivity;

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            findPreference("connect").setOnPreferenceClickListener(this);
            findPreference("notification_permission").setOnPreferenceClickListener(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mActivity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                findPreference("notification_permission").setVisible(true);
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            } else {
                findPreference("notification_permission").setVisible(false);
                startWsService();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                startWsService();
                findPreference("notification_permission").setVisible(false);
            }
        }

        private void startWsService() {
            ContextCompat.startForegroundService(mActivity, new Intent(mActivity, WsService.class));
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);

            mActivity = (SettingsActivity) getActivity();
        }

        @Override
        public void onDetach() {
            super.onDetach();

            mActivity = null;
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            if ("connect".equals(preference.getKey())) {
                var port = getPreferenceManager().getSharedPreferences().getString("port", "1080");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/socks?server=127.0.0.1&port=" + port)));
            } else if ("notification_permission".equals(preference.getKey())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
                }
            }
            return true;
        }
    }
}