package com.exgym.training.cucumber.integration;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/microservice-integration")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.exgym.training.cucumber.integration")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class MicroservicesIntegrationCucumberTest {
}