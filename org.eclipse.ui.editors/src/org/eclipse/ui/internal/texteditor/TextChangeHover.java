/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineChangeHover;

import org.eclipse.ui.editors.text.EditorsUI;



/**
 * Change hover for text editors. Respects tab settings and text editor font.
 *
 * @since 3.0
 */
public class TextChangeHover extends LineChangeHover {

	/** The last created information control. */
	private int fLastScrollIndex= 0;

	/*
	 * @see org.eclipse.jface.text.source.LineChangeHover#getTabReplacement()
	 */
	protected String getTabReplacement() {
		return Character.toString('\t');
	}

	/*
	 * @see org.eclipse.jface.text.source.LineChangeHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, org.eclipse.jface.text.source.ILineRange, int)
	 */
	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleLines) {
		fLastScrollIndex= sourceViewer.getTextWidget().getHorizontalPixel();
		return super.getHoverInfo(sourceViewer, lineRange, visibleLines);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				SourceViewerInformationControl control= new SourceViewerInformationControl(parent, false, JFaceResources.TEXT_FONT, EditorsUI.getTooltipAffordanceString());
				control.setHorizontalScrollPixel(fLastScrollIndex);
				return control;
			}
		};
	}

	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.3
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new SourceViewerInformationControl(parent, true, JFaceResources.TEXT_FONT, null);
			}
		};
	}

}
