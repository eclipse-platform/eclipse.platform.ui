package org.eclipse.e4.ui.compat.tests.tweaklet;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.tests.helpers.TestFacade;

public class TestFacadeE4Impl extends TestFacade {

	@Override
	public void assertActionSetId(IWorkbenchPage page, String id,
			boolean condition) {
		E4Util.unsupported("assertActionSetId");
	}

	@Override
	public int getActionSetCount(IWorkbenchPage page) {
		E4Util.unsupported("assertActionSetId");
		return 0;
	}

	@Override
	public void addFastView(IWorkbenchPage page, IViewReference ref) {
		E4Util.unsupported("assertActionSetId");
	}

	@Override
	public IStatus saveState(IWorkbenchPage page, IMemento memento) {
		E4Util.unsupported("assertActionSetId");
		return null;
	}

	@Override
	public IViewReference[] getFastViews(IWorkbenchPage page) {
		E4Util.unsupported("assertActionSetId");
		return null;
	}

	@Override
	public ArrayList getPerspectivePartIds(IWorkbenchPage page, String folderId) {
		E4Util.unsupported("assertActionSetId");
		return null;
	}

	@Override
	public boolean isFastView(IWorkbenchPage page, IViewReference ref) {
		E4Util.unsupported("assertActionSetId");
		return false;
	}

	@Override
	public void saveableHelperSetAutomatedResponse(int response) {
		E4Util.unsupported("assertActionSetId");
	}

}
