/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * An {@link IControlCreator} for SWT Combo controls. This is a convenience class
 * for creating combo controls to be supplied to a decorated field.
 * 
 * @since 3.3
 */
public class ComboControlCreator implements IControlCreator {

	public Control createControl(Composite parent, int style) {
		return new Combo(parent, style);
	}
}
