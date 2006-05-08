/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.resources.IFile;

/**
 * This interface is used as API for the Readme parser extension 
 * point. The default implementation simply looks for lines
 * in the file that start with a number and assumes that they
 * represent sections. Tools are allowed to replace this 
 * algorithm by defining an extension and supplying an 
 * alternative that implements this interface.
 */
public interface IReadmeFileParser {
    /**
     * Parses the contents of the provided file
     * and generates a collection of sections.
     */
    public MarkElement[] parse(IFile readmeFile);
}
