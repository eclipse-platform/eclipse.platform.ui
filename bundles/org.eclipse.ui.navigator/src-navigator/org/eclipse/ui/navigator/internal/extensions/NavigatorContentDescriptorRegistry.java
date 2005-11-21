/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorContentDescriptorRegistry extends RegistryReader {

	private static final NavigatorContentDescriptorRegistry INSTANCE = new NavigatorContentDescriptorRegistry();
	private static final NavigatorActivationService NAVIGATOR_ACTIVATION_SERVICE = NavigatorActivationService.getInstance();

	protected static final String NAVIGATOR_CONTENT = "navigatorContent"; //$NON-NLS-1$
	private static boolean isInitialized = false;

	private final Map contentDescriptors = new HashMap();

	private static final Comparator EXTENSION_COMPARATOR = new Comparator() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object lvalue, Object rvalue) {
			return ((NavigatorContentDescriptor) lvalue).getPriority() - ((NavigatorContentDescriptor) rvalue).getPriority();
		}
	};

	private ImageRegistry imageRegistry;

	/**
	 *  
	 */
	public static NavigatorContentDescriptorRegistry getInstance() {
		if (isInitialized)
			return INSTANCE;
		synchronized (INSTANCE) {
			if (!isInitialized) {
				INSTANCE.readRegistry();
				isInitialized = true;
			}
		}
		return INSTANCE;
	}


	/**
	 * @param aPluginId
	 * @param anExtensionPoint
	 */
	public NavigatorContentDescriptorRegistry() {
		super(NavigatorPlugin.PLUGIN_ID, NAVIGATOR_CONTENT);
	}

	/**
	 * Added method.
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param element
	 *            the element to return the best content descriptor for
	 * @return the best content descriptor for the given element.
	 */
	public NavigatorContentDescriptor[] getAllContentDescriptors() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[contentDescriptors.size()];
		contentDescriptors.values().toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, EXTENSION_COMPARATOR);
		return finalDescriptors;
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * @return the best content descriptor for the given element.
	 */
	public Set getEnabledContentDescriptors(Object anElement) {
		Set descriptors = new HashSet();

		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = contentDescriptors.values().iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr.next();

			if (descriptor.isEnabledFor(anElement))
				descriptors.add(descriptor);
		}
		//Collections.sort(descriptors, EXTENSION_COMPARATOR);

		return descriptors;
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param aStructuredSelection
	 *            the element to return the best content descriptor for
	 * @return the best content descriptor for the given element.
	 */
	public Set getEnabledContentDescriptors(IStructuredSelection aStructuredSelection) {
		Set descriptors = new HashSet();

		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = contentDescriptors.values().iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr.next();

			if (descriptor.isEnabledFor(aStructuredSelection))
				descriptors.add(descriptor);
		}
		// Collections.sort(descriptors, EXTENSION_COMPARATOR);

		return descriptors;
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * @return the best content descriptor for the given element.
	 */
	public Set getEnabledContentDescriptors(Object anElement, NavigatorViewerDescriptor aViewerDescriptor) {
		Set descriptors = new HashSet();

		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = contentDescriptors.values().iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr.next();

			if (NAVIGATOR_ACTIVATION_SERVICE.isNavigatorExtensionActive(aViewerDescriptor.getViewerId(), descriptor.getId()) && !aViewerDescriptor.filtersContentDescriptor(descriptor)) {
				if (descriptor.isEnabledFor(anElement))
					descriptors.add(descriptor);
			}
		}
		// Collections.sort(descriptors, EXTENSION_COMPARATOR);

		return descriptors;
	}

	/**
	 * Returns the navigator content descriptor with the given id.
	 * 
	 * @param id
	 *            the id of the content descriptor that should be returned
	 */
	public NavigatorContentDescriptor getContentDescriptor(String id) {
		return (NavigatorContentDescriptor) contentDescriptors.get(id);
	}

	public void removeContentDescriptor(String id) {
		contentDescriptors.remove(id);
	}

	public String getText(String descriptorId) {
		NavigatorContentDescriptor descriptor = getContentDescriptor(descriptorId);
		if (descriptor != null)
			return descriptor.getName();
		return descriptorId;
	}

	public Image getImage(String descriptorId) {
		return retrieveAndStoreImage(descriptorId);
	}

	protected Image retrieveAndStoreImage(String descriptorId) {
		NavigatorContentDescriptor contentDescriptor = getContentDescriptor(descriptorId);

		Image image = null;
		if (contentDescriptor != null) {
			String icon = contentDescriptor.getIcon();
			if (icon != null) {
				image = getImageRegistry().get(icon);
				if (image == null || image.isDisposed()) {
					ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(contentDescriptor.getDeclaringPluginId(), icon);
					if (imageDescriptor != null) {
						image = imageDescriptor.createImage();
						if (image != null)
							getImageRegistry().put(icon, image);
					}
				}
			}
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (NAVIGATOR_CONTENT.equals(element.getName())) {
			try {
				NavigatorContentDescriptor desc = new NavigatorContentDescriptor(element);
				addNavigatorContentDescriptor(desc);
				return true;
			} catch (WorkbenchException e) {
				// log an error since its not safe to open a dialog here
				NavigatorPlugin.log("Unable to create navigator descriptor.", e.getStatus());//$NON-NLS-1$
			}
		} else {
			NavigatorPlugin.log("The tag " + element.getName() + " is not yet supported."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	/**
	 * @param desc
	 */
	private void addNavigatorContentDescriptor(NavigatorContentDescriptor desc) {
		if (desc == null)
			return;
		synchronized (contentDescriptors) {
			contentDescriptors.put(desc.getId(), desc);
		}
	}

	// TODO MDE Should be moved or optimized in someway to minimize the amount of time that image
	// resources are held onto
	private ImageRegistry getImageRegistry() {
		if (imageRegistry == null)
			imageRegistry = new ImageRegistry();
		return imageRegistry;
	}

}
