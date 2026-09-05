/*******************************************************************************
 * Copyright (c) 2026 Eclipse Platform contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import org.eclipse.ui.internal.texteditor.codemining.BlockEndCodeMiningProvider;
import org.eclipse.ui.internal.texteditor.codemining.BlockEndCodeMiningProvider.BlockEnd;

/**
 * Tests the structural block matching of {@link BlockEndCodeMiningProvider}.
 */
public class BlockEndCodeMiningTest {

	private static final int MIN_LINES= 20;

	private static final String COMMENT= "__comment"; //$NON-NLS-1$

	private static final String STRING= "__string"; //$NON-NLS-1$

	private static IDocument document(String text) {
		return new Document(text);
	}

	/**
	 * Returns a document with a partitioner that puts Java-like comments and string
	 * literals into their own partitions.
	 */
	private static IDocument partitionedDocument(String text) {
		Document document= new Document(text);
		IToken comment= new Token(COMMENT);
		IToken string= new Token(STRING);
		RuleBasedPartitionScanner scanner= new RuleBasedPartitionScanner();
		scanner.setPredicateRules(new IPredicateRule[] {
				new EndOfLineRule("//", comment), //$NON-NLS-1$
				new MultiLineRule("/*", "*/", comment), //$NON-NLS-1$ //$NON-NLS-2$
				new SingleLineRule("\"", "\"", string, '\\'), //$NON-NLS-1$ //$NON-NLS-2$
		});
		IDocumentPartitioner partitioner= new FastPartitioner(scanner, new String[] { COMMENT, STRING });
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		return document;
	}

	private static List<BlockEnd> blockEnds(IDocument document) {
		return blockEnds(document, MIN_LINES);
	}

	private static List<BlockEnd> blockEnds(IDocument document, int minLines) {
		return BlockEndCodeMiningProvider.computeBlockEnds(document, minLines, null);
	}

	private static String body(int lines) {
		return body(lines, "\n"); //$NON-NLS-1$
	}

	private static String body(int lines, String delimiter) {
		StringBuilder builder= new StringBuilder();
		for (int i= 0; i < lines; i++) {
			builder.append("\tstatement").append(i).append("();").append(delimiter); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return builder.toString();
	}

	@Test
	public void longBlockIsAnnotated() {
		IDocument document= document("void method() {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(26, ends.get(0).endLine());
	}

	@Test
	public void shortBlockIsNotAnnotated() {
		IDocument document= document("void method() {\n" + body(3) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue(blockEnds(document).isEmpty());
	}

	@Test
	public void blockOfExactlyMinLinesIsAnnotated() {
		// The opening and the closing brace line count towards the block size.
		IDocument document= document("void method() {\n" + body(MIN_LINES - 2) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals(MIN_LINES - 1, ends.get(0).endLine());
	}

	@Test
	public void blockOneLineShorterThanMinLinesIsNotAnnotated() {
		IDocument document= document("void method() {\n" + body(MIN_LINES - 3) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue(blockEnds(document).isEmpty());
	}

	@Test
	public void nestingAloneDoesNotQualifyAShortBlock() {
		IDocument document= document("if (a) {\n\tif (b) {\n\t\tx();\n\t}\n}\n"); //$NON-NLS-1$

		assertTrue(blockEnds(document).isEmpty());
	}

	@Test
	public void singleLineBlockIsNotAnnotatedForAnyThreshold() {
		IDocument document= document("if (a) { x(); }\n"); //$NON-NLS-1$

		assertTrue(blockEnds(document, 1).isEmpty());
		assertTrue(blockEnds(document, 2).isEmpty());
	}

	@Test
	public void nestedBlocksAreReportedInnermostFirst() {
		IDocument document= document("outer() {\n\tinner() {\n" + body(25) + "\t}\n}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(2, ends.size());
		assertEquals("// inner()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(27, ends.get(0).endLine());
		assertEquals("// outer()", ends.get(1).label()); //$NON-NLS-1$
		assertEquals(28, ends.get(1).endLine());
	}

	@Test
	public void siblingBlocksAreBothAnnotated() {
		IDocument document= document("a() {\n" + body(25) + "}\nb() {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(2, ends.size());
		assertEquals("// a()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals("// b()", ends.get(1).label()); //$NON-NLS-1$
	}

	@Test
	public void allmanStyleLabelUsesPrecedingLine() {
		IDocument document= document("void method()\n{\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
	}

	@Test
	public void allmanStyleLabelSkipsBlankLines() {
		IDocument document= document("void method()\n\n   \n{\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
	}

	@Test
	public void blockWithoutAnyPrecedingTextIsNotAnnotated() {
		IDocument document= document("{\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue(blockEnds(document).isEmpty());
	}

	@Test
	public void labelCollapsesWhitespaceAndStripsBraces() {
		IDocument document= document("\tvoid\tmethod(int  a)   {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method(int a)", ends.get(0).label()); //$NON-NLS-1$
	}

	@Test
	public void longLabelIsTruncated() {
		String name= "m".repeat(200); //$NON-NLS-1$
		IDocument document= document("void " + name + "() {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		String label= ends.get(0).label();
		assertEquals("// ".length() + 100 + 1, label.length()); //$NON-NLS-1$
		assertTrue(label.endsWith("\u2026"), label); //$NON-NLS-1$
	}

	@Test
	public void unbalancedClosingBraceIsIgnored() {
		IDocument document= document("}\nvoid method() {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
	}

	@Test
	public void unbalancedOpeningBraceIsIgnored() {
		IDocument document= document("void broken() {\n" + body(25)); //$NON-NLS-1$

		assertTrue(blockEnds(document).isEmpty());
	}

	@Test
	public void carriageReturnLineDelimitersAreSupported() {
		IDocument document= document("void method() {\r\n" + body(25, "\r\n") + "}\r\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(26, ends.get(0).endLine());
	}

	@Test
	public void emptyDocumentYieldsNoMinings() {
		assertTrue(blockEnds(document("")).isEmpty()); //$NON-NLS-1$
	}

	@Test
	public void nullDocumentYieldsNoMinings() {
		assertTrue(blockEnds(null, MIN_LINES).isEmpty());
	}

	@Test
	public void canceledMonitorStopsTheScan() {
		IDocument document= document("void method() {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$
		NullProgressMonitor monitor= new NullProgressMonitor();
		monitor.setCanceled(true);

		assertTrue(BlockEndCodeMiningProvider.computeBlockEnds(document, MIN_LINES, monitor).isEmpty());
	}

	@Test
	public void bracesInLineCommentsAreIgnored() {
		IDocument document= partitionedDocument("void method() {\n\t// } not a block end {\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(27, ends.get(0).endLine());
	}

	@Test
	public void bracesInBlockCommentsAreIgnored() {
		IDocument document= partitionedDocument("void method() {\n\t/* }\n\t   { */\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(28, ends.get(0).endLine());
	}

	@Test
	public void bracesInStringLiteralsAreIgnored() {
		IDocument document= partitionedDocument("void method() {\n\tString s= \"{}\";\n" + body(25) + "}\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(27, ends.get(0).endLine());
	}

	@Test
	public void partitionedDocumentStillMatchesCodeBraces() {
		IDocument document= partitionedDocument("void method() { // opening\n" + body(25) + "} // closing\n"); //$NON-NLS-1$ //$NON-NLS-2$

		List<BlockEnd> ends= blockEnds(document);

		assertEquals(1, ends.size());
		assertEquals("// void method()", ends.get(0).label()); //$NON-NLS-1$
		assertEquals(26, ends.get(0).endLine());
	}
}
