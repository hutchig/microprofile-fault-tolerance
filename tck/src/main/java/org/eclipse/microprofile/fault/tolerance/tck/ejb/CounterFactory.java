/*
 *******************************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.eclipse.microprofile.fault.tolerance.tck.ejb;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import javax.ws.rs.Produces;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Creates counters that can be injected.
 * <p>
 * Usage:
 * {@code @Inject @CounterId("foo") AtomicInteger fooCounter;}
 * <p>
 * Injection points which specify the same {@code @CounterId} will receive the same counter instance.
 */
@ApplicationScoped
public class CounterFactory {


    private final Map<String, AtomicInteger> counters = new HashMap<>();


    @Qualifier
    @Retention(RUNTIME)
    @Target({ METHOD, FIELD, PARAMETER })
    public @interface CounterId {
        @Nonbinding
        String value();
    }

    @Produces
    @Dependent
    @CounterId(value = "")
    private synchronized AtomicInteger produce(InjectionPoint injectionPoint) {
        String id = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType() == CounterId.class) {
                id = ((CounterId) qualifier).value();
            }
        }

        if (id == null) {
            throw new IllegalStateException("No counter id for injection point: " + injectionPoint.getMember());
        }

        AtomicInteger counter = counters.get(id);
        if (counter == null) {
            counter = new AtomicInteger();
            counters.put(id, counter);
        }

        return counter;
    }

}
