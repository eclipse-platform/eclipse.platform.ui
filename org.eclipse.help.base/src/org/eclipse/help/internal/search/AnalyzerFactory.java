/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import org.apache.lucene.analysis.ru.RussianAnalyzer;

import org.apache.lucene.analysis.nl.DutchAnalyzer;

import org.apache.lucene.analysis.fr.FrenchAnalyzer;

import org.apache.lucene.analysis.el.GreekAnalyzer;

import org.apache.lucene.analysis.cz.CzechAnalyzer;

import org.apache.lucene.analysis.cn.ChineseAnalyzer;

import org.apache.lucene.analysis.cjk.CJKAnalyzer;

import org.apache.lucene.analysis.br.BrazilianAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.*;

/**
 * A factory responsible for instantiating a lucene {@link Analyzer}.
 */
public class AnalyzerFactory implements IExecutableExtension{
	private String locale = null;
	public Analyzer create() {
		if (locale == null)
			return null;
		Version version = Version.LUCENE_35;
		if ("pt".equals(locale)) //$NON-NLS-1$
			return new BrazilianAnalyzer(version);
		if ("ja".equals(locale)) //$NON-NLS-1$
			return new CJKAnalyzer(version);
		if ("ko".equals(locale)) //$NON-NLS-1$
			return new CJKAnalyzer(version);
		if ("pt".equals(locale)) //$NON-NLS-1$
			return new BrazilianAnalyzer(version);
		if ("cs".equals(locale)) //$NON-NLS-1$
			return new CzechAnalyzer(version);
		if ("de".equals(locale)) //$NON-NLS-1$
			return new GermanAnalyzer(version);
		if ("el".equals(locale)) //$NON-NLS-1$
			return new GreekAnalyzer(version);
		if ("fr".equals(locale)) //$NON-NLS-1$
			return new FrenchAnalyzer(version);
		if ("nl".equals(locale)) //$NON-NLS-1$
			return new DutchAnalyzer(version);
		if ("ru".equals(locale)) //$NON-NLS-1$
			return new RussianAnalyzer(version);
		//unknown language
		return null;
		
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		if (data instanceof String)
			locale = (String)data;
	}

}
