/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.SharedLabelProvider;
import org.eclipse.update.internal.ui.wizards.*;

/**
 *
 */
public class SelectedUpdatesView extends BaseTableView {
	private UpdateModelChangedListener modelListener;
	private Action deleteAction;
	private Action processAction;
	private Action processAllAction;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return UpdateUI
				.getDefault()
				.getUpdateModel()
				.getPendingChanges();
		}
	}

	class ViewLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		public String getColumnText(Object obj, int col) {
			return getText(obj);
		}
		public String getText(Object obj) {
			if (obj instanceof IFeatureAdapter) {
				try {
					IFeature feature = ((IFeatureAdapter) obj).getFeature(null);
					VersionedIdentifier versionedIdentifier =
						(feature != null)
							? feature.getVersionedIdentifier()
							: null;
					String version = "";
					if (versionedIdentifier != null)
						version = versionedIdentifier.getVersion().toString();
					String label = (feature != null) ? feature.getLabel() : "";
					return label + " " + version;
				} catch (CoreException e) {
				}
			}
			return super.getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			PendingChange job = (PendingChange) obj;
			int flags = 0;
			switch (job.getJobType()) {
				case PendingChange.INSTALL :
					flags = SharedLabelProvider.F_ADD;
					break;
				case PendingChange.UNINSTALL :
					flags = SharedLabelProvider.F_DEL;
					break;
				case PendingChange.CONFIGURE :
					break;
				case PendingChange.UNCONFIGURE :
					flags = SharedLabelProvider.F_UNCONFIGURED;
					break;
			}
			boolean patch = job.getFeature().isPatch();
			ImageDescriptor desc = patch?UpdateUIImages.DESC_EFIX_OBJ:
			UpdateUIImages.DESC_FEATURE_OBJ;
			return UpdateUI.getDefault().getLabelProvider().get(
				desc,
				flags);
		}
	}

	class UpdateModelChangedListener implements IUpdateModelChangedListener {
		/**
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectChanged(java.lang.Object, java.lang.String)
		 */
		public void objectChanged(final Object object, String property) {
			if (object instanceof PendingChange) {
				getTableViewer()
					.getControl()
					.getDisplay()
					.asyncExec(new Runnable() {
					public void run() {
						getTableViewer().update(object, null);
						updateTitle();
					}
				});
			}
		}

		/**
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsAdded(java.lang.Object, java.lang.Object)
		 */
		public void objectsAdded(Object parent, final Object[] children) {
			if (children[0] instanceof PendingChange) {
				getTableViewer()
					.getControl()
					.getDisplay()
					.asyncExec(new Runnable() {
					public void run() {
						getTableViewer().add(children);
						updateTitle();
					}
				});
			}
		}

		/**
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsRemoved(java.lang.Object, java.lang.Object)
		 */
		public void objectsRemoved(Object parent, final Object[] children) {
			if (children[0] instanceof PendingChange) {
				getTableViewer()
					.getControl()
					.getDisplay()
					.asyncExec(new Runnable() {
					public void run() {
						getTableViewer().remove(children);
						updateTitle();
					}
				});
			}
		}
	}

	public SelectedUpdatesView() {
		modelListener = new UpdateModelChangedListener();
		UpdateUI.getDefault().getLabelProvider().connect(this);
	}

	/**
	 * @see org.eclipse.update.internal.ui.views.BaseView#initProviders()
	 */
	public void initProviders() {
		TableViewer viewer = getTableViewer();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(UpdateUI.getDefault().getUpdateModel());
		WorkbenchHelp.setHelp(viewer.getControl(), "org.eclipse.update.ui.SelectedUpdatesView");
	}

	protected void fillContextMenu(IMenuManager manager) {
		processAction.setEnabled(canProcess());
		manager.add(processAction);
		manager.add(processAllAction);
		manager.add(new Separator());
		deleteAction.setEnabled(canDelete());
		manager.add(deleteAction);
		manager.add(new Separator());
		super.fillContextMenu(manager);
	}

	protected void makeActions() {
		deleteAction = new Action() {
			public void run() {
				doDelete();
			}
		};
		deleteAction.setText(
			UpdateUI.getString("ItemsView.popup.delete"));
			
		processAction = new Action() {
			public void run() {
				doProcess();
			}
		};
		processAction.setText(
			UpdateUI.getString("ItemsView.popup.process"));

		processAllAction = new Action() {
			public void run() {
				doProcessAll();
			}
		};
		processAllAction.setText(
			UpdateUI.getString("ItemsView.popup.processAll"));

		super.makeActions();
	}

	public void dispose() {
		hookListeners(false);
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	protected void partControlCreated() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		hookListeners(true);
	}

	protected void hookListeners(boolean add) {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		if (add)
			model.addUpdateModelChangedListener(modelListener);
		else
			model.removeUpdateModelChangedListener(modelListener);
	}

	protected void deleteKeyPressed(Widget widget) {
		if (canDelete())
			doDelete();
	}

	private boolean canProcess() {
		return canDelete();
	}

	private void doProcess() {
		IStructuredSelection ssel =
			(IStructuredSelection) getViewer().getSelection();
		ArrayList jobs = new ArrayList();
		for (Iterator iter = ssel.iterator(); iter.hasNext();) {
			PendingChange job = (PendingChange) iter.next();
			if (job.isProcessed() == false)
				jobs.add(job);
		}
		PendingChange[] result =
			(PendingChange[]) jobs.toArray(new PendingChange[jobs.size()]);
		doProcess(result);
	}

	private void doProcessAll() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		PendingChange[] jobs = model.getPendingChanges();
		doProcess(jobs);
	}

	private void doProcess(final PendingChange[] jobs) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				MultiInstallWizard wizard = new MultiInstallWizard(jobs);
				WizardDialog dialog =
					new InstallWizardDialog(
						getControl().getShell(),
						wizard);
				dialog.create();
				dialog.getShell().setSize(600, 500);
				dialog.open();
				if (wizard.isSuccessfulInstall())
					UpdateUI.requestRestart();
			}
		});
	}

	private void doDelete() {
		if (!confirmDeletion())
			return;
		IStructuredSelection ssel =
			(IStructuredSelection) getViewer().getSelection();
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();

		for (Iterator iter = ssel.iterator(); iter.hasNext();) {
			model.removePendingChange((PendingChange) iter.next());
		}
	}

	private boolean canDelete() {
		return getViewer().getSelection().isEmpty() == false;
	}

	private void updateTitle() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		PendingChange[] changes = model.getPendingChanges();

		int total = changes.length;
		int completed = 0;

		for (int i = 0; i < changes.length; i++) {
			PendingChange job = changes[i];
			if (job.isProcessed())
				completed++;
		}
		String baseName = getSite().getRegisteredName();
		String title =
			UpdateUI.getFormattedMessage(
				"ItemsView.title",
				new String[] {
					getSite().getRegisteredName(),
					"" + total,
					"" + completed });
		setTitle(title);
	}
}
