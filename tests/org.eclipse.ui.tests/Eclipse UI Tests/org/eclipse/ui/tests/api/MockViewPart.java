package org.eclipse.ui.tests.api;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {	
	public static String ID = "org.eclipse.ui.tests.api.MockViewPart";
	public static String ID2 = ID + "2";
	public static String ID3 = ID + "3";
	public static String NAME = "Mock View 1";

	public MockViewPart()
	{
		super();
	}
	
	/**
	 * @see IViewPart#getViewSite()
	 */
	public IViewSite getViewSite() {
		return (IViewSite)getSite();
	}

	/**
	 * @see IViewPart#init(IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
		callTrace.add("init" );		
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
		callTrace.add("init" );
	}

	/**
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
	}
}

