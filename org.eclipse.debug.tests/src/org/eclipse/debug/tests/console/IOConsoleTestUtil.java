/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.console;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.tests.TestUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import junit.framework.TestCase;

/**
 * Utility to help testing input and output in {@link IOConsole}.
 */
public final class IOConsoleTestUtil {
	/** The tested console. */
	private final IOConsole console;
	/** Document of the tested console. */
	private final IDocument doc;
	/** Text widget of the tested console. */
	private final StyledText textPanel;
	/** The tested consoles document partitioner. */
	private final IConsoleDocumentPartitioner partitioner;
	/** Valid partition types of the used document partitioner. */
	private final List<String> validPartionTypes;
	/** Name used for some logging purpose. */
	private final String name;
	/**
	 * The default output stream used to simulate output in console. Lazy
	 * initialized.
	 */
	private IOConsoleOutputStream defaultOut = null;

	/**
	 * Create a new testing helper.
	 *
	 * @param console the console to test
	 * @param textPanel the consoles text widget. May be <code>null</code> but
	 *            none of the user input or caret movement methods can be used
	 *            in this case.
	 * @param name name of the caller which will be logged as prefix if some job
	 *            waiting timed out.
	 */
	public IOConsoleTestUtil(IOConsole console, StyledText textPanel, String name) {
		this.console = console;
		TestCase.assertNotNull(this.console);
		this.doc = console.getDocument();
		TestCase.assertNotNull(this.doc);
		final Class<?> expectedInterface = IConsoleDocumentPartitioner.class;
		TestCase.assertTrue("Expected partitioner implements " + expectedInterface.getName() + //
				". Found: " + this.doc.getDocumentPartitioner().getClass(), //
				expectedInterface.isAssignableFrom(this.doc.getDocumentPartitioner().getClass()));
		this.partitioner = (IConsoleDocumentPartitioner) this.doc.getDocumentPartitioner();
		this.validPartionTypes = Arrays.asList(this.partitioner.getLegalContentTypes());
		this.textPanel = textPanel;
		this.name = name;
	}

	/**
	 * Clear console.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil clear() throws Exception {
		console.clearConsole();
		waitForScheduledJobs();
		TestCase.assertEquals("Console is not cleared.", 0, doc.getLength());
		return this;
	}

	/**
	 * Simulate {@link IOConsole} received input from connected processes output
	 * stream but do not wait until input was actually written to console.
	 *
	 * @param s content to write in output stream
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #write(String)
	 */
	public IOConsoleTestUtil writeFast(final String s) throws IOException {
		return writeFast(s, getDefaultOutputStream());
	}

	/**
	 * Simulate {@link IOConsole} received input from connected processes output
	 * stream and wait until content actually arrived in console.
	 *
	 * @param s content to write in output stream
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #writeFast(String)
	 */
	public IOConsoleTestUtil write(final String s) throws Exception {
		return write(s, getDefaultOutputStream());
	}

	/**
	 * Simulate {@link IOConsole} received input from connected processes output
	 * stream and verify new console content and partitioning. Expects written
	 * string was appended to end of previous console content and has not
	 * overwritten or removed any previous content. Further expects that
	 * partitioning of new written content is not mixed with other partition
	 * types.
	 *
	 * @param s content to write in output stream
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil writeAndVerify(final String s) throws Exception {
		return writeAndVerify(s, getDefaultOutputStream());
	}

	/**
	 * Simulate {@link IOConsole} received input from connected processes output
	 * stream but do not wait until input was actually written to console.
	 *
	 * @param s content to write in output stream
	 * @param out use this output stream instead of default one
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #write(String, IOConsoleOutputStream)
	 */
	public IOConsoleTestUtil writeFast(final String s, IOConsoleOutputStream out) throws IOException {
		out.write(s);
		return this;
	}

