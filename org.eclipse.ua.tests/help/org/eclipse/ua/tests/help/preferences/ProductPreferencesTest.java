/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.preferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.util.ProductPreferences;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

/*
 * Tests the products preferences utility
 */
public class ProductPreferencesTest extends TestCase {
	
	// [expected number of matches], [comma-separated set 1] [comma-separated set 2]
	private static final String[][] COUNT_MATCHING_ITEMS_DATA = {
		
		// no items, no matches
		{ "0", "", "" },
		
		// second set empty
		{ "0", "one,two,three", "" },
		
		// first set empty
		{ "0", "", "four,five,six" },
		
		// neither empty, but no matches
		{ "0", "a,b,c", "d,e,f" },
		
		// two matches
		{ "2", "a,b,c", "b,c,d" },
		
		// all matches
		{ "3", "a,b,c", "a,b,c" },
		
		// all matches; different order
		{ "3", "a,b,c", "c,b,a" },
		
		// two matches with extra items
		{ "2", "one,two,three,four,five", "two,six,three,seven" }
	};

	// [comma-delimited items to search], [index of expected match], [list1], [list2], ...
	private static final String[][] FIND_BEST_MATCH_DATA = {
		
		// not found
		{ "a", null, "b,c,d" },
		
		// not found, more choices
		{ "a", null, "b,c,d", "c,f,e", "h,f,w", "j,e,z", "x,y,z","1,2,3" },
		
		// found one, take only choice
		{ "a", "0", "a,b,c,d" },

		// found some, take only choice
		{ "a,c", "0", "a,b,c,d" },

		// found all, take only choice
		{ "a,c,b,d", "0", "a,b,c,d" },
		
		// found one item in one list
		{ "a", "0", "a,b,c", "d,e,f", "g,h,i", "j,k,l" },

		// found one item in one list, not the first
		{ "h", "2", "a,b,c", "d,e,f", "g,h,i", "j,k,l" },

		// found one item in two list, take the first
		{ "e,j", "1", "a,b,c", "d,e,f", "g,h,i", "j,k,l" },

		// d,e,f is the best choice
		{ "b,c,d,e,f,g,h", "1", "a,b,c", "d,e,f", "g,h,i", "j,k,l" },

		// same, but items and lists shuffled
		{ "g,c,e,b,h,d,f", "2", "i,g,h", "l,k,j", "e,f,d", "b,c,a" },

		// longer names and different delimiters
		{ "one,five,two,four,three", "3", "one;five,;zero", "two five,six", "seven, one, eight, ;four", "five;;two;;three" },
	};

	// [items], [expectedOrder], [primaryOrdering], [secondaryOrdering1], [secondaryOrdering2], ...
	private static final String[][] GET_ORDERED_LIST_DATA = {
		
		// just one item to order, not found
		{ "a", "a", "b,c,d", "e,f,g" },

		// just one item to order
		{ "a", "a", "a", "a" },

		// several items
		{ "a,b,c", "a,b,c", "a", "b,c" },

		// extra element not ordered
		{ "a,b,c", "b,c,a", "b,c", "d,e,f", "g,h,i" },

		// longer test
		{ "one,two,three,four,five", "two,three,four,five,one", "two,three", "one,two", "four,five", "three,four" },

		// same, but items shuffled
		{ "four,two,five,one,three", "two,three,four,five,one", "two,three", "one,two", "four,five", "three,four" },

		// would fail if only used one secondary ordering
		{ "seven,six,five,four,three,two,one", "one,two,three,four,five,six,seven", "one,two", "two,three,four", "one,two,six", "three,four,five", "five,six", "four,five", "six,seven" },
	};

	// [inputFile in data/help/preferences/], [key1=value1], [key2=value2], ...
	private static final String[][] GET_PROPERTIES_FILE_DATA = {
		
		// file with a single property
		{ "propertiesSingle.txt", "myKey=myValue" },
		
		// file with many properies
		{ "propertiesMultiple.txt", "key1=value1", "key2=value2", "this.is.another/key=this.is.another.value" }
	};
	
	// [key to find], [possible resulting values], [primaryProperties], [secondaryProperties1], [secondaryProperties2], ...
	private static final String[][] GET_VALUE_DATA = {
		
		// key doesn't exist
		{ "keyDoesNotExist", "", "values1.txt", "values1.txt" },
		
		// only exists in primary properties
		{ "key1", "value1", "values1.txt", "values1.txt" },
		
		// exists only in two secondary properties
		{ "key2", "value2,value3", "values1.txt", "values1.txt", "values2.txt", "values3.txt" },
		
		// exists both primary and secondary; primary should take precedence
		{ "key3", "value3", "values1.txt", "values1.txt", "values2.txt", "values3.txt" }
	};
	
