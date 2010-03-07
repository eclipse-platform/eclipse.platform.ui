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
package org.eclipse.e4.tools.emf.ui.internal.common.component;

public class HandledToolItemEditor extends ToolItemEditor {

	@Override
	public String getLabel(Object element) {
		return "Handled Tool Item";
	}

	@Override
	public String getDescription(Object element) {
		return "Handled Tool Item bla bla bla";
	}
}