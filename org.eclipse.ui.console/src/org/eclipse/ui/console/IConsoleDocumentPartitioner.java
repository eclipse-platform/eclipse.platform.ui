/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.console;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.swt.custom.StyleRange;

public interface IConsoleDocumentPartitioner extends IDocumentPartitioner {
    public void clearBuffer();
    public boolean isReadOnly(int offset);
    public StyleRange[] getStyleRanges(int offset, int length);
}
