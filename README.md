# PocketLedger

PocketLedger is a mobile ledger app for young entrepreneurs and students. Track income and expenses across multiple wallets, categorize spending, and view your net worth at a glance.

This repository contains the native **Android** client (`:app`), application id `com.macarambon.pocketledger`.

## Requirements

- **JDK 17** (use Android Studio JBR if your system JDK is newer)
- **Android Studio** Koala or newer recommended
- Android **SDK** with `compileSdk 34` as configured in the module

## Build and run

Set `JAVA_HOME` to JDK 17 if needed, then:

```bash
./gradlew assembleDebug
```

Create `local.properties` with your SDK path if it is missing:

```properties
sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
```

Install the debug APK on a device or emulator, or run from Android Studio.

## Architecture

The app follows a layered structure aligned with [Renvest-mobile](https://github.com/gael55x/Renvest-mobile):

| Layer | Package | Role |
|--------|---------|------|
| Application | `com.macarambon.pocketledger.app` | `PocketLedgerApplication`, shared `AuthStore` + `PocketLedgerDatabase` wiring |
| Screens | `com.macarambon.pocketledger.screens.*` | Vertical feature slices (login, register, dashboard, profile, wallet, transaction, category) |
| Data | `com.macarambon.pocketledger.data` | `PocketLedgerResult`, `AuthStore`, **Room/SQLite** (users, wallets, categories, transactions) |
| Utils | `com.macarambon.pocketledger.utils` | Activity extensions, form helpers, `authStore()`, `pocketLedgerDb()` |

More detail:

- [docs/architecture.md](docs/architecture.md) — Vertical slicing, MVP pattern, how to add a screen
- [docs/database.md](docs/database.md) — Room schema, business rules, ER diagram
- [docs/screens.md](docs/screens.md) — Per-screen reference (files, navigation, validation)
- [docs/ui-guide.md](docs/ui-guide.md) — Material 3 theme, layouts, form conventions
- [PRESENTATION.md](PRESENTATION.md) — Code walkthrough script for Vertical Slicing + MVP demo

## UI and design system

- **Theme:** `Theme.PocketLedger` (Material 3 DayNight, no action bar)
- **Text fields:** `Widget.PocketLedger.TextInput.Outlined`
- **Buttons:** `Widget.PocketLedger.Button.Primary` / `.Outlined` / `.Text`
- **Spacing:** `padding_screen_*`, `spacing_section`, `spacing_field` in `res/values/dimens.xml`
- **Edge-to-edge:** use `setupPocketLedgerContent(R.layout.*, R.id.root)` on every Activity

## Git workflow

- **Branches:** `feature/*`, `fix/*`, `refactor/*`
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/) — e.g. `feat: add wallet screen`, `fix: reject future transaction dates`

## License

This project is open source. See the [LICENSE](LICENSE) file for details.
