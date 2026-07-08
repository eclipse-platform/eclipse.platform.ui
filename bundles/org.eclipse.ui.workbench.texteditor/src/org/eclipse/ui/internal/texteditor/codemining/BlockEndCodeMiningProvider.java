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
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
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
 * Echoes the opening line of a curly-brace block at its closing brace.
 * <p>
 * Braces are matched structurally, without a parser: braces in comments and string literals are
 * skipped where the document has a partitioner, and a document without one is treated as code in
 * full, so a brace in a character literal can pair up wrongly there.
 * </p>
 */
public class BlockEndCodeMiningProvider extends AbstractCodeMiningProvider implements IPropertyChangeListener {

	/**
	 * A block whose closing brace should be annotated.
	 *
	 * @param endLine the zero-based line of the closing brace
	 * @param label the text to render there
	 */
	public record BlockEnd(int endLine, String label) {
	}

	private static final int MAX_LABEL_LENGTH= 100;

	/** Reads like the closing brace comment people write by hand, the mining is drawn right after the brace. */
	private static final String LABEL_PREFIX= " // "; //$NON-NLS-1$

	private static final Pattern WHITESPACE= Pattern.compile("\\s+"); //$NON-NLS-1$

	/** How many characters to scan between two cancellation checks. */
	private static final int CANCELLATION_CHECK_INTERVAL= 8192;

	/** How long to wait after a document change before recomputing, in milliseconds. */
	private static final int UPDATE_DELAY= 500;

	private volatile IPreferenceStore store;

	private volatile boolean enabled;

	private volatile int minLines= BlockEndCodeMiningPreferenceConstants.DEFAULT_MIN_LINES;

	private final Runnable updater= this::updateCodeMinings;

