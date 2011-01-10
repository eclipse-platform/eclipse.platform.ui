/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
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
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.xml.sax.SAXException;

public class IndexAssemblePerformanceTest extends PerformanceTestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IndexAssemblePerformanceTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testIndexAssemble() throws Exception {
		tagAsSummary("Assemble Index", Dimension.ELAPSED_PROCESS);
	
		// run the tests
		for (int i=0; i < 10; ++i) {
			boolean warmup = i == 0;
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
		IndexContribution a = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/performance/index/index1.xml", "en"));
		IndexContribution b = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/performance/index/index2.xml", "en"));
		IndexContribution c = parser.parse(new IndexFile(UserAssistanceTestPlugin.getPluginId(), "data/help/performance/index/index3.xml", "en"));
		IndexAssembler assembler = new IndexAssembler();
		@SuppressWarnings("unchecked")
		List contributions = new ArrayList(Arrays.asList(new Object[] { a, b, c }));
		Index assembled = assembler.assemble(contributions, Platform.getNL());	
		assertEquals(100, assembled.getChildren().length);
	}
	
}