	/**
	 * Simulate {@link IOConsole} received input from connected processes output
	 * stream and wait until content actually arrived in console.
	 *
	 * @param s content to write in output stream
	 * @param out use this output stream instead of default one
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #writeFast(String, IOConsoleOutputStream)
	 */
	public IOConsoleTestUtil write(final String s, IOConsoleOutputStream out) throws Exception {
		writeFast(s, out);
		waitForScheduledJobs();
		return this;
	}

	/**
	 * Simulate {@link IOConsole} received input from connected processes output
	 * stream and verify new console content and partitioning. Expects written
	 * string was appended to end of previous console content and has not
	 * overwritten or removed any previous content. Further expects that
	 * partitioning of new written content is not mixed with other partition
	 * types.
	 *
	 * @param s content to write in output stream
	 * @param out use this output stream instead of default one
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil writeAndVerify(final String s, IOConsoleOutputStream out) throws Exception {
		final int oldLength = doc.getLength();
		write(s, out);
		TestCase.assertEquals("Console content length not as expected.", oldLength + s.length(), doc.getLength());
		verifyContentByOffset(s, oldLength);
		verifyOutputPartitions(oldLength, s.length());
		return this;
	}

	/**
	 * Simulate user input. This method is similar to user pasting some content
	 * in console but may not be equal in some case. Especially caret movement
	 * can be different in so use with care.
	 *
	 * @param content string to insert in console
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #insertTyping(String)
	 */
	public IOConsoleTestUtil insert(String content) {
		final int oldCaretOffset = getCaretOffset();
		final int oldSelectionLength = textPanel.getSelectionCount();
		final Event e = new Event();
		e.start = oldCaretOffset - oldSelectionLength;
		e.end = oldCaretOffset;
		e.text = content;
		e.doit = true;
		textPanel.notifyListeners(SWT.Verify, e);
		if (e.doit) {
			textPanel.replaceTextRange(e.start, e.end - e.start, e.text);
			// replace does not move caret itself but when a real user paste
			// content the caret is updated
			setCaretOffset(e.start + content.length());
		}
		TestUtil.waitForJobs(name, 0, 1000);
		return this;
	}

	/**
	 * Simulate user input and verify the resulting content. This method is
	 * similar to user pasting some content in console but not equal in some
	 * case. Especially caret movement is different in some cases so use with
	 * care.
	 * <p>
	 * The verification expects the content is written at current caret offset,
	 * so do not use if caret is magically moved. (e.g. if insert starts in
	 * read-only part)
	 * </p>
	 *
	 * @param content string to insert in console
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #insertTypingAndVerify(String)
	 */
	public IOConsoleTestUtil insertAndVerify(String content) {
		final int oldLength = doc.getLength();
		final int oldSelectionLength = textPanel.getSelectionCount();
		final int oldOffset = getCaretOffset() - oldSelectionLength;
		insert(content);
		TestCase.assertEquals("Console content length not as expected.", oldLength + content.length() - oldSelectionLength, doc.getLength());
		TestCase.assertEquals("Caret not at expected position.", oldOffset + content.length(), getCaretOffset());
		verifyContentByOffset(content, oldOffset);
		verifyInputPartitions(oldOffset, content.length());
		return this;
	}

	/**
	 * Simulate user typing some text in console. This method simulates
	 * keystrokes and therefore tests console behavior quite realistic.
	 * <p>
	 * Note: typing <code>\n</code> may result in platform dependent line
	 * delimiter entered. Also <code>\r\n</code> is typed as two separate
	 * characters and therefore leads to two line breaks.
	 * </p>
	 *
	 * @param content string to insert in console
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #insert(String)
	 */
	public IOConsoleTestUtil insertTyping(String content) {
		for (char c : content.toCharArray()) {
			final Event e = new Event();
			e.character = c;
			textPanel.notifyListeners(SWT.KeyDown, e);
			textPanel.notifyListeners(SWT.KeyUp, e);
		}
		TestUtil.waitForJobs(name, 0, 1000);
		return this;
	}

