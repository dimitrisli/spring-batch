package org.springframework.batch.item.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.kafka.core.ConsumerFactory;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mathieu Ouellet
 */
public class KafkaItemReaderTests {

	private static final TopicPartition TOPIC_PARTITION = new TopicPartition("topic", 0);

	@Mock
	private ConsumerFactory<String, String> consumerFactory;

	@Mock
	private Consumer<String, String> consumer;

	@Mock
	private OffsetsProvider offsetsProvider;

	private KafkaItemReader<String, String> reader;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Map<String, Object> config = new HashMap<>();
		config.put("max.poll.records", 2);
		config.put("enable.auto.commit", false);
		when(consumerFactory.getConfigurationProperties()).thenReturn(config);
		when(consumerFactory.createConsumer()).thenReturn(consumer);
		reader = new KafkaItemReader<>();
		reader.setConsumerFactory(consumerFactory);
		reader.setOffsetsProvider(offsetsProvider);
		reader.setTopicPartitions(singletonList(TOPIC_PARTITION));
		reader.setSaveState(true);
		reader.setPollTimeout(50L);
		reader.afterPropertiesSet();
	}

	@Test
	public void testAfterPropertiesSet() throws Exception {
		reader = new KafkaItemReader<>();

		try {
			reader.afterPropertiesSet();
			fail("Expected exception was not thrown");
		}
		catch (IllegalStateException ignore) {
		}

		reader.setTopicPartitions(singletonList(new TopicPartition("topic", 0)));
		reader.setTopics(singletonList("topic"));
		try {
			reader.afterPropertiesSet();
			fail("Expected exception was not thrown");
		}
		catch (IllegalStateException ignore) {
		}

		reader.setTopics(null);
		try {
			reader.afterPropertiesSet();
			fail("Expected exception was not thrown");
		}
		catch (IllegalArgumentException ignore) {
		}

		reader.setConsumerFactory(consumerFactory);
		try {
			reader.afterPropertiesSet();
			fail("Expected exception was not thrown");
		}
		catch (IllegalArgumentException ignore) {
		}

		reader.setOffsetsProvider(offsetsProvider);
		reader.afterPropertiesSet();
	}

	@Test
	public void testAssignTopicPartitions() {
		reader.open(new ExecutionContext());
		verify(consumer).assign(singletonList(TOPIC_PARTITION));
	}

	@Test
	public void testRead() throws Exception {
		Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new HashMap<>();
		records.put(TOPIC_PARTITION, singletonList(
				new ConsumerRecord<>(TOPIC_PARTITION.topic(), TOPIC_PARTITION.partition(), 0L, "key0", "val0")));
		when(consumer.poll(any())).thenReturn(new ConsumerRecords<>(records));

		reader.open(new ExecutionContext());
		String read = reader.read();
		assertThat(read, is("val0"));
	}

	@Test
	public void testPollRecords() throws Exception {
		Map<TopicPartition, List<ConsumerRecord<String, String>>> firstPoll = new HashMap<>();
		firstPoll.put(TOPIC_PARTITION, asList(new ConsumerRecord<>("topic", 0, 0L, "key0", "val0"),
				new ConsumerRecord<>("topic", 0, 1L, "key1", "val1")));
		when(consumer.poll(Duration.ofMillis(2000L))).thenReturn(new ConsumerRecords<>(firstPoll));

		Map<TopicPartition, List<ConsumerRecord<String, String>>> secondPoll = new HashMap<>();
		secondPoll.put(TOPIC_PARTITION, singletonList(new ConsumerRecord<>("topic", 0, 2L, "key2", "val2")));
		when(consumer.poll(Duration.ofMillis(50L))).thenReturn(new ConsumerRecords<>(secondPoll));

		reader.open(new ExecutionContext());

		String read = reader.read();
		assertThat(read, is("val0"));

		read = reader.read();
		assertThat(read, is("val1"));

		read = reader.read();
		assertThat(read, is("val2"));
	}

	@Test
	public void testSeekOnSavedState() {
		long offset = 100L;
		Map<TopicPartition, Long> offsets = new HashMap<>();
		offsets.put(TOPIC_PARTITION, offset);
		ExecutionContext executionContext = new ExecutionContext();
		executionContext.put("topic.partition.offset", offsets);
		reader.open(executionContext);
		verify(consumer).seek(TOPIC_PARTITION, offset);
	}

	@Test
	public void testSeekToProvidedOffsets() {
		long offset = 100L;
		Map<TopicPartition, Long> offsets = new HashMap<>();
		offsets.put(TOPIC_PARTITION, offset);
		given(offsetsProvider.get(singletonList(TOPIC_PARTITION))).willReturn(offsets);
		reader.open(new ExecutionContext());
		verify(consumer).seek(TOPIC_PARTITION, offset);
	}

}