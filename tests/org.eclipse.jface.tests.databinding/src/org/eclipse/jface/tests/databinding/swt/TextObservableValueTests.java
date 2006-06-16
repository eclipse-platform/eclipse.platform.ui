package org.eclipse.jface.tests.databinding.swt;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Tests to assert the inputs of the TextObservableValue constructor.
 * 
 * @since 3.2
 */
public class TextObservableValueTests extends TestCase {
	private Text text;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Shell shell = new Shell();
		text = new Text(shell, SWT.NONE);
	}
	
	/**
	 * Asserts that if the a <code>null</code> Text is passed TextObservableValue throws a IAE.
	 */
	public void testConstructor() {
		try {
			new TextObservableValue(null, SWT.NONE);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	/**
	 * Asserts that only valid SWT event types are accepted on construction of TextObservableValue.
	 */
	public void testConstructorUpdateEventTypes() {
		try {
			new TextObservableValue(text, SWT.NONE);
			new TextObservableValue(text, SWT.FocusOut);
			new TextObservableValue(text, SWT.Modify);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
		
		try {
			new TextObservableValue(text, SWT.Verify);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
}