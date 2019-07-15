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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private static final List<Integer> weekdayColors = [R.color.slate, R.color.lavender, R.color.carnation,
                                                        R.color.sapphire, R.color.ruby, R.color.amber, R.color.fern]
    private static final List<Integer> phaseColors = [R.color.chaos, R.color.serenity, R.color.fervidity]
    private static final List<String> months = ['A', 'C', 'E', 'F', 'G', 'H', 'L', 'M', 'P', 'S', 'T', 'V', 'Z']
    private static final List<String> weeks = ['N', 'P', 'S', 'V', 'Z']
    private static final List<String> days = ['B', 'F', 'L', 'N', 'P', 'R', 'T']

    private OnFragmentInteractionListener mListener

    @Override
    View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final view = inflater.inflate(R.layout.fragment_clock, container, false)
        final hourText = view.findViewById(R.id.hour_text) as TextView
        final minuteText = view.findViewById(R.id.minute_text) as TextView
        final dateText = view.findViewById(R.id.date_text) as TextView
        final fira = Typeface.createFromAsset(activity?.getAssets(), "fonts/fira_mono.otf")
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
                final backgroundTop = activity?.findViewById(R.id.background_top) as AppCompatImageView
                final backgroundBottom = activity?.findViewById(R.id.background_bottom) as AppCompatImageView
                backgroundTop.setColorFilter(ContextCompat.getColor(activity, weekdayColor))
                backgroundBottom.setColorFilter(ContextCompat.getColor(activity, phaseColor))
                dateText.setText(String.format("%d\n%s %s %s", date.year, months[date.month - 1],
                        weeks[(date.date - 1).intdiv(7).intValue()], days[((date.date - 1) % 7).intValue()]))
                hourText.setText((now.hourOfDay % 8).toString())
                minuteText.setText(String.format("%02d", now.minuteOfHour))
                final nextMinute = now.plusMinutes(1).withSecondOfMinute(0)
                if (now.hourOfDay % 8 == 7) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(activity, nextPhaseColor), hsv)
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
                    Color.colorToHSV(ContextCompat.getColor(MainActivity.this, nextWeekdayColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    dateText.setTextColor(Color.HSVToColor(hsv))
                } else {
                    dateText.setTextColor(Color.WHITE)
                }
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
    void onAttach(Context context) {
        super.onAttach(context)
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    @Override
    void onDetach() {
        super.onDetach()
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
