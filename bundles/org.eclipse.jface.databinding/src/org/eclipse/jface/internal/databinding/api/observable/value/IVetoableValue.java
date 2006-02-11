package org.eclipse.jface.internal.databinding.api.observable.value;

public interface IVetoableValue extends IObservableValue {
	
	public void addValueChangingListener(IValueChangingListener listener);
	
	public void removeValueChangingListener(IValueChangingListener listener);

}
