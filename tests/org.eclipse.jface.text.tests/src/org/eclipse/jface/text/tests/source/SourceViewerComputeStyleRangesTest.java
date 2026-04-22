/*******************************************************************************
 * Copyright (c) 2026 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * Tests for {@link SourceViewer#computeStyleRanges(IDocument, org.eclipse.jface.text.IRegion)}.
 */
public class SourceViewerComputeStyleRangesTest {

	private static final RGB HIGHLIGHT_RGB= new RGB(0, 0, 255);
	private static final String NAMED_PARTITIONING= "test_partitioning"; //$NON-NLS-1$

	private Shell shell;
	private Color highlightColor;

	@BeforeEach
	public void setUp() {
		shell= new Shell();
		highlightColor= new Color(HIGHLIGHT_RGB);
	}

	@AfterEach
	public void tearDown() {
		highlightColor.dispose();
		shell.dispose();
	}

	@Test
	public void testBasicStyleRanges() throws Exception {
		SourceViewer viewer= createConfiguredViewer();
		var document= new Document("original content");
		setupDefaultPartitioning(document);
		viewer.setDocument(document);

		Document externalDoc= new Document("some 'highlighted' text");
		List<StyleRange> styles= viewer.computeStyleRanges(externalDoc, new Region(0, externalDoc.getLength()));

		assertNotNull(styles);
		assertFalse(styles.isEmpty(), "Expected style ranges for quoted text");
		// The SingleLineRule for 'x' should produce a style covering 'highlighted' (offsets 5..17)
		boolean foundHighlight= styles.stream().anyMatch(
				sr -> sr.start == 5 && sr.length == 13 && sr.foreground != null
						&& HIGHLIGHT_RGB.equals(sr.foreground.getRGB()));
		assertTrue(foundHighlight, "Expected a blue highlight style for the quoted region");
	}

	@Test
	public void testNoMatchingContent() throws Exception {
		SourceViewer viewer= createConfiguredViewer();
		var document= new Document("original content");
		setupDefaultPartitioning(document);
		viewer.setDocument(document);

		Document externalDoc= new Document("no special content here");
		List<StyleRange> styles= viewer.computeStyleRanges(externalDoc, new Region(0, externalDoc.getLength()));

		assertNotNull(styles);
		// All style ranges should have null foreground (default styling) since no rule matches
		for (StyleRange sr : styles) {
			assertTrue(sr.foreground == null || !HIGHLIGHT_RGB.equals(sr.foreground.getRGB()),
					"Expected no highlight color for unmatched content");
		}
	}

	@Test
	public void testRegionSubset() throws Exception {
		SourceViewer viewer= createConfiguredViewer();
		var document= new Document("original content");
		setupDefaultPartitioning(document);
		viewer.setDocument(document);

		// Put quoted text at position 10..22
		Document externalDoc= new Document("0123456789'highlighted'rest");
		// Request styles only for the region starting at offset 10, length 13
		List<StyleRange> styles= viewer.computeStyleRanges(externalDoc, new Region(10, 13));

		assertNotNull(styles);
		for (StyleRange sr : styles) {
			assertTrue(sr.start >= 10, "Style range should start at or after region start");
			assertTrue(sr.start + sr.length <= 23, "Style range should end at or before region end");
		}
		boolean foundHighlight= styles.stream().anyMatch(
				sr -> sr.foreground != null && HIGHLIGHT_RGB.equals(sr.foreground.getRGB()));
		assertTrue(foundHighlight, "Expected highlight within subset region");
	}

	@Test
	public void testOriginalDocumentNotAffected() throws Exception {
		SourceViewer viewer= createConfiguredViewer();
		String originalContent= "original content";
		Document originalDoc= new Document(originalContent);
		IDocumentPartitioner originalPartitioner= setupDefaultPartitioning(originalDoc);
		assertNotNull(originalPartitioner);
		viewer.setDocument(originalDoc);

		Document externalDoc= new Document("some 'highlighted' text");
		IDocumentPartitioner externalPartitioner= setupDefaultPartitioning(externalDoc);
		assertNotNull(externalPartitioner);
		viewer.computeStyleRanges(externalDoc, new Region(0, externalDoc.getLength()));

		assertEquals(originalContent, originalDoc.get(), "Original document content must not change");
		assertEquals(originalPartitioner, originalDoc.getDocumentPartitioner(IDocumentExtension3.DEFAULT_PARTITIONING),
				"Original document partitioner must be restored");
		assertEquals(externalPartitioner, externalDoc.getDocumentPartitioner(IDocumentExtension3.DEFAULT_PARTITIONING),
				"External document partitioner must be restored");
	}

