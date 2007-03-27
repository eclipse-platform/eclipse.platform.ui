package org.eclipse.ui.tests.statushandlers.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * A sample view throwing a PartInitException initialization.
 */
public class PartInitExceptionView extends ViewPart {

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		throw new PartInitException(
				"A sample PartInitException thrown during viewpart initialization.");
	}

	public void createPartControl(Composite parent) {

	}

	public void setFocus() {

	}

}
