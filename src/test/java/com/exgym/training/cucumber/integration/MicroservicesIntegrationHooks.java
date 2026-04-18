package com.exgym.training.cucumber.integration;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;

public class MicroservicesIntegrationHooks {

    @Before(order = 0)
    public void ensureExternalServiceStarted() {
        ExternalWorkloadServiceManager.ensureStarted();
    }

    @AfterAll
    public static void stopExternalService() {
        ExternalWorkloadServiceManager.stop();
    }
}