	@Test
	public void testEmptyRegion() throws Exception {
		SourceViewer viewer= createConfiguredViewerWithNamedPartitioning();
		var document= new Document("original content");
		setupNamedPartitioning(document);
		viewer.setDocument(document);

		Document externalDoc= new Document("some 'highlighted' text");
		setupNamedPartitioning(externalDoc);
		List<StyleRange> styles= viewer.computeStyleRanges(externalDoc, new Region(0, 0));

		assertNotNull(styles);
		assertEquals(1, styles.size());
		// empty style range
		assertEquals(0, styles.get(0).start);
		assertEquals(0, styles.get(0).length);
		assertNull(styles.get(0).font);
	}

	@Test
	public void testMultipleStyleRanges() throws Exception {
		SourceViewer viewer= createConfiguredViewerWithNamedPartitioning();
		var document= new Document("original content");
		setupNamedPartitioning(document);
		viewer.setDocument(document);

		Document externalDoc= new Document("'first' normal 'second' end");
		List<StyleRange> styles= viewer.computeStyleRanges(externalDoc, new Region(0, externalDoc.getLength()));

		assertNotNull(styles);
		long highlightCount= styles.stream()
				.filter(sr -> sr.foreground != null && HIGHLIGHT_RGB.equals(sr.foreground.getRGB()))
				.count();
		assertTrue(highlightCount >= 2, "Expected at least 2 highlighted regions, got " + highlightCount);
	}

	@Test
	public void testBadLocationExceptionForOutOfBoundsRegion() throws Exception {
		SourceViewer viewer= createConfiguredViewer();
		var document= new Document("original content");
		setupDefaultPartitioning(document);
		viewer.setDocument(document);

		Document externalDoc= new Document("short");
		assertThrows(BadLocationException.class,
				() -> viewer.computeStyleRanges(externalDoc, new Region(0, 100)));
	}

	@Test
	public void testExceptionSafetyPartitionerRestored() throws Exception {
		SourceViewer viewer= createConfiguredViewerWithNamedPartitioning();
		Document originalDoc= new Document("original content");
		setupNamedPartitioning(originalDoc);
		viewer.setDocument(originalDoc);
		IDocumentPartitioner originalPartitioner= originalDoc
				.getDocumentPartitioner(NAMED_PARTITIONING);
		assertNotNull(originalPartitioner, "Original document should have a named partitioner");

		Document externalDoc= new Document("short");
		setupNamedPartitioning(externalDoc);
		IDocumentPartitioner externalPartitioner= externalDoc
				.getDocumentPartitioner(NAMED_PARTITIONING);
		assertNotNull(externalPartitioner, "External document should have a named partitioner");

		try {
			viewer.computeStyleRanges(externalDoc, new Region(0, 100));
		} catch (BadLocationException expected) {
			// expected
		}

		// Verify partitioner was restored to original document
		IDocumentPartitioner restoredPartitioner= originalDoc
				.getDocumentPartitioner(NAMED_PARTITIONING);
		assertNotNull(restoredPartitioner, "Partitioner must be restored to original document after exception");
		assertEquals(originalPartitioner, restoredPartitioner);

		IDocumentPartitioner restoredExternalPartitioner= externalDoc
				.getDocumentPartitioner(NAMED_PARTITIONING);
		assertNotNull(restoredExternalPartitioner, "External partitioner must be restored to original document after exception");
		assertEquals(externalPartitioner, restoredExternalPartitioner);
	}

