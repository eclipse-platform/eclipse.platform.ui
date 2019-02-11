package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Spinner;
import org.junit.Test;

public class TestUnitSpinnerFactory extends AbstractFactoryTest {

	@Test
	public void createsSpinner() {
		Spinner spinner = SpinnerFactory.createSpinner(SWT.READ_ONLY).create(shell);

		assertEquals(shell, spinner.getParent());
		assertEquals(SWT.READ_ONLY, spinner.getStyle() & SWT.READ_ONLY);
	}

	@Test
	public void createsSpinnerWithAllProperties() {
		final TypedEvent[] raisedEvents = new TypedEvent[2];

		Spinner spinner = SpinnerFactory.createSpinner(SWT.NONE).bounds(20, 120).increment(2, 10).limitTo(3)
				.onSelect(e -> raisedEvents[0] = e).onModify(e -> raisedEvents[1] = e).create(shell);

		assertEquals(2, spinner.getIncrement());
		assertEquals(10, spinner.getPageIncrement());
		assertEquals(20, spinner.getMinimum());
		assertEquals(120, spinner.getMaximum());
		assertEquals(3, spinner.getTextLimit());

		assertEquals(1, spinner.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);

		assertEquals(1, spinner.getListeners(SWT.Modify).length);
		assertNotNull(raisedEvents[1]);
	}

}
