package productor; // O el paquete que estés usando (ej: impresion.productor)

import org.apache.kafka.clients.producer.*;
import com.fasterxml.jackson.databind.ObjectMapper;

// Asegúrate de que estos imports coinciden con donde tienes tus clases
import modelo.Config;     
import modelo.Documento;  

import java.util.Properties;
import java.util.Random;

public class Emisor {
    
    public void iniciar() {
        Properties props = new Properties();
        props.put("bootstrap.servers", Config.get("kafka.server"));
        props.put("key.serializer", Config.get("kafka.key.serializer"));
        props.put("value.serializer", Config.get("kafka.value.serializer"));

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ObjectMapper mapper = new ObjectMapper();
            Random random = new Random();
            int contador = 1;

            // --- LISTA DE EMPLEADOS ---
            // Añadimos varios nombres para probar la creación de distintas carpetas
            String[] empleados = { 
                "Pablo Higuero", 
                "Ana Martinez", 
                "David Garcia", 
                "Laura Ruiz", 
                "Javier Lopez" 
            };

            System.out.println("--- Oficina abierta: Varios empleados enviando trabajos ---");

            while (true) {
                // 1. Elegir empleado al azar
                String remitenteActual = empleados[random.nextInt(empleados.length)];

                // 2. Elegir tipo al azar
                boolean esColor = random.nextBoolean();
                String tipo = esColor ? "Color" : "B/N";
                String titulo = (esColor ? "Catalogo_" : "Informe_") + contador;
                
                String textoBase = "Contenido de prueba para rellenar espacio. ";
                String textoLargo = textoBase.repeat(Config.getInt("app.test.repeticiones")); 

                Documento doc = new Documento(titulo, textoLargo, tipo, remitenteActual);
                
                producer.send(new ProducerRecord<>(Config.get("topic.recepcion"), doc.sender, mapper.writeValueAsString(doc)));
                
                System.out.println("[Enviado] " + doc.titulo + " (" + tipo + ") por: " + remitenteActual);
                
                contador++;
                Thread.sleep(Config.getInt("app.tiempo.envio")); 
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        new Emisor().iniciar();
    }
}