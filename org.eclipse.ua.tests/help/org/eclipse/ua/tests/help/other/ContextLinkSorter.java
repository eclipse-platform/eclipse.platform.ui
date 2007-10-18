/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.ui.internal.views.ContextHelpSorter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ContextLinkSorter extends TestCase {
	
	private class TestResource implements IHelpResource {
		
		private String category;
		private String href;
		private String label;

		public TestResource(String label, String category, String href) {
			this.category = category;
			this.href = href;
			this.label = label;
		}

		public String getHref() {
			return href;
		}

		public String getLabel() {
			return label;
		}
		
		public String getCategory() {
			return category;
		}
		
	}
	
	private class TestContext implements IContext2 {

		public String getCategory(IHelpResource topic) {
			if (topic instanceof TestResource) {
				return ((TestResource)topic).getCategory();
			}
			return null;
		}

		public String getStyledText() {
			return null;
		}

		public String getTitle() {
			return null;
		}

		public IHelpResource[] getRelatedTopics() {
			return null;
		}

		public String getText() {
			return null;
		}
		
	}

	public static Test suite() {
		return new TestSuite(ContextLinkSorter.class);
	}

	public void testSortEmptyArray() {
		ContextHelpSorter sorter = new ContextHelpSorter(new TestContext());
		TestResource[] resources = new TestResource[0];
		sorter.sort(null, resources);
		assertEquals(0, resources.length);
	}

	public void testSortSingleCategory() {
		ContextHelpSorter sorter = new ContextHelpSorter(new TestContext());
		TestResource[] resources = {
				new TestResource("a1", "c1", "http://www.a1.com"),
				new TestResource("a3", "c1", "http://www.a3.com"),
				new TestResource("a2", "c1", "http://www.a2.com")			
		};
		sorter.sort(null, resources);
		assertEquals(3, resources.length);
		assertEquals("a1", resources[0].getLabel());
		assertEquals("a3", resources[1].getLabel());
		assertEquals("a2", resources[2].getLabel());
	}
	
	public void testSortMultipleCategory() {
		ContextHelpSorter sorter = new ContextHelpSorter(new TestContext());
		TestResource[] resources = {
				new TestResource("a1", "c1", "http://www.a1.com"),
				new TestResource("a3", "c2", "http://www.a3.com"),
				new TestResource("a2", "c1", "http://www.a2.com"),		
				new TestResource("a9", "c1", "http://www.a1.com"),
				new TestResource("a5", null, "http://www.a3.com"),
				new TestResource("a4", "c2", "http://www.a3.com"),
				new TestResource("a0", "c3", "http://www.a3.com"),
				new TestResource("a7", null, "http://www.a2.com")			
		};
		sorter.sort(null, resources);
		assertEquals(8, resources.length);
		assertEquals("a1", resources[0].getLabel());
		assertEquals("a2", resources[1].getLabel());
		assertEquals("a9", resources[2].getLabel());
		assertEquals("a3", resources[3].getLabel());
		assertEquals("a4", resources[4].getLabel());
		assertEquals("a5", resources[5].getLabel());
		assertEquals("a7", resources[6].getLabel());
		assertEquals("a0", resources[7].getLabel());
	}
	
}
