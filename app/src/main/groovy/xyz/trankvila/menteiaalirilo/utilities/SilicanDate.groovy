package xyz.trankvila.menteiaalirilo.utilities

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.joda.time.Days
import org.joda.time.LocalDate



@CompileStatic
@TypeChecked
final class SilicanDate {
    private static final LocalDate syncPoint = new LocalDate(2018, 1, 1)
    private static final int syncDayNumber = 12018 * 364 + 7 * ((12018.intdiv(5)) - 12018.intdiv(40) + (12018.intdiv(400)))
    private static final int daysIn400Years = 400 * 364 + 7 * (400.intdiv(5) - 400.intdiv(40) + 1)
    private static final int daysIn40Years = 40 * 364 + 7 * (40.intdiv(5) - 1)
    private static final int daysIn5Years = 5 * 364 + 7

    final int year
    final int month
    final int date

    SilicanDate(int year, int month, int date) {
        this.year = year
        this.month = month
        this.date = date
    }

    static SilicanDate fromGregorian(LocalDate date) {
        final difference = Days.daysBetween(syncPoint, date).getDays()
        final dayNumber = syncDayNumber + difference
        final years400 = dayNumber.intdiv(daysIn400Years)
        final remain400 = dayNumber % daysIn400Years
        final years40 = remain400.intdiv(daysIn40Years)
        final remain40 = remain400 % daysIn40Years
        final years5 = remain40.intdiv(daysIn5Years)
        final remain5 = remain40 % daysIn5Years
        final remainingYears = remain5.intdiv(364)
        final remainingDays = remain5 % 364
        final year = years400 * 400 + years40 * 40 + years5 * 5 + Math.min(remainingYears.intValue(), 5)
        final dayOfYear = remainingYears == 6 ? 364 + remainingDays : remainingDays
        final month = dayOfYear.intdiv(28).intValue()
        final day = dayOfYear % 28 + 1
        return new SilicanDate(year, Math.min(month + 1, 13), (month == 13 ? day + 28 : day))
    }
}
