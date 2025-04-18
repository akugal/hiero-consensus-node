// SPDX-License-Identifier: Apache-2.0
package org.hiero.otter.test;

import java.time.Duration;
import org.hiero.otter.fixtures.Network;
import org.hiero.otter.fixtures.OtterTest;
import org.hiero.otter.fixtures.TestEnvironment;
import org.hiero.otter.fixtures.TimeManager;
import org.hiero.otter.fixtures.Validator.Profile;
import org.junit.jupiter.api.Disabled;

public class HappyPathTest {

    @OtterTest
    @Disabled
    void testHappyPath(TestEnvironment env) throws InterruptedException {
        final Network network = env.network();
        final TimeManager timeManager = env.timeManager();

        // Setup simulation
        network.addNodes(4);
        network.start(Duration.ofMinutes(1L));
        env.generator().start();

        // Wait for two minutes
        timeManager.waitFor(Duration.ofMinutes(2L));

        // Validations
        env.validator().validateRemaining(Profile.DEFAULT);
    }
}
