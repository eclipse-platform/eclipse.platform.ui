/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.watson;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;

/**
 * Tests functionality of the DeltaIterator class
 */
public class DeltaIteratorTest extends ElementDeltaTest implements IPathConstants {
public DeltaIteratorTest() {
	super(null);
}
public DeltaIteratorTest(String name) {
	super(name);
}
protected String compareToString(int compare) {
	switch (compare) {
		case TestElementComparator.ADDED:
			return "ADDED";
		case TestElementComparator.REMOVED:
			return "REMOVED";
		case TestElementComparator.CHANGED:
			return "CHANGED";
		case 0:
			return "NOCHANGE";
		default:
			return "Unknown: " + compare;
	}
}
/**
 * Initializes a hashtable of IPath -> String describing the changes
 * For that path.  The string is of the form oldObject:newObject:comparison.
 */
protected Hashtable getChangeTable() {
	Hashtable table = new Hashtable();
	table.put(solution, "solution:solution:NOCHANGE");
	table.put(project2, "project2:project2:NOCHANGE");
	table.put(project3, "null:project3:ADDED");
	table.put(folder1, "folder1:folder1:NOCHANGE");
	table.put(folder2, "folder2:ChangedData:CHANGED");
	table.put(folder3, "folder3:null:REMOVED");
	table.put(folder5, "null:folder5:ADDED");
	table.put(file1, "file1:null:REMOVED");
	table.put(file3, "file3:null:REMOVED");
	table.put(file4, "null:file4:ADDED");
	table.put(file5, "null:file5:ADDED");
	return table;
}
public static Test suite() { 
	TestSuite suite= new TestSuite(DeltaIteratorTest.class);
	return suite;
}
public void testIterator() {
	DeltaIterator iterator = new DeltaIterator();

	final Hashtable changeTable = getChangeTable();

	IDeltaVisitor visitor = new IDeltaVisitor() {
		public boolean visitElement(ElementTreeDelta tree, IPath path, Object oldData, Object newData, int comparison) {

			String val = (String)changeTable.remove(path);
			String visitVal = (oldData == null) ? "null" : oldData.toString();
			visitVal += ":" + newData + ":" + compareToString(comparison);
			assertEquals(val, visitVal);
			return true;
		}
	};
		
	iterator.iterate(fTreeDelta, visitor);
	assertTrue(changeTable.size() == 0);
}
}
