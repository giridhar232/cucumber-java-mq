package com.gn.tests;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;


@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber", "com.gn.utils.CucumberHooks"},
        features = "src/test/java/com/gn/tests",
        glue = {"com.gn.tests"})
public class RunTest {
}
