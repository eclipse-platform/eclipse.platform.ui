package org.eclipse.ui.tests.forms.events;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Section;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpansionListenerTest {

	private ExpansionEvent firedEvent;
	private ExpansionEvent catchedEvent;
	private Consumer<ExpansionEvent> expansionEventConsumer;
	private static Shell shell;
	private static Section section;

	@BeforeAll
	public static void classSetup() {
		shell = new Shell();
		section = new Section(shell, SWT.NONE);
	}

	@BeforeEach
	public void setup() {
		firedEvent = new ExpansionEvent(section, true);
		expansionEventConsumer = event -> this.catchedEvent = event;
	}

	@AfterAll
	public static void classTeardown() {
		shell.dispose();
	}

	@Test
	public void callsExpansionStateChangedConsumer() {
		IExpansionListener.expansionStateChangedAdapter(expansionEventConsumer).expansionStateChanged(firedEvent);
		assertEquals(firedEvent, catchedEvent);
	}

	@Test
	public void callsExpansionStateChangingConsumer() {
		IExpansionListener.expansionStateChangingAdapter(expansionEventConsumer).expansionStateChanging(firedEvent);
		assertEquals(firedEvent, catchedEvent);
	}
	@Test
	void throwsNullPointerOnNullStateChangedAdapter() {
		assertThrows(NullPointerException.class, () -> IExpansionListener.expansionStateChangedAdapter(null));
	}

	@Test
	void throwsNullPointerOnNullStateChangingAdapter() {
		assertThrows(NullPointerException.class, () -> IExpansionListener.expansionStateChangingAdapter(null));
	}
}