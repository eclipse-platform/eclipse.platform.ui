/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.*;

/**
 * Text Analyzer Descriptor.  Encapsulates Lucene Analyzer
 */
public class AnalyzerDescriptor {
	private Analyzer luceneAnalyzer;
	private String id;
	private String lang;

	/**
	 * Constructor
	 */
	public AnalyzerDescriptor(String locale) {

		// try creating the analyzer for the specified locale (usually lang_country)
		this.luceneAnalyzer = createAnalyzer(locale);

		// 	try creating configured analyzer for the language only
		if (this.luceneAnalyzer == null) {
			String language = null;
			if (locale.length() > 2) {
				language = locale.substring(0, 2);
				this.luceneAnalyzer = createAnalyzer(language);
			}
		}

		// if all fails, create default analyzer
		if (this.luceneAnalyzer == null) {
			this.id =
				HelpPlugin.getDefault().getDescriptor().getUniqueIdentifier()
					+ "#"
					+ HelpPlugin.getDefault().getDescriptor().getVersionIdentifier().toString();
			this.luceneAnalyzer = new DefaultAnalyzer(locale);
			this.lang = locale;
		}

	}
	/**
	 * Gets the analyzer.
	 * @return Returns a Analyzer
	 */
	public Analyzer getAnalyzer() {
		return luceneAnalyzer;
	}

	/**
	 * Gets the id.
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the language for the analyzer
	 * @return Returns a String
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Creates analyzer for a locale, 
	 * if it is configured in the org.eclipse.help.luceneAnalyzer
	 * extension point. The identifier of the analyzer  and locale and lang are also set.
	 * @return Analyzer or null if no analyzer is configured
	 * for given locale.
	 */
	private Analyzer createAnalyzer(String locale) {
		Collection contributions = new ArrayList();
		// find extension point
		IConfigurationElement configElements[] =
			Platform.getPluginRegistry().getConfigurationElementsFor(
				"org.eclipse.help",
				"luceneAnalyzer");
		for (int i = 0; i < configElements.length; i++) {
			if (!configElements[i].getName().equals("analyzer"))
				continue;
			String analyzerLocale = configElements[i].getAttribute("locale");
			if (analyzerLocale == null || !analyzerLocale.equals(locale))
				continue;
			try {
				Object analyzer = configElements[i].createExecutableExtension("class");
				if (!(analyzer instanceof Analyzer))
					continue;
				else {
					String pluginId =
						configElements[i]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					String pluginVersion =
						configElements[i]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getVersionIdentifier()
							.toString();
					this.luceneAnalyzer = (Analyzer) analyzer;
					this.id = pluginId + "#" + pluginVersion;
					this.lang = locale;
					return this.luceneAnalyzer;
				}
			} catch (CoreException ce) {
				Logger.logError(
					Resources.getString("ES23", configElements[i].getAttribute("class"), locale),
					ce);
			}
		}

		return null;
	}

}