package org.eclipse.core.tests.internal.watson;

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.watson.*;
import java.util.*;
import junit.framework.*;

/**
 * Tests the class org.eclispe.core.internal.watson.ElementDelta
 */
public class ElementDeltaTest extends WatsonTest implements IPathConstants {
	protected ElementTreeDelta fTreeDelta;
	protected ElementTree fTree, fNewTree;

	protected IPath project3, folder5, file4, file5;
public ElementDeltaTest() {
	super(null);
}
public ElementDeltaTest(String name) {
	super(name);
}
protected void setUp() throws Exception {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project3
	 *  - add folder5 below project3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete folder3
	 */

	fTree = TestUtil.createTestElementTree();
	fNewTree = fTree.newEmptyDelta();

	project3 = solution.append("project3");
	folder5 = project3.append("folder5");
	file4 = project2.append("file4");
	file5 = folder1.append("file5");
	 
	fNewTree.createElement(project3, "project3");
	fNewTree.createElement(folder5, "folder5");
	fNewTree.deleteElement(file1);
	fNewTree.createElement(folder2, "ChangedData");
	fNewTree.createElement(file4, "file4");
	fNewTree.createElement(file5, "file5");
	fNewTree.deleteElement(folder3);
	fNewTree.immutable();

	/* assert the new structure */
	TestUtil.assertHasPaths(fNewTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder4, folder5});
	TestUtil.assertNoPaths(fNewTree, new IPath[] {file1, file3, folder3});

	/* compute the delta */
	fTreeDelta = fNewTree.computeDeltaWith(fTree, TestUtil.getComparator());
}
public static Test suite() { 
	TestSuite suite= new TestSuite(ElementDeltaTest.class);
	suite.addTest(DeltaIteratorTest.suite());
	return suite;
}
/**
 * 
 */
protected void tearDown() throws Exception {
	//ElementTree tests don't use the CoreTest infrastructure
}
/**
 * Tests the getAffectedChildren(filter) API method.
 */
public void testGetAddedChildren() {
	/* create a filter for added children */
	IDeltaFilter filter = new IDeltaFilter() {
		public boolean includeElement(int flag) {
			return flag == TestElementComparator.ADDED;
		}
	};
	
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);
	assertTrue(delta.getAffectedChildren(filter).length == 0);

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	ElementDelta[] addedChildren = delta.getAffectedChildren(filter);
	assertTrue(addedChildren.length == 1);
	assertTrue(addedChildren[0].getPath().equals(project3));
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	addedChildren = delta.getAffectedChildren(filter);
	assertTrue(addedChildren.length == 1);
	assertTrue(addedChildren[0].getPath().equals(file4));

	
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	addedChildren = delta.getAffectedChildren(filter);
	assertTrue(addedChildren.length == 1);
	assertTrue(addedChildren[0].getPath().equals(folder5));
	
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	addedChildren = delta.getAffectedChildren(filter);
	assertTrue(addedChildren.length == 1);
	assertTrue(addedChildren[0].getPath().equals(file5));

	
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
}
/**
 * Tests the getAffectedChildren API call.
 */
public void testGetAffectedChildren() {
	IDeltaFilter filter = TestUtil.getFilter();
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);
	ElementDelta[] children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(solution));

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 2);
	assertTrue(children[0].getPath().equals(project2));
	assertTrue(children[1].getPath().equals(project3));
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 4);
	assertTrue(children[0].getPath().equals(file1));
	assertTrue(children[1].getPath().equals(file4));
	assertTrue(children[2].getPath().equals(folder1));
	assertTrue(children[3].getPath().equals(folder2));
	
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(folder5));
	
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 2);
	assertTrue(children[0].getPath().equals(file5));
	assertTrue(children[1].getPath().equals(folder3));
	
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(file3));
	
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
}
/**
 * Tests the getChangedChildren API call
 */
public void testGetChangedChildren() {
	/* create a filter for changed children */
	IDeltaFilter filter = new IDeltaFilter() {
		public boolean includeElement(int flag) {
			return flag == TestElementComparator.CHANGED;
		}
	};
	
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);
	ElementDelta[] children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 0);

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 0);
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(folder2));
	
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
}
/**
 * Tests the getComparison() API call
 */
