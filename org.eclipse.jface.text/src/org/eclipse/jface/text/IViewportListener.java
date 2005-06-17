/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;



/**
 * Registered with a text viewer, view port listeners are informed about changes
 * of a text viewer's view port. The view port is that portion of the viewer's
 * document which is visible in the viewer.
 * <p>
 * Clients may implement this interface.</p>
 *
 * @see org.eclipse.jface.text.ITextViewer
 */
public interface IViewportListener {

	/**
	 * Informs about view port changes. The given vertical position is the new
	 * vertical scrolling offset measured in pixels.
	 *
	 * @param verticalOffset the vertical offset measured in pixels
	 */
	void viewportChanged(int verticalOffset);
}
