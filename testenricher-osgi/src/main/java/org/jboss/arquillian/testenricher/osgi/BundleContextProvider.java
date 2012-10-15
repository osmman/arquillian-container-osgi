/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.testenricher.osgi;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

/**
 * {@link OperatesOnDeploymentAwareProvider} implementation to
 * provide {@link BundleContext} injection to {@link ArquillianResource}-
 * annotated fields.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 15-Oct-2012
 */
public class BundleContextProvider implements ResourceProvider {

    private AtomicBoolean initialized = new AtomicBoolean();

    @Inject
    @ApplicationScoped
    private InstanceProducer<BundleContext> bundleContextProducer;

    @Inject
    private Instance<BundleContext> bundleContext;

    @Override
    public boolean canProvide(final Class<?> type) {
        return type.isAssignableFrom(BundleContext.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        initialize();
        return bundleContext.get();
    }

    private void initialize() {
        if (initialized.compareAndSet(false, true)) {
            bundleContextProducer.set(getBundleContext());
        }
    }

    static BundleContext getBundleContext() {
        // [ARQ-459] Allow TestRunner to TestEnricher communication
        BundleContext bundleContext = BundleContextAssociation.getBundleContext();
        if (bundleContext == null) {
            ClassLoader classLoader = BundleContextProvider.class.getClassLoader();
            if (classLoader instanceof BundleReference) {
                BundleReference bref = (BundleReference) classLoader;
                bundleContext = bref.getBundle().getBundleContext();
                bundleContext = bundleContext.getBundle(0).getBundleContext();
            }
        }
        return bundleContext;
    }
}
