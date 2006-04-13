/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.actions;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.internal.navigator.extensions.SkeletonActionProvider;
import org.eclipse.ui.navigator.CommonActionProvider;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonActionProviderDescriptor implements
		INavigatorContentExtPtConstants {

	private static final String DEFAULT_ID = "org.eclipse.ui.navigator.actionProvider"; //$NON-NLS-1$
	
	private static int count = 0;

	private final IConfigurationElement configurationElement;

	private final boolean isNested;

	private Set dependentDescriptors;

	private Set overridingDescriptors;

	private IConfigurationElement enablementElement;

	private Expression enablement;

	private boolean hasLoadingFailed;

	private String definedId;

	private String visibilityId;

	private String dependsOnId;

	private String overridesId;

	private String toString;

	/**
	 * @param aConfigElement
	 *            A configuration element with the name "actionProvider" and a
	 *            "class" attribute which subclasses
	 *            {@link CommonActionProvider}.
	 */
	public CommonActionProviderDescriptor(IConfigurationElement aConfigElement) {
		super();
		Assert.isTrue(TAG_ACTION_PROVIDER.equals(aConfigElement.getName()));
		configurationElement = aConfigElement;
		isNested = false;
		init();
	}

	/**
	 * @param aConfigElement
	 *            A configuration element with the name "actionProvider" and a
	 *            "class" attribute which subclasses
	 *            {@link CommonActionProvider}.
	 * @param anEnablementExpression
	 *            A configuration element with the name 'enablement' or
	 *            'triggerPoints' and containing an Eclipse Core Expression
	 * @param anOverrideId
	 *            A unique identifier for this descriptor. Ids can be used as a
	 *            filtering device for activities or viewer***Bindings.
	 * @param nestedUnderNavigatorContent
	 *            A value of <b>true</b> indicates that this
	 *            CommonActionProvider was declared as a nested
	 *            &lt;actionProvider /&gt; element under a &lt;navigatorContent
	 *            /&gt; element.
	 */
	public CommonActionProviderDescriptor(IConfigurationElement aConfigElement,
			IConfigurationElement anEnablementExpression, String anOverrideId,
			boolean nestedUnderNavigatorContent) {
		super();
		Assert.isTrue(TAG_ACTION_PROVIDER.equals(aConfigElement.getName()));
		Assert.isTrue(TAG_POSSIBLE_CHILDREN.equals(anEnablementExpression
				.getName())
				|| TAG_ENABLEMENT.equals(anEnablementExpression.getName()));
		configurationElement = aConfigElement;
		enablementElement = anEnablementExpression;
		visibilityId = anOverrideId;
		isNested = nestedUnderNavigatorContent;
		init();
	}

	private void init() {

		try {

			definedId = configurationElement.getAttribute(ATT_ID);

			// if there was no id attribute, use the default id.
			if (definedId == null) {
				definedId = DEFAULT_ID + "." + count++; //$NON-NLS-1$
			}

			// we try the id attribute if no override id was supplied.
			if (visibilityId == null) {
				visibilityId = definedId;
			}

			dependsOnId = configurationElement.getAttribute(ATT_DEPENDS_ON);

			overridesId = configurationElement.getAttribute(ATT_OVERRIDES);

			IConfigurationElement[] children = configurationElement
					.getChildren(TAG_ENABLEMENT);
			// if no child enablement is specified, and we have an override, use
			// it
			if (children.length == 0 && enablementElement != null) {
				enablement = new CustomAndExpression(enablementElement);
				// otherwise the child enablement takes priority
			} else if (children.length == 1) {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[0]);

			} else {
				System.err.println("Incorrect number of expressions: " + //$NON-NLS-1$
						TAG_ENABLEMENT
						+ " in navigator extension: " + //$NON-NLS-1$
						configurationElement.getDeclaringExtension()
								.getUniqueIdentifier());
			}
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @return The instantiated CommonActionProvider for this descriptor as
	 *         declared in the ATT_CLASS attribute or
	 *         {@link SkeletonActionProvider} if a problem occurs while loading
	 *         the instance.
	 */
	public CommonActionProvider createActionProvider() {
		if (hasLoadingFailed) {
			return SkeletonActionProvider.INSTANCE;
		}
		CommonActionProvider provider = null;
		try {
			provider = (CommonActionProvider) configurationElement
					.createExecutableExtension(ATT_CLASS);
		} catch (CoreException exception) {
			NavigatorPlugin.log(exception.getStatus());
			hasLoadingFailed = true;
			provider = SkeletonActionProvider.INSTANCE;
		} catch (Exception e) {
			NavigatorPlugin.log(new Status(IStatus.ERROR,
					NavigatorPlugin.PLUGIN_ID, 0, e.getMessage(), e));
			hasLoadingFailed = true;
			provider = SkeletonActionProvider.INSTANCE;
		}

		return provider;
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
		
		if(aStructuredSelection.isEmpty()) {
			IEvaluationContext context = null; 
			context = new EvaluationContext(null, Collections.EMPTY_LIST);
			context.setAllowPluginActivation(true);
			try { 
				if (enablement.evaluate(context) != EvaluationResult.TRUE) {
					return false;
				}
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
				return false;
			} 
		} else {

			IEvaluationContext context = null;
			Iterator elements = aStructuredSelection.iterator();
			while (elements.hasNext()) {
				context = new EvaluationContext(null, elements.next());
				context.setAllowPluginActivation(true);
				try { 
					if (enablement.evaluate(context) != EvaluationResult.TRUE) {
						return false;
					}
				} catch (CoreException e) {
					NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
					return false;
				}
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
		if (enablement == null || anElement == null) {
			return false;
		}

		try {
			EvaluationContext context = new EvaluationContext(null, anElement);
			context.setAllowPluginActivation(true);
			return (enablement.evaluate(context) == EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 
	 * @return An identifier for this ICommonActionProvider. Defaults to
	 *         "org.eclipse.ui.navigator.actionProvider". May not be unique.
	 *         Used to filter the actionProvider using the visibility state
	 *         information.
	 */
	public String getId() {
		return visibilityId;
	}

	/**
	 * 
	 * @return An identifier for this ICommonActionProvider. Defaults to
	 *         'org.eclipse.ui.navigator.actionProvider'. May not be unique.
	 *         Used to determine override or depends on cases.
	 */
	public String getDefinedId() {
		return definedId;
	}

	/**
	 * 
	 * @return True if this is a nested &lt;actionProvider /&gt; element.
	 */
	public boolean isNested() {
		return isNested;
	}

	/**
	 * 
	 * @return The value specified by the <i>dependsOn</i> attribute of the
	 *         &lt;actionProvider /&gt; element.
	 */
	public String getDependsOnId() {
		return dependsOnId;
	}

	/**
	 * 
	 * @return The value specified by the <i>overridesId</i> attribute of the
	 *         &lt;actionProvider /&gt; element.
	 */
	public String getOverridesId() {
		return overridesId;
	}

	public boolean equals(Object obj) {

		if (obj != null && obj instanceof CommonActionProviderDescriptor) {
			CommonActionProviderDescriptor other = (CommonActionProviderDescriptor) obj;
			return getId().equals(other.getId());
		}
		return false;
	} 

	protected void addDependentDescriptor(
			CommonActionProviderDescriptor dependentDescriptor) {
		Assert.isTrue(this != dependentDescriptor);
		if (dependentDescriptors == null) {
			dependentDescriptors = new LinkedHashSet();
		}
		dependentDescriptors.add(dependentDescriptor);
	}

	protected void addOverridingDescriptor(
			CommonActionProviderDescriptor overridingDescriptor) {
		Assert.isTrue(this != overridingDescriptor);
		if (overridingDescriptors == null) {
			overridingDescriptors = new LinkedHashSet();
		}
		overridingDescriptors.add(overridingDescriptor);
	}

	protected boolean hasDependentDescriptors() {
		return dependentDescriptors != null && !dependentDescriptors.isEmpty();
	}

	protected boolean hasOverridingDescriptors() {
		return overridingDescriptors != null
				&& !overridingDescriptors.isEmpty();
	}

	protected Iterator dependentDescriptors() {
		return dependentDescriptors.iterator();
	}

	protected Iterator overridingDescriptors() {
		return overridingDescriptors.iterator();
	}

	public String toString() {
		if (toString == null) {
			toString = "CommonActionProviderDescriptor[" + getId() + ", dependsOn=" + getDependsOnId() + ", overrides=" + getOverridesId() + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
		}
		return toString;
	}

}
