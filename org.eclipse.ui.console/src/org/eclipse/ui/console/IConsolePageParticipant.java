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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console page participant contributes to the context menu and toolbar for a
 * console page. A page participant can also provide adapters for the page.
 * Participants are contributed via the
 * <code>org.eclispe.ui.console.consolePageParticipants</code> extension point.
 * <p>
 * Following is an example extension definition.
 * <pre>
 * </pre>
 * &lt;extension point="org.eclipse.ui.console.consolePageParticipants"&gt;
 *   &lt;consolePageParticipant
 *      id="com.example.ExamplePageParticipant"
 *      class="com.example.ExamplePageParticipant"&gt;
 *   &lt;/consolePageParticipant&gt;
 * &lt;/extension&gt;
 * </p>
 * The example page participant is contributed to all console pages. An optional
 * <code>enablement</code> attriubte may be specified to control which consoles
 * a page participant is applicable to.
 * <p>
 * Clients contributing console page participant extensions are intended to 
 * implement this interface.
 * </p>
 * @since 3.1
 */
public interface IConsolePageParticipant extends IAdaptable {
    /**
     * Called during page initialization. Marks the start of this 
     * page participant's lifecycle.
     * 
     * @param page the page corresponsing to the given console
     * @param console the console for which a page has been created
     */
    public void init(IPageBookViewPage page, IConsole console);
    
    /**
     * Disposes this page participant. Marks the end of this
     * page participant's lifecycle.
     */
    public void dispose();
    
    /**
     * Notification this participant's page has been activated.
     */
    public void activated();
    
    /**
     * Notification this participant's page has been deactivated.
     */
    public void deactivated();
    
}
