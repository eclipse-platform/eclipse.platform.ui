package org.eclipse.ui.tests.forms.events;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HyperLinkListenerTest {

	private static Shell shell;
	private HyperlinkEvent firedEvent;
	private HyperlinkEvent catchedEvent;
	private Consumer<HyperlinkEvent> linkEventConsumer;
	private static Link link;

	@BeforeAll
	public static void classSetup() {
		shell = new Shell();
		link = new Link(shell, SWT.NONE);
	}

	@BeforeEach
	public void setup() {
		firedEvent = new HyperlinkEvent(link, "uri://test", "link", 0);
		linkEventConsumer = event -> this.catchedEvent = event;
	}

	@AfterAll
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

	@Test
	public void throwsNullPointerOnNullActivatedAdapter() {
		assertThrows(NullPointerException.class, () -> IHyperlinkListener.linkActivatedAdapter(null));
	}

	@Test
	public void throwsNullPointerOnNullExitedAdapter() {
		assertThrows(NullPointerException.class, () -> IHyperlinkListener.linkExitedAdapter(null));
	}

	@Test
	public void throwsNullPointerOnNullEnteredAdapter() {
		assertThrows(NullPointerException.class, () -> IHyperlinkListener.linkEnteredAdapter(null));
	}

}