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
package org.eclipse.debug.internal.ui.contexts;

/**
 * Adapter retrieved from an <code>ILaunch</code> that notifies
 * listeners when it suspends. A suspend trigger is responsible
 * for cleaning itself up when it's launch is no longer capable
 * of suspending.
 * 
 * @since 3.2
 */
public interface ISuspendTrigger {
	
	public void addSuspendTriggerListener(ISuspendTriggerListener listener);
	public void removeSuspendTriggerListener(ISuspendTriggerListener listener);
	
}
