# Screens Reference

Each screen follows the MVP file set: `{Feature}Contract.kt`, `{Feature}Activity.kt`, `{Feature}Presenter.kt`, `{Feature}Model.kt`.

Package root: `com.macarambon.pocketledger.screens.{feature}`

---

## Login

| MVP | File / Source |
|-----|---------------|
| View | `LoginActivity.kt`, `activity_login.xml` |
| Presenter | `LoginPresenter.kt` |
| Model | `LoginModel.kt` → `AuthStore` |
| Data | `AuthStore.signInWithEmail()` |

**Navigation:**
- Success → `DashboardActivity` (clear task)
- Register link → `RegisterActivity`

**Validation:**
- Required email + password (`FormValidation.validateRequired`)
- Email regex (`ValidationUtils.isValidEmail`)

---

## Register

| MVP | File / Source |
|-----|---------------|
| View | `RegisterActivity.kt`, `activity_register.xml` |
| Presenter | `RegisterPresenter.kt` |
| Model | `RegisterModel.kt` → `AuthStore` |
| Data | `AuthStore.register()` + `CategorySeeder` |

**Fields:** First name, middle name, last name, email, password, confirm password

**Validation:**
- All fields required
- Email regex before repository call
- Password match check in Presenter

**Navigation:** Success → `LoginActivity` + Toast

---

## Dashboard

| MVP | File / Source |
|-----|---------------|
| View | `DashboardActivity.kt`, `LedgerAdapter.kt`, `activity_dashboard.xml` |
| Presenter | `DashboardPresenter.kt` |
| Model | `DashboardModel.kt` → Room DAOs |
| Data | `WalletDao`, `TransactionDao`, `CategoryDao` |

**Features:**
- Welcome message with first name
- Net worth, income (green), expense (red) totals
- RecyclerView ledger (newest first)
- Category filter Spinner
- Nav buttons: Profile, Wallets, Log Transaction, Categories
- Logout (clear task → Login)

**Refresh:** `onResume()` reloads data

---

## Profile

| MVP | File / Source |
|-----|---------------|
| View | `ProfileActivity.kt`, `activity_profile.xml` |
| Presenter | `ProfilePresenter.kt` |
| Model | `ProfileModel.kt` → AuthStore + WalletDao |
| Data | `UserEntity`, `WalletDao.getNetWorth()` |

**Displays:** Full name, email, total net worth

---

## Wallet

| MVP | File / Source |
|-----|---------------|
| View | `WalletActivity.kt`, `WalletAdapter.kt`, `activity_wallet.xml` |
| Presenter | `WalletPresenter.kt` |
| Model | `WalletModel.kt` → `TransactionHelper` |
| Data | `WalletDao` |

**Features:**
- Create wallet: name, starting amount, type (Checking/Savings/Cash)
- Conditional interest fields when Savings selected
- Portfolio RecyclerView with live `currentAmount`

---

## Transaction

| MVP | File / Source |
|-----|---------------|
| View | `TransactionActivity.kt`, `activity_transaction.xml` |
| Presenter | `TransactionPresenter.kt` |
| Model | `TransactionModel.kt` → `TransactionHelper` |
| Data | `TransactionDao`, `WalletDao` (balance update) |

**Features:**
- Wallet + category Spinners
- Amount (positive integer, `inputType="number"`)
- DatePicker with `maxDate = today`
- Type RadioGroup: Income / Expense / Interest
- Conditional applied interest rate (Interest only)

**Navigation:** Success → `DashboardActivity`

---

## Category

| MVP | File / Source |
|-----|---------------|
| View | `CategoryActivity.kt`, `CategoryAdapter.kt`, `activity_category.xml` |
| Presenter | `CategoryPresenter.kt` |
| Model | `CategoryModel.kt` → `CategoryDao` |
| Data | `CategoryDao` |

**Features:**
- List all user categories
- Search/filter by name
- Add custom category

---

## Navigation map

```
Login (launcher)
  ↔ Register
  → Dashboard
Dashboard → Profile, Wallet, Transaction, Category
Dashboard → Logout → Login
Secondary screens → Back (finish) → Dashboard
```
