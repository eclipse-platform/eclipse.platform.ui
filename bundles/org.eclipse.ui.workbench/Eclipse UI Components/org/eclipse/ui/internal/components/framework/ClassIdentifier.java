/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.components.framework;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;


/**
 * Identifies a class, given a namespace and a fully-qualified class name.
 * This can be used to refer to a class without actually loading its plugin.
 * Note that it is possible to have two different ClassIdentifiers that point
 * to the same Class since the same class can exist in multiple namespaces.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class ClassIdentifier {

    private String namespace;
    private String className;
    
    /**
     * Creates an identifier for the given class in the given namespace.
     * 
     * @param namespace namespace in which to resolve the class
     * @param className fully-qualified class name
     */
    public ClassIdentifier(String namespace, String className) {
        this.namespace = namespace;
        this.className = className;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentType#getInterfaceId()
     */
    public String getTypeName() {
        return className;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentType#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Loads and returns the class. Use with caution: may cause plugin activation.
     *
     * @return the Class being referred to
     * @throws ClassNotFoundException if the given class cannot be found in the
     * given bundle
     */
    public Class loadClass() throws ClassNotFoundException {
        Bundle pluginBundle = Platform.getBundle(namespace);
        Class result = pluginBundle.loadClass(className);
        return result;
    }
    
}
