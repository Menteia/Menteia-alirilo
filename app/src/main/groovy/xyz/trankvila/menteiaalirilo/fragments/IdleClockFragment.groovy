package xyz.trankvila.menteiaalirilo.fragments

import android.graphics.Color
import android.graphics.Typeface;
import android.os.Bundle
import android.os.Handler;
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.joda.time.DateTime
import org.w3c.dom.Text;
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
        final yearView = view.findViewById(R.id.idle_year_text) as TextView
        final monthWeekView = view.findViewById(R.id.idle_month_week_text) as TextView
        final weekdayView = view.findViewById(R.id.idle_weekday_text) as TextView
        final hourView = view.findViewById(R.id.idle_hour_text) as TextView
        final minuteView = view.findViewById(R.id.idle_minute_text) as TextView
        final backgroundTop = view.findViewById(R.id.idle_clock_background_top) as AppCompatImageView
        final backgroundBottom = view.findViewById(R.id.idle_clock_background_bottom) as AppCompatImageView
        final lato = Typeface.createFromAsset(activity?.assets, "fonts/lato_regular.ttf")

        yearView.typeface = lato
        monthWeekView.typeface = lato
        weekdayView.typeface = lato
        hourView.typeface = lato
        minuteView.typeface = lato

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

                backgroundTop.colorFilter = ContextCompat.getColor(activity, weekdayColor)
                backgroundBottom.colorFilter = ContextCompat.getColor(activity, ClockFragment.phaseColors[phase])

                hourView.text = "${now.hourOfDay % 8}"
                minuteView.text = String.format("%02d", now.minuteOfHour)

                if (now.hourOfDay % 8 == 7) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(activity, nextPhaseColor), hsv)
                    hsv[1] = hsv[1] * now.minuteOfHour / 60.0 as float
                    final rgb = Color.HSVToColor(hsv)
                    hourView.textColor = rgb
                    minuteView.textColor = rgb
                } else {
                    hourView.textColor = Color.WHITE
                    minuteView.textColor = Color.WHITE
                }

                if (now.hourOfDay == 23) {
                    final hsv = new float[3]
                    Color.colorToHSV(ContextCompat.getColor(activity, nextWeekdayColor), hsv)
                    hsv[1] = hsv[1] * now.minuteOfHour / 60.0 as float
                    yearView.textColor = Color.HSVToColor(hsv)
                    monthWeekView.textColor = Color.HSVToColor(hsv)
                    weekdayView.textColor = Color.HSVToColor(hsv)
                } else {
                    yearView.textColor = Color.WHITE
                    monthWeekView.textColor = Color.WHITE
                    weekdayView.textColor = Color.WHITE
                }

                yearView.text = "$date.year"
                monthWeekView.text = "${months[date.month - 1]} ${weeks[(date.date - 1).intdiv(7).intValue()]}"
                weekdayView.text = "${weekdays[weekday]}"
                handler.postDelayed(this, nextMinute.millis - now.millis)
            }
        }
        updater.run()
        return view
    }

}
