package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class MockWorkbenchPart implements IWorkbenchPart,
	IExecutableExtension
{	
	protected CallHistory callTrace;
		
	private IPropertyListener myListener;
	private Composite myParent;		
	private IWorkbenchPartSite site;
	private String title;
	
	public MockWorkbenchPart()
	{		
	}
	
	public CallHistory getCallHistory()
	{
		return callTrace;
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
		callTrace.add( this, "createPartControl" );
		Label label = new Label(parent, SWT.NONE);
		label.setText(title);
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		callTrace.add( this, "dispose" );
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
		callTrace.add( this,"setFocus" );
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}
}