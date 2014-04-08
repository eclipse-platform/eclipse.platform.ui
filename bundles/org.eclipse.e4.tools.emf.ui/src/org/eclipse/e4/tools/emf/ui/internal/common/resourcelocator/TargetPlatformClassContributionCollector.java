/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator;

import java.util.regex.Pattern;

/**
 * A contribution collector encompassing the current target platform.<br />
 * Uses FilterEx for bundle, package, and location filtering
 *
 * @author Steven Spungin
 *
 */
public class TargetPlatformClassContributionCollector extends TargetPlatformContributionCollector {

	protected TargetPlatformClassContributionCollector(String cacheName) {
		super(cacheName);
	}

	static final private Pattern pattern = Pattern.compile("(.*/)?([^/]+)\\.class"); //$NON-NLS-1$
	protected static TargetPlatformClassContributionCollector instance;

	static public TargetPlatformClassContributionCollector getInstance() {
		if (instance == null) {
			instance = new TargetPlatformClassContributionCollector(Messages.TargetPlatformClassContributionCollector_classes);
		}
		return instance;
	}

	@Override
	protected boolean shouldIgnore(String name) {
		return name.contains("$"); //$NON-NLS-1$
	}

	@Override
	protected Pattern getFilePattern() {
		return pattern;
	}

}
