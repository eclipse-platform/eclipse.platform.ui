package org.eclipse.ui.tests.api;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

/**
 * Tests the PlatformUI class.
 */
public class IPageServiceTest extends TestCase 
	implements IPageListener
{
	private IWorkbenchWindow fWindow;
	private IWorkspace fWorkspace;
	
	private int eventsReceived = 0;
	final private int OPEN = 0x01;
	final private int CLOSE = 0x02;
	final private int ACTIVATE = 0x04;
	
	public IPageServiceTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		IWorkbench wb = PlatformUI.getWorkbench();
		fWindow = wb.getActiveWorkbenchWindow();
		fWorkspace = ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Adds a page listener.  Then verifies that events are
	 * received.
	 */	
	public void testAddPageListener() throws Throwable {
		// Add listener.
		fWindow.addPageListener(this);
		
		// Open and close page.
		// Verify events are received.
		eventsReceived = 0;
		IWorkbenchPage page = fWindow.openPage(IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		page.close();
		assert(eventsReceived != 0);
		
		// Remove listener.	
		fWindow.removePageListener(this);		
	}
	
	/**
	 * Adds and removes a page listener.  Then verifies that no
	 * further events are received.
	 */
	public void testRemovePageListener() throws Throwable {
		// Add and remove listener.
		fWindow.addPageListener(this);
		fWindow.removePageListener(this);		
		
		// Open and close page.
		// Verify no events are received.
		eventsReceived = 0;
		IWorkbenchPage page = fWindow.openPage(IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		page.close();
		assert(eventsReceived == 0);
	}
	
	/**
	 * Adds two pages and then tests getActivePage.
	 */
	public void testGetActivePage() throws Throwable {
		// Add page.
		IWorkbenchPage page1 = fWindow.openPage(
			IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		assertEquals(fWindow.getActivePage(), page1);
		
		// Add second page.
		IWorkbenchPage page2 = fWindow.openPage(
			IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		assertEquals(fWindow.getActivePage(), page2);
		
		// Set active page.
		fWindow.setActivePage(page1);
		assertEquals(fWindow.getActivePage(), page1);
		fWindow.setActivePage(page2);
		assertEquals(fWindow.getActivePage(), page2);
		
		// Cleanup.
		page1.close();
		page2.close();
	}
	
	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		eventsReceived |= ACTIVATE;
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		eventsReceived |= CLOSE;
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		eventsReceived |= OPEN;
	}

}