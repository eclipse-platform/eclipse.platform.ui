/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.radioGroup;

import org.eclipse.swt.events.SelectionEvent;

/**
 * Interface VetoableSelectionListener.  An interface for SelectionListeners
 * that permit the new selection to be vetoed before widgetSelected or
 * widgetDefaultSelected is called.
 */
public interface VetoableSelectionListener {
   /**
    * Method widgetCanChangeSelection.  Indicates that the selection is
    * about to be changed.  Setting e.doit to false will prevent the
    * selection from changing.
    *
    * @param e The SelectionEvent that is being processed.
    */
   public void canWidgetChangeSelection(SelectionEvent e);
}
