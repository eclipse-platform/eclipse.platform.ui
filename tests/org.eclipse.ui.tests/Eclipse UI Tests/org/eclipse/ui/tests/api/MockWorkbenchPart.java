package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public abstract class MockWorkbenchPart implements IWorkbenchPart,
	IExecutableExtension
{	
	public boolean 
		createPartControlCalled = false,
		disposeCalled = false,
		setFocusCalled = false,
		initCalled = false;
		
	private Composite myParent;
	private IPropertyListener myListener;
	private String title;
	private IWorkbenchPartSite site;
	
	public MockWorkbenchPart()
	{
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		title = (String)config.getAttribute("name");
	}
	
	public void setSite(IWorkbenchPartSite site) {
		this.site = site;
	}
	
	public IWorkbenchPartSite getSite() {
		return site;
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
		Label label = new Label(parent, SWT.NONE);
		label.setText(title);
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		disposeCalled = true;
	}

	/**
	 * @see IWorkbenchPart#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @see IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		return null;
	}

	/**
	 * @see IWorkbenchPart#getTitleToolTip()
	 */
	public String getTitleToolTip() {
		return title;
	}

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