	/**
	 * Simulate user typing some text in console and check results. This method
	 * simulates keystrokes and therefore tests console behavior quite
	 * realistic.
	 * <p>
	 * Note: typing <code>\n</code> may result in platform dependent line
	 * delimiter entered. Also <code>\r\n</code> is typed as two separate
	 * characters and therefore leads to two line breaks.
	 * </p>
	 *
	 * @param content string to insert in console
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see #insertAndVerify(String)
	 */
	public IOConsoleTestUtil insertTypingAndVerify(String content) {
		final int oldLength = doc.getLength();
		final int oldSelectionLength = textPanel.getSelectionCount();
		final int oldOffset = getCaretOffset() - oldSelectionLength;
		insertTyping(content);
		TestCase.assertEquals("Console content length not as expected.", oldLength + content.length() - oldSelectionLength, doc.getLength());
		TestCase.assertEquals("Caret not at expected position.", oldOffset + content.length(), getCaretOffset());
		verifyContentByOffset(content, oldOffset);
		verifyInputPartitions(oldOffset, content.length());
		return this;
	}

	/**
	 * Simulate user pressing backspace.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil backspace() {
		return backspace(1);
	}

	/**
	 * Simulate user pressing backspace multiple times.
	 *
	 * @param repeat perform this many backspaces
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil backspace(int repeat) {
		for (int i = 0; i < repeat; i++) {
			textPanel.invokeAction(ST.DELETE_PREVIOUS);
		}
		return this;
	}

	/**
	 * Simulate user pressing enter.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil enter() {
		// Note: newline will be replaced by
		// System.getProperty("line.separator") so do not assume document length
		// will increase by 1
		insertTyping("\n");
		return this;
	}

	/**
	 * Current caret position in console.
	 *
	 * @return current caret position in console
	 * @see StyledText#getCaretOffset()
	 */
	public int getCaretOffset() {
		return textPanel.getCaretOffset();
	}

	/**
	 * Set caret to new position.
	 *
	 * @param offset new caret position
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see StyledText#setCaretOffset(int)
	 */
	public IOConsoleTestUtil setCaretOffset(int offset) {
		textPanel.setCaretOffset(offset);
		return this;
	}

	/**
	 * Move caret by given amount forth or back.
	 *
	 * @param amount steps to set caret forth (positive value) or back (negative
	 *            value)
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil moveCaret(int amount) {
		setCaretOffset(getCaretOffset() + amount);
		return this;
	}

	/**
	 * Move caret to start of console content.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil moveCaretToStart() {
		textPanel.invokeAction(ST.TEXT_START);
		return this;
	}

	/**
	 * Move caret to end of console content.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil moveCaretToEnd() {
		textPanel.invokeAction(ST.TEXT_END);
		return this;
	}

	/**
	 * Move caret to start of its current line.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil moveCaretToLineStart() {
		textPanel.invokeAction(ST.LINE_START);
		return this;
	}

	/**
	 * Move caret to end of its current line.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil moveCaretToLineEnd() {
		textPanel.invokeAction(ST.LINE_END);
		return this;
	}

	/**
	 * Select text in console.
	 *
	 * @param offset selection start position. May be negative to select
	 *            relative from document end.
	 * @param length selection length
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see StyledText#setSelection(int, int)
	 */
	public IOConsoleTestUtil select(int offset, int length) {
		final int o = offset < 0 ? doc.getLength() + offset : offset;
		setCaretOffset(o + length);
		textPanel.setSelectionRange(o, length);
		return this;
	}

