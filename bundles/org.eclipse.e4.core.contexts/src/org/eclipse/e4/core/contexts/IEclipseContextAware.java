/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.contexts;

/**
 * Objects used in the context injection might choose to implement this interface to perform
 * post-processing of injected values, perform custom processing of the context, and be notified if
 * the context was disposed.
 * <p>
 * If objects don't want to introduce dependency on the context injection mechanism, they can add
 * methods from this interface directly to their class implementations. As long as method signatures
 * match exactly, they will be called (even if the object does not implement this interface).
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IEclipseContextAware {

	/**
	 * The last method called during the context injection process.
	 * 
	 * @param context
	 *            the injected context
	 */
	public void contextSet(IEclipseContext context);

}