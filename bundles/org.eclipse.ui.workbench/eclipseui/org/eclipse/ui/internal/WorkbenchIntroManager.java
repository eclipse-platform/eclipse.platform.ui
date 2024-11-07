/*******************************************************************************
 * Copyright (c) 2004, 2011, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 463043
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityView;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IntroContentDetector;

/**
 * Workbench implementation of the IIntroManager interface.
 *
 * @since 3.0
 */
public class WorkbenchIntroManager implements IIntroManager {

	private final Workbench workbench;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param workbench the workbench instance
	 */
	WorkbenchIntroManager(Workbench workbench) {
		this.workbench = workbench;
		workbench.getExtensionTracker().registerHandler(new IExtensionChangeHandler() {

			@Override
			public void addExtension(IExtensionTracker tracker, IExtension extension) {
				// Do nothing
			}

			@Override
			public void removeExtension(IExtension source, Object[] objects) {
				for (Object object : objects) {
					if (object instanceof IIntroPart) {
						closeIntro((IIntroPart) object);
					}
				}

			}
		}, null);

	}

	/**
	 * The currently active introPart in this workspace, <code>null</code> if none.
	 */
	private IIntroPart introPart;

	@Override
	public boolean closeIntro(IIntroPart part) {
		if (introPart == null || !introPart.equals(part)) {
			return false;
		}

		IViewPart introView = getViewIntroAdapterPart();
		if (introView != null) {
			// assumption is that there is only ever one intro per workbench
			// if we ever support one per window then this will need revisiting
			IWorkbenchPage page = introView.getSite().getPage();
			if (page == null) {
				introPart = null;
				return true;
			}
			IViewReference reference = page.findViewReference(IIntroConstants.INTRO_VIEW_ID);
			page.hideView(introView);
			if (reference == null || reference.getPart(false) == null) {
				introPart = null;
				return true;
			}
			return false;
		}

		// if there is no part then null our reference
		introPart = null;

		return true;
	}

