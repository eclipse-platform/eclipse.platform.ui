package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ResourceBundle;
import org.eclipse.jface.text.source.IVerticalRulerInfo;


/**
 * @deprecated use <code>SelectMarkerRulerAction</code> instead
 */
public class SelectMarkerRulerInfoAction extends SelectMarkerRulerAction {

	/**
	 * @deprecated use super class instead
	 */
	public SelectMarkerRulerInfoAction(ResourceBundle bundle, String prefix, IVerticalRulerInfo ruler, ITextEditor editor) {
		super(bundle, prefix, editor, ruler);
	}
}
