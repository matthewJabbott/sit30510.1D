# SIT305-10.1D
 
An AI-powered Android tutoring application built with Java and Jetpack for the SIT305 course assignment 10.1D.
 
## Project Overview
 
SIT305-10.1D is a mobile educational application that combines an Android client with a Python/Flask backend to deliver personalised quiz-based learning experiences. The app leverages Google's Gemini AI to dynamically generate quizzes on user-selected topics and provide intelligent feedback on assessment results.
 
### Key Features
 
- **User Authentication**: Secure login and registration system with SQLite database
- **Topic Selection**: Users can select from multiple learning topics or switch between them
- **AI-Generated Quizzes**: Dynamic quiz generation using Google Gemini API
- **Interactive Lessons**: Multi-question quiz format with radio button answers
- **AI-Powered Feedback**: Intelligent explanations of quiz results and learning recommendations
- **Hint System**: Context-aware hints provided by AI tutor during assessments
- **User Preferences**: Persistent storage of user interests and session management
## Technology Stack
 
### Android Client
- **Language**: Java
- **UI Framework**: Android XML layouts with ViewBinding
- **Architecture**: Activity-based with SharedPreferences for session management
- **Database**: SQLite (via Android SQLiteDatabase)
- **Networking**: Retrofit 2.9.0 with Gson serialization
- **Material Design**: Material Design 3 components
- **Target SDK**: 34
- **Minimum SDK**: 24 (Android 7.0)
### Backend Server
- **Language**: Python 3
- **Framework**: Flask with CORS support
- **AI Integration**: Google Genai (Gemini 2.5 Flash)
- **Port**: 8080
## Architecture Overview
 
### Android Application Flow
 
```
LoginActivity
    ↓
RegisterActivity (if new user)
    ↓
MainActivity (routing logic)
    ├─→ SetupActivity (if no interests selected)
    ├─→ TopicSelectionActivity (if multiple interests)
    └─→ LessonActivity (quiz flow)
            ↓
    AssessmentActivity (hint & submission)
            ↓
    ResultActivity (AI-generated feedback)
```
 
### Key Components
 
**Activities:**
- **LoginActivity**: User authentication against SQLite database
- **RegisterActivity**: New user registration with interest selection
- **MainActivity**: Central routing hub with session validation
- **SetupActivity**: Initial user setup and interest selection
- **TopicSelectionActivity**: Topic switching interface
- **LessonActivity**: Multi-question quiz display and answer submission
- **AssessmentActivity**: Single-question assessment with hint generation
- **ResultActivity**: Quiz results display with AI-powered explanations
**Services & Utilities:**
- **DatabaseHelper**: SQLite database management for users and credentials
- **QuizService**: Retrofit interface for backend communication
- **RetrofitClient**: Singleton for Retrofit client configuration
- **User**: Data model for user information
- **Question**: Data model for quiz questions
### Backend API Endpoints
 
**`GET /getQuiz`**
- **Query Parameters**: `topic` (string) — the quiz topic
- **Returns**: JSON object with quiz array containing questions and correct answers
- **Powered by**: Google Gemini 2.5 Flash
**`POST /explainResults`**
- **Request Body**: JSON with `score`, `total`, and `topic`
- **Returns**: JSON object with AI-generated explanation and learning recommendations
- **Powered by**: Google Gemini 2.5 Flash
## Getting Started
 
### Prerequisites
 
- **Android Development:**
  - Android Studio (latest stable)
  - Java 17 or higher
  - Gradle 8.x
  - Android SDK 34+
- **Backend Development:**
  - Python 3.8+
  - pip package manager
### Installation
 
#### Android Client
 
1. Clone the repository:
   ```bash
   git clone https://github.com/matthewJabbott/sit30510.1D.git
   cd sit30510.1D
   ```
 
2. Build the project:
   ```bash
   ./gradlew build
   ```
 
3. Install and run on emulator or device:
   ```bash
   ./gradlew installDebug
   ```
 
#### Backend Server
 
1. Navigate to the backend directory:
   ```bash
   cd backend-server
   ```
 
2. Create a virtual environment (recommended):
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
 
3. Install dependencies:
   ```bash
   pip install flask flask-cors google-genai python-dotenv
   ```
 
4. Set up environment variables — create a `.env` file with your Google API key:
   ```
   GOOGLE_API_KEY=your_api_key_here
   ```
 
5. Run the Flask server:
   ```bash
   python app.py
   ```
   The server will start on `http://localhost:8080`
## Configuration
 
**Android Emulator Backend Connection:** The app is configured to connect to `http://10.0.2.2:8080/`, which is the Android emulator's way of accessing the host machine's localhost.
 
For physical devices, update the base URL in the relevant Activity files to your machine's local IP address (e.g. `http://192.168.x.x:8080/`).
 
## Project Structure
 
```
sit30510.1D/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/sit30510_1d/
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SetupActivity.java
│   │   │   │   ├── TopicSelectionActivity.java
│   │   │   │   ├── LessonActivity.java
│   │   │   │   ├── AssessmentActivity.java
│   │   │   │   ├── ResultActivity.java
│   │   │   │   ├── DatabaseHelper.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Question.java
│   │   │   │   ├── QuizService.java (Retrofit interface)
│   │   │   │   └── RetrofitClient.java
│   │   │   ├── res/
│   │   │   │   ├── layout/         (XML layouts)
│   │   │   │   ├── values/         (strings, colors, styles)
│   │   │   │   └── mipmap/         (app icons)
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── backend-server/
│   ├── app.py                      (Flask application)
│   ├── .env                        (environment variables)
│   └── requirements.txt
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```
 
## Database Schema
 
The SQLite database stores user information:
 
**Users Table:**
- `username` (PRIMARY KEY)
- `password` (hashed)
- `interests` (comma-separated topics)
- `created_at` (timestamp)
## Testing
 
Run unit tests:
```bash
./gradlew test
```
 
Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```
 
## Key Implementation Details
 
### Session Management
 
User sessions are maintained via Android SharedPreferences with the key `UserPrefs`. The `current_user` preference stores the logged-in username.
 
### Quiz Generation Flow
 
1. User selects a topic and starts a lesson
2. Android client sends `GET` request to `/getQuiz?topic=TopicName`
3. Backend prompts Gemini AI to generate a 5-question quiz
4. Questions are parsed and displayed progressively
5. On completion, results are sent to `/explainResults` endpoint for AI feedback
### Error Handling
 
- Network errors display user-friendly messages
- Rate limiting (429 responses) from Gemini API are handled gracefully
- Missing user data defaults to "Technology" topic
- Database operations include null checks and try-catch blocks
## Build Configuration
 
- **Compile SDK**: 34
- **Java Compatibility**: Java 17
- **View Binding**: Enabled for type-safe view access
- **ProGuard**: Enabled for release builds (minification disabled)
## Important Notes
 
- **API Key**: The backend requires a Google Gemini API key set via environment variables. Never commit your `.env` file or hardcode keys in source code.
- **Network Security**: The app allows cleartext traffic for local development. Update `android:usesCleartextTraffic` before any production deployment.
- **Rate Limiting**: Google Gemini API has usage quotas. If receiving 429 errors, implement exponential backoff and user-level rate limiting.
## License
 
This project is part of the SIT305 course curriculum.
 
## Author
 
Matthew Jabbott
