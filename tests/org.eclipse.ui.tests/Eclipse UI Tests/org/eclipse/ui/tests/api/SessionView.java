package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.*;

/**
 * This view is used to test the creation and restoration of 
 * view state between sessions.
 */
public class SessionView extends MockViewPart {

	private IMemento memento;
	
	public static String VIEW_ID = "org.eclipse.ui.tests.api.SessionView";
	/**
	 * Constructor for SessionView
	 */
	public SessionView() {
		super();
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	/**
	 * Create an IMemento.
	 */
	public void saveState(IMemento memento) {
		createMementoState(memento);
	}
	
	/**
	 * Creates an IMemento.
	 */
	private void createMementoState(IMemento memento) {
		// Create float, integer and string.
		memento.putFloat("float", 0.50f);
		memento.putInteger("integer", 50);
		memento.putString("string", "50");
	}
	
	/**
	 * Restore an IMemento.
	 */
	public void testMementoState(TestCase testCase) {
		// Verify that the memento was passed to us in
		// constructor.
		testCase.assertNotNull(memento);
	
		// Read float.	
		Float bigFloat = memento.getFloat("float");
		testCase.assertNotNull(bigFloat);
		testCase.assertEquals(bigFloat.floatValue(), 0.50f, 0.0001);
		
		// Read int.	
		Integer bigInt = memento.getInteger("integer");
		testCase.assertNotNull(bigInt);
		testCase.assertEquals(bigInt.intValue(), 50);
		
		// Read string.
		String str = memento.getString("string");
		testCase.assertNotNull(str);
		testCase.assertEquals(str, "50");
	}
}

