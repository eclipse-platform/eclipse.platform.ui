package org.eclipse.ui.internal.activities.ws;

import java.util.Set;

import org.eclipse.ui.activities.ActivityManagerFactory;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

public class WorkbenchActivitySupport implements IWorkbenchActivitySupport {
	private IMutableActivityManager mutableActivityManager;

	public WorkbenchActivitySupport() {
		mutableActivityManager =
			ActivityManagerFactory.getMutableActivityManager();
	}

	public IActivityManager getActivityManager() {
		// TODO need to proxy this to prevent casts to IMutableActivityManager
		return mutableActivityManager;
	}

	public void setEnabledActivityIds(Set enabledActivityIds) {
		mutableActivityManager.setEnabledActivityIds(enabledActivityIds);
	}
}
