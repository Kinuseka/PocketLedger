package com.macarambon.pocketledger.data

sealed class PocketLedgerResult<out T> {
    data class Ok<T>(val value: T) : PocketLedgerResult<T>()

    sealed class Err : PocketLedgerResult<Nothing>() {
        data class Storage(val reason: String) : Err()
        data class Validation(val reason: String) : Err()
    }
}

fun PocketLedgerResult<*>.notifyErrorIfNotOk(notify: (String) -> Unit) {
    when (this) {
        is PocketLedgerResult.Ok -> Unit
        is PocketLedgerResult.Err.Storage -> notify(reason)
        is PocketLedgerResult.Err.Validation -> notify(reason)
    }
}
