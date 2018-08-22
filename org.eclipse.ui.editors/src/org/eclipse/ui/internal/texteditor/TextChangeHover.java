/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	protected String getTabReplacement() {
		return Character.toString('\t');
	}

	@Override
	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleLines) {
		fLastScrollIndex= sourceViewer.getTextWidget().getHorizontalPixel();
		return super.getHoverInfo(sourceViewer, lineRange, visibleLines);
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				SourceViewerInformationControl control= new SourceViewerInformationControl(parent, false, JFaceResources.TEXT_FONT, EditorsUI.getTooltipAffordanceString());
				control.setHorizontalScrollPixel(fLastScrollIndex);
				return control;
			}
		};
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new SourceViewerInformationControl(parent, true, JFaceResources.TEXT_FONT, null);
			}
		};
	}

}
