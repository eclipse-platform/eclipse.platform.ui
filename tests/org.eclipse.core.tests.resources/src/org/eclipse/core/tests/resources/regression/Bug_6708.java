package org.eclipse.core.tests.resources.regression;

import java.io.ByteArrayInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class Bug_6708 extends EclipseWorkspaceTest {
/**
 * Constructor for Bug_6708.
 */
public Bug_6708() {
	super();
}
/**
 * Constructor for Bug_6708.
 * @param name
 */
public Bug_6708(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(Bug_6708.class);
}
public void testBug() {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();

	try {
		IProject sourceProj = workspace.getRoot().getProject("P1");
		sourceProj.create(null);
		sourceProj.open(null);
		IFile source = sourceProj.getFile("Source.txt");
		source.create(new ByteArrayInputStream("abcdef".getBytes()), false, null);
	
		IProject destProj = workspace.getRoot().getProject("P2");
		destProj.create(null);
		destProj.open(null);
		IFile dest = destProj.getFile("Dest.txt");
	
		source.copy(dest.getFullPath(), false, null);
		dest.setContents(new ByteArrayInputStream("ghijkl".getBytes()), false, true, null);
	} catch (CoreException e) {
		fail("1.0", e);
	}
}
}

