@kafka
Feature: Kafka Producer and Consumer Test

	Scenario: Validate that Consumer received what Producer published
		Given kafka is running on "localhost:9092" and zookeeper is running on "localhost:2181"
		When kafka producer publishes "Kafka01" event to "cucumber-test-topic-gn" topic
		Then kafka consumer receives "Kafka01" event on "cucumber-test-topic-gn" topic

	Scenario Outline: validate scenario1 with multiple data points
		Given kafka is running on "localhost:9092" and zookeeper is running on "localhost:2181"
		When kafka producer publishes "<producer_event>" event to "<topic>" topic
		Then kafka consumer receives "<consumer_event>" event on "<topic>" topic
		Examples:
		| producer_event	| consumer_event	| topic |
		| Kafka02 			| Kafka02			| cucumber-test-topic-gn |
		| Kafka03 			| Kafka03			| cucumber-test-topic-gn |