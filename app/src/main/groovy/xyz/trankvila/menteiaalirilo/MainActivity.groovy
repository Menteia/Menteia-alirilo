package xyz.trankvila.menteiaalirilo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.annotation.Nullable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
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
import xyz.trankvila.menteiaalirilo.communication.Queries
import xyz.trankvila.menteiaalirilo.utilities.AudioPlayback
import xyz.trankvila.menteiaalirilo.utilities.Constants
import xyz.trankvila.menteiaalirilo.utilities.Session
import xyz.trankvila.menteiaalirilo.utilities.SilicanDate

@CompileStatic
class MainActivity extends FragmentActivity {
    private static final List<Integer> weekdayColors = [R.color.slate, R.color.lavender, R.color.carnation,
                                                        R.color.sapphire, R.color.ruby, R.color.amber, R.color.fern]
    private static final List<Integer> phaseColors = [R.color.chaos, R.color.serenity, R.color.fervidity]
    private static final List<String> months = ['A', 'C', 'E', 'F', 'G', 'H', 'L', 'M', 'P', 'S', 'T', 'V', 'Z']
    private static final List<String> weeks = ['N', 'P', 'S', 'V', 'Z']
    private static final List<String> days = ['B', 'F', 'L', 'N', 'P', 'R', 'T']
    private static final int RC_LOGIN = 1

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
        Session.requestQueue = Volley.newRequestQueue(this)
        makeFullscreen()
        setContentView(R.layout.activity_main)
        final backgroundTop = (AppCompatImageView) findViewById(R.id.background_top)
        final backgroundBottom = (AppCompatImageView) findViewById(R.id.background_bottom)
        final mainProgressBar = findViewById(R.id.main_progress_bar) as ProgressBar

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
                final phase = now.hourOfDay.intdiv(8).intValue()
                final phaseColor = phaseColors[phase]
                final nextWeekdayColor = weekdayColors[(weekday + 1) % 7]
                final nextPhaseColor = phaseColors[(phase + 1) % 3]
                backgroundTop.setColorFilter(ContextCompat.getColor(MainActivity.this, weekdayColor))
                backgroundBottom.setColorFilter(ContextCompat.getColor(MainActivity.this, phaseColor))
                dateText.setText(String.format("%d\n%s %s %s", date.year, months[date.month - 1],
                        weeks[(date.date - 1).intdiv(7).intValue()], days[((date.date - 1) % 7).intValue()]))
                hourText.setText((now.hourOfDay % 8).toString())
                minuteText.setText(String.format("%02d", now.minuteOfHour))
                final nextMinute = now.plusMinutes(1).withSecondOfMinute(0)
                if (now.hourOfDay % 8 == 7) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(MainActivity.this, nextPhaseColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    final rgb = Color.HSVToColor(hsv)
                    hourText.setTextColor(rgb)
                    minuteText.setTextColor(rgb)
                }
                if (now.hourOfDay == 23) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(MainActivity.this, nextWeekdayColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    dateText.setTextColor(Color.HSVToColor(hsv))
                }
                handler.postDelayed(this, nextMinute.millis - now.millis)
            }
        }
        updater.run()
        dateText.onClickListener = {
            final request = Queries.currentDateReadout({
                mainProgressBar.visibility = View.GONE
                AudioPlayback.play(it, this)
            })
            mainProgressBar.visibility = View.VISIBLE
            Session.requestQueue.add(request)
        }
        hourText.onClickListener = minuteText.onClickListener = {
            final request = Queries.currentTimeReadout({
                mainProgressBar.visibility = View.GONE
                AudioPlayback.play(it, this)
            })
            mainProgressBar.visibility = View.VISIBLE
            Session.requestQueue.add(request)
        }

        final temperatureText = findViewById(R.id.temperature_text) as TextView
        final temperature = new SpannableString("nevum 21,5")
        temperature.setSpan(new RelativeSizeSpan(4F), 6, 10, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        temperatureText.setTypeface(firaSans)
        temperatureText.setText(temperature)
        final temperatureButton = findViewById(R.id.temperature_button) as Button
        temperatureButton.setOnClickListener({
            final dialog = new QueryDialog()
            final arguments = new Bundle()
            arguments.putString("title", "doni ko des frodeni testos")
            dialog.setArguments(arguments)
            dialog.show(getSupportFragmentManager(), "QueryFragment")
        })
    }

    @Override
    View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs)
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data)
        switch (requestCode) {
            case RC_LOGIN:
                final response = IdpResponse.fromResultIntent(data)
                if (resultCode == RESULT_OK) {
                    final user = FirebaseAuth.instance.currentUser
                    Log.d(this.class.simpleName, user.email)
                    submitAppToken()
                }
                break
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
                if (error.networkResponse != null) {
                    Log.e(this.class.simpleName, "Error registering with Menteia cerbo: ${error.networkResponse.statusCode}")
                } else {
                    Log.e(this.class.simpleName, "Error connecting to Menteia cerbo")
                }
            }) {
                @Override
                byte[] getBody() throws AuthFailureError {
                    return registrationID.getBytes()
                }
            }
            Session.requestQueue.add(request)
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
