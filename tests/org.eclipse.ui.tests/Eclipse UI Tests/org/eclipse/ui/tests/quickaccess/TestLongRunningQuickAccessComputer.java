package org.eclipse.ui.tests.quickaccess;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;

public class TestLongRunningQuickAccessComputer implements IQuickAccessComputerExtension {

	public static final int DELAY = 3000;
	public static final QuickAccessElement THE_ELEMENT = new QuickAccessElement() {
		@Override
		public String getLabel() {
			return TestLongRunningQuickAccessComputer.class.getSimpleName();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getId() {
			return getLabel().toLowerCase();
		}

		@Override
		public void execute() {
		}
	};

	@Override
	public QuickAccessElement[] computeElements() {
		return new QuickAccessElement[0];
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean needsRefresh() {
		return false;
	}

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		if (!THE_ELEMENT.getLabel().toLowerCase().equals(query)) {
			return new QuickAccessElement[0];
		}
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new QuickAccessElement[] { THE_ELEMENT };
	}

}
