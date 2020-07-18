package ee.ioc.phon.android.k6neleservice.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import ee.ioc.phon.android.k6neleservice.R;
import ee.ioc.phon.android.k6neleservice.databinding.MainBinding;
import ee.ioc.phon.android.k6neleservice.utils.Utils;

public class PermissionsRequesterActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(PermissionsRequesterActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_RECORD_AUDIO);

        MainBinding binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.labelApp);
            ab.setSubtitle("v" + Utils.getVersionName(this));
        }
        binding.tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        String about = String.format(
                getString(R.string.tvAbout),
                getString(R.string.labelApp)
        );
        binding.tvAbout.setText(Html.fromHtml(about));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted: just finish
                    // finish();
                    // TODO: show thank you note instead with link to settings
                } else {
                    // Permission not granted: explain the consequences and offer a link to the app settings
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    Utils.getLaunchIntentDialog(this, getString(R.string.promptPermissionRationale), intent).show();
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}
