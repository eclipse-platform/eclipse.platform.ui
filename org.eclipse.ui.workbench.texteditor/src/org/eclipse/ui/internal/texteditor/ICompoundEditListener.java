package org.eclipse.ui.internal.texteditor;

/**
 * <code>ICompoundEditListener</code>s can be registered with a
 * {@link CompoundEditExitStrategy} to be notified if a compound edit session is ended.
 * 
 * @since 3.1
 */
public interface ICompoundEditListener {
	/**
	 * Notifies the receiver that the sending <code>CompoundEditExitStrategy</code> has
	 * detected the end of a compound operation.
	 */
	void endCompoundEdit();
}