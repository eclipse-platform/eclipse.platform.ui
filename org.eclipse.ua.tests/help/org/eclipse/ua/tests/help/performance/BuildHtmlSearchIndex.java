/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.performance;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractIndexProvider;
import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.index.IndexManager;
import org.eclipse.help.internal.search.AnalyzerDescriptor;
import org.eclipse.help.internal.search.SearchIndex;
import org.eclipse.help.internal.search.SearchIndexWithIndexingProgress;
import org.eclipse.help.internal.toc.TocFile;
import org.eclipse.help.internal.toc.TocFileProvider;
import org.eclipse.help.internal.toc.TocManager;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class BuildHtmlSearchIndex extends PerformanceTestCase {

	private AbstractTocProvider[] tocProviders;
	private AbstractIndexProvider[] indexProviders;
	private AnalyzerDescriptor analyzerDesc;
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(BuildHtmlSearchIndex.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		TocManager tocManager = HelpPlugin.getTocManager();
		tocProviders = tocManager.getTocProviders();
		tocManager.setTocProviders(new AbstractTocProvider[] { new TestTocFileProvider() });
		tocManager.clearCache();

		IndexManager indexManager = HelpPlugin.getIndexManager();
		indexProviders = indexManager.getIndexProviders();
		indexManager.setIndexProviders(new AbstractIndexProvider[0]);
		indexManager.clearCache();
		analyzerDesc = new AnalyzerDescriptor("en-us");
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		TocManager tocManager = HelpPlugin.getTocManager();
		tocManager.setTocProviders(tocProviders);
		tocManager.clearCache();

		IndexManager indexManager = HelpPlugin.getIndexManager();
		indexManager.setIndexProviders(indexProviders);
		indexManager.clearCache();
		analyzerDesc = null;
		tocProviders = null;
		indexProviders = null;
	}
	
	public void testHtmlSearchIndexCreate() throws Exception {
		tagAsGlobalSummary("HTML Search Index", Dimension.ELAPSED_PROCESS);

		// warm-up
		for (int i=0;i<3;++i) {
			buildIndex();
		}
		
		// run the tests
		for (int i=0;i<100;++i) {
			startMeasuring();
			buildIndex();
			stopMeasuring();
		}
		
		commitMeasurements();
		assertPerformance();
	}

	private void buildIndex() {
		SearchIndexWithIndexingProgress index = new SearchIndexWithIndexingProgress("en-us", analyzerDesc, HelpPlugin
				.getTocManager());
		IToc[] tocs = HelpPlugin.getTocManager().getTocs("en-us");
		for (int i = 0; i < tocs.length; i++) {
			ITopic[] topics = tocs[i].getTopics();
			addTopicsToIndex(index, topics);
		}	
	}

	private void addTopicsToIndex(SearchIndexWithIndexingProgress index,
			ITopic[] topics) {
		for (int i = 0; i < topics.length; i++) {
			String href = topics[i].getHref();
			if (href != null) {
				addHrefToIndex(index, href);
			}
			addTopicsToIndex(index, topics[i].getSubtopics());
		}
		
	}

	private void addHrefToIndex(SearchIndexWithIndexingProgress index,
			String doc) {
		URL url = SearchIndex.getIndexableURL(index.getLocale(), doc);
		index.addDocument(url.getFile(), url);
	}

	private static class TestTocFileProvider extends TocFileProvider {
		protected TocFile[] getTocFiles(String locale) {
			String id = UserAssistanceTestPlugin.getPluginId();
			String nl = Platform.getNL();
			return new TocFile[] {
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.isv/toc.xml", true, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.isv/topics_Guide.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.isv/topics_Porting.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.isv/topics_Questions.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.isv/topics_Reference.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.isv/topics_Samples.xml", false, nl, null, null),

				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.user/toc.xml", true, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.user/topics_Concepts.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.user/topics_GettingStarted.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.user/topics_Reference.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.jdt.doc.user/topics_Tasks.xml", false, nl, null, null),
				
				new TocFile(id, "data/help/performance/org.eclipse.pde.doc.user/toc.xml", true, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.pde.doc.user/topics_Reference.xml", false, nl, null, null),
				
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.isv/toc.xml", true, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.isv/topics_Guide.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.isv/topics_Porting.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.isv/topics_Questions.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.isv/topics_Reference.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.isv/topics_Samples.xml", false, nl, null, null),
				
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.user/toc.xml", true, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.user/topics_Concepts.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.user/topics_GettingStarted.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.user/topics_Reference.xml", false, nl, null, null),
				new TocFile(id, "data/help/performance/org.eclipse.platform.doc.user/topics_Tasks.xml", false, nl, null, null),
			};
		}
	}
}
