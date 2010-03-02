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
package org.eclipse.ui.internal.dnd;

/**
 * This interface allows a particular drop target to be informed that
 * the drag operation was cancelled. This allows the target to clean
 * up any extended drag feedback.
 * 
 * @since 3.2
 *
 */
public interface IDropTarget2 extends IDropTarget {
	/**
	 * This is called whenever a drag operation is cancelled
	 */
	public void dragFinished(boolean dropPerformed);
}
