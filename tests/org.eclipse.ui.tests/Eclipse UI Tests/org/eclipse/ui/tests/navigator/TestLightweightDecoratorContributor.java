package org.eclipse.ui.tests.navigator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.tests.TestPlugin;

public class TestLightweightDecoratorContributor implements ILightweightLabelDecorator {

	public static TestLightweightDecoratorContributor contributor;
	private Set listeners = new HashSet();
	public static String DECORATOR_SUFFIX = "_SUFFIX";
	public static String DECORATOR_PREFIX= "PREFIX_";
	private ImageDescriptor descriptor;

	public TestLightweightDecoratorContributor() {
		contributor = this;
	}

	/*
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		contributor = null;
		listeners = new HashSet();
	}

	/*
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Refresh the listeners to update the decorators for 
	 * element.
	 */

	public void refreshListeners(Object element) {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, element);
			((ILabelProviderListener) iterator.next()).labelProviderChanged(event);
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getOverlay(java.lang.Object)
	 */
	public ImageDescriptor getOverlay(Object element) {
		Assert.isTrue(element instanceof IResource);
		if(descriptor == null){
			URL source = TestPlugin.getDefault().getDescriptor().getInstallURL();
			try{
				descriptor = ImageDescriptor.createFromURL(new URL(source,"icons/binary_co.gif"));
			}
			catch(MalformedURLException exception){
				return null;
			}
		}
		return descriptor;
	
	}

	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getPrefix(java.lang.Object)
	 */
	public String getPrefix(Object element) {
		return DECORATOR_PREFIX;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getSuffix(java.lang.Object)
	 */
	public String getSuffix(Object element) {
		return DECORATOR_SUFFIX;
	}

}