/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * <p>
 * A composite widget containing a combo for picking items, a list with selectable items, and action buttons for
 * modifying the list.
 * </p>
 *
 * @author Steven Spungin
 *
 */
public abstract class AbstractPickList extends Composite {

	public static enum PickListFeatures {
		NO_ORDER, NO_PICKER
	}

	protected ComboViewer picker;
	protected TableViewer viewer;

	private final Group group;
	private final ToolBar toolBar;
	private final ToolItem tiRemove;
	private final ToolItem tiUp;
	private final ToolItem tiDown;
	private final ToolItem tiAdd;
	private final AutoCompleteField autoCompleteField;
	private Map<String, Object> proposals;

	public AbstractPickList(Composite parent, int style, List<PickListFeatures> listFeatures, Messages messages,
		AbstractComponentEditor componentEditor) {
		super(parent, style);

		// TODO remove dependency to Messages and AbstractComponentEditor. They
		// are only needed for labels and icons.

		setLayout(new FillLayout());

		group = new Group(this, SWT.NONE);
		// gridData.horizontalIndent = 30;
		group.setLayout(new GridLayout(1, false));

		toolBar = new ToolBar(group, SWT.FLAT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		tiAdd = new ToolItem(toolBar, SWT.PUSH);
		tiAdd.setText(messages.ModelTooling_Common_AddEllipsis);
		tiAdd.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_table_add));
		tiAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPressed();
			}
		});

		// new ToolItem(toolBar, SWT.SEPARATOR_FILL);

		tiDown = new ToolItem(toolBar, SWT.PUSH);
		tiDown.setText(messages.ModelTooling_Common_Down);
		tiDown.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_arrow_down));
		tiDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveDownPressed();
			}
		});

		tiUp = new ToolItem(toolBar, SWT.PUSH);
		tiUp.setText(messages.ModelTooling_Common_Up);
		tiUp.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_arrow_up));
		tiUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveUpPressed();
			}
		});
		tiRemove = new ToolItem(toolBar, SWT.PUSH);
		tiRemove.setText(messages.ModelTooling_Common_Remove);
		tiRemove.setImage(componentEditor.createImage(ResourceProvider.IMG_Obj16_table_delete));
		tiRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removePressed();
			}
		});

		picker = new ComboViewer(group, SWT.DROP_DOWN);
		final Combo control = picker.getCombo();
		control.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		final ComboContentAdapter controlContentAdapter = new ComboContentAdapter() {
			@Override
			public void setControlContents(Control control, String text1, int cursorPosition) {
				super.setControlContents(control, text1, cursorPosition);
				final Object valueInModel = proposals.get(text1);
				if (valueInModel != null) {
					getPicker().setSelection(new StructuredSelection(valueInModel));
				}
			}
		};
		autoCompleteField = new AutoCompleteField(control, controlContentAdapter, new String[0]);

		picker.addOpenListener(new IOpenListener() {

			@Override
			public void open(OpenEvent event) {
				addPressed();
			}
		});

		viewer = new TableViewer(group);
		final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1);
		viewer.getControl().setLayoutData(gd);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateUiState();
			}
		});

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

	protected void addPressed() {
	}

	abstract protected int getItemCount();

	public TableViewer getList() {
		return viewer;
	}

	public void setInput(Object input) {
		getPicker().setInput(input);

		proposals = toProposals(input);
		final Set<String> keySet = proposals.keySet();
		autoCompleteField.setProposals(keySet.toArray(new String[keySet.size()]));
	}

	public ISelection getSelection() {
		return getPicker().getSelection();
	}

	public void setSelection(ISelection selection) {
		getPicker().setSelection(selection);
	}

	private Map<String, Object> toProposals(Object inputElement) {

		final Map<String, Object> props = new TreeMap<String, Object>();

		if (inputElement instanceof Object[]) {
			for (final Object value : (Object[]) inputElement) {
				props.put(getTextualValue(value), value);
			}
		}
		if (inputElement instanceof Collection) {
			for (final Object value : (Collection<Object>) inputElement) {
				props.put(getTextualValue(value), value);
			}

		}

		return props;
	}

	private String getTextualValue(Object value) {
		return ((ILabelProvider) getPicker().getLabelProvider()).getText(value);
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

	protected ToolBar getToolBar() {
		return toolBar;
	}

	protected void moveDownPressed() {
	}

	protected void moveUpPressed() {
	}

	protected void removePressed() {
	}

	public void setText(String text) {
		group.setText(text);
	}

	public void updateUiState() {
		final IStructuredSelection selection = (IStructuredSelection) getList().getSelection();
		final boolean selected = selection.size() > 0;
		final int count = getItemCount();
		if (tiDown.isDisposed() == false) {
			tiDown.setEnabled(selected && count > 1);
			tiUp.setEnabled(selected && count > 1);
		}
		tiRemove.setEnabled(selected);
	}
}
