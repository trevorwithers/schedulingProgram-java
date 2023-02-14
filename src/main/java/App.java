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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;

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

    public static boolean checkIfEventExists(Calendar calendarService, String calendarId, Event event)
            throws IOException {
        // Get a list of all events on the calendar
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = calendarService.events().list(calendarId)
                .setTimeMin(now)
                .execute();
        List<Event> items = events.getItems();
        boolean exists = false;
        // Check if an event with the same title and start time exists
        for (Event existingEvent : items) {
            System.out.println(existingEvent.getSummary());
            if (existingEvent.getSummary().equals(event.getSummary()) &&
                    existingEvent.getStart().getDateTime().equals(event.getStart().getDateTime())) {
                exists = true;
            }
        }

        return exists;
    }

    private static String day;
    private static String finishMonth;

    public static void main(String[] args) throws IOException, GeneralSecurityException, ParseException {
        try {
            // Set the look and feel to the system look and feel 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create the string to hold the email
        String email = "";
        // Create the file chooser
        JFileChooser fileChooser = new JFileChooser();
        // Set the default directory to the downloads folder
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Downloads"));
        // Show the file chooser
        int result = fileChooser.showOpenDialog(null);
        // If the user selects a file
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the path of the file
            File selectedFile = fileChooser.getSelectedFile();
            // Get the absolute path of the file
            String path = selectedFile.getAbsolutePath();
            // Load the file into a PDDocument
            PDDocument document = PDDocument.load(new File(path));
            // Create a PDFTextStripper to strip the text from the PDF
            PDFTextStripper pdfStripper = new PDFTextStripper();
            // Get the text from the PDF
            email = pdfStripper.getText(document);
            document.close();
        }

        // Create the pattern to find the dates and times
        Pattern patternFindDates = Pattern.compile(
                "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{2})[a-z]*:\\s+(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\s+(\\d{2}:\\d{2})([APM]+)\\s+-\\s+(Mon\\s+|Tue\\s+|Wed\\s+|Thu\\s+|Fri\\s+|Sat\\s+|Sun\\s+)?(\\d{2}:\\d{2})([APM]+)");

        // Create the pattern to find the years
        Pattern patternFindYears = Pattern
                .compile("Period:\\s+(\\w+)\\s+\\d+,\\s+(\\d+)\\s+-\\s+(\\w+)\\s+\\d+,\\s+(\\d+)");

        // Create the matchers
        Matcher datesMatch = patternFindDates.matcher(email);
        Matcher yearsMatch = patternFindYears.matcher(email);

        // Create the arrays to hold the dates and times
        String[] datesTimes = new String[14];
        // Create the counter to keep track of the dates and times
        int daysWorkedCtr = 0;
        // Create the string to hold the start year
        String startYear = "";

        if (yearsMatch.find()) {
            // Get the start year
            startYear = yearsMatch.group(2);
        }
        // Cycle through the dates and times and add them to the array
        while (datesMatch.find()) {

            String beginYear = startYear;
            String finishYear = startYear;
            String beginMonth = "";
            finishMonth = "";
            day = "";
            String startHour = "";
            String endHour = "";

            // Convert the month to a number
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

            // Convert the start time to 24 hour time
            if (datesMatch.group(5).equals("PM")) {
                startHour = datesMatch.group(4).substring(0, 2);
                startHour = "" + (Integer.parseInt(startHour) + 12);
            } else {
                startHour = datesMatch.group(4).substring(0, 2);
            }
            // Convert the end time to 24 hour time
            if (datesMatch.group(8).equals("PM")) {
                endHour = datesMatch.group(7).substring(0, 2);
                endHour = "" + (Integer.parseInt(endHour) + 12);
            } else {
                endHour = datesMatch.group(7).substring(0, 2);
            }

            // Check if an overnight shift was worked
            if (datesMatch.group(6) != null) {
                // Get the day
                day = "" + datesMatch.group(2);
                // Determine which month the shift ends in
                if (beginMonth.equals("01") || beginMonth.equals("03") || beginMonth.equals("05")
                        || beginMonth.equals("07") || beginMonth.equals("08") || beginMonth.equals("10")
                        || beginMonth.equals("12")) {
                    // Call the method to check the end month
                    checkEndMonth("31", beginMonth);
                    // Check if the month is December and set the year to the next year
                    if (finishMonth.equals("13")) {
                        // Set the year to the next year
                        finishYear = "" + (Integer.parseInt(startYear) + 1);
                        // Set the month to January
                        finishMonth = "01";
                    }
                    // Check if the month is April, June, September, or November
                } else if (beginMonth.equals("04") || beginMonth.equals("06") || beginMonth.equals("09")
                        || beginMonth.equals("11")) {
                    // Call the method to check the end month
                    checkEndMonth("30", beginMonth);
                } else {
                    // Call the method to check the end month
                    checkEndMonth("28", beginMonth);

                }
            } else {
                // Set the day if the shift is not overnight
                day = datesMatch.group(2);
                // Set the finish month to the begin month if the shift is not overnight
                finishMonth = beginMonth;
            }
            // Add the start time and date to the array in Google Calendar format
            datesTimes[daysWorkedCtr] = beginYear + "-" + beginMonth + "-" + datesMatch.group(2) + "T" + startHour
                    + ":00:00-05:00";
            // Add the end time and date to the array in Google Calendar format
            datesTimes[daysWorkedCtr + 1] = finishYear + "-" + finishMonth + "-" + day + "T" + endHour + ":00:00-05:00";
            System.out.println(datesTimes[daysWorkedCtr]);
            System.out.println(datesTimes[daysWorkedCtr + 1]);
            // Increment the counter by 2 to add the next start and end time
            daysWorkedCtr += 2;
        }

        // Create the NetHttpTransport
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Create the Calendar service
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT))
                .setApplicationName("applicationName").build();

        // Cycle through the dates and times and add them to the calendar
        for (int i = 0; i < daysWorkedCtr; i += 2) {
            // Create the event
            Event event = new Event()
                    .setSummary("Work");
            // Set the start time and date
            DateTime startDateTime = new DateTime(datesTimes[i]);
            EventDateTime starting = new EventDateTime()
                    .setDateTime(startDateTime);
            event.setStart(starting);
            // Set the end time and date
            DateTime endDateTime = new DateTime(datesTimes[i + 1]);
            EventDateTime ending = new EventDateTime()
                    .setDateTime(endDateTime);
            event.setEnd(ending);
            // Set the calendar ID
            String calendarId = "withers.trevor@gmail.com";
            // Check if the event already exists
            if (!checkIfEventExists(service, calendarId, event)) {
                // Create the event if it doesn't exist
                service.events().insert(calendarId, event).execute();
            } else {
                System.out.println("Event already exists");
            }
        }
    }

    // Method to check if the shift ends on the last day of the month
    private static void checkEndMonth(String numDays, String beginMonth) {
        // Check if the day is the last day of the month
        if (day.equals(numDays)) {
            // Set the day to the first day of the next month
            day = "01";
            // Set the month to the next month
            finishMonth = "" + (Integer.parseInt(beginMonth) + 1);
            // Check if the month is less than 10 and add a 0 if it is
            if (Integer.parseInt(finishMonth) < 10) {
                finishMonth = "0" + finishMonth;
            }
        } else {
            // Set the month to the same month if it is not the last day of the month
            finishMonth = beginMonth;
            // Set the day to the next day
            day = "" + (Integer.parseInt(day) + 1);
            // Check if the day is less than 10 and add a 0 if it is
            if (Integer.parseInt(day) < 10) {
                day = "0" + day;
            }
        }
    }
}
