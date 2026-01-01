package com.github.noamm9.event.impl

import com.github.noamm9.event.Event

abstract class ServerEvent: Event() {
    class Connect : ServerEvent()
    class Disconnect : ServerEvent()
}
