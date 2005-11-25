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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.Priority;
import org.eclipse.ui.navigator.internal.ActionExpression;

/**
 * Encapsulates the
 * <code>org.eclipse.wst.common.navigator.internal.views.navigator.navigatorContent</code>
 * extension point.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class NavigatorContentDescriptor implements INavigatorContentDescriptor {
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$	
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$	
	private static final String ATT_ROOT_LABEL = "rootLabel"; //$NON-NLS-1$	 
	
	private static final IConfigurationElement[] NO_DUPLICATE_CONTENT_FILTERS = new IConfigurationElement[0];

	private String id;
	private String name;
	private String className;
	private IConfigurationElement configElement;

	private String rootLabel;

	private static final String CHILD_ENABLEMENT = "enablement"; //$NON-NLS-1$ 
	private static final String CONTRIBUTION_ENABLEMENT = "contributes"; //$NON-NLS-1$
	private static final String ATT_PRIORITY = "priority"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ENABLED_BY_DEFAULT = "enabledByDefault"; //$NON-NLS-1$
	static final String ATT_CONTENT_PROVIDER = "contentProvider"; //$NON-NLS-1$
	static final String ATT_LABEL_PROVIDER = "labelProvider"; //$NON-NLS-1$
	static final String ATT_VIEWER_FILTER = "viewerFilter"; //$NON-NLS-1$
	static final String ATT_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$
	private static final String ATT_OPEN_LISTENER = "openListener"; //$NON-NLS-1$
	static final String ATT_SORTER = "sorter"; //$NON-NLS-1$
	private static final String DUPLICATE_CONTENT_FILTER = "duplicateContentFilter"; //$NON-NLS-1$
	


	private int priority = Integer.MAX_VALUE;
	private ActionExpression enablement; 
	private boolean root;
	private String icon;
	private String declaringPluginId;
	private boolean enabledByDefault;
	private IPluginContribution contribution;
	private boolean hasLoadingFailed;
	private IConfigurationElement[] duplicateContentFilterElements; 
	


	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param configElement
	 *            configuration element to create a descriptor from
	 */
	public NavigatorContentDescriptor(IConfigurationElement configElement) throws WorkbenchException {
		super();
		this.configElement = configElement;
		readConfigElement();
	}

	public boolean isEnabledFor(IStructuredSelection aStructuredSelection) {
		return (enablement != null && enablement.isEnabledFor(aStructuredSelection));
	}

	public boolean isEnabledFor(Object anElement) {
		return (enablement != null && enablement.isEnabledFor(anElement));
	}

	/**
	 * Returns the configuration element parsed by the receiver.
	 * 
	 * @return the configuration element parsed by the receiver
	 */
	public IConfigurationElement getConfigurationElement() {
		return configElement;
	}

	/**
	 * Returns the enablement expression that is used to determine if this navigator content
	 * extension provides children for a given element.
	 * 
	 * @return the enablement expression
	 */
	public ActionExpression getEnablementExpression() {
		return enablement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#getPriority()
	 */
	public int getPriority() {
		return priority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#isRoot()
	 */
	public boolean isRoot() {
		return root;
	}

	/**
	 * @return a label to be used to delay the loading -- used for a content extension that
	 *         contributes a single root element
	 *  
	 */
	protected String getRootLabel() {
		return rootLabel;
	}

	/**
	 * Parses the configuration element.
	 * 
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	void readConfigElement() throws WorkbenchException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME); 
		String priorityString = configElement.getAttribute(ATT_PRIORITY);
		icon = configElement.getAttribute(ATT_ICON);
		rootLabel = configElement.getAttribute(ATT_ROOT_LABEL);
		
		declaringPluginId = configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		String enabledByDefaultString = configElement.getAttribute(ENABLED_BY_DEFAULT);
		enabledByDefault = (enabledByDefaultString != null && enabledByDefaultString.length() > 0) ? Boolean.valueOf(configElement.getAttribute(ENABLED_BY_DEFAULT)).booleanValue() : true;

		if (priorityString != null) {
			try {
				Priority p = Priority.get(priorityString);
				priority = p != null ? p.getValue() : -1; 
			} catch (NumberFormatException exception) {
			}
		}
		if (id == null) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
						ATT_ID + " in navigator extension: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}

		IConfigurationElement[] children = configElement.getChildren(CHILD_ENABLEMENT);
		if (children.length == 1) {
			enablement = new ActionExpression(children[0]);
		} else if (children.length > 1) {
			throw new WorkbenchException("More than one element: " + //$NON-NLS-1$
						CHILD_ENABLEMENT + " in content provider: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}

		contribution = new IPluginContribution() {

			public String getLocalId() {
				return configElement.getDeclaringExtension().getSimpleIdentifier();
			}

			public String getPluginId() {
				return configElement.getDeclaringExtension().getUniqueIdentifier();
			}

		};
		

		children = configElement.getChildren(DUPLICATE_CONTENT_FILTER); 
		duplicateContentFilterElements = children != null ? children : NO_DUPLICATE_CONTENT_FILTERS;
	}

	/**
	 * Set whether or not the receiver is a root navigator extension
	 * 
	 * @param root
	 *            true if the receiver is a root navigator extension. false if the receiver is not a
	 *            root navigator extension.
	 */
	void setRoot(boolean root) {
		this.root = root;
	}

	/**
	 * @return Returns the icon.
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @return Returns the declaringPluginId.
	 */
	public String getDeclaringPluginId() {
		return declaringPluginId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#isEnabledByDefault()
	 */
	public boolean isEnabledByDefault() {
		return enabledByDefault;
	}

	/**
	 * @return Returns the contribution.
	 */
	public IPluginContribution getContribution() {
		return contribution;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#hasLoadingFailed()
	 */
	public boolean hasLoadingFailed() {
		return hasLoadingFailed;
	}

	public IConfigurationElement[] getDuplicateContentFilterElements() {
		return duplicateContentFilterElements;
	}
}
