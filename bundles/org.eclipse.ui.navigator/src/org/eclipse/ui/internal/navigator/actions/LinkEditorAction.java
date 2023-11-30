/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.LinkHelperService;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.progress.UIJob;

/**
 * This action links the activate editor with the Navigator selection.
 *
 * @since 3.2
 */
public class LinkEditorAction extends Action implements ISelectionChangedListener, IPropertyListener {

	private IPartListener partListener;

	private final CommonNavigator commonNavigator;

	private final CommonViewer commonViewer;

	private final LinkHelperService linkService;

	private boolean ignoreSelectionChanged;
	private boolean ignoreEditorActivation;

	private UIJob activateEditorJob = new UIJob(CommonNavigatorMessages.Link_With_Editor_Job_) {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {

			if (!commonViewer.getControl().isDisposed()) {
				IStructuredSelection selection = commonViewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()) {

					if (selection.size() == 1) {
						ILinkHelper[] helpers = linkService.getLinkHelpersFor(selection.getFirstElement());
						if (helpers.length > 0) {
							ignoreEditorActivation = true;
							SafeRunner.run(new NavigatorSafeRunnable() {
								@Override
								public void run() throws Exception {
									helpers[0].activateEditor(commonNavigator.getSite().getPage(), selection);
								}
							});
							ignoreEditorActivation = false;
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	};

	private UIJob updateSelectionJob = new UIJob(CommonNavigatorMessages.Link_With_Editor_Job_) {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {

			if (!commonNavigator.getCommonViewer().getControl().isDisposed()) {
				SafeRunner.run(new NavigatorSafeRunnable() {
					@Override
					public void run() throws Exception {
						IWorkbenchPage page = commonNavigator.getSite().getPage();
						if (page != null) {
							IEditorPart editor = page.getActiveEditor();
							if (editor != null) {
								IEditorInput input = editor.getEditorInput();
								IStructuredSelection newSelection = linkService.getSelectionFor(input);
								if (!newSelection.isEmpty()) {
									ignoreSelectionChanged = true;
									commonNavigator.selectReveal(newSelection);
									ignoreSelectionChanged = false;
								}
							}
						}
					}
				});
			}

			return Status.OK_STATUS;
		}
	};

	/**
	 * Create a LinkEditorAction for the given navigator and viewer.
	 *
	 * @param aNavigator
	 *            The navigator which defines whether linking is enabled and
	 *            implements {@link ISetSelectionTarget}.
	 * @param aViewer
	 *            The common viewer instance with a
	 *            {@link INavigatorContentService}.
	 */
	public LinkEditorAction(CommonNavigator aNavigator, CommonViewer aViewer, LinkHelperService linkHelperService) {
		super(CommonNavigatorMessages.LinkEditorActionDelegate_0);
		activateEditorJob.setSystem(true);
		updateSelectionJob.setSystem(true);
		linkService = linkHelperService;
		setToolTipText(CommonNavigatorMessages.LinkEditorActionDelegate_1);
		commonNavigator = aNavigator;
		commonViewer = aViewer;
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, NavigatorPlugin.PLUGIN_ID + ".link_editor_action"); //$NON-NLS-1$
		init();
	}

	protected void init() {
		partListener = new IPartListener() {

			@Override
			public void partActivated(IWorkbenchPart part) {
				if (part instanceof IEditorPart && !ignoreEditorActivation) {
					updateSelectionJob.schedule(NavigatorPlugin.LINK_HELPER_DELAY);
				}
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
				if (part instanceof IEditorPart && !ignoreEditorActivation) {
					updateSelectionJob.schedule(NavigatorPlugin.LINK_HELPER_DELAY);
				}
			}

			@Override
			public void partClosed(IWorkbenchPart part) {

			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}

			@Override
			public void partOpened(IWorkbenchPart part) {
			}
		};

		updateLinkingEnabled(commonNavigator.isLinkingEnabled());

		commonNavigator.addPropertyListener(this);

	}

	public void dispose() {
		commonNavigator.removePropertyListener(this);
		if (isChecked()) {
			commonViewer.removePostSelectionChangedListener(this);
			commonNavigator.getSite().getPage().removePartListener(partListener);
		}

	}

	@Override
	public void run() {
		commonNavigator.setLinkingEnabled(!commonNavigator.isLinkingEnabled());
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (commonNavigator.isLinkingEnabled() && !ignoreSelectionChanged) {
			activateEditor();
		}
	}

	/**
	 * Update the active editor based on the current selection in the Navigator.
	 */
	protected void activateEditor() {
		IStructuredSelection selection = commonViewer.getStructuredSelection();
		if (selection != null && !selection.isEmpty()) {
			/*
			 * Create and schedule a UI Job to activate the editor in a valid
			 * Display thread
			 */
			activateEditorJob.schedule(NavigatorPlugin.LINK_HELPER_DELAY);
		}
	}

	@Override
	public void propertyChanged(Object aSource, int aPropertyId) {
		switch (aPropertyId) {
		case CommonNavigator.IS_LINKING_ENABLED_PROPERTY:
			updateLinkingEnabled(((CommonNavigator) aSource).isLinkingEnabled());
		}
	}

	private void updateLinkingEnabled(boolean toEnableLinking) {
		setChecked(toEnableLinking);

		if (toEnableLinking) {

			updateSelectionJob.schedule(NavigatorPlugin.LINK_HELPER_DELAY);

			commonViewer.addPostSelectionChangedListener(this);
			commonNavigator.getSite().getPage().addPartListener(partListener);
		} else {
			commonViewer.removePostSelectionChangedListener(this);
			commonNavigator.getSite().getPage().removePartListener(partListener);
		}
	}

}
