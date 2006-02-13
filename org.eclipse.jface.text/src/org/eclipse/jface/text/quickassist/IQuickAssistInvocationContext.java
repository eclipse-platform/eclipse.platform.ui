/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.quickassist;

import org.eclipse.jface.text.ITextViewer;


/**
 * Context information for quick fix and quick assist processors.
 * <p>
 * This interface can be implemented by clients.</p>
 * 
 * @since 3.2
 */
public interface IQuickAssistInvocationContext {

	/**
	 * @return the offset of the current selection or <code>-1</code> if unknown
	 */
	int getSelectionOffset();

	/**
	 * Returns the selection length.
	 * 
	 * @return the length of the current selection or <code>-1</code> if unknown
	 */
	int getSelectionLength();
	
	/**
	 * Returns the viewer for this context.
	 * 
	 * @return the viewer or <code>null</code> if not available
	 */
	ITextViewer getTextViewer();
}
