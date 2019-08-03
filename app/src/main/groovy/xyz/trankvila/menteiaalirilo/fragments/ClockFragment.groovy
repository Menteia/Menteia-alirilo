package xyz.trankvila.menteiaalirilo.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.support.v4.app.Fragment
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import org.joda.time.DateTime
import xyz.trankvila.menteiaalirilo.R
import xyz.trankvila.menteiaalirilo.utilities.SilicanDate


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ClockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
class ClockFragment extends Fragment {
    static final List<Integer> weekdayColors = [R.color.slate, R.color.lavender, R.color.carnation,
                                                        R.color.sapphire, R.color.ruby, R.color.amber, R.color.fern]
    static final List<Integer> phaseColors = [R.color.chaos, R.color.serenity, R.color.fervidity]
    private static final List<String> phases = ['Feroma', 'Lerima', 'Veruma']
    private static final List<String> months = ['A', 'C', 'E', 'F', 'G', 'H', 'L', 'M', 'P', 'S', 'T', 'V', 'Z']
    private static final List<String> weeks = ['N', 'P', 'S', 'V', 'Z']
    private static final List<String> days = ['B', 'F', 'L', 'N', 'P', 'R', 'T']

    private OnFragmentInteractionListener mListener

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)

        if (parentFragment instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) parentFragment
        } else {
            throw new RuntimeException(parentFragment.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    @Override
    View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final view = inflater.inflate(R.layout.fragment_clock, container, false)
        final hourText = view.findViewById(R.id.hour_text) as TextView
        final minuteText = view.findViewById(R.id.minute_text) as TextView
        final yearText = view.findViewById(R.id.year_text) as TextView
        final dateText = view.findViewById(R.id.clock_date_text) as TextView
        final phaseText = view.findViewById(R.id.clock_phase_text) as TextView
        final lato = Typeface.createFromAsset(activity?.getAssets(), "fonts/lato_regular.ttf")
        hourText.setTypeface(lato)
        minuteText.setTypeface(lato)
        yearText.setTypeface(lato)
        dateText.setTypeface(lato)
        phaseText.setTypeface(lato)
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
                yearText.setText("$date.year")
                final dateSpan = new SpannableString(String.format("%s%s%s", months[date.month - 1],
                        weeks[(date.date - 1).intdiv(7).intValue()], days[((date.date - 1) % 7).intValue()]))
                phaseText.setText(phases[now.hourOfDay.intdiv(8).intValue()])
                phaseText.setTextColor(ContextCompat.getColor(parentFragment?.activity, phaseColor))
                hourText.setText((now.hourOfDay % 8).toString())
                minuteText.setText(String.format("%02d", now.minuteOfHour))
                final nextMinute = now.plusMinutes(1).withSecondOfMinute(0)
                if (now.hourOfDay % 8 == 7) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(parentFragment?.activity, nextPhaseColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    final rgb = Color.HSVToColor(hsv)
                    hourText.setTextColor(rgb)
                    minuteText.setTextColor(rgb)
                } else {
                    hourText.setTextColor(Color.WHITE)
                    minuteText.setTextColor(Color.WHITE)
                }
                if (now.hourOfDay == 23) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(parentFragment?.activity, nextWeekdayColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    dateSpan.setSpan(new ForegroundColorSpan(Color.HSVToColor(hsv)), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    dateSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                dateSpan.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(parentFragment?.activity, weekdayColor)),
                        2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                dateText.setText(dateSpan)
                handler.postDelayed(this, nextMinute.millis - now.millis)
            }
        }
        updater.run()
        dateText.onClickListener = {
            mListener?.onFragmentQuery("doni ko fidinas")
        }
        hourText.onClickListener = minuteText.onClickListener = {
            mListener?.onFragmentQuery("doni ko geradas")
        }
        return view
    }

    @Override
    void onDestroy() {
        super.onDestroy()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        void onFragmentQuery(String line)
    }
}
