/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IEditorPart;

/**
 * An adapter that converts the marker information to a postion
 * in the editor, and causes the editor to show that position.
 */
public interface IMarkerEditorPositioner {
	/**
	 * Convert the marker information to a position within the
	 * editor, and cause the editor to show that position.
	 * 
	 * @param marker the resource marker
	 * @param editor the editor to show the position
	 */
	public void gotoPosition(IMarker marker, IEditorPart editor);
}
