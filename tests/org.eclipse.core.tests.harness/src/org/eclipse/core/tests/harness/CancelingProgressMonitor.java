package org.eclipse.core.tests.harness;

public class CancelingProgressMonitor extends TestProgressMonitor {
/**
 * @see IProgressMonitor#isCanceled
 */
public boolean isCanceled() {
	return true;
}
}
