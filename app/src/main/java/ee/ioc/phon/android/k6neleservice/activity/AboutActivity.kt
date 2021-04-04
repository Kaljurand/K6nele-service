package ee.ioc.phon.android.k6neleservice.activity

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import ee.ioc.phon.android.k6neleservice.R
import ee.ioc.phon.android.k6neleservice.databinding.AboutBinding
import ee.ioc.phon.android.k6neleservice.utils.Utils

class AboutActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = AboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val ab = supportActionBar
        if (ab != null) {
            ab.setTitle(R.string.labelApp)
            ab.subtitle = "v" + Utils.getVersionName(this)
        }
        binding.tvAbout.movementMethod = LinkMovementMethod.getInstance()
        val about = String.format(
                getString(R.string.tvAbout),
                getString(R.string.labelApp)
        )
        binding.tvAbout.text = Html.fromHtml(about)
    }
}