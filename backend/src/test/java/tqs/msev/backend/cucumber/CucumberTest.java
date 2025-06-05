package tqs.msev.backend.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("tqs/msev/backend")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "tqs.msev.backend")
public class CucumberTest {
}
