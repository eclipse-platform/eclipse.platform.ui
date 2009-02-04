package org.eclipse.e4.ui.services;


/**
 * Service which allows to observe the current workbench-selection using
 * standard Eclipse-Databinding infrastructure and interfaces
 */
public interface ISelectionService { // extends IObservableValue {
	public void setValue(Object value);
	public Object getSelection(Class api);
}
