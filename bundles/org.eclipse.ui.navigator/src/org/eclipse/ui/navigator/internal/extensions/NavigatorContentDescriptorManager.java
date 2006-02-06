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
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.VisibilityAssistant;
import org.eclipse.ui.navigator.internal.VisibilityAssistant.VisibilityListener;
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

	private final Map firstClassDescriptors = new HashMap();

	private final Map allDescriptors = new HashMap();
	
	private class EvaluationCache implements VisibilityListener {
		 
		private final Map evaluations = new WeakHashMap(); 

		public EvaluationCache(VisibilityAssistant anAssistant) {  
			anAssistant.addListener(this);
		}

		protected final Set getDescriptors(Object anElement) {
			return (Set) evaluations.get(anElement);
		}

		protected final void setDescriptors(Object anElement, Set theDescriptors) {
			evaluations.put(anElement, theDescriptors);
		}
  
		/* (non-Javadoc)
		 * @see org.eclipse.ui.navigator.internal.VisibilityAssistant.VisibilityListener#onVisibilityOrActivationChange()
		 */
		public void onVisibilityOrActivationChange() {
			evaluations.clear();		
		} 
	} 
	
	/* Map of (VisibilityAssistant, EvaluationCache)-pairs */
	private final Map cachedTriggerPointEvaluations = new WeakHashMap();
	
	/* Map of (VisibilityAssistant, EvaluationCache)-pairs */
	private final Map cachedPossibleChildrenEvaluations = new WeakHashMap();

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

	private final Set overridingDescriptors = new HashSet();

	private final Set firstClassDescriptorsSet = new HashSet();

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
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[firstClassDescriptors
				.size()];
		allDescriptors.values().toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, EXTENSION_COMPARATOR);
		return finalDescriptors;
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * 
	 * @param aVisibilityAssistant
	 *            The relevant viewer assistant; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set findDescriptorsForTriggerPoint(Object anElement,
			VisibilityAssistant aVisibilityAssistant) {
		EvaluationCache cache = getEvaluationCache(cachedTriggerPointEvaluations, aVisibilityAssistant);
		if(cache.getDescriptors(anElement) != null)
			return cache.getDescriptors(anElement);
		
		Set descriptors = new HashSet();

		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = firstClassDescriptors.values()
				.iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			if (aVisibilityAssistant.isActive(descriptor)
					&& aVisibilityAssistant.isVisible(descriptor)
					&& descriptor.isTriggerPoint(anElement))
				descriptors.add(descriptor);
		}
		
		cache.setDescriptors(anElement, descriptors);

		return descriptors;
	}

	
	private EvaluationCache getEvaluationCache(Map anEvaluationMap, VisibilityAssistant aVisibilityAssistant) { 
		EvaluationCache c = (EvaluationCache) anEvaluationMap.get(aVisibilityAssistant);
		if(c == null)
			anEvaluationMap.put(aVisibilityAssistant, c = new EvaluationCache(aVisibilityAssistant));
		return c;
		
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * 
	 * @param aVisibilityAssistant
	 *            The relevant viewer assistant; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set findDescriptorsForPossibleChild(Object anElement,
			VisibilityAssistant aVisibilityAssistant) {
		

		EvaluationCache cache = getEvaluationCache(cachedPossibleChildrenEvaluations, aVisibilityAssistant);
		if(cache.getDescriptors(anElement) != null)
			return cache.getDescriptors(anElement);
		
		Set descriptors = new HashSet();
		addDescriptorsForPossibleChild(anElement, firstClassDescriptorsSet,
				aVisibilityAssistant, descriptors); 
		
		cache.setDescriptors(anElement, descriptors);

		return descriptors;
	}

	private boolean addDescriptorsForPossibleChild(Object anElement,
			Set theChildDescriptors, VisibilityAssistant aVisibilityAssistant,
			Set theFoundDescriptors) {
		int initialSize = theFoundDescriptors.size();

		NavigatorContentDescriptor descriptor;
		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = theChildDescriptors.iterator(); contentDescriptorsItr
				.hasNext();) {
			descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			boolean isApplicable = aVisibilityAssistant.isActive(descriptor)
					&& aVisibilityAssistant.isVisible(descriptor)
					&& descriptor.isPossibleChild(anElement);

			if (descriptor.hasOverridingExtensions()) {

				boolean isOverridden = addDescriptorsForPossibleChild(anElement,
						descriptor.getOverriddingExtensions(),
						aVisibilityAssistant, theFoundDescriptors);
				
				if (!isOverridden && isApplicable)
					theFoundDescriptors.add(descriptor);

			} else if (isApplicable)
				theFoundDescriptors.add(descriptor);

		}
		return initialSize < theFoundDescriptors.size();

	}

	/**
	 * Returns the navigator content descriptor with the given id.
	 * 
	 * @param id
	 *            The id of the content descriptor that should be returned
	 * @return The content descriptor of the given id
	 */
	public NavigatorContentDescriptor getContentDescriptor(String id) {
		return (NavigatorContentDescriptor) allDescriptors.get(id);
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
									.getContribution().getPluginId(), icon);
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
		synchronized (firstClassDescriptors) {
			if (firstClassDescriptors.containsKey(desc.getId())) {
				NavigatorPlugin
						.logError(
								0,
								"An extension already exists with id \"" + desc.getId() + "\".", null); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (desc.getSuppressedExtensionId() == null) {
					firstClassDescriptors.put(desc.getId(), desc);
					firstClassDescriptorsSet.add(desc);
				} else {
					overridingDescriptors.add(desc);
				}
				allDescriptors.put(desc.getId(), desc);
			}
		}
	}

	/**
	 * 
	 */
	private void computeOverrides() {
		if (overridingDescriptors.size() > 0) {
			NavigatorContentDescriptor descriptor;
			NavigatorContentDescriptor overriddenDescriptor;
			for (Iterator overridingIterator = overridingDescriptors.iterator(); overridingIterator
					.hasNext();) {
				descriptor = (NavigatorContentDescriptor) overridingIterator
						.next();
				overriddenDescriptor = (NavigatorContentDescriptor) firstClassDescriptors
						.get(descriptor.getSuppressedExtensionId());
				if (overriddenDescriptor != null) {

					/*
					 * add the descriptor as an overriding extension for its
					 * suppressed extension
					 */
					overriddenDescriptor.getOverriddingExtensions().add(
							descriptor);
					descriptor.setOverriddenDescriptor(overriddenDescriptor);
					/*
					 * the always policy implies this is also a top-level
					 * extension
					 */
					if (descriptor.getOverridePolicy() == OverridePolicy.InvokeAlwaysRegardlessOfSuppressedExt)
						firstClassDescriptors.put(descriptor.getId(),
								descriptor);

				} else {
					NavigatorPlugin.logError(0,
							"Invalid suppressedExtensionId (\"" //$NON-NLS-1$
									+ descriptor.getSuppressedExtensionId()
									+ "\" specified from " //$NON-NLS-1$
									+ descriptor.getContribution()
											.getPluginId()
									+ ". No extension with matching id found.", //$NON-NLS-1$
							null);
				}
			}
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

	private class NavigatorContentDescriptorRegistry extends
			NavigatorContentRegistryReader {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.internal.extensions.RegistryReader#readRegistry()
		 */
		public void readRegistry() {
			super.readRegistry();
			computeOverrides();
		}

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
