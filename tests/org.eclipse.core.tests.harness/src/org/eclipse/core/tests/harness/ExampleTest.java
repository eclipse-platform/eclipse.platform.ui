package org.eclipse.core.tests.harness;

import org.eclipse.core.runtime.*;
import java.util.*;
import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Tests which use the Eclipse Platform runtime only.
 */
public class ExampleTest extends TestCase {
/**
 * Need a zero argument constructor to satisfy the test harness.
 * This constructor should not do any real work nor should it be
 * called by user code.
 */
public ExampleTest() {
	super(null);
}
public ExampleTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(ExampleTest.class);
}
public void testPluginRegistry() throws Throwable {
	System.out.println();
	IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();
	Comparator c = new Comparator() {
		public int compare(Object a, Object b) {
			return ((IPluginDescriptor) a).getLabel().compareTo(((IPluginDescriptor) b).getLabel());
		}
	};
	Arrays.sort(descriptors, c);
	for (int i = 0; i < descriptors.length; i++) {
		IPluginDescriptor descriptor = descriptors[i];
		System.out.print(descriptor.isPluginActivated() ? "+\t" : "-\t");
		System.out.println(descriptor.getLabel() + " (" + descriptor.getUniqueIdentifier() + ") [" + descriptor.getVersionIdentifier() + "]");
	}
}
}
