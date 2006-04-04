/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

/**
 * A calendarable dispose listener because Calendarable objects don't have
 * any SWT parent.
 * 
 * @since 3.2
 */
public interface DisposeListener {

	/**
	 * Called when the calendarable object is about to be removed from the
	 * GUI.  GUI elements hooked up to the calenderable should then dispose
	 * themselves.
	 * 
	 * @param sender The Calendarable being disposed.
	 */
	public void widgetDisposed(Calendarable sender);
}