	@Override
	public IIntroPart showIntro(IWorkbenchWindow preferredWindow, boolean standby) {
		if (preferredWindow == null) {
			preferredWindow = this.workbench.getActiveWorkbenchWindow();
		}

		if (preferredWindow == null) {
			return null;
		}

		ViewIntroAdapterPart viewPart = getViewIntroAdapterPart();
		if (viewPart == null) {
			createIntro(preferredWindow);
		} else {
			try {
				IWorkbenchPage page = viewPart.getSite().getPage();
				IWorkbenchWindow window = page.getWorkbenchWindow();
				if (!window.equals(preferredWindow)) {
					window.getShell().setActive();
				}

				page.showView(IIntroConstants.INTRO_VIEW_ID);
			} catch (PartInitException e) {
				WorkbenchPlugin.log("Could not open intro", new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, //$NON-NLS-1$
						IStatus.ERROR, "Could not open intro", e)); //$NON-NLS-1$
			}
		}
		setIntroStandby(introPart, standby);
		return introPart;
	}

	/**
	 * @param testWindow the window to test
	 * @return whether the intro exists in the given window
	 */
	/* package */boolean isIntroInWindow(IWorkbenchWindow testWindow) {
		ViewIntroAdapterPart viewPart = getViewIntroAdapterPart();
		if (viewPart == null) {
			return false;
		}

		IWorkbenchWindow window = viewPart.getSite().getWorkbenchWindow();
		if (window.equals(testWindow)) {
			return true;
		}
		return false;
	}

	/**
	 * Create a new Intro area (a view, currently) in the provided window. If there
	 * is no intro descriptor for this workbench then no work is done.
	 *
	 * @param preferredWindow the window to create the intro in.
	 */
	private void createIntro(IWorkbenchWindow preferredWindow) {
		if (this.workbench.getIntroDescriptor() == null) {
			return;
		}

		IWorkbenchPage workbenchPage = preferredWindow.getActivePage();
		if (workbenchPage == null) {
			return;
		}
		try {
			workbenchPage.showView(IIntroConstants.INTRO_VIEW_ID);
		} catch (PartInitException e) {
			WorkbenchPlugin.log(IntroMessages.Intro_could_not_create_part, new Status(IStatus.ERROR,
					WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, IntroMessages.Intro_could_not_create_part, e));
		}
	}

	@Override
	public void setIntroStandby(IIntroPart part, boolean standby) {
		if (introPart == null || !introPart.equals(part)) {
			return;
		}

		ViewIntroAdapterPart viewIntroAdapterPart = getViewIntroAdapterPart();
		if (viewIntroAdapterPart == null) {
			return;
		}

		MPartStack introStack = getIntroStack(viewIntroAdapterPart);
		if (introStack == null)
			return;

		boolean isMaximized = isIntroMaximized(viewIntroAdapterPart);
		if (!isMaximized && !standby)
			introStack.getTags().add(IPresentationEngine.MAXIMIZED);
		else if (isMaximized && standby)
			introStack.getTags().remove(IPresentationEngine.MAXIMIZED);
	}

	private MPartStack getIntroStack(ViewIntroAdapterPart introAdapter) {
		ViewSite site = (ViewSite) introAdapter.getViewSite();

		MPart introModelPart = site.getModel();
		if (introModelPart.getCurSharedRef() != null) {
			MUIElement introPartParent = introModelPart.getCurSharedRef().getParent();
			if (introPartParent instanceof MPartStack) {
				return (MPartStack) introPartParent;
			}
		}

		return null;
	}

	private boolean isIntroMaximized(ViewIntroAdapterPart introAdapter) {
		MPartStack introStack = getIntroStack(introAdapter);
		if (introStack == null)
			return false;

		return introStack.getTags().contains(IPresentationEngine.MAXIMIZED);
	}

	@Override
	public boolean isIntroStandby(IIntroPart part) {
		if (introPart == null || !introPart.equals(part)) {
			return false;
		}

		ViewIntroAdapterPart viewIntroAdapterPart = getViewIntroAdapterPart();
		if (viewIntroAdapterPart == null) {
			return false;
		}

		return !isIntroMaximized(viewIntroAdapterPart);
	}

	@Override
	public IIntroPart getIntro() {
		return introPart;
	}

	/**
	 * @return the <code>ViewIntroAdapterPart</code> for this workbench,
	 *         <code>null</code> if it cannot be found.
	 */
	/* package */ViewIntroAdapterPart getViewIntroAdapterPart() {
		for (IWorkbenchWindow iWorkbenchWindow : this.workbench.getWorkbenchWindows()) {
			WorkbenchWindow window = (WorkbenchWindow) iWorkbenchWindow;
			MUIElement introPart = window.modelService.find(IIntroConstants.INTRO_VIEW_ID, window.getModel());
			if (introPart instanceof MPlaceholder) {
				MPlaceholder introPH = (MPlaceholder) introPart;
				MPart introModelPart = (MPart) introPH.getRef();
				CompatibilityView compatView = (CompatibilityView) introModelPart.getObject();
				if (compatView != null) {
					Object obj = compatView.getPart();
					if (obj instanceof ViewIntroAdapterPart)
						return (ViewIntroAdapterPart) obj;
				}
			}
		}
		return null;
	}

	/**
	 * @return a new IIntroPart. This has the side effect of setting the introPart
	 *         field to the new value.
	 */
	/* package */IIntroPart createNewIntroPart() throws CoreException {
		IntroDescriptor introDescriptor = workbench.getIntroDescriptor();
		introPart = introDescriptor == null ? null : introDescriptor.createIntro();
		if (introPart != null) {
			workbench.getExtensionTracker().registerObject(
					introDescriptor.getConfigurationElement().getDeclaringExtension(), introPart,
					IExtensionTracker.REF_WEAK);
		}
		return introPart;
	}

	@Override
	public boolean hasIntro() {
		return workbench.getIntroDescriptor() != null;
	}

	@Override
	public boolean isNewContentAvailable() {
		IntroDescriptor introDescriptor = workbench.getIntroDescriptor();
		if (introDescriptor == null) {
			return false;
		}
		try {
			IntroContentDetector contentDetector = introDescriptor.getIntroContentDetector();
			if (contentDetector != null) {
				return contentDetector.isNewContentAvailable();
			}
		} catch (CoreException ex) {
			WorkbenchPlugin.log(new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH, IStatus.WARNING,
					"Could not load intro content detector", ex)); //$NON-NLS-1$
		}
		return false;
	}
}
