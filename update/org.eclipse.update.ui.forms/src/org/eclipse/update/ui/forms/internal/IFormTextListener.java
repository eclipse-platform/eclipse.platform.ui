package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IFormTextListener {
	public void textValueChanged(FormEntry text);
	public void textDirty(FormEntry text);
}
