package ee.ioc.phon.android.k6neleservice.activity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import ee.ioc.phon.android.k6neleservice.R;
import ee.ioc.phon.android.k6neleservice.databinding.AboutBinding;
import ee.ioc.phon.android.k6neleservice.utils.Utils;

public class AboutActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AboutBinding binding = AboutBinding.inflate(getLayoutInflater());
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
}
