package org.eclipse.ui.tests.api;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class MockWorkbenchPart implements IWorkbenchPart {	
	public boolean 
		createPartControlCalled = false,
		disposeCalled = false,
		setFocusCalled = false,
		initCalled = false;
		
	private IPropertyListener myListener;
	private Composite myParent;
	
	public MockWorkbenchPart()
	{
	}

	/**
	 * @see IWorkbenchPart#addPropertyListener(IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		myListener = listener;	
	}

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		myParent = parent;
		createPartControlCalled = true;
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		disposeCalled = true;
	}

	/**
	 * @see IWorkbenchPart#getSite()
	 */
	public abstract IWorkbenchPartSite getSite();

	/**
	 * @see IWorkbenchPart#getTitle()
	 */
	public abstract String getTitle();

	/**
	 * @see IWorkbenchPart#getTitleImage()
	 */
	public abstract Image getTitleImage();

	/**
	 * @see IWorkbenchPart#getTitleToolTip()
	 */
	public abstract String getTitleToolTip();

	/**
	 * @see IWorkbenchPart#removePropertyListener(IPropertyListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		myListener = null;
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		setFocusCalled = true;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}
}