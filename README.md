# RempahRasa

RempahRasa is an Android application designed to classify spices using images captured from the camera or selected from the gallery. The app also features user authentication, history tracking, and a favorites system for spices.

## Features

- **Spice Classification**: Classify spices by capturing images using the camera or selecting from the gallery.
- **User Authentication**: Secure login and registration system.
- **History Tracking**: View the history of previously classified spices.
- **Favorites**: Mark spices as favorites and view them in a dedicated section.

## API & Documentation

For detailed API documentation, please refer to [RempahRasa API Documentation](https://documenter.getpostman.com/view/35228443/2sA3Qy6pDx#5cbe18a8-586d-4118-9e3a-2c0bf6303480).
*Please contact us for the BaseURL.

## Installation

### Prerequisites

- Android Studio
- Android device or emulator running API level 24 or higher

### Steps

1. **Clone the repository:**
    ```sh
    git clone https://github.com/yourusername/rempahrasa.git
    ```
2. **Open the project in Android Studio.**
3. **Add the BaseURL***
5. **Build the project:**
    - Let Android Studio download and install the required dependencies.
6. **Run the project:**
    - Connect your Android device or start an emulator.
    - Click on the "Run" button in Android Studio.

## Usage

1. **Register an account**: Open the app and register a new account.
2. **Login**: Use your registered credentials to log in.
3. **Classify Spice**:
    - Use the camera button to capture an image of a spice.
    - Or use the gallery button to select an image from your device.
4. **View History**: Navigate to the history tab to view previously classified spices.
5. **Manage Favorites**: Mark spices as favorites and view them in the favorites tab.

## Project Structure

- `MainActivity.kt`: Handles navigation and permissions.
- `ScanFragment.kt`: Manages spice classification through camera and gallery.
- `HistoryFragment.kt`: Displays the history of classified spices.
- `FavoritesFragment.kt`: Displays user's favorite spices.
- `LoginActivity.kt`: Manages user login.
- `RegisterActivity.kt`: Manages user registration.
- `ApiService.kt`: Defines API endpoints.
- `RetrofitInstance.kt`: Configures Retrofit for API calls.
