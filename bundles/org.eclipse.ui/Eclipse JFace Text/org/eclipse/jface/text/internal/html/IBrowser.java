package org.eclipse.jface.text.internal.html;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Control;


/**
 * HTML browser
 * @deprecated will be removed - present just for test purposes
 */
public interface IBrowser {
	
	public int navigate(String url);
	
	public void setVisible(boolean visible);
	
	public Control getControl();
	public void dispose();	
}
