package com.example.meerkatservice.database

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_entity")
data class LocationEntity(
    /**
     * プライマリーキー（自動生成）
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    val locationId: Int,
    /**
     *  緯度
     */
    val latitude: Double,
    /**
     * 経度
     */
    val longitude: Double,
    /**
     * 高度（メートル）
     */
    val altitude: Double,
    /**
     * 水平精度（メートル）
     */
    val accuracy: Float,
    /**
     * 垂直精度
     */
    @ColumnInfo(name = "vertical_accuracy_meters")
    val verticalAccuracyMeters: Float,
    /**
     * 移動速度（m/s）
     */
    val speed: Float,
    /**
     * 速度精度
     */
    @ColumnInfo(name = "speed_accuracy_meters_per_second")
    val speedAccuracyMetersPerSecond: Float,
    /**
     * 方角（北を0とした度数）
     */
    val bearing: Float,
    /**
     * 方角の精度
     */
    @ColumnInfo(name = "bearing_accuracy_degrees")
    val bearingAccuracyDegrees: Float,
    /**
     * 位置情報のタイムスタンプ（UNIX time, ms）
     */
    val time: Long,
    /**
     * システム起動からのナノ秒（バッテリ節約に便利）
     */
    @ColumnInfo(name = "elapsed_realtime_nanos")
    val elapsedRealtimeNanos: Long,
    /**
     * 位置情報の提供元（"gps", "network", など）
     */
    val provider: String,
    /**
     * accuracy が存在するか
     */
    @ColumnInfo(name = "has_accuracy")
    val hasAccuracy: Boolean,
    /**
     * speed が存在するか
     */
    @ColumnInfo(name = "has_speed")
    val hasSpeed: Boolean,
    /**
     * altitude が存在するか
     */
    @ColumnInfo(name = "has_altitude")
    val hasAltitude: Boolean,
) {
    companion object {
        fun fromLocation(location: Location): LocationEntity {
            return LocationEntity(
                locationId = 0,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                verticalAccuracyMeters = location.verticalAccuracyMeters,
                speed = location.speed,
                speedAccuracyMetersPerSecond = location.speedAccuracyMetersPerSecond,
                bearing = location.bearing,
                bearingAccuracyDegrees = location.bearingAccuracyDegrees,
                time = location.time,
                elapsedRealtimeNanos = location.elapsedRealtimeNanos,
                provider = location.provider.toString(),
                hasAccuracy = location.hasAccuracy(),
                hasSpeed = location.hasSpeed(),
                hasAltitude = location.hasAltitude()
            )
        }

        fun fromLocations(locations: List<Location>): List<LocationEntity> {
            val list = mutableListOf<LocationEntity>()
            locations.forEach {
                val entity = fromLocation(it)
                list.add(entity)
            }
            return list
        }
    }
}
