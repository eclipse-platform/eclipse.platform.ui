/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.helpers;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * @since 3.5
 *
 */
public abstract class TestFacade {
	public static TweakKey KEY = new Tweaklets.TweakKey(TestFacade.class);

	static {
		// load the default, but carefully
		try {
			Class testClass = TestFacade.class.getClassLoader().loadClass("org.eclipse.ui.tests.helpers.TestFacadeImpl");
			Tweaklets.setDefault(TestFacade.KEY, testClass.newInstance());
		} catch (ClassNotFoundException e) {
			// we're in e4 land, that boat won't float
		} catch (InstantiationException e) {
			// we're in e4 land, that boat won't float
		} catch (IllegalAccessException e) {
			// we're in e4 land, that boat won't float
		} catch (Error err) {
			// unresolved compilation problems ... tests in the workspace, but that's OK
		}
	}
	
	public abstract void assertActionSetId(IWorkbenchPage page, String id, boolean condition);
	
	public abstract int getActionSetCount(IWorkbenchPage page);
	
	public abstract void addFastView(IWorkbenchPage page, IViewReference ref);
	
	public abstract IStatus saveState(IWorkbenchPage page, IMemento memento);
	
	public abstract IViewReference[] getFastViews(IWorkbenchPage page);
	
	public abstract ArrayList getPerspectivePartIds(IWorkbenchPage page, String folderId);
	
	public abstract boolean isClosableInPerspective(IViewReference ref);
	
	public abstract boolean isMoveableInPerspective(IViewReference ref);
	
	public abstract boolean isFastView(IWorkbenchPage page, IViewReference ref);

	public abstract void saveableHelperSetAutomatedResponse(int response);

	public abstract void isSlavePageService(IPageService slaveService);

	public abstract IContributionItem getFVBContribution(IWorkbenchPage page);

	public abstract void setFVBTarget(IContributionItem menuContribution,
			IViewReference viewRef);

	public abstract boolean isViewPaneVisible(IViewReference viewRef);
	
	public abstract Control getPaneControl(IWorkbenchPartSite site);

	public abstract boolean isViewToolbarVisible(IViewReference viewRef);

	public abstract boolean isSlavePartService(IPartService slaveService);

	public abstract boolean isSlaveSelectionService(ISelectionService slaveService);
}
