package org.eclipse.core.tests.internal.plugins;

import junit.framework.*;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.tests.harness.*;

public abstract class PluginResolveTest extends EclipseWorkspaceTest {
public PluginResolveTest() {
	super(null);
}
public PluginResolveTest(String name) {
	super(name);
}
public IPluginDescriptor checkResolved(IPluginRegistry r, String pfx, String id, String v) {

	IPluginDescriptor pd = r.getPluginDescriptor(id);
	assertNotNull(pfx + ".0.0", pd);
	assertEquals(pfx + ".0.1", pd.getUniqueIdentifier(), id);
	assertEquals(pfx + ".0.2", pd.getVersionIdentifier(), new PluginVersionIdentifier(v));
	return pd;
}
public IPluginDescriptor checkResolved(IPluginRegistry r, String pfx, String id, String v, boolean ok) {

	IPluginDescriptor pd = r.getPluginDescriptor(id, new PluginVersionIdentifier(v));
	if (ok) {
		assertNotNull(pfx + ".0.0", pd);
		assertEquals(pfx + ".0.1", pd.getUniqueIdentifier(), id);
		assertEquals(pfx + ".0.2", pd.getVersionIdentifier(), new PluginVersionIdentifier(v));
	} else
		assertNull(pfx + ".0.3", pd);
	return pd;
}
public void checkResolvedPrereqs(String pfx, IPluginDescriptor pd, String[] id, String[] v) {

	IPluginPrerequisite[] prq = pd.getPluginPrerequisites();
	assertEquals(pfx + ".0.0", prq.length, id.length);
	for (int i = 0; i < prq.length; i++) {
		int ix = -1;
		for (int j = 0; j < id.length; j++) {
			if (prq[i].getUniqueIdentifier().equals(id[j])) {
				ix = j;
				break;
			}
		}
		assertTrue(pfx + ".0.1 " + prq[i].getUniqueIdentifier(), ix != -1);
		assertEquals(pfx + ".0.2 " + prq[i].getUniqueIdentifier(), prq[i].getResolvedVersionIdentifier(), new PluginVersionIdentifier(v[ix]));
	}
}
}