public void testGetComparison() {
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);
	TestUtil.assertNoChange(delta.getComparison());

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	TestUtil.assertNoChange(delta.getComparison());
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	TestUtil.assertNoChange(delta.getComparison());
	
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	TestUtil.assertAdded(delta.getComparison());
	
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	TestUtil.assertNoChange(delta.getComparison());
	
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	TestUtil.assertChanged(delta.getComparison());
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	TestUtil.assertRemoved(delta.getComparison());
	
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	TestUtil.assertAdded(delta.getComparison());
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	TestUtil.assertRemoved(delta.getComparison());
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	TestUtil.assertRemoved(delta.getComparison());
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	TestUtil.assertAdded(delta.getComparison());
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
	TestUtil.assertAdded(delta.getComparison());
}
/**
 * Tests the getElementDelta API call.  This method is basically
 * a template for all other tests in this class.
 */
public void testGetElementDelta() {
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
}
/**
 * Tests the getRemovedChildren API call
 */
public void testGetRemovedChildren() {
	/* create a filter for removed children */
	IDeltaFilter filter = new IDeltaFilter() {
		public boolean includeElement(int flag) {
			return flag == TestElementComparator.REMOVED;
		}
	};
	
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);
	assertTrue(delta.getAffectedChildren(filter).length == 0);

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	ElementDelta[] children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(file1));
	
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(folder3));
	
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	children = delta.getAffectedChildren(filter);
	assertTrue(children.length == 1);
	assertTrue(children[0].getPath().equals(file3));
	
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
}
/**
 * Tests the hasAffectedChildren API call.
 */
public void testHasAffectedChildren() {
	IDeltaFilter filter = TestUtil.getFilter();
	/* delta for root */
	ElementDelta delta = fTreeDelta.getElementDelta(Path.ROOT);
	assertTrue(delta.hasAffectedChildren(filter));

	/* delta for solution */
	delta = fTreeDelta.getElementDelta(solution);
	assertTrue(delta.hasAffectedChildren(filter));
	
	/* delta for project1 */
	delta = fTreeDelta.getElementDelta(project1);
	assertTrue(delta == null);
	
	/* delta for project2 */
	delta = fTreeDelta.getElementDelta(project2);
	assertTrue(delta.hasAffectedChildren(filter));
		
	/* delta for project3 */
	delta = fTreeDelta.getElementDelta(project3);
	assertTrue(delta.hasAffectedChildren(filter));
		
	/* delta for folder1 */
	delta = fTreeDelta.getElementDelta(folder1);
	assertTrue(delta.hasAffectedChildren(filter));
		
	/* delta for folder2 */
	delta = fTreeDelta.getElementDelta(folder2);
	assertTrue(delta.getAffectedChildren(filter).length == 0);
	
	/* delta for folder3 */
	delta = fTreeDelta.getElementDelta(folder3);
	assertTrue(delta.hasAffectedChildren(filter));
		
	/* delta for folder4 */
	delta = fTreeDelta.getElementDelta(folder4);
	assertTrue(delta == null);
	
	/* delta for folder5 */
	delta = fTreeDelta.getElementDelta(folder5);
	assertTrue(!delta.hasAffectedChildren(filter));
	
	/* delta for file1 */
	delta = fTreeDelta.getElementDelta(file1);
	assertTrue(!delta.hasAffectedChildren(filter));
	
	/* delta for file2 */
	delta = fTreeDelta.getElementDelta(file2);
	assertTrue(delta == null);
	
	/* delta for file3 */
	delta = fTreeDelta.getElementDelta(file3);
	assertTrue(!delta.hasAffectedChildren(filter));
	
	/* delta for file4 */
	delta = fTreeDelta.getElementDelta(file4);
	assertTrue(!delta.hasAffectedChildren(filter));
	
	/* delta for file5 */
	delta = fTreeDelta.getElementDelta(file5);
	assertTrue(!delta.hasAffectedChildren(filter));
}
}
