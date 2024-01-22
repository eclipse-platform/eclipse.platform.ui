/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The registry of action set extensions.
 */
public class ActionSetRegistry implements IExtensionChangeHandler {

	/**
	 * @since 3.1
	 */
	private static class ActionSetPartAssociation {
		public ActionSetPartAssociation(String partId, String actionSetId) {
			this.partId = partId;
			this.actionSetId = actionSetId;
		}

		String partId;
		String actionSetId;
	}

	private ArrayList<ActionSetDescriptor> children = new ArrayList<>();

	private Map<String, ArrayList<String>> mapPartToActionSetIds = new HashMap<>();

	private Map<String, ArrayList<IActionSetDescriptor>> mapPartToActionSets = new HashMap<>();

	private IContextService contextService;

	/**
	 * Creates the action set registry.
	 */
	public ActionSetRegistry() {
		contextService = PlatformUI.getWorkbench().getService(IContextService.class);
		PlatformUI.getWorkbench().getExtensionTracker().registerHandler(this,
				ExtensionTracker.createExtensionPointFilter(new IExtensionPoint[] { getActionSetExtensionPoint(),
						getActionSetPartAssociationExtensionPoint() }));
		readFromRegistry();
	}

	/**
	 * Return the action set part association extension point.
	 *
	 * @return the action set part association extension point
	 * @since 3.1
	 */
	private IExtensionPoint getActionSetPartAssociationExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTION_SET_PART_ASSOCIATIONS);
	}

	/**
	 * Return the action set extension point.
	 *
	 * @return the action set extension point
	 * @since 3.1
	 */
	private IExtensionPoint getActionSetExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTION_SETS);
	}

	/**
	 * Adds an action set.
	 */
	private void addActionSet(ActionSetDescriptor desc) {
		children.add(desc);
		Context actionSetContext = contextService.getContext(desc.getId());
		if (!actionSetContext.isDefined()) {
			actionSetContext.define(desc.getLabel(), desc.getDescription(), "org.eclipse.ui.contexts.actionSet"); //$NON-NLS-1$
		}
	}

	/**
	 * Remove the action set.
	 */
	private void removeActionSet(IActionSetDescriptor desc) {
		Context actionSetContext = contextService.getContext(desc.getId());
		if (actionSetContext.isDefined()) {
			actionSetContext.undefine();
		}
		children.remove(desc);
	}

	/**
	 * Adds an association between an action set an a part.
	 */
	private Object addAssociation(String actionSetId, String partId) {
		// get the action set ids for this part
		ArrayList<String> actionSets = mapPartToActionSetIds.get(partId);
		if (actionSets == null) {
			actionSets = new ArrayList<>();
			mapPartToActionSetIds.put(partId, actionSets);
		}
		actionSets.add(actionSetId);

		return new ActionSetPartAssociation(partId, actionSetId);
	}

	/**
	 * Finds and returns the registered action set with the given id.
	 *
	 * @param id the action set id
	 * @return the action set, or <code>null</code> if none
	 * @see IActionSetDescriptor#getId
	 */
	public IActionSetDescriptor findActionSet(String id) {

		for (IActionSetDescriptor desc : children) {
			if (desc.getId().equals(id)) {
				return desc;
			}
		}
		return null;
	}

	/**
	 * Returns a list of the action sets known to the workbench.
	 *
	 * @return a list of action sets
	 */
	public IActionSetDescriptor[] getActionSets() {
		return children.toArray(new IActionSetDescriptor[children.size()]);
	}

	/**
	 * Returns a list of the action sets associated with the given part id.
	 *
	 * @param partId the part id
	 * @return a list of action sets
	 */
	public IActionSetDescriptor[] getActionSetsFor(String partId) {
		// check the resolved map first
		ArrayList<IActionSetDescriptor> actionSets = mapPartToActionSets.get(partId);
		if (actionSets != null) {
			return actionSets.toArray(new IActionSetDescriptor[actionSets.size()]);
		}

		// get the action set ids for this part
		ArrayList<String> actionSetIds = mapPartToActionSetIds.get(partId);
		if (actionSetIds == null) {
			return new IActionSetDescriptor[0];
		}

		// resolve to action sets
		actionSets = new ArrayList<>(actionSetIds.size());

		for (String actionSetId : actionSetIds) {

			IActionSetDescriptor actionSet = findActionSet(actionSetId);
			if (actionSet != null) {
				actionSets.add(actionSet);
			} else {
				WorkbenchPlugin.log("Unable to associate action set with part: " + //$NON-NLS-1$
						partId + ". Action set " + actionSetId + " not found."); //$NON-NLS-2$ //$NON-NLS-1$
			}
		}

		mapPartToActionSets.put(partId, actionSets);

		return actionSets.toArray(new IActionSetDescriptor[actionSets.size()]);
	}

	/**
	 * Reads the registry.
	 */
	private void readFromRegistry() {
		for (IExtension extension : getActionSetExtensionPoint().getExtensions()) {
			addActionSets(PlatformUI.getWorkbench().getExtensionTracker(), extension);
		}

		for (IExtension extension : getActionSetPartAssociationExtensionPoint().getExtensions()) {
			addActionSetPartAssociations(PlatformUI.getWorkbench().getExtensionTracker(), extension);
		}
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		String extensionPointUniqueIdentifier = extension.getExtensionPointUniqueIdentifier();
		if (extensionPointUniqueIdentifier.equals(getActionSetExtensionPoint().getUniqueIdentifier())) {
			addActionSets(tracker, extension);
		} else if (extensionPointUniqueIdentifier
				.equals(getActionSetPartAssociationExtensionPoint().getUniqueIdentifier())) {
			addActionSetPartAssociations(tracker, extension);
		}
	}

	private void addActionSetPartAssociations(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement element : extension.getConfigurationElements()) {
			if (element.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET_PART_ASSOCIATION)) {
				String actionSetId = element.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
				for (IConfigurationElement child : element.getChildren()) {
					if (child.getName().equals(IWorkbenchRegistryConstants.TAG_PART)) {
						String partId = child.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
						if (partId != null) {
							Object trackingObject = addAssociation(actionSetId, partId);
							if (trackingObject != null) {
								tracker.registerObject(extension, trackingObject, IExtensionTracker.REF_STRONG);

							}

						}
					} else {
						WorkbenchPlugin.log("Unable to process element: " + //$NON-NLS-1$
								child.getName() + " in action set part associations extension: " + //$NON-NLS-1$
								extension.getUniqueIdentifier());
					}
				}
			}
		}

		// TODO: optimize
		mapPartToActionSets.clear();
	}

	private void addActionSets(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement element : extension.getConfigurationElements()) {
			if (element.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
				try {
					ActionSetDescriptor desc = new ActionSetDescriptor(element);
					addActionSet(desc);
					tracker.registerObject(extension, desc, IExtensionTracker.REF_WEAK);

				} catch (CoreException e) {
					// log an error since its not safe to open a dialog here
					WorkbenchPlugin.log("Unable to create action set descriptor.", e.getStatus());//$NON-NLS-1$
				}
			}
		}

		// TODO: optimize
		mapPartToActionSets.clear();
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		String extensionPointUniqueIdentifier = extension.getExtensionPointUniqueIdentifier();
		if (extensionPointUniqueIdentifier.equals(getActionSetExtensionPoint().getUniqueIdentifier())) {
			removeActionSets(objects);
		} else if (extensionPointUniqueIdentifier
				.equals(getActionSetPartAssociationExtensionPoint().getUniqueIdentifier())) {
			removeActionSetPartAssociations(objects);
		}
	}

	private void removeActionSetPartAssociations(Object[] objects) {
		for (Object object : objects) {
			if (object instanceof ActionSetPartAssociation) {
				ActionSetPartAssociation association = (ActionSetPartAssociation) object;
				String actionSetId = association.actionSetId;
				ArrayList<String> actionSets = mapPartToActionSetIds.get(association.partId);
				if (actionSets == null) {
					return;
				}
				actionSets.remove(actionSetId);
				if (actionSets.isEmpty()) {
					mapPartToActionSetIds.remove(association.partId);
				}
			}
		}
		// TODO: optimize
		mapPartToActionSets.clear();

	}

	private void removeActionSets(Object[] objects) {
		for (Object object : objects) {
			if (object instanceof IActionSetDescriptor) {
				IActionSetDescriptor desc = (IActionSetDescriptor) object;
				removeActionSet(desc);

				// now clean up the part associations
				// TODO: this is expensive. We should consider another map from
				// actionsets
				// to parts.
				for (Iterator<ArrayList<String>> j = mapPartToActionSetIds.values().iterator(); j.hasNext();) {
					ArrayList<String> list = j.next();
					list.remove(desc.getId());
					if (list.isEmpty()) {
						j.remove();
					}
				}
			}
		}
		// TODO: optimize
		mapPartToActionSets.clear();
	}
}
