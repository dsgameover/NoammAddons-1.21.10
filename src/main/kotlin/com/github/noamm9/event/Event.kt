package com.github.noamm9.event

abstract class Event(val cancelable: Boolean) {
    var isCanceled = false
}