package org.eclipse.core.tests.resources.usecase;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.CorePerformanceTest;
import junit.framework.*;

public class ConcurrencyPerformanceTest extends CorePerformanceTest {
public ConcurrencyPerformanceTest() {
	super("");
}
public ConcurrencyPerformanceTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(ConcurrencyPerformanceTest.class);
}
public void testSimpleCalls() throws CoreException {

	IWorkspaceRunnable job = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
		}
	};
	startBench();
	int repeat = 50;
 	for (int i = 0; i < repeat; i++) {
		getWorkspace().run(job, null);
	}
	stopBench("testSimpleCalls", repeat);
}
}
