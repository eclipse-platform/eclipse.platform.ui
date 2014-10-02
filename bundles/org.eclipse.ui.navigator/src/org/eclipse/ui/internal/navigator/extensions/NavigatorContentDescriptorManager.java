/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Bug 349224 Navigator content provider "appearsBefore" creates hard reference to named id - paul.fullbright@oracle.com
 * C. Sean Young <csyoung@google.com> - Bug 436645
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.Policy;
import org.eclipse.ui.internal.navigator.VisibilityAssistant;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.OverridePolicy;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @since 3.2
 */
public class NavigatorContentDescriptorManager {

	private static final NavigatorContentDescriptorManager INSTANCE = new NavigatorContentDescriptorManager();

	private final Map<String, NavigatorContentDescriptor> firstClassDescriptorsMap = new HashMap<String, NavigatorContentDescriptor>();

	private final Map<String, NavigatorContentDescriptor> allDescriptors = new HashMap<String, NavigatorContentDescriptor>();

	private ImageRegistry imageRegistry;

	private final Set<NavigatorContentDescriptor> overridingDescriptors = new HashSet<NavigatorContentDescriptor>();

	private final Set<NavigatorContentDescriptor> saveablesProviderDescriptors = new HashSet<NavigatorContentDescriptor>();

	private final Set<NavigatorContentDescriptor> sortOnlyDescriptors = new HashSet<NavigatorContentDescriptor>();

	private final Set<NavigatorContentDescriptor> firstClassDescriptorsSet = new HashSet<NavigatorContentDescriptor>();

	/**
	 * @return the singleton instance of the manager
	 */
	public static NavigatorContentDescriptorManager getInstance() {
		return INSTANCE;
	}

	private NavigatorContentDescriptorManager() {
		new NavigatorContentDescriptorRegistry().readRegistry();
	}

