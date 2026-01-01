package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import net.minecraft.world.entity.Entity

class EntityDeathEvent(val entity: Entity): Event(false)
