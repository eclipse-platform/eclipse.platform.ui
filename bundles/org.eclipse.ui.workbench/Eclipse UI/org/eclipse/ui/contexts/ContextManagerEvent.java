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
 * 
 * @since 3.0
 * @see IContextManagerListener#contextManagerChanged
 */
public final class ContextManagerEvent {
	private IContextManager contextManager;
	private String[] contextsEnabled;
	private String[] contextsDisabled;
	private String[] contextsDefined;
	
	private static final String[] EMPTY_ARRAY = new String[0];
	
	/**
     * Creates a new instance of this class
     * 
     * @param contextManager
     *            the context manager in which the change occurred
     * @param enabledContexts
     *            the context identifiers that have been enabled
     * @param disabledContexts
     *            the context identifiers that have been disabled
     * @param definedContexts
     *            the context identifiers that have been defined
     */
    public ContextManagerEvent(IContextManager contextManager,
            String[] enabledContexts, String[] disabledContexts,
            String[] definedContexts) {
        if (contextManager == null) { throw new NullPointerException(); }
        this.contextManager = contextManager;
        this.contextsEnabled = enabledContexts;
        this.contextsDisabled = disabledContexts;
        this.contextsDefined = definedContexts;
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
	 * Returns the collection of contexts which have been enabled.
	 * 
	 * @return the collection of contexts which have been enabled
	 */
	public String[] getEnabledContexts() {
		if (contextsEnabled != null) {
			return contextsEnabled;
		}
		return EMPTY_ARRAY;
	}
	
	/**
	 * Returns the collection of contexts which have been disabled.
	 * 
	 * @return the collection of contexts which have been disabled
	 */
	public String[] getDisabledContexts() {
		if (contextsDisabled != null) {
			return contextsDisabled;
		}
		return EMPTY_ARRAY;
	}
	
	/**
	 * Returns the collection of contexts which have been defined.
	 * 
	 * @return the collection of contexts which have been defined
	 */
	public String[] getDefinedContexts() {
		if (contextsDefined != null) {
			return contextsDefined;
		}
		return EMPTY_ARRAY;
	}

	/**
	 * Returns whether or not the definedContextIds property changed.
	 * 
	 * @return true, iff the definedContextIds property changed.
	 * @deprecated Please use the three accessors for defined, disabled and
	 * enabled contexts.
	 */
	public boolean haveDefinedContextIdsChanged() {
		return getDefinedContexts().length > 0;
	}

	/**
	 * Returns whether or not the enabledContextIds property changed.
	 * 
	 * @return true, iff the enabledContextIds property changed.
	 * @deprecated Please use the three accessors for defined, disabled and
	 * enabled contexts.
	 */
	public boolean haveEnabledContextIdsChanged() {
		return getEnabledContexts().length > 0;
	}
}
