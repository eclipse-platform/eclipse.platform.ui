package org.eclipse.ui.tests.api;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {	
	public static String ID = "org.eclipse.ui.tests.api.MockViewPart";
	public static String ID2 = ID + "2";
	public static String ID3 = ID + "3";

	public MockViewPart()
	{
		super();
		callTrace = new CallHistory();
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
		callTrace.add( this, "init" );		
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
		callTrace.add( this, "init" );
	}

	/**
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
	}
}

