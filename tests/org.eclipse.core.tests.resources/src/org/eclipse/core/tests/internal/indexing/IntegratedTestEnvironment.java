package org.eclipse.core.tests.internal.indexing;

import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class IntegratedTestEnvironment extends EclipseWorkspaceTest implements TestEnvironment {

	public String getFileName() {
		return getWorkspace().getRoot().getLocation().append("test.dat").toOSString();
	}

	public void print(String s) {
	}
	
	public void print(int n, int width) {
	}

	public void println(String s) {
	}
	
	public void printHeading(String s) {
	}

}
