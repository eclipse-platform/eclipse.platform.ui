/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.actions;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.internal.navigator.extensions.SkeletonActionProvider;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.Priority;

/**
 *  
 * 
 * @since 3.2
 */
public class CommonActionProviderDescriptor implements
		INavigatorContentExtPtConstants {

	private static final String DEFAULT_ID = "org.eclipse.ui.navigator.actionProvider"; //$NON-NLS-1$
	
	private static int count = 0;

	private final IConfigurationElement configurationElement;

	private final String pluginId;
	
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
	
	private String appearsBeforeId;

	private String toString;

	private Priority priority;

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
		pluginId = configurationElement.getContributor().getName();
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
	 * @param defaultPriority 
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
			IConfigurationElement anEnablementExpression, Priority defaultPriority, String anOverrideId,
			boolean nestedUnderNavigatorContent) {
		super();
		Assert.isTrue(TAG_ACTION_PROVIDER.equals(aConfigElement.getName()));
		Assert.isTrue(TAG_POSSIBLE_CHILDREN.equals(anEnablementExpression
				.getName())
				|| TAG_ENABLEMENT.equals(anEnablementExpression.getName()));
		configurationElement = aConfigElement;
		pluginId = configurationElement.getContributor().getName();
		enablementElement = anEnablementExpression;
		visibilityId = anOverrideId;
		isNested = nestedUnderNavigatorContent;
		priority = defaultPriority;
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

			appearsBeforeId = configurationElement.getAttribute(ATT_APPEARS_BEFORE);
			
			if(priority == null) {
				String prio = configurationElement.getAttribute(ATT_PRIORITY);
				if(prio != null)
					priority = Priority.get(prio);
				else
					priority = Priority.NORMAL;
			}

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
								.getUniqueIdentifier() + " in plugin " + //$NON-NLS-1$ 
								configurationElement.getDeclaringExtension().getNamespaceIdentifier());
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
		final CommonActionProvider[] provider = new CommonActionProvider[1];
		SafeRunner.run(new NavigatorSafeRunnable(configurationElement) {
			public void run() throws Exception {
				provider[0] = (CommonActionProvider) configurationElement
						.createExecutableExtension(ATT_CLASS);
			}
		});

		if (provider[0] != null)
			return provider[0];
		hasLoadingFailed = true;
		return SkeletonActionProvider.INSTANCE;
	}

	/**
	 * Determine if this action provider descriptor is enabled for the given selection.
	 * The action provider descriptor is enabled for the selection if and only if it is
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
			context = NavigatorPlugin.getEmptyEvalContext();
			if (NavigatorPlugin.safeEvaluate(enablement, context) != EvaluationResult.TRUE) {
				return false;
			}
		} else {
			IEvaluationContext context = null;
			IEvaluationContext parentContext = NavigatorPlugin.getApplicationContext();
			Iterator elements = aStructuredSelection.iterator();
			while (elements.hasNext()) {
				context = new EvaluationContext(parentContext, elements.next());
				context.setAllowPluginActivation(true);
				if (NavigatorPlugin.safeEvaluate(enablement, context) != EvaluationResult.TRUE) {
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

		IEvaluationContext context = NavigatorPlugin.getEvalContext(anElement);
		return NavigatorPlugin.safeEvaluate(enablement, context) == EvaluationResult.TRUE;
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
	
	/**
	 * 
	 * @return The value specified by the <i>appearsBefore</i> attribute of the
	 *         &lt;actionProvider /&gt; element.
	 */
	public String getAppearsBeforeId() {
		return appearsBeforeId;
	}
	
	/**
	 * Only nested Action Providers have priority (as of 3.2.1). 
	 * 
	 * @return The priority associated with this Action Provider Descriptor. 
	 */
	public Priority getPriority() {
		return priority;
	}


	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((definedId == null) ? 0 : definedId.hashCode());
		result = PRIME * result + ((visibilityId == null) ? 0 : visibilityId.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CommonActionProviderDescriptor other = (CommonActionProviderDescriptor) obj;
		if (definedId == null) {
			if (other.definedId != null)
				return false;
		} else if (!definedId.equals(other.definedId))
			return false;
		if (visibilityId == null) {
			if (other.visibilityId != null)
				return false;
		} else if (!visibilityId.equals(other.visibilityId))
			return false;
		return true;
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
			overridingDescriptors = new TreeSet(CommonActionProviderDescriptorCompator.INSTANCE);
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

	/**
	 * @return the id of the contributing plugin
	 */
	public String getPluginId() {
		return pluginId;
	}
	
	public String toString() {
		if (toString == null) {
			toString = "CommonActionProviderDescriptor[definedId=" + getDefinedId() + ", visibilityId=" + getId() + ", dependsOn=" + getDependsOnId() + ", overrides=" + getOverridesId() + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return toString;
	} 

	
	/**
	 * Sorts CommonActionProviderDescriptors by priority, and then by defined id. 
	 * @since 3.2
	 *
	 */
	public static class CommonActionProviderDescriptorCompator implements Comparator { 

		/**
		 * The singleton instance.
		 */
		public static final CommonActionProviderDescriptorCompator INSTANCE = new CommonActionProviderDescriptorCompator();
		
		private static final int LESS_THAN = -1;
		private static final int EQUALS = 0; 
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			CommonActionProviderDescriptor lvalue= null, rvalue= null;
			
			if(o1 instanceof CommonActionProviderDescriptor)
				lvalue = (CommonActionProviderDescriptor) o1;
			
			if(o2 instanceof CommonActionProviderDescriptor)
				rvalue = (CommonActionProviderDescriptor) o2;
			
			if(lvalue == null || rvalue == null)
				return LESS_THAN;
			if(lvalue.equals(rvalue))
				return EQUALS;

			int comparison = lvalue.getPriority().getValue() - rvalue.getPriority().getValue();
			if(comparison == 0) 
				return lvalue.getDefinedId().compareTo(rvalue.getDefinedId());
			return comparison;
			
		}
	}

}
 
