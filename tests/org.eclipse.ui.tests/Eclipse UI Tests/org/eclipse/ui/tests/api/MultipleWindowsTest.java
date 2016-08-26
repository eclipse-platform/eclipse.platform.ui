package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotSame;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A set of tests for multiple monitor situations that ensures interactions are
 * isolated to the respective window.
 */
public class MultipleWindowsTest {
	IWorkbench wb;
	IWorkbenchWindow win1;
	IWorkbenchWindow win2;

	@Before
	public void setUp() throws WorkbenchException {
		IWorkbench wb = PlatformUI.getWorkbench();
		win1 = wb.openWorkbenchWindow(null);
		win2 = wb.openWorkbenchWindow(null);
		assertNotSame(win1, win2);
	}

	@After
	public void tearDown() {
		if (win1 != null) {
			win1.close();
			win1 = null;
		}
		if (win2 != null) {
			win2.close();
			win2 = null;
		}
	}

	/**
	 * @see <a href="http://eclip.se/493335"> Bug 493335 - [WorkingSets] Setting
	 *      window working set affects all windows</a>
	 */
	@Test
	public void testIndependentWorkingSets() {
		assertNotSame(win1.getActivePage().getAggregateWorkingSet(), win2.getActivePage().getAggregateWorkingSet());
	}
}