	private final IDocumentListener documentListener= new IDocumentListener() {
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			// Nothing to do.
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			scheduleUpdate();
		}
	};

	private IDocument trackedDocument;

	private volatile boolean disposed;

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		loadStore();
		IDocument document= viewer.getDocument();
		// Nothing in the platform recomputes minings on a document change, JDT brings its own
		// reconciler for that, so follow the document here.
		trackDocument(enabled ? document : null);
		if (!enabled || document == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		int blockSize= minLines;
		// Scanning a large document takes long enough to be felt, and this is called on the UI
		// thread; the mining infrastructure waits for the future either way.
		return CompletableFuture.supplyAsync(() -> collectMinings(document, blockSize, monitor));
	}

	private List<ICodeMining> collectMinings(IDocument document, int blockSize, IProgressMonitor monitor) {
		List<ICodeMining> minings= new ArrayList<>();
		for (BlockEnd blockEnd : computeBlockEnds(document, blockSize, monitor)) {
			if (monitor != null && monitor.isCanceled()) {
				break;
			}
			try {
				minings.add(new BlockEndCodeMining(document, blockEnd.endLine(), blockEnd.label(), this));
			} catch (BadLocationException e) {
				// Skip minings that can no longer be positioned.
			}
		}
		return minings;
	}

	/**
	 * Computes the blocks that should be annotated in the given document.
	 *
	 * @param document the document to scan, may be <code>null</code>
	 * @param minLines the minimum number of lines a block must span
	 * @param monitor the monitor to check for cancellation, may be <code>null</code>
	 * @return the closing braces to annotate, innermost block first
	 */
	public static List<BlockEnd> computeBlockEnds(IDocument document, int minLines, IProgressMonitor monitor) {
		List<BlockEnd> result= new ArrayList<>();
		if (document == null || document.getLength() == 0) {
			return result;
		}
		Deque<Integer> openBraces= new ArrayDeque<>();
		int sinceLastCheck= 0;
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
					if (++sinceLastCheck >= CANCELLATION_CHECK_INTERVAL) {
						sinceLastCheck= 0;
						if (monitor != null && monitor.isCanceled()) {
							return result;
						}
					}
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

	/**
	 * Returns the partitions to scan for braces, so that comments and string literals can be left
	 * out. Only a partitioning that marks code as {@link IDocument#DEFAULT_CONTENT_TYPE} can say
	 * where the code is; TextMate for instance partitions every text file into content types of its
	 * own, and reading those as "no code here" would leave the document unannotated. A document
	 * with no such partitioning is treated as code in full.
	 */
	private static ITypedRegion[] computeCodeRegions(IDocument document) throws BadLocationException {
		ITypedRegion[] wholeDocument= { new TypedRegion(0, document.getLength(), IDocument.DEFAULT_CONTENT_TYPE) };
		if (!(document instanceof IDocumentExtension3 extension)) {
			return wholeDocument;
		}
		ITypedRegion[] partitions= computePartitioning(extension, document, IDocumentExtension3.DEFAULT_PARTITIONING);
		if (partitions == null) {
			// Editors like JDT register their partitioner under their own partitioning name only.
			// Sorting keeps the choice reproducible, getPartitionings() is unordered.
			String[] partitionings= extension.getPartitionings().clone();
			Arrays.sort(partitionings);
			for (String partitioning : partitionings) {
				if (IDocumentExtension3.DEFAULT_PARTITIONING.equals(partitioning)) {
					continue;
				}
				partitions= computePartitioning(extension, document, partitioning);
				if (partitions != null) {
					break;
				}
			}
		}
		return partitions != null ? partitions : wholeDocument;
	}

	/**
	 * Returns the partitioning of the document, or <code>null</code> when the given partitioning
	 * has no partitioner or does not mark any region as code.
	 */
	private static ITypedRegion[] computePartitioning(IDocumentExtension3 extension, IDocument document,
			String partitioning) throws BadLocationException {
		if (extension.getDocumentPartitioner(partitioning) == null) {
			return null;
		}
		ITypedRegion[] partitions;
		try {
			partitions= extension.computePartitioning(partitioning, 0, document.getLength(), false);
		} catch (BadPartitioningException e) {
			return null;
		}
		for (ITypedRegion partition : partitions) {
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
				return partitions;
			}
		}
		return null;
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
		String collapsed= WHITESPACE.matcher(text).replaceAll(" ").trim(); //$NON-NLS-1$
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
			readPreferences(store);
			updateCodeMinings();
		}
	}

	private void updateCodeMinings() {
		ITextViewer viewer= getAdapter(ITextViewer.class);
		if (viewer instanceof ISourceViewerExtension5 codeMiningExtension) {
			codeMiningExtension.updateCodeMinings();
		}
	}

	private synchronized void trackDocument(IDocument document) {
		if (trackedDocument == document) {
			return;
		}
		if (trackedDocument != null) {
			trackedDocument.removeDocumentListener(documentListener);
		}
		trackedDocument= document;
		if (document != null) {
			document.addDocumentListener(documentListener);
		}
	}

	/** Recomputes once the typing has paused, timerExec collapses the pending updates into one. */
	private void scheduleUpdate() {
		Display display= displayOfViewer();
		if (display != null) {
			display.asyncExec(() -> {
				if (!disposed) {
					display.timerExec(UPDATE_DELAY, updater);
				}
			});
		}
	}

	private Display displayOfViewer() {
		ITextViewer viewer= getAdapter(ITextViewer.class);
		StyledText widget= viewer != null ? viewer.getTextWidget() : null;
		return widget != null && !widget.isDisposed() ? widget.getDisplay() : null;
	}

	@Override
	public synchronized void dispose() {
		disposed= true;
		Display display= displayOfViewer();
		if (display != null) {
			display.timerExec(-1, updater);
		}
		trackDocument(null);
		if (store != null) {
			store.removePropertyChangeListener(this);
			store= null;
		}
		super.dispose();
	}

	/**
	 * Reads the preferences and starts listening for their changes, once. Code minings are computed
	 * on a background thread, so this can be reached concurrently.
	 */
	private synchronized void loadStore() {
		if (store != null) {
			return;
		}
		IPreferenceStore preferenceStore= getAdapter(IPreferenceStore.class);
		readPreferences(preferenceStore);
		if (preferenceStore != null) {
			preferenceStore.addPropertyChangeListener(this);
			store= preferenceStore;
		}
	}

	private void readPreferences(IPreferenceStore preferenceStore) {
		if (preferenceStore == null) {
			enabled= false;
			minLines= BlockEndCodeMiningPreferenceConstants.DEFAULT_MIN_LINES;
			return;
		}
		enabled= preferenceStore.getBoolean(BlockEndCodeMiningPreferenceConstants.SHOW_BLOCK_END_CODE_MINING);
		minLines= preferenceStore.getInt(BlockEndCodeMiningPreferenceConstants.BLOCK_END_CODE_MINING_MIN_LINES);
	}
}
