package com.github.noamm9.event

data class EventListener(val eventClass: Class<out Event>, val listener: EventBus.EventContext<Event>.() -> Unit) {
    fun unregister() = also { EventBus.listeners[eventClass]?.remove(this) }
    fun register() = also { EventBus.listeners[eventClass]?.add(it) }
}