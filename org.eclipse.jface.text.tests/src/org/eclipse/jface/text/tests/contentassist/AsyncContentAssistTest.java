/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.jface.text.tests.contentassist;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class AsyncContentAssistTest {

	private ILogListener listener;
	private IStatus errorStatus;

	@Before
	public void setUp() {
		listener= (status, plugin) -> {
			if(status.getSeverity() == IStatus.ERROR && "org.eclipse.jface.text".equals(status.getPlugin())) {
				errorStatus = status;
			}
		};
		Platform.addLogListener(listener);
	}

	@After
	public void tearDown() {
		Platform.removeLogListener(listener);
	}

	@Test
	public void testAsyncFailureStackOverflow() {
		Shell shell = new Shell();
		SourceViewer viewer = new SourceViewer(shell, null, SWT.NONE);
		Document document = new Document("a");
		viewer.setDocument(document);
		ContentAssistant contentAssistant = new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new DelayedErrorContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.addContentAssistProcessor(new ImmediateContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.install(viewer);
		contentAssistant.showPossibleCompletions();
		document.set("ab"); // Simulate user typing a key when popup visible
		DisplayHelper.sleep(shell.getDisplay(), 2000);
		assertNotNull(errorStatus);
	}

	@Test
	public void testSyncFailureNPE() {
		Shell shell = new Shell();
		SourceViewer viewer = new SourceViewer(shell, null, SWT.NONE);
		Document document = new Document("a");
		viewer.setDocument(document);
		ContentAssistant contentAssistant = new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new ImmediateContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.addContentAssistProcessor(new ImmediateNullContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.install(viewer);
		contentAssistant.showPossibleCompletions();
		document.set("ab"); // Simulate user typing a key when popup visible
		DisplayHelper.sleep(shell.getDisplay(), 1000);
		assertNull(errorStatus);
	}

	@Test
	public void testCompletePrefix() {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(500, 300);
		SourceViewer viewer = new SourceViewer(shell, null, SWT.NONE);
		Document document = new Document("b");
		viewer.setDocument(document);
		viewer.setSelectedRange(1, 0);
		ContentAssistant contentAssistant = new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new BarContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.enablePrefixCompletion(true);
		contentAssistant.install(viewer);
		shell.open();
		Display display = shell.getDisplay();
		final Set<Shell> beforeShells = Arrays.stream(display.getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		contentAssistant.showPossibleCompletions();
		assertTrue("Completion item not shown", new DisplayHelper() {
			@Override
			protected boolean condition() {
				Set<Shell> newShells = Arrays.stream(display.getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
				newShells.removeAll(beforeShells);
				if (!newShells.isEmpty()) {
					Table completionTable = findCompletionSelectionControl(newShells.iterator().next());
					return Arrays.stream(completionTable.getItems()).map(TableItem::getText).anyMatch(item -> item.contains(BarContentAssistProcessor.PROPOSAL.substring(document.getLength())));
				}
				return false;
			}
		}.waitForCondition(display, 2000));
	}

	private static Table findCompletionSelectionControl(Widget control) {
		if (control instanceof Table) {
			return (Table)control;
		} else if (control instanceof Composite) {
			for (Widget child : ((Composite)control).getChildren()) {
				Table res = findCompletionSelectionControl(child);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}
}
