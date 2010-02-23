package org.eclipse.ui.activities;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractActivityManager implements IActivityManager {
    private List activityManagerListeners;

    protected AbstractActivityManager() {
    }

    public void addActivityManagerListener(
            IActivityManagerListener activityManagerListener) {
        if (activityManagerListener == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners == null) {
			activityManagerListeners = new ArrayList();
		}

        if (!activityManagerListeners.contains(activityManagerListener)) {
			activityManagerListeners.add(activityManagerListener);
		}
    }

    protected void fireActivityManagerChanged(
            ActivityManagerEvent activityManagerEvent) {
        if (activityManagerEvent == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners != null) {
			for (int i = 0; i < activityManagerListeners.size(); i++) {
				((IActivityManagerListener) activityManagerListeners.get(i))
                        .activityManagerChanged(activityManagerEvent);
			}
		}
    }

    public void removeActivityManagerListener(
            IActivityManagerListener activityManagerListener) {
        if (activityManagerListener == null) {
			throw new NullPointerException();
		}

        if (activityManagerListeners != null) {
			activityManagerListeners.remove(activityManagerListener);
		}
    }
}
