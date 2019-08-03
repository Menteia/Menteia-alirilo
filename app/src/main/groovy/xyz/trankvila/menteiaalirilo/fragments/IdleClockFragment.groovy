package xyz.trankvila.menteiaalirilo.fragments

import android.graphics.Color
import android.graphics.Typeface;
import android.os.Bundle
import android.os.Handler;
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup
import android.widget.TextView
import org.joda.time.DateTime;
import xyz.trankvila.menteiaalirilo.R
import xyz.trankvila.menteiaalirilo.utilities.SilicanDate

/**
 * A simple {@link Fragment} subclass.
 */
class IdleClockFragment extends Fragment {
    private static final List<String> months = [
            'Ateluna',
            'Beviruto',
            'Deruna',
            'Elito',
            'Feridina',
            'Geranito',
            'Lunamarina',
            'Miraliluto',
            'Peridina',
            'Timireto',
            'Verana',
            'Wiladito',
            'Zerona'
    ],
    weeks = [
            'Nevari',
            'Penari',
            'Sevari',
            'Venari',
            'Zevari',
    ],
    weekdays = [
            'Temiranika',
            'Boromika',
            'Ferimanika',
            'Luvanika',
            'Navilimika',
            'Perinatika',
            'Relikanika',
    ],
    phases = [
            'Feroma',
            'Lerima',
            'Veruma',
    ]

    IdleClockFragment() {
        // Required empty public constructor
    }


    @Override
    View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final view = inflater.inflate(R.layout.fragment_idle_clock, container, false)
        final phaseText = view.findViewById(R.id.idle_phase_text) as TextView
        final timeText = view.findViewById(R.id.idle_time_text) as TextView
        final dateText = view.findViewById(R.id.idle_date_text) as TextView
        final lato = Typeface.createFromAsset(activity?.getAssets(), "fonts/lato_regular.ttf")

        phaseText.setTypeface(lato)
        timeText.setTypeface(lato)
        dateText.setTypeface(lato)

        final handler = new Handler()
        final updater = new Runnable() {
            @Override
            void run() {
                final now = DateTime.now()
                final date = SilicanDate.fromGregorian(now.toLocalDate())
                final nextMinute = now.plusMinutes(1).withSecondOfMinute(0)

                final phase = now.hourOfDay.intdiv(8).intValue()
                final weekday = date.date % 7
                final weekdayColor = ClockFragment.weekdayColors[weekday]
                final nextWeekdayColor = ClockFragment.weekdayColors[(weekday + 1) % 7]
                final nextPhaseColor = ClockFragment.phaseColors[(phase + 1) % 3]

                phaseText.setText(phases[phase])

                timeText.setText(String.format("%d %02d", now.hourOfDay % 8, now.minuteOfHour))
                if (now.hourOfDay % 8 == 7) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(activity, nextPhaseColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    final rgb = Color.HSVToColor(hsv)
                    timeText.setTextColor(rgb)
                } else {
                    timeText.setTextColor(Color.WHITE)
                }

                if (now.hourOfDay == 23) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(activity, nextWeekdayColor), hsv)
                    hsv[2] = hsv[2] * (now.minuteOfHour / 60.0) as float
                    dateText.setTextColor(Color.HSVToColor(hsv))
                } else {
                    dateText.setTextColor(Color.WHITE)
                }

                dateText.setText("$date.year ${months[date.month - 1]} ${weeks[(date.date - 1).intdiv(7).intValue()]} ${weekdays[weekday]}")

                handler.postDelayed(this, nextMinute.millis - now.millis)
            }
        }
        updater.run()
        return view
    }

}