	/**
	 *
	 * @return Returns all content descriptor(s).
	 */
	public NavigatorContentDescriptor[] getAllContentDescriptors() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[allDescriptors
				.size()];
		finalDescriptors = allDescriptors.values().toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, ExtensionSequenceNumberComparator.INSTANCE);
		return finalDescriptors;
	}

	/**
	 *
	 * @return Returns all content descriptors that provide saveables.
	 */
	public NavigatorContentDescriptor[] getContentDescriptorsWithSaveables() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[saveablesProviderDescriptors
				.size()];
		saveablesProviderDescriptors.toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, ExtensionSequenceNumberComparator.INSTANCE);
		return finalDescriptors;
	}

	/**
	 *
	 * @return Returns all content descriptors that are sort only
	 */
	public NavigatorContentDescriptor[] getSortOnlyContentDescriptors() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[sortOnlyDescriptors
				.size()];
		sortOnlyDescriptors.toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, ExtensionSequenceNumberComparator.INSTANCE);
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
	 * @param considerOverrides
	 * @return the best content descriptor for the given element.
	 */
	public Set<NavigatorContentDescriptor> findDescriptorsForTriggerPoint(Object anElement,
			VisibilityAssistant aVisibilityAssistant, boolean considerOverrides) {
		return findDescriptors(anElement, aVisibilityAssistant, considerOverrides, !POSSIBLE_CHILD);
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
	 * @param toComputeOverrides
	 * @return the best content descriptor for the given element.
	 */
	public Set<NavigatorContentDescriptor> findDescriptorsForPossibleChild(Object anElement,
			VisibilityAssistant aVisibilityAssistant, boolean toComputeOverrides) {
		return findDescriptors(anElement, aVisibilityAssistant, toComputeOverrides, POSSIBLE_CHILD);
	}

	private static final boolean POSSIBLE_CHILD = true;


	private Set<NavigatorContentDescriptor> findDescriptors(Object anElement,
			VisibilityAssistant aVisibilityAssistant, boolean considerOverrides, boolean possibleChild) {

		Set<NavigatorContentDescriptor> descriptors = new TreeSet<NavigatorContentDescriptor>(ExtensionSequenceNumberComparator.INSTANCE);

		if (considerOverrides) {
			addDescriptorsConsideringOverrides(anElement, firstClassDescriptorsSet, aVisibilityAssistant, descriptors, possibleChild);
			if (Policy.DEBUG_RESOLUTION) {
				System.out.println("Find descriptors for: " + Policy.getObjectString(anElement) + //$NON-NLS-1$
						": " + descriptors); //$NON-NLS-1$
			}
		} else {

			/* Find other ContentProviders which enable for this object */
			for (Iterator<NavigatorContentDescriptor> contentDescriptorsItr = firstClassDescriptorsSet.iterator(); contentDescriptorsItr.hasNext();) {
				NavigatorContentDescriptor descriptor = contentDescriptorsItr.next();

				if (aVisibilityAssistant.isActive(descriptor) && aVisibilityAssistant.isVisible(descriptor)
						&& (possibleChild ? descriptor.isPossibleChild(anElement) : descriptor.isTriggerPoint(anElement))) {
					descriptors.add(descriptor);
				}
			}
		}

		return descriptors;
	}

	private boolean addDescriptorsConsideringOverrides(Object anElement,
			Set<NavigatorContentDescriptor> theChildDescriptors, VisibilityAssistant aVisibilityAssistant,
			Set<NavigatorContentDescriptor> theFoundDescriptors, boolean possibleChild) {
		int initialSize = theFoundDescriptors.size();

		NavigatorContentDescriptor descriptor;
		/* Find other ContentProviders which enable for this object */
		for (Iterator<NavigatorContentDescriptor> contentDescriptorsItr = theChildDescriptors.iterator(); contentDescriptorsItr
				.hasNext();) {
			descriptor = contentDescriptorsItr
					.next();

			boolean isApplicable = aVisibilityAssistant.isActive(descriptor)
					&& aVisibilityAssistant.isVisible(descriptor)
					&& (possibleChild ? descriptor.isPossibleChild(anElement) : descriptor.isTriggerPoint(anElement));

			if (descriptor.hasOverridingExtensions()) {

				boolean isOverridden;

				Set<NavigatorContentDescriptor> overridingDescriptors = new TreeSet<NavigatorContentDescriptor>(ExtensionSequenceNumberComparator.INSTANCE);
				isOverridden = addDescriptorsConsideringOverrides(anElement, descriptor.getOverriddingExtensions(),
						aVisibilityAssistant, overridingDescriptors, possibleChild);

				if (!isOverridden && isApplicable) {
					theFoundDescriptors.add(descriptor);
				} else if (isOverridden) {
					theFoundDescriptors.addAll(overridingDescriptors);
				}

			} else if (isApplicable) {
				theFoundDescriptors.add(descriptor);
			}

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
		return allDescriptors.get(id);
	}

	/**
	 *
	 * @param descriptorId
	 *            The unique id of a particular descriptor
	 * @return The name (value of the 'name' attribute) of the given descriptor
	 */
	public String getText(String descriptorId) {
		INavigatorContentDescriptor descriptor = getContentDescriptor(descriptorId);
		if (descriptor != null) {
			return descriptor.getName();
		}
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
						if (image != null) {
							getImageRegistry().put(icon, image);
						}
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
		if (desc == null) {
			return;
		}
		synchronized (firstClassDescriptorsMap) {
			if (firstClassDescriptorsMap.containsKey(desc.getId())) {
				NavigatorPlugin
						.logError(
								0,
								"An extension already exists with id \"" + desc.getId() + "\".", null); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (desc.getSuppressedExtensionId() == null) {
					firstClassDescriptorsMap.put(desc.getId(), desc);
					firstClassDescriptorsSet.add(desc);
					if (Policy.DEBUG_EXTENSION_SETUP) {
						System.out.println("First class descriptor: " + desc); //$NON-NLS-1$
					}
				} else {
					overridingDescriptors.add(desc);
					if (Policy.DEBUG_EXTENSION_SETUP) {
						System.out.println("Overriding descriptor: " + desc); //$NON-NLS-1$
					}
				}
				allDescriptors.put(desc.getId(), desc);
				if (desc.hasSaveablesProvider()) {
					saveablesProviderDescriptors.add(desc);
					if (Policy.DEBUG_EXTENSION_SETUP) {
						System.out.println("Saveables provider descriptor: " + desc); //$NON-NLS-1$
					}
				}
				if (desc.isSortOnly()) {
					sortOnlyDescriptors.add(desc);
					if (Policy.DEBUG_EXTENSION_SETUP) {
						System.out.println("SortOnly descriptor: " + desc); //$NON-NLS-1$
					}
				}
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
			for (Iterator<NavigatorContentDescriptor> overridingIterator = overridingDescriptors.iterator(); overridingIterator
					.hasNext();) {
				descriptor = overridingIterator
						.next();
				overriddenDescriptor = allDescriptors
						.get(descriptor.getSuppressedExtensionId());
				if (overriddenDescriptor != null) {

					if (Policy.DEBUG_EXTENSION_SETUP) {
						System.out.println(descriptor + " overrides: " + overriddenDescriptor); //$NON-NLS-1$
					}
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
					if (descriptor.getOverridePolicy() == OverridePolicy.InvokeAlwaysRegardlessOfSuppressedExt) {
						if (Policy.DEBUG_EXTENSION_SETUP) {
							System.out.println(descriptor + " is first class"); //$NON-NLS-1$
						}
						firstClassDescriptorsMap.put(descriptor.getId(),
								descriptor);
						firstClassDescriptorsSet.add(descriptor);
					}

				} else {
					String message =
							"Invalid suppressedExtensionId \"" //$NON-NLS-1$
									+ descriptor.getSuppressedExtensionId()
									+ "\" specified from \"" //$NON-NLS-1$
									+ descriptor.getId() + "\" in \"" + descriptor.getContribution() //$NON-NLS-1$
											.getPluginId()
									+ "\". No extension with matching id found."; //$NON-NLS-1$
					if (Policy.DEBUG_EXTENSION_SETUP) {
						System.out.println("Error: " + message); //$NON-NLS-1$
					}
					NavigatorPlugin.logError(0, message, null);
				}
			}
		}
	}

	private int findId(List<NavigatorContentDescriptor> list, String id) {
		for (int i = 0, len = list.size(); i < len; i++) {
			NavigatorContentDescriptor desc = list.get(i);
			if (desc.getId().equals(id))
				return i;
		}
		// Do not require content descriptor to exist in workspace
		NavigatorPlugin.log(IStatus.WARNING, 0,
				"Can't find Navigator Content Descriptor with id: " + id, null); //$NON-NLS-1$
		return -1;
	}

	private void computeSequenceNumbers() {
		NavigatorContentDescriptor[] descs = getAllContentDescriptors();

		LinkedList<NavigatorContentDescriptor> list = new LinkedList<NavigatorContentDescriptor>();
		for (int i = 0; i < descs.length; i++) {
			list.add(descs[i]);
		}

		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0, len = list.size(); i < len; i++) {
				NavigatorContentDescriptor desc = list.get(i);
				if (desc.getAppearsBeforeId() != null) {
					int beforeInd = findId(list, desc.getAppearsBeforeId());
					if (beforeInd >= 0 && beforeInd < i) {
						list.add(beforeInd, desc);
						list.remove(i + 1);
						changed = true;
					}
				}
			}
		}

		for (int i = 0, len = list.size(); i < len; i++) {
			NavigatorContentDescriptor desc = list.get(i);
			desc.setSequenceNumber(i);
			if (Policy.DEBUG_EXTENSION_SETUP) {
				System.out.println("Descriptors by sequence: " + desc); //$NON-NLS-1$
			}
		}
	}

	private ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	private class NavigatorContentDescriptorRegistry extends
			NavigatorContentRegistryReader {

		@Override
		public void readRegistry() {
			super.readRegistry();
			computeSequenceNumbers();
			computeOverrides();
		}

		@Override
		protected boolean readElement(final IConfigurationElement anElement) {
			if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {
				SafeRunner.run(new NavigatorSafeRunnable(anElement) {
					@Override
					public void run() throws Exception {
						addNavigatorContentDescriptor(new NavigatorContentDescriptor(anElement));
					}
				});
			}
			return super.readElement(anElement);
		}
	}

}
