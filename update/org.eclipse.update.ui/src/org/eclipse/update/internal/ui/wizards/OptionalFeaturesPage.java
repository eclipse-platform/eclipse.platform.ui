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
package org.eclipse.update.internal.ui.wizards;
import java.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.operations.*;

public class OptionalFeaturesPage extends BannerPage implements IDynamicPage {
	// NL keys
	private static final String KEY_TITLE =
		"InstallWizard.OptionalFeaturesPage.title"; //$NON-NLS-1$
	private static final String KEY_DESC =
		"InstallWizard.OptionalFeaturesPage.desc"; //$NON-NLS-1$
	private static final String KEY_TREE_LABEL =
		"InstallWizard.OptionalFeaturesPage.treeLabel"; //$NON-NLS-1$
	private static final String KEY_SELECT_ALL =
		"InstallWizard.OptionalFeaturesPage.selectAll"; //$NON-NLS-1$
	private static final String KEY_DESELECT_ALL =
		"InstallWizard.OptionalFeaturesPage.deselectAll"; //$NON-NLS-1$
	private CheckboxTreeViewer treeViewer;
	private Button selectAllButton;
	private Button deselectAllButton;
	private IInstallConfiguration config;
	private JobRoot[] jobRoots;

	class TreeContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
			if (parent instanceof JobRoot) {
				return ((JobRoot) parent).getElements();
			}
			if (parent instanceof FeatureHierarchyElement) {
				FeatureHierarchyElement fe = (FeatureHierarchyElement) parent;
				Object root = fe.getRoot();
				boolean oldFeature = false;
				if (root instanceof JobRoot) {
					IInstallFeatureOperation job = ((JobRoot)root).getJob();
					boolean patch = UpdateUtils.isPatch(job.getFeature());
					oldFeature = job.getOldFeature() != null;
					return fe.getChildren(oldFeature, patch, config);
				}
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			return null;
		}

		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}

		public Object[] getElements(Object input) {
			if (jobRoots == null)
				return new Object[0];
			return jobRoots;
		}
	}

	class TreeLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof JobRoot) {
				IFeature feature = ((JobRoot) obj).getJob().getFeature();
				return feature.getLabel()
					+ " " //$NON-NLS-1$
					+ feature.getVersionedIdentifier().getVersion().toString();
			}
			if (obj instanceof FeatureHierarchyElement) {
				String name = ((FeatureHierarchyElement) obj).getLabel();
				if (name != null)
					return name;
			}

			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			return UpdateUI.getDefault().getLabelProvider().get(
				UpdateUIImages.DESC_FEATURE_OBJ);
		}
	}

	/**
	 * Constructor for ReviewPage2
	 */
	public OptionalFeaturesPage(IInstallConfiguration config) {
		super("OptionalFeatures"); //$NON-NLS-1$
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
		this.config = config;
		UpdateUI.getDefault().getLabelProvider().connect(this);
	}

	public void setJobs(IInstallFeatureOperation[] jobs) {
		jobRoots = new JobRoot[jobs.length];
		for (int i = 0; i < jobs.length; i++) {
			jobRoots[i] = new JobRoot(jobs[i]);
		}
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}


	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);

		createCheckboxTreeViewer(client);

		selectAllButton = new Button(client, SWT.PUSH);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}
		});
		selectAllButton.setText(UpdateUI.getString(KEY_SELECT_ALL));
		GridData gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		selectAllButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(selectAllButton);

		deselectAllButton = new Button(client, SWT.PUSH);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}
		});
		deselectAllButton.setText(UpdateUI.getString(KEY_DESELECT_ALL));
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		deselectAllButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(deselectAllButton);
		WorkbenchHelp.setHelp(client, "org.eclipse.update.ui.MultiOptionalFeaturesPage2"); //$NON-NLS-1$
		
		Dialog.applyDialogFont(parent);
		
		return client;
	}

	private void createCheckboxTreeViewer(Composite parent) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_TREE_LABEL));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		treeViewer =
			new CheckboxTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		treeViewer.getTree().setLayoutData(gd);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				handleChecked(e.getElement(), e.getChecked());
			}
		});
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setInput(this);
	}

	public void setVisible(boolean visible) {
		if (visible) {
			treeViewer.setInput(jobRoots);
			initializeStates();
		}
		super.setVisible(visible);
		if (visible) {
			treeViewer.getTree().setFocus();
		}		
	}

	private void initializeStates() {
		ArrayList checked = new ArrayList();
		ArrayList grayed = new ArrayList();
		ArrayList editable = new ArrayList();

		for (int i = 0; i < jobRoots.length; i++) {
			checked.add(jobRoots[i]);
			grayed.add(jobRoots[i]);
			boolean update = jobRoots[i].getJob().getOldFeature() != null;
			initializeStates(update, jobRoots[i].getElements(), checked, grayed, editable);
		}
		treeViewer.setCheckedElements(checked.toArray());
		treeViewer.setGrayedElements(grayed.toArray());
		selectAllButton.setEnabled(editable.size()>0);
		deselectAllButton.setEnabled(editable.size()>0);
	}

	private void initializeStates(
		boolean update,
		Object[] elements,
		ArrayList checked,
		ArrayList grayed,
		ArrayList editable) {

		for (int i = 0; i < elements.length; i++) {
			FeatureHierarchyElement element =
				(FeatureHierarchyElement) elements[i];
			if (element.isChecked())
				checked.add(element);
			if (!element.isEditable())
				grayed.add(element);
			else
				editable.add(element);
			initializeStates(update, element.getChildren(), checked, grayed, editable);
		}
	}

	private void selectAll(boolean value) {
		ArrayList selected = new ArrayList();
		for (int i = 0; i < jobRoots.length; i++) {
			IInstallFeatureOperation job = jobRoots[i].getJob();
			selected.add(job);
			Object[] elements = jobRoots[i].getElements();
			for (int j = 0; j < elements.length; j++) {
				FeatureHierarchyElement element = (FeatureHierarchyElement) elements[j];
				selectAll(job.getOldFeature() != null, element, selected, value);
			}
		}
		treeViewer.setCheckedElements(selected.toArray());
	}

	private void selectAll(
		boolean update,
		FeatureHierarchyElement ref,
		ArrayList selected,
		boolean value) {

		if (!ref.isOptional()) {
			selected.add(ref);
		} else {
			if (ref.isEditable()) {
				ref.setChecked(value);
				if (value)
					selected.add(ref);
			} else if (ref.isChecked()) {
				selected.add(ref);
			}
		}
		Object[] included = ref.getChildren();
		for (int i = 0; i < included.length; i++) {
			selectAll(update, (FeatureHierarchyElement) included[i], selected, value);
		}
	}

	private void handleChecked(Object element, boolean checked) {
		if (element instanceof JobRoot) {
			treeViewer.setChecked(element, !checked);
			return;
		}
		FeatureHierarchyElement fe = (FeatureHierarchyElement) element;

		if (!fe.isEditable())
			treeViewer.setChecked(element, !checked);
		else {
			// update the result
			fe.setChecked(checked);
		}
	}
	
	public IFeature[] getUnconfiguredOptionalFeatures(IInstallFeatureOperation job, IConfiguredSite targetSite) {
		for (int i = 0; i < jobRoots.length; i++) {
			if (job.equals(jobRoots[i].getJob())) {
				return jobRoots[i].getUnconfiguredOptionalFeatures(config, targetSite);
			}
		}
		return new IFeature[0];
	}
	
	public IFeatureReference[] getCheckedOptionalFeatures(IInstallFeatureOperation currentJob) {
		HashSet set = new HashSet();
		JobRoot jobRoot = null;

		for (int i = 0; i < jobRoots.length; i++) {
			if (currentJob.equals(jobRoots[i].getJob())) {
				jobRoot = jobRoots[i];
				break;
			}
		}
		if (jobRoot == null)
			return new IFeatureReference[0];

		IInstallFeatureOperation job = jobRoot.getJob();
		boolean update = job.getOldFeature() != null;
		boolean patch = UpdateUtils.isPatch(job.getFeature());
		FeatureHierarchyElement[] elements = jobRoot.getElements();
		for (int i = 0; i < elements.length; i++) {
			elements[i].addCheckedOptionalFeatures(update, patch, config, set);
		}
		return (IFeatureReference[]) set.toArray(new IFeatureReference[set.size()]);
	}
}
