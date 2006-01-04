/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal.extensions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

class Binding {

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

		Assert.isTrue(NavigatorViewerDescriptor.TAG_INCLUDES.equals(element.getName()));
		IConfigurationElement[] contentExtensionPatterns = element
				.getChildren(TAG_EXTENSION);
		String isRootString = null;
		boolean isRoot = false;
		String patternString = null;
		Pattern compiledPattern = null;
		for (int i = 0; i < contentExtensionPatterns.length; i++) {
			if (toRespectRoots) {
				isRootString = contentExtensionPatterns[i]
						.getAttribute(NavigatorViewerDescriptor.ATT_IS_ROOT);
				isRoot = (isRootString != null) ? Boolean.valueOf(
						isRootString.trim()).booleanValue() : false;
			}

			patternString = contentExtensionPatterns[i]
					.getAttribute(NavigatorViewerDescriptor.ATT_PATTERN);
			if (patternString == null)
				NavigatorPlugin.logError(0, NLS.bind(
						CommonNavigatorMessages.Attribute_Missing_Warning,
						new Object[] {
								NavigatorViewerDescriptor.ATT_PATTERN,
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
		Assert.isTrue(NavigatorViewerDescriptor.TAG_EXCLUDES.equals(element.getName()));
		IConfigurationElement[] contentExtensionPatterns = element
				.getChildren(TAG_EXTENSION);
		String patternString = null;
		Pattern compiledPattern = null;
		for (int i = 0; i < contentExtensionPatterns.length; i++) {

			patternString = contentExtensionPatterns[i]
					.getAttribute(NavigatorViewerDescriptor.ATT_PATTERN);
			if (patternString == null)
				NavigatorPlugin.logError(0, NLS.bind(
						CommonNavigatorMessages.Attribute_Missing_Warning,
						new Object[] {
								NavigatorViewerDescriptor.ATT_PATTERN,
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