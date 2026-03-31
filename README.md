# Arirang Beauty Lounge 💅

An Android app for **Arirang Beauty Lounge** — a full-featured beauty salon management system with role-based access for Customers, Staff, and Owners.

---

## 📋 Features

### Customer
- Role selection on first launch
- Registration & login with Firebase Authentication
- Dashboard with quick access to services, bookings, chatbot & profile
- **AI Beauty Assistant** chatbot with FAQ responses about services, pricing, hours, booking & more
- Services catalogue with prices

### Staff
- Dedicated staff registration (with Employee ID)
- Dashboard showing today's schedule, assigned customers, and duties
- Firestore-driven schedule & booking data

### Owner
- Owner registration (with Salon Code)
- Dashboard with real-time stats: staff count, customer count, total bookings
- Management buttons for staff, bookings, inventory, reports, and schedules

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Min SDK | 21 (Android 5.0) |
| Target SDK | 34 (Android 14) |
| Authentication | Firebase Auth |
| Database | Firebase Firestore |
| UI | Material Design 3, ConstraintLayout |
| Architecture | Activity-based with View Binding |

---

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8+
- A Firebase project

### Step 1: Clone the repository
```bash
git clone https://github.com/your-org/Arirang-Beauty-Lounge.git
cd Arirang-Beauty-Lounge
```

### Step 2: Add `google-services.json` ⚠️ REQUIRED

> **This file is NOT included in the repository.** Without it the app will not compile.

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or open an existing one)
3. Add an Android app with package name: `com.arirang.beautylounge`
4. Download the `google-services.json` file
5. Place it in the `app/` directory:
   ```
   app/
   └── google-services.json   ← place here
   ```

### Step 3: Enable Firebase services
In the Firebase Console, enable:
- **Authentication** → Email/Password sign-in method
- **Firestore Database** → Start in test mode (or configure security rules)

### Step 4: Firestore Security Rules (recommended)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /bookings/{bookingId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    match /schedules/{staffId} {
      allow read: if request.auth != null && request.auth.uid == staffId;
      allow write: if request.auth != null;
    }
  }
}
```

### Step 5: Build & Run
1. Open the project in Android Studio
2. Let Gradle sync complete
3. Run on an emulator or physical device (API 21+)

---

## 📁 Project Structure

```
app/src/main/
├── java/com/arirang/beautylounge/
│   ├── SplashActivity.kt          # Entry point, auth check & role routing
│   ├── RoleSelectionActivity.kt   # Customer / Staff / Owner selection
│   ├── LoginActivity.kt           # Shared login screen (role-aware)
│   ├── CustomerRegistrationActivity.kt
│   ├── StaffRegistrationActivity.kt
│   ├── OwnerRegistrationActivity.kt
│   ├── CustomerDashboardActivity.kt
│   ├── StaffDashboardActivity.kt
│   ├── OwnerDashboardActivity.kt
│   ├── ChatbotActivity.kt         # FAQ chatbot with predefined responses
│   ├── ChatAdapter.kt             # RecyclerView adapter for chat bubbles
│   ├── ChatMessage.kt             # Data class for chat messages
│   └── ServicesActivity.kt        # Services & pricing catalogue
└── res/
    ├── layout/                    # All activity & item layouts
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   ├── themes.xml
    │   └── dimens.xml
    └── drawable/
        └── ic_launcher_foreground.xml
```

---

## 🗃️ Firestore Data Model

### `users/{uid}`
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+27 12 345 6789",
  "role": "customer | staff | owner",
  "employeeId": "EMP001",   // staff only
  "salonCode": "SALON01",   // owner only
  "createdAt": "<timestamp>"
}
```

### `bookings/{bookingId}`
```json
{
  "staffId": "<uid>",
  "customerId": "<uid>",
  "service": "Haircut",
  "date": "<timestamp>",
  "status": "confirmed"
}
```

### `schedules/{staffUid}`
```json
{
  "tasks": ["10:00 - Haircut for Alice", "14:00 - Manicure for Bob"]
}
```

---

## 📝 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
Mobile-based management system
