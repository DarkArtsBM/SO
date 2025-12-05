package com.so.cloudjrb.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features") // Pasta onde estão os arquivos .feature
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.so.cloudjrb.bdd") // Pacote das classes Steps
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty") // Saída bonita no console
public class CucumberTestRunner {
}