@mqtt
Feature: MQTT Producer and Consumer Test

	Scenario: Validate that Consumer received what Producer published
		Given mqtt is running on "localhost:1883"
		When mqtt producer publishes "GN" "string event" to "cucumber-test-topic" topic
		Then mqtt consumer receives "GN" "string event" on "cucumber-test-topic" topic

	Scenario Outline: validate scenario1 with multiple data points
		Given mqtt is running on "localhost:1883"
		When mqtt producer publishes "<producer_event>" "string event" to "<topic>" topic
		Then mqtt consumer receives "<consumer_event>" "string event" on "<topic>" topic
		Examples:
			| producer_event	| consumer_event	| topic |
			| test_event_01 	| test_event_01		| cucumber-test-topic |
			| test_event_02 	| test_event_02		| cucumber-test-topic |
			| test_event_03 	| test_event_03		| cucumber-test-topic |

	Scenario: Sample AddressBook message test
		Given mqtt is running on "localhost:1883"
		And "sample" message is used
		And serialize and compress the sample message
		When mqtt producer publishes "AddressBook" "protobuf event" to "cucumber-test-topic" topic
		Then mqtt consumer receives "AddressBook" "protobuf event" on "cucumber-test-topic" topic

	Scenario: Build AddressBook message using Auto-Generated Java classes
		Given mqtt is running on "localhost:1883"
		And "newly built" message is used
		And serialize and compress the sample message
		When mqtt producer publishes "AggregatedStats" "protobuf event" to "cucumber-test-topic" topic
		Then mqtt consumer receives "AggregatedStats" "protobuf event" on "cucumber-test-topic" topic