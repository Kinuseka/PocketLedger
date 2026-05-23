# UI Guide

PocketLedger uses **Material 3** with a simplified design system based on Renvest-mobile conventions.

## Theme

- **Base:** `Theme.PocketLedger` (`Theme.Material3.DayNight.NoActionBar`)
- **Defined in:** `res/values/themes.xml`, `res/values-night/themes.xml`
- **Colors:** `res/values/colors.xml` — primary `#1A237E`, positive `#2E7D32`, negative `#C62828`

Prefer theme attributes where possible:

- `?android:attr/colorBackground`
- `?attr/colorPrimary`
- `?attr/colorOnSurface`

## Spacing

Defined in `res/values/dimens.xml`:

| Token | Value |
|-------|-------|
| `padding_screen_horizontal` | 24dp |
| `padding_screen_vertical` | 24dp |
| `spacing_section` | 24dp |
| `spacing_field` | 16dp |
| `spacing_small` | 8dp |
| `input_corner` | 12dp |

## Components

### Text fields

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.PocketLedger.TextInput.Outlined"
    ...>
    <com.google.android.material.textfield.TextInputEditText ... />
</com.google.android.material.textfield.TextInputLayout>
```

### Buttons

| Style | Use |
|-------|-----|
| `Widget.PocketLedger.Button.Primary` | Main actions (login, save, submit) |
| `Widget.PocketLedger.Button.Outlined` | Secondary nav (back, profile) |
| `Widget.PocketLedger.Button.Text` | Text links (register, login) |

Destructive actions (logout) override `backgroundTint` to `@color/negative`.

## Activity setup

Every activity layout must have `@+id/root` as the root or ScrollView id.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupPocketLedgerContent(R.layout.activity_example, R.id.root)
    // ...
}
```

`setupPocketLedgerContent()` enables edge-to-edge and applies system bar insets.

## Layout naming

| Pattern | Example |
|---------|---------|
| `activity_{screen}.xml` | `activity_login.xml` |
| `item_{purpose}.xml` | `item_ledger_entry.xml` |

## View ID naming

Lowercase concatenated type + name, matching Kotlin properties:

| XML ID | Kotlin |
|--------|--------|
| `textinputEmailLayout` | `textinputEmailLayout` |
| `buttonLogin` | `buttonLogin` |
| `recyclerviewLedger` | `recyclerviewLedger` |

## Form validation

**Activity layer** — required fields via `FormValidation.kt`:

```kotlin
textinputEmailLayout.validateRequired(getString(R.string.error_field_required))
textinputEmailLayout.valueText()
```

**Presenter layer** — cross-field rules (password match, email regex, amount > 0, date not future)

**Data layer** — `TransactionHelper` and `AuthStore` enforce business rules and return `PocketLedgerResult.Err.Validation`

## Color coding

| Element | Color |
|---------|-------|
| Income / Interest amounts | `@color/positive` (green) |
| Expense amounts | `@color/negative` (red) |
| Income total on dashboard | Green text |
| Expense total on dashboard | Red text |

## RecyclerView rows

Ledger, wallet, and category lists use dedicated `item_*.xml` layouts inflated by screen-local adapters in the same `screens/{feature}/` package.
