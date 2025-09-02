#  EnergyCommunity

**Distributed Systems Semester Project – FH Technikum Wien**  
Team: Ambar Irfan, Julia Brandstätter, Yuchang Zhang
GitHub_Link: https://github.com/wi23b165/EnergyCommunity.git

---

##  Overview
Dieses Projekt simuliert eine **Energy Community**, in der Energieproduktion (Solar) und -verbrauch (User/Consumer) in einem verteilten System erfasst, verarbeitet, gespeichert und visualisiert werden.  

- **Kommunikation:** RabbitMQ  
- **Persistenz:** PostgreSQL (via Docker, Flyway Migrationen)  
- **Abfragen:** REST-API (Spring Boot)  
- **Visualisierung:** JavaFX GUI  

---

##  Architektur

### Services
- **Producer-Solar** → simuliert Solarproduktion  
- **Consumer-Meter** → simuliert Stromverbrauch  
- **Usage-Worker** → aggregiert Werte & speichert in PostgreSQL  
- **Percentage-Service** → berechnet Anteil Community vs. Grid  

### Schnittstellen
- **REST-API** → `/energy/current` und `/energy/historical`  
- **JavaFX GUI** → Dashboard für aktuelle & historische Daten  

### Infrastruktur
- **PostgreSQL (5432)** – Datenbank  
- **RabbitMQ (5672 / 15672)** – Message Broker  

---

##  Startreihenfolge

1. **Docker-Services starten**
   ```bash
   cd docker
   docker compose up -d
   ```
   - PostgreSQL → Port 5432  
   - RabbitMQ → Ports 5672 / 15672  

2. **Usage-Worker starten**
   ```bash
   cd usage-worker
   mvn spring-boot:run
   ```

3. **Percentage-Service starten**
   ```bash
   cd percentage-service
   mvn spring-boot:run
   ```

4. **Producer-Solar starten**
   ```bash
   cd producer-solar
   mvn spring-boot:run
   ```

5. **Consumer-Meter starten**
   ```bash
   cd consumer-meter
   mvn spring-boot:run
   ```

6. **REST-API starten**
   ```bash
   cd rest-api
   mvn spring-boot:run
   ```
   Läuft auf → [http://localhost:8081](http://localhost:8081)

7. **JavaFX GUI starten**
   ```bash
   cd gui
   mvn javafx:run
   ```

---

## 🔌 Ports

| Komponente    | Port  | Beschreibung            |
|---------------|-------|-------------------------|
| REST-API      | 8081  | API Endpunkte           |
| JavaFX GUI    | –     | Desktop-Anwendung       |
| PostgreSQL    | 5432  | Datenbank               |
| RabbitMQ      | 5672  | AMQP (Broker)           |
| RabbitMQ      | 15672 | Web UI (guest/guest)    |

---

##  Datenbank

### Tabellen
- **usage_hourly** – Aggregation pro Stunde (Produktion, Verbrauch, Grid)  
- **current_percentage** – Community vs. Grid Anteil  

### Migrationen (Flyway)
- V1__energy_reading.sql  
- V2__usage_hourly.sql  
- V3__current_percentage.sql
- V4__indexes.sql
- V1__init_usage_worker.sql


---

## REST-API Endpunkte

### 1) Aktueller Stand
```http
GET /energy/current
```
Response:
```json
{
  "hour": "2025-09-02T14:00Z",
  "usedKwh": 3.8,
  "gridUsedKwh": 1.4,
  "communityPct": 63.2
}
```

### 2) Historische Daten
```http
GET /energy/historical?start=2025-09-01&end=2025-09-02
```
Response:
```json
[
  { "hour": "2025-09-01T10:00Z", "communityProduced": 5.0, "communityUsed": 4.0, "gridUsed": 1.0 },
  { "hour": "2025-09-01T11:00Z", "communityProduced": 6.0, "communityUsed": 3.5, "gridUsed": 0.5 }
]
```

---

##  GUI (JavaFX)
- Zeigt **aktuellen Anteil** (Community vs. Grid)  
- Abfrage **historischer Daten** (Datumsauswahl)  
- **Auto-Refresh alle 10s**  

---

