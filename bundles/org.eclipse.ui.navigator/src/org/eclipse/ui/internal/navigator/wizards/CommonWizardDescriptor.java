/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.wizards;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;

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
public class CommonWizardDescriptor implements INavigatorContentExtPtConstants, IPluginContribution {

	/** The default menu group id for commonWizards without a menuGroupId attribute. */
	public static final String DEFAULT_MENU_GROUP_ID = "all-uncategorized"; //$NON-NLS-1$

	private String id;

	private String wizardId;

	private String menuGroupId;

	private String type;

	private Expression enablement;

	private IConfigurationElement configElement;

	/**
	 * @param aConfigElement The configuration element from the extension point.
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	public CommonWizardDescriptor(IConfigurationElement aConfigElement)
			throws WorkbenchException {
		super();
		configElement = aConfigElement;
		init();
	}

	/**
	 * @param aConfigElement The configuration element from the extension point.
	 * @param anId the identifier for visibility purposes.
	 *
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	public CommonWizardDescriptor(IConfigurationElement aConfigElement, String anId)
			throws WorkbenchException {
		super();
		configElement = aConfigElement;
		id = anId;
		init();

	}

	/**
	 * Determine if this content extension is enabled for the given selection.
	 * The content extension is enabled for the selection if and only if it is
	 * enabled for each element in the selection.
	 *
	 * @param aStructuredSelection
	 *            The selection from the viewer
	 * @return True if and only if the extension is enabled for each element in
	 *         the selection.
	 */
	public boolean isEnabledFor(IStructuredSelection aStructuredSelection) {
		if (enablement == null) {
			return false;
		}

		IEvaluationContext context = null;
		IEvaluationContext parentContext = NavigatorPlugin.getApplicationContext();

		for (Object element : aStructuredSelection) {
			context = new EvaluationContext(parentContext, element);
			context.setAllowPluginActivation(true);
			if (NavigatorPlugin.safeEvaluate(enablement, context) == EvaluationResult.FALSE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if this content extension is enabled for the given element.
	 *
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension is enabled for the element.
	 */
	public boolean isEnabledFor(Object anElement) {
		if (enablement == null) {
			return false;
		}

		IEvaluationContext context = NavigatorPlugin.getEvalContext(anElement);
		return (NavigatorPlugin.safeEvaluate(enablement, context) == EvaluationResult.TRUE);
	}

	void init() throws WorkbenchException {
		wizardId = configElement.getAttribute(ATT_WIZARD_ID);
		type = configElement.getAttribute(ATT_TYPE);

		menuGroupId = configElement.getAttribute(ATT_MENU_GROUP_ID);
		if(menuGroupId == null) {
			menuGroupId = DEFAULT_MENU_GROUP_ID;
		}

		/*
		 * The id defaults to the id of the enclosing navigatorContent extension, if any
		 * If not enclosed, this can be null initially, so it will default to the
		 * value of the associatedExtensionId
		 *
		 * Code elsewhere anticipates that this attribute may be null, so there is
		 * no need to set it to a default non-null value. Indeed, this will cause
		 * incorrect behavior.
		 * */
		if(id == null) {
			id = configElement.getAttribute(ATT_ASSOCIATED_EXTENSION_ID);
		}

		if (wizardId == null || wizardId.length() == 0) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
					ATT_WIZARD_ID + " in common wizard extension: " + //$NON-NLS-1$
					configElement.getDeclaringExtension().getContributor().getName());
		}

		if (type == null || type.length() == 0) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
					ATT_TYPE + " in common wizard extension: " + //$NON-NLS-1$
					configElement.getDeclaringExtension().getContributor().getName());
		}

		IConfigurationElement[] children = configElement
				.getChildren(TAG_ENABLEMENT);
		if (children.length == 1) {
			try {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[0]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
			}
		} else if (children.length > 1) {
			throw new WorkbenchException("More than one element: " + //$NON-NLS-1$
					TAG_ENABLEMENT + " in common wizard extension: " + //$NON-NLS-1$
					configElement.getDeclaringExtension().getUniqueIdentifier());
		}
	}

	/**
	 *
	 * @return Returns the common wizard wizardId
	 */
	public String getWizardId() {
		return wizardId;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the declaring namespace.
	 */
	public String getNamespace() {
		return configElement.getDeclaringExtension().getNamespaceIdentifier();
	}

	/**
	 *
	 * @return The identifier of the wizard descriptor for visibility purposes (or null) if none.
	 */
	public String getId() {
		return id;
	}


	/**
	 *
	 * @return A developer-defined logical group that this wizard menu option and
	 * 	others like it should be rendered in a localized manner.
	 */
	public String getMenuGroupId() {
		return menuGroupId;
	}


	@Override
	public String toString() {
		return "CommonWizardDescriptor["+getId()+", wizardId="+getWizardId()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getLocalId() {
		return getWizardId();
	}

	@Override
	public String getPluginId() {
		return (configElement != null) ? configElement.getNamespaceIdentifier() : null;
	}
}
