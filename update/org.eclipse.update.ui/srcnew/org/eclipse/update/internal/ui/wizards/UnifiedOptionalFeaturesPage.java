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

public class UnifiedOptionalFeaturesPage extends BannerPage2 implements IDynamicPage2 {
	// NL keys
	private static final String KEY_TITLE =
		"InstallWizard.OptionalFeaturesPage.title";
	private static final String KEY_DESC =
		"InstallWizard.OptionalFeaturesPage.desc";
	private static final String KEY_TREE_LABEL =
		"InstallWizard.OptionalFeaturesPage.treeLabel";
	private static final String KEY_SELECT_ALL =
		"InstallWizard.OptionalFeaturesPage.selectAll";
	private static final String KEY_DESELECT_ALL =
		"InstallWizard.OptionalFeaturesPage.deselectAll";
	private CheckboxTreeViewer treeViewer;
	private IInstallConfiguration config;
	private JobRoot[] jobRoots;

	class TreeContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
			if (parent instanceof JobRoot) {
				return ((JobRoot) parent).getElements();
			}
			if (parent instanceof FeatureHierarchyElement2) {
				FeatureHierarchyElement2 fe = (FeatureHierarchyElement2) parent;
				Object root = fe.getRoot();
				boolean oldFeature = false;
				if (root instanceof JobRoot) {
					PendingOperation job = ((JobRoot)root).getJob();
					boolean patch = UpdateUI.isPatch(job.getFeature());
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
					+ " "
					+ feature.getVersionedIdentifier().getVersion().toString();
			}
			if (obj instanceof FeatureHierarchyElement2) {
				FeatureHierarchyElement2 fe = (FeatureHierarchyElement2) obj;
				String name = fe.getLabel();
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
	public UnifiedOptionalFeaturesPage(IInstallConfiguration config) {
		super("OptionalFeatures");
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
		this.config = config;
		UpdateUI.getDefault().getLabelProvider().connect(this);
	}

	public void setJobs(PendingOperation[] jobs) {
		jobRoots = new JobRoot[jobs.length];
		for (int i = 0; i < jobs.length; i++) {
			jobRoots[i] = new JobRoot(config, jobs[i]);
		}
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		createCheckboxTreeViewer(client);
		Button selectAllButton = new Button(client, SWT.PUSH);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}
		});
		selectAllButton.setText(
			UpdateUI.getString(KEY_SELECT_ALL));
		GridData gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		selectAllButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(selectAllButton);

		Button deselectAllButton = new Button(client, SWT.PUSH);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}
		});
		deselectAllButton.setText(
			UpdateUI.getString(KEY_DESELECT_ALL));
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		deselectAllButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(deselectAllButton);
		WorkbenchHelp.setHelp(client, "org.eclipse.update.ui.MultiOptionalFeaturesPage2");
		return client;
	}

	private void createCheckboxTreeViewer(Composite parent) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_TREE_LABEL));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		treeViewer =
			new CheckboxTreeViewer(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(gd);
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

		for (int i = 0; i < jobRoots.length; i++) {
			JobRoot jobRoot = jobRoots[i];
			PendingOperation job = jobRoot.getJob();
			checked.add(jobRoot);
			grayed.add(jobRoot);
			boolean update = job.getOldFeature() != null;
			initializeStates(update, jobRoot.getElements(), checked, grayed);
		}
		treeViewer.setCheckedElements(checked.toArray());
		treeViewer.setGrayedElements(grayed.toArray());
	}

	private void initializeStates(
		boolean update,
		Object[] elements,
		ArrayList checked,
		ArrayList grayed) {

		for (int i = 0; i < elements.length; i++) {
			FeatureHierarchyElement2 element =
				(FeatureHierarchyElement2) elements[i];
			if (element.isChecked())
				checked.add(element);
			if (!element.isEditable())
				grayed.add(element);
			Object[] children = element.getChildren();
			initializeStates(update, children, checked, grayed);
		}
	}

	private void selectAll(boolean value) {
		ArrayList selected = new ArrayList();

		for (int i = 0; i < jobRoots.length; i++) {
			JobRoot jobRoot = jobRoots[i];
			PendingOperation job = jobRoot.getJob();
			selected.add(job);
			Object[] elements = jobRoot.getElements();
			for (int j = 0; j < elements.length; j++) {
				FeatureHierarchyElement2 element =
					(FeatureHierarchyElement2) elements[j];
				selectAll(
					job.getOldFeature() != null,
					element,
					selected,
					value);
			}
		}
		treeViewer.setCheckedElements(selected.toArray());
	}

	private void selectAll(
		boolean update,
		FeatureHierarchyElement2 ref,
		ArrayList selected,
		boolean value) {

		if (ref.isOptional() == false)
			selected.add(ref);
		else {
			if (ref.isEditable()) {
				ref.setChecked(value);
				if (value)
					selected.add(ref);
			} else {
				if (ref.isChecked())
					selected.add(ref);
			}
		}
		Object[] included = ref.getChildren();
		for (int i = 0; i < included.length; i++) {
			FeatureHierarchyElement2 fe = (FeatureHierarchyElement2) included[i];
			selectAll(update, fe, selected, value);
		}
	}

	private void handleChecked(Object element, boolean checked) {
		if (element instanceof JobRoot) {
			treeViewer.setChecked(element, !checked);
			return;
		}
		FeatureHierarchyElement2 fe = (FeatureHierarchyElement2) element;

		if (!fe.isEditable())
			treeViewer.setChecked(element, !checked);
		else {
			// update the result
			fe.setChecked(checked);
		}
	}
	
	public FeatureHierarchyElement2[] getOptionalElements(PendingOperation job) {
		for (int i = 0; i < jobRoots.length; i++) {
			JobRoot root = jobRoots[i];
			PendingOperation curr = root.getJob();
			if (job.equals(curr)) {
				return root.getElements();
			}
		}
		return null;
	}
	
	public IFeatureReference[] getCheckedOptionalFeatures(PendingOperation currentJob) {
		HashSet set = new HashSet();
		JobRoot jobRoot = null;

		for (int i = 0; i < jobRoots.length; i++) {
			JobRoot root = jobRoots[i];
			PendingOperation job = root.getJob();
			if (currentJob.equals(job)) {
				jobRoot = root;
				break;
			}
		}
		if (jobRoot == null)
			return new IFeatureReference[0];

		PendingOperation job = jobRoot.getJob();
		boolean update = job.getOldFeature() != null;
		boolean patch = UpdateUI.isPatch(job.getFeature());
		FeatureHierarchyElement2[] elements = jobRoot.getElements();
		for (int i = 0; i < elements.length; i++) {
			elements[i].addCheckedOptionalFeatures(update, patch, config, set);
		}
		return (IFeatureReference[]) set.toArray(
			new IFeatureReference[set.size()]);
	}
}
