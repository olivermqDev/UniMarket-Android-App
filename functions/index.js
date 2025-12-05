const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifySellerOnNewOrder = functions.firestore
    .document("pedidos/{pedidoId}")
    .onCreate(async (snap, context) => {
      const pedidoId = context.params.pedidoId;
      const pedidoData = snap.data();

      // 1. Verificar estado "pago_reportado"
      if (pedidoData.estado !== "pago_reportado") {
        console.log(`Pedido ${pedidoId} no tiene estado pago_reportado. Estado actual: ${pedidoData.estado}`);
        return null;
      }

      const idComprador = pedidoData.idComprador;
      const idVendedor = pedidoData.idVendedor;
      const productos = pedidoData.productos || [];
      const total = pedidoData.total;
      const codigoYape = pedidoData.codigoYape;
      const urlComprobante = pedidoData.urlComprobante;
      const tipoEntrega = pedidoData.tipoEntrega;

      try {
        // 2. Buscar datos del Comprador
        const compradorSnapshot = await admin.firestore().collection("users").doc(idComprador).get();
        const compradorData = compradorSnapshot.data();
        const nombreComprador = compradorData ? (compradorData.name || "Cliente") : "Cliente";

        // 3. Formatear lista de productos
        // Formato: "[Nombre del Producto] (x[Cantidad]) - S/[Precio Unitario]"
        const productosFormateados = productos.map((p) => {
          return `${p.nombre} (x${p.cantidad}) - S/${p.precio}`;
        }).join(", ");

        // 4. Buscar token del Vendedor
        const vendedorSnapshot = await admin.firestore().collection("users").doc(idVendedor).get();
        const vendedorData = vendedorSnapshot.data();
        
        if (!vendedorData || !vendedorData.fcmToken) {
            console.log(`El vendedor ${idVendedor} no tiene token FCM.`);
            return null;
        }

        const fcmToken = vendedorData.fcmToken;

        // 5. Construir Notificación
        const message = {
          token: fcmToken,
          notification: {
            title: `¡NUEVO PAGO YAPE REPORTADO! Pedido #${pedidoId}`,
            body: `El comprador ${nombreComprador} ha reportado un pago de S/${total} por Yape. Productos: ${productosFormateados}. Código Yape: ${codigoYape}. Entrega: ${tipoEntrega}.`,
          },
          data: {
            urlComprobante: urlComprobante || "",
            idPedido: pedidoId,
            codigoYape: codigoYape || "",
            click_action: "FLUTTER_NOTIFICATION_CLICK" // O la acción que maneje tu app Android
          },
          android: {
            priority: "high",
            notification: {
                channelId: "seller_notifications" // Asegúrate de tener este canal en la app
            }
          }
        };

        // 6. Enviar Notificación
        const response = await admin.messaging().send(message);
        console.log("Notificación enviada exitosamente:", response);
        return response;

      } catch (error) {
        console.error("Error enviando notificación al vendedor:", error);
        return null;
      }
    });
