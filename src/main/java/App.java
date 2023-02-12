import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.time.Year;

public class App {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        // returns an authorized Credential object.
        return credential;
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException, ParseException {
        File file = new File("src/main/resources/email.txt");
        Scanner scanner = new Scanner(file);
        String email = "";
        while (scanner.hasNextLine()) {
            email += scanner.nextLine() + "\n";
        }

        scanner.close();

        // Feb 13: Monday 10:00PM - Tue 06:00AM, 4839 - Highway 15 Kingston, Prep Cook
        Pattern patternFindDates = Pattern.compile(
                "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{2})[a-z]*:\\s+(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\s+(\\d{2}:\\d{2})([APM]+)\\s+-\\s+(Mon\\s+|Tue\\s+|Wed\\s+|Thu\\s+|Fri\\s+|Sat\\s+|Sun\\s+)?(\\d{2}:\\d{2})([APM]+)");

        Matcher datesMatch = patternFindDates.matcher(email);

        Pattern patternFindYears = Pattern
                .compile("Period:\\s+(\\w+)\\s+\\d+,\\s+(\\d+)\\s+-\\s+(\\w+)\\s+\\d+,\\s+(\\d+)");
        Matcher yearsMatch = patternFindYears.matcher(email);
        String[] datesTimes = new String[14];
        int ctr = 0;
        int numOvernights = 0;
        int[] overNightStart = new int[7];
        String[] overnights = new String[7];
        String startMonth = "";
        String startYear = "";
        String endYear = "";
        if (yearsMatch.find()) {
            startMonth = yearsMatch.group(1);
            startYear = yearsMatch.group(2);
            endYear = yearsMatch.group(4);
        }
        while (datesMatch.find()) {
            String beginYear = startYear;
            String finishYear = startYear;
            String beginMonth = "";
            String finishMonth = "";
            String day = "";
            String startHour = "";
            String endHour = "";

            switch (datesMatch.group(1)) {
                case "Jan":
                    beginMonth = "01";
                    break;
                case "Feb":
                    beginMonth = "02";
                    break;
                case "Mar":
                    beginMonth = "03";
                    break;
                case "Apr":
                    beginMonth = "04";
                    break;
                case "May":
                    beginMonth = "05";
                    break;
                case "Jun":
                    beginMonth = "06";
                    break;
                case "Jul":
                    beginMonth = "07";
                    break;
                case "Aug":
                    beginMonth = "08";
                    break;
                case "Sep":
                    beginMonth = "09";
                    break;
                case "Oct":
                    beginMonth = "10";
                    break;
                case "Nov":
                    beginMonth = "11";
                    break;
                case "Dec":
                    beginMonth = "12";
                    break;
            }
            if (datesMatch.group(5).equals("PM")) {
                startHour = datesMatch.group(4).substring(0, 2);
                startHour = "" + (Integer.parseInt(startHour) + 12);
            } else {
                startHour = datesMatch.group(4).substring(0, 2);
            }
            if (datesMatch.group(8).equals("PM")) {
                endHour = datesMatch.group(7).substring(0, 2);
                endHour = "" + (Integer.parseInt(endHour) + 12);
            } else {
                endHour = datesMatch.group(7).substring(0, 2);
            }
            if (datesMatch.group(6) != null) {
                day = "" + datesMatch.group(2);
                if (beginMonth.equals("01") || beginMonth.equals("03") || beginMonth.equals("05")
                        || beginMonth.equals("07")
                        || beginMonth.equals("08") || beginMonth.equals("10") || beginMonth.equals("12")) {
                    if (day.equals("31")) {
                        day = "01";
                        finishMonth = "" + (Integer.parseInt(beginMonth) + 1);
                        if (Integer.parseInt(finishMonth) < 10) {
                            finishMonth = "0" + finishMonth;
                        }
                        if (finishMonth.equals("13")) {
                            finishYear = "" + (Integer.parseInt(startYear) + 1);
                            finishMonth = "01";
                        }
                    } else {
                        finishMonth = beginMonth;
                        day = "" + (Integer.parseInt(day) + 1);
                        if (Integer.parseInt(day) < 10) {
                            day = "0" + day;
                        }
                    }
                } else if (beginMonth.equals("04") || beginMonth.equals("06") || beginMonth.equals("09")
                        || beginMonth.equals("11")) {
                    if (day.equals("30")) {
                        day = "01";
                        finishMonth = "" + (Integer.parseInt(beginMonth) + 1);
                        if (Integer.parseInt(finishMonth) < 10) {
                            finishMonth = "0" + finishMonth;
                        }
                    } else {
                        finishMonth = beginMonth;
                        day = "" + (Integer.parseInt(day) + 1);
                        if (Integer.parseInt(day) < 10) {
                            day = "0" + day;
                        }
                    }
                } else if (beginMonth.equals("02")) {
                    if (day.equals("28")) {
                        day = "01";
                        finishMonth = "" + (Integer.parseInt(beginMonth) + 1);
                        if (Integer.parseInt(finishMonth) < 10) {
                            finishMonth = "0" + finishMonth;
                        }
                    } else {
                        finishMonth = beginMonth;
                        day = "" + (Integer.parseInt(day) + 1);
                        if (Integer.parseInt(day) < 10) {
                            day = "0" + day;
                        }
                    }
                }
            } else {
                day = datesMatch.group(2);
                finishMonth = beginMonth;
            }
            datesTimes[ctr] = beginYear + "-" + beginMonth + "-" + datesMatch.group(2) + "T" + startHour
                    + ":00:00-05:00";
            datesTimes[ctr + 1] = finishYear + "-" + finishMonth + "-" + day + "T" + endHour + ":00:00-05:00";
            System.out.println(datesTimes[ctr]);
            System.out.println(datesTimes[ctr + 1]);
            ctr += 2;
        }

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT))
                .setApplicationName("applicationName").build();
        for (int i = 0; i < ctr; i+=2) {

            Event event = new Event()
                    .setSummary("Work");

            DateTime startDateTime = new DateTime(datesTimes[i]);
            EventDateTime starting = new EventDateTime()
                    .setDateTime(startDateTime);
            event.setStart(starting);

            DateTime endDateTime = new DateTime(datesTimes[i + 1]);
            EventDateTime ending = new EventDateTime()
                    .setDateTime(endDateTime);
            event.setEnd(ending);

            String calendarId = "withers.trevor@gmail.com";
            event = service.events().insert(calendarId, event).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());

        }
    }
}
