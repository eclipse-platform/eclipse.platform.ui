package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public interface IHyperlinkListener {
	/**
	 * @param linkLabel org.eclipse.swt.widgets.Label
	 */
	public void linkActivated(Control linkLabel);
	/**
	 * @param linkLabel org.eclipse.swt.widgets.Label
	 */
	public void linkEntered(Control linkLabel);
	/**
	 * @param linkLabel org.eclipse.swt.widgets.Label
	 */
	public void linkExited(Control linkLabel);
}
