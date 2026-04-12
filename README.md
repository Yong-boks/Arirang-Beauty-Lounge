# Arirang Beauty Lounge 💅

An Android app for **Arirang Beauty Lounge** — a full-featured beauty salon management system with role-based access for Customers, Staff, and Owners.

---

## 📋 Features

### Customer
- Role selection on first launch
- Registration & login with Firebase Authentication
- Email format validation and Forgot Password reset via email
- Dashboard with quick access to services, bookings, chatbot & profile
- **AI Beauty Assistant** chatbot with FAQ responses about services, pricing, hours, booking & more
- Services catalogue with prices
- Book appointments in a 4-step wizard (service → stylist → date/time → confirm)
- Scheduling conflict detection before confirming
- View, filter, cancel, and reschedule bookings

### Staff
- Staff registration requires a valid **Employee ID** (validated against the seeded `staffMembers` whitelist in Firestore)
- Dashboard showing today's appointment count and schedule summary
- Full 14-day upcoming schedule with "Mark as Completed" action
- Today's duties view (chronological)
- Customer visit history

### Owner
- Owner registration (with Salon Code)
- Dashboard with real-time stats: staff count, customer count, total bookings
- **Staff Management** – view all registered staff (name, employee ID, phone)
- **General Schedule** – all bookings for the next 14 days across all staff, filterable by status
- **Inventory Management** – add, edit, and delete products; low-stock indicator; backed by Firestore `inventory` collection
- **Reports & Analytics** – total and collected revenue, booking status breakdown, revenue by service category, top 5 services and top 5 staff by booking count

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
- **Firestore Database** → Start in test mode (or apply the security rules below)

### Step 4: Apply Firestore Security Rules
Copy the contents of `firestore.rules` (at the repository root) into the Firestore Rules editor in the Firebase Console, or deploy via the Firebase CLI:

```bash
firebase deploy --only firestore:rules
```

The rules enforce:
- Users can only read/write their own `users` document
- Customers can only create bookings for themselves
- Customers can update/reschedule/cancel their own bookings; staff/owners can only change `status` and `updatedAt`
- Services are read-only from the app
- Inventory is readable by all authenticated users and writable (owner-managed)

### Step 5: Staff Profiles (First Launch)
On the first app launch, `StaffInitializer` automatically seeds the `staffMembers` Firestore collection with 10 predefined staff profiles. This collection acts as the **employee ID whitelist** — staff must enter a valid ID from this list when registering their account.

No Firebase Auth accounts are created automatically. Staff register themselves through the app.

### Step 6: Build & Run
1. Open the project in Android Studio
2. Let Gradle sync complete
3. Run on an emulator or physical device (API 21+)

---

## 📁 Project Structure

```
app/src/main/
├── java/com/arirang/beautylounge/
│   ├── SplashActivity.kt               # Entry point, auth check & role routing
│   ├── RoleSelectionActivity.kt        # Customer / Staff / Owner selection
│   ├── LoginActivity.kt                # Shared login screen (role-aware, forgot password)
│   ├── CustomerRegistrationActivity.kt
│   ├── StaffRegistrationActivity.kt    # Validates employee ID against staffMembers whitelist
│   ├── OwnerRegistrationActivity.kt
│   ├── CustomerDashboardActivity.kt
│   ├── StaffDashboardActivity.kt
│   ├── OwnerDashboardActivity.kt
│   ├── BookingActivity.kt              # 4-step booking wizard with conflict detection
│   ├── MyBookingsActivity.kt           # View / cancel / reschedule bookings
│   ├── ManageScheduleActivity.kt       # Staff 14-day schedule
│   ├── MyDutiesActivity.kt             # Staff today's duties
│   ├── StaffCustomersActivity.kt       # Staff customer visit history
│   ├── OwnerAllBookingsActivity.kt     # Owner general schedule (14 days)
│   ├── OwnerManageStaffActivity.kt     # Owner staff list
│   ├── OwnerReportsActivity.kt         # Revenue, booking stats, top services/staff
│   ├── InventoryActivity.kt            # Product inventory CRUD
│   ├── InventoryItem.kt                # Inventory data class
│   ├── InventoryAdapter.kt             # Inventory RecyclerView adapter
│   ├── ChatbotActivity.kt              # FAQ chatbot with predefined responses
│   ├── ServicesActivity.kt             # Services & pricing catalogue
│   ├── MyProfileActivity.kt            # Customer profile view/edit
│   ├── TimeSlotUtils.kt                # Shared 30-min time slot generator
│   ├── StaffInitializer.kt             # Seeds staffMembers Firestore collection on first launch
│   ├── Booking.kt                      # Booking data class
│   ├── BookingAdapter.kt               # Booking RecyclerView adapter
│   ├── Service.kt                      # Service data class
│   ├── StaffMember.kt                  # StaffMember data class
│   └── ...
└── res/
    ├── layout/                         # All activity & item layouts
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   ├── themes.xml
    │   └── dimens.xml
    └── drawable/
```

---

## 🗃️ Firestore Data Model

### `users/{uid}`
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+254 712 345 678",
  "role": "customer | staff | owner",
  "employeeId": "EMP001",   // staff only
  "salonCode": "SALON01",   // owner only
  "createdAt": "<timestamp>"
}
```

### `bookings/{bookingId}`
```json
{
  "bookingId": "<document ID>",
  "customerId": "<Firebase Auth UID>",
  "customerName": "Jane Doe",
  "staffId": "EMP001",
  "staffName": "Amara Njeri",
  "serviceId": "haircut",
  "serviceName": "Haircut",
  "serviceCategory": "Hair Services",
  "date": "Mon, 14 Apr 2026",
  "time": "10:00 AM",
  "price": 1000,
  "durationMin": 20,
  "durationMax": 30,
  "status": "Confirmed | Completed | Cancelled",
  "createdAt": 1712345678000
}
```

### `staffMembers/{employeeId}`
```json
{
  "employeeId": "EMP001",
  "name": "Amara Njeri",
  "email": "amara.njeri@arirang.com",
  "title": "Senior Hair Stylist",
  "services": ["Hair Services"],
  "status": "active",
  "createdAt": "<timestamp>"
}
```

### `inventory/{itemId}`
```json
{
  "name": "Wella Colour Cream",
  "quantity": 12,
  "unit": "tubes",
  "lowStockThreshold": 3,
  "updatedAt": 1712345678000
}
```

---

## 📝 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

