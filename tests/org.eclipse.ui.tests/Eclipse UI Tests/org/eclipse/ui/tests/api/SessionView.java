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
		
		// Create a single child.
		IMemento child = memento.createChild("single");
		child.putInteger("id", 1);
		
		// Create multiple children.
		int count = 10;
		for (int nX = 0; nX < count; nX ++) {
			child = memento.createChild("multiple");
			child.putInteger("id", nX);
		}
		memento.putInteger("multiple.count", count);
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
		testCase.assertEquals(bigInt, new Integer(50));
		
		// Read string.
		String str = memento.getString("string");
		testCase.assertEquals(str, "50");
		
		// Read single child.
		IMemento child = memento.getChild("single");
		testCase.assertNotNull(child);
		bigInt = child.getInteger("id");
		testCase.assertEquals(bigInt, new Integer(1));
		
		// Read multiple children.
		bigInt = memento.getInteger("multiple.count");
		testCase.assertNotNull(bigInt);
		int count = bigInt.intValue();
		IMemento [] children = memento.getChildren("multiple");
		testCase.assertEquals(count, children.length);
		for (int nX = 0; nX < count; nX ++) {
			child = children[nX];
			testCase.assertNotNull(child);
			bigInt = child.getInteger("id");
			testCase.assertEquals(bigInt, new Integer(nX));
		}
	}
}

