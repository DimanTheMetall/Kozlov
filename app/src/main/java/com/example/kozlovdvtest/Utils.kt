package com.example.kozlovdvtest

inline fun <reified T> MutableList<T?>.fillNullsToIndex(index: Int) {
    if (index > this.lastIndex) {
        this.addAll(arrayOfNulls(index - this.lastIndex))
    }
}