/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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