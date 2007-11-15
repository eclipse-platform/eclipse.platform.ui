/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.index.Index;
import org.eclipse.help.internal.index.IndexAssembler;
import org.eclipse.help.internal.index.IndexContribution;
import org.eclipse.help.internal.index.IndexFile;
import org.eclipse.help.internal.index.IndexFileParser;
import org.eclipse.help.internal.toc.TocAssembler;
import org.eclipse.help.internal.toc.TocContribution;
import org.eclipse.help.internal.toc.TocFile;
import org.eclipse.help.internal.toc.TocFileParser;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.xml.sax.SAXException;

public class UAElementPerformance extends PerformanceTestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(UAElementPerformance.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void assembleToc() throws Exception {
		TocFileParser parser = new TocFileParser();
		TocContribution b = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/b.xml", true, "en", null, null));
		TocContribution c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/c.xml", true, "en", null, null));
		TocAssembler assembler = new TocAssembler();
		List contributions = new ArrayList(Arrays.asList(new Object[] { b, c }));
		contributions = assembler.assemble(contributions);
		assertEquals(1, contributions.size());
		
		TocContribution a = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/a.xml", true, "en", null, null));
		b = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/b.xml", true, "en", null, null));
		c = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/c.xml", true, "en", null, null));
		TocContribution d = parser.parse(new TocFile(UserAssistanceTestPlugin.getPluginId(), "data/help/toc/assembler/d.xml", false, "en", null, null));
	
		contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c, d }));
		contributions = assembler.assemble(contributions);
		assertEquals(1, contributions.size());		
	}
	
	public void testTocAssemble() throws Exception {
		tagAsSummary("Assemble TOC", Dimension.ELAPSED_PROCESS);
		
		// run the tests
		for (int i=0; i < 250; ++i) {
			boolean warmup = i < 2;
			if (!warmup) {
			    startMeasuring();
			}
			assembleToc();
			if (!warmup) {
			    stopMeasuring();
		    }
		}
		
		commitMeasurements();
		assertPerformance();
	}
	
	public void testIndexAssemble() throws Exception {
		tagAsSummary("Assemble Index", Dimension.ELAPSED_PROCESS);
	
		// run the tests
		for (int i=0; i < 250; ++i) {
			boolean warmup = i < 2;
			if (!warmup) {
			    startMeasuring();
			}
			assembleIndex();
			if (!warmup) {
			    stopMeasuring();
		    }
		}
		
		commitMeasurements();
		assertPerformance();
	}

	private void assembleIndex() throws IOException, SAXException,
			ParserConfigurationException {
		IndexFileParser parser = new IndexFileParser();
		IndexContribution a = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/a.xml", "en"));
		IndexContribution b = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/b.xml", "en"));
		IndexContribution c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/index/assembler/c.xml", "en"));
		IndexAssembler assembler = new IndexAssembler();
		List contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c }));
		Index assembled = assembler.assemble(contributions, Platform.getNL());	
		assertEquals(5, assembled.getChildren().length);
	}
	
}
