package org.eclipse.e4.ui.services;

import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * Service which allows to observe the current workbench-selection using
 * standard Eclipse-Databinding infrastructure and interfaces
 */
public interface ISelectionService extends IObservableValue {
	public Object getSelection(Class api);
}
