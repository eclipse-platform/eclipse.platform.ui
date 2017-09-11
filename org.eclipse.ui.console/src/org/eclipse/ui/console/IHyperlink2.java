/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import org.eclipse.swt.widgets.Event;

/**
 * Optional extension to {@link IHyperlink}.
 * <p>
 * Clients implementing {@link IHyperlink} may also implement this interface.
 * When implemented, the method <code>linkActivated(Event)</code> is called instead of
 * <code>linkActivated()</code>.
 * </p>
 * @since 3.2
 */
public interface IHyperlink2 extends IHyperlink {
	
	/**
	 * Notification that this link has been activated. Performs
	 * context specific linking.
	 * 
	 * @param event the SWT event which triggered this hyperlink
	 */
	public void linkActivated(Event event);
}
