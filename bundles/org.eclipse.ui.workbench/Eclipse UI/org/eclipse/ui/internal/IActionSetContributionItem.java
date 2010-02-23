/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

/**
 * This interface should be implemented by all contribution items
 * defined by an action set.
 */
public interface IActionSetContributionItem {

    /**
     * Returns the action set id.
     */
    public String getActionSetId();

    /**
     * Sets the action set id.
     */
    public void setActionSetId(String newActionSetId);
}
