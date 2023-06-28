/*******************************************************************************
 * Copyright (c) 2017, 2019 Stephan Wahlbrink and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Wahlbrink <sw@wahlbrink.eu>
 *******************************************************************************/
package org.eclipse.jface.text.tests.contentassist;

import static org.eclipse.jface.text.tests.contentassist.ContextInformationTest.getInfoText;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;


public class ContextInformationPresenterTest extends AbstractContentAssistTest {

	private static class ValidatorWithPresenter extends BarContentAssistProcessor.ContextInformationValidator
			implements IContextInformationPresenter {

		private boolean isStyled;

		/**
		 * Hightlight (bold) "idx" if caret is inside the word (including end)
		 */
		@Override
		public boolean updatePresentation(int offset, TextPresentation presentation) {
			if (info == null) { // Ignore unknown information
				return false;
			}

			int begin= info.getContextInformationPosition();
			int end= begin + info.getContextDisplayString().length();
			boolean style= (offset >= begin && offset <= end);
			if (style == isStyled) {
				return false;
			}
			if (style) {
				presentation.clear();
				presentation.addStyleRange(new StyleRange(0, 3, null, null, SWT.BOLD));
			} else {
				presentation.clear();
			}
			isStyled= style;
			return true;
		}

	}


	private Shell infoShell;


	private ContentAssistant createBarContentAssist() {
		ContentAssistant contentAssistant= new ContentAssistant();
		contentAssistant.setContentAssistProcessor(new BarContentAssistProcessor() {
			@Override
			public IContextInformationValidator getContextInformationValidator() {
				return new ValidatorWithPresenter();
			}
		}, IDocument.DEFAULT_CONTENT_TYPE);
		return contentAssistant;
	}

	@Test
	public void testContextInfo_withStyledTextPresentation() throws Exception {
		setupSourceViewer(createBarContentAssist(), BarContentAssistProcessor.PROPOSAL);

		postSourceViewerKeyEvent(SWT.ARROW_RIGHT, 0, SWT.KeyDown);
		selectAndReveal(4, 0);
		processEvents();

		final List<Shell> beforeShells= getCurrentShells();
		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));
		assertArrayEquals(new StyleRange[] {
				new StyleRange(0, 3, null, null, SWT.BOLD)
		}, getInfoStyleRanges(this.infoShell));

		emulatePressArrowKey(SWT.ARROW_RIGHT);

		assertEquals("idx= 0", getInfoText(this.infoShell));
		assertArrayEquals(new StyleRange[] {
		}, getInfoStyleRanges(this.infoShell));
	}


	static StyleRange[] getInfoStyleRanges(final Shell shell) {
		assertTrue(shell.isVisible());
		Control[] children= shell.getChildren();
		for (Control child : children) {
			if (child instanceof Text) {
				return null;
			}
			if (child instanceof StyledText) {
				return ((StyledText) child).getStyleRanges();
			}
		}
		return null;
	}

}
