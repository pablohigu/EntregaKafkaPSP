# Sistema de Gestión de Colas de Impresión Distribuidas (Kafka)

Este proyecto implementa un sistema de impresión empresarial robusto utilizando **Java** y **Apache Kafka**. Simula un entorno distribuido donde los documentos son recibidos, archivados, procesados (paginados) y enviados a colas de impresión específicas (Blanco/Negro o Color) para ser consumidos por múltiples impresoras en paralelo.

## 📋 Descripción del Sistema

El sistema cumple con los siguientes requerimientos de arquitectura:

1.  **Entrada de Datos:** Recepción de documentos JSON con metadatos (Título, Contenido, Tipo, Sender).
2.  **Paralelismo:** Un procesador central realiza dos tareas simultáneas mediante hilos:
    * **Archivado:** Guarda una copia inmutable del documento original (RAW) en disco.
    * **Transformación:** Divide el texto largo en páginas (configurado a 400 caracteres por defecto) y enruta el trabajo.
3.  **Enrutamiento Inteligente:** Distribuye las páginas a topics diferenciados (`cola-impresion-bn` o `cola-impresion-color`).
4.  **Escalabilidad:** Simula hardware físico con **consumer groups**:
    * 3 Impresoras B/N (Consumiendo de 3 particiones).
    * 2 Impresoras Color (Consumiendo de 2 particiones).
5.  **Configuración Externa:** Sin "números mágicos", todo configurable desde `app.conf`.

## 🛠️ Tecnologías

* **Java 17**
* **Apache Kafka** (Modo KRaft)
* **Maven** (Gestión de dependencias)
* **Jackson** (Procesamiento JSON)

## ⚙️ Configuración (app.conf)

El comportamiento del sistema se controla desde `src/main/resources/app.conf`. Aquí puedes modificar:

* Dirección del servidor Kafka.
* Nombres de los topics y grupos de consumo.
* Tamaño de paginación (caracteres por página).
* Tiempos de espera simulados (velocidad de impresión y envío).

## 🚀 Guía de Instalación y Despliegue

### 1. Prerrequisitos
Tener instalado y configurado Apache Kafka en el sistema (ej. en `C:\kafka`).

### 2. Arrancar Infraestructura Kafka
1.  Iniciar el servidor Kafka:
    ```powershell
    .\kafka-server-start.bat ..\..\config\server.properties
    ```

2.  Crear los Topics necesarios (con particiones para el paralelismo):
    ```powershell
    # Cola de entrada única
    .\kafka-topics.bat --create --topic cola-recepcion --bootstrap-server localhost:9092

    # Cola B/N (3 particiones para 3 impresoras)
    .\kafka-topics.bat --create --topic cola-impresion-bn --bootstrap-server localhost:9092 --partitions 3

    # Cola Color (2 particiones para 2 impresoras)
    .\kafka-topics.bat --create --topic cola-impresion-color --bootstrap-server localhost:9092 --partitions 2
    ```

### 3. Ejecución del Software

El sistema consta de 3 módulos independientes que deben ejecutarse en paralelo (en terminales distintas o consolas de Eclipse).

**Orden recomendado de ejecución:**

1.  **Sistema de Impresión (Hardware):**
    Arranca los 5 hilos de impresión que quedarán a la espera de trabajo.
    * Clase: `impresion.consumidor.Impresoras`

2.  **Procesador Central (Servidor):**
    Arranca el Archivador y el Transformador/Router.
    * Clase: `impresion.consumidor.Procesador`

3.  **Emisor (Empleado):**
    Empieza a generar y enviar documentos aleatorios continuamente.
    * Clase: `impresion.productor.Emisor`

## 📂 Resultados

El sistema generará automáticamente en la raíz del proyecto:
* Una carpeta `/archivos_originales` con los JSON recibidos organizados por nombre del remitente.
* Logs en consola detallando el flujo de impresión y paginación.

## 👤 Autor
Miguel Goyena - Desarrollo de Interfaces y Sistemas Distribuidos.
