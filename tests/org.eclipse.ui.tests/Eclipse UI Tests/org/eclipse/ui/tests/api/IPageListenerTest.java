package org.eclipse.ui.tests.api;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

/**
 * Tests the PlatformUI class.
 */
public class IPageListenerTest extends TestCase 
	implements IPageListener
{
	private IWorkbenchWindow fWindow;
	private IWorkspace fWorkspace;
	
	private int eventsReceived = 0;
	final private int OPEN = 0x01;
	final private int CLOSE = 0x02;
	final private int ACTIVATE = 0x04;
	private IWorkbenchPage pageMask;
	
	public IPageListenerTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		IWorkbench wb = PlatformUI.getWorkbench();
		fWindow = wb.getActiveWorkbenchWindow();
		fWorkspace = ResourcesPlugin.getWorkspace();
		fWindow.addPageListener(this);
	}
	
	protected void tearDown() {
		fWindow.removePageListener(this);
	}

	/**
	 * Verifies that open and activated events are received
	 * when openPage is performed.
	 */	
	public void testPageOpened() throws Throwable {
		// Test open page.
		eventsReceived = 0;
		IWorkbenchPage page = fWindow.openPage(IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		assertEquals(eventsReceived, OPEN|ACTIVATE);
		
		// Close page.
		page.close();
	}
	
	/**
	 * Verifies that close events are received when a page
	 * is closed.
	 */	
	public void testPageClosed() throws Throwable {
		// Open page.
		IWorkbenchPage page = fWindow.openPage(IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
			
		// Test close page.
		eventsReceived = 0;
		pageMask = page;
		page.close();
		assertEquals(eventsReceived, CLOSE);
	}
	
	/**
	 * Verifies that activate events are received when a page
	 * is activated.
	 */	
	public void testPageActivate() throws Throwable {
		// Add pages.
		IWorkbenchPage page1 = fWindow.openPage(
			IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		IWorkbenchPage page2 = fWindow.openPage(
			IWorkbenchConstants.DEFAULT_LAYOUT_ID,
			fWorkspace);
		
		// Test activation of page 1.
		eventsReceived = 0;
		pageMask = page1;
		fWindow.setActivePage(page1);
		assertEquals(eventsReceived, ACTIVATE);

		// Test activation of page 2.
		eventsReceived = 0;		
		pageMask = page2;
		fWindow.setActivePage(page2);
		assertEquals(eventsReceived, ACTIVATE);
		
		// Cleanup.
		page1.close();
		page2.close();
	}
	
	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask)
			eventsReceived |= ACTIVATE;
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask)
			eventsReceived |= CLOSE;
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask)
			eventsReceived |= OPEN;
	}

}