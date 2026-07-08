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
package org.eclipse.ui.internal.texteditor.codemining;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.source.ISourceViewerExtension5;

import org.eclipse.ui.internal.texteditor.BlockEndCodeMiningPreferenceConstants;

/**
 * A language-agnostic code mining provider that echoes the opening line of a
 * curly-brace block at its closing brace, helping to identify which block a
 * closing brace belongs to.
 * <p>
 * To avoid cluttering the editor, a block is only annotated when it spans at
 * least {@link BlockEndCodeMiningPreferenceConstants#BLOCK_END_CODE_MINING_MIN_LINES}
 * lines. The feature is disabled by default and controlled through the
 * preferences.
 * </p>
 * <p>
 * Braces are matched purely structurally. When the document provides a
 * partitioner, braces inside non-default partitions (typically comments and
 * strings) are ignored; otherwise the whole document is treated as code.
 * </p>
 */
public class BlockEndCodeMiningProvider extends AbstractCodeMiningProvider implements IPropertyChangeListener {

	/**
	 * Immutable description of a block whose closing brace should be annotated.
	 *
	 * @param endLine the zero-based document line of the closing brace
	 * @param label the label rendered at the closing brace, echoing the block's opening line
	 */
	public record BlockEnd(int endLine, String label) {
	}

	private static final int MAX_LABEL_LENGTH= 100;

	/** Marks the echoed text as an annotation, following the usual hand-written closing brace comment. */
	private static final String LABEL_PREFIX= "// "; //$NON-NLS-1$

	private IPreferenceStore store;
	private boolean enabled;
	private int minLines;

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		if (store == null) {
			loadStore();
		}
		IDocument document= viewer.getDocument();
		if (!enabled || document == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		List<ICodeMining> minings= new ArrayList<>();
		for (BlockEnd blockEnd : computeBlockEnds(document, minLines, monitor)) {
			if (monitor != null && monitor.isCanceled()) {
				break;
			}
			try {
				minings.add(new BlockEndCodeMining(document, blockEnd.endLine(), blockEnd.label(), this));
			} catch (BadLocationException e) {
				// Skip minings that can no longer be positioned.
			}
		}
		return CompletableFuture.completedFuture(minings);
	}

