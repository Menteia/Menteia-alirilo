package xyz.trankvila.menteiaalirilo.fragments

import android.graphics.Typeface
import android.os.Bundle;
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView;
import xyz.trankvila.menteiaalirilo.R
import xyz.trankvila.menteiaalirilo.communication.Queries
import xyz.trankvila.menteiaalirilo.utilities.AudioPlayback
import xyz.trankvila.menteiaalirilo.utilities.Session;


/**
 * A simple {@link Fragment} subclass.
 */
class ControlFragment extends Fragment implements ClockFragment.OnFragmentInteractionListener {
    private ProgressBar mainProgressBar
    private Fragment centralFragment

    ControlFragment() {
        // Required empty public constructor
    }


    @Override
    View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final view = inflater.inflate(R.layout.fragment_control, container, false)
        centralFragment = new ClockFragment()
        final transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.central_container, centralFragment)
        transaction.commit()

        mainProgressBar = view.findViewById(R.id.main_progress_bar) as ProgressBar

        final lato = Typeface.createFromAsset(activity.assets, "fonts/lato_regular.ttf")

        final temperatureText = view.findViewById(R.id.temperature_text) as TextView
        final temperature = new SpannableString("nevum 21,5")
        temperature.setSpan(new RelativeSizeSpan(4F), 6, 10, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        temperatureText.setTypeface(lato)
        temperatureText.setText(temperature)
        final temperatureButton = view.findViewById(R.id.temperature_button) as Button
        temperatureButton.setOnClickListener({
            query("doni ko des frodeni testos")
        })
        return view
    }

    private void query(String line) {
        mainProgressBar.setVisibility(View.VISIBLE)
        final question = Queries.speak(line, {
            AudioPlayback.speak(it, activity, true, {
                final response = Queries.request(line, {
                    mainProgressBar.setVisibility(View.GONE)
                    AudioPlayback.speak(it.getString("ssml"), activity, false, {})
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
