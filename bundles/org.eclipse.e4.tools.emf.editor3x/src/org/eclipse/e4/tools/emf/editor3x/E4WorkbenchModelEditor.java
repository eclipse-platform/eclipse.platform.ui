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
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.tools.emf.editor3x.compat.E4CompatEditorPart;

public class E4WorkbenchModelEditor extends E4CompatEditorPart {

	public E4WorkbenchModelEditor() {
		super("platform:/plugin/org.eclipse.e4.tools.emf.ui/org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor");
	}
}
