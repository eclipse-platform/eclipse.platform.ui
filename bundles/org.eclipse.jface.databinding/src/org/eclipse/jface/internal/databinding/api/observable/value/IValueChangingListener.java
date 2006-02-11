package org.eclipse.jface.internal.databinding.api.observable.value;

/**
 * @since 3.2
 *
 */
public interface IValueChangingListener {
	
	/**
	 * This method is called when the value is about to change and provides an opportunity to veto the change.
	 * @param source
	 * @param diff
	 * @return false if this listener is vetoing the change, true otherwise
	 */
	public boolean handleValueChanging(IVetoableValue source, IValueDiff diff);

}
