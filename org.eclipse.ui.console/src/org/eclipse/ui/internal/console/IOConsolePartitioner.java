/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Paul Pazderski  - Contributions for:
 *                          Bug 547064: use binary search for getPartition
 *                          Bug 548356: fixed user input handling
 *                          Bug 550618: getStyleRanges produced invalid overlapping styles
 *                          Bug 550621: Implementation of IConsoleDocumentPartitionerExtension
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.MultiStringMatcher;
import org.eclipse.jface.text.MultiStringMatcher.Match;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleDocumentPartitionerExtension;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Partitions an IOConsole's document
 *
 * @since 3.1
 */
public class IOConsolePartitioner
		implements IConsoleDocumentPartitioner, IConsoleDocumentPartitionerExtension, IDocumentPartitionerExtension {
	/**
	 * Enumeration used to distinct sources of document updates. (especially to
	 * distinct updates triggered by this partitioner from other document changes)
	 */
	private enum DocUpdateType {
		/**
		 * Default if reason for document change is not known. Document change is
		 * interpreted as user input.
		 */
		INPUT,
		/**
		 * Document update was triggered from this partitioner by appending content
		 * received from output streams.
		 */
		OUTPUT,
		/** Document update was triggered from this partitioner's {@link TrimJob}. */
		TRIM,
	}

	/**
	 * If true validate partitioning after changes and do other additional
	 * assertions. Useful for developing/debugging.
	 */
	private static final boolean ASSERT = false;

	/**
	 * Comparator to sort or search {@link IRegion}s by {@link IRegion#getOffset()}.
	 */
	private static final Comparator<IRegion> CMP_REGION_BY_OFFSET = Comparator.comparing(IRegion::getOffset);

	/** The connected {@link IDocument} this partitioner manages. */
	private IDocument document;
	/**
	 * List of all partitions. Must always be sorted ascending by
	 * {@link IRegion#getOffset()} and not contain <code>null</code> or 0-length
	 * elements. (see also {@link #checkPartitions()})
	 */
	private final ArrayList<IOConsolePartition> partitions = new ArrayList<>();
	/** Blocks of data that have not yet been appended to the document. */
	private final ArrayList<PendingPartition> pendingPartitions = new ArrayList<>();
	/** Total length of pending partitions content. */
	private int pendingSize;
	/** Job that appends pending partitions to the document. */
	private final QueueProcessingJob queueJob = new QueueProcessingJob();
	/** Job that trims console content if it exceeds {@link #highWaterMark}. */
	private final TrimJob trimJob = new TrimJob();
	/**
	 * Reason for document update. Set before changing document inside this
	 * partitioner to prevent that change is interpreted as user input.
	 * <p>
	 * Automatically reset to {@link DocUpdateType#INPUT} after every document
	 * change.
	 * </p>
	 */
	private DocUpdateType updateType = DocUpdateType.INPUT;
	private IRegion changedRegion;
	/**
	 * A list of partitions containing input from the console, that have not been
	 * appended to the input stream yet. No guarantees on element order.
	 */
	private ArrayList<IOConsolePartition> inputPartitions;
	/**
	 * A matcher to search for legal line delimiters in new input. Never
	 * <code>null</code> but match nothing if no document connected.
	 */
	private MultiStringMatcher legalLineDelimiterMatcher;
	/**
	 * The high mark for console content trimming. If console content exceeds this
	 * length trimming is scheduled. Trimming is disabled if value is negative.
	 */
	private int highWaterMark = -1;
	private int lowWaterMark = -1;

	/** The partitioned {@link IOConsole}. */
	private IOConsole console;

	/** Set after console signaled that all streams are closed. */
	private volatile boolean streamsClosed;

	/**
	 * Create new partitioner for an {@link IOConsole}.
	 * <p>
	 * The partitioner must be explicit {@link #connect(IDocument) connected} with
	 * the consoles {@link IDocument}.
	 * </p>
	 *
	 * @param console the partitioned console. Not <code>null</code>.
	 */
	public IOConsolePartitioner(IOConsole console) {
		this.console = Objects.requireNonNull(console);
		queueJob.setRule(console.getSchedulingRule());
		trimJob.setRule(console.getSchedulingRule());
	}

	/**
	 * Get partitioned document or <code>null</code> if none connected.
	 *
	 * @return partitioned document
	 */
	public IDocument getDocument() {
		return document;
	}

	@Override
	public void connect(IDocument doc) {
		if (doc == document) {
			return;
		}
		disconnect();
		if (doc != null) {
			synchronized (partitions) {
				inputPartitions = new ArrayList<>();
				document = doc;
				legalLineDelimiterMatcher = MultiStringMatcher.create(document.getLegalLineDelimiters());
			}
		}
	}

	@Override
	public void disconnect() {
		synchronized (pendingPartitions) {
			pendingPartitions.clear();
			pendingSize = 0;
			pendingPartitions.notifyAll();
		}
		synchronized (partitions) {
			trimJob.cancel();
			queueJob.cancel();
			legalLineDelimiterMatcher = null;
			document = null;
			inputPartitions = null;
			partitions.clear();
		}
	}

	/**
	 * Get high water mark.
	 *
	 * @return the trim if exceeded mark
	 * @see IOConsole#getHighWaterMark()
	 */
	public int getHighWaterMark() {
		return highWaterMark;
	}

	/**
	 * Get low water mark.
	 *
	 * @return the trim to this length mark
	 * @see IOConsole#getLowWaterMark()
	 */
	public int getLowWaterMark() {
		return lowWaterMark;
	}

	/**
	 * Set low and high water marks.
	 *
	 * @param low  the trim to this length mark
	 * @param high the trim if exceeded mark
	 * @see IOConsole#setWaterMarks(int, int)
	 */
	public void setWaterMarks(int low, int high) {
		lowWaterMark = low;
		highWaterMark = high;
		ConsolePlugin.getStandardDisplay().asyncExec(this::checkBufferSize);
	}

	/**
	 * Notification from the console that all of its streams have been closed.
	 */
	public void streamsClosed() {
		if (streamsClosed) {
			log(IStatus.ERROR, "Streams are already closed."); //$NON-NLS-1$
			return;
		}
		streamsClosed = true;
		checkFinished();
	}

	/**
	 * Check if partitioner is finished and does not expect any new data appended to
	 * document.
	 */
	private void checkFinished() {
		if (streamsClosed) {
			// do not expect new data since all streams are closed
			// check if pending data is queued
			final boolean morePending;
			synchronized (pendingPartitions) {
				morePending = !pendingPartitions.isEmpty();
			}
			if (morePending) {
				queueJob.schedule();
			} else {
				console.partitionerFinished();
			}
		}
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	@Override
	public boolean documentChanged(DocumentEvent event) {
		return documentChanged2(event) != null;
	}

	@Override
	public String[] getLegalContentTypes() {
		return new String[] { IOConsolePartition.OUTPUT_PARTITION_TYPE, IOConsolePartition.INPUT_PARTITION_TYPE };
	}

	@Override
	public String getContentType(int offset) {
		return getPartition(offset).getType();
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) {
		return computeIOPartitioning(offset, length);
	}

	/**
	 * Same as {@link #computePartitioning(int, int)} but with more specific return
	 * type.
	 *
	 * @param offset the offset of the range of interest
	 * @param length the length of the range of interest
	 * @return the partitioning of the requested range (never <code>null</code>)
	 */
	private IOConsolePartition[] computeIOPartitioning(int offset, int length) {
		return computePartitioning(offset, length, true, true);
	}

	/**
	 * Get partitioning for a given range with possibility to filter partitions by
	 * their read-only property.
	 *
	 * @param offset          the offset of the range of interest
	 * @param length          the length of the range of interest
	 * @param includeWritable if false writable partitions are skipped
	 * @param includeReadOnly if false read-only partitions are skipped
	 * @return the partitioning of the requested range (never <code>null</code>)
	 */
	private IOConsolePartition[] computePartitioning(int offset, int length, boolean includeWritable,
			boolean includeReadOnly) {
		final List<IOConsolePartition> result = new ArrayList<>();
		synchronized (partitions) {
			int index = findPartitionCandidate(offset);
			if (index < 0) { // requested range starts before any known partition offset
				index = 0; // so we start collecting at first known partition
			}

			final int end = offset + length;
			for (; index < partitions.size(); index++) {
				final IOConsolePartition partition = partitions.get(index);
				if (partition.getOffset() >= end) {
					break;
				}
				if ((includeWritable && !partition.isReadOnly()) || (includeReadOnly && partition.isReadOnly())) {
					result.add(partition);
				}
			}
		}
		return result.toArray(new IOConsolePartition[0]);
	}

	@Override
	public ITypedRegion getPartition(int offset) {
		final ITypedRegion partition = getIOPartition(offset);
		return partition != null ? partition : new TypedRegion(offset, 0, IOConsolePartition.INPUT_PARTITION_TYPE);
	}

	/**
	 * Like {@link #getPartition(int)} but returns <code>null</code> for
	 * unpartitioned or invalid offsets.
	 *
	 * @param offset the offset for which to determine the partition
	 * @return the partition containing this offset or <code>null</code> if offset
	 *         is not partitioned
	 */
	private IOConsolePartition getIOPartition(int offset) {
		synchronized (partitions) {
			final int index = findPartitionCandidate(offset);
			if (index >= 0) {
				final IOConsolePartition partition = partitions.get(index);
				if (partition.getOffset() + partition.getLength() > offset) {
					return partition;
				}
			}
			return null;
		}
	}

	/**
	 * Search {@link #partitions} for the partition which is most likely containing
	 * the requested offset.
	 * <p>
	 * This (index + 1) can be used to insert a new partition with this offset. The
	 * resulting {@link #partitions} list is guaranteed to still be sorted. (as long
	 * as you do proper synchronization and consider concurrency problems)
	 * </p>
	 *
	 * @param offset the offset for which to determine the partition candidate
	 * @return index of partition element with partition offset closest to requested
	 *         offset or <code>-1</code> if requested offset is lower than offset of
	 *         any known partition
	 */
	private int findPartitionCandidate(int offset) {
		final Region target = new Region(offset, 0);
		final int index = Collections.binarySearch(partitions, target, CMP_REGION_BY_OFFSET);
		if (index >= 0) {
			// found partition whose offset equals the requested offset
			return index;
		}
		// no exact offset match. Adjust index to point at partition which is closest to
		// requested offset but whose offset is still lower than requested offset.
		// Results in -1 if all known offsets are greater.
		return (-index) - 2;
	}

	/**
	 * Enforces the buffer size.
	 * <p>
	 * When the number of lines in the document exceeds the high water mark, the
	 * beginning of the document is trimmed until the number of lines equals the low
	 * water mark.
	 * </p>
	 */
	private void checkBufferSize() {
		if (document != null && highWaterMark > 0) {
			int length = document.getLength();
			if (length > highWaterMark) {
				if (trimJob.getState() == Job.NONE) { // if the job isn't already running
					trimJob.setOffset(length - lowWaterMark);
					trimJob.schedule();
				}
			}
		}
	}

	/**
	 * Clears the console content.
	 */
	public void clearBuffer() {
		trimJob.setOffset(-1);
		trimJob.schedule();
	}

	@Override
	public IRegion documentChanged2(DocumentEvent event) {
		try {
			if (document != event.getDocument()) {
				log(IStatus.WARNING, "IOConsolePartitioner is connected to wrong document."); //$NON-NLS-1$
				return null;
			}
			if (document.getLength() == 0) { // document cleared
				synchronized (partitions) {
					partitions.clear();
					inputPartitions.clear();
				}
				return new Region(0, 0);
			}

			synchronized (partitions) {
				switch (updateType) {
				case INPUT:
					return applyUserInput(event);

				// update and trim jobs are triggered by this partitioner and all partitioning
				// changes are applied separately
				case OUTPUT:
					return changedRegion;
				case TRIM:
					return null; // trim does not change partition types

				default:
					log(IStatus.ERROR, "Invalid enum value " + updateType); //$NON-NLS-1$
					return null;
				}
			}
		} finally {
			// always reset type since all change events not triggered by this partitioner
			// are interpreted as user input
			updateType = DocUpdateType.INPUT;
		}
	}

	/**
	 * Update partitioning due to document change. All document change events not
	 * triggered by this partitioner are considered user input and therefore
	 * partitioned as input.
	 * <p>
	 * This method does not care if the document event removed or replaced parts of
	 * read-only partitions. It assumes manipulating read-only partitions is valid
	 * or is blocked before this method is used.
	 * </p>
	 *
	 * @param event the event describing the document change
	 * @return the region of the document in which the partition type changed or
	 *         <code>null</code>
	 */
	// Required for a false 'resource not closed' warning on inputStream.
	// This input stream must not be closed by this method.
	@SuppressWarnings("resource")
	private IRegion applyUserInput(DocumentEvent event) {
		final int eventTextLength = event.getText() != null ? event.getText().length() : 0;
		final int offset = event.getOffset();
		final int amountDeleted = event.getLength();
		final IOConsoleInputStream inputStream = console.getInputStream(); // do not close this stream

		if (amountDeleted == 0 && eventTextLength == 0) {
			// event did not changed document
			return null;
		}

		final int eventPartitionIndex = findPartitionCandidate(offset);
		int lastPartitionWithValidOffset = eventPartitionIndex;

		if (amountDeleted > 0 && eventPartitionIndex >= 0) {
			// adjust length of all partitions affected by replace/remove event
			int toDelete = amountDeleted;
			for (int i = eventPartitionIndex; i < partitions.size() && toDelete > 0; i++) {
				final IOConsolePartition partition = partitions.get(i);
				final int removeLength = Math.min(partition.getLength(), toDelete);
				partition.setLength(partition.getLength() - removeLength);
				toDelete -= removeLength;
			}
			if (ASSERT) {
				Assert.isTrue(toDelete == 0, "Tried to delete outside partitioned range."); //$NON-NLS-1$
			}
			lastPartitionWithValidOffset--; // update one more since first affected partition may be empty now
		}

		if (eventTextLength > 0) {
			// find best partition for event text
			int inputPartitionIndex = eventPartitionIndex;
			IOConsolePartition inputPartition = getPartitionByIndex(inputPartitionIndex);
			if (inputPartition != null && inputPartition.isReadOnly() && offset == inputPartition.getOffset()) {
				// if we could not reuse partition at event offset we may append the partition
				// right before our event offset (e.g. if input is at end of document)
				inputPartitionIndex--;
				lastPartitionWithValidOffset--;
				inputPartition = getPartitionByIndex(inputPartitionIndex);
			}

			// process event text in parts split on line delimiters
			int textOffset = 0;
			while (textOffset < eventTextLength) {
				final Match nextNewline = legalLineDelimiterMatcher.indexOf(event.getText(), textOffset);
				final int newTextOffset = nextNewline != null ? nextNewline.getOffset() + nextNewline.getText().length()
						: eventTextLength;
				final int inputLength = newTextOffset - textOffset;

				if (inputPartition == null || inputPartition.isReadOnly()) {
					final int inputOffset = offset + textOffset;
					if (inputPartition != null
							&& inputOffset < inputPartition.getOffset() + inputPartition.getLength()) {
						// input is inside an existing read-only partition
						splitPartition(inputOffset);
					}
					inputPartition = new IOConsolePartition(inputOffset, inputStream);
					inputPartitionIndex++;
					partitions.add(inputPartitionIndex, inputPartition);
					inputPartitions.add(inputPartition);
					lastPartitionWithValidOffset++; // new input partitions get build with correct offsets
				}

				inputPartition.setLength(inputPartition.getLength() + inputLength);

				if (nextNewline != null) {
					inputPartitions.sort(CMP_REGION_BY_OFFSET);
					final StringBuilder inputLine = new StringBuilder();
					for (IOConsolePartition p : inputPartitions) {
						try {
							final String fragment = document.get(p.getOffset(), p.getLength());
							inputLine.append(fragment);
						} catch (BadLocationException e) {
							log(e);
						}
						p.setReadOnly();
					}
					inputPartitions.clear();
					if (ASSERT) {
						Assert.isTrue(inputLine.length() > 0);
					}
					if (inputStream != null) {
						inputStream.appendData(inputLine.toString());
					}
				}
				Assert.isTrue(newTextOffset > textOffset); // can prevent infinity loop
				textOffset = newTextOffset;
			}
		}

		// repair partition offsets
		int newOffset = 0;
		if (lastPartitionWithValidOffset >= 0) {
			// reduce number of partition to update by skipping still valid entries
			final IOConsolePartition partition = partitions.get(lastPartitionWithValidOffset);
			newOffset = partition.getOffset() + partition.getLength();
		}
		final Iterator<IOConsolePartition> it = partitions.listIterator(lastPartitionWithValidOffset + 1);
		while (it.hasNext()) {
			final IOConsolePartition partition = it.next();
			if (partition.getLength() <= 0) {
				if (ASSERT) {
					Assert.isTrue(partition.getLength() == 0);
				}
				it.remove();
				if (isInputPartition(partition)) {
					final boolean removed = inputPartitions.remove(partition);
					if (ASSERT) {
						Assert.isTrue(removed);
					}
				}
			} else {
				partition.setOffset(newOffset);
				newOffset += partition.getLength();
			}
		}

		if (ASSERT) {
			checkPartitions();
		}
		return new Region(0, document.getLength());
	}

	/**
	 * Split an existing partition at offset. The offset must not be the first or
	 * last offset of the existing partition because this leads to empty partitions
	 * not bearable by this partitioner.
	 * <p>
	 * New partition is added to {@link #partitions} (always) and
	 * {@link #inputPartitions} (if applicable).
	 * </p>
	 *
	 * @param offset the offset where the existing partition will end after split
	 *               and a new partition will start
	 * @return the newly created partition (i.e. the right side of the split)
	 */
	private IOConsolePartition splitPartition(int offset) {
		final int partitionIndex = findPartitionCandidate(offset);
		final IOConsolePartition existingPartition = partitions.get(partitionIndex);
		final IOConsolePartition newPartition;
		if (isInputPartition(existingPartition)) {
			newPartition = new IOConsolePartition(offset, existingPartition.getInputStream());
			if (existingPartition.isReadOnly()) {
				newPartition.setReadOnly();
			}
			if (inputPartitions.contains(existingPartition)) {
				inputPartitions.add(newPartition);
			}
		} else {
			newPartition = new IOConsolePartition(offset, existingPartition.getOutputStream());
		}
		newPartition.setLength((existingPartition.getOffset() + existingPartition.getLength()) - offset);
		existingPartition.setLength(offset - existingPartition.getOffset());
		partitions.add(partitionIndex + 1, newPartition);
		return newPartition;
	}

	/**
	 * A stream has been appended, add to pendingPartions list and schedule
	 * updateJob. updateJob is scheduled with a slight delay, this allows the
	 * console to run the job less frequently and update the document with a greater
	 * amount of data each time the job is run
	 *
	 * @param stream The stream that was written to.
	 * @param s      The string that should be appended to the document.
	 * @throws IOException if partitioner is not connected to a document
	 */
	public void streamAppended(IOConsoleOutputStream stream, String s) throws IOException {
		if (document == null) {
			throw new IOException("Document is closed"); //$NON-NLS-1$
		}
		if (s == null) {
			return;
		}
		synchronized (pendingPartitions) {
			final PendingPartition lastPending = pendingPartitions.size() > 0
					? pendingPartitions.get(pendingPartitions.size() - 1)
					: null;
			if (lastPending != null && lastPending.stream == stream) {
				lastPending.append(s);
			} else {
				pendingPartitions.add(new PendingPartition(stream, s));
			}

			if (pendingSize > 1000) {
				queueJob.schedule();
			} else {
				queueJob.schedule(50);
			}

			if (pendingSize > 160000) {
				if (Display.getCurrent() == null) {
					try {
						pendingPartitions.wait();
					} catch (InterruptedException e) {
					}
				} else {
					// if we are in UI thread we cannot lock it, so process queued output.
					processPendingPartitions();
				}
			}
		}
	}

	/**
	 * Holds data until updateJob can be run and the document can be updated.
	 */
	private class PendingPartition {
		StringBuilder text = new StringBuilder(8192);
		IOConsoleOutputStream stream;

		PendingPartition(IOConsoleOutputStream stream, String text) {
			this.stream = stream;
			append(text);
		}

		void append(String moreText) {
			text.append(moreText);
			pendingSize += moreText.length();
		}
	}

	/**
	 * Updates the document and partitioning structure. Will append everything
	 * received from output streams that is available before finishing.
	 */
	private class QueueProcessingJob extends UIJob {

		QueueProcessingJob() {
			super("IOConsole Updater"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			processPendingPartitions();
			if (ASSERT) {
				checkPartitions();
			}
			return Status.OK_STATUS;
		}

		/*
		 * Job will process as much as it can each time it's run, but it gets scheduled
		 * everytime a PendingPartition is added to the list, meaning that this job
		 * could get scheduled unnecessarily in cases of heavy output. Note however,
		 * that schedule() will only reschedule a running/scheduled Job once even if
		 * it's called many times.
		 */
		@Override
		public boolean shouldRun() {
			synchronized (pendingPartitions) {
				final boolean shouldRun = pendingPartitions.size() > 0;
				return shouldRun;
			}
		}
	}

	/**
	 * Process {@link #pendingPartitions}, append their content to document and
	 * update partitioning.
	 */
	private void processPendingPartitions() {
		final List<PendingPartition> pendingCopy;
		final int size;
		synchronized (pendingPartitions) {
			pendingCopy = new ArrayList<>(pendingPartitions);
			size = pendingSize;
			pendingPartitions.clear();
			pendingSize = 0;
			pendingPartitions.notifyAll();
		}
		synchronized (partitions) {
			final StringBuilder addedContent = new StringBuilder(size);
			IOConsolePartition lastPartition = getPartitionByIndex(partitions.size() - 1);
			int nextOffset = document.getLength();
			for (PendingPartition pendingPartition : pendingCopy) {
				if (lastPartition == null || lastPartition.getOutputStream() != pendingPartition.stream) {
					lastPartition = new IOConsolePartition(nextOffset, pendingPartition.stream);
					partitions.add(lastPartition);
				}
				final int pendingLength = pendingPartition.text.length();
				lastPartition.setLength(lastPartition.getLength() + pendingLength);
				nextOffset += pendingLength;
				addedContent.append(pendingPartition.text);
			}
			try {
				updateType = DocUpdateType.OUTPUT;
				document.replace(document.getLength(), 0, addedContent.toString());
			} catch (BadLocationException e) {
				log(e);
			}
		}
		checkBufferSize();
		checkFinished();
	}

	/**
	 * Job to trim the console document, runs in the UI thread.
	 */
	private class TrimJob extends WorkbenchJob {

		/**
		 * Trims output up to the line containing the given offset, or all output if -1.
		 */
		private int truncateOffset;

		/**
		 * Creates a new job to trim the buffer.
		 */
		TrimJob() {
			super("Trim Job"); //$NON-NLS-1$
			setSystem(true);
		}

		/**
		 * Sets the trim offset.
		 *
		 * @param offset trims output up to the line containing the given offset
		 */
		public void setOffset(int offset) {
			truncateOffset = offset;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			synchronized (partitions) {
				if (document == null) {
					return Status.OK_STATUS;
				}

				int length = document.getLength();
				if (truncateOffset < length) {
					try {
						if (truncateOffset < 0) {
							// clear
							updateType = DocUpdateType.TRIM;
							document.set(""); //$NON-NLS-1$
						} else {
							// overflow
							int cutoffLine = document.getLineOfOffset(truncateOffset);
							int cutOffset = document.getLineOffset(cutoffLine);

							// set the new length of the first partition
							IOConsolePartition partition = (IOConsolePartition) getPartition(cutOffset);
							partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);

							updateType = DocUpdateType.TRIM;
							document.replace(0, cutOffset, ""); //$NON-NLS-1$

							// remove partitions and reset Partition offsets
							int index = partitions.indexOf(partition);
							for (int i = 0; i < index; i++) {
								partitions.remove(0);
							}

							int offset = 0;
							for (IOConsolePartition p : partitions) {
								p.setOffset(offset);
								offset += p.getLength();
							}
						}
						if (ASSERT) {
							checkPartitions();
						}
					} catch (BadLocationException e) {
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	@Override
	public boolean isReadOnly(int offset) {
		final IOConsolePartition partition = getIOPartition(offset);
		return partition != null ? partition.isReadOnly() : true;
	}

	@Override
	public StyleRange[] getStyleRanges(int offset, int length) {
		final IOConsolePartition[] computedPartitions = computeIOPartitioning(offset, length);
		final StyleRange[] styles = new StyleRange[computedPartitions.length];
		for (int i = 0; i < computedPartitions.length; i++) {
			int rangeStart = computedPartitions[i].getOffset();
			int rangeLength = computedPartitions[i].getLength();

			// snap partitions to requested range
			final int underflow = offset - rangeStart;
			if (underflow > 0) {
				rangeStart += underflow;
				rangeLength -= underflow;
			}
			final int overflow = (rangeStart + rangeLength) - (offset + length);
			if (overflow > 0) {
				rangeLength -= overflow;
			}

			styles[i] = computedPartitions[i].getStyleRange(rangeStart, rangeLength);
		}
		return styles;
	}

	@Override
	public ITypedRegion[] computeReadOnlyPartitions() {
		if (document == null) {
			return new IOConsolePartition[0];
		}
		return computeReadOnlyPartitions(0, document.getLength());
	}

	@Override
	public ITypedRegion[] computeReadOnlyPartitions(int offset, int length) {
		return computePartitioning(offset, length, false, true);
	}

	@Override
	public ITypedRegion[] computeWritablePartitions() {
		if (document == null) {
			return new IOConsolePartition[0];
		}
		return computeWritablePartitions(0, document.getLength());
	}

	@Override
	public ITypedRegion[] computeWritablePartitions(int offset, int length) {
		return computePartitioning(offset, length, true, false);
	}

	@Override
	public boolean isReadOnly(int offset, int length) {
		final ITypedRegion[] readOnlyRegions = computeReadOnlyPartitions(offset, length);
		int o = offset;
		int end = offset + length;
		for (ITypedRegion readOnlyRegion : readOnlyRegions) {
			if (o < readOnlyRegion.getOffset()) {
				return false;
			}
			o += readOnlyRegion.getLength();
			if (o >= end) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsReadOnly(int offset, int length) {
		return computeReadOnlyPartitions(offset, length).length > 0;
	}

	@Override
	public int getPreviousOffsetByState(int offset, boolean searchWritable) {
		synchronized (partitions) {
			int partitionIndex = findPartitionCandidate(offset);
			for (; partitionIndex >= 0; partitionIndex--) {
				final IOConsolePartition partition = partitions.get(partitionIndex);
				if (partition.isReadOnly() != searchWritable) {
					return Math.min(partition.getOffset() + partition.getLength() - 1, offset);
				}
			}
		}
		return -1;
	}

	@Override
	public int getNextOffsetByState(int offset, boolean searchWritable) {
		synchronized (partitions) {
			int partitionIndex = findPartitionCandidate(offset);
			if (partitionIndex >= 0) {
				for (; partitionIndex < partitions.size(); partitionIndex++) {
					final IOConsolePartition partition = partitions.get(partitionIndex);
					if (partition.isReadOnly() != searchWritable) {
						return Math.max(partition.getOffset(), offset);
					}
				}
			}
		}
		return document != null ? document.getLength() : 0;
	}

	/**
	 * Get a partition by its index. Safe from out of bounds exceptions.
	 *
	 * @param index index of requested partition
	 * @return the requested partition or <code>null</code> if index is invalid
	 */
	private IOConsolePartition getPartitionByIndex(int index) {
		return (index >= 0 && index < partitions.size()) ? partitions.get(index) : null;
	}

	/**
	 * Check if given partition is from type input partition.
	 *
	 * @param partition partition to check (not <code>null</code>)
	 * @return true if partition is an input partition
	 */
	private static boolean isInputPartition(IOConsolePartition partition) {
		return IOConsolePartition.INPUT_PARTITION_TYPE.equals(partition.getType());
	}

	private static void log(Throwable t) {
		ConsolePlugin.log(t);
	}

	private static void log(int status, String msg) {
		ConsolePlugin.log(new Status(status, ConsolePlugin.getUniqueIdentifier(), msg));
	}

	/**
	 * For debug purpose. Check if whole document is partitioned, partitions are
	 * ordered by offset, every partition has length greater 0 and all writable
	 * input partitions are listed in {@link #inputPartitions}.
	 */
	private void checkPartitions() {
		if (document == null) {
			return;
		}
		synchronized (partitions) {
			final List<IOConsolePartition> knownInputPartitions = new ArrayList<>(inputPartitions);
			int offset = 0;
			for (IOConsolePartition partition : partitions) {
				Assert.isTrue(offset == partition.getOffset());
				Assert.isTrue(partition.getLength() > 0);
				offset += partition.getLength();

				if (isInputPartition(partition) && !partition.isReadOnly()) {
					Assert.isTrue(knownInputPartitions.remove(partition));
				}
			}
			Assert.isTrue(offset == document.getLength());
			Assert.isTrue(knownInputPartitions.isEmpty());
		}
	}
}
