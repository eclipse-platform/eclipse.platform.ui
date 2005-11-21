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
/*
 * Created on Apr 27, 2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.navigator.internal.extensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.navigator.internal.ActionExpression;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
public class LinkHelperRegistry extends RegistryReader {

	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();

	private static final LinkHelperRegistry INSTANCE = new LinkHelperRegistry();
	private static final ILinkHelper[] NO_LINK_HELPERS = new ILinkHelper[0];

	private static boolean isInitialized = false;

	private List descriptors;

	protected LinkHelperRegistry() {
		super(NavigatorPlugin.PLUGIN_ID, Descriptor.LINK_HELPER);
	}

	public static LinkHelperRegistry getInstance() {
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

	public class Descriptor {

		private final IConfigurationElement configElement;

		private String id;

		/* May be null */
		private String navigatorContentExtensionId;

		private ILinkHelper linkHelper;

		public static final String LINK_HELPER = "linkHelper"; //$NON-NLS-1$

		public static final String ATT_ID = "id"; //$NON-NLS-1$

		private static final String ATT_CLASS = "class"; //$NON-NLS-1$

		private static final String ATT_NAVIGATOR_CONTENT_EXTENSION_ID = "navigatorContentExtensionId"; //$NON-NLS-1$

		private ActionExpression editorInputEnablement;

		/* The following field may be null */
		private ActionExpression selectionEnablement;

		private static final String EDITOR_INPUT_ENABLEMENT = "editorInputEnablement"; //$NON-NLS-1$

		private static final String SELECTION_ENABLEMENT = "selectionEnablement"; //$NON-NLS-1$

		public Descriptor(IConfigurationElement element) {
			Assert.isNotNull(element, NavigatorMessages.getString("LinkHelperRegistry.4")); //$NON-NLS-1$
			Assert.isLegal(LINK_HELPER.equals(element.getName()), NavigatorMessages.getString("LinkHelperRegistry.5")); //$NON-NLS-1$
			this.configElement = element;
			init();
		}

		private void init() {
			id = this.configElement.getAttribute(ATT_ID);
			IConfigurationElement[] expressions = this.configElement.getChildren(EDITOR_INPUT_ENABLEMENT);
			Assert.isNotNull(expressions, NavigatorMessages.getString("LinkHelperRegistry.6")); //$NON-NLS-1$
			Assert.isLegal(expressions.length == 1, NavigatorMessages.getString("LinkHelperRegistry.7")); //$NON-NLS-1$
			editorInputEnablement = new ActionExpression(expressions[0]);

			expressions = this.configElement.getChildren(SELECTION_ENABLEMENT);
			if (expressions.length > 0) {
				/* The following attribute is optional */
				navigatorContentExtensionId = expressions[0].getAttribute(ATT_NAVIGATOR_CONTENT_EXTENSION_ID);
				if (expressions[0].getChildren() != null && expressions[0].getChildren().length > 0)
					selectionEnablement = new ActionExpression(expressions[0]);
			}
		}

		/**
		 * @return Returns the id.
		 */
		public String getId() {
			return id;
		}

		public ILinkHelper getLinkHelper() {
			if (linkHelper == null) {
				try {
					linkHelper = (ILinkHelper) this.configElement.createExecutableExtension(ATT_CLASS);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return linkHelper;
		}

		public boolean isEnabledFor(IEditorInput anInput) {
			return (editorInputEnablement != null) ? editorInputEnablement.isEnabledFor(anInput) : false;
		}

		public boolean isEnabledFor(String aNavigatorContentExtensionId) {
			return (navigatorContentExtensionId != null) ? navigatorContentExtensionId.equals(aNavigatorContentExtensionId) : false;
		}

		public boolean isEnabledFor(IStructuredSelection aSelection) {
			return (selectionEnablement != null) ? selectionEnablement.isEnabledFor(aSelection) : false;
		}
	}

	// TODO Define more explicitly the expected order that LinkHelpers will be returned
	public ILinkHelper[] getLinkHelpersFor(IStructuredSelection aSelection) {

		if (aSelection.isEmpty())
			return NO_LINK_HELPERS;

		Set contentDescriptors = CONTENT_DESCRIPTOR_REGISTRY.getEnabledContentDescriptors(aSelection.getFirstElement());
		if (contentDescriptors.isEmpty())
			return NO_LINK_HELPERS;

		/* Use the first Navigator Content Descriptor for now */
		NavigatorContentDescriptor contentDescriptor = (NavigatorContentDescriptor) contentDescriptors.iterator().next();

		List helpersList = new ArrayList();
		ILinkHelper[] helpers = NO_LINK_HELPERS;
		Descriptor descriptor = null;
		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
			descriptor = (Descriptor) itr.next();
			if (descriptor.isEnabledFor(contentDescriptor.getId()))
				helpersList.add(descriptor.getLinkHelper());
			else if (descriptor.isEnabledFor(aSelection))
				helpersList.add(descriptor.getLinkHelper());
		}
		if (helpersList.size() > 0)
			helpersList.toArray((helpers = new ILinkHelper[helpersList.size()]));

		return helpers;
	}

	public ILinkHelper[] getLinkHelpersFor(IEditorInput input) {
		List helpersList = new ArrayList();
		ILinkHelper[] helpers = new ILinkHelper[0];
		Descriptor descriptor = null;
		for (Iterator itr = getDescriptors().iterator(); itr.hasNext();) {
			descriptor = (Descriptor) itr.next();
			if (descriptor.isEnabledFor(input))
				helpersList.add(descriptor.getLinkHelper());
		}
		if (helpersList.size() > 0)
			helpersList.toArray((helpers = new ILinkHelper[helpersList.size()]));

		return helpers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {
		if (Descriptor.LINK_HELPER.equals(element.getName())) {
			getDescriptors().add(new Descriptor(element));
			return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	protected List getDescriptors() {
		if (descriptors == null)
			descriptors = new ArrayList();
		return descriptors;
	}
}
