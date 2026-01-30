package consumidor;
import org.apache.kafka.clients.consumer.*;

import modelo.Config;

import java.time.Duration;
import java.util.*;

public class Impresoras {
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        
        for (int i = 1; i <= 3; i++) new Thread(new TareaImpresora("BN", i)).start();
        for (int i = 1; i <= 2; i++) new Thread(new TareaImpresora("Color", i)).start();
    }

    static class TareaImpresora implements Runnable {
        String tipo;
        int id;

        public TareaImpresora(String t, int i) { tipo = t; id = i; }

        public void run() {
            Properties props = new Properties();
            props.put("bootstrap.servers", Config.get("kafka.server"));
            
            String grupo = tipo.equals("Color") ? Config.get("group.impresoras.color") : Config.get("group.impresoras.bn");
            String topic = tipo.equals("Color") ? Config.get("topic.impresion.color") : Config.get("topic.impresion.bn");
            
            props.put("group.id", grupo);
            props.put("key.deserializer", Config.get("kafka.key.deserializer"));
            props.put("value.deserializer", Config.get("kafka.value.deserializer"));

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(topic));
            System.out.println("[INFO] Impresora " + tipo + "-" + id + " online.");

            while (true) {
                for (ConsumerRecord<String, String> r : consumer.poll(Duration.ofMillis(500))) {
                    System.out.println("[Imprimiendo][" + tipo + "-" + id + "] " + r.value());
                    try { 
                        Thread.sleep(Config.getInt("app.tiempo.impresion")); 
                    } catch (Exception e) {}
                }
            }
        }
    }
}