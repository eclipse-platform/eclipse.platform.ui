/*******************************************************************************
 * Copyright (c) 2019 Stephan Wahlbrink and others.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.tests.util.DisplayHelper;


public class AbstractContentAssistTest {


	private Shell shell;
	private SourceViewer viewer;
	private ContentAssistant assistant;
	private Document document;

	private Button button;


	public AbstractContentAssistTest() {
	}


	@After
	public void close() {
		if (shell != null && !shell.isDisposed()) {
			shell.close();
		}
	}


	protected void setupSourceViewer(ContentAssistant contentAssistant, String initialText) {
		shell= new Shell();
		shell.setSize(500, 280);
		shell.setLayout(new GridLayout());

		viewer= new SourceViewer(shell, null, SWT.NONE);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		assistant= contentAssistant;
		viewer.configure(createSourceViewerConfiguration());

		document= new Document();
		if (initialText != null) {
			document.set(initialText);
		}
		viewer.setDocument(document);

		button= new Button(shell, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		shell.open();
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return viewer.getTextWidget().isVisible();
			}
		}.waitForCondition(shell.getDisplay(), 3000));
	}

	protected SourceViewerConfiguration createSourceViewerConfiguration() {
		return new SourceViewerConfiguration() {
			@Override
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				return AbstractContentAssistTest.this.getContentAssistant();
			}
		};
	}

	protected SourceViewer getSourceViewer() {
		return viewer;
	}

	protected Document getDocument() {
		return document;
	}

	protected ContentAssistant getContentAssistant() {
		return assistant;
	}

	protected void selectAndReveal(int selectionStart, int selectionLength) {
		StyledText widget= viewer.getTextWidget();
		widget.setRedraw(false);

		viewer.revealRange(selectionStart, selectionLength);
		viewer.setSelectedRange(selectionStart, selectionLength);

		widget.setRedraw(true);
	}

	protected void postSourceViewerKeyEvent(int keyCode, int stateMask, int type) {
		processEvents();
		Event event= new Event();
		event.type= type;
		event.widget= viewer.getTextWidget();
		event.display= viewer.getTextWidget().getDisplay();
		event.keyCode= keyCode;
		event.stateMask= stateMask;
		event.doit= true;
		viewer.getTextWidget().notifyListeners(type, event);
		processEvents();
	}

	protected void postSourceViewerKeyEvent(char character, int stateMask, int type) {
		processEvents();
		Event event= new Event();
		event.type= type;
		event.widget= viewer.getTextWidget();
		event.display= viewer.getTextWidget().getDisplay();
		event.character= character;
		event.stateMask= stateMask;
		event.doit= true;
		viewer.getTextWidget().notifyListeners(type, event);
		processEvents();
	}

	protected void emulatePressArrowKey(int keyCode) {
		switch (keyCode) {
		case SWT.ARROW_LEFT:
			viewer.getTextWidget().invokeAction(ST.COLUMN_PREVIOUS);
			break;
		case SWT.ARROW_RIGHT:
			viewer.getTextWidget().invokeAction(ST.COLUMN_NEXT);
			break;
		case SWT.ARROW_UP:
			viewer.getTextWidget().invokeAction(ST.LINE_UP);
			break;
		case SWT.ARROW_DOWN:
			viewer.getTextWidget().invokeAction(ST.LINE_DOWN);
			break;
		}
		postSourceViewerKeyEvent(keyCode, 0, ST.VerifyKey);
	}

	protected void emulatePressEscKey() {
		postSourceViewerKeyEvent(SWT.ESC, 0, ST.VerifyKey);
	}

	protected void runTextOperation(int operation) {
		ITextOperationTarget textOperationTarget= viewer.getTextOperationTarget();

		assertTrue(textOperationTarget.canDoOperation(operation));
		textOperationTarget.doOperation(operation);
	}

	protected void triggerContextInformation() {
		runTextOperation(ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
	}


	public Button getButton() {
		return button;
	}


	protected void processEvents() {
		DisplayHelper.driveEventQueue(shell.getDisplay());
	}

	protected List<Shell> getCurrentShells() {
		return Arrays.stream(shell.getDisplay().getShells())
				.filter(Shell::isVisible)
				.collect(Collectors.toList());
	}

	protected List<Shell> findNewShells(Collection<Shell> beforeShells) {
		return Arrays.stream(shell.getDisplay().getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.collect(Collectors.toList());
	}

	protected Shell findNewShell(Collection<Shell> beforeShells) {
		DisplayHelper.sleep(shell.getDisplay(), 100);
		List<Shell> afterShells= findNewShells(beforeShells);
		if (afterShells.isEmpty()) {
			DisplayHelper.sleep(shell.getDisplay(), 1000);
		}
		afterShells= findNewShells(beforeShells);
		assertEquals("No new shell found", 1, afterShells.size());
		return afterShells.get(0);
	}

}
