/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.Separator;

/**
 * Provides a basic metadata about the abstract viewer for a particular content
 * service.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.2
 * 
 */
public interface INavigatorViewerDescriptor {

	/**
	 * {@value} (boolean): True indicates the
	 * "Available Extensions" tab in the "Available Customizations" dialog
	 * should not be available for the user (defaults to <b>false</b>).
	 * 
	 */
	String PROP_HIDE_AVAILABLE_EXT_TAB = "org.eclipse.ui.navigator.hideAvailableExtensionsTab"; //$NON-NLS-1$

	/**
	 * {@value} (boolean): True
	 * indicates the entire "Available Customizations" dialog should not be
	 * available for the user (defaults to <b>false</b>).
	 */
	String PROP_HIDE_AVAILABLE_CUSTOMIZATIONS_DIALOG = "org.eclipse.ui.navigator.hideAvailableCustomizationsDialog"; //$NON-NLS-1$

	/**
	 * {@value} (boolean): True indicates the
	 * "Collapse All" button should not be available for the user (defaults to
	 * <b>false</b>).
	 */
	String PROP_HIDE_COLLAPSE_ALL_ACTION = "org.eclipse.ui.navigator.hideCollapseAllAction"; //$NON-NLS-1$

	/**
	 * {@value} (boolean): True indicates
	 * the "Link With Editor" action should not be available for the user
	 * (defaults to <b>false</b>).
	 */
	String PROP_HIDE_LINK_WITH_EDITOR_ACTION = "org.eclipse.ui.navigator.hideLinkWithEditorAction"; //$NON-NLS-1$

	/**
	 * {@value} (string): The help context id to be used for the customize view dialog, if not specified
	 * help will not be available.
	 * @since 3.5
	 */
	String PROP_CUSTOMIZE_VIEW_DIALOG_HELP_CONTEXT = "org.eclipse.ui.navigator.customizeViewDialogHelpContext"; //$NON-NLS-1$

	/**
	 * Returns the id of the viewer targeted by this extension.
	 * 
	 * @return the id of the viewer targeted by this extension.
	 */
	String getViewerId();

	/**
	 * The default value of the popup menu id is the viewer id. Clients may
	 * override this value using a <b>navigatorConfiguration</b> extension.
	 * 
	 * @return The id of the context menu of the viewer.
	 */
	String getPopupMenuId();

	/**
	 * Returns true if the content extension of the given id is 'visible'. A
	 * content extension is 'visible' if it matches a viewerContentBinding for
	 * the given viewer id.
	 * 
	 * @param aContentExtensionId
	 *            The id to query
	 * @return True if the content extension matches a viewerContentBinding for
	 *         the viewer id of this descriptor.
	 */
	boolean isVisibleContentExtension(String aContentExtensionId);

	/**
	 * Returns true if the action extension of the given id is 'visible'. An
	 * action extension is 'visible' if it matches a viewerActionBinding for the
	 * given viewer id.
	 * 
	 * @param anActionExtensionId
	 *            The id to query
	 * @return True if the action extension matches a viewerActionBinding for
	 *         the viewer id of this descriptor.
	 */
	boolean isVisibleActionExtension(String anActionExtensionId);

	/**
	 * Returns true if the content extension of the given id matches a
	 * viewerContentBinding extension that declares isRoot as true.
	 * 
	 * @param aContentExtensionId
	 *            The id to query
	 * @return True if the content extension matches a viewerContentBinding
	 *         which declares 'isRoot' as true for the viewer id of this
	 *         descriptor.
	 */
	boolean isRootExtension(String aContentExtensionId);

	/**
	 * Returns true if there exists at least one matching viewerContentBinding
	 * which declares isRoot as true. This behavior will override the default
	 * enablement for the viewer root.
	 * 
	 * @return True if there exists a matching viewerContentBinding which
	 *         declares isRoot as true.
	 */
	boolean hasOverriddenRootExtensions();

	/**
	 * Returns true by default. A true value indicates that object and view
	 * contributions should be supported by the popup menu of any viewer
	 * described by this viewer descriptor. The value may be overridden from the
	 * &lt;popupMenu /&gt; child element of the &lt;viewer /&gt; element in the
	 * <b>org.eclipse.ui.navigator.viewer</b> extension point.
	 * 
	 * @return True if object/view contributions should be allowed or False
	 *         otherwise.
	 */
	boolean allowsPlatformContributionsToContextMenu();

	/**
	 * 
	 * Custom insertion points are declared through a nested 'popupMenu' element
	 * in the <b>org.eclipse.ui.navigator.viewer</b> extension point. Each
	 * insertion point represents either a {@link Separator} or
	 * {@link GroupMarker} in the context menu of the viewer.
	 * <p>
	 * 
	 * @return The set of custom insertion points, if any. A null list indicates
	 *         the default set (as defined by {@link NavigatorActionService})
	 *         should be used. An empty list indicates there are no declarative
	 *         insertion points.
	 */
	MenuInsertionPoint[] getCustomInsertionPoints();

	/**
	 * @param aPropertyName
	 *            A property name corresponding to a configuration option from
	 *            <b>org.eclipse.ui.navigator.viewer</b>
	 * @return The unmodified string value returned from the extension (<b>null</b>
	 *         is a possible return value).
	 */
	String getStringConfigProperty(String aPropertyName);

	/**
	 * @param aPropertyName
	 *            A property name corresponding to a configuration option from
	 *            <b>org.eclipse.ui.navigator.viewer</b>
	 * @return The boolean value returned from the extension (<b>null</b> is a
	 *         possible return value).
	 */
	boolean getBooleanConfigProperty(String aPropertyName);
	
	/**
	 * @return the help context associated with this viewer as specified by 
	 * the helpContext attribute of the &lt;viewer /&gt; element in the
	 * <b>org.eclipse.ui.navigator.viewer</b> extension point.
	 * @since 3.4
	 */
	String getHelpContext();

}