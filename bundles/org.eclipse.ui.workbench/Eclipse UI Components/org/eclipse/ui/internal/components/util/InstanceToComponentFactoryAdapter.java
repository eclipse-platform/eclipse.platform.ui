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
package org.eclipse.ui.internal.components.util;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentFactory;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.NonDisposingHandle;

/**
 * Factory that always returns the same instance
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public class InstanceToComponentFactoryAdapter extends ComponentFactory {

	private ComponentHandle handle;
	
    /**
     * Creates a factory that always returns handles to the given object instance
     * 
     * @param existingInstance instance to adapt
     */
	public InstanceToComponentFactoryAdapter(Object existingInstance) {
		handle = new NonDisposingHandle(existingInstance);
	}
	
	public ComponentHandle createHandle(IServiceProvider availableServices)
			throws ComponentException {
		return handle;
	}
}