	/**
	 * Computes the blocks that should be annotated in the given document.
	 *
	 * @param document the document to scan
	 * @param minLines the minimum number of lines a block must span to be annotated
	 * @param monitor the progress monitor to check for cancellation, may be <code>null</code>
	 * @return the closing braces to annotate together with the echoed opening line
	 */
	public static List<BlockEnd> computeBlockEnds(IDocument document, int minLines, IProgressMonitor monitor) {
		List<BlockEnd> result= new ArrayList<>();
		if (document == null || document.getLength() == 0) {
			return result;
		}
		Deque<Integer> openBraces= new ArrayDeque<>();
		try {
			for (ITypedRegion region : computeCodeRegions(document)) {
				if (monitor != null && monitor.isCanceled()) {
					return result;
				}
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType())) {
					continue;
				}
				int regionOffset= region.getOffset();
				String code= document.get(regionOffset, region.getLength());
				for (int i= 0; i < code.length(); i++) {
					char c= code.charAt(i);
					if (c == '{') {
						openBraces.push(regionOffset + i);
					} else if (c == '}' && !openBraces.isEmpty()) {
						collectIfSignificant(document, openBraces.pop(), regionOffset + i, minLines, result);
					}
				}
			}
		} catch (BadLocationException e) {
			// Return what has been collected so far.
		}
		return result;
	}

	private static ITypedRegion[] computeCodeRegions(IDocument document) throws BadLocationException {
		if (document instanceof IDocumentExtension3 extension) {
			ITypedRegion[] partitions= computePartitioning(extension, document, IDocumentExtension3.DEFAULT_PARTITIONING);
			if (partitions != null) {
				return partitions;
			}
			// Editors like JDT register their partitioner under their own partitioning name.
			for (String partitioning : extension.getPartitionings()) {
				partitions= computePartitioning(extension, document, partitioning);
				if (partitions != null) {
					return partitions;
				}
			}
		}
		// No partitioner installed at all; treat the whole document as code.
		return new ITypedRegion[] { new TypedRegion(0, document.getLength(), IDocument.DEFAULT_CONTENT_TYPE) };
	}

	private static ITypedRegion[] computePartitioning(IDocumentExtension3 extension, IDocument document,
			String partitioning) throws BadLocationException {
		if (extension.getDocumentPartitioner(partitioning) == null) {
			return null;
		}
		try {
			return extension.computePartitioning(partitioning, 0, document.getLength(), false);
		} catch (BadPartitioningException e) {
			return null;
		}
	}

	private static void collectIfSignificant(IDocument document, int openOffset, int closeOffset, int minLines,
			List<BlockEnd> result) throws BadLocationException {
		int openLine= document.getLineOfOffset(openOffset);
		int closeLine= document.getLineOfOffset(closeOffset);
		int lineCount= closeLine - openLine + 1;
		// A single line block never needs a marker, both braces are visible at once.
		if (lineCount < Math.max(2, minLines)) {
			return;
		}
		String label= computeLabel(document, openOffset, openLine);
		if (!label.isEmpty()) {
			result.add(new BlockEnd(closeLine, LABEL_PREFIX + label));
		}
	}

	private static String computeLabel(IDocument document, int openBraceOffset, int openLine) throws BadLocationException {
		int lineOffset= document.getLineOffset(openLine);
		String label= normalize(document.get(lineOffset, openBraceOffset - lineOffset));
		if (!label.isEmpty()) {
			return label;
		}
		// The opening brace is the first token on its line (e.g. Allman style): use
		// the closest preceding non-blank line instead.
		for (int line= openLine - 1; line >= 0; line--) {
			IRegion region= document.getLineInformation(line);
			String text= normalize(document.get(region.getOffset(), region.getLength()));
			if (!text.isEmpty()) {
				return text;
			}
		}
		return ""; //$NON-NLS-1$
	}

	private static String normalize(String text) {
		String collapsed= text.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
		while (collapsed.endsWith("{")) { //$NON-NLS-1$
			collapsed= collapsed.substring(0, collapsed.length() - 1).trim();
		}
		if (collapsed.length() > MAX_LABEL_LENGTH) {
			collapsed= collapsed.substring(0, MAX_LABEL_LENGTH) + "\u2026"; //$NON-NLS-1$
		}
		return collapsed;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (BlockEndCodeMiningPreferenceConstants.SHOW_BLOCK_END_CODE_MINING.equals(property)
				|| BlockEndCodeMiningPreferenceConstants.BLOCK_END_CODE_MINING_MIN_LINES.equals(property)) {
			readPreferences();
			updateCodeMinings();
		}
	}

	private void updateCodeMinings() {
		ITextViewer viewer= getAdapter(ITextViewer.class);
		if (viewer instanceof ISourceViewerExtension5 codeMiningExtension) {
			codeMiningExtension.updateCodeMinings();
		}
	}

	@Override
	public void dispose() {
		if (store != null) {
			store.removePropertyChangeListener(this);
			store= null;
		}
		super.dispose();
	}

	private void loadStore() {
		store= getAdapter(IPreferenceStore.class);
		readPreferences();
		if (store != null) {
			store.addPropertyChangeListener(this);
		}
	}

	private void readPreferences() {
		if (store == null) {
			enabled= false;
			minLines= BlockEndCodeMiningPreferenceConstants.DEFAULT_MIN_LINES;
			return;
		}
		enabled= store.getBoolean(BlockEndCodeMiningPreferenceConstants.SHOW_BLOCK_END_CODE_MINING);
		minLines= store.getInt(BlockEndCodeMiningPreferenceConstants.BLOCK_END_CODE_MINING_MIN_LINES);
	}
}
