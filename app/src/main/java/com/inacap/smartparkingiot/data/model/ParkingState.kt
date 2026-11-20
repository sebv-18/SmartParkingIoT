package com.inacap.smartparkingiot.data.model

enum class BarrierState {
    OPEN,
    CLOSED
}

data class ParkingState(
    val maxCapacity: Int = 20,
    val occupiedSpots: Int = 0,
    val isBluetoothConnected: Boolean = false,
    val isSyncConnected: Boolean = false,
    val isEmergencyMode: Boolean = false,
    val isFailSecureActive: Boolean = true,
    val barrierState: BarrierState = BarrierState.CLOSED
) {
    val availableSpots: Int
        get() = (maxCapacity - occupiedSpots).coerceAtLeast(0)

    fun withAddedVehicle(): ParkingState =
        copy(occupiedSpots = (occupiedSpots + 1).coerceAtMost(maxCapacity))

    fun withRemovedVehicle(): ParkingState =
        copy(occupiedSpots = (occupiedSpots - 1).coerceAtLeast(0))

    fun withResetCounter(): ParkingState =
        copy(occupiedSpots = 0)

    fun withConnectionLossFailSecure(): ParkingState =
        copy(
            isBluetoothConnected = false,
            barrierState = BarrierState.CLOSED,
            isFailSecureActive = true
        )
}