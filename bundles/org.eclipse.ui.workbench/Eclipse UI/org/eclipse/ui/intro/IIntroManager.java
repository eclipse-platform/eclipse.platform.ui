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
package org.eclipse.ui.intro;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * An instance of this interface allows clients to manage the introduction
 * component of a workbench, as defined by the extension point
 * <code>org.eclipse.ui.intro</code>.
 * 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 */
public interface IIntroManager {

    /**
     * Close the intro part.
     * 
     * @param part
     *            the part to close
     * @return whether the intro part was closed
     */
    public boolean closeIntro(IIntroPart part);

    /**
     * Return the <code>IIntroPart</code> for this <code>IWorkbench</code>,
     * if any. This method will always return <code>null</code> until
     * showIntro is called successfully for the first time.
     * 
     * @return the <code>IIntroPart</code> or <code>null</code> if one is
     *         not available
     * @see IIntroManager#showIntro(IWorkbenchWindow, boolean)
     */
    public IIntroPart getIntro();

    /**
     * Return whether there is an intro associated with this workspace. This
     * does not answer whether the intro part exists, only if the workbench is
     * aware of an intro implementation.
     * 
     * @return whether the workbench is aware of an intro implementation
     */
    public boolean hasIntro();

    /**
     * Returns <code>false</code> if the intro part is full screen,
     * <code>true</code> if it is in stand-by mode.
     * 
     * @param part
     *            the <code>IIntroPart</code> to test
     * @return the standby state of the area.
     */
    boolean isIntroStandby(IIntroPart part);

    /**
     * Controls the intro site standby mode.
     * 
     * @param part
     *            the <code>IIntroPart</code> to set
     * @param standby
     *            if <code>false</code>, the intro area will be fully
     *            visible. Otherwise, it will go into standby mode and only be
     *            partially visible to allow users quick return to the starting
     *            point.
     * @since 3.0
     */
    public void setIntroStandby(IIntroPart part, boolean standby);

    /**
     * Show the intro part in the preferred window. If the intro part is
     * currently being shown in another window, make it the active window.
     * 
     * @param preferredWindow
     *            the preferred <code>IWorkbenchWindow</code>. If
     *            <code>null</code>, then the currently active window is
     *            used.
     * @param standby
     *            whether to show the intro in standby mode or not
     * @return the <code>IIntroPart</code>, or <code>null</code> if one is
     *         not available
     */
    public IIntroPart showIntro(IWorkbenchWindow preferredWindow,
            boolean standby);
}