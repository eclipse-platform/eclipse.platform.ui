/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core;

/**
 * A decorator enablement listener is notified of changes to the enablement
 * of CVS state decorators.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see CVSProviderPlugin#addDecoratorEnablementListener(ICVSDecoratorEnablementListener)
 */
public interface ICVSDecoratorEnablementListener {
	/**
	 * Called when CVS decoration is enabled or disabled. Implementers can use the
	 * decorator enablement change as a chance to create or destroy cached CVS information
	 * that would help decorate CVS elements. 
	 * 
	 * @param enabled a flag indicating the enablement state of the decorators.
	 */
	void decoratorEnablementChanged(boolean enabled);
}
