package org.eclipse.ui.texteditor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ResourceBundle;

import org.eclipse.jface.text.source.IVerticalRulerInfo;


/**
 * @deprecated use <code>MarkerRulerAction</code> instead
 */
public class MarkerRulerInfoAction extends MarkerRulerAction {
	
	/**
	 * @deprecated use super class instead
	 */
	public MarkerRulerInfoAction(ResourceBundle bundle, String prefix, IVerticalRulerInfo ruler, ITextEditor editor, String markerType, boolean askForLabel) {
		super(bundle, prefix, editor, ruler, markerType, askForLabel);
	}
}