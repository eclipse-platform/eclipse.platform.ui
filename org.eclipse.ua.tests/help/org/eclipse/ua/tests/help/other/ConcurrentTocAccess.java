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

package org.eclipse.ua.tests.help.other;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.ITopic;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.ua.tests.help.util.DocumentCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConcurrentTocAccess extends TestCase {
	
	public static Test suite() {
		return new TestSuite(ConcurrentTocAccess.class);
	}
	
	private class TocGenerator {
		private int[] dimensions;
		private StringBuffer result;

		public String generateToc(int dimensions[]) {
			this.dimensions = dimensions;
			result = new StringBuffer();
			result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            result.append("<?NLS TYPE=\"org.eclipse.help.toc\"?>\n");
            result.append("<toc label=\"Test Toc\" >\n");
	        generateTopics(0);
            result.append("</toc>");
			return result.toString();
		}

		private void generateTopics(int depth) {
			if (depth >= dimensions.length) {
				return;
			}
			int numChildren = dimensions[depth];
			for (int i = 0; i < numChildren; i++) {
				result.append("<topic label=\"topicLabel"  + i + "\" href=\"page" + i + ".html\">\n");
				generateTopics(depth + 1);
				result.append("</topic>\n");
			}
		}
	}
	
	/*
	 * Class which visits every topic in a TOC
	 */
	private class TocVisitor extends Thread {
		private final Toc toc;
		public int leafCount;
		public Exception exception;

		TocVisitor(Toc toc) {
			this.toc = toc;
		}
		
		public void run() {
            try {
				leafCount = traverseToc(toc);
			} catch (Exception e) {
                leafCount = -1;
                this.exception = e;
			}
		}
	}

	private void accessInParallel(int[] dimensions, int numberOfThreads) throws Exception {
		Toc toc = createToc(dimensions);
		int expectedLeafCount = computeNumberOfLeafTopics(dimensions);
		TocVisitor[] visitors = new TocVisitor[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++) {
			visitors[i] = new TocVisitor(toc);
		}
		for (int i = 0; i < numberOfThreads; i++) {
			visitors[i].start();
		}
		// Now wait for the threads to complete
		boolean complete = false;
		do {
			complete = true;
			for (int i = 0; i < numberOfThreads; i++) {
				if (visitors[i].isAlive()) {
				    complete = false;
				    try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						fail("Interrupted Exception");
					}
				}
			}
		} while (!complete);
		for (int i = 0; i < numberOfThreads; i++) {
			if (visitors[i].exception != null) {
				throw visitors[i].exception;
			}
			assertEquals(expectedLeafCount, visitors[i].leafCount);
		}
	}
	
	// Visit every child of a TOC and count the number of leaf topics
	private int traverseToc(Toc toc) {
		int leafNodes = 0;
		ITopic[] children = toc.getTopics();
		for (int i = 0; i < children.length; i++) {
			leafNodes += traverseTopic(children[i]);
		}
		return leafNodes;
	}

	private int computeNumberOfLeafTopics(int[] dimensions) {
		int expectedLeaves = 1;
		for (int dim = 0; dim < dimensions.length; dim++) {
			expectedLeaves = expectedLeaves * dimensions[dim];
		}
		return expectedLeaves;
	}

	private int traverseTopic(ITopic topic) {
		assertNotNull(topic.getLabel());
		assertNotNull(topic.getHref());
		ITopic[] children = topic.getSubtopics();
		if (children.length == 0) {
			return 1;
		}
		int leafNodes = 0;
		for (int i = 0; i < children.length; i++) {
			leafNodes += traverseTopic(children[i]);
		}
		return leafNodes;
	}

	private Toc createToc(int[] dimensions) {
		String tocSource = new TocGenerator().generateToc(dimensions);
		Toc toc;
		Document doc;
		try {
		    doc = DocumentCreator.createDocument(tocSource);
		} catch (Exception e) {
			fail("Exception creating TOC");
			doc = null;
		}
		Element tocElement = (Element) doc.getElementsByTagName("toc").item(0);
		toc = new Toc(tocElement);
		return toc;
	}
	
	public void testFlatTocSize5() throws Exception {
		int[] dimensions = {5};
		accessInParallel(dimensions, 2);
	}

	public void testFlatTocSize1000() throws Exception {
		int[] dimensions = {1000};
		accessInParallel(dimensions, 2);
	}

	public void testFlatTocSize10000() throws Exception {
		int[] dimensions = {10000};
		accessInParallel(dimensions, 2);
	}

	public void testTwoLevelToc() throws Exception {
		int[] dimensions = {50, 50};
		accessInParallel(dimensions, 2);
	}
	
	public void testDeepToc() throws Exception {
		int[] dimensions = {2,2,2,2,2,2,2,2,2,2,2};
		accessInParallel(dimensions, 2);
	}
	
	public void testFlatTocManyThreads() throws Exception {
		int[] dimensions = {100};
		accessInParallel(dimensions, 100);
	}	
		
}
