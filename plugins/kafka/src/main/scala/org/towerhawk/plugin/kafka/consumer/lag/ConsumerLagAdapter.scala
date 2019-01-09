package org.towerhawk.plugin.kafka.consumer.lag

import kafka.admin.ConsumerGroupCommand._
import org.apache.kafka.common.{KafkaException, Node}
import org.towerhawk.monitor.check.execution.ExecutionResult

class ConsumerLagAdapter(brokers: String, groupId: String, timeoutMs: String, throwIfNotExists: Boolean) extends AutoCloseable {

  val opts = new ConsumerGroupCommandOptions(Array("--bootstrap-server", brokers, "--group", groupId, "--timeout", timeoutMs))

  val consumerGroupService = new KafkaConsumerGroupService(opts)

  private val MISSING_VALUE = "-"

  def getLags(): ExecutionResult = {
    val result = ExecutionResult.startTimer()

    val (state, assignments) = consumerGroupService.collectGroupOffsets()
    result.complete()
    result.addResult("groupId", groupId)

    def convertAssignments(): Unit = {
      //End up creating a map of group -> topic -> partition -> lag info

      val resultList = new java.util.ArrayList[KafkaConsumerLagDTO]
      assignments.get.foreach(a => {
        val lag = new KafkaConsumerLagDTO
        lag.setGroup(a.group)
        lag.setTopic(a.topic.getOrElse(MISSING_VALUE))
        lag.setPartition(String.valueOf(a.partition.getOrElse(-1)))
        lag.setLag(a.lag.getOrElse(-1))
        lag.setLogEndOffset(a.logEndOffset.getOrElse(-1))
        lag.setOffset(a.offset.getOrElse(-1))
        lag.setClientId(a.clientId.getOrElse(MISSING_VALUE))
        lag.setConsumerId(a.consumerId.getOrElse(MISSING_VALUE))
        lag.setHost(a.host.getOrElse(MISSING_VALUE))
        val node = a.coordinator.getOrElse(Node.noNode)
        lag.setCoordinatorId(node.id())
        lag.setCoordinatorHost(node.host())
        resultList.add(lag)
      })
      result.setResult(resultList)
    }

    assignments match {
      case None =>
        // applies to both old and new consumer
        result.addResult("state", "nonExistent")
        if (throwIfNotExists) {
          throw new IllegalStateException(s"Consumer group '$groupId' does not exist.")
        }
      case _ =>
        state match {
          case Some("Dead") =>
            result.addResult("state", "nonExistent")
            if (throwIfNotExists) {
              throw new IllegalStateException(s"Consumer group '$groupId' does not exist.")
            }
          case Some("Empty") =>
            result.addResult("state", "noneAlive")
            convertAssignments()
          case Some("PreparingRebalance") | Some("AwaitingSync") =>
            result.addResult("state", "rebalancing")
            convertAssignments()
          case Some("Stable") =>
            result.addResult("state", "stable")
            convertAssignments()
          case other =>
            // the control should never reach here
            result.addResult("state", "error")
            throw new KafkaException(s"Expected a valid consumer group state, but found '${other.getOrElse("NONE")}'.")
        }
    }
    result
  }

  override def close(): Unit = {
    consumerGroupService.close()
  }
}

