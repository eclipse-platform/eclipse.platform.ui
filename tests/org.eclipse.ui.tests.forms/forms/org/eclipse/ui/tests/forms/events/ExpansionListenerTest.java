package org.eclipse.ui.tests.forms.events;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Section;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExpansionListenerTest {

	private ExpansionEvent firedEvent;
	private ExpansionEvent catchedEvent;
	private Consumer<ExpansionEvent> expansionEventConsumer;
	private static Shell shell;
	private static Section section;

	@BeforeClass
	public static void classSetup() {
		shell = new Shell();
		section = new Section(shell, SWT.NONE);
	}

	@Before
	public void setup() {
		firedEvent = new ExpansionEvent(section, true);
		expansionEventConsumer = event -> this.catchedEvent = event;
	}

	@AfterClass
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

	@Test(expected = NullPointerException.class)
	public void throwsNullPointerOnNullStateChangedAdapter() {
		IExpansionListener.expansionStateChangedAdapter(null);
	}

	@Test(expected = NullPointerException.class)
	public void throwsNullPointerOnNullStateChangingAdapter() {
		IExpansionListener.expansionStateChangingAdapter(null);
	}
}