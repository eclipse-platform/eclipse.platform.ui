/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.contexts;

/**
 * An instance of this class describes changes to an instance of <code>IContextManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IContextManagerListener#contextManagerChanged
 */
public final class ContextManagerEvent {
	private IContextManager contextManager;
	private boolean definedContextIdsChanged;
	private boolean enabledContextIdsChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param contextManager
	 *            the instance of the interface that changed.
	 * @param definedContextIdsChanged
	 *            true, iff the definedContextIds property changed.
	 * @param enabledContextIdsChanged
	 *            true, iff the enabledContextIdsChanged property changed.
	 */
	public ContextManagerEvent(
		IContextManager contextManager,
		boolean definedContextIdsChanged,
		boolean enabledContextIdsChanged) {
		if (contextManager == null)
			throw new NullPointerException();

		this.contextManager = contextManager;
		this.definedContextIdsChanged = definedContextIdsChanged;
		this.enabledContextIdsChanged = enabledContextIdsChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public IContextManager getContextManager() {
		return contextManager;
	}

	/**
	 * Returns whether or not the definedContextIds property changed.
	 * 
	 * @return true, iff the definedContextIds property changed.
	 */
	public boolean haveDefinedContextIdsChanged() {
		return definedContextIdsChanged;
	}

	/**
	 * Returns whether or not the enabledContextIdsChanged property changed.
	 * 
	 * @return true, iff the enabledContextIdsChanged property changed.
	 */
	public boolean haveEnabledContextIdsChanged() {
		return enabledContextIdsChanged;
	}
}
