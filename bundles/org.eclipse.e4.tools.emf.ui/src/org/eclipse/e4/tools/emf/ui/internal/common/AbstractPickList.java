/*******************************************************************************
 * Copyright (c) 2014, 2017 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 466731
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.List;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;

/**
 * <p>
 * A composite widget containing a combo for picking items, a list with
 * selectable items, and action buttons for modifying the list.
 * </p>
 *
 * @author Steven Spungin
 *
 */
public abstract class AbstractPickList extends Composite {

	public static enum PickListFeatures {
		NO_ORDER, NO_PICKER, NO_GROUP
	}

	protected ComboViewer picker;
	protected TableViewer viewer;

	private final Composite composite;
	private final Composite toolBar;

	protected final Button tiAdd;
	protected final Button tiRemove;
	protected final Button tiUp;
	protected final Button tiDown;
	// private final AutoCompleteField autoCompleteField;

	@Deprecated
	public AbstractPickList(Composite parent, int style, List<PickListFeatures> listFeatures, Messages messages,
			AbstractComponentEditor componentEditor) {
		this(parent, style, listFeatures, componentEditor);
	}

	public AbstractPickList(Composite parent, int style, List<PickListFeatures> listFeatures,
			AbstractComponentEditor componentEditor) {
		super(parent, style);

		// TODO remove dependency AbstractComponentEditor. They
		// are only needed for labels and icons.

		setLayout(new FillLayout());

		if (listFeatures != null && listFeatures.contains(PickListFeatures.NO_GROUP)) {
			composite = new Composite(this, SWT.NONE);
		} else {
			composite = new Group(this, SWT.NONE);
		}
		composite.setLayout(new GridLayout(1, false));

		final Composite comp = new Composite(composite, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		picker = new ComboViewer(comp, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		final Combo control = picker.getCombo();
		GridData gdpicker = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdpicker.minimumWidth = 180;
		gdpicker.grabExcessHorizontalSpace = true;
		control.setLayoutData(gdpicker);

		// ComboContentAdapter controlContentAdapter = new ComboContentAdapter()
		// {
		// @Override
		// public void setControlContents(Control control, String text1, int
		// cursorPosition) {
		// super.setControlContents(control, text1, cursorPosition);
		// Object valueInModel = proposals.get(text1);
		// if (valueInModel != null) {
		// getPicker().setSelection(new StructuredSelection(valueInModel));
		// }
		// }
		// };
		// autoCompleteField = new AutoCompleteField(control,
		// controlContentAdapter, new String[0]);

		toolBar = new Composite(comp, SWT.NONE);
		layout = new GridLayout(5, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		toolBar.setLayout(layout);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		toolBar.setFont(composite.getFont());

		picker.addOpenListener(event -> addPressed());

		tiAdd = new Button(toolBar, SWT.PUSH);
		tiAdd.setToolTipText(Messages.ModelTooling_Common_AddEllipsis);
		tiAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		tiAdd.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_table_add));
		tiAdd.setFont(getButtonFont());
		tiAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPressed();
			}
		});

		tiRemove = new Button(toolBar, SWT.PUSH);
		tiRemove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		tiRemove.setToolTipText(Messages.ModelTooling_Common_Remove);
		tiRemove.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_table_delete));
		tiRemove.setFont(getButtonFont());
		tiRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removePressed();
			}
		});

		tiDown = new Button(toolBar, SWT.PUSH);
		tiDown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		tiDown.setToolTipText(Messages.ModelTooling_Common_Down);
		tiDown.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_arrow_down));
		tiDown.setFont(getButtonFont());
		tiDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveDownPressed();
				boolean enable = viewer.getTable().getSelectionIndex() + 1 < getItemCount();
				if (tiDown.isEnabled() != enable) {
					tiDown.setEnabled(enable);
				}
				if (enable == false && !tiUp.isEnabled()) {
					tiUp.setEnabled(true);
				}
				tiDown.setFocus();
			}
		});

		tiUp = new Button(toolBar, SWT.PUSH);
		tiUp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		tiUp.setToolTipText(Messages.ModelTooling_Common_Up);
		tiUp.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_arrow_up));
		tiUp.setFont(getButtonFont());
		tiUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveUpPressed();
				boolean enable = viewer.getTable().getSelectionIndex() > 0;
				if (tiUp.isEnabled() != enable) {
					tiUp.setEnabled(enable);
				}
				if (enable == false && !tiDown.isEnabled()) {
					tiDown.setEnabled(true);
				}
				tiUp.setFocus();
			}
		});

		viewer = new TableViewer(composite);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		viewer.addSelectionChangedListener(event -> updateUiState());
		updateUiState();

		if (listFeatures != null) {
			if (listFeatures.contains(PickListFeatures.NO_ORDER)) {
				tiDown.dispose();
				tiUp.dispose();
			}
			if (listFeatures.contains(PickListFeatures.NO_PICKER)) {
				((GridData) picker.getControl().getLayoutData()).exclude = true;
				picker.getControl().setVisible(false);
				pack();
			}
		}

	}

	protected Font getButtonFont() {
		return toolBar.getFont();
	}

	protected void addPressed() {
	}

	abstract protected int getItemCount();

	public TableViewer getList() {
		return viewer;
	}

	public void setInput(Object input) {
		getPicker().setInput(input);

		// proposals = toProposals(input);
		// final Set<String> keySet = proposals.keySet();
		// autoCompleteField.setProposals(keySet.toArray(new String[keySet.size()]));
	}

	public IStructuredSelection getSelection() {
		return getPicker().getStructuredSelection();
	}

	public void setSelection(ISelection selection) {
		getPicker().setSelection(selection);
	}

	public void setContentProvider(IContentProvider contentProvider) {
		getPicker().setContentProvider(contentProvider);
	}

	public void setLabelProvider(ILabelProvider labelProvider) {
		getPicker().setLabelProvider(labelProvider);
	}

	private ComboViewer getPicker() {
		return picker;
	}

	public void setComparator(ViewerComparator comparator) {
		getPicker().setComparator(comparator);
	}

	protected Composite getToolBar() {
		return toolBar;
	}

	protected void moveDownPressed() {
	}

	protected void moveUpPressed() {
	}

	protected void removePressed() {
	}

	public void setText(String text) {
		if (composite instanceof Group) {
			((Group) composite).setText(text);
		}
	}

	public void updateUiState() {
		TableViewer tableViewer = getList();
		Table table = tableViewer.getTable();
		int itemCount = getItemCount();
		int selectionCount = table.getSelectionCount();
		int selectionIndex = table.getSelectionIndex();

		if (tiRemove.isEnabled() != selectionCount > 0) {
			tiRemove.setEnabled(selectionCount > 0);
		}

		// supports only single selection
		boolean enableButtons = selectionCount > 0 && itemCount > 1 && selectionCount == 1;

		// disable if last entry is selected
		boolean enableDown = (enableButtons) ? selectionIndex + 1 < itemCount : false;
		if (!tiDown.isDisposed() && tiDown.isEnabled() != enableDown) {
			tiDown.setEnabled(enableDown);
		}

		// disable if first entry is selected
		boolean enableUp = (enableButtons) ? selectionIndex > 0 : false;
		if (!tiUp.isDisposed() && tiUp.isEnabled() != enableUp) {
			tiUp.setEnabled(enableUp);
		}
	}
}
