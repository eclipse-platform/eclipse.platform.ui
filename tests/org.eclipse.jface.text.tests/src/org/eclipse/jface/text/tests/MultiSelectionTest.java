/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.edits.MalformedTreeException;

import org.eclipse.jface.internal.text.SelectionProcessor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class MultiSelectionTest {

	@Test
	public void testSelectionProcessor() throws MalformedTreeException, BadLocationException {
		Shell shell= new Shell();
		TextViewer textViewer= new TextViewer(shell, SWT.NONE);
		String content = "ababa\nbaba";
		Document document= new Document(content);
		List<Region> regions = new ArrayList<>();
		int index = 0;
		while ((index = document.get().indexOf('a', index)) >= 0) {
			regions.add(new Region(index, 1));
			index++;
		}
		textViewer.setDocument(document);
		SelectionProcessor selectionProcessor = new SelectionProcessor(textViewer);
		MultiTextSelection selection = new MultiTextSelection(document, regions.toArray(new IRegion[regions.size()]));
		assertEquals(2, selectionProcessor.getCoveredLines(selection));
		assertEquals("aaaaa", selectionProcessor.getText(selection));
		//
		document.set(content);
		selectionProcessor.doDelete(selection);
		assertEquals("bb\nbb", document.get());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(1, 0), new Region(2, 0), new Region(4, 0), new Region(5, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
		//
		document.set(content);
		selectionProcessor.doBackspace(selection);
		assertEquals("bb\nbb", document.get());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(1, 0), new Region(2, 0), new Region(4, 0), new Region(5, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
		//
		document.set(content);
		selectionProcessor.doReplace(selection, "cc");
		assertEquals("ccbccbcc\nbccbcc", document.get());
		assertArrayEquals(new IRegion[] { new Region(2, 0), new Region(5, 0), new Region(8, 0), new Region(12, 0), new Region(15, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
		//
		selection = new MultiTextSelection(document, regions.stream().map(region -> new Region(region.getOffset(), 0)).toArray(IRegion[]::new));
		document.set(content);
		selectionProcessor.doDelete(selection);
		assertEquals("bb\nbb", document.get());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(1, 0), new Region(2, 0), new Region(4, 0), new Region(5, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
		//
		document.set(content);
		selectionProcessor.doBackspace(selection);
		assertEquals("aaa\naa", document.get());
		assertArrayEquals(new IRegion[] { new Region(0, 0), new Region(1, 0), new Region(2, 0), new Region(4, 0), new Region(5, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
		//
		document.set(content);
		selectionProcessor.doReplace(selection, "cc");
		assertEquals("ccabccabcca\nbccabcca", document.get());
		assertArrayEquals(new IRegion[] { new Region(2, 0), new Region(6, 0), new Region(10, 0), new Region(15, 0), new Region(19, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
	}

	@Test
	public void testCopyPaste() throws MalformedTreeException, BadLocationException {
		Shell shell= new Shell();
		TextViewer textViewer= new TextViewer(shell, SWT.NONE);
		String content= "ababa\nbaba";
		Document document= new Document(content);
		List<Region> regions= new ArrayList<>();
		int index= 0;
		while ((index= document.get().indexOf('a', index)) >= 0) {
			regions.add(new Region(index, 0));
			index++;
		}
		textViewer.setDocument(document);
		SelectionProcessor selectionProcessor= new SelectionProcessor(textViewer);
		MultiTextSelection selection= new MultiTextSelection(document, regions.toArray(new IRegion[regions.size()]));
		//
		AtomicInteger idx= new AtomicInteger();
		selectionProcessor.doReplace(selection,
				Arrays.stream(selection.getRegions()).mapToInt(r -> idx.getAndIncrement()).mapToObj(Integer::toString).collect(Collectors.joining(System.lineSeparator())));
		assertEquals("0ab1ab2a\nb3ab4a", document.get());
		assertArrayEquals(new IRegion[] { new Region(1, 0), new Region(4, 0), new Region(7, 0), new Region(11, 0), new Region(14, 0) },
				((IMultiTextSelection) textViewer.getSelectionProvider().getSelection()).getRegions());
	}

	@Test
	public void testBackspace() throws MalformedTreeException {
		Shell shell= new Shell();
		TextViewer textViewer= new TextViewer(shell, SWT.NONE);
		String content = "ababa\nbaba";
		Document document= new Document(content);
		List<Region> regions = new ArrayList<>();
		int index = 0;
		while ((index = document.get().indexOf('a', index)) >= 0) {
			regions.add(new Region(index + 1, 0));
			index++;
		}
		textViewer.setDocument(document);
		IMultiTextSelection selection = new MultiTextSelection(document, regions.toArray(new IRegion[regions.size()]));
		textViewer.setSelection(selection);
		Event keyEvent = new Event();
		keyEvent.type = SWT.KeyDown;
		keyEvent.widget = textViewer.getTextWidget();
		keyEvent.display = textViewer.getTextWidget().getDisplay();
		keyEvent.doit = true;
		keyEvent.keyCode = SWT.BS;
		keyEvent.character = 0;
		textViewer.getTextWidget().notifyListeners(SWT.KeyDown, keyEvent);
		assertEquals("bb\nbb", textViewer.getDocument().get());
		ISelection sel = textViewer.getSelection();
		assertTrue(sel instanceof IMultiTextSelection);
		selection = (IMultiTextSelection)sel;
		assertArrayEquals(new IRegion[] {
				new Region(0, 0),
				new Region(1, 0),
				new Region(2, 0),
				new Region(4, 0),
				new Region(5, 0)},
			selection.getRegions());
	}

	@Test
	@Ignore(value = "this is currently for manual testing")
	public void testViewer() {
		Shell shell= new Shell();
		Button b = new Button(shell, SWT.PUSH);
		b.setText("Reset selection");
		TextViewer textViewer= new TextViewer(shell, SWT.NONE);
		String content = "ababa\nbaba";
		Document document= new Document(content);
		List<Region> regions = new ArrayList<>();
		int index = 0;
		while ((index = document.get().indexOf('a', index)) >= 0) {
			regions.add(new Region(index, 1));
			index++;
		}
		MultiTextSelection selection = new MultiTextSelection(document, regions.toArray(new IRegion[regions.size()]));
		textViewer.setDocument(document);
		shell.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(shell);
		shell.pack();
		shell.setVisible(true);
		b.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			textViewer.setSelection(selection);
			textViewer.getTextWidget().setFocus();
		}));
		DisplayHelper.sleep(textViewer.getTextWidget().getDisplay(), 1000000);
	}
}
