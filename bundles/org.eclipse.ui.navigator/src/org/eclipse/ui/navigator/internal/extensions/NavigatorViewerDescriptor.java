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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * Encapsulates the <code>org.eclipse.ui.navigator.viewer</code> extension.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorViewerDescriptor implements INavigatorViewerDescriptor {

	private static final String TAG_INCLUDES = "includes"; //$NON-NLS-1$

	private static final String TAG_EXCLUDES = "excludes"; //$NON-NLS-1$

	private static final String TAG_CONTENT_EXTENSION = "contentExtension"; //$NON-NLS-1$

	private static final String TAG_ACTION_EXTENSION = "actionExtension"; //$NON-NLS-1$

	private static final String ATT_PATTERN = "pattern"; //$NON-NLS-1$

	private static final String ATT_IS_ROOT = "isRoot"; //$NON-NLS-1$	

	private final String viewerId;

	private String popupMenuId = null;

	private class Binding {

		private final Set rootPatterns = new HashSet();

		private final Set includePatterns = new HashSet();

		private final Set excludePatterns = new HashSet();

		private final String TAG_EXTENSION;

		protected Binding(String tagExtension) {
			TAG_EXTENSION = tagExtension;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#isVisibleExtension(java.lang.String)
		 */
		public boolean isVisibleExtension(String anExtensionId) {
			Pattern pattern = null;
			for (Iterator itr = includePatterns.iterator(); itr.hasNext();) {
				pattern = (Pattern) itr.next();
				if (pattern.matcher(anExtensionId).matches())
					return true;
			}

			for (Iterator itr = excludePatterns.iterator(); itr.hasNext();) {
				pattern = (Pattern) itr.next();
				if (pattern.matcher(anExtensionId).matches())
					return false;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#isRootExtension(java.lang.String)
		 */
		public boolean isRootExtension(String anExtensionId) {
			if (rootPatterns.size() == 0)
				return false;
			Pattern pattern = null;
			for (Iterator itr = rootPatterns.iterator(); itr.hasNext();) {
				pattern = (Pattern) itr.next();
				if (pattern.matcher(anExtensionId).matches())
					return true;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#hasOverriddenRootExtensions()
		 */
		public boolean hasOverriddenRootExtensions() {
			return rootPatterns.size() > 0;
		}

		public void consumeIncludes(IConfigurationElement element,
				boolean toRespectRoots) throws WorkbenchException {

			Assert.isTrue(TAG_INCLUDES.equals(element.getName()));
			IConfigurationElement[] contentExtensionPatterns = element
					.getChildren(TAG_EXTENSION);
			String isRootString = null;
			boolean isRoot = false;
			String patternString = null;
			Pattern compiledPattern = null;
			for (int i = 0; i < contentExtensionPatterns.length; i++) {
				if (toRespectRoots) {
					isRootString = contentExtensionPatterns[i]
							.getAttribute(ATT_IS_ROOT);
					isRoot = (isRootString != null) ? Boolean.valueOf(
							isRootString.trim()).booleanValue() : false;
				}

				patternString = contentExtensionPatterns[i]
						.getAttribute(ATT_PATTERN);
				if (patternString == null)
					NavigatorPlugin.logError(0, NLS.bind(
							CommonNavigatorMessages.Attribute_Missing_Warning,
							new Object[] {
									ATT_PATTERN,
									element.getDeclaringExtension()
											.getUniqueIdentifier(),
									element.getDeclaringExtension()
											.getNamespace() }), null);
				else {
					compiledPattern = Pattern.compile(patternString);
					includePatterns.add(compiledPattern);
					if (toRespectRoots && isRoot)
						rootPatterns.add(compiledPattern);
				}
			}

		}

		public void consumeExcludes(IConfigurationElement element)
				throws WorkbenchException {
			Assert.isTrue(TAG_EXCLUDES.equals(element.getName()));
			IConfigurationElement[] contentExtensionPatterns = element
					.getChildren(TAG_EXTENSION);
			String patternString = null;
			Pattern compiledPattern = null;
			for (int i = 0; i < contentExtensionPatterns.length; i++) {

				patternString = contentExtensionPatterns[i]
						.getAttribute(ATT_PATTERN);
				if (patternString == null)
					NavigatorPlugin.logError(0, NLS.bind(
							CommonNavigatorMessages.Attribute_Missing_Warning,
							new Object[] {
									ATT_PATTERN,
									element.getDeclaringExtension()
											.getUniqueIdentifier(),
									element.getDeclaringExtension()
											.getNamespace() }), null);
				else {
					compiledPattern = Pattern.compile(patternString);
					excludePatterns.add(compiledPattern);
				}
			}

		}
	}

	private Binding actionBinding = new Binding(TAG_ACTION_EXTENSION);

	private Binding contentBinding = new Binding(TAG_CONTENT_EXTENSION);

	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param aViewerId
	 *            The identifier for this descriptor.
	 */
	public NavigatorViewerDescriptor(String aViewerId) {
		super();
		this.viewerId = aViewerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#getViewerId()
	 */
	public String getViewerId() {
		return viewerId;
	}

	public void setPopupMenuId(String newPopupMenuId) {

		if (newPopupMenuId != null) {
			if (popupMenuId != null)
				NavigatorPlugin
						.log(NLS
								.bind(
										CommonNavigatorMessages.NavigatorViewerDescriptor_Popup_Menu_Overridden,
										new Object[] { getViewerId(),
												popupMenuId, newPopupMenuId }));
			popupMenuId = newPopupMenuId;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#getPopupMenuId()
	 */
	public String getPopupMenuId() {
		return popupMenuId != null ? popupMenuId : viewerId;
	}

	public void consumeContentBinding(IConfigurationElement element)
			throws WorkbenchException {
		consumeBinding(element, true);
	}

	public void consumeActionBinding(IConfigurationElement element)
			throws WorkbenchException {
		consumeBinding(element, false);
	}

	private void consumeBinding(IConfigurationElement element, boolean isContent)
			throws WorkbenchException {
		IConfigurationElement[] includesElement = element
				.getChildren(TAG_INCLUDES);

		if (includesElement.length == 1) {
			if (isContent)
				contentBinding.consumeIncludes(includesElement[0], true);
			else
				actionBinding.consumeIncludes(includesElement[0], false);
		} else if (includesElement.length >= 1) {
			NavigatorPlugin.logError(0, NLS.bind(
					CommonNavigatorMessages.Too_many_elements_Warning,
					new Object[] {
							TAG_INCLUDES,
							element.getDeclaringExtension()
									.getUniqueIdentifier(),
							element.getDeclaringExtension().getNamespace() }),
					null);
		}

		IConfigurationElement[] excludesElement = element
				.getChildren(TAG_EXCLUDES);

		if (excludesElement.length == 1) {

			if (isContent)
				contentBinding.consumeExcludes(excludesElement[0]);
			else
				actionBinding.consumeExcludes(excludesElement[0]);
		} else if (excludesElement.length >= 1) {
			NavigatorPlugin.logError(0, NLS.bind(
					CommonNavigatorMessages.Too_many_elements_Warning,
					new Object[] {
							TAG_EXCLUDES,
							element.getDeclaringExtension()
									.getUniqueIdentifier(),
							element.getDeclaringExtension().getNamespace() }),
					null);
		}
	}

	public boolean isVisibleContentExtension(String aContentExtensionId) {
		return contentBinding.isVisibleExtension(aContentExtensionId);
	}

	public boolean isVisibleActionExtension(String anActionExtensionId) {
		return actionBinding.isVisibleExtension(anActionExtensionId);
	}

	public boolean isRootExtension(String aContentExtensionId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasOverriddenRootExtensions() {
		// TODO Auto-generated method stub
		return false;
	}

}
