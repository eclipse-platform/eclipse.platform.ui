package org.eclipse.ui.tests.forms.events;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HyperLinkListenerTest {

	private static Shell shell;
	private HyperlinkEvent firedEvent;
	private HyperlinkEvent catchedEvent;
	private Consumer<HyperlinkEvent> linkEventConsumer;
	private static Link link;

	@BeforeClass
	public static void classSetup() {
		shell = new Shell();
		link = new Link(shell, SWT.NONE);
	}

	@Before
	public void setup() {
		firedEvent = new HyperlinkEvent(link, "uri://test", "link", 0);
		linkEventConsumer = event -> this.catchedEvent = event;
	}

	@AfterClass
	public static void classTeardown() {
		shell.dispose();
	}

	@Test
	public void callsActivatedConsumer() {
		IHyperlinkListener.linkActivatedAdapter(linkEventConsumer).linkActivated(firedEvent);
		assertEquals(firedEvent, catchedEvent);
	}

	@Test
	public void callsExitedConsumer() {
		IHyperlinkListener.linkExitedAdapter(linkEventConsumer).linkExited(firedEvent);
		assertEquals(firedEvent, catchedEvent);
	}

	@Test
	public void callsEnteredConsumer() {
		IHyperlinkListener.linkEnteredAdapter(linkEventConsumer).linkEntered(firedEvent);
		assertEquals(firedEvent, catchedEvent);
	}

	@Test(expected = NullPointerException.class)
	public void throwsNullPointerOnNullActivatedAdapter() {
		IHyperlinkListener.linkActivatedAdapter(null);
	}

	@Test(expected = NullPointerException.class)
	public void throwsNullPointerOnNullExitedAdapter() {
		IHyperlinkListener.linkExitedAdapter(null);
	}

	@Test(expected = NullPointerException.class)
	public void throwsNullPointerOnNullEnteredAdapter() {
		IHyperlinkListener.linkEnteredAdapter(null);
	}
}