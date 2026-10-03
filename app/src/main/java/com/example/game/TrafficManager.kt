package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.engine3d.Polygon3D
import com.example.engine3d.Vector3
import com.example.engine3d.World3D
import com.example.model.TrafficType
import kotlin.math.abs
import kotlin.random.Random

data class TrafficVehicle(
    val id: Int,
    val type: TrafficType,
    var x: Float, // Lane offset (-4.5 to +4.5)
    var z: Float, // Position along highway
    var speedKmh: Float,
    val isOncoming: Boolean = false,
    var targetX: Float = 0f
)

class TrafficManager {
    private var nextId = 1
    val vehicles = mutableListOf<TrafficVehicle>()

    fun reset(playerZ: Float) {
        vehicles.clear()
        // Pre-populate traffic ahead and behind
        for (i in 1..10) {
            spawnVehicleAhead(playerZ + i * 45f)
        }
    }

    private fun spawnVehicleAhead(spawnZ: Float) {
        val isOncoming = Random.nextFloat() < 0.45f
        val type = when (Random.nextInt(10)) {
            0, 1 -> TrafficType.CNG_AUTO
            2 -> TrafficType.RICKSHAW
            3, 4 -> TrafficType.TRUCK
            5 -> TrafficType.LOCAL_BUS
            6, 7 -> TrafficType.CAR
            8 -> TrafficType.MOTORCYCLE
            else -> TrafficType.PEDESTRIAN
        }

        // Lanes:
        // Same direction: x = +1.8f (slow lane), x = -1.8f (fast lane)
        // Oncoming: x = -5.2f, x = -8.2f (or on 2-lane road: x = -2.5f)
        val laneX = if (isOncoming) {
            if (Random.nextBoolean()) -4.2f else -7.2f
        } else {
            if (type == TrafficType.RICKSHAW || type == TrafficType.PEDESTRIAN) 4.2f
            else if (Random.nextBoolean()) 1.8f else -1.8f
        }

        val baseSpeed = when (type) {
            TrafficType.RICKSHAW -> 18f
            TrafficType.PEDESTRIAN -> 5f
            TrafficType.CNG_AUTO -> 45f
            TrafficType.TRUCK -> 52f
            TrafficType.LOCAL_BUS -> 60f
            TrafficType.CAR -> 85f
            TrafficType.MOTORCYCLE -> 78f
        }

        val speed = if (isOncoming) -baseSpeed else baseSpeed

        vehicles.add(
            TrafficVehicle(
                id = nextId++,
                type = type,
                x = laneX,
                z = spawnZ,
                speedKmh = speed,
                isOncoming = isOncoming,
                targetX = laneX
            )
        )
    }

    fun update(deltaSec: Float, playerZ: Float, playerHonked: Boolean) {
        val iter = vehicles.iterator()
        while (iter.hasNext()) {
            val v = iter.next()

            // Move vehicle forward/backward
            val speedMps = (v.speedKmh * 1000f) / 3600f
            v.z += speedMps * deltaSec

            // If player honks, slow vehicles yield slightly to shoulder
            if (playerHonked && abs(v.z - playerZ) < 35f && !v.isOncoming) {
                if (v.x > 0f) {
                    v.targetX = (v.x + 0.8f).coerceAtMost(4.5f)
                }
            }

            // Smooth lane shift
            v.x += (v.targetX - v.x) * 2.0f * deltaSec

            // Remove vehicles that are too far behind or far ahead
            if (v.z < playerZ - 80f || v.z > playerZ + 350f) {
                iter.remove()
            }
        }

        // Maintain density
        val aheadCount = vehicles.count { it.z > playerZ }
        if (aheadCount < 9) {
            val maxZ = vehicles.maxOfOrNull { it.z } ?: playerZ
            spawnVehicleAhead(maxZ + Random.nextFloat() * 40f + 25f)
        }
    }

    fun checkCollision(playerX: Float, playerZ: Float, playerWidth: Float, playerLength: Float): TrafficVehicle? {
        val pHalfW = playerWidth / 2f
        val pHalfL = playerLength / 2f

        for (v in vehicles) {
            val vHalfW = v.type.widthM / 2f
            val vHalfL = v.type.lengthM / 2f

            val dx = abs(playerX - v.x)
            val dz = abs(playerZ - v.z)

            if (dx < (pHalfW + vHalfW) && dz < (pHalfL + vHalfL)) {
                return v
            }
        }
        return null
    }

    fun generatePolygons(headlightsOn: Boolean): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()
        for (v in vehicles) {
            val yaw = if (v.isOncoming) Math.PI.toFloat() else 0f
            polys.addAll(
                World3D.buildTrafficVehicle(
                    type = v.type,
                    pos = Vector3(v.x, 0f, v.z),
                    yawRad = yaw,
                    headlightsOn = headlightsOn
                )
            )
        }
        return polys
    }
}
