/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.provisional;

import org.eclipse.ui.IWorkbenchPage;

/**
 * An adapter that is capable of displaying source for an object.
 * The adapter is obtained from an object in a debug context.
 * 
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface ISourceDisplayAdapter {
	
	/**
	 * Displays source for the given context in the specified page.
	 * 
	 * @param context debug context to display source for
	 * @param page the page in which to display source
	 * @param forceSourceLookup whether source lookup should be performed
	 */
	public void displaySource(Object context, IWorkbenchPage page, boolean forceSourceLookup);
	
}
