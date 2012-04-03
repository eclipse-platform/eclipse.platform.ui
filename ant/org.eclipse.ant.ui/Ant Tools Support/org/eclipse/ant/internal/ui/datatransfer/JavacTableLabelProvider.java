/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.datatransfer;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModelLabelProvider;

/**
 * Ant javac task label provider for a table
 */
public class JavacTableLabelProvider extends AntModelLabelProvider {
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		AntElementNode parent= ((AntElementNode)element).getParentNode();
		if (parent == null) {
			return IAntCoreConstants.EMPTY_STRING;
		}
		String targetName= super.getText(parent);
        return MessageFormat.format(DataTransferMessages.JavacTableLabelProvider_0, new String[] {targetName});
	}
}
