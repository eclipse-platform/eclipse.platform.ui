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
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
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
public class IOConsolePartitioner implements IConsoleDocumentPartitioner, IDocumentPartitionerExtension {
	/**
	 * If true validate partitioning after changes and do other additional
	 * assertions. Useful for developing/debugging.
	 */
	private static final boolean ASSERT = false;

	/**
	 * Comparator to sort or search {@link IRegion}s by {@link IRegion#getOffset()}.
	 */
	private static final Comparator<IRegion> CMP_REGION_BY_OFFSET = Comparator.comparing(IRegion::getOffset);

	private PendingPartition consoleClosedPartition;
	/** The connected {@link IDocument} this partitioner manages. */
	private IDocument document;
	/**
	 * List of all partitions. Must always be sorted ascending by
	 * {@link IRegion#getOffset()} and not contain <code>null</code> or 0-length
	 * elements. (see also {@link #checkPartitions()})
	 */
	private ArrayList<IOConsolePartition> partitions;
	/** Blocks of data that have not yet been appended to the document. */
	private ArrayList<PendingPartition> pendingPartitions;
	/**
	 * A list of PendingPartitions to be appended by the updateJob
	 */
	private ArrayList<PendingPartition> updatePartitions;
	/**
	 * Job that appends pending partitions to the document.
	 */
	private QueueProcessingJob queueJob;
	/** Job that trims console content if it exceeds {@link #highWaterMark}. */
	private TrimJob trimJob = new TrimJob();
	/** The input stream attached to this document. */
	private IOConsoleInputStream inputStream;
	/**
	 * Flag to indicate that the updateJob is updating the document.
	 */
	private boolean updateInProgress;
	/**
	 * A list of partitions containing input from the console, that have not been
	 * appended to the input stream yet. No guarantees on element order.
	 */
	private ArrayList<IOConsolePartition> inputPartitions;
	/**
	 * offset used by updateJob
	 */
	private int firstOffset;
	/** An array of legal line delimiters. */
	private String[] lld;
	private int highWaterMark = -1;
	private int lowWaterMark = -1;
	private boolean connected = false;

	/** The partitioned {@link IOConsole}. */
	private IOConsole console;

	/**
	 * Lock for appending to and removing from the document - used
	 * to synchronize addition of new text/partitions in the update
	 * job and handling buffer overflow/clearing of the console.
	 */
	private Object overflowLock = new Object();


	private int fBuffer;

	public IOConsolePartitioner(IOConsoleInputStream inputStream, IOConsole console) {
		this.inputStream = inputStream;
		this.console = console;
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
		document = doc;
		document.setDocumentPartitioner(this);
		lld = document.getLegalLineDelimiters();
		partitions = new ArrayList<>();
		pendingPartitions = new ArrayList<>();
		inputPartitions = new ArrayList<>();
		queueJob = new QueueProcessingJob();
		queueJob.setSystem(true);
		queueJob.setPriority(Job.INTERACTIVE);
		queueJob.setRule(console.getSchedulingRule());
		connected = true;
	}

	public int getHighWaterMark() {
		return highWaterMark;
	}

	public int getLowWaterMark() {
		return lowWaterMark;
	}

	public void setWaterMarks(int low, int high) {
		lowWaterMark = low;
		highWaterMark = high;
		ConsolePlugin.getStandardDisplay().asyncExec(this::checkBufferSize);
	}

	/**
	 * Notification from the console that all of its streams have been closed.
	 */
	public void streamsClosed() {
		consoleClosedPartition = new PendingPartition(null, null);
		synchronized (pendingPartitions) {
			pendingPartitions.add(consoleClosedPartition);
		}
		queueJob.schedule(); //ensure that all pending partitions are processed.
	}

