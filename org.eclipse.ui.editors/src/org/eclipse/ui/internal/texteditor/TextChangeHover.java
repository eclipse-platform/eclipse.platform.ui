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
package org.eclipse.ui.internal.texteditor;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.LineChangeHover;



/**
 * Change hover for text editors. Respects tab settings and text editor font.
 * 
 * @since 3.0
 */
public class TextChangeHover extends LineChangeHover {
	
	/** The last created information control. */
	SourceViewerInformationControl fInformationControl;

	/*
	 * @see org.eclipse.jface.text.source.LineChangeHover#getTabReplacement()
	 */
	protected String getTabReplacement() {
		return Character.toString('\t');
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				fInformationControl= new SourceViewerInformationControl(parent, JFaceResources.TEXT_FONT);
				return fInformationControl;
			}
		};
	}
}
