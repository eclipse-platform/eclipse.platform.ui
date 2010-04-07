/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.services.adapter;

/**
 * An adapter can adapt an object to the specified type, allowing clients to request domain-specific
 * behavior for an object.
 */
public abstract class Adapter {

	public abstract <T> T adapt(Object element, Class<T> adapterType);

}
