

# Sistema de Gestión de Colas de Impresión (Kafka + Java)

Este proyecto implementa un sistema de mensajería asíncrono para gestionar una cola de impresión compleja utilizando **Apache Kafka** y **Java**.

El sistema cumple con los requerimientos de la empresa:
* **Recepción** de documentos JSON.
* **Procesamiento paralelo:** Archivado (guardar original) y Transformación (paginación).
* **Enrutamiento inteligente** a impresoras de B/N y Color.

---

## 1. Arquitectura de Topics y Diseño

El sistema utiliza el patrón de mensajería **Fan-Out** (Difusión) para garantizar que los procesos de guardado y transformación ocurran simultáneamente sin bloquearse.

### Diagrama de Flujo

```text
[Productor: Empleados]
       |
       v
( TOPIC: cola-recepcion ) --------------------------------+
       |                                                  |
       v                                                  v
[Consumidor A: Archivador]                    [Consumidor B: Transformador]
       |                                     (Divide y decide B/N o Color)
       v                                                  |
[Disco: Archivos Originales]             +----------------+----------------+
                                         |                                 |
                                         v                                 v
                             ( TOPIC: cola-impresion-bn )      ( TOPIC: cola-impresion-color )
                                         |                                 |
                                         v                                 v
                                 [Impresoras B/N x3]               [Impresoras Color x2]

```

### Definición de Topics (Kafka)

| Topic | Propósito | Formato del Mensaje |
| --- | --- | --- |
| **`cola-recepcion`** | Entrada principal. Recibe los trabajos de los empleados. | JSON Original (Título, Documento completo, Sender, Tipo) |
| **`cola-impresion-bn`** | Cola de salida para documentos procesados en Blanco y Negro. | JSON Paginado (400 chars máx) |
| **`cola-impresion-color`** | Cola de salida para documentos procesados en Color. | JSON Paginado (400 chars máx) |

### Consumer Groups (Paralelismo)

Para cumplir el requisito de eficiencia máxima, se configuran grupos de consumo específicos en `app.properties`:

* **`grupo-archivador`**: Lee de `cola-recepcion`.
* **`grupo-transformador`**: Lee de `cola-recepcion`. Al tener un ID distinto al archivador, Kafka entrega una copia del mensaje a ambos procesos a la vez.
* **`grupo-impresoras-BN`**: Compartido por 3 hilos de impresión (balanceo de carga).
* **`grupo-impresoras-Color`**: Compartido por 2 hilos de impresión.

---

## 2. Guía para el Desarrollador

Información técnica sobre la construcción y estructura del proyecto.

### Requisitos Previos

* **Java:** JDK 17 o superior.
* **Maven:** 3.8 o superior.
* **Apache Kafka:** 3.6+ corriendo en `localhost:9092`.

### Estructura del Código (Clean Code)

El proyecto está modularizado para facilitar el mantenimiento:

* `src/main/resources/app.properties`: **Configuración centralizada.** Define los nombres de los topics, rutas de salida y tiempos de simulación.
* `com.empresa.config`: Clases de carga de configuración y constantes.
* `com.empresa.productor`: Simulación de envío de mensajes.
* `com.empresa.procesador`: Lógica de negocio (ETL) para archivar y transformar.
* `com.empresa.impresora`: Consumidores finales que escriben en disco.

### Compilación

Para generar el artefacto ejecutable:

```bash
mvn clean package

```

---

## 3. Guía para el Implantador (Puesta en Marcha)

Pasos para desplegar el sistema en un entorno local de desarrollo.

### Paso 1: Iniciar Kafka

Asegúrese de que **Zookeeper** y **Kafka Broker** están iniciados y operativos en el puerto 9092.

### Paso 2: Creación de Topics

Ejecute los siguientes comandos para crear la infraestructura de colas necesaria:

**En Linux / Mac:**

```bash
bin/kafka-topics.sh --create --topic cola-recepcion --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic cola-impresion-bn --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic cola-impresion-color --bootstrap-server localhost:9092

```

**En Windows:**

```cmd
bin\windows\kafka-topics.bat --create --topic cola-recepcion --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic cola-impresion-bn --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic cola-impresion-color --bootstrap-server localhost:9092

```

### Paso 3: Ejecución

Se recomienda iniciar los módulos en el siguiente orden (en terminales separadas):

1. **Impresoras (`ImpresorasApp`):** Para dejar los consumidores listos.
2. **Procesador (`ProcesadorApp`):** Para iniciar el archivado y enrutamiento.
3. **Productor (`EmisorApp`):** Para empezar a enviar carga de trabajo.

---

## 4. Guía para el Mantenedor

Instrucciones para la operación diaria, limpieza y reinicio del sistema.

### Ubicación de Archivos (Salida)

El sistema genera automáticamente la carpeta `storage/` en la raíz del proyecto:

* `storage/archivos_originales/`: Copia de seguridad legal (por Sender).
* `storage/impresiones_bn/`: Salida de impresión B/N.
* `storage/impresiones_color/`: Salida de impresión Color.

### Procedimiento de Reinicio y Limpieza (Wipe)

Si el sistema se bloquea o se requiere una ejecución limpia (sin mensajes antiguos):

1. **Detener:** Cierre todas las aplicaciones Java.
2. **Eliminar Topics:** Esto borrará todos los mensajes pendientes en Kafka.
* *Comando:* `kafka-topics.sh --delete --topic cola-recepcion,cola-impresion-bn,cola-impresion-color --bootstrap-server localhost:9092`


3. **Limpiar Disco:** Borre el contenido de la carpeta `storage/`.
4. **Reiniciar:** Vuelva a crear los topics siguiendo el **Paso 2** de la guía de implantación.

```

```
