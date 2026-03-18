# Big Data Architecture: Scaling the Fraud Detection Pipeline

## Current Architecture (Batch Processing)

```
┌─────────────────────────────────────────────────────────────────────┐
│                     CURRENT: SPRING BATCH PIPELINE                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────┐    ┌──────────────────┐    ┌─────────────────┐       │
│  │  CSV     │───▶│  FlatFileReader   │───▶│ ItemProcessor   │       │
│  │  File    │    │  (Chunk: 1000)    │    │ (Feature Eng.)  │       │
│  │ 284K rows│    └──────────────────┘    └────────┬────────┘       │
│  └──────────┘                                      │                │
│                                                     ▼                │
│                              ┌──────────────────────────────┐       │
│                              │    JpaItemWriter → MySQL      │       │
│                              │    (Batch Insert, Size: 50)   │       │
│                              └──────────────────────────────┘       │
│                                                                     │
│  Characteristics:                                                   │
│  • Throughput: ~10,000 rows/sec                                     │
│  • Memory: O(chunk_size) - constant memory usage                    │
│  • Fault Tolerance: Skip up to 100 malformed records                │
│  • Latency: Minutes (suitable for historical analysis)              │
└─────────────────────────────────────────────────────────────────────┘
```

## Proposed Real-Time Architecture (Apache Kafka + Spark)

For a production deployment processing millions of transactions per second, the batch pipeline would be replaced with a streaming architecture:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     PROPOSED: REAL-TIME STREAMING PIPELINE                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌────────────────┐       ┌─────────────────────────────────┐              │
│  │  Payment        │──────▶│   Apache Kafka                   │              │
│  │  Gateway        │       │   Topic: raw-transactions        │              │
│  │  (POS/Online)   │       │   Partitions: 12                 │              │
│  └────────────────┘       │   Replication Factor: 3           │              │
│                            └──────────┬──────────────────────┘              │
│                                       │                                      │
│             ┌─────────────────────────┼──────────────────────┐              │
│             ▼                         ▼                      ▼              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐         │
│  │ Spark Streaming   │  │ Spark Streaming   │  │ Spark Streaming   │         │
│  │ Consumer Group 1  │  │ Consumer Group 2  │  │ Consumer Group 3  │         │
│  │ (Feature Eng.)    │  │ (Feature Eng.)    │  │ (Feature Eng.)    │         │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘         │
│           │                      │                      │                    │
│           ▼                      ▼                      ▼                    │
│  ┌───────────────────────────────────────────────────────────────┐          │
│  │              ML Scoring Layer (Distributed)                    │          │
│  │   ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐    │          │
│  │   │Random Forest │  │ Gradient     │  │ Neural Network    │    │          │
│  │   │(Primary)     │  │ Boosting     │  │ (Deep Learning)   │    │          │
│  │   └─────────────┘  └──────────────┘  └──────────────────┘    │          │
│  └────────────────────────────┬──────────────────────────────────┘          │
│                               │                                              │
│              ┌────────────────┼────────────────┐                            │
│              ▼                ▼                 ▼                            │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐              │
│  │ Kafka Topic:      │  │  MySQL /     │  │ Elasticsearch    │              │
│  │ fraud-alerts      │  │  PostgreSQL  │  │ (Real-time       │              │
│  │ (Notifications)   │  │  (Storage)   │  │  Dashboards)     │              │
│  └──────────────────┘  └──────────────┘  └──────────────────┘              │
│                                                                             │
│  Characteristics:                                                           │
│  • Throughput: 100,000+ transactions/sec                                    │
│  • Latency: < 100ms per transaction                                         │
│  • Scalability: Horizontal (add Kafka partitions + Spark executors)          │
│  • Fault Tolerance: Kafka replication, Spark checkpointing                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Scaling Strategy

### Phase 1: Current (Spring Batch)
- **Suitable for**: Historical analysis, model training, dataset ingestion
- **Scale**: Up to ~1M transactions per batch run
- **Infrastructure**: Single application server + MySQL

### Phase 2: Kafka Integration
- **Change**: Replace CSV reader with Kafka consumer
- **Benefit**: Decouple data ingestion from processing
- **Key Technology**: Spring Kafka + KafkaItemReader for Spring Batch
- **Scale**: ~10,000 tx/sec with a single consumer group

```java
// Example: KafkaItemReader integration
@Bean
public KafkaItemReader<String, TransactionEvent> kafkaReader() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "fraud-detection-group");
    
    return new KafkaItemReaderBuilder<String, TransactionEvent>()
            .name("kafkaTransactionReader")
            .consumerProperties(props)
            .topic("raw-transactions")
            .partitions(0, 1, 2, 3)
            .build();
}
```

### Phase 3: Spark Structured Streaming
- **Change**: Replace Spring Batch with Spark for distributed processing
- **Benefit**: Horizontal scaling across a cluster
- **Key Technology**: Spark MLlib for distributed ML inference

```python
# Example: Spark Structured Streaming
from pyspark.sql import SparkSession
from pyspark.ml import PipelineModel

spark = SparkSession.builder.appName("FraudDetection").getOrCreate()
model = PipelineModel.load("hdfs:///models/random-forest")

# Read from Kafka
transactions = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "kafka:9092") \
    .option("subscribe", "raw-transactions") \
    .load()

# Feature engineering + ML scoring
scored = model.transform(
    transactions.selectExpr("CAST(value AS STRING)")
)

# Write alerts to Kafka
scored.filter("prediction = 1.0") \
    .writeStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "kafka:9092") \
    .option("topic", "fraud-alerts") \
    .start()
```

### Phase 4: Full Production Architecture
- **ML Model Registry**: MLflow for model versioning and A/B testing
- **Feature Store**: Apache Feast for real-time feature serving
- **Monitoring**: Prometheus + Grafana for pipeline health
- **Data Lake**: Delta Lake / Apache Iceberg for historical data

## Technology Comparison

| Aspect | Spring Batch (Current) | Kafka + Spark (Proposed) |
|--------|----------------------|--------------------------|
| Throughput | ~10K rows/sec | 100K+ tx/sec |
| Latency | Minutes | < 100ms |
| Scalability | Vertical | Horizontal |
| Fault Tolerance | Skip & retry | Checkpointing + replication |
| Complexity | Low | High |
| Cost | $ | $$$ |
| Use Case | Historical analysis | Real-time detection |

## Conclusion

The current Spring Batch implementation provides a solid foundation for processing the 284K-row dataset and demonstrates the core pipeline pattern. The architecture is designed to be evolutionarily replaceable — the Strategy Pattern for ML models and the modular processor design allow each component to be individually upgraded to streaming equivalents without a complete rewrite.
