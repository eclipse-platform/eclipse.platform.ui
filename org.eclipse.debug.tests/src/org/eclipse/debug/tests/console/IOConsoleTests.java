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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.tests.AbstractDebugTest;
import org.eclipse.debug.tests.TestUtil;
import org.eclipse.debug.tests.TestsPlugin;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Tests the {@link IOConsole}. Especially the partitioner and viewer parts.
 */
public class IOConsoleTests extends AbstractDebugTest {
	/**
	 * The console view used for the running test. Required to obtain access to
	 * consoles {@link StyledText} widget to simulate user input.
	 */
	@SuppressWarnings("restriction")
	private org.eclipse.ui.internal.console.ConsoleView consoleView;

	/** Track console finished property notification. */
	private final AtomicBoolean consoleFinished = new AtomicBoolean(false);

	/**
	 * Number of received log messages with severity error while running a
	 * single test method.
	 */
	private final AtomicInteger loggedErrors = new AtomicInteger();

	/** Listener to count error messages while testing. */
	private final ILogListener errorLogListener = (IStatus status, String plugin) -> {
		if (status.matches(IStatus.ERROR)) {
			loggedErrors.incrementAndGet();
		}
	};

	public IOConsoleTests() {
		super(IOConsoleTests.class.getSimpleName());
	}

