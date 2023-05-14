package ua.sinaver.web3.mq;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SigningTaskEvent(@JsonProperty("taskId") String taskId, @JsonProperty("batchSize") int batchSize) {
}
