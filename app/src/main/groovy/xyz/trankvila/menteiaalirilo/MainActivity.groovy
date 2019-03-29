package xyz.trankvila.menteiaalirilo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.amazon.device.messaging.ADM
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import xyz.trankvila.menteiaalirilo.utilities.Constants
import xyz.trankvila.menteiaalirilo.utilities.Session
import xyz.trankvila.menteiaalirilo.utilities.SilicanDate

@CompileStatic
class MainActivity extends Activity {
    private static final List<Integer> weekdayColors = [R.color.slate, R.color.lavender, R.color.carnation,
                                                        R.color.sapphire, R.color.ruby, R.color.amber, R.color.fern]
    private static final List<Integer> phaseColors = [R.color.chaos, R.color.serenity, R.color.fervidity]
    private static final int RC_LOGIN = 1

    private RequestQueue requestQueue

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        final user = FirebaseAuth.instance.currentUser
        if (user == null) {
            final providers = [new AuthUI.IdpConfig.EmailBuilder().build()]
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
                    RC_LOGIN
            )
        } else {
            submitAppToken()
            Log.d(this.class.simpleName, "Logged in as ${user.email}")
        }
        requestQueue = Volley.newRequestQueue(this)
        makeFullscreen()
        setContentView(R.layout.activity_main)
        final backgroundTop = (AppCompatImageView) findViewById(R.id.background_top)
        final backgroundBottom = (AppCompatImageView) findViewById(R.id.background_bottom)

        final fira = Typeface.createFromAsset(getAssets(), "fonts/fira_mono.otf")
        final firaSans = Typeface.createFromAsset(getAssets(), "fonts/fira_sans_regular.otf")
        final hourText = findViewById(R.id.hour_text) as TextView
        final minuteText = findViewById(R.id.minute_text) as TextView
        final dateText = findViewById(R.id.date_text) as TextView
        hourText.setTypeface(fira)
        minuteText.setTypeface(fira)
        dateText.setTypeface(fira)
        final handler = new Handler()
        final updater = new Runnable() {
            @Override
            void run() {
                final now = DateTime.now()
                final date = SilicanDate.fromGregorian(now.toLocalDate())
                final weekday = date.date % 7
                final weekdayColor = weekdayColors[weekday]
                final phaseColor = phaseColors[now.hourOfDay.intdiv(8).intValue()]
                backgroundTop.setColorFilter(ContextCompat.getColor(MainActivity.this, weekdayColor))
                backgroundBottom.setColorFilter(ContextCompat.getColor(MainActivity.this, phaseColor))
                dateText.setText(String.format("%d\n%02d %02d", date.year, date.month, date.date))
                hourText.setText((now.hourOfDay % 8).toString())
                minuteText.setText(String.format("%02d", now.minuteOfHour))
                final nextMinute = now.plusMinutes(1).withSecondOfMinute(0)
                handler.postDelayed(this, nextMinute.millis - now.millis)
            }
        }
        updater.run()

        final temperatureText = findViewById(R.id.temperature_text) as TextView
        final temperature = new SpannableString("nevum 21,5")
        temperature.setSpan(new RelativeSizeSpan(4F), 6, 10, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        temperatureText.setTypeface(firaSans)
        temperatureText.setText(temperature)
    }

    @Override
    View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs)
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOGIN) {
            final response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                final user = FirebaseAuth.instance.currentUser
                Log.d(this.class.simpleName, user.email)
                submitAppToken()
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume()
        makeFullscreen()
    }

    private void submitAppToken() {
        final adm = new ADM(this)
        final registrationID = adm.getRegistrationId()
        if (registrationID == null) {
            adm.startRegister()
        }
        Log.d(this.class.simpleName, "Registration ID: $registrationID")
        FirebaseAuth.instance.currentUser.getIdToken(false).addOnCompleteListener({
            final token = it.result.token
            Session.userToken = token
            final url = "${Constants.baseURL}/sciigi?token=${token}"
            final request = new StringRequest(Request.Method.POST, url, {
                Log.d(this.class.simpleName, "Registered with Menteia cerbo")
            }, { VolleyError error ->
                Log.e(this.class.simpleName, "Error registering with Menteia cerbo: ${error.networkResponse.statusCode}")
            }) {
                @Override
                byte[] getBody() throws AuthFailureError {
                    return registrationID.getBytes()
                }
            }
            requestQueue.add(request)
        })
    }

    private void makeFullscreen() {
        final decorView = getWindow().getDecorView()
        final flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decorView.setSystemUiVisibility(flags)
    }
}
