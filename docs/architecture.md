# Architecture

PocketLedger uses **Vertical Slice Architecture** at the screen layer combined with the **Android MVP** pattern. Persistence is shared in a horizontal `data/` layer (Room + AuthStore), following [Renvest-mobile](../Renvest-mobile) conventions.

## Vertical slicing

Code is organized by **feature capability**, not by technical layer:

```
screens/
├── login/          ← auth slice
├── register/
├── dashboard/      ← analytics hub
├── profile/
├── wallet/
├── transaction/    ← core engine
└── category/
```

Each folder is a self-contained slice containing:

- `{Feature}Activity.kt` — **View**
- `{Feature}Contract.kt` — View ↔ Presenter boundary
- `{Feature}Presenter.kt` — **Logic**
- `{Feature}Model.kt` — **Data facade**
- Optional screen-local adapters (e.g. `LedgerAdapter.kt`)

Shared persistence lives in `data/` because multiple slices read/write the same Room database.

## MVP roles

| Role | File | Responsibility |
|------|------|----------------|
| **View** | `{Feature}Activity.kt` + `activity_{feature}.xml` | Renders UI, captures input, navigation, Toast. No business logic. |
| **Presenter** | `{Feature}Presenter.kt` | Validates input, orchestrates flow, calls Model on IO thread, updates View. |
| **Model** | `{Feature}Model.kt` | Thin facade — one method per use case, delegates to AuthStore or Room DAOs. |
| **Contract** | `{Feature}Contract.kt` | Interface boundary between View and Presenter. |

The Presenter never calls Room directly. The Activity never calls the database.

## Request flow

```
User tap
  → Activity reads widget values
  → Presenter validates
  → Presenter launches coroutine (Dispatchers.IO)
  → Model calls AuthStore / DAO
  → Presenter receives PocketLedgerResult
  → Presenter calls view.showToast() / view.navigate*()
```

## Layer packages

| Package | Contents |
|---------|----------|
| `app/` | `PocketLedgerApplication` — lazy `AuthStore`, `PocketLedgerDatabase` |
| `screens/` | All feature slices |
| `data/` | `PocketLedgerResult`, `AuthStore`, Room entities/DAOs, `TransactionHelper` |
| `utils/` | `ActivityExtensions`, `FormValidation`, `ValidationUtils`, `PocketLedgerContext` |

## Dependency access

Activities wire dependencies manually (no DI framework):

```kotlin
presenter = LoginPresenter(
    this,
    LoginModel(authStore()),
    lifecycleScope,
    this,
)
```

Context extensions in `PocketLedgerContext.kt`:

- `authStore()` — session + auth operations
- `pocketLedgerDb()` — Room database singleton

## Result handling

Operations return `PocketLedgerResult<T>`:

```kotlin
sealed class PocketLedgerResult<out T> {
    data class Ok<T>(val value: T)
    sealed class Err {
        data class Validation(val reason: String)
        data class Storage(val reason: String)
        data class Network(val reason: String)
    }
}
```

Presenters use `notifyErrorIfNotOk { view.showToast(it) }` for error display.

## How to add a new screen

1. Create `screens/{feature}/` with Contract, Presenter, Model, Activity
2. Add `res/layout/activity_{feature}.xml` with `@+id/root`
3. Register Activity in `AndroidManifest.xml` as `.screens.{feature}.{Feature}Activity`
4. Add navigation from an existing screen via `startActivity()`
5. If new data is needed, add Room entity + DAO under `data/local/`
6. Document the screen in [screens.md](screens.md)

## Coroutine convention

Presenters receive `lifecycleScope` from the Activity. All database work runs on `Dispatchers.IO`; UI callbacks run on `Dispatchers.Main`.
