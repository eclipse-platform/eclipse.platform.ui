/*******************************************************************************
 * Copyright (c) 2006-2015 IBM Corporation and others.
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
 *     Matthieu Wipliez <matthieu.wipliez@synflow.com> (Synflow SAS) - [CommonNavigator] Implementation of Binding isVisibleExtension not excluding as expected - http://bugs.eclipse.org/425867
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.Policy;

class Binding {

	private final Set<Pattern> rootPatterns = new HashSet<>();

	private final Set<Pattern> includePatterns = new HashSet<>();

	private final Set<Pattern> excludePatterns = new HashSet<>();

	private final String TAG_EXTENSION;

	private final Map<String, Boolean> knownIds = new HashMap<>();
	private final Map<String, Boolean> knownRootIds = new HashMap<>();

	protected Binding(String tagExtension) {
		TAG_EXTENSION = tagExtension;
	}

	boolean isVisibleExtension(String anExtensionId) {
		// Have we seen this pattern before?
		if (knownIds.containsKey(anExtensionId)) {
			// we have, don't recompute
			return knownIds.get(anExtensionId).booleanValue();
		}

		for (Pattern pattern : excludePatterns) {
			if (pattern.matcher(anExtensionId).matches()) {
				knownIds.put(anExtensionId, Boolean.FALSE);
				if (Policy.DEBUG_RESOLUTION) {
					System.out.println("Viewer Binding: EXCLUDED: " + TAG_EXTENSION +//$NON-NLS-1$
							" to: " + anExtensionId); //$NON-NLS-1$
				}
				return false;
			}
		}

		for (Pattern pattern : includePatterns) {
			if (pattern.matcher(anExtensionId).matches()) {
				// keep track of the result for next time
				knownIds.put(anExtensionId, Boolean.TRUE);
				if (Policy.DEBUG_RESOLUTION) {
					System.out.println("Viewer Binding: " + TAG_EXTENSION +//$NON-NLS-1$
							" to: " + anExtensionId); //$NON-NLS-1$
				}
				return true;
			}
		}

		if (Policy.DEBUG_RESOLUTION) {
			System.out.println("Viewer Binding: NOT FOUND: " + TAG_EXTENSION +//$NON-NLS-1$
					" to: " + anExtensionId); //$NON-NLS-1$
		}
		knownIds.put(anExtensionId, Boolean.FALSE);
		return false;
	}

	boolean isRootExtension(String anExtensionId) {
		if (rootPatterns.isEmpty()) {
			return false;
		}
		// Have we seen this pattern before?
		if (knownRootIds.containsKey(anExtensionId)) {
			// we have, don't recompute
			return knownRootIds.get(anExtensionId).booleanValue();
		}
		Pattern pattern = null;
		for (Iterator<Pattern> itr = rootPatterns.iterator(); itr.hasNext();) {
			pattern = itr.next();
			if (pattern.matcher(anExtensionId).matches()) {
				knownRootIds.put(anExtensionId, Boolean.TRUE);
				return true;
			}
		}
		knownRootIds.put(anExtensionId, Boolean.FALSE);
		return false;
	}

	boolean hasOverriddenRootExtensions() {
		return rootPatterns.size() > 0;
	}

	void consumeIncludes(IConfigurationElement element, boolean toRespectRoots) {

		Assert.isTrue(NavigatorViewerDescriptor.TAG_INCLUDES.equals(element
				.getName()));
		IConfigurationElement[] contentExtensionPatterns = element
				.getChildren(TAG_EXTENSION);
		String isRootString = null;
		boolean isRoot = false;
		String patternString = null;
		Pattern compiledPattern = null;
		for (IConfigurationElement contentExtensionPattern : contentExtensionPatterns) {
			if (toRespectRoots) {
				isRootString = contentExtensionPattern
						.getAttribute(NavigatorViewerDescriptor.ATT_IS_ROOT);
				isRoot = (isRootString != null) ? Boolean.parseBoolean(isRootString.trim()) : false;
			}

			patternString = contentExtensionPattern
					.getAttribute(NavigatorViewerDescriptor.ATT_PATTERN);
			if (patternString == null) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Attribute_Missing_Warning,
												NavigatorViewerDescriptor.ATT_PATTERN, element
														.getDeclaringExtension()
														.getUniqueIdentifier(), element
														.getDeclaringExtension()
														.getContributor().getName()),
								null);
			} else {
				compiledPattern = Pattern.compile(patternString);
				includePatterns.add(compiledPattern);
				knownIds.clear();// Cache is now invlaid
				if (toRespectRoots && isRoot) {
					rootPatterns.add(compiledPattern);
				}
			}
		}

	}

	void consumeExcludes(IConfigurationElement element) {
		Assert.isTrue(NavigatorViewerDescriptor.TAG_EXCLUDES.equals(element
				.getName()));
		IConfigurationElement[] contentExtensionPatterns = element
				.getChildren(TAG_EXTENSION);
		String patternString = null;
		Pattern compiledPattern = null;
		for (IConfigurationElement contentExtensionPattern : contentExtensionPatterns) {

			patternString = contentExtensionPattern
					.getAttribute(NavigatorViewerDescriptor.ATT_PATTERN);
			if (patternString == null) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Attribute_Missing_Warning,
												NavigatorViewerDescriptor.ATT_PATTERN, element
														.getDeclaringExtension()
														.getUniqueIdentifier(), element
														.getDeclaringExtension()
														.getContributor().getName()),
								null);
			} else {
				compiledPattern = Pattern.compile(patternString);
				excludePatterns.add(compiledPattern);
				knownIds.clear();// Clear the cache
			}
		}

	}

	void addBinding(Binding otherBinding) {
		includePatterns.addAll(otherBinding.includePatterns);
		excludePatterns.addAll(otherBinding.excludePatterns);
		rootPatterns.addAll(otherBinding.rootPatterns);
	}

}