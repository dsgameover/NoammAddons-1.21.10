package com.github.noamm9.utils

import com.github.noamm9.NoammAddons.MOD_NAME
import com.github.noamm9.NoammAddons.logger
import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.event.EventBus.register
import com.github.noamm9.event.impl.TickEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object ThreadUtils {
    private data class TickTask(var ticks: Int, val action: () -> Unit)

    private fun createDaemonFactory(name: String): ThreadFactory {
        return ThreadFactory { r ->
            Thread(r, name).apply { isDaemon = true }
        }
    }

    private val asyncExecutor = Executors.newCachedThreadPool(createDaemonFactory("$MOD_NAME-Async"))
    private val scheduler = Executors.newScheduledThreadPool(1, createDaemonFactory("$MOD_NAME-Scheduler"))
    private val tickTasks = ConcurrentLinkedQueue<TickTask>()


    fun runOnMcThread(block: () -> Unit) {
        if (mc.isSameThread) safeRun(block)
        else mc.execute { safeRun(block) }
    }

    fun runAsync(block: () -> Unit) {
        asyncExecutor.submit { safeRun(block) }
    }

    fun setTimeout(delay: Long, block: () -> Unit): ScheduledFuture<*> {
        return scheduler.schedule({ safeRun(block) }, delay, TimeUnit.MILLISECONDS)
    }

    fun scheduledTask(ticks: Int = 0, block: () -> Unit) {
        tickTasks.add(TickTask(ticks, block))
    }

    fun loop(delayProvider: () -> Number, stopCondition: () -> Boolean = { false }, block: () -> Unit) {
        val taskWrapper = object: Runnable {
            override fun run() {
                safeRun(block)
                if (! stopCondition()) {
                    scheduler.schedule(this, delayProvider().toLong(), TimeUnit.MILLISECONDS)
                }
            }
        }
        scheduler.execute(taskWrapper)
    }

    fun loop(delay: Number, stopCondition: () -> Boolean = { false }, block: () -> Unit) {
        loop({ delay }, stopCondition, block)
    }

    fun init() {
        register<TickEvent.Start> {
            if (tickTasks.isEmpty()) return@register

            tickTasks.removeIf { entry ->
                if (entry.ticks <= 0) {
                    safeRun(entry.action)
                    true
                }
                else {
                    entry.ticks --
                    false
                }
            }
        }
    }

    private inline fun safeRun(block: () -> Unit) {
        try {
            block()
        }
        catch (e: Throwable) {
            logger.error("Error in ThreadUtils task: ${e.message}")
            e.printStackTrace()
        }
    }
}