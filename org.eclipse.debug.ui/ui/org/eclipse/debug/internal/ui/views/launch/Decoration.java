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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.core.model.IThread;

/**
 * A decoration in an editor, created by the debugger.
 */
public abstract class Decoration {
	
	/**
	 * Removes this decoration
	 */
	public abstract void remove();
	
	/**
	 * Returns the thread this decoration decorates.
	 * 
	 * @return thead associated with this decoration
	 */
	public abstract IThread getThread();

}
