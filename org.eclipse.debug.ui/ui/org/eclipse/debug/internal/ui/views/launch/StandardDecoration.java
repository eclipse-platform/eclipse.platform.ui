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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.ui.IEditorPart;

/**
 * A reminder to remove any annotations created by an editor presentation
 */
public class StandardDecoration extends Decoration {
	
	private IThread fThread;
	private IEditorPart fEditor;
	private IDebugEditorPresentation fPresentation;
	
	public StandardDecoration(IDebugEditorPresentation presentation, IEditorPart editorPart, IThread thread) {
		fThread = thread;
		fEditor = editorPart;
		fPresentation = presentation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.launch.Decoration#remove()
	 */
	public void remove() {
		fPresentation.removeAnnotations(fEditor, fThread);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.launch.Decoration#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}

}
