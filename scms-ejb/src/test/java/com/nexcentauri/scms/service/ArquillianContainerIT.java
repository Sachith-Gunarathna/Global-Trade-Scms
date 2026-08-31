package com.nexcentauri.scms.service;

import jakarta.ejb.EJB;
import java.time.LocalDateTime;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
class ArquillianContainerIT {
    @EJB
    private ContainerProbeBean probeBean;

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class, "scms-arquillian-test.jar")
                .addClasses(ContainerProbeBean.class, RoutePriorityCalculator.class);
    }

    @Test
    void statelessEjbRunsInsidePayaraContainer() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 10, 0);
        assertEquals(90, probeBean.routePriority("DELAYED", now.plusHours(12), now));
    }
}
