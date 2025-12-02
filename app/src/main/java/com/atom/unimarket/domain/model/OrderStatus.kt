package com.atom.unimarket.domain.model

sealed class OrderStatus(val value: String) {
    object PagoReportado : OrderStatus("pago_reportado")
    object PagoRechazado : OrderStatus("pago_rechazado")
    object PagoConfirmado : OrderStatus("pago_confirmado")
    object EnPreparacion : OrderStatus("en_preparacion")
    object EnCamino : OrderStatus("en_camino")
    object Entregado : OrderStatus("entregado")

    companion object {
        fun fromString(value: String): OrderStatus {
            return when (value) {
                "pago_reportado" -> PagoReportado
                "pago_rechazado" -> PagoRechazado
                "pago_confirmado" -> PagoConfirmado
                "en_preparacion" -> EnPreparacion
                "en_camino" -> EnCamino
                "entregado" -> Entregado
                else -> PagoReportado // Default or handle error
            }
        }
    }
}
