/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.jmx;

import org.jboss.as.Extension;
import org.jboss.as.ExtensionContext;
import org.jboss.as.SubsystemFactory;
import org.jboss.msc.service.ServiceActivatorContext;

/**
 * JMX Extension
 *
 * @author John Bailey
 */
public class JmxExtension implements Extension {

    final SubsystemFactory<JmxSubsystemElement> FACTORY = new SubsystemFactory<JmxSubsystemElement>() {
        public JmxSubsystemElement createSubsystemElement() {
            return new JmxSubsystemElement();
        }
    };

    /** {@inheritDoc} */
    public void activate(final ServiceActivatorContext context) {
    }

    /** {@inheritDoc} */
    public void initialize(ExtensionContext context) {
        context.registerSubsystem(Namespace.JMX_1_0.getUriString(), new JmxSubsystemElementParser());
    }
}