	@Override
	public void disconnect() {
		synchronized (overflowLock) {
			document = null;
			partitions.clear();
			connected = false;
			try {
				inputStream.close();
			} catch (IOException e) {
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
				result.add(partition);
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
	 * When the number of lines in the document exceeds the high water mark, the
	 * beginning of the document is trimmed until the number of lines equals the
	 * low water mark.
	 */
	private void checkBufferSize() {
		if (document != null && highWaterMark > 0) {
			int length = document.getLength();
			if (length > highWaterMark) {
				if (trimJob.getState() == Job.NONE) { //if the job isn't already running
					trimJob.setOffset(length - lowWaterMark);
					trimJob.schedule();
				}
			}
		}
	}

	/**
	 * Clears the console
	 */
	public void clearBuffer() {
		synchronized (overflowLock) {
			trimJob.setOffset(-1);
			trimJob.schedule();
		}
	}

	@Override
	public IRegion documentChanged2(DocumentEvent event) {
		if (document == null) {
			return null; //another thread disconnected the partitioner
		}
		if (document.getLength() == 0) { // document cleared
			synchronized (partitions) {
				partitions.clear();
				inputPartitions.clear();
			}
			return new Region(0, 0);
		}

		if (updateInProgress) {
			synchronized(partitions) {
				if (updatePartitions != null) {
					IOConsolePartition lastPartition = getPartitionByIndex(partitions.size() - 1);
					for (PendingPartition pp : updatePartitions) {
						if (pp == consoleClosedPartition) {
							continue;
						}

						int ppLen = pp.text.length();
						if (lastPartition != null && lastPartition.getOutputStream() == pp.stream) {
							int len = lastPartition.getLength();
							lastPartition.setLength(len + ppLen);
						} else {
							IOConsolePartition partition = new IOConsolePartition(firstOffset, pp.stream);
							partition.setLength(ppLen);
							lastPartition = partition;
							partitions.add(partition);
						}
						firstOffset += ppLen;
					}
				}
			}
		} else {
			synchronized (partitions) {
				return applyUserInput(event);
			}
		}
		return new Region(event.fOffset, event.fText.length());
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
	private IRegion applyUserInput(DocumentEvent event) {
		final int eventTextLength = event.getText() != null ? event.getText().length() : 0;
		final int offset = event.getOffset();
		final int amountDeleted = event.getLength();

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
				final int[] result = TextUtilities.indexOf(lld, event.getText(), textOffset);
				final boolean foundNewline = result[1] >= 0;
				final int newTextOffset = foundNewline ? result[0] + lld[result[1]].length() : eventTextLength;
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

				if (foundNewline) {
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
					inputStream.appendData(inputLine.toString());
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

	private void setUpdateInProgress(boolean b) {
		updateInProgress = b;
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
		synchronized(pendingPartitions) {
			PendingPartition last = pendingPartitions.size() > 0 ? pendingPartitions.get(pendingPartitions.size()-1) : null;
			if (last != null && last.stream == stream) {
				last.append(s);
			} else {
				pendingPartitions.add(new PendingPartition(stream, s));
				if (fBuffer > 1000) {
					queueJob.schedule();
				} else {
					queueJob.schedule(50);
				}
			}

			if (fBuffer > 160000) {
				if(Display.getCurrent() == null){
					try {
						pendingPartitions.wait();
					} catch (InterruptedException e) {
					}
				} else {
					/*
					 * if we are in UI thread we cannot lock it, so process
					 * queued output.
					 */
					processQueue();
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
			if (text != null) {
				append(text);
			}
		}

		void append(String moreText) {
			text.append(moreText);
			fBuffer += moreText.length();
		}
	}

	/**
	 * Updates the document. Will append everything that is available before
	 * finishing.
	 */
	private class QueueProcessingJob extends UIJob {

		QueueProcessingJob() {
			super("IOConsole Updater"); //$NON-NLS-1$
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			processQueue();
			if (ASSERT) {
				checkPartitions();
			}
			return Status.OK_STATUS;
		}

		/*
		 * Job will process as much as it can each time it's run, but it gets
		 * scheduled everytime a PendingPartition is added to the list, meaning
		 * that this job could get scheduled unnecessarily in cases of heavy output.
		 * Note however, that schedule() will only reschedule a running/scheduled Job
		 * once even if it's called many times.
		 */
		@Override
		public boolean shouldRun() {
			boolean shouldRun = connected && pendingPartitions != null && pendingPartitions.size() > 0;
			return shouldRun;
		}
	}

	void processQueue() {
		synchronized (overflowLock) {
			ArrayList<PendingPartition> pendingCopy = new ArrayList<>();
			StringBuilder buffer = null;
			boolean consoleClosed = false;
			synchronized(pendingPartitions) {
				pendingCopy.addAll(pendingPartitions);
				pendingPartitions.clear();
				fBuffer = 0;
				pendingPartitions.notifyAll();
			}
			// determine buffer size
			int size = 0;
			for (PendingPartition pp : pendingCopy) {
				if (pp != consoleClosedPartition) {
					size+= pp.text.length();
				}
			}
			buffer = new StringBuilder(size);
			for (PendingPartition pp : pendingCopy) {
				if (pp != consoleClosedPartition) {
					buffer.append(pp.text);
				} else {
					consoleClosed = true;
				}
			}
			if (connected) {
				setUpdateInProgress(true);
				updatePartitions = pendingCopy;
				firstOffset = document.getLength();
				try {
					if (buffer != null) {
						document.replace(firstOffset, 0, buffer.toString());
					}
				} catch (BadLocationException e) {
				}
				updatePartitions = null;
				setUpdateInProgress(false);
			}
			if (consoleClosed) {
				console.partitionerFinished();
			}
			checkBufferSize();
		}

	}

	/**
	 * Job to trim the console document, runs in the  UI thread.
	 */
	private class TrimJob extends WorkbenchJob {

		/**
		 * trims output up to the line containing the given offset,
		 * or all output if -1.
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
			if (document == null) {
				return Status.OK_STATUS;
			}

			int length = document.getLength();
			if (truncateOffset < length) {
				synchronized (overflowLock) {
					try {
						if (truncateOffset < 0) {
							// clear
							setUpdateInProgress(true);
							document.set(""); //$NON-NLS-1$
							setUpdateInProgress(false);
						} else {
							// overflow
							int cutoffLine = document.getLineOfOffset(truncateOffset);
							int cutOffset = document.getLineOffset(cutoffLine);


							// set the new length of the first partition
							IOConsolePartition partition = (IOConsolePartition) getPartition(cutOffset);
							partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);

							setUpdateInProgress(true);
							document.replace(0, cutOffset, ""); //$NON-NLS-1$
							setUpdateInProgress(false);

							//remove partitions and reset Partition offsets
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
		return partition != null ? partition.isReadOnly() : false;
	}

	@Override
	public StyleRange[] getStyleRanges(int offset, int length) {
		if (!connected) {
			return new StyleRange[0];
		}
		IOConsolePartition[] computedPartitions = computeIOPartitioning(offset, length);
		StyleRange[] styles = new StyleRange[computedPartitions.length];
		for (int i = 0; i < computedPartitions.length; i++) {
			int rangeStart = Math.max(computedPartitions[i].getOffset(), offset);
			int rangeLength = computedPartitions[i].getLength();
			styles[i] = computedPartitions[i].getStyleRange(rangeStart, rangeLength);
		}
		return styles;
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

	/**
	 * For debug purpose. Check if whole document is partitioned, partitions are
	 * ordered by offset, every partition has length greater 0 and all writable
	 * input partitions are listed in {@link #inputPartitions}.
	 */
	private void checkPartitions() {
		if (!connected) {
			return;
		}
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
