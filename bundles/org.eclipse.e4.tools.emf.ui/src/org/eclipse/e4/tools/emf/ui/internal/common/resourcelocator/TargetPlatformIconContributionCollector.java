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
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;

/**
 * A contribution collector encompassing the current target platform.<br />
 * Uses FilterEx for bundle, package, and location filtering
 *
 * @author Steven Spungin
 *
 */
public class TargetPlatformIconContributionCollector extends TargetPlatformContributionCollector {

	protected TargetPlatformIconContributionCollector(String cacheName) {
		super(cacheName);
	}

	static final Pattern pattern = Pattern.compile("(.*/)?([^/]+\\.(jpg|jpeg|png|gif))"); //$NON-NLS-1$
	protected static TargetPlatformIconContributionCollector instance;

	static public TargetPlatformIconContributionCollector getInstance() {
		if (instance == null) {
			instance = new TargetPlatformIconContributionCollector(Messages.TargetPlatformIconContributionCollector_images);
		}
		return instance;
	}

	@Override
	protected Pattern getFilePattern() {
		return pattern;
	};

	@Override
	protected ContributionData makeData(Entry e) {
		ContributionData data = new ContributionData(e.bundleSymName, null, "Java", e.path + e.name); //$NON-NLS-1$
		data.installLocation = e.installLocation;
		data.resourceRelativePath = data.iconPath;
		return data;
	}

}
