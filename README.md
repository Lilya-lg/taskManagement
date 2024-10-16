
# Task Management System

The Task Management System is a web application that allows users to create, manage, and track tasks efficiently. It supports task creation, editing, deletion, file uploads, and task assignment to users. The system helps teams stay organized and monitor task progress.

## Features

- **Task Management**: Create, update, view, and delete tasks.
- **File Uploads**: Attach files to tasks and download them later.
- **User Assignment**: Assign tasks to users and keep track of who is responsible for each task.
- **Task Status Tracking**: Track the status of tasks 
- **Responsive Design**: User-friendly interface with Thymeleaf templates for web-based task management.

## Technologies Used

- **Backend**: Java 17, Spring Boot, Spring Data JPA, Hibernate
- **Frontend**: Thymeleaf, HTML, CSS
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Testing**: JUnit, Mockito, Spring Boot Test

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Git (for version control)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/task-management-system.git
cd task-management-system
```

### 2. Configure the Database

1. Make sure PostgreSQL is installed and running.
2. Create a new database:

    ```sql
    CREATE DATABASE task_management;

3. Update the `application.properties` file located in `src/main/resources` with your database configuration:

    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
    spring.datasource.username=your_db_username
    spring.datasource.password=your_db_password
    spring.jpa.hibernate.ddl-auto=update
    ```

### 3. Build the Application

Use Maven to build the application:

```bash
mvn clean install
```

### 4. Run the Application

Start the application using:

```bash
mvn spring-boot:run
```

Access the application at `http://localhost:8080`.

## Application Structure

The project follows a typical Spring Boot structure:

```
task-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.taskmanagement/
│   │   │       ├── controller/         # Controllers for handling HTTP requests
│   │   │       ├── entity/             # Entity classes representing the data model
│   │   │       ├── repository/         # Repositories for data access
│   │   │       ├── service/            # Services containing business logic
│   │   │       └── TaskManagementApplication.java # Main application class
│   │   └── resources/
│   │       ├── templates/              # Thymeleaf templates (HTML views)
│   │       ├── static/                 # Static assets (CSS, JS)
│   │       └── application.properties  # Application configuration
└── pom.xml                              # Maven build file
```

## Usage

### 1. Creating a Task

- Go to the "Create Task" page.
- Fill out the form with details such as the title, description, due date, and assigned user.
- (Optional) Upload a file related to the task.
- Click "Save" to create the task.

### 2. Viewing Tasks

- Navigate to the "Task List" page to see all tasks.
- Each task entry displays the title, description, due date, assigned user, and file (if any).

### 3. Editing a Task

- Click the "Edit" button next to a task to update its information.
- Update the task details and click "Save."


### 4. Downloading Attached Files

- If a task has an associated file, a "Download" link will appear. Click it to download the file.

## Running Tests

Run the unit and integration tests using:

```bash
mvn test
```

## Configuration

### File Upload Directory

The default directory for file uploads is set to `uploads` in the project root. You can customize it in the `application.properties` file:

```properties
file.upload-dir=uploads
```


## Troubleshooting

- **Cannot connect to the database**: Ensure PostgreSQL is running and the database credentials in `application.properties` are correct.
- **File upload issues**: Verify that the `uploads` directory exists and is writable by the application.

## Future Enhancements

- **User Authentication and Authorization**: Implement user roles and permissions.
- **Email Notifications**: Notify users about task assignments and due dates.
- **Advanced Reporting**: Add dashboards for visualizing task progress and status.

