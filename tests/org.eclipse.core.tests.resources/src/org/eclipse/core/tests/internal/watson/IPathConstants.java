package org.eclipse.core.tests.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
/**
 * Testing interface containing various paths.
 */
interface IPathConstants {

	/**
	 * The following paths are used in the tree created by 
	 * ElementTreeTestUtilities.createTestElementTree()
	 */
	IPath root = Path.ROOT;
	IPath solution = root.append("solution");
	IPath project1 = solution.append("project1");
	IPath project2 = solution.append("project2");
	IPath file1 = project2.append("file1");
	IPath folder1 = project2.append("folder1");
	IPath folder2 = project2.append("folder2");

	IPath file2 = folder1.append("file2");
	IPath folder3 = folder1.append("folder3");
	IPath folder4 = folder1.append("folder4");

	IPath file3 = folder3.append("file3");
}
