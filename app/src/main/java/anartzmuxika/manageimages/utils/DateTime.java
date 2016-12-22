package anartzmuxika.manageimages.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTime {


    public static String getCurrentData ()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());

    }

    public static Calendar getCurrentDataCalendar()
    {
        return  Calendar.getInstance();
    }

    /**********
     *
     * @param normal: If true, show yyyy-MM-dd HH:mm:ss format. False: yyyy_MM_dd_HH_mm_ss
     * @return
     */
    public static String getCurrentDataTime (boolean normal)
    {
        String format_data = "yyyy-MM-dd HH:mm:ss";
        if (!normal) format_data = "yyyy_MM_dd__HH_mm_ss";
        DateFormat dateFormat = new SimpleDateFormat(format_data);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getYesterdayData ()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return dateFormat.format(cal.getTime());
    }

    public static boolean isFirstMonthDay()
    {
        Calendar cal = Calendar.getInstance();
        System.out.println("Day of Month: " + cal.get(Calendar.DAY_OF_MONTH));
        return cal.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static boolean isFirstWeekDay()
    {
        Calendar cal = Calendar.getInstance();
        System.out.println("Day of Week: " + cal.get(Calendar.DAY_OF_WEEK));
        return cal.get(Calendar.DAY_OF_WEEK) == 2; //Monday
    }

    public static String[] getFirstDayNextYearAndLastDayBeforeYear ()
    {
        String[] datas = new String[2];
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 0);
        datas [0] = dateFormat.format(cal.getTime());
        cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.MONTH, 11);
        datas [1] = dateFormat.format(cal.getTime());
        return datas;
    }

    public static String getCurrentDataWithAddSetMonthValue (String first_data, int add_month)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        if (!first_data.equals("")) //add first_data in calendar object to correct asign in second data
        {
            int urtea = Integer.parseInt(first_data.substring(0, 4));
            int hilabetea = Integer.parseInt(first_data.substring(5,7))-1;
            int eguna = Integer.parseInt(first_data.substring(8,10));

            cal.set(urtea, hilabetea, eguna);
        }

        cal.add(Calendar.MONTH, add_month);  //Add two months to first data
        return dateFormat.format(cal.getTime());

    }

    public static String getCurrentDataWithAddSetBeforeDays (String first_data, int add_month)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        if (!first_data.equals("")) //add first_data in calendar object to correct asign in second data
        {
            int urtea = Integer.parseInt(first_data.substring(0, 4));
            int hilabetea = Integer.parseInt(first_data.substring(5,7))-1;
            int eguna = Integer.parseInt(first_data.substring(8,10));

            cal.set(urtea, hilabetea, eguna);
        }

        cal.add(Calendar.DAY_OF_WEEK, -(add_month));  //DownDays value to first data
        return dateFormat.format(cal.getTime());
    }

    public static Calendar getAllDataTimeCalendarObject (String date, String hour_string)
    {
        //Extract details data
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5,7))-1;
        int day = Integer.parseInt(date.substring(8,10));

        int [] hour_int = getHourMinutesSecondsInfo(hour_string);

        System.out.println(year+"/"+month+"/"+day+" "+hour_int[0]+":"+hour_int[1]+":"+hour_int[2]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour_int[0], hour_int[1], hour_int[2]);
        return calendar;
    }

    public static String getCurrentTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        //get current date time with Date()
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static int [] getHourMinutesSecondsInfo (String hour_string)
    {
        String hour = hour_string.substring(0, 2);
        int hour_int;
        if (hour.charAt(0) == '0')
        {
            hour_int = Integer.parseInt(hour.substring(1));
        }
        else
        {
            hour_int = Integer.parseInt(hour);
        }

        int minute_int = Integer.parseInt(hour_string.substring(3, 5));
        return new int [] {hour_int,minute_int,0};
    }

    //To create new DatePicker Dialog to select new data
    /*public static DatePickerFragment getNewDate (DatePickerFragment date)
    {
        /**
         * Set Up Current Date Into dialog
         */
        /*Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));

        date.setArguments(args);
        return date;
    }*/

    public static String[] setFormatData (int year, int monthOfYear, int dayOfMonth)
    {
        //year/month/day
        String[] format_data = new String[3];

        format_data[0] = String.valueOf(year);
        if (monthOfYear+1 < 10)
        {
            format_data [1] = "0"+ String.valueOf(monthOfYear+1);
        }
        else
        {
            format_data [1] = String.valueOf(monthOfYear+1);
        }

        if (dayOfMonth<10)
        {
            format_data [2] = "0"+ String.valueOf(dayOfMonth);
        }
        else
        {
            format_data [2] = String.valueOf(dayOfMonth);
        }
        return format_data;
    }

    public static String getCorrectFormatDataTime(String datatime)
    {
        return datatime.substring(0, 19);
    }

    public static String getOnlyData(String datatime)
    {
        return datatime.substring(0,10);
    }

    public static String getCorrectDataTimeInCurrentTimeZone(String pass, boolean with_hour)
    {
        System.out.println("*********************************************");
        System.out.println("Select data time: " + pass);
        System.out.println("**********************************************");

        //Parse date in UTC
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date value = null;
        try {
            value = formatter.parse(pass);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Convert UTC datetime in our timezone
        String format_data = "yyyy-MM-dd";
        if (with_hour) format_data = "yyyy-MM-dd HH:mm";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(format_data); //this format changeable
        dateFormatter.setTimeZone(TimeZone.getDefault());
        System.out.println("SELECT DATA IN DEFAULT TIMEZONE: " + dateFormatter.format(value));




        return dateFormatter.format(value);
    }



    /*****************************************************************************************
     *
     * @param pass: Pass last sincro date
     * @return: boolean Update data or no
     */
    public static boolean isPassOneHour(String pass)
    {

        //Get curren data time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String current = dateFormat.format(date);

        //Extract current data time all values

        int current_year = Integer.parseInt(current.substring(0, 4));
        int current_month = Integer.parseInt(current.substring(5,7))-1;
        int current_day = Integer.parseInt(current.substring(8,10));

        int current_hour = Integer.parseInt(current.substring(11,13));
        int current_minute = Integer.parseInt(current.substring(14,16));
        int current_seconds = Integer.parseInt(current.substring(17));

        //String pass = "2016-10-05 10:14:00";

        //Extract last update data (pass from parameter

        int year = Integer.parseInt(pass.substring(0, 4));
        int month = Integer.parseInt(pass.substring(5,7))-1;
        int day = Integer.parseInt(pass.substring(8,10));

        int hour = Integer.parseInt(pass.substring(11,13));
        int minute = Integer.parseInt(pass.substring(14,16));
        int seconds = Integer.parseInt(pass.substring(17));


        System.out.println("CURRENT: " + current_year+"/"+current_month+"/"+current_day+" "+current_hour+":"+current_minute+":"+current_seconds);

        System.out.println("PASS: " + year+"/"+month+"/"+day+" "+hour+":"+minute+":"+seconds);

        if (current_year == year)
        {
            if (current_month == month)
            {
                if (current_day == day)
                {
                    if (current_hour > hour)
                    {
                        if ((current_hour - hour > 1))
                        {
                            //Bi ordu gutxienez
                            System.out.println("Bi ordu gutxienez: " + (current_hour) +  " / " + hour);
                            return true;
                        }
                        else
                        {
                            if (((current_minute + 60) - minute ) < 60)
                            {
                                //Ordu barruan dago
                                System.out.println("Ordu barruan nago ez delako 60 minutu pasa: " + ((current_minute + 60) - minute ));
                                return false;
                            }
                            else
                            {
                                //Refresh
                                System.out.println("Eguneratu, ordu bat pasa da gutxienez: " + ((current_minute + 60) - minute ));
                                return true;
                            }
                        }

                    }
                }
            }
        }
        return false;
    }
}
