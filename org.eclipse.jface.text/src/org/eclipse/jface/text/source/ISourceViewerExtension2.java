/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.source;

/**
 * Extension interface for {@link ISourceViewer}.
 * Extends the source viewer with the functionality of explicit
 * unconfiguration.
 * 
 * @since 3.0
 */
public interface ISourceViewerExtension2 {
	
	/**
	 * Unconfigures this source viewer. The source viewer can be configured again
	 * after a call to this method. Unlike {@link ISourceViewer#configure(SourceViewerConfiguration)}
	 * this method can be called more than once without interleaving calls to
	 * {@link ISourceViewer#configure(SourceViewerConfiguration)}.
	 */
	void unconfigure();
	
	/**
	 * Returns the visual annotation model of this viewer.
	 * 
	 * @return the visual annotation model of this viewer
	 */
	IAnnotationModel getVisualAnnotationModel();
}
