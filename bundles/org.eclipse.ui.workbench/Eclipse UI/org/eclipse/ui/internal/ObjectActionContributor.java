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
package org.eclipse.ui.internal;


import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class describes the object contribution element within the popup menu
 * action registry.
 */
public class ObjectActionContributor extends PluginActionBuilder implements IObjectActionContributor {
	private static final String ATT_NAME_FILTER = "nameFilter"; //$NON-NLS-1$
	private static final String ATT_ADAPTABLE = "adaptable"; //$NON-NLS-1$
	private static final String P_TRUE = "true"; //$NON-NLS-1$

	private IConfigurationElement config;
	private boolean configRead = false;
	private boolean adaptable = false;
	
	/**
	 * The constructor.
	 */
	public ObjectActionContributor(IConfigurationElement config) {
		this.config = config;
		this.adaptable = P_TRUE.equalsIgnoreCase(config.getAttribute(ATT_ADAPTABLE));
	}

	/* (non-Javadoc)
	 * Method declared on IObjectContributor.
	 */
	public boolean canAdapt() {
		return adaptable;
	}
	
	
	/* (non-Javadoc)
	 * Method declared on IObjectActionContributor.
	 */
	public void contributeObjectActionIdOverrides(List actionIdOverrides) {
		if (!configRead)
			readConfigElement();

		// Easy case out if no actions
		if (currentContribution.actions != null) {
			for (int i = 0; i < currentContribution.actions.size(); i++) {
				ActionDescriptor ad = (ActionDescriptor)currentContribution.actions.get(i);
				String id = ad.getAction().getOverrideActionId();
				if (id != null)
					actionIdOverrides.add(id);
			}
		}
	}

	/**
	 * Contributes actions applicable for the current selection.
	 */
	public boolean contributeObjectActions(IWorkbenchPart part, IMenuManager menu, ISelectionProvider selProv, List actionIdOverrides) {
		if (!configRead)
			readConfigElement();

		// Easy case out if no actions
		if (currentContribution.actions == null)
			return false;
			
		// Get a structured selection.	
		ISelection sel = selProv.getSelection();
		if ((sel == null) || !(sel instanceof IStructuredSelection))
			return false;
		IStructuredSelection selection = (IStructuredSelection) sel;

		// Generate menu.
		for (int i = 0; i < currentContribution.actions.size(); i++) {
			ActionDescriptor ad = (ActionDescriptor)currentContribution.actions.get(i);
			if (!actionIdOverrides.contains(ad.getId())) {
				currentContribution.contributeMenuAction(ad, menu, true);
				// Update action for the current selection and part.
				if (ad.getAction() instanceof ObjectPluginAction) {
					ObjectPluginAction action = (ObjectPluginAction) ad.getAction();
					action.setActivePart(part);
					action.selectionChanged(selection);
				}
			}
		}
		return true;
	}

	/**
	 * Contributes menus applicable for the current selection.
	 */
	public boolean contributeObjectMenus(IMenuManager menu, ISelectionProvider selProv) {
		if (!configRead)
			readConfigElement();

		// Easy case out if no menus
		if (currentContribution.menus == null)
			return false;
			
		// Get a structured selection.	
		ISelection sel = selProv.getSelection();
		if ((sel == null) || !(sel instanceof IStructuredSelection))
			return false;

		// Generate menu.
		for (int i = 0; i < currentContribution.menus.size(); i++) {
			IConfigurationElement menuElement = (IConfigurationElement)currentContribution.menus.get(i);
			currentContribution.contributeMenu(menuElement, menu, true);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
		return new ActionDescriptor(element, ActionDescriptor.T_POPUP);
	}

	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected BasicContribution createContribution() {
		return new ObjectContribution();
	}

	/**
	 * Returns true if name filter is not specified for the contribution
	 * or the current selection matches the filter.
	 */
	public boolean isApplicableTo(Object object) {
		if (!configRead)
			readConfigElement();

		if (!testName(object))
			return false;

		return ((ObjectContribution)currentContribution).isApplicableTo(object);
	}
	
	/**
	 * Reads the configuration element and all the children.
	 * This creates an action descriptor for every action in the extension.
	 */
	private void readConfigElement() {
		currentContribution = createContribution();
		readElementChildren(config);
		configRead = true;
	}
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected boolean readElement(IConfigurationElement element) {
		String tag = element.getName();
		
		// Found visibility sub-element
		if (tag.equals(PluginActionBuilder.TAG_VISIBILITY)) {
			((ObjectContribution)currentContribution).setVisibilityTest(element);
			return true;
		} 
		
		// Found filter sub-element				
		if (tag.equals(PluginActionBuilder.TAG_FILTER)) {
			((ObjectContribution)currentContribution).addFilterTest(element);
			return true;
		}

		return super.readElement(element);
	}
	
	/**
	 * Returns whether the current selection matches the contribution name filter.
	 */
	private boolean testName(Object object) {
		String nameFilter = config.getAttribute(ATT_NAME_FILTER);
		if (nameFilter == null)
			return true;
		String objectName = null;
		if (object instanceof IAdaptable) {
			IAdaptable element = (IAdaptable) object;
			IWorkbenchAdapter de = (IWorkbenchAdapter) element.getAdapter(IWorkbenchAdapter.class);
			if (de != null)
				objectName = de.getLabel(element);
		}
		if (objectName == null) {
			objectName = object.toString();
		}
		return SelectionEnabler.verifyNameMatch(objectName, nameFilter);
	}


	/**
	 * Helper class to collect the menus and actions defined within a
	 * contribution element.
	 */
	private static class ObjectContribution extends BasicContribution {
		private ObjectFilterTest filterTest;
		private ActionExpression visibilityTest;

		public void addFilterTest(IConfigurationElement element) {
			if (filterTest == null)
				filterTest = new ObjectFilterTest();
			filterTest.addFilterElement(element);
		}
		
		public void setVisibilityTest(IConfigurationElement element) {
			visibilityTest = new ActionExpression(element);
		}
		
		/**
		 * Returns true if name filter is not specified for the contribution
		 * or the current selection matches the filter.
		 */
		public boolean isApplicableTo(Object object) {
			if (visibilityTest != null)
				return visibilityTest.isEnabledFor(object);

			if (filterTest != null)
				return filterTest.matches(object, true);

			return true;
		}
	}
}
