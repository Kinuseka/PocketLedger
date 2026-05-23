# PocketLedger — Source Code Presentation

Walkthrough script for demonstrating **Vertical Slicing** and **Android MVP** to instructors or reviewers.

---

## 1. Vertical Slicing Approach

**Opening line:**

> "PocketLedger is organized by feature, not by technical layer. Each folder under `screens/` is a vertical slice containing everything needed for that capability — from the Android Activity down to the data access call."

**Show this tree in the IDE:**

```
app/src/main/java/com/macarambon/pocketledger/
├── app/PocketLedgerApplication.kt
├── data/                    ← shared persistence (Room + AuthStore)
├── screens/                 ← vertical slices
│   ├── login/
│   ├── register/
│   ├── dashboard/
│   ├── profile/
│   ├── wallet/
│   ├── transaction/
│   └── category/
└── utils/
```

**Key points:**

- 7 feature folders = 7 app capabilities
- `data/` is shared because wallets, transactions, and categories all use the same Room database
- Adding a new feature = add one folder under `screens/`, not edit unrelated layers

---

## 2. MVP Roles

| Role | What it is | Example file |
|------|-----------|--------------|
| **View** | Activity + XML — displays UI, forwards clicks | `TransactionActivity.kt` |
| **Presenter** | Logic — validates, orchestrates, updates View | `TransactionPresenter.kt` |
| **Model** | Data facade — wraps AuthStore / DAO calls | `TransactionModel.kt` |
| **Contract** | Interface boundary between View and Presenter | `TransactionContract.kt` |

**Quote:** "The Activity is dumb. The Presenter holds testable logic. The Model hides data access."

---

## 3. UI → Logic → Data Demo (Transaction slice)

Walk through these files **in order**:

### Step 1: `TransactionActivity.kt` (View)

- Show `setupPocketLedgerContent`, `findViewById`
- Show DatePicker with `datePicker.maxDate = System.currentTimeMillis()`
- Show RadioGroup listener toggling interest field visibility
- Show click listener calling `presenter.onSubmitClicked(...)` — **Activity only reads widgets**

### Step 2: `TransactionContract.kt`

- Point out `View` methods: `showToast`, `navigateToDashboard`, `setAppliedInterestVisible`
- Point out `Presenter` methods: `loadFormData`, `onSubmitClicked`, `onTransactionTypeSelected`
- **Contract decouples View from logic**

### Step 3: `TransactionPresenter.kt` (Logic)

- Show validation: amount > 0, strip interest rate for Income/Expense
- Show `scope.launch { withContext(Dispatchers.IO) { model.addTransaction(...) } }`
- Show result handling: success → navigate, error → `showToast`

### Step 4: `TransactionModel.kt` (Data facade)

- One-liner delegation to `TransactionHelper`
- **Presenter does not know about Room**

### Step 5: `TransactionHelper.kt` (Data / business rules)

- Show future date guard, positive amount, wallet balance update
- **Bottom of the slice — mirrors database trigger logic**

---

## 4. Second Demo: Register (validation)

Briefly show:

1. `RegisterActivity` — required field validation via `FormValidation.kt`
2. `RegisterPresenter` — `ValidationUtils.isValidEmail()` **before** calling the repository
3. `RegisterModel` → `AuthStore.register()` — creates user in Room + seeds categories

---

## 5. MVP Table (all 7 screens)

| Screen | View | Presenter | Model | Data |
|--------|------|-----------|-------|------|
| Login | `LoginActivity` | `LoginPresenter` | `LoginModel` | `AuthStore` |
| Register | `RegisterActivity` | `RegisterPresenter` | `RegisterModel` | `AuthStore` |
| Dashboard | `DashboardActivity` | `DashboardPresenter` | `DashboardModel` | Room DAOs |
| Profile | `ProfileActivity` | `ProfilePresenter` | `ProfileModel` | AuthStore + WalletDao |
| Wallet | `WalletActivity` | `WalletPresenter` | `WalletModel` | TransactionHelper |
| Transaction | `TransactionActivity` | `TransactionPresenter` | `TransactionModel` | TransactionHelper |
| Category | `CategoryActivity` | `CategoryPresenter` | `CategoryModel` | CategoryDao |

---

## 6. Closing talking points

- **Vertical slice:** "Each feature folder is a mini-app — UI, logic, and data together."
- **MVP separation:** "We can change the database without touching the Activity, and change the layout without touching business rules."
- **Renvest alignment:** "Same Contract/Presenter/Model naming, Room persistence, Material 3 UI, and coroutine-based Presenters as our reference project."

---

## Sequence diagram (optional slide)

```
TransactionActivity → TransactionPresenter → TransactionModel → TransactionHelper → Room DAO
        ↑                              |
        └──────── showToast / navigate ┘
```
