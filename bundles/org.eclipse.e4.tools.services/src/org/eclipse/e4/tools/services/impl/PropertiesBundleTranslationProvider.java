/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services.impl;

import java.io.InputStream;

import org.eclipse.osgi.service.localization.BundleLocalization;

/**
 * The lookup of the translation the same than the one in {@link BundleLocalization} which is based
 * upon the value found in equinox.root.locale which defaults to "en":
 * <ul>
 * <li>If set to empty string then the search order for:
 * <ul>
 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
 * <li>bn + Ls + "_" + Cs</li>
 * <li>bn + Ls</li>
 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
 * <li>bn + Ld + "_" + Cd</li>
 * <li>bn + Ld</li>
 * </ul>
 * </li>
 * <li>If Ls equals the value of equinox.root.locale then the following search order is used:
 * <ul>
 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
 * <li>bn + Ls + "_" + Cs</li>
 * <li>bn + Ls</li>
 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
 * <li>bn + Ld + "_" + Cd</li>
 * <li>bn + Ld</li>
 * </ul>
 * </li>
 * </ul>
 * Where bn is this bundle's localization basename, Ls, Cs and Vs are the specified locale
 * (language, country, variant) and Ld, Cd and Vd are the default locale (language, country,
 * variant).
 */
public class PropertiesBundleTranslationProvider extends AbstractTranslationProvider {
	private ClassLoader loader;
	private String basename;
	
	public PropertiesBundleTranslationProvider(ClassLoader loader, String baseName) {
		super();
		this.basename = baseName;
		this.loader = loader;
	}

	@Override
	protected InputStream getResourceAsStream(String name) {
		return loader.getResourceAsStream(name);
	}
	
	@Override
	protected String getBasename() {
		return basename;
	}
}