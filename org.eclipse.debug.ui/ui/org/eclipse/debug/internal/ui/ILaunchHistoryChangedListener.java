package org.eclipse.debug.internal.ui;

/**
 * Implementors of this interface are notified whenever a change is made to the launch history.
 * This could be an addition or deletion from either of the run or debug histories, or change
 * to the last launched item.
 */
public interface ILaunchHistoryChangedListener {

	/**
	 * Notification that the launch history has changed.  Any of the run history, debug history
	 * or last launched items could have changed.  To examine the history items, retrieve them
	 * from <code>DebugUIPlugin</code>.
	 */
	public void launchHistoryChanged();
}
