/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services;

public interface IClipboardService {
	public void copy();
	public void paste();
	public void cut();
	public void setHandler(Handler handler);
	
	public interface Handler {
		public void paste();
		public void copy();
		public void cut();
//		public boolean canCopy();
//		public boolean canPaste();
	}
}