	@Test
	public void testNamedPartitioning() throws Exception {
		SourceViewer viewer= createConfiguredViewerWithNamedPartitioning();
		Document originalDoc= new Document("original 'content' here");
		IDocumentPartitioner originalPartitioner= setupNamedPartitioning(originalDoc);
		assertNotNull(originalPartitioner);
		viewer.setDocument(originalDoc);

		Document externalDoc= new Document("external 'styled' text");
		IDocumentPartitioner externalPartitioner= setupNamedPartitioning(externalDoc);
		assertNotNull(externalPartitioner);
		List<StyleRange> styles= viewer.computeStyleRanges(externalDoc, new Region(0, externalDoc.getLength()));

		assertNotNull(styles);
		assertFalse(styles.isEmpty(), "Expected style ranges for quoted text with named partitioning");

		// Verify partitioner is reconnected to original document
		IDocumentPartitioner restoredPartitioner= originalDoc
				.getDocumentPartitioner(NAMED_PARTITIONING);
		assertNotNull(restoredPartitioner, "Partitioner must be restored to original document");
		assertEquals(originalPartitioner, restoredPartitioner);

		IDocumentPartitioner restoredExternalPartitioner= externalDoc
				.getDocumentPartitioner(NAMED_PARTITIONING);
		assertNotNull(restoredExternalPartitioner, "External partitioner must be restored to external document");
		assertEquals(externalPartitioner, restoredExternalPartitioner);
	}

	@Test
	public void testNonDocumentExtension3ReturnsEmpty() throws Exception {
		SourceViewer viewer= createConfiguredViewer();
		Document originalDoc= new Document("original content");
		setupDefaultPartitioning(originalDoc);
		viewer.setDocument(originalDoc);

		// Create a document that does NOT implement IDocumentExtension3
		// so that computeStyleRanges takes the early-return path
		IDocument document= mock(IDocument.class);
		List<StyleRange> styles= viewer.computeStyleRanges(document, new Region(0, document.getLength()));

		assertNotNull(styles);
		assertTrue(styles.isEmpty(), "Expected empty style ranges for non-IDocumentExtension3 document");
	}

	private SourceViewer createConfiguredViewer() {
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		viewer.configure(new SourceViewerConfiguration() {
			@Override
			public org.eclipse.jface.text.presentation.IPresentationReconciler getPresentationReconciler(
					ISourceViewer sourceViewer) {
				PresentationReconciler reconciler= new PresentationReconciler();
				DefaultDamagerRepairer dr= new DefaultDamagerRepairer(createScanner());
				reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
				reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
				return reconciler;
			}
		});
		return viewer;
	}

	private SourceViewer createConfiguredViewerWithNamedPartitioning() {
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		viewer.configure(new SourceViewerConfiguration() {
			@Override
			public org.eclipse.jface.text.presentation.IPresentationReconciler getPresentationReconciler(
					ISourceViewer sourceViewer) {
				PresentationReconciler reconciler= new PresentationReconciler();
				reconciler.setDocumentPartitioning(NAMED_PARTITIONING);
				DefaultDamagerRepairer dr= new DefaultDamagerRepairer(createScanner());
				reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
				reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
				return reconciler;
			}
		});
		return viewer;
	}

	private RuleBasedScanner createScanner() {
		RuleBasedScanner scanner= new RuleBasedScanner();
		IRule[] rules= new IRule[1];
		rules[0]= new SingleLineRule("'", "'", new Token(new TextAttribute(highlightColor))); //$NON-NLS-1$ //$NON-NLS-2$
		scanner.setRules(rules);
		return scanner;
	}

	private IDocumentPartitioner setupNamedPartitioning(Document document) {
		IPartitionTokenScanner partitionScanner= new RuleBasedPartitionScanner();
		IDocumentPartitioner partitioner= new FastPartitioner(partitionScanner, new String[] {});
		document.setDocumentPartitioner(NAMED_PARTITIONING, partitioner);
		partitioner.connect(document);
		return partitioner;
	}

	private IDocumentPartitioner setupDefaultPartitioning(Document document) {
		IPartitionTokenScanner partitionScanner= new RuleBasedPartitionScanner();
		IDocumentPartitioner partitioner= new FastPartitioner(partitionScanner, new String[] {});
		document.setDocumentPartitioner(IDocumentExtension3.DEFAULT_PARTITIONING, partitioner);
		partitioner.connect(document);
		return partitioner;
	}
}