	public IOConsoleTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// create or activate console view
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		assertNotNull(window);
		final IWorkbenchPage activePage = window.getActivePage();
		assertNotNull(activePage);
		IViewPart viewPart = activePage.findView(IConsoleConstants.ID_CONSOLE_VIEW);
		if (viewPart == null) {
			viewPart = activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_CREATE);
		}
		@SuppressWarnings("restriction")
		final org.eclipse.ui.internal.console.ConsoleView castConsoleView = (org.eclipse.ui.internal.console.ConsoleView) viewPart;
		consoleView = castConsoleView;
		activePage.activate(consoleView);

		// add error listener
		loggedErrors.set(0);
		Platform.addLogListener(errorLogListener);
	}

	@Override
	protected void tearDown() throws Exception {
		assertEquals("Test triggered errors in IOConsole.", 0, loggedErrors.get());
		Platform.removeLogListener(errorLogListener);
		super.tearDown();
	}

	/**
	 * Create test util connected to a new {@link IOConsole}.
	 *
	 * @param title console title
	 * @return util to help testing console functions
	 */
	private IOConsoleTestUtil getTestUtil(String title) {
		final IOConsole console = new IOConsole(title, "", null, StandardCharsets.UTF_8.name(), true);
		consoleFinished.set(false);
		console.addPropertyChangeListener((PropertyChangeEvent event) -> {
			if (event.getSource() == console && IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE.equals(event.getProperty())) {
				consoleFinished.set(true);
			}
		});
		final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoles(new IConsole[] { console });
		TestUtil.waitForJobs(getName(), 25, 10000);
		consoleManager.showConsoleView(console);
		@SuppressWarnings("restriction")
		final org.eclipse.ui.internal.console.IOConsolePage page = (org.eclipse.ui.internal.console.IOConsolePage) consoleView.getCurrentPage();
		final StyledText textPanel = (StyledText) page.getControl();
		return new IOConsoleTestUtil(console, textPanel, getName());
	}

	/**
	 * Close the console and optionally check content in {@link IOConsole}'s
	 * input stream.
	 * <p>
	 * Note: all output streams explicitly opened with
	 * {@link IOConsole#newOutputStream()} must be closed before.
	 * </p>
	 *
	 * @param c the test util containing the console to close
	 * @param expected content this {@link IOConsole} input stream has received
	 */
	private void closeConsole(IOConsoleTestUtil c, String... expectedInputLines) throws IOException {
		if (consoleFinished.get()) {
			// This should only happen if no output streams where used and the
			// user input stream was explicit closed before
			TestUtil.log(IStatus.WARNING, TestsPlugin.PLUGIN_ID, "Console was finished before streams where explicit closed.");
		}

		c.closeOutputStream();
		if (c.getConsole().getInputStream() != null) {
			c.getConsole().getInputStream().close();
		}

		if (expectedInputLines.length > 0) {
			assertNotNull(c.getConsole().getInputStream());
			assertTrue("InputStream is empty.", c.getConsole().getInputStream().available() > 0);

			final List<String> inputLines = new ArrayList<>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(c.getConsole().getInputStream(), c.getConsole().getCharset()))) {
				String line;
				while (reader.ready() && (line = reader.readLine()) != null) {
					inputLines.add(line);
				}
			}
			assertEquals("Input contains to many/few lines.", expectedInputLines.length, inputLines.size());
			for (int i = 0; i < expectedInputLines.length; i++) {
				assertEquals("Content of input line " + i + " not as expected.", expectedInputLines[i], inputLines.get(i));
			}
		}
		c.waitForScheduledJobs();
		assertTrue("Console close was not signaled.", consoleFinished.get());

		final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.removeConsoles(new IConsole[] { c.getConsole() });
	}

	/**
	 * Test console clear.
	 */
	public void testConsoleClear() throws Exception {
		final IOConsoleTestUtil c = getTestUtil("Test clear");
		c.writeAndVerify("New console content.");
		c.clear();
		closeConsole(c);
	}

	/**
	 * Test multiple writes, i.e. {@link IOConsole} receives content from
	 * connected {@link IOConsoleOutputStream}.
	 */
	public void testWrites() throws Exception {
		final IOConsoleTestUtil c = getTestUtil("Test writes");
		final String[] strings = new String[] {
				"Hello ", "World", "foo-", "bar", "123 456" };
		for (String s : strings) {
			c.writeAndVerify(s);
		}
		c.write("\n");
		final String longString = String.join("", Collections.nCopies(1000, "0123456789"));
		c.writeAndVerify(longString);
		c.verifyContentByOffset(strings[1], strings[0].length());
		c.verifyContentByLine(String.join("", strings), 0);
		c.verifyPartitions();
		closeConsole(c);
	}

	/**
	 * Test {@link IOConsole} input stream, i.e. simulate user typing or pasting
	 * input in console.
	 */
	public void testUserInput() throws Exception {
		final IOConsoleTestUtil c = getTestUtil("Test input");
		final List<String> expectedInput = new ArrayList<>();

		c.insertAndVerify("remove").select(0, c.getContentLength()).verifyPartitions();
		c.insertTypingAndVerify("abc").insertAndVerify("123").verifyContent("abc123");
		c.moveCaret(-3).insertAndVerify("foo").insertTypingAndVerify("bar").verifyContentByOffset("123", c.getCaretOffset());
		c.moveCaretToLineEnd().backspace(2).verifyContent("abcfoobar1").verifyPartitions();
		c.insert("\r\n").backspace(5);
		expectedInput.add("abcfoobar1");
		int pos = c.getCaretOffset();
		c.insertTypingAndVerify("NewLine").moveCaret(-4).enter();
		expectedInput.add("NewLine");
		assertEquals("Expected newline entered inside line does not break this line.", c.getContentLength(), c.getCaretOffset());
		c.verifyPartitions().verifyContentByOffset("NewLine", pos);
		c.backspace().insertAndVerify("--").select(0, c.getContentLength()).insertTyping("<~>");
		c.verifyContentByLine("--<~>", 2).verifyPartitions();
		c.select(-2, 1).insertAndVerify("-=-").verifyContentByLine("--<-=->", 2).verifyPartitions();

		closeConsole(c, expectedInput.toArray(new String[0]));
	}

	/**
	 * Test mixes of outputs and user inputs in various variants.
	 */
	public void testMixedWriteAndInput() throws Exception {
		final IOConsoleTestUtil c = getTestUtil("Test input output mix");
		final List<String> expectedInput = new ArrayList<>();

		// user input mixed with outputs without caret movement
		c.writeAndVerify("foo");
		c.insertTyping("~~~");
		c.writeAndVerify("bar");
		c.insertTyping("input.");
		c.verifyContent("foo~~~barinput.").verifyPartitions(3);
		c.enter().clear();
		expectedInput.add("~~~input.");

		// type in output or finished input partitions and replace input
		c.insert("fixed\n");
		expectedInput.add("fixed");
		c.setCaretOffset(2);
		c.insertTyping("+");
		c.writeAndVerify("out");
		c.moveCaretToEnd().moveCaret(-1);
		c.insert("more input");
		c.select(0, c.getContentLength()).backspace();
		c.insert("~~p#t").select(c.getContentLength() - 5, 2).insert("in");
		c.select(c.getContentLength() - 2, 1).insertTyping("u");
		c.enter().clear();
		expectedInput.add("+more inputinput");

		closeConsole(c, expectedInput.toArray(new String[0]));
	}

	/**
	 * Test larger number of partitions with pseudo random console content.
	 */
	public void testManyPartitions() throws IOException {
		final IOConsoleTestUtil c = getTestUtil("Test many partitions");
		final List<String> expectedInput = new ArrayList<>();
		final StringBuilder input = new StringBuilder();
		try (IOConsoleOutputStream otherOut = c.getConsole().newOutputStream()) {
			final Random rand = new Random(12);
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 20; i++) {
				for (int j = 0; j < 80; j += sb.length()) {
					sb.setLength(0);
					for (int k = rand.nextInt(15) + 1; k > 0; k--) {
						// add printable ASCII character
						sb.append((char) (rand.nextInt(95) + 32));
					}
					switch (rand.nextInt(5)) {
						case 0:
							c.insert(sb.toString());
							input.append(sb);
							break;

						case 1:
						case 2:
							c.writeFast(sb.toString(), otherOut);
							break;

						default:
							c.writeFast(sb.toString());
							break;
					}
				}
				c.enter().verifyPartitions();
				expectedInput.add(input.toString());
				input.setLength(0);
			}
		}
		closeConsole(c, expectedInput.toArray(new String[0]));
	}

	/**
	 * Test console trimming.
	 */
	public void testTrim() throws Exception {
		final IOConsoleTestUtil c = getTestUtil("Test trim");
		try (IOConsoleOutputStream otherOut = c.getConsole().newOutputStream()) {
			c.writeFast("first\n");
			for (int i = 0; i < 20; i++) {
				c.writeFast("0123456789\n", (i & 1) == 0 ? c.getDefaultOutputStream() : otherOut);
			}
			c.write("last\n");
			c.verifyContentByLine("first", 0).verifyContentByLine("last", -2);
			assertTrue("Document not filled.", c.getDocument().getNumberOfLines() > 15);

			c.getConsole().setWaterMarks(50, 100);
			c.waitForScheduledJobs();
			c.verifyContentByOffset("0123456789", 0);
			assertTrue("Document not trimmed.", c.getDocument().getNumberOfLines() < 15);
		}
		closeConsole(c);
	}
}
