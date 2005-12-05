package org.eclipse.ui.internal.dnd;

/**
 * This interface allows a particular drop target to be informed that
 * the drag operation was cancelled. This allows the target to clean
 * up any extended drag feedback.
 * 
 * @since 3.2
 *
 */
public interface IDropTarget2 extends IDropTarget {
	/**
	 * This is called whenever a drag operation is cancelled
	 */
	public void dragFinished(boolean dropPerformed);
}