	/**
	 * Check if console content equals the expected content.
	 *
	 * @param expectedContent content expect in console
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyContent(String expectedContent) {
		verifyContentByOffset(expectedContent, 0);
		TestCase.assertEquals("More or less content in console as expected.", expectedContent.length(), doc.getLength());
		return this;
	}

	/**
	 * Check if line in console has expected content.
	 *
	 * @param expectedContent content expect in console
	 * @param lineNum the line in console to check. First line has number
	 *            <code>0</code>. Accepts negative numbers as line relative to
	 *            console end.
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyContentByLine(String expectedContent, int lineNum) {
		final int l = lineNum < 0 ? doc.getNumberOfLines() + lineNum : lineNum;
		try {
			final IRegion line = doc.getLineInformation(l);
			verifyContentByOffset(expectedContent, line.getOffset());
			TestCase.assertEquals("Line " + l + " has wrong length.", expectedContent.length(), line.getLength());
		} catch (BadLocationException e) {
			TestCase.fail("Expected line not found in console document. Bad location!");
		}
		return this;
	}

	/**
	 * Check if console contains expected content at given offset.
	 *
	 * @param expectedContent content expect in console
	 * @param offset position where content is expected. If offset is negative
	 *            it will be interpreted as relative to document end.
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyContentByOffset(String expectedContent, int offset) {
		try {
			final int o = offset < 0 ? doc.getLength() + offset : offset;
			final int len = Math.min(doc.getLength() - o, expectedContent.length());
			TestCase.assertEquals("Expected string not found in console document.", expectedContent, doc.get(o, len));
		} catch (BadLocationException ex) {
			TestCase.fail("Expected string not found in console document. Bad location!");
		}
		return this;
	}

	/**
	 * Check if console range is partitioned only by output partitions.
	 *
	 * @param offset range start
	 * @param length range length
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyOutputPartitions(int offset, int length) {
		return verifyPartitions(offset, length, outputPartitionType(), false, false, 0);
	}

	/**
	 * Check if console range is partitioned only by input partitions.
	 *
	 * @param offset range start
	 * @param length range length
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyInputPartitions(int offset, int length) {
		return verifyPartitions(offset, length, inputPartitionType(), false, false, 0);
	}

	/**
	 * Check if whole console document is partitioned.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyPartitions() {
		return verifyPartitions(0);
	}

	/**
	 * Check if whole console document is partitioned.
	 *
	 * @param minPartitionNumber the minimum number of partitions expected
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil verifyPartitions(int minPartitionNumber) {
		return verifyPartitions(0, doc.getLength(), null, true, false, minPartitionNumber);
	}

	/**
	 * Check partitioning of console range.
	 *
	 * @param offset range start
	 * @param length range length
	 * @param expectedType either all partitions in checked ranges are expected
	 *            to be of this type or <code>null</code> to not force type
	 * @param allowMixedTypes if true range may comprised from all partition
	 *            types. If false all partitions in range must be of same type.
	 * @param allowGaps if false
	 *            {@link IDocumentPartitioner#computePartitioning(int, int)}
	 *            must not contain any unpartitioned parts
	 * @param minPartitionNumber the minimum number of partitions expected
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 * @see IDocumentPartitioner#computePartitioning(int, int)
	 * @see IDocumentPartitioner#getPartition(int)
	 */
	public IOConsoleTestUtil verifyPartitions(final int offset, final int length, final String expectedType, final boolean allowMixedTypes, final boolean allowGaps, int minPartitionNumber) {
		{
			final ITypedRegion[] partitions = getPartitioner().computePartitioning(offset, length);
			TestCase.assertNotNull("Got no partitions.", partitions);
			TestCase.assertTrue("Got less partitions than expected.", partitions.length >= minPartitionNumber);

			// points to offset after last partition range aka. expected start
			// of next partition
			int lastEnd = -1;
			ITypedRegion lastPartition = null;
			String partitionType = expectedType;
			for (ITypedRegion partition : partitions) {
				TestCase.assertNotSame("Got same partition twice.", lastPartition, partition);
				TestCase.assertTrue("Partition overlapping. (or not sorted as expected)", partition.getOffset() >= lastEnd);
				if (!allowGaps && lastEnd != -1) {
					TestCase.assertTrue("Partitioning gap.", partition.getOffset() == lastEnd);
				}
				TestCase.assertTrue("Not a valid partition type.", validPartionTypes.contains(partition.getType()));
				TestCase.assertTrue("Wrong partition type.", partitionType == null || partitionType.equals(partition.getType()) || allowMixedTypes);
				if (partitionType == null && !allowMixedTypes) {
					partitionType = partition.getType();
				}
				lastEnd = partition.getOffset() + partition.getLength();
				lastPartition = partition;
			}
		}
		{
			int pos = offset;
			int end = offset + length;
			ITypedRegion lastPartition = null;
			String partitionType = expectedType;
			while (pos < end) {
				final ITypedRegion partition = getPartitioner().getPartition(pos);
				if (partition == null) {
					TestCase.assertTrue("Did not expect 'null' partition.", allowGaps);
					pos++;
					continue;
				}
				TestCase.assertNotSame("Got same partition again.", lastPartition, partition);
				TestCase.assertFalse("Did not expected and cannot handle empty partition.", partition.getLength() == 0);
				TestCase.assertTrue("Got not the requested partition.", partition.getOffset() <= pos && partition.getOffset() + partition.getLength() > pos);
				TestCase.assertTrue("Not a valid partition type.", validPartionTypes.contains(partition.getType()));
				lastPartition = partition;
				if (partitionType != null && !partitionType.equals(partition.getType())) {
					TestCase.assertTrue("Wrong partition type.", allowMixedTypes);
					pos += partition.getLength();
					end += partition.getLength();
					continue;
				}
				if (partitionType == null && !allowMixedTypes) {
					partitionType = partition.getType();
				}
				pos = partition.getOffset() + partition.getLength();
			}
		}
		return this;
	}

