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
package org.eclipse.update.internal.ui.wizards;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;

/**
 * 
 */
public class InstallDeltaWizardPage extends WizardPage {
	private ISessionDelta[] deltas;
	private static final String KEY_TITLE = "InstallDeltaWizard.title";
	private static final String KEY_DESC = "InstallDeltaWizard.desc";
	private static final String KEY_LABEL = "InstallDeltaWizard.label";
	private static final String KEY_DELETE = "InstallDeltaWizard.delete";
	private static final String KEY_ERRORS = "InstallDeltaWizard.errors";
	private static final String KEY_MESSAGE = "InstallDeltaWizard.message";
	private CheckboxTreeViewer deltaViewer;
	private Button deleteButton;
	private Button errorsButton;
	private ArrayList features = new ArrayList();

	class DeltaContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		public boolean hasChildren(Object parent) {
			if (parent instanceof DeltaAdapter)
				return true;
			return false;
		}
		public Object[] getChildren(Object parent) {
			if (parent instanceof DeltaAdapter) {
				return ((DeltaAdapter) parent).getFeatures();
			}
			return new Object[0];
		}
		public Object getParent(Object child) {
			if (child instanceof DeltaFeatureAdapter) {
				return ((DeltaFeatureAdapter) child).getDeltaAdapter();
			}
			return null;
		}
		public Object[] getElements(Object input) {
			return features.toArray();
		}
	}

	class DeltaLabelProvider extends LabelProvider {
		public Image getImage(Object obj) {
			if (obj instanceof DeltaAdapter) {
				int flags = 0;
				DeltaAdapter adapter = (DeltaAdapter) obj;
				if (adapter.getStatus() != null)
					flags = UpdateLabelProvider.F_ERROR;
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_UPDATES_OBJ,
					flags);
			}
			if (obj instanceof DeltaFeatureAdapter) {
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_FEATURE_OBJ);
			}
			return super.getImage(obj);
		}
	}

	/**
	 * Constructor for InstallDeltaWizardPage.
	 * @param pageName
	 */
	public InstallDeltaWizardPage(ISessionDelta[] deltas) {
		super("installDeltaPage");
		this.deltas = deltas;
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
		UpdateUI.getDefault().getLabelProvider().connect(this);
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	private Object [] initializeFeatures() {
		ArrayList selection = new ArrayList();
		features = new ArrayList();
		for (int i = 0; i < deltas.length; i++) {
			ISessionDelta delta = deltas[i];
			DeltaAdapter adapter = new DeltaAdapter(delta);
			features.add(adapter);
			selection.add(adapter);
			adapter.addFeaturesTo(selection);
		}
		return selection.toArray();
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_LABEL));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		deltaViewer = new CheckboxTreeViewer(container, SWT.BORDER);
		deltaViewer.setContentProvider(new DeltaContentProvider());
		deltaViewer.setLabelProvider(new DeltaLabelProvider());
		deltaViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChanged(event.getElement(), event.getChecked());
			}
		});
		deltaViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				updateButtons((IStructuredSelection) e.getSelection());
			}
		});
		deltaViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parent, Object child) {
				if (child instanceof DeltaAdapter) {
					return !((DeltaAdapter) child).isRemoved();
				}
				return true;
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		deltaViewer.getControl().setLayoutData(gd);

		Composite buttonContainer = new Composite(container, SWT.NULL);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		buttonContainer.setLayout(layout);

		deleteButton = new Button(buttonContainer, SWT.PUSH);
		deleteButton.setEnabled(false);
		deleteButton.setText(UpdateUI.getString(KEY_DELETE));
		gd =
			new GridData(
				GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		deleteButton.setLayoutData(gd);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDelete();
			}
		});
		SWTUtil.setButtonDimensionHint(deleteButton);

		errorsButton = new Button(buttonContainer, SWT.PUSH);
		errorsButton.setEnabled(false);
		errorsButton.setText(UpdateUI.getString(KEY_ERRORS));
		gd =
			new GridData(
				GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		errorsButton.setLayoutData(gd);
		errorsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleShowErrors();
			}
		});
		SWTUtil.setButtonDimensionHint(errorsButton);

		Object[] checked = initializeFeatures();
		deltaViewer.setInput(this);
		deltaViewer.setCheckedElements(checked);
		
		dialogChanged();
		WorkbenchHelp.setHelp(
			container,
			"org.eclipse.update.ui.InstallDeltaWizardPage");
		setControl(container);
	}

	private void updateButtons(IStructuredSelection selection) {
		boolean enableShowErrors = false;
		boolean enableDelete = selection.size() > 0;

		if (selection.size() == 1) {
			Object obj = selection.getFirstElement();
			if (obj instanceof DeltaAdapter) {
				enableShowErrors = !((DeltaAdapter) obj).isValid();
			}
		}
		if (enableDelete) {
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (!(obj instanceof DeltaAdapter)) {
					enableDelete = false;
					break;
				}
			}
		}
		deleteButton.setEnabled(enableDelete);
		errorsButton.setEnabled(enableShowErrors);
	}

	private void handleDelete() {
		IStructuredSelection selection =
			(IStructuredSelection) deltaViewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof DeltaAdapter) {
				((DeltaAdapter) obj).setRemoved(true);
			}
		}
		deltaViewer.refresh();
		dialogChanged();
	}

	private void handleShowErrors() {
		IStructuredSelection sel =
			(IStructuredSelection) deltaViewer.getSelection();
		DeltaAdapter adapter = (DeltaAdapter) sel.getFirstElement();
		IStatus status = adapter.getStatus();

		if (status != null) {
			ErrorDialog.openError(getShell(), null, null, status);
			return;
		}
	}

	private void handleCheckStateChanged(Object obj, boolean checked) {
		if (obj instanceof DeltaFeatureAdapter) {
			DeltaFeatureAdapter dfeature = (DeltaFeatureAdapter) obj;
			dfeature.setSelected(checked);
			DeltaAdapter adapter = dfeature.getDeltaAdapter();
			deltaViewer.setGrayed(adapter, adapter.isMixedSelection());
			deltaViewer.setChecked(adapter, adapter.isSelected());
			adapter.resetStatus();
			deltaViewer.update(adapter, null);
		} else if (obj instanceof DeltaAdapter) {
			DeltaAdapter adapter = (DeltaAdapter) obj;
			adapter.setSelected(checked);
			deltaViewer.setGrayed(adapter, false);
			computeCheckedElements();
		}
		dialogChanged();
	}

	private void computeCheckedElements() {
		ArrayList checked = new ArrayList();
		for (int i = 0; i < features.size(); i++) {
			DeltaAdapter adapter = (DeltaAdapter) features.get(i);
			if (adapter.isRemoved())
				continue;
			if (adapter.isSelected()) {
				checked.add(adapter);
				DeltaFeatureAdapter df[] = adapter.getFeatures();
				for (int j = 0; j < df.length; j++) {
					if (df[j].isSelected())
						checked.add(df[j]);
				}
			}
		}
		deltaViewer.setCheckedElements(checked.toArray());
	}

	private void dialogChanged() {
		int nremoved = 0;
		int nselected = 0;
		int errors = 0;

		for (int i = 0; i < features.size(); i++) {
			DeltaAdapter adapter = (DeltaAdapter) features.get(i);
			if (adapter.isRemoved())
				nremoved++;
			else if (adapter.isSelected()) {
				nselected++;
				if (adapter.getStatus() != null)
					errors++;
			}
		}
		setPageComplete(errors == 0 && (nremoved > 0 || nselected > 0));
		String message = null;
		if (errors > 0)
			message = UpdateUI.getString(KEY_MESSAGE);
		setErrorMessage(message);
	}

	public DeltaAdapter[] getDeltaAdapters() {
		return (DeltaAdapter[]) features.toArray(
			new DeltaAdapter[features.size()]);
	}
}