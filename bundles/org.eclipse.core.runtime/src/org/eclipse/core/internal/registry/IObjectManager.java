/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

/**
 * @since 3.1 
 */
public interface IObjectManager {
	Handle getHandle(int id, byte type);

	Handle[] getHandles(int[] ids, byte type);

	Object getObject(int id, byte type);

	RegistryObject[] getObjects(int[] values, byte type);
	
	void close();
}
