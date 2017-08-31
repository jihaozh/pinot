package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.FrameworkNode;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.dag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.dag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.processor.Processor;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorConfig;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessorRunner extends FrameworkNode {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessorRunner.class);

  private FrameworkNode logicalNode;
  protected Map<NodeIdentifier, ExecutionResultsReader> incomingResultsReaderMap = new HashMap<>();
  protected ExecutionStatus executionStatus = ExecutionStatus.RUNNING;

  public AbstractProcessorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass,
      FrameworkNode logicalNode) {
    super(nodeIdentifier, nodeConfig, operatorClass);
    this.logicalNode = logicalNode;
  }

  public void addIncomingExecutionResultReader(NodeIdentifier nodeIdentifier,
      ExecutionResultsReader executionResultsReader) {
    incomingResultsReaderMap.put(nodeIdentifier, executionResultsReader);
  }

  public Map<NodeIdentifier, ExecutionResultsReader> getIncomingResultsReaderMap() {
    return incomingResultsReaderMap;
  }

  @Override
  public FrameworkNode getLogicalNode() {
    return logicalNode;
  }

  @Override
  public Collection<FrameworkNode> getPhysicalNode() {
    return Collections.emptyList();
  }

  @Override
  public ExecutionStatus getExecutionStatus() {
    return executionStatus;
  }

  protected void setFailure(Exception e) {
    LOG.error("Failed to execute node: {}.", nodeIdentifier, e);
    if (nodeConfig.skipAtFailure()) {
      executionStatus = ExecutionStatus.SKIPPED;
    } else {
      executionStatus = ExecutionStatus.FAILED;
    }
  }

  // TODO: Implement this method
  static ProcessorConfig convertNodeConfigToOperatorConfig(NodeConfig nodeConfig) {
    return null;
  }

  static Processor initializeOperator(Class operatorClass, ProcessorConfig processorConfig)
      throws IllegalAccessException, InstantiationException {
    try {
      Processor processor = (Processor) operatorClass.newInstance();
      processor.initialize(processorConfig);
      return processor;
    } catch (Exception e) {
      // We cannot do anything if something bad happens here excepting rethrow the exception.
      LOG.warn("Failed to initialize {}", operatorClass.getName());
      throw e;
    }
  }
}