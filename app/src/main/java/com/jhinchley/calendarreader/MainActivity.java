package com.jhinchley.calendarreader;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.R.id.text1;

public class MainActivity extends AppCompatActivity {

    TextView textView1;

    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get buttons
        Button dateButton = (Button) findViewById(R.id.dateButton);
        Button agendaButton = (Button) findViewById(R.id.agendaButton);
        textView1 = (TextView) findViewById(R.id.text1);

        //set their click listeners and tie to resepective function
    }

    @TargetApi(Build.VERSION_CODES.N)
    protected void getDate(View view) {
        //method to output today's date
        //not working before api 24???
        //Calendar rightNow = Calendar.getInstance();
        String date = new SimpleDateFormat("MMMM dd, yyyy").format(new Date());
        Toast.makeText(this, date, Toast.LENGTH_SHORT).show();
    }

    protected void getAgenda(View view) {
        //method to output today's agenda
        //ArrayAdapter arrayAdapter =
        runQuery();
    }

    protected void runQuery() {

        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?))";
        String[] selectionArgs = new String[]{"jhinchley@tamu.edu", "jhinchley@tamu.edu"};

        // Submit the query and get a Cursor object back.

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //all visible calendars
        //cur = cr.query(uri, EVENT_PROJECTION, CalendarContract.Calendars.VISIBLE + " = 1", null, CalendarContract.Calendars._ID + " ASC");
        cur = cr.query(uri,EVENT_PROJECTION,selection,selectionArgs,null);
        String output = "";

        //calendar ID used to access events, reminders, etc.
        long calID = 0;
        while (cur.moveToNext()) {

            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(0);
            displayName = cur.getString(2);
            accountName = cur.getString(1);


            Log.d("getAgenda: ", displayName);
        }
        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
        //Uri.Builder builder = Uri.parse("content://com.android.calendar/calendars").buildUpon();

        long now = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(-1)).getTime();
        long next12 = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)).getTime();
        ContentUris.appendId(builder, now - DateUtils.DAY_IN_MILLIS * 10000);
        ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS * 10000);

        Cursor eventCursor = cr.query(builder.build(),
                new String[]  { "title", "begin", "end", "allDay"}, "Events.CALENDAR_ID="+ calID+ " AND Events.DTSTART>="+now+ " AND Events.DTSTART<"+next12,
                null, "startDay ASC, startMinute ASC");

        while (eventCursor.moveToNext()){
            final String title = eventCursor.getString(0);
            final Date begin = new Date(eventCursor.getLong(1));
            final Date end = new Date(eventCursor.getLong(2));
            final Boolean allDay = !eventCursor.getString(3).equals("0");
            output+=title+begin+end+allDay+"\n";
        }

        textView1.setText(output);


    }
}
