# Work Schedule Event Creator
The Work Schedule Event Creator is a Java application that uses the Google Calendar API to create events on a user's calendar. The goal of this application is to allow users to create events for their work schedule, and ensure that events with the same title and start time are not duplicated.

Prerequisites
Before using this application, you must have the following:

A Google Account
A Google Calendar
A project in the Google Cloud Console
The Google Calendar API enabled for the project
The appropriate credentials (OAuth client ID and client secret) for your project
Setting Up the Project
Clone the repository to your local machine:
shell
Copy code
$ git clone https://github.com/[your_username]/Work-Schedule-Event-Creator.git
Change into the project directory:
shell
Copy code
$ cd Work-Schedule-Event-Creator
Open the project in your preferred Java IDE.

Import the required libraries and dependencies. You will need the following:

Google API Client Library for Java
Google OAuth Client Library for Java
Create a new class, GoogleCalendarEventChecker. In this class, you will create a method checkIfEventExists() that takes in a Calendar object, a calendar ID, and an Event object. This method will retrieve a list of all events on the calendar, and check if an event with the same title and start time already exists. If it does, the method will return true. If not, it will return false.

In the main method of your App class, create a new instance of the GoogleCalendarEventChecker class, and call the checkIfEventExists() method. Pass in the required parameters, and use the return value to determine whether to create the event or not.

Run the project to create events on your Google Calendar.

Conclusion
The Work Schedule Event Creator is a useful tool for anyone who wants to automate the process of creating events for their work schedule. By using the Google Calendar API, this application allows users to easily create and manage events, without having to worry about duplicates. If you have any questions or encounter any issues while setting up the project, please feel free to open an issue on GitHub.
