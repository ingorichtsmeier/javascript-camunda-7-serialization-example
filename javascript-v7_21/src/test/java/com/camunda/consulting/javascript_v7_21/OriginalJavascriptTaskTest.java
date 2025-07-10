package com.camunda.consulting.javascript_v7_21;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.findId;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.spring_test.platform7.ProcessEngineCoverageConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@SpringBootTest
@Import(ProcessEngineCoverageConfiguration.class)
public class OriginalJavascriptTaskTest {
	
  private static final Logger LOG = LoggerFactory.getLogger(OriginalJavascriptTaskTest.class);

  @Autowired
  private ProcessEngine processEngine;

  @BeforeEach
  public void setup() {
    init(processEngine);
  }

  @Test
  @Deployment(resources = "InvestigationsCheckReportStatusService.bpmn") // only required for process test coverage
  public void testHappyPath() {
    // Drive the process by API and assert correct behavior by camunda-bpm-assert

    ProcessInstance processInstance = processEngine().getRuntimeService()
        .startProcessInstanceByKey("InvestigationsCheckReportStatusService", Map.of("ReferenceNumber", "12345",
        		"url", "http://localhost:7000/wiremock"));
    
    assertThat(processInstance).isWaitingAt(findId("Test completed"));
    
    VariableInstance catchedError = processEngine.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(processInstance.getId()).variableName("CatchedError").singleResult();
    LOG.info("CatchedError details: {}", catchedError.getTypedValue());
//    assertThat(xmlExample.getTypeName()).isEqualTo("application/json");
    
    execute(job());

    assertThat(processInstance).isEnded()
    .variables()
    .containsEntry("CatchedError", "some wrong value to see the difference")
//    .containsKey("CatchedError")
    ;

  }

}
