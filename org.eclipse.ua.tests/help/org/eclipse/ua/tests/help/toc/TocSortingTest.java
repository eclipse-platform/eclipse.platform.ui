/*******************************************************************************
 *  Copyright (c) 2007, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.toc;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.HelpData;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.toc.TocSorter;
import org.eclipse.help.internal.util.ProductPreferences;

public class TocSortingTest extends TestCase {
	
	private static final String BASE_TOCS = "baseTOCS";
	private static final String ORDERED_XML = "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/toc_data/helpDataOrdered.xml";
	private static final String EMPTY_XML = "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/toc_data/helpDataEmpty.xml";
	private static final String NO_SORT_XML = "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/toc_data/helpDataOrderedNoSort.xml";
	private static final String BAD_PLUGIN_HELP_DATA_XML = "PLUGINS_ROOT/org.eclipse.nosuchplugin/data/help/toc/toc_data/helpData.xml";
	private static final String NO_SUCH_FILE_XML = "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/toc_data/noSuchFile.xml";
	private static final String ALPHA_SORT_XML = "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/toc_data/helpDataOrderedAlphaSort.xml";
	private String helpDataPreference;
	private String baseTocsPreference;
	
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
	
	private class TC implements ITocContribution {

		private IToc toc;
		private String categoryId;
		private String id;
		
		public TC(String name, String category) {
			this.categoryId = category;
			this.id = "/" + name + "/toc.xml";
			this.toc = new Toc(name);
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
	
	protected void setUp() throws Exception {
		helpDataPreference = Platform.getPreferencesService().getString
	       (HelpPlugin.HELP_DATA_KEY, HelpPlugin.HELP_DATA_KEY, "", null);
		baseTocsPreference = Platform.getPreferencesService().getString
	       (HelpPlugin.HELP_DATA_KEY, BASE_TOCS, "", null);
		HelpData.clearProductHelpData();
		ProductPreferences.resetPrimaryTocOrdering();
		setHelpData(EMPTY_XML);
		setBaseTocs("");
	}
	
	protected void tearDown() throws Exception {
		setHelpData(helpDataPreference);
		setBaseTocs(baseTocsPreference);
		HelpData.clearProductHelpData();
		ProductPreferences.resetPrimaryTocOrdering();
	}

	private void setHelpData(String value) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(HelpPlugin.PLUGIN_ID);
		prefs.put(HelpPlugin.HELP_DATA_KEY, value);
	}
	
	private void setBaseTocs(String value) {
	    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(HelpPlugin.PLUGIN_ID);
	    prefs.put(BASE_TOCS, value);
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

	public void testTocOrderPreference() {
		setHelpData(ORDERED_XML);
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("a", null), new TC("c", null), new TC("b", null), new TC("d", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("dbac", toString(result));
	}

	public void testTocNoSortOthers() {
		setHelpData(NO_SORT_XML);
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("e", null), new TC("c", null), new TC("b", null), new TC("d", null) , new TC("a", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("dbeca", toString(result));
	}

	public void testTocAlphaSortOthers() {
		setHelpData(ALPHA_SORT_XML);
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("e", null), new TC("c", null), new TC("b", null), new TC("d", null) , new TC("a", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("dbace", toString(result));
	}

	public void testTocBadHelpDataPlugin() {
		setHelpData(BAD_PLUGIN_HELP_DATA_XML);
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("e", null), new TC("c", null), new TC("b", null), new TC("d", null) , new TC("a", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("abcde", toString(result));
	}

	public void testTocBadHelpDataPath() {
		setHelpData(NO_SUCH_FILE_XML);
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("e", null), new TC("c", null), new TC("b", null), new TC("d", null) , new TC("a", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("abcde", toString(result));
	}

	public void testNoHelpData() {
		setHelpData("");
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("e", null), new TC("c", null), new TC("b", null), new TC("d", null) , new TC("a", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("abcde", toString(result));
	}
	
	public void testBaseTocs() {
		setHelpData("");
		setBaseTocs("/d/toc.xml,/b/toc.xml");
		TocSorter sorter = new TocSorter();
		ITocContribution[] tocs = new ITocContribution[] {
				new TC("e", null), new TC("c", null), new TC("b", null), new TC("d", null) , new TC("a", null)
		};
		ITocContribution[] result = sorter.orderTocContributions(tocs);
		assertEquals("dbace", toString(result));
	}

	public void testNoProductNoHelpData() {
		List ordering = ProductPreferences.getTocOrdering(null, "", "/a/b.xml,/c/d.xml");
		assertEquals(2, ordering.size());
		assertEquals("/a/b.xml", ordering.get(0));
		assertEquals("/c/d.xml", ordering.get(1));
	}
	
	public void testNoProductWithHelpData() {
		List ordering = ProductPreferences.getTocOrdering(null, "helpData.xml", "/a/b.xml,/c/d.xml");
		assertNull(ordering);
	}
	
	public void testNoProductWithPluginsRoot() {
		List ordering = ProductPreferences.getTocOrdering(null, ORDERED_XML, "/a/b.xml,/c/d.xml");
		assertEquals(3, ordering.size());
		assertEquals("/x/toc.xml", ordering.get(0));
		assertEquals("/d/toc.xml", ordering.get(1));
		assertEquals("/b/toc.xml", ordering.get(2));
	}
	
}
