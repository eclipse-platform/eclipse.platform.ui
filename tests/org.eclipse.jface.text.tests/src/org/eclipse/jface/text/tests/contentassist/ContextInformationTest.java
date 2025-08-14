/*******************************************************************************
 * Copyright (c) 2017, 2025 Stephan Wahlbrink and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.util.List;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.Platform;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;


public class ContextInformationTest extends AbstractContentAssistTest {


	private Shell infoShell;


	private ContentAssistant createBarContentAssist() {
		ContentAssistant contentAssistant= new ContentAssistant();
		contentAssistant.setContentAssistProcessor(new BarContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		return contentAssistant;
	}

	@Test
	public void testContextInfo() throws Exception {
		setupSourceViewer(createBarContentAssist(), BarContentAssistProcessor.PROPOSAL);

		selectAndReveal(4, 0);
		processEvents();

		final List<Shell> beforeShells= getCurrentShells();
		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));

		selectAndReveal(8, 0);
		processEvents();

		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 1", getInfoText(this.infoShell));
	}

	@Test
	public void testContextInfo_hide_Bug512251() throws Exception {
		setupSourceViewer(createBarContentAssist(), BarContentAssistProcessor.PROPOSAL);

		selectAndReveal(4, 0);
		processEvents();

		final List<Shell> beforeShells= getCurrentShells();
		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);

		selectAndReveal(8, 0);
		processEvents();

		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);

		// ITextEditorActionConstants.DELETE_LINE
		getDocument().set("");

		new Accessor(getContentAssistant(), ContentAssistant.class).invoke("hide");
	}

	@Test
	public void testContextInfo_hide_focusOut() throws Exception {
		// opens source viewer shell:
		setupSourceViewer(createBarContentAssist(), BarContentAssistProcessor.PROPOSAL);

		selectAndReveal(4, 0);
		processEvents();

		final List<Shell> beforeShells= getCurrentShells();
		// opens content assist shell:
		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));

		selectAndReveal(8, 0);
		processEvents();

		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 1", getInfoText(this.infoShell));

		// Hide all
		getButton().setFocus();
		// hides and disposes Shell (by org.eclipse.jface.text.contentassist.ContentAssistant.hide()):
		processEvents();
		assumeFalse("Test fails on Mac: Bug 558989", Platform.OS_MACOSX.equals(Platform.getOS()));
		assumeFalse("Test fails on CentOS 8: See https://github.com/eclipse-platform/eclipse.platform.text/pull/162", Platform.OS_LINUX.equals(Platform.getOS()));
		assertTrue("Shell not disposed:" + this.infoShell, this.infoShell.isDisposed());
	}

	@Test
	public void testContextInfo_hide_keyEsc() throws Exception {
		setupSourceViewer(createBarContentAssist(), BarContentAssistProcessor.PROPOSAL);

		selectAndReveal(4, 0);
		processEvents();

		final List<Shell> beforeShells= getCurrentShells();
		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));

		selectAndReveal(8, 0);
		processEvents();

		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 1", getInfoText(this.infoShell));

		emulatePressEscKey();
		processEvents();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));

		emulatePressEscKey();
		processEvents();
		assertTrue(this.infoShell.isDisposed() || !this.infoShell.isVisible());
	}

	@Test
	public void testContextInfo_hide_validRange() throws Exception {
		setupSourceViewer(createBarContentAssist(), BarContentAssistProcessor.PROPOSAL + '\n');

		selectAndReveal(4, 0);
		processEvents();

		final List<Shell> beforeShells= getCurrentShells();
		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));

		selectAndReveal(8, 0);
		processEvents();

		triggerContextInformation();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 1", getInfoText(this.infoShell));

		emulatePressArrowKey(SWT.ARROW_LEFT);
		processEvents();
		this.infoShell= findNewShell(beforeShells);
		assertEquals("idx= 0", getInfoText(this.infoShell));

		emulatePressArrowKey(SWT.ARROW_DOWN);
		processEvents();
		assertTrue(this.infoShell.isDisposed() || !this.infoShell.isVisible());
	}


	static String getInfoText(final Shell shell) {
		assertTrue(shell.isVisible());
		Control[] children= shell.getChildren();
		for (Control child : children) {
			if (child instanceof Text) {
				return ((Text) child).getText();
			}
			if (child instanceof StyledText) {
				return ((StyledText) child).getText();
			}
		}
		return null;
	}

}
