/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.preferences.WorkbenchSettingsTransfer;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * The WorkbenchSettings handles the recording and restoring of workbench
 * settings.
 * 
 * @since 3.3
 * 
 */
public class WorkbenchLayoutSettingsTransfer extends WorkbenchSettingsTransfer {

	// private static final String WORKBENCH_LAYOUT_PATH =
	// ".metadata/.plugins/org.eclipse.ui.workbench/workbench.xml";
	// //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public WorkbenchLayoutSettingsTransfer() {
		super();
	}

	/**
	 * Record the sharable workbench state in a document.
	 * 
	 * @return {@link XMLMemento}
	 */
	public XMLMemento recordSharableWorkbenchState() {
		XMLMemento memento = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		IStatus status = saveSettings(memento);
		if (status.getSeverity() != IStatus.OK) {
			// don't use newWindow as parent because it has not yet been opened
			// (bug 76724)
			ErrorDialog.openError(null,
					WorkbenchMessages.Workbench_problemsSaving,
					WorkbenchMessages.Workbench_problemsSavingMsg, status);
		}
		return memento;
	}

	/**
	 * Save the settings to the memento.
	 * 
	 * @param memento
	 * @return IStatus
	 */
	private IStatus saveSettings(XMLMemento memento) {
		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.Workbench_problemsSaving, null);

		// Save the version number.
		memento.putString(IWorkbenchConstants.TAG_VERSION,
				Workbench.VERSION_STRING[1]);

		// Save the workbench windows.
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (int nX = 0; nX < windows.length; nX++) {
			WorkbenchWindow window = (WorkbenchWindow) windows[nX];
			IMemento childMem = memento
					.createChild(IWorkbenchConstants.TAG_WINDOW);
			result.merge(saveState(window, childMem));
		}
		return result;
	}

	/**
	 * Save the workbench window state.
	 * 
	 * @param window
	 * @param memento
	 * @return IStatus
	 */
	private IStatus saveState(WorkbenchWindow window, IMemento memento) {

		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.WorkbenchWindow_problemsSavingWindow, null);

		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null
				&& activePage.findView(IIntroConstants.INTRO_VIEW_ID) != null) {
			IMemento introMem = memento
					.createChild(IWorkbenchConstants.TAG_INTRO);
			boolean isStandby = getWorkbench()
					.getIntroManager()
					.isIntroStandby(getWorkbench().getIntroManager().getIntro());
			introMem.putString(IWorkbenchConstants.TAG_STANDBY, String
					.valueOf(isStandby));
		}

		// Save each page.
		IWorkbenchPage[] pages = window.getPages();
		for (int i = 0; i < pages.length; i++) {
			IWorkbenchPage page = pages[i];

			// Save perspective.
			IMemento pageMem = memento
					.createChild(IWorkbenchConstants.TAG_PAGE);
			pageMem.putString(IWorkbenchConstants.TAG_LABEL, page.getLabel());
			result.add(saveState((WorkbenchPage) page, pageMem));

			if (page == window.getActivePage()) {
				pageMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$
			}

			// Get the input.
			IAdaptable input = page.getInput();
			if (input != null) {
				IPersistableElement persistable = (IPersistableElement) Util
						.getAdapter(input, IPersistableElement.class);
				if (persistable == null) {
					WorkbenchPlugin
							.log("Unable to save page input: " //$NON-NLS-1$
									+ input
									+ ", because it does not adapt to IPersistableElement"); //$NON-NLS-1$

				} else {
					// Save input.
					IMemento inputMem = pageMem
							.createChild(IWorkbenchConstants.TAG_INPUT);
					inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID,
							persistable.getFactoryId());
					persistable.saveState(inputMem);
				}
			}
		}

		return result;
	}

	/**
	 * Save the state of the workbench page.
	 * 
	 * @param page
	 * @param pageMem
	 * @return IStatus
	 */
	private IStatus saveState(WorkbenchPage page, IMemento memento) {

		MultiStatus result = new MultiStatus(
				PlatformUI.PLUGIN_ID,
				IStatus.OK,
				NLS
						.bind(
								WorkbenchMessages.WorkbenchPage_unableToSavePerspective,
								page.getLabel()), null);

		saveEditorState( memento);

		IMemento viewMem = memento.createChild(IWorkbenchConstants.TAG_VIEWS);

		IViewReference[] refs = page.getViewReferences();

		for (int i = 0; i < refs.length; i++) {
			IViewReference viewReference = refs[i];
			String tagId = ViewFactory.getKey(viewReference);
			if (tagId != null) {
				IMemento childMem = viewMem
						.createChild(IWorkbenchConstants.TAG_VIEW);
				childMem.putString(IWorkbenchConstants.TAG_ID, tagId);
				String name = viewReference.getPartName();
				if (name != null) {
					childMem.putString(IWorkbenchConstants.TAG_PART_NAME, name);
				}
			}
		}

		// Create persp block.
		IMemento perspectiveMemento = memento
				.createChild(IWorkbenchConstants.TAG_PERSPECTIVES);
		if (page.getPerspective() != null) {
			perspectiveMemento.putString(
					IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE, page
							.getPerspective().getId());
		}
		if (page.getActivePart() != null) {
			if (page.getActivePart() instanceof IViewPart) {
				IViewReference ref = (IViewReference) page.getReference(page
						.getActivePart());
				if (ref != null) {
					perspectiveMemento.putString(
							IWorkbenchConstants.TAG_ACTIVE_PART, ViewFactory
									.getKey(ref));
				}
			} else {
				perspectiveMemento.putString(
						IWorkbenchConstants.TAG_ACTIVE_PART, page
								.getActivePart().getSite().getId());
			}
		}

		// Save each perspective in opened order
		IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();

		for (int i = 0; i < perspectives.length; i++) {
			IPerspectiveDescriptor perspectiveDescriptor = perspectives[i];
			IMemento gChildMem = perspectiveMemento
					.createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
			Perspective perspective = page
					.findPerspective(perspectiveDescriptor);
			perspective.saveState(gChildMem);
		}

		return result;

	}

	/**
	 * Save the editor state. Set it to be the defaults.
	 * 
	 * @param memento
	 */
	private void saveEditorState(IMemento memento) {

		IMemento editorsMemento = memento
				.createChild(IWorkbenchConstants.TAG_EDITORS);
		IMemento editorArea = editorsMemento
				.createChild(IWorkbenchConstants.TAG_AREA);
		editorArea.putString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK,
				EditorSashContainer.DEFAULT_WORKBOOK_ID);
		IMemento info = editorArea.createChild(IWorkbenchConstants.TAG_INFO);
		info.putString(IWorkbenchConstants.TAG_PART,
				EditorSashContainer.DEFAULT_WORKBOOK_ID);
		IMemento folder = info.createChild(IWorkbenchConstants.TAG_FOLDER);
		folder.putInteger(IWorkbenchConstants.TAG_APPEARANCE,
				PresentationFactoryUtil.ROLE_EDITOR);
		folder.putInteger(IWorkbenchConstants.TAG_EXPANDED,
				IStackPresentationSite.STATE_RESTORED);
		IMemento presentation = folder
				.createChild(IWorkbenchConstants.TAG_PRESENTATION);
		presentation.putString(IWorkbenchConstants.TAG_ID,
				getCurrentPresentationClassName());

	}

	/**
	 * Get the name of the current presentation class name.
	 * 
	 * @return String
	 */
	private String getCurrentPresentationClassName() {

		// update the current selection (used to look for changes on apply)
		String currentPresentationFactoryId = PrefUtil.getAPIPreferenceStore()
				.getString(
						IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID);
		// Workbench.getInstance().getPresentationId();

		AbstractPresentationFactory factory = WorkbenchPlugin.getDefault()
				.getPresentationFactory(currentPresentationFactoryId);

		if (factory == null)
			factory = WorkbenchPlugin.getDefault().getPresentationFactory(
					IWorkbenchConstants.DEFAULT_PRESENTATION_ID);
		return factory.getClass().getName();

	}

	/**
	 * Return the workbench we are using.
	 * 
	 * @return IWorkbench
	 */
	private IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.SettingsTransfer#transferSettings(org.eclipse.core.runtime.IPath)
	 */
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		try {
			File workspaceFile = createFileAndDirectories(newWorkspaceRoot);

			if (workspaceFile == null)
				return new Status(
						IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH,
						WorkbenchMessages.WorkbenchSettings_CouldNotCreateDirectories);

			FileOutputStream stream = new FileOutputStream(workspaceFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			XMLMemento memento = XMLMemento
					.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
			IStatus status = saveSettings(memento);
			if (status.getSeverity() != IStatus.OK)
				return status;

			memento.save(writer);
			writer.close();

		} catch (IOException e) {
			return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					WorkbenchMessages.Workbench_problemsSavingMsg, e);

		}

		return Status.OK_STATUS;
	}

	/**
	 * Create the parent directories for the workbench layout file and then
	 * return the File.
	 * 
	 * @param newWorkspaceRoot
	 * @return File the new layout file. Return <code>null</code> if the file
	 *         cannot be created.
	 */
	private File createFileAndDirectories(IPath newWorkspaceRoot) {
		IPath newWorkspaceLocation = getNewWorkbenchStateLocation(
				newWorkspaceRoot).append(
				Workbench.DEFAULT_WORKBENCH_STATE_FILENAME);
		File workspaceFile = new File(newWorkspaceLocation.toOSString());

		File parent = workspaceFile.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs())
				return null;
		}

		return workspaceFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.SettingsTransfer#getName()
	 */
	public String getName() {
		return WorkbenchMessages.WorkbenchLayoutSettings_Name;
	}

}
