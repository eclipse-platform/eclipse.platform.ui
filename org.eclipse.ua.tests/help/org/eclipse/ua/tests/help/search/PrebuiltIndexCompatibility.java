/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.search;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.AnalyzerDescriptor;
import org.eclipse.help.internal.search.PluginIndex;
import org.eclipse.help.internal.search.QueryBuilder;
import org.eclipse.help.internal.search.SearchIndexWithIndexingProgress;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.osgi.framework.Bundle;

/**
 * Verify that older versions of the index can be read by this
 * version of Eclipse. 
 * 
 * How to maintain this test - if when upgrading to a new version
 * of Lucene one of the IndexReadable tests fails you need to
 * make the following changes:
 * 1. Change the corresponding Compatible() test to expect a result of false
 * 2. Comment out the failing test
 * 3. Change the help system to recognize that version of Lucene as being incompatible
 */

public class PrebuiltIndexCompatibility extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(PrebuiltIndexCompatibility.class);
	}

	/**
	 * Test index built with Lucene 1.9.1
	 */
	public void test1_9_1_IndexReadable() throws Exception {
		checkReadable("data/help/searchindex/index191");
	}
	
	/**
	 * Test index built with Lucene 2.9.1
	 */
	public void test2_9_1_IndexReadable() throws Exception {
		checkReadable("data/help/searchindex/index291");
	}
	
	/**
	 ** Test compatibility of Lucene 1.9.1 index with current Lucene
	 */
	public void test1_9_1Compatible()
	{
		checkCompatible("data/help/searchindex/index191", true);
	}
	
	/**
	 ** Test compatibility of Lucene 2.9.1 index with current Lucene
	 */
	public void test2_9_1Compatible()
	{
		checkCompatible("data/help/searchindex/index291", true);
	}

	public void test1_9_1LuceneCompatible()
	{
		checkLuceneCompatible("1.9.1", true);
	}

	public void test1_4_103NotLuceneCompatible()
	{
		checkLuceneCompatible("1.4.103", false);
	}

	public void test2_9_1LuceneCompatible()
	{
		checkLuceneCompatible("2.9.1", true);
	}

	public void testPluginIndexEqualToItself() {
		PluginIndex index = createPluginIndex("data/help/searchindex/index191");
		assertTrue(index.equals(index));
	}

	/**
	 * Verify that if the paths and plugins are the same two PluginIndex objects are equal
	 */
	public void testPluginIndexEquality() {
		PluginIndex index1a = createPluginIndex("data/help/searchindex/index191");
		PluginIndex index1b = createPluginIndex("data/help/searchindex/index191");
		assertTrue(index1a.equals(index1b));
	}
	
	/**
	 * Verify that if the paths and plugins are the same two PluginIndex objects are equal
	 */
	public void testPluginIndexHash() {
		PluginIndex index1a = createPluginIndex("data/help/searchindex/index191");
		PluginIndex index1b = createPluginIndex("data/help/searchindex/index191");
		assertEquals(index1a.hashCode(), index1b.hashCode());
	}
	
	/**
	 * Verify that if the paths are different two PluginIndex objects are not equal
	 */
	public void testPluginIndexInequality() {
		PluginIndex index1 = createPluginIndex("data/help/searchindex/index191");
		PluginIndex index2 = createPluginIndex("data/help/searchindex/index291");
		assertFalse(index1.equals(index2));
	}

    /*
     * Verifies that a prebuilt index can be searched
     */
	private void checkReadable(String indexPath) throws IOException,
			CorruptIndexException {
		Path path = new Path(indexPath);
		Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle(); 
		URL url = FileLocator.find(bundle, path, null);
		URL resolved = FileLocator.resolve(url);
		if ("file".equals(resolved.getProtocol())) { //$NON-NLS-1$
			String filePath = resolved.getFile();
			Directory luceneDirectory = new NIOFSDirectory(new File(filePath));
			IndexSearcher searcher = new IndexSearcher(luceneDirectory, true);
			QueryBuilder queryBuilder = new QueryBuilder("eclipse", new AnalyzerDescriptor("en-us"));
			Query luceneQuery = queryBuilder.getLuceneQuery(new ArrayList<String>() , false);
			TopDocs hits = searcher.search(luceneQuery, 500);
			assertEquals(hits.totalHits, 1);
		} else {
			fail("Cannot resolve to file protocol");
		}
	}
	
	/*
	 * Tests the isCompatible method in PluginIndex
	 */
	private void checkCompatible(String versionDirectory, boolean expected) {
		PluginIndex pluginIndex = createPluginIndex(versionDirectory);
		Path path = new Path(versionDirectory);
		assertEquals(expected, pluginIndex.isCompatible(UserAssistanceTestPlugin.getDefault().getBundle(), path));
	}

	public PluginIndex createPluginIndex(String versionDirectory) {
		PluginIndex pluginIndex;
		SearchIndexWithIndexingProgress index = BaseHelpSystem.getLocalSearchManager().getIndex("en_us".toString());
		BaseHelpSystem.getLocalSearchManager().ensureIndexUpdated(
				new NullProgressMonitor(),
				index);
		pluginIndex = new PluginIndex("org.eclipse.ua.tests", "data/help/searchindex/" + versionDirectory, index);
		return pluginIndex;
	}
	
	/*
	 * Tests the isLuceneCompatible method in SearchIndex 
	 */
	private void checkLuceneCompatible(String version, boolean expected) {
		SearchIndexWithIndexingProgress index = BaseHelpSystem.getLocalSearchManager().getIndex("en_us".toString());
		BaseHelpSystem.getLocalSearchManager().ensureIndexUpdated(
				new NullProgressMonitor(),
				index);
		assertEquals(expected, index.isLuceneCompatible(version));
	}
	
}
