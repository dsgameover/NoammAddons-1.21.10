package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import net.minecraft.world.entity.Entity

class EntityCheckRenderEvent(val entity: Entity): Event(cancelable = true)