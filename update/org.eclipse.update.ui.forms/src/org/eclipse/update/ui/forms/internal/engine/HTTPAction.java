/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;
import org.eclipse.swt.program.Program;

/**
 * @version 	1.0
 * @author
 */
public class HTTPAction extends HyperlinkAction {
	public HTTPAction () {
	}
	
	public void linkActivated(IHyperlinkSegment link) {
		Program.launch(link.getText());
	}
	
	public void linkEntered(IHyperlinkSegment link) {
		setDescription(link.getText());
		super.linkEntered(link);
	}
}