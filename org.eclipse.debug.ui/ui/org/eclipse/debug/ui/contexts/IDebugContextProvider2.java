/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Allow multiple debug views and 
 *     		multiple debug context providers (Bug 327263)
 *****************************************************************/
package org.eclipse.debug.ui.contexts;

/**
 * IDebugContextProvider extension.
 * 
 * @since 3.7
 */
public interface IDebugContextProvider2 extends IDebugContextProvider {
	/**
	 * Return whether the provider can be set a an active provider for the 
	 * window.  
	 * <p>
	 * If <code>true</code> then upon the provider's part is 
	 * activation this provider will become the active debug context provider
	 * for the whole window.  If <code>false</code>, then this provider will
	 * only set the active context in a given workbench part.
	 *  
	 * @return <code>true</code> if capable to be the active debug context 
	 * provider for a window.
	 */
	boolean isWindowContextProvider();
}
