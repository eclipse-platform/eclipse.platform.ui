/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.tests.util.CallHistory;

public abstract class MockWorkbenchPart implements IWorkbenchPart,
	IExecutableExtension
{	
	protected CallHistory callTrace;
		
	private IPropertyListener myListener;
	private Composite myParent;		
	private IWorkbenchPartSite site;
	private String title;
	private MockSelectionProvider selectionProvider;
	private IConfigurationElement config;
	private Image titleImage;
	
	public MockWorkbenchPart() {		
		callTrace = new CallHistory(this);
		selectionProvider = new MockSelectionProvider();
	}
	
	public CallHistory getCallHistory()
	{
		return callTrace;
	}	

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
		title = (String)config.getAttribute("name");

		// Icon.
		String strIcon = config.getAttribute("icon");//$NON-NLS-1$
		if (strIcon != null) {
			try {
				IPluginDescriptor pd = config.getDeclaringExtension()
					.getDeclaringPluginDescriptor();
				URL fullPathString = new URL(pd.getInstallURL(), strIcon);
				ImageDescriptor imageDesc = ImageDescriptor.createFromURL(fullPathString);
				titleImage = imageDesc.createImage();
			} catch (MalformedURLException e) {
			}
		}
	}

	public IConfigurationElement getConfig() {
		return config;
	}
		
	public void setSite(IWorkbenchPartSite site) {
		this.site = site;
		site.setSelectionProvider(selectionProvider);
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
		callTrace.add("createPartControl" );
		Label label = new Label(parent, SWT.NONE);
		label.setText(title);
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		callTrace.add("dispose" );
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
		return titleImage;
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
		callTrace.add("setFocus" );
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}
	
	/**
	 * Fires a selection out.
	 */
	public void fireSelection() {
		selectionProvider.fireSelection();
	}
}