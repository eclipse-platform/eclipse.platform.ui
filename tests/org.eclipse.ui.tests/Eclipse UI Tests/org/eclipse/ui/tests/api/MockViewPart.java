package org.eclipse.ui.tests.api;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {
	
	public static final String ID_MOCK_VIEW_1 = "org.eclipse.ui.tests.api.mockView1";
	public static final String ID_MOCK_VIEW_2 = "org.eclipse.ui.tests.api.mockView2";
	public static final String ID_MOCK_VIEW_3 = "org.eclipse.ui.tests.api.mockView3";
	public static final String ID_MOCK_VIEW_4 = "org.eclipse.ui.tests.api.mockView4";
	public static final String ID_MOCK_VIEW_5 = "org.eclipse.ui.tests.api.mockView5";
	
	public MockViewPart() {
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
		initCalled = true;
		setSite(site);
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		initCalled = true;
		setSite(site);
	}

	/**
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
	}
}

