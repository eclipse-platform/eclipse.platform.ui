package org.eclipse.ui.tests.api;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {
	private static String TITLE = "Mock ViewPart";	

	public MockViewPart()
	{
	}
	
	public String getTitle()
	{
		return TITLE;
	}
	
	public Image getTitleImage()
	{
		return null;	
	}

	public String getTitleToolTip() 
	{
		return null;
	}	
	/**
	 * @see MockWorkbenchPart#getSite()
	 */
	public IWorkbenchPartSite getSite() {
		return null;
	}

	/**
	 * @see IViewPart#getViewSite()
	 */
	public IViewSite getViewSite() {
		return null;
	}

	/**
	 * @see IViewPart#init(IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		initCalled = true;
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		initCalled = true;
	}

	/**
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
	}
}

