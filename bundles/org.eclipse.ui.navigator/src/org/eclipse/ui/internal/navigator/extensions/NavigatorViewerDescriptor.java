/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.MenuInsertionPoint;
import org.eclipse.ui.navigator.NavigatorActionService;

/**
 * Encapsulates the <code>org.eclipse.ui.navigator.viewer</code> extension.
 * <p>
 * 
 * @since 3.2
 */
public final class NavigatorViewerDescriptor implements
		INavigatorViewerDescriptor {
	

	/**
	 * {@value} (boolean): True indicates the ITreeContentProvider.hasChildren() 
	 * should force plugins to load if necessary <b>false</b>).
	 */
	public static final String PROP_ENFORCE_HAS_CHILDREN = "org.eclipse.ui.navigator.enforceHasChildren"; //$NON-NLS-1$

	static final String TAG_INCLUDES = "includes"; //$NON-NLS-1$

	static final String TAG_EXCLUDES = "excludes"; //$NON-NLS-1$

	static final String ATT_IS_ROOT = "isRoot"; //$NON-NLS-1$

	static final String ATT_PATTERN = "pattern"; //$NON-NLS-1$

	private static final String TAG_CONTENT_EXTENSION = "contentExtension"; //$NON-NLS-1$

	private static final String TAG_ACTION_EXTENSION = "actionExtension"; //$NON-NLS-1$ 

	private final String viewerId;

	private String popupMenuId = null;

	private Binding actionBinding = new Binding(TAG_ACTION_EXTENSION);

	private Binding contentBinding = new Binding(TAG_CONTENT_EXTENSION);

	private MenuInsertionPoint[] customInsertionPoints = null;

	private boolean allowsPlatformContributions = true;

	private String inheritBindingsFromViewer;
	
	private String helpContext;
	
	private final Properties properties = new Properties();

	private Set dragAssistants; 

	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param aViewerId
	 *            The identifier for this descriptor.
	 */
	/* package */NavigatorViewerDescriptor(String aViewerId) {
		super();
		this.viewerId = aViewerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.navigator.extensions.INavigatorViewerDescriptor#getViewerId()
	 */
	public String getViewerId() {
		return viewerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.navigator.extensions.INavigatorViewerDescriptor#getPopupMenuId()
	 */
	public String getPopupMenuId() {
		return popupMenuId != null ? popupMenuId : viewerId;
	}

	/**
	 * Consume an action binding for this viewer.
	 * 
	 * @param element
	 *            The IConfigurationElement containing a viewerActionBinding
	 *            element.
	 */
	public void consumeActionBinding(IConfigurationElement element) {
		consumeBinding(element, false);
	}

	/**
	 * Consume a content binding for this viewer.
	 * 
	 * @param element
	 *            The IConfigurationElement containing a viewerContentBinding
	 *            element.
	 */
	public void consumeContentBinding(IConfigurationElement element) {
		consumeBinding(element, true);
	}

	public boolean isRootExtension(String aContentExtensionId) {
		return contentBinding.isRootExtension(aContentExtensionId);
	}

	public boolean allowsPlatformContributionsToContextMenu() {
		return allowsPlatformContributions;
	}

	public boolean isVisibleContentExtension(String aContentExtensionId) {
		return contentBinding.isVisibleExtension(aContentExtensionId);
	}

	public boolean isVisibleActionExtension(String anActionExtensionId) {
		return actionBinding.isVisibleExtension(anActionExtensionId);
	}

	public boolean hasOverriddenRootExtensions() {
		return contentBinding.hasOverriddenRootExtensions();
	}

	public MenuInsertionPoint[] getCustomInsertionPoints() {
		return customInsertionPoints;
	}

	/**
	 * 
	 * @param newCustomInsertionPoints
	 *            The set of custom insertion points, if any. A null list
	 *            indicates the default set (as defined by
	 *            {@link NavigatorActionService}) should be used. An empty list
	 *            indicates there are no declarative insertion points.
	 */
	public void setCustomInsertionPoints(
			MenuInsertionPoint[] newCustomInsertionPoints) {
		if (customInsertionPoints != null) {
			NavigatorPlugin
					.logError(
							0,
							"Attempt to override custom insertion points denied. Verify there are no colliding org.eclipse.ui.navigator.viewer extension points.", null); //$NON-NLS-1$
			return; // do not let them override the insertion points.
		}
		customInsertionPoints = newCustomInsertionPoints;
	}

	/**
	 * 
	 * @param toAllowPlatformContributions
	 *            A value of 'true' enables object/viewer contributions. 'false'
	 *            will only allow programmatic contributions from
	 *            {@link CommonActionProvider}s.
	 */
	public void setAllowsPlatformContributions(
			boolean toAllowPlatformContributions) {
		allowsPlatformContributions = toAllowPlatformContributions;
	}

	/**
	 * @return the viewer from which this viewer inherits its bindings
	 */
	public String getInheritBindingsFromViewer() {
		return inheritBindingsFromViewer;
	}
	
	/**
	 * @param inherit
	 */
	public void setInheritBindingsFromViewer(String inherit) {
		inheritBindingsFromViewer = inherit;
	}
	
	/**
	 * @return the help context associated with this viewer
	 */
	public String getHelpContext() {
		return helpContext;
	}

	/**
	 * @param context
	 *            the help context associated with this viewer
	 */
	public void setHelpContext(String context) {
		helpContext = context;
	}
	
	/**
	 * @param binding
	 */
	public void setContentBinding(Binding binding) {
		contentBinding = binding;
	}
	
	/**
	 * @return the content Binding
	 */
	public Binding getContentBinding() {
		return contentBinding;
	}
	
	/**
	 * @param binding
	 */
	public void setActionBinding(Binding binding) {
		actionBinding = binding;
	}
	
	/**
	 * @return the action Binding
	 */
	public Binding getActionBinding() {
		return actionBinding;
	}
	
	/**
	 * @param assistants
	 */
	public void setDragAssistants(Set assistants) {
		dragAssistants = assistants;
	}
	

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getStringConfigProperty(java.lang.String)
	 */
	public String getStringConfigProperty(String aPropertyName) {
		return properties.getProperty(aPropertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getBooleanConfigProperty(java.lang.String)
	 */
	public boolean getBooleanConfigProperty(String aPropertyName) {
		String propValue = properties.getProperty(aPropertyName);
		if (propValue == null) {
			return false;
		}
		return Boolean.valueOf(propValue).booleanValue();
	}
	 

	/* package */ void setProperty(String aPropertyName, String aPropertyValue) {
		properties.setProperty(aPropertyName, aPropertyValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ViewerDescriptor[" + viewerId + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Update the popupMenuId. If a value is already set, then a warning message
	 * will be logged.
	 * 
	 * @param newPopupMenuId
	 *            The new popup menu id.
	 */
	/* package */ void setPopupMenuId(String newPopupMenuId) {

		if (newPopupMenuId != null) {
			if (popupMenuId != null) {
				NavigatorPlugin
						.log(
								IStatus.WARNING,
								0,
								NLS
										.bind(
												CommonNavigatorMessages.NavigatorViewerDescriptor_Popup_Menu_Overridden,
												new Object[] { getViewerId(),
														popupMenuId,
														newPopupMenuId }), null);
			}
			popupMenuId = newPopupMenuId;
		}
	}

	/**
	 * @param descriptor
	 *            A non-null descriptor to add
	 */
	/* package */ void addDragAssistant(CommonDragAssistantDescriptor descriptor) {
		getDragAssistants().add(descriptor);

	}

	/**
	 * 
	 * @return The set of {@link CommonDragAssistantDescriptor}s for this
	 *         viewer.
	 */
	public Set getDragAssistants() {
		if (dragAssistants == null) {
			dragAssistants = new HashSet();
		}
		return dragAssistants;
	}

	private void consumeBinding(IConfigurationElement element, boolean isContent) {
		IConfigurationElement[] includesElement = element
				.getChildren(TAG_INCLUDES);

		if (includesElement.length == 1) {
			if (isContent) {
				contentBinding.consumeIncludes(includesElement[0], true);
			} else {
				actionBinding.consumeIncludes(includesElement[0], false);
			}
		} else if (includesElement.length >= 1) {
			NavigatorPlugin.logError(0, NLS.bind(
					CommonNavigatorMessages.Too_many_elements_Warning,
					new Object[] {
							TAG_INCLUDES,
							element.getDeclaringExtension()
									.getUniqueIdentifier(),
							element.getDeclaringExtension().getNamespaceIdentifier() }),
					null);
		}

		IConfigurationElement[] excludesElement = element
				.getChildren(TAG_EXCLUDES);

		if (excludesElement.length == 1) {

			if (isContent) {
				contentBinding.consumeExcludes(excludesElement[0]);
			} else {
				actionBinding.consumeExcludes(excludesElement[0]);
			}
		} else if (excludesElement.length >= 1) {
			NavigatorPlugin.logError(0, NLS.bind(
					CommonNavigatorMessages.Too_many_elements_Warning,
					new Object[] {
							TAG_EXCLUDES,
							element.getDeclaringExtension()
									.getUniqueIdentifier(),
							element.getDeclaringExtension().getNamespaceIdentifier() }),
					null);
		}
	}
	
	void updateFromParent(NavigatorViewerDescriptor parent) {
		getActionBinding().addBinding(parent.getActionBinding());
		getContentBinding().addBinding(parent.getContentBinding());
		getDragAssistants().addAll(parent.getDragAssistants());
	}

}
