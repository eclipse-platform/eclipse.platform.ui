package org.eclipse.debug.ui;

import org.eclipse.debug.core.model.IValue;

public interface IValueDetailListener {
	public void detailComputed(IValue value, String result);
}

