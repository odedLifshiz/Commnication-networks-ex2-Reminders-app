The program uses 3 global hash tables as the database:
GlobalReminderHash 
GlobalTaskHash 
GlobalPollHash 

The webserver initializes the database and reads the config file.
After that all http requests are handled in the HttpRequest class.
the database is updated after every add/edit/delete for any application task
the reminder/task/poll extend ApplicationObjectTask.
The html pages are dynamically build using the HtmlPagesBuilder class.

Runner - holds the main of the program.
WebServer - reads config file and initializes the program. then runs an infinite loop of handling http requests.
ConfigFileParser - reads configuration file
BlockingQueue - synchronized queue that holds tasks(http requests)
ThreadPool - holds the threads that try to execute tasks from BlockingQueue
HttpRequest - handled all http requests and updates databse
HtmlPagesBuilder - build dynamic http pages
SmtpClient - used to send email messages through smtp

Enums - 
TaskStatus - represents the task status
PollStatus - represents the poll status

ApplicationObject - reminder/poll/task inherit from this object
ApplicationObjectTask - represents an action to be executed on a specific date and time.
Reminder- reminder object
Task - task object
Poll - poll object

GlobalReminderHash - The data structure for all reminders. it's key is email address and value is ReminderHash for specific user
GlobalTaskHash - The data structure for all tasks. it's key is email address and value is TaskHash for specific user
GlobalPollHash - The data structure for all polls. it's key is email address and value is PollHash for specific user

ReminderHash - hash of all reminders per specific user. it's key is Id and value is Reminder
TasksHash - hash of all tasks per specific user. it's key is Id and value is Task
PollsHash - hash of all polls per specific user. it's key is Id and value is Poll

DateComparator - compares dates
SerializableTimer - a timer object for an ApllicationObjectTask that is serializable

Database - holds the global hashes and provides access to data files
DatabaseAccess - utility class for accessing the database
IdGenerator - generates unique id's for application tasks

Exceptions- 
BadVoteException - thrown when a vote is sent to an already closed poll or recipient already voted
InvalidEmailException - thrown when a user enters bad email address
BadConfigFileException - thrown when config file is corrupted
BadHttpRequest - thrown when the request is invalid
BadReminder- thrown when the user enters bad form parameters in reminder_editor
BadTask - thrown when the user enters bad form parameters in task_editor
BadPoll - thrown when the user enters bad form parameters in poll_editor

