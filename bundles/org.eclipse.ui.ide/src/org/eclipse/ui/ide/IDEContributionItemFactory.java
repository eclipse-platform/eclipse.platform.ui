/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.actions.BuildContributionItem;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Access to standard contribution items provided by the IDE workbench (including
 * those of the generic workbench).
 * <p>
 * Most of the functionality of this class is provided by
 * static methods and fields.
 * Example usage:
 * <pre>
 * MenuManager menu = ...;
 * IContributionItem reEdit
 * 	  = IDEContributionItemFactory.REOPEN_EDITORS.create(window);
 * menu.add(reEdit);
 * </pre>
 * </p>
 * <p>
 * Clients may declare subclasses that provide additional application-specific
 * contribution item factories.
 * </p>
 * 
 * @since 3.0
 */
public abstract class IDEContributionItemFactory extends ContributionItemFactory {

	/**
	 * Creates a new IDE workbench contribution item factory with the given id.
	 * 
	 * @param contributionItemId the id of contribution items created by this factory
	 */
	public IDEContributionItemFactory(String contributionItemId) {
		super(contributionItemId);
	}

	/**
	 * IDE-specific workbench contribution item (id "build"): Incremental build.
	 * This item maintains its enablement and visible state.
	 */
	public static final ContributionItemFactory BUILD = new ContributionItemFactory("build") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ContributionItemFactory */
		public IContributionItem create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			GlobalBuildAction action = new GlobalBuildAction(window, IncrementalProjectBuilder.INCREMENTAL_BUILD);
			action.setId(getId());
			return new BuildContributionItem(action, window);
		}
	};
		
	/**
	 * IDE-specific workbench contribution item (id "buildProject"): Incremental build project.
	 * This item maintains its enablement and visible state.
	 */
	public static final ContributionItemFactory BUILD_PROJECT = new ContributionItemFactory("buildProject") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IContributionItem create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.buildProject")); //$NON-NLS-1$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.buildProject.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.project.buildProject"); //$NON-NLS-1$
			return new BuildContributionItem(action, window);
		}
	};
}
