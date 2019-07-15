package xyz.trankvila.menteiaalirilo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
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
import android.widget.FrameLayout
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
import xyz.trankvila.menteiaalirilo.fragments.ClockFragment
import xyz.trankvila.menteiaalirilo.utilities.AudioPlayback
import xyz.trankvila.menteiaalirilo.utilities.Constants
import xyz.trankvila.menteiaalirilo.utilities.Session
import xyz.trankvila.menteiaalirilo.utilities.SilicanDate

@CompileStatic
class MainActivity extends FragmentActivity implements ClockFragment.OnFragmentInteractionListener {
    private static final int RC_LOGIN = 1

    private ProgressBar mainProgressBar
    private Fragment centralFragment

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
        mainProgressBar = findViewById(R.id.main_progress_bar) as ProgressBar

        final firaSans = Typeface.createFromAsset(getAssets(), "fonts/fira_sans_regular.otf")

        centralFragment = new ClockFragment()
        final transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.central_container, centralFragment)
        transaction.commit()

        final temperatureText = findViewById(R.id.temperature_text) as TextView
        final temperature = new SpannableString("nevum 21,5")
        temperature.setSpan(new RelativeSizeSpan(4F), 6, 10, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        temperatureText.setTypeface(firaSans)
        temperatureText.setText(temperature)
        final temperatureButton = findViewById(R.id.temperature_button) as Button
        temperatureButton.setOnClickListener({
            query("doni ko des frodeni testos")
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

    private void query(String line) {
        mainProgressBar.setVisibility(View.VISIBLE)
        final question = Queries.speak(line, {
            AudioPlayback.speak(it, this, true, {
                final response = Queries.request(line, {
                    mainProgressBar.setVisibility(View.GONE)
                    AudioPlayback.speak(it.getString("ssml"), this, false, {})
                })
                Session.requestQueue.add(response)
            })
        })
        Session.requestQueue.add(question)
    }

    @Override
    void onFragmentQuery(String line) {
        query(line)
    }
}