	/**
	 * Wait for waiting or scheduled jobs.
	 *
	 * @return this {@link IOConsoleTestUtil} to chain methods
	 */
	public IOConsoleTestUtil waitForScheduledJobs() {
		boolean jobSleeping = false;
		for (int i = 0; i < 5 && !jobSleeping; i++) {
			final boolean jobPending = TestUtil.waitForJobs(name, 25, 2000);
			if (!jobPending) {
				jobSleeping = false;
				for (Job job : TestUtil.getRunningOrWaitingJobs(null)) {
					if (job.getState() == Job.SLEEPING) {
						jobSleeping = true;
						break;
					}
				}
			}
		}
		return this;
	}

	/**
	 * Close the default output stream if it was used.
	 */
	public void closeOutputStream() {
		if (defaultOut != null) {
			try {
				defaultOut.close();
			} catch (IOException e) {
				TestCase.fail("Failed to close output stream.");
			}
		}
	}

	/**
	 * Get length of current console content.
	 *
	 * @return consoles document length
	 */
	public int getContentLength() {
		return getDocument().getLength();
	}

	public IOConsole getConsole() {
		return console;
	}

	public IDocument getDocument() {
		return doc;
	}

	public IConsoleDocumentPartitioner getPartitioner() {
		return partitioner;
	}

	public IOConsoleOutputStream getDefaultOutputStream() {
		if (defaultOut == null) {
			defaultOut = console.newOutputStream();
		}
		return defaultOut;
	}

	/**
	 * Get identifier for output {@link IOConsolePartition}s.
	 *
	 * @return output partition identifier
	 */
	@SuppressWarnings("restriction")
	public static String outputPartitionType() {
		return org.eclipse.ui.internal.console.IOConsolePartition.OUTPUT_PARTITION_TYPE;
	}

	/**
	 * Get identifier for input {@link IOConsolePartition}s.
	 *
	 * @return input partition identifier
	 */
	@SuppressWarnings("restriction")
	public static String inputPartitionType() {
		return org.eclipse.ui.internal.console.IOConsolePartition.INPUT_PARTITION_TYPE;
	}
}
