@file:Suppress("UNCHECKED_CAST")

package com.github.noamm9.event

import com.github.noamm9.NoammAddons
import com.github.noamm9.utils.ChatUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

object EventBus {
    val listeners = ConcurrentHashMap<Class<out Event>, CopyOnWriteArraySet<EventListener>>()

    @JvmStatic
    fun post(event: Event): Boolean {
        listeners[event.javaClass]?.forEach {
            runCatching {
                it.listener.invoke(EventContext(event))
            }.onFailure { exception ->
                NoammAddons.logger.error("EventBus", exception)
                ChatUtils.modMessage("§c§lError in event: ${event.javaClass.name}. Error: ${exception.message}")
            }
        }
        return if (event.cancelable) event.isCanceled else false
    }

    @JvmStatic
    inline fun <reified T : Event> register(noinline listener: EventContext<T>.() -> Unit): EventListener {
        val wrappedListener: EventContext<out Event>.() -> Unit = { listener.invoke(EventContext(this.event as T)) }
        val eventListener = EventListener(T::class.java, wrappedListener as EventContext<Event>.() -> Unit)
        listeners.getOrPut(T::class.java) { CopyOnWriteArraySet() }.add(eventListener)
        return eventListener
    }

    class EventContext<T : Event>(val event: T)
}