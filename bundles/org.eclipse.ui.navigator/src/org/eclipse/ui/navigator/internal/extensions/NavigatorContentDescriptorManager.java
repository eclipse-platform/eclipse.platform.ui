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
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.Utilities;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorContentDescriptorManager {

	private static final NavigatorContentDescriptorManager INSTANCE = new NavigatorContentDescriptorManager();

	private static boolean isInitialized = false;

	private final Map contentDescriptors = new HashMap();

	private static final Comparator EXTENSION_COMPARATOR = new Comparator() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object lvalue, Object rvalue) {
			return ((INavigatorContentDescriptor) lvalue).getPriority()
					- ((INavigatorContentDescriptor) rvalue).getPriority();
		}
	};

	private ImageRegistry imageRegistry;

	/**
	 * @return the singleton instance of the registry
	 */
	public static NavigatorContentDescriptorManager getInstance() {
		if (isInitialized)
			return INSTANCE;
		synchronized (INSTANCE) {
			if (!isInitialized) {
				INSTANCE.init();
				isInitialized = true;
			}
		}
		return INSTANCE;
	}

	private void init() {
		new NavigatorContentDescriptorRegistry().readRegistry();
	}

	/**
	 * 
	 * @return Returns all content descriptor(s).
	 */
	public NavigatorContentDescriptor[] getAllContentDescriptors() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[contentDescriptors
				.size()];
		contentDescriptors.values().toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, EXTENSION_COMPARATOR);
		return finalDescriptors;
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param aStructuredSelection
	 *            The element to return the best content descriptor for
	 * @param aViewerDescriptor
	 *            The relevant viewer descriptor; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set getEnabledContentDescriptors(
			IStructuredSelection aStructuredSelection,
			INavigatorViewerDescriptor aViewerDescriptor) {
		Set descriptors = new HashSet();

		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = contentDescriptors.values()
				.iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			if (Utilities.isApplicable(aViewerDescriptor, descriptor,
					aStructuredSelection))
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
	 * 
	 * @param aViewerDescriptor
	 *            The relevant viewer descriptor; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set getEnabledContentDescriptors(Object anElement,
			INavigatorViewerDescriptor aViewerDescriptor) {
		Set descriptors = new HashSet();

		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = contentDescriptors.values()
				.iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			if (Utilities
					.isApplicable(aViewerDescriptor, descriptor, anElement))
				descriptors.add(descriptor);
		}
		// Collections.sort(descriptors, EXTENSION_COMPARATOR);

		return descriptors;
	}

	/**
	 * Returns the navigator content descriptor with the given id.
	 * 
	 * @param id
	 *            The id of the content descriptor that should be returned
	 * @return The content descriptor of the given id
	 */
	public NavigatorContentDescriptor getContentDescriptor(String id) {
		return (NavigatorContentDescriptor) contentDescriptors.get(id);
	}

	/**
	 * 
	 * @param id
	 *            The id of the descriptor to remove from the manager.
	 */
	public void removeContentDescriptor(String id) {
		contentDescriptors.remove(id);
	}

	/**
	 * 
	 * @param descriptorId
	 *            The unique id of a particular descriptor
	 * @return The name (value of the 'name' attribute) of the given descriptor
	 */
	public String getText(String descriptorId) {
		INavigatorContentDescriptor descriptor = getContentDescriptor(descriptorId);
		if (descriptor != null)
			return descriptor.getName();
		return descriptorId;
	}

	/**
	 * 
	 * @param descriptorId
	 *            The unique id of a particular descriptor
	 * @return The image (corresponding to the value of the 'icon' attribute) of
	 *         the given descriptor
	 */
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
					ImageDescriptor imageDescriptor = AbstractUIPlugin
							.imageDescriptorFromPlugin(contentDescriptor
									.getDeclaringPluginId(), icon);
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

	// TODO MDE Should be moved or optimized in someway to minimize the amount
	// of time that image
	// resources are held onto
	private ImageRegistry getImageRegistry() {
		if (imageRegistry == null)
			imageRegistry = new ImageRegistry();
		return imageRegistry;
	}

	private class NavigatorContentDescriptorRegistry extends NavigatorContentRegistryReader {
  
		protected boolean readElement(IConfigurationElement anElement) {
			if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {
				try {
					addNavigatorContentDescriptor(new NavigatorContentDescriptor(
							anElement));

				} catch (WorkbenchException e) {
					// log an error since its not safe to open a dialog here
					NavigatorPlugin
							.log(
									"Unable to create navigator descriptor.", e.getStatus()); //$NON-NLS-1$
				}
			}
			return super.readElement(anElement);
		}
	}

}
