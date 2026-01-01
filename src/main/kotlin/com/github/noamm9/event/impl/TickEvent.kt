package com.github.noamm9.event.impl

import com.github.noamm9.event.Event

abstract class TickEvent: Event(false) {
    class Start: TickEvent()
    class End: TickEvent()

    class Server: TickEvent()
}