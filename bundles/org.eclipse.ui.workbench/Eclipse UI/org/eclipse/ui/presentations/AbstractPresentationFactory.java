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
package org.eclipse.ui.presentations;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a factory for objects that control the appearance of editors and
 * views.
 * 
 * @since 3.0
 */
public abstract class AbstractPresentationFactory {

    public static final int ROLE_EDITOR_WORKBOOK = 0;

    public static final int ROLE_DOCKED_VIEW = 1;

    //public static final int ROLE_FAST_VIEW = 2;

    //public static final int ROLE_DETACHED_VIEW = 3;

    /**
     * Creates a part presentation.
     * <p>
     * The presentation creates its controls under the given parent composite.
     * </p>
     * <p>
     * The role argument indicates how this presentation is being used: whether
     * it presenting editors, regular docked views, fast views, or detached
     * views.
     * </p>
     * <p>
     * The flags SWT.SINGLE and SWT.MULTI indicate whether this presentation
     * will contain a single part or multiple parts (either SWT.SINGLE or
     * SWT.MULTIPLE is specified, not both). If SWT.SINGLE is specified, then
     * <code>addPart</code> will be called exactly once.
     * </p>
     * <p>
     * In Eclipse 3.0, SWT.SINGLE is used for presenting fast views and
     * detached views (which are not presented together with other views),
     * while SWT.MULTI is used for presenting editors and docked views. These
     * assumptions may change in future versions of Eclipse, so implementors
     * should base their choice on single vs. multi presentation on these flags
     * rather than on the role argument.
     * </p>
     * <p>
     * The remaining flags (SWT.MIN, SWT.MAX, SWT.MOVE) indicate whether the
     * corresponding operation is supported by the presentation. These
     * operations apply to the presentation as a whole, including all parts it
     * contains.
     * </p>
     * <p>
     * The perspective id and folder id can be used to tailor the choice of
     * presentation to particular perspectives or folders within a perspective.
     * To preserve Eclipse's independence of presentation and function, neither
     * the presentation factory nor the part presentation implementation should
     * refer directly to particular perspectives or folders (nor should
     * perspectives refer to particular presentations). Any mapping between
     * perspectives, folders and presentations should be done extrinsically,
     * for example using a separate extension point to associate a perspective
     * or folder with a presentation. Maintaining this separation of concerns
     * allows functional components (views, editors, perspectives) and
     * presentations to be mixed and matched independently across different
     * products.
     * </p>
     * 
     * @param parent
     *            the parent composite to use for the presentation's controls
     * @param site
     *            the site used for communication between the presentation and
     *            the workbench
     * @param role
     *            one of the ROLE_* constants indicating the role of this
     *            presentation
     * @param perspectiveId
     *            the id of the perspective containing this presentation
     * @param folderId
     *            the id of the folder corresponding to this presentation, or
     *            <code>null</code> if there is no corresponding folder or if
     *            it has no id
     * @return a newly created part presentation
     */
    public abstract StackPresentation createPresentation(Composite parent,
            IStackPresentationSite site, int role, 
            String perspectiveId, String folderId);
    
    /**
     * Creates the status line manager for the window.
     * 
     * @return the window's status line manager
     */
    public abstract IStatusLineManager createStatusLineManager();

    /**
     * Creates the control for the window's status line.
     * 
     * @param statusLine the window's status line manager
     * @param parent the parent composite
     * @return the window's status line control
     */
    public abstract Control createStatusLineControl(IStatusLineManager statusLine, Composite parent);
}