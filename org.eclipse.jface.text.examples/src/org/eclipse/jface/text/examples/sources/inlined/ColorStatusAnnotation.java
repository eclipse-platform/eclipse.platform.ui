/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.examples.sources.inlined;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;

/**
 * Color status annotation shows the result of rgb parse before each line which
 * defines 'color:'.
 */
public class ColorStatusAnnotation extends LineHeaderAnnotation {

	public ColorStatusAnnotation(Position position, ISourceViewer viewer) {
		super(position, viewer);
	}

	public void setStatus(String status) {
		super.setText(status);
	}

}
