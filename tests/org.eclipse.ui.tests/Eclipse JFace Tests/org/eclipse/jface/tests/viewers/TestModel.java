package org.eclipse.jface.tests.viewers;

import java.util.Vector;

public class TestModel {
	Vector fListeners = new Vector();
	int fNumLevels;
	int fNumChildren;
public TestModel(int numLevels, int numChildren) {
	fNumLevels = numLevels;
	fNumChildren = numChildren;
}
public void addListener(ITestModelListener listener) {
	fListeners.addElement(listener);
}
/**
 * Fires a model changed event to all listeners.
 */
public void fireModelChanged(TestModelChange change) {
	for (int i = 0; i < fListeners.size(); ++i) {
		ITestModelListener listener = (ITestModelListener) fListeners.get(i);
		listener.testModelChanged(change);
	}
}
public int getNumChildren() {
	return fNumChildren;
}
public int getNumLevels() {
	return fNumLevels;
}
public void removeListener(ITestModelListener listener) {
	fListeners.removeElement(listener);
}
}
