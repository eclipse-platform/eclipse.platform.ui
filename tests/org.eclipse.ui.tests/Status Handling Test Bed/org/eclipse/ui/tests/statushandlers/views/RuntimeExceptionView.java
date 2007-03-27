package org.eclipse.ui.tests.statushandlers.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * A view throwing RuntimeException during control creation.
 */
public class RuntimeExceptionView extends ViewPart {

	public void createPartControl(Composite parent) {
		throw new RuntimeException(
				"A sample RuntimeException thrown during control creation.");
	}

	public void setFocus() {

	}

}
