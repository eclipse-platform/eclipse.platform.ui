package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IValue;

public interface IValueDetailListener {
	public void detailComputed(IValue value, String result);
}