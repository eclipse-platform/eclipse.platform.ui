package org.eclipse.ui.externaltools.internal.ant.preferences;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;

/**
 * Content provider that maintains a generic list of objects which
 * are shown in a table viewer.
 */
public class AntClasspathContentProvider extends ExternalToolsContentProvider {
	public void add(Object o) {
		URL newURL = (URL) o;
		Iterator itr = elements.iterator();
		while (itr.hasNext()) {
			URL url = (URL) itr.next();
			if (url.sameFile(newURL)) {
				return;
			}
		}
		elements.add(o);
		viewer.add(o);
	}

	public void removeAll() {
		if (viewer != null) {
			viewer.remove(elements.toArray());
		}
		elements = new ArrayList(5);
	}
}