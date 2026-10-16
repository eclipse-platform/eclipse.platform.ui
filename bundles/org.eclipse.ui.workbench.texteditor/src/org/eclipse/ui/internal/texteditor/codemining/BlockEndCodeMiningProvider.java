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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
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
 * skipped where a partitioning marks code as the default content type, and any other document is
 * treated as code in full, so a brace in a character literal can pair up wrongly there. A closing
 * brace already followed by a comment is left alone.
 * </p>
 * <p>
 * Scans use a document snapshot and are canceled immediately on edits; replacement scans are
 * deferred until typing pauses.
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

	/** How many lines a wrapped opening line, such as a long method signature, may span. */
	private static final int MAX_WRAPPED_LINES= 5;

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
			cancelRequest();
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			scheduleUpdate();
		}
	};

	private IDocument trackedDocument;

	private IProgressMonitor currentMonitor;

	private volatile boolean disposed;

	@Override
	public synchronized CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		cancelRequest();
		loadStore();
		IDocument document= viewer.getDocument();
		// Nothing in the platform recomputes minings on a document change, JDT brings its own
		// reconciler for that, so follow the document here.
		trackDocument(enabled ? document : null);
		if (!enabled || document == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		IProgressMonitor requestMonitor= monitor != null ? monitor : new NullProgressMonitor();
		currentMonitor= requestMonitor;
		int blockSize= minLines;
		ITypedRegion[] codeRegions;
		try {
			codeRegions= computeCodeRegions(document);
		} catch (BadLocationException e) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		// The scan is slow enough on large documents to be felt on the UI thread, and the live
		// document and its partitioner are not thread safe, so scan a copy in the background.
		String text= document.get();
		return CompletableFuture.supplyAsync(() -> {
			List<ICodeMining> minings= collectMinings(new Document(text), codeRegions, blockSize, requestMonitor);
			if (requestMonitor.isCanceled()) {
				throw new CancellationException();
			}
			return minings;
		});
	}

	private List<ICodeMining> collectMinings(IDocument document, ITypedRegion[] codeRegions, int blockSize,
			IProgressMonitor monitor) {
		List<ICodeMining> minings= new ArrayList<>();
		for (BlockEnd blockEnd : computeBlockEnds(document, codeRegions, blockSize, monitor)) {
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
		if (document == null || document.getLength() == 0) {
			return new ArrayList<>();
		}
		try {
			return computeBlockEnds(document, computeCodeRegions(document), minLines, monitor);
		} catch (BadLocationException e) {
			return new ArrayList<>();
		}
	}

	private static List<BlockEnd> computeBlockEnds(IDocument document, ITypedRegion[] codeRegions, int minLines,
			IProgressMonitor monitor) {
		List<BlockEnd> result= new ArrayList<>();
		Deque<Integer> openBraces= new ArrayDeque<>();
		int sinceLastCheck= 0;
		try {
			for (ITypedRegion region : codeRegions) {
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
						collectIfSignificant(document, codeRegions, openBraces.pop(), regionOffset + i, minLines, result);
					}
				}
			}
		} catch (BadLocationException e) {
			// Return what has been collected so far.
		}
		return result;
	}

	/**
	 * Returns the first partitioning that marks code as {@link IDocument#DEFAULT_CONTENT_TYPE}, or
	 * the whole document as code.
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
	 * has no partitioner or does not mark any region, including empty boundary regions, as code.
	 */
	private static ITypedRegion[] computePartitioning(IDocumentExtension3 extension, IDocument document,
			String partitioning) throws BadLocationException {
		if (extension.getDocumentPartitioner(partitioning) == null) {
			return null;
		}
		ITypedRegion[] partitions;
		try {
			// A comment-only document still has empty default partitions at its boundaries.
			partitions= extension.computePartitioning(partitioning, 0, document.getLength(), true);
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

	private static void collectIfSignificant(IDocument document, ITypedRegion[] codeRegions, int openOffset,
			int closeOffset, int minLines, List<BlockEnd> result) throws BadLocationException {
		int openLine= document.getLineOfOffset(openOffset);
		int closeLine= document.getLineOfOffset(closeOffset);
		int lineCount= closeLine - openLine + 1;
		// A single line block never needs a marker, both braces are visible at once.
		if (lineCount < Math.max(2, minLines)) {
			return;
		}
		IRegion closeLineRegion= document.getLineInformation(closeLine);
		int closeLineEnd= closeLineRegion.getOffset() + closeLineRegion.getLength();
		if (hasBlockOrCommentAfter(document, codeRegions, closeOffset + 1, closeLineEnd)) {
			return;
		}
		// One mining per line, for "}}" the innermost block wins.
		if (!result.isEmpty() && result.get(result.size() - 1).endLine() == closeLine) {
			return;
		}
		String label= computeLabel(document, codeRegions, openOffset, openLine);
		if (!label.isEmpty()) {
			result.add(new BlockEnd(closeLine, LABEL_PREFIX + label));
		}
	}

	private static String computeLabel(IDocument document, ITypedRegion[] codeRegions, int openBraceOffset,
			int openLine) throws BadLocationException {
		int lineOffset= document.getLineOffset(openLine);
		if (!normalize(document.get(lineOffset, openBraceOffset - lineOffset)).isEmpty()) {
			return wrappedLabel(document, codeRegions, openLine, openBraceOffset);
		}
		// The opening brace is the first token on its line (e.g. Allman style): use
		// the closest preceding line that holds code instead.
		for (int line= openLine - 1; line >= 0; line--) {
			IRegion region= document.getLineInformation(line);
			String raw= document.get(region.getOffset(), region.getLength());
			String text= raw.trim();
			if (text.isEmpty() || isCommentLine(text)
					|| !isCode(codeRegions, region.getOffset() + raw.indexOf(text.charAt(0)))) {
				continue;
			}
			String code= codeText(document, codeRegions, line);
			// A statement or another block ends there, so the brace opens a bare block.
			if (code.isEmpty() || endsStatementOrBlock(code.trim())) {
				return ""; //$NON-NLS-1$
			}
			return wrappedLabel(document, codeRegions, line, region.getOffset() + code.length());
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the text from the start of the given line to the end offset, extended upwards over
	 * wrapped lines until its parentheses balance, as in a long method signature.
	 */
	private static String wrappedLabel(IDocument document, ITypedRegion[] codeRegions, int line, int endOffset)
			throws BadLocationException {
		int start= document.getLineOffset(line);
		String text= document.get(start, endOffset - start);
		for (int first= line - 1; first >= 0 && line - first <= MAX_WRAPPED_LINES
				&& parenthesisDepth(codeRegions, text, start) < 0; first--) {
			// A line ending a statement or block is not part of a wrapped signature, as in "})).then(() -> {".
			if (endsStatementOrBlock(codeText(document, codeRegions, first).trim())) {
				break;
			}
			start= document.getLineOffset(first);
			text= document.get(start, endOffset - start);
		}
		return normalize(text);
	}

	/** Returns the line up to its last code character, so a trailing comment does not hide how it ends. */
	private static String codeText(IDocument document, ITypedRegion[] codeRegions, int line) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int offset= region.getOffset();
		String text= document.get(offset, region.getLength());
		int end= 0;
		for (int i= 0; i < text.length(); i++) {
			ITypedRegion partition= regionAt(codeRegions, offset + i);
			boolean commentStart= partition == null || IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
					|| partition.getOffset() == offset + i;
			if (commentStart && text.startsWith("//", i)) { //$NON-NLS-1$
				break;
			}
			if (commentStart && text.startsWith("/*", i)) { //$NON-NLS-1$
				int close= text.indexOf("*/", i + 2); //$NON-NLS-1$
				if (close < 0) {
					break;
				}
				i= close + 1;
			} else if (!Character.isWhitespace(text.charAt(i))) {
				end= i + 1;
			}
		}
		return text.substring(0, end);
	}

	private static boolean endsStatementOrBlock(String text) {
		return text.endsWith(";") || text.endsWith("{") || text.endsWith("}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static int parenthesisDepth(ITypedRegion[] codeRegions, String text, int offset) {
		int depth= 0;
		for (int i= 0; i < text.length(); i++) {
			char c= text.charAt(i);
			if ((c == '(' || c == ')') && isCode(codeRegions, offset + i)) {
				depth+= c == '(' ? 1 : -1;
			}
		}
		return depth;
	}

	/** Recognizes C-style comment lines where no partitioning marks them, as in TextMate documents. */
	private static boolean isCommentLine(String text) {
		return text.startsWith("//") || text.startsWith("/*") || text.startsWith("*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Tells whether the rest of the closing line opens a block, so the echo would read as its
	 * label as in "} else {", or holds a comment that already says what the echo would.
	 */
	private static boolean hasBlockOrCommentAfter(IDocument document, ITypedRegion[] codeRegions, int from, int to)
			throws BadLocationException {
		String rest= document.get(from, to - from);
		for (int i= 0; i < rest.length(); i++) {
			ITypedRegion region= regionAt(codeRegions, from + i);
			boolean code= region == null || IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType());
			if (code && rest.charAt(i) == '{') {
				return true;
			}
			// A comment partition starts with its delimiter, a string partition with its quote.
			if ((code || region.getOffset() == from + i) && (rest.startsWith("//", i) || rest.startsWith("/*", i))) { //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
		}
		return false;
	}

	private static boolean isCode(ITypedRegion[] codeRegions, int offset) {
		ITypedRegion region= regionAt(codeRegions, offset);
		return region == null || IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType());
	}

	/** Finds the region holding the offset, the regions are sorted and do not overlap. */
	private static ITypedRegion regionAt(ITypedRegion[] regions, int offset) {
		int low= 0;
		int high= regions.length - 1;
		while (low <= high) {
			int middle= (low + high) >>> 1;
			ITypedRegion region= regions[middle];
			if (offset < region.getOffset()) {
				high= middle - 1;
			} else if (offset >= region.getOffset() + region.getLength()) {
				low= middle + 1;
			} else {
				return region;
			}
		}
		return null;
	}

	private static String normalize(String text) {
		String collapsed= WHITESPACE.matcher(text).replaceAll(" ").trim(); //$NON-NLS-1$
		// A block opened on a closing line, as in "} else {", is labelled without the brace.
		while (collapsed.startsWith("}")) { //$NON-NLS-1$
			collapsed= collapsed.substring(1).trim();
		}
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
			cancelRequest();
			readPreferences(store);
			updateCodeMinings();
		}
	}

	private synchronized void cancelRequest() {
		if (currentMonitor != null) {
			// Also cancel the combined render if this provider finished before another provider.
			currentMonitor.setCanceled(true);
			currentMonitor= null;
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
		cancelRequest();
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
	 * Reads the preferences and starts listening for their changes, once.
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
