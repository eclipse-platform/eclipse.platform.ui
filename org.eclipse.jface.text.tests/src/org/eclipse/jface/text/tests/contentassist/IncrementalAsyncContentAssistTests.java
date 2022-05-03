/*******************************************************************************
 * Copyright (c) 2020 Julian Honnen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Julian Honnen - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.contentassist;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class IncrementalAsyncContentAssistTests {

	private Shell shell;

	private SourceViewer viewer;

	private ContentAssistant ca;

	@Before
	public void setup() {
		tearDown();

		shell= new Shell();
		shell.setSize(300, 300);
		shell.open();
		DisplayHelper.driveEventQueue(shell.getDisplay());

		viewer= new SourceViewer(shell, null, SWT.NONE);
		Document document= new Document();
		viewer.setDocument(document);
		ca= new ContentAssistant(true);

		Comparator<ICompletionProposal> comparator= Comparator.comparing(ICompletionProposal::getDisplayString);
		ca.setSorter(comparator::compare);
	}

	@After
	public void tearDown() {
		if (shell != null) {
			ca.uninstall();
			if (!shell.isDisposed()) {
				shell.dispose();
			}
			shell= null;
		}
	}

	@Test
	public void testIncrementalComplete() throws Exception {
		ca.addContentAssistProcessor(new FilteringAsyncContentAssistTests.ImmediateContentAssistProcessor("testC", "testB", "testA"), IDocument.DEFAULT_CONTENT_TYPE);

		viewer.getDocument().set("t");

		ca.install(viewer);
		viewer.setSelectedRange(1, 0);

		ca.completePrefix();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		List<String> filteredProposals= FilteringAsyncContentAssistTests.getFilteredProposals(ca).stream() //
				.map(ICompletionProposal::getDisplayString) //
				.collect(toList());
		assertEquals(Arrays.asList("testA", "testB", "testC"), filteredProposals);
	}

	@Test
	public void testIncrementalComplete_async() throws Exception {
		long delay= 200;
		ca.addContentAssistProcessor(new FilteringAsyncContentAssistTests.DelayedContentAssistProcessor(Arrays.asList("testC", "testB", "testA"), delay, false), IDocument.DEFAULT_CONTENT_TYPE);

		viewer.getDocument().set("t");

		ca.install(viewer);
		viewer.setSelectedRange(1, 0);

		ca.completePrefix();

		DisplayHelper.sleep(shell.getDisplay(), delay + 100);

		List<String> filteredProposals= FilteringAsyncContentAssistTests.getFilteredProposals(ca).stream() //
				.map(ICompletionProposal::getDisplayString) //
				.collect(toList());
		assertEquals(Arrays.asList("testA", "testB", "testC"), filteredProposals);
	}

	@Test
	public void testIncrementalCompleteOfSingleProposal() throws Exception {
		ca.enableAutoInsert(true);
		ca.addContentAssistProcessor(new FilteringAsyncContentAssistTests.ImmediateContentAssistProcessor("testA"), IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);
		viewer.setSelectedRange(0, 0);

		ca.completePrefix();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		assertEquals("testA", viewer.getDocument().get());
	}

}
