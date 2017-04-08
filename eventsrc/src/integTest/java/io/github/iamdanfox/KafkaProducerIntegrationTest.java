/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.palantir.docker.compose.DockerComposeRule;
import org.junit.ClassRule;
import org.junit.Test;

public class KafkaProducerIntegrationTest {

    @ClassRule
    public static final DockerComposeRule docker = DockerComposeRule.builder()
            .file("../docker-compose.yml")
            .build();

    @Test
    public void smoke_test() {}
}