	// [input], [token1], [token2], ...
	private static final String[][] TOKENIZE_DATA = {
		
		// tolerate null; return no tokens
		{ null },
		
		// no delimiters
		{ "Test", "Test" },
		
		// simple test
		{ "one, two, three", "one", "two", "three" },
		
		// same one repeated
		{ "repeat, repeat, repeat", "repeat", "repeat", "repeat" },
		
		// just commas, no whitespace
		{ "a,b,c", "a", "b", "c" },
		
		// semicolons
		{ "semi;colon;separator", "semi", "colon", "separator" },
		
		// various whitespace
		{ "all\nkinds\rof\t whitespace", "all", "kinds", "of", "whitespace" },
		
		// mixed commas and semicolons
		{ "many;,;,;,separators,;,;,;,test", "many", "separators", "test" },
		
		// all mixed
		{ "mixed; space, and  separators", "mixed", "space", "and", "separators" }
	};
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ProductPreferencesTest.class);
	}
	
	public void testCountMatchingItems() {
		for (int i=0;i<COUNT_MATCHING_ITEMS_DATA.length;++i) {
			String[] data = COUNT_MATCHING_ITEMS_DATA[i];
			int expectedCount = Integer.parseInt(data[0]);
			Set a = new HashSet(ProductPreferences.tokenize(data[1]));
			Set b = new HashSet(ProductPreferences.tokenize(data[2]));
			int actualCount = ProductPreferences.countCommonItems(a, b);
			Assert.assertEquals("Number of matching items found was incorrect for: " + data[1] + " and " + data[2], expectedCount, actualCount);
		}
	}
	
	public void testFindBestMatch() {
		for (int i=0;i<FIND_BEST_MATCH_DATA.length;++i) {
			String[] data = FIND_BEST_MATCH_DATA[i];
			Set items = new HashSet(ProductPreferences.tokenize(data[0]));
			List[] lists = new List[data.length - 2];
			for (int j=0;j<lists.length;++j) {
				lists[j] = ProductPreferences.tokenize(data[j + 2]);
			}
			List expectedBestMatch = null;
			if (data[1] != null) {
				expectedBestMatch = lists[Integer.parseInt(data[1])];
			}
			Assert.assertEquals("The best match found did not match the expected one", expectedBestMatch, ProductPreferences.findBestMatch(items, Arrays.asList(lists)));
		}
	}
	
	public void testGetOrderedList() {
		for (int i=0;i<GET_ORDERED_LIST_DATA.length;++i) {
			String[] data = GET_ORDERED_LIST_DATA[i];
			List items = ProductPreferences.tokenize(data[0]);
			List expectedOrder = ProductPreferences.tokenize(data[1]);
			List primaryOrdering = ProductPreferences.tokenize(data[2]);
			List[] secondaryOrderings = new List[data.length - 3];
			for (int j=0;j<secondaryOrderings.length;++j) {
				secondaryOrderings[j] = ProductPreferences.tokenize(data[j + 3]);
			}
			
			List actualOrder = ProductPreferences.getOrderedList(items, primaryOrdering, secondaryOrderings);
			Assert.assertEquals("Items in list were not ordered as expected", expectedOrder, actualOrder);
		}
	}
	
	public void testGetPropertiesFile() {
		for (int i=0;i<GET_PROPERTIES_FILE_DATA.length;++i) {
			String[] data = GET_PROPERTIES_FILE_DATA[i];
			String path = "data/help/preferences/" + data[0];
			Properties properties = ProductPreferences.loadPropertiesFile(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName(), path);
			
			Assert.assertNotNull("The result of loading a properties file was unexpectedly null", properties);
			Assert.assertEquals(data.length - 1, properties.size());
			
			for (int j=1;j<data.length;++j) {
				StringTokenizer tok = new StringTokenizer(data[j], "=");
				String key = tok.nextToken();
				String expectedValue = tok.nextToken();
				String actualValue = (String)properties.getProperty(key);
				Assert.assertEquals("One of the properties files' keys did not match the expected value: file=" + path + ", key=" + key, expectedValue, actualValue);
			}
		}
	}
	
	public void testGetValue() {
		for (int i=0;i<GET_VALUE_DATA.length;++i) {
			String[] data = GET_VALUE_DATA[i];
			String key = data[0];
			Set allowableValues = new HashSet(ProductPreferences.tokenize(data[1]));
			Properties primary = ProductPreferences.loadPropertiesFile(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName(), "data/help/preferences/" + data[2]);
			Properties[] secondary = new Properties[data.length - 3];
			for (int j=0;j<secondary.length;++j) {
				secondary[j] = ProductPreferences.loadPropertiesFile(UserAssistanceTestPlugin.getDefault().getBundle().getSymbolicName(), "data/help/preferences/" + data[j + 3]);
			}
			
			String value = ProductPreferences.getValue(key, primary, secondary);
			if (allowableValues.isEmpty()) {
				Assert.assertNull("Value should have been null, but was not: " + key, value);
			}
			else {
				Assert.assertTrue("Value returned was not one of the allowable values", allowableValues.contains(value));
			}
		}
	}
	
	public void testTokenize() {
		for (int i=0;i<TOKENIZE_DATA.length;++i) {
			String[] data = TOKENIZE_DATA[i];
			String input = data[0];
			List output = ProductPreferences.tokenize(input);
			
			Assert.assertNotNull("The tokenized output was unexpectedly null for: " + input, output);
			Assert.assertEquals("The number of tokens did not match the expected result for: " + input, data.length - 1, output.size());
			
			for (int j=0;j<output.size();++j) {
				Assert.assertEquals("One of the tokens did not match the expected result", data[j + 1], output.get(j));
			}
		}
	}
}
