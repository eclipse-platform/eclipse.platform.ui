/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.debug.internal.core.stringsubstitution.IStringVariable;
import org.eclipse.jface.viewers.LabelProvider;


class StringVariableLabelProvider extends LabelProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof IStringVariable) {
				IStringVariable variable = (IStringVariable)element;
				return variable.getName();
			}
			return super.getText(element);
		}

}