# Expiry Reminder - Android App

A modern Android application built with Kotlin and Jetpack Compose to manage product expiration dates with reliable system-level reminders.

## Features

### Product Management
- Add, edit, and delete products with expiration tracking
- Two ways to set expiration dates:
  - Direct date selection
  - Production date + shelf life calculation
- Local storage using Room database

### Reliable Reminders via Calendar Provider
The app leverages Android's Calendar Provider to create highly reliable reminders that persist even if the app is killed by the system.

#### Notification Reminders
- Creates calendar events with standard notification alerts
- Appears in the user's system calendar
- Uses the Android notification system

#### Alarm Clock Reminders (with Graceful Degradation)
- **Primary Strategy**: Attempts to use the calendar's native alarm method (METHOD_ALARM)
  - Works on some Android systems (like MIUI)
  - Creates a calendar event with alarm-type reminder
- **Fallback Strategy**: If native alarm method fails or is unsupported:
  - Falls back to AlarmManager for precise alarm scheduling
  - Creates a visual calendar event for reference (without duplicate notifications)
  - Launches a full-screen alarm activity with sound when triggered

### Reminder Configuration
- Set how many days before expiration to be reminded (e.g., 3 days before)
- Choose specific time of day for reminders (e.g., 9:00 AM)
- Select reminder method: Notification or Alarm

## Technical Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Database**: Room
- **Architecture Pattern**: MVVM (UI → ViewModel → Repository → DataSource)
- **Build System**: Gradle with Kotlin DSL
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

### Project Structure
```
com.example.expiryreminder
├── data                    # Data layer
│   ├── ProductEntity       # Room entity
│   ├── ProductDao          # Database operations
│   ├── AppDatabase         # Room database
│   ├── ProductRepository   # Repository interface and implementation
│   └── Mappers             # Entity <-> Domain mappers
├── domain                  # Domain models
│   ├── Product             # Domain model
│   └── ReminderMethod      # Enum for reminder types
├── calendar                # Calendar Provider integration
│   └── CalendarEventManager
├── alarm                   # AlarmManager integration
│   ├── ReminderScheduler
│   ├── ReminderReceiver    # BroadcastReceiver for alarms
│   └── FullScreenAlarmActivity
├── reminder                # Business logic coordination
│   └── ReminderCoordinator # Orchestrates calendar and alarm scheduling
├── ui                      # UI layer
│   ├── home                # Home screen (product list)
│   ├── addedit             # Add/Edit screen (product form)
│   └── theme               # Material theme configuration
└── core                    # Application core
    └── AppGraph            # Dependency injection
```

### Key Design Patterns
- **Repository Pattern**: Abstracts data sources
- **MVVM**: Separation of UI, business logic, and data
- **Graceful Degradation**: Falls back to AlarmManager when calendar alarms aren't supported
- **Reactive Programming**: Uses Kotlin Flow for reactive data updates

## Permissions

The app requires the following permissions:

- **READ_CALENDAR**: Read existing calendar events
- **WRITE_CALENDAR**: Create and modify calendar events
- **SCHEDULE_EXACT_ALARM**: (Android 12+) Schedule precise alarms
- **USE_FULL_SCREEN_INTENT**: Display full-screen alarm activity

## How It Works

### Creating a Reminder

1. User creates a product with:
   - Product name
   - Expiration date (direct or calculated from production date + shelf life)
   - Reminder settings (days before, time, method)

2. App saves product to Room database

3. Based on selected reminder method:

   **For Notification Reminders:**
   - Creates a calendar event via CalendarContract
   - Sets a notification-type reminder
   - Saves calendar event ID to database

   **For Alarm Reminders:**
   - Tries to create calendar event with METHOD_ALARM
   - If successful, uses system calendar's alarm feature
   - If unsuccessful:
     - Schedules alarm via AlarmManager
     - Creates a silent calendar event for visual reference
     - When alarm triggers, launches full-screen activity with sound

### Updating a Reminder

1. User edits product details
2. App updates Room database
3. Updates calendar event using stored event ID
4. If using alarm method, cancels old alarm and schedules new one

### Deleting a Reminder

1. User deletes product
2. App removes product from Room database
3. Deletes calendar event using stored event ID
4. Cancels any scheduled AlarmManager alarms

## Building and Running

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK with API 34

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew installDebug
```

Or open the project in Android Studio and run from the IDE.

## Notes

- The app stores calendar event IDs to maintain the link between products and their reminders
- Calendar events are created in the user's default calendar
- The graceful degradation strategy ensures alarms work even on devices that don't support calendar METHOD_ALARM
- Full-screen alarm activities work even when the device is locked (requires USE_FULL_SCREEN_INTENT permission)
