/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.ServiceFactory;
import org.eclipse.ui.part.Part;

/**
 * @since 3.1
 */
public abstract class PartGenerator {
    public abstract Part createPart(Composite parent, ServiceFactory context) throws ComponentException;
}
