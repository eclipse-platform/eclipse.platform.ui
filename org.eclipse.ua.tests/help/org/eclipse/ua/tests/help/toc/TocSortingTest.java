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
package org.eclipse.ua.tests.help.toc;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.toc.TocSorter;

import junit.framework.TestCase;

public class TocSortingTest extends TestCase {
	
	private class Toc implements IToc {
		
		private String label;

		public Toc(String label) {
			this.label = label;
		}

		public ITopic getTopic(String href) {
			return null;
		}

		public ITopic[] getTopics() {
            return new ITopic[0];
		}

		public IUAElement[] getChildren() {
			return new IUAElement[0];
		}

		public boolean isEnabled(IEvaluationContext context) {
			return true;
		}

		public String getHref() {
			return null;
		}

		public String getLabel() {
			return label;
		}

	}
	
	private int count = 0;
	
	private class TC implements ITocContribution {

		private IToc toc;
		private String categoryId;
		private String id;
		
		public TC(String name, String category) {
			this.categoryId = category;
			id = "id " + count++;
			toc = new Toc(name);
		}
		
		public String getCategoryId() {
			return categoryId;
		}

		public String getContributorId() {
			return "org.eclipse.ua.tests";
		}

		public String[] getExtraDocuments() {
			return new String[0];
		}

		public String getId() {
			return id;
		}

		public String getLinkTo() {
			return null;
		}

		public String getLocale() {
			return "en";
		}

		public IToc getToc() {
			return toc;
		}

		public boolean isPrimary() {
			return true;
		}		
	}
	
	private String toString(ITocContribution[] tocs) {
		String result = "";
		for (int i = 0; i < tocs.length; i++) {
			result += tocs[i].getToc().getLabel();
		}
		return result;
	}
	
	public void testNoTocs() {
		TocSorter sorter = new TocSorter();
		ITocContribution[] result = sorter.orderTocContributions(new TC[0]);
		assertEquals(result.length, 0);
	}

	public void testNoCategory() {
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("5", null), new TC("3", null), new TC("8", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("358", toString(result));
	}

	public void testCaseInsensitive() {
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("a", null), new TC("c", null), new TC("B", null), new TC("D", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("aBcD", toString(result));
	}
	
	public void testCategories() {
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("4", null), new TC("2", "a"), new TC("5", "b"), new TC("1", null), new TC("8", ""),  
				new TC("7", "a"), new TC("9", "b"), new TC("3", ""), new TC("6", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("127345968", toString(result));
	}
	
}
