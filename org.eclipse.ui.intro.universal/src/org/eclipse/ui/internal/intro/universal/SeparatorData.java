/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.universal;

import java.io.PrintWriter;

public class SeparatorData extends BaseData {
	
	public SeparatorData() {
	}
	
	public SeparatorData(String id) {
		this.id = id;
	}

	public void write(PrintWriter writer, String indent) {
		writer.print(indent);
		writer.print("<separator id=\""); //$NON-NLS-1$
		writer.print(id);
		writer.println("\"/>"); //$NON-NLS-1$
	}
}