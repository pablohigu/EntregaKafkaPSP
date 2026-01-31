package productor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import modelo.AppConfig;
import modelo.Constantes;
import modelo.Documento;
import util.JsonUtil;

import java.util.Properties;
import java.util.Random;

public class Emisor {
    private final Random random = new Random();
    private final String[] empleados = {"Miguel Goyena", "Ana Ruiz", "Carlos Diaz", "Lucia Fern"};

    public static void main(String[] args) {
        new Emisor().ejecutar();
    }

    public void ejecutar() {
        Properties props = new Properties();
        props.put("bootstrap.servers", AppConfig.get(Constantes.CFG_KAFKA_SERVER));
        props.put("key.serializer", AppConfig.get(Constantes.CFG_KEY_SER));
        props.put("value.serializer", AppConfig.get(Constantes.CFG_VAL_SER));

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            System.out.println("--- Emisor Iniciado ---");
            int contador = 1;

            while (true) {
                Documento doc = generarDocumentoAleatorio(contador++);
                String json = JsonUtil.toJson(doc);

                // sender como KEY para mantener orden por empleado si fuera necesario
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        AppConfig.get(Constantes.CFG_TOPIC_ENTRADA),
                        doc.getSender(),
                        json
                );

                producer.send(record);
                System.out.printf("[Enviado] %s (%s) de %s%n", doc.getTitulo(), doc.getTipo(), doc.getSender());

                Thread.sleep(AppConfig.getInt(Constantes.CFG_SLEEP_PROD));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Documento generarDocumentoAleatorio(int id) {
        String sender = empleados[random.nextInt(empleados.length)];
        boolean esColor = random.nextBoolean();
        String tipo = esColor ? Constantes.TIPO_COLOR : Constantes.TIPO_BN;
        String titulo = "Doc_" + id + "_" + (esColor ? "Color" : "BN");
        
        String textoBase = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ";
        String texto = textoBase.repeat(AppConfig.getInt(Constantes.CFG_TEST_REPETICIONES));

        return new Documento(titulo, texto, tipo, sender);
    }
}