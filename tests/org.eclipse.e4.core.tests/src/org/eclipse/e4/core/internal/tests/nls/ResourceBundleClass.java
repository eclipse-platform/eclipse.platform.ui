package org.eclipse.e4.core.internal.tests.nls;

import java.util.ListResourceBundle;

public class ResourceBundleClass extends ListResourceBundle {
	@Override
	protected Object[][] getContents() {
		return new Object[][] {
			new Object[] { "message_1", "ResourceBundleClass 1" }
		};
	}
}