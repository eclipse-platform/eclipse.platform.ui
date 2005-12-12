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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorExtensionFilter;
import org.eclipse.ui.navigator.Priority;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.CustomAndExpression;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * Encapsulates the <code>org.eclipse.ui.navigator.navigatorContent</code>
 * extension point.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class NavigatorContentDescriptor implements
		INavigatorContentDescriptor {

	private static final INavigatorExtensionFilter[] NO_VIEWER_FILTERS = new INavigatorExtensionFilter[0];

	private static final IConfigurationElement[] NO_DUPLICATE_CONTENT_FILTERS = new IConfigurationElement[0];

	private static final String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$

	private static final String TAG_TRIGGER_POINTS = "triggerPoints"; //$NON-NLS-1$  

	private static final String TAG_POSSIBLE_CHILDREN = "possibleChildren"; //$NON-NLS-1$

	private static final String TAG_DUPLICATE_CONTENT_FILTER = "duplicateContentFilter"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_NAME = "name"; //$NON-NLS-1$	 

	private static final String ATT_ROOT_LABEL = "rootLabel"; //$NON-NLS-1$	 

	private static final String ATT_PRIORITY = "priority"; //$NON-NLS-1$

	private static final String ATT_ICON = "icon"; //$NON-NLS-1$

	private static final String ATT_ACTIVE_BY_DEFAULT = "activeByDefault"; //$NON-NLS-1$

	private static final String ATT_CONTENT_PROVIDER = "contentProvider"; //$NON-NLS-1$

	private static final String ATT_LABEL_PROVIDER = "labelProvider"; //$NON-NLS-1$

	private static final String ATT_VIEWER_FILTER = "viewerFilter"; //$NON-NLS-1$

	private static final String ATT_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$ 

	private static final String ATT_SORTER = "sorter"; //$NON-NLS-1$

	private String id;

	private String name;

	private IConfigurationElement configElement;

	private String rootLabel;

	private int priority = Integer.MAX_VALUE;

	private Expression enablement;

	private Expression possibleChildren;

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
	 * 
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	public NavigatorContentDescriptor(IConfigurationElement configElement)
			throws WorkbenchException {
		super();
		this.configElement = configElement;
		readConfigElement();
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
		if (enablement == null)
			return false;

		IEvaluationContext context = null;

		Iterator elements = aStructuredSelection.iterator();
		while (elements.hasNext()) {
			context = new EvaluationContext(null, elements.next());
			try {
				if (enablement.evaluate(context) == EvaluationResult.FALSE)
					return false;
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
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

		if (enablement == null || anElement == null)
			return false;

		try {
			return (enablement.evaluate(new EvaluationContext(null, anElement)) == EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Determine if this content extension could provide the given element as a
	 * child.
	 * 
	 * <p>
	 * This method is used to determine what the parent of an element could be
	 * for Link with Editor support.
	 * </p>
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension might provide an object of this
	 *         type as a child.
	 */
	public boolean isPossibleChild(Object anElement) {

		if ((enablement == null && possibleChildren == null)
				|| anElement == null)
			return false;

		try {
			if (possibleChildren != null)
				return (possibleChildren.evaluate(new EvaluationContext(null,
						anElement)) == EvaluationResult.TRUE);
			else if (enablement != null)
				return (enablement.evaluate(new EvaluationContext(null,
						anElement)) == EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#getPriority()
	 */
	public int getPriority() {
		return priority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#isRoot()
	 */
	public boolean isRoot() {
		return root;
	}

	/**
	 * @return a label to be used to delay the loading -- used for a content
	 *         extension that contributes a single root element
	 * 
	 */
	protected String getRootLabel() {
		return rootLabel;
	}

	/**
	 * Parses the configuration element.
	 * 
	 * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
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

		declaringPluginId = configElement.getDeclaringExtension()
				.getNamespace();
		String enabledByDefaultString = configElement
				.getAttribute(ATT_ACTIVE_BY_DEFAULT);
		enabledByDefault = (enabledByDefaultString != null && enabledByDefaultString
				.length() > 0) ? Boolean.valueOf(
				configElement.getAttribute(ATT_ACTIVE_BY_DEFAULT))
				.booleanValue() : true;

		if (priorityString != null) {
			try {
				Priority p = Priority.get(priorityString);
				priority = p != null ? p.getValue() : -1;
			} catch (NumberFormatException exception) {
			}
		}
		if (id == null) {
			throw new WorkbenchException(NLS.bind(
					CommonNavigatorMessages.Attribute_Missing_Warning,
					new Object[] {
							ATT_ID,
							configElement.getDeclaringExtension()
									.getUniqueIdentifier(),
							configElement.getDeclaringExtension()
									.getNamespace() }));
		}

		IConfigurationElement[] children = configElement
				.getChildren(TAG_ENABLEMENT);
		if (children.length == 0) {

			children = configElement.getChildren(TAG_TRIGGER_POINTS);
			if (children.length == 1) {
				enablement = new CustomAndExpression(children[0]);
			} else if (children.length > 1) {
				throw new WorkbenchException(NLS.bind(
						CommonNavigatorMessages.Attribute_Missing_Warning,
						new Object[] {
								TAG_TRIGGER_POINTS,
								configElement.getDeclaringExtension()
										.getUniqueIdentifier(),
								configElement.getDeclaringExtension()
										.getNamespace() }));
			}

			children = configElement.getChildren(TAG_POSSIBLE_CHILDREN);
			if (children.length == 1) {
				possibleChildren = new CustomAndExpression(children[0]);
			} else if (children.length > 1) {
				throw new WorkbenchException(NLS.bind(
						CommonNavigatorMessages.Attribute_Missing_Warning,
						new Object[] {
								TAG_POSSIBLE_CHILDREN,
								configElement.getDeclaringExtension()
										.getUniqueIdentifier(),
								configElement.getDeclaringExtension()
										.getNamespace() }));
			}
		} else if (children.length == 1) {
			try {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[0]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
			}
		} else if (children.length > 1) {
			throw new WorkbenchException(NLS.bind(
					CommonNavigatorMessages.Attribute_Missing_Warning,
					new Object[] {
							TAG_ENABLEMENT,
							configElement.getDeclaringExtension()
									.getUniqueIdentifier(),
							configElement.getDeclaringExtension()
									.getNamespace() }));
		}

		contribution = new IPluginContribution() {

			public String getLocalId() {
				return configElement.getDeclaringExtension()
						.getSimpleIdentifier();
			}

			public String getPluginId() {
				return configElement.getDeclaringExtension()
						.getUniqueIdentifier();
			}

		};

		children = configElement.getChildren(TAG_DUPLICATE_CONTENT_FILTER);
		duplicateContentFilterElements = children != null ? children
				: NO_DUPLICATE_CONTENT_FILTERS;
	}

	/**
	 * Set whether or not the receiver is a root navigator extension
	 * 
	 * @param root
	 *            true if the receiver is a root navigator extension. false if
	 *            the receiver is not a root navigator extension.
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

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor#hasLoadingFailed()
	 */
	public boolean hasLoadingFailed() {
		return hasLoadingFailed;
	}

	protected IConfigurationElement[] getDuplicateContentFilterElements() {
		return duplicateContentFilterElements;
	}

	/**
	 * 
	 * @return The duplicate content filters associated with this descriptor.
	 */
	public INavigatorExtensionFilter[] createDuplicateContentFilters() {

		List viewerFiltersList = new ArrayList();
		IConfigurationElement[] dupeFilters = getDuplicateContentFilterElements();
		INavigatorExtensionFilter vFilter = null;
		for (int i = 0; i < dupeFilters.length; i++) {
			try {

				vFilter = (INavigatorExtensionFilter) dupeFilters[i]
						.createExecutableExtension(NavigatorContentDescriptor.ATT_VIEWER_FILTER);
				viewerFiltersList.add(vFilter);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}

		}
		return (INavigatorExtensionFilter[]) (viewerFiltersList.size() > 0 ? viewerFiltersList
				.toArray(new INavigatorExtensionFilter[viewerFiltersList.size()])
				: NO_VIEWER_FILTERS);
	}

	/**
	 * The content provider could be an instance of
	 * {@link ICommonContentProvider}, but only {@link ITreeContentProvider} is
	 * required.
	 * 
	 * 
	 * @return An instance of the Content provider defined for this extension.
	 * @throws CoreException
	 *             if an instance of the executable extension could not be
	 *             created for any reason
	 * 
	 */
	public ITreeContentProvider createContentProvider() throws CoreException {
		return (ITreeContentProvider) configElement
				.createExecutableExtension(ATT_CONTENT_PROVIDER);
	}

	/**
	 * 
	 * The content provider could be an instance of {@link ICommonLabelProvider},
	 * but only {@link ILabelProvider} is required.
	 * 
	 * @return An instance of the Label provider defined for this extension
	 * @throws CoreException
	 *             if an instance of the executable extension could not be
	 *             created for any reason
	 */
	public ILabelProvider createLabelProvider() throws CoreException {
		return (ILabelProvider) configElement
				.createExecutableExtension(ATT_LABEL_PROVIDER);
	}

	/**
	 * 
	 * The action provider is an instance of {@link ICommonActionProvider}.
	 * Extensions may or may not define an action provider.
	 * 
	 * @return An instance of the Action provider defined for this extension
	 * @throws CoreException
	 *             if an instance of the executable extension could not be
	 *             created for any reason
	 * @see ICommonActionProvider
	 */
	public ICommonActionProvider createActionProvider() throws CoreException {
		if (configElement.getAttribute(ATT_ACTION_PROVIDER) != null)
			return (ICommonActionProvider) configElement
					.createExecutableExtension(ATT_ACTION_PROVIDER);
		return null;
	}

	/**
	 * Extensions may or may not define a comparator. Without a comparator,
	 * items are sorted in the same order they are returned from the content
	 * provider.
	 * 
	 * @return An instance of the Comparator defined for this extension
	 * @throws CoreException
	 *             if an instance of the executable extension could not be
	 *             created for any reason
	 */
	public Comparator createComparator() throws CoreException {
		if (configElement.getAttribute(ATT_SORTER) != null)
			return (Comparator) configElement
					.createExecutableExtension(ATT_SORTER);
		return null;
	}
}
