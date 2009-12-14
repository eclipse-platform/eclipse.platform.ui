/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.swt.widgets.Menu;

/**
 * This interface can be implement by the breakpoint manager input to overrides the standard
 * breakpoint group local menu in the breakpoints view.
 * 
 * @since 3.6
 */
public interface IBreakpointOrganizerInputProvider {
	
	/**
	 * Fill the menu for the view input. The action is responsible to fire the model delta
	 * for update.
	 *  
	 * @param input the view input.
	 * @param context the presentation context.
	 * @param menu the menu to file the action.
	 */
	void fillMenu(Object input, IPresentationContext context, Menu menu);
	
}
