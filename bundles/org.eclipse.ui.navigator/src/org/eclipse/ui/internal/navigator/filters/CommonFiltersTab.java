/*******************************************************************************
 * Copyright (c) 2006, 2019 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.eclipse.core.text.StringMatcher;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorFilterService;

/**
 * @since 3.2
 */
public class CommonFiltersTab extends CustomizationTab {

	private static final String ALL = "*"; //$NON-NLS-1$

	private Text filterText;

	private ILabelProvider filterLabelProvider = new CommonFilterLabelProvider();

	private CommonFilterContentProvider filterContentProvider = new CommonFilterContentProvider();

	private TablePatternFilter patternFilter = new TablePatternFilter();

	private Deque<ICommonFilterDescriptor> filterDescriptorChangeHistory = new ArrayDeque<>();

	protected CommonFiltersTab(Composite parent,
			INavigatorContentService aContentService) {
		super(parent, aContentService);
		createControl();
	}

	@Override
	protected void checkStateChanged(CheckStateChangedEvent event) {
		super.checkStateChanged(event);
		ICommonFilterDescriptor filterDescriptor = (ICommonFilterDescriptor) event.getElement();
		filterDescriptorChangeHistory.remove(filterDescriptor);
		filterDescriptorChangeHistory.push(filterDescriptor);
	}

	protected ICommonFilterDescriptor[] getFilterDescriptorChangeHistory() {
		return filterDescriptorChangeHistory.toArray(new ICommonFilterDescriptor[filterDescriptorChangeHistory.size()]);
	}

	private void createControl() {

		createInstructionsLabel(CommonNavigatorMessages.CommonFilterSelectionDialog_Select_the_filters_to_apply);

		createPatternFilterText(this);

		createTable();

		getTableViewer().setContentProvider(filterContentProvider);
		getTableViewer().setLabelProvider(filterLabelProvider);
		getTableViewer().setComparator(new CommonFilterComparator());
		getTableViewer().setInput(getContentService());

		getTableViewer().addFilter(patternFilter);

		updateFiltersCheckState();

	}

	private void createPatternFilterText(Composite composite) {
		filterText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		GridData filterTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		filterText.setLayoutData(filterTextGridData);
		filterText.setMessage(CommonNavigatorMessages.CommonFilterSelectionDialog_enter_name_of_filte_);
		filterText.setFont(composite.getFont());

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// on a CR we want to transfer focus to the list
				boolean hasItems = getTable().getItemCount() > 0;
				if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
					getTable().setFocus();
				} else if (e.character == SWT.CR) {
					return;
				}
			}
		});

		// enter key set focus to tree
		filterText.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
					if (getTableViewer().getTable().getItemCount() == 0) {
						Display.getCurrent().beep();
					} else {
						// if the initial filter text hasn't changed, do not try
						// to match
						boolean hasFocus = getTable().setFocus();
						if (hasFocus && filterText.getText().trim().length() > 0) {
							TableItem item = getFirstHighlightedItem(getTable()
									.getItems());
							if (item != null) {
								getTable().setSelection(
										new TableItem[] { item });
								ISelection sel = getTableViewer()
										.getSelection();
								getTableViewer().setSelection(sel, true);
							}
						}
					}
				}
			}

			private TableItem getFirstHighlightedItem(TableItem[] items) {
				for (TableItem item : items) {
					if (patternFilter.match(item.getText())) {
						return item;
					}
				}
				return null;
			}
		});

		filterText.addModifyListener(e -> textChanged());
	}

	void setInitialFocus() {
		filterText.forceFocus();
	}

	private void textChanged() {
		patternFilter.setPattern(filterText.getText());
		getTableViewer().refresh();

		for (Object checkedItem : getCheckedItems()) {
			getTableViewer().setChecked(checkedItem, true);
		}
	}

	private void updateFiltersCheckState() {
		ICommonFilterDescriptor filterDescriptor;
		INavigatorFilterService filterService = getContentService()
				.getFilterService();
		for (Object child : filterContentProvider.getElements(getContentService())) {
			filterDescriptor = (ICommonFilterDescriptor) child;
			if(filterService.isActive(filterDescriptor.getId())) {
				getTableViewer().setChecked(child, true);
				getCheckedItems().add(child);
			} else {
				getTableViewer().setChecked(child, false);
			}
		}
	}

	private class TablePatternFilter extends ViewerFilter {

		private StringMatcher matcher = null;

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			return match(filterLabelProvider.getText(element));
		}

		protected void setPattern(String newPattern) {
			if (newPattern == null || newPattern.trim().length() == 0) {
				matcher = new StringMatcher(ALL, true, false);
			} else {
				String patternString = ALL + newPattern + ALL;
				matcher = new StringMatcher(patternString, true, false);
			}

		}

		/**
		 * Answers whether the given String matches the pattern.
		 *
		 * @param input
		 *            the String to test
		 *
		 * @return whether the string matches the pattern
		 */
		protected boolean match(String input) {
			if (input == null) {
				return false;
			}
			return matcher == null || matcher.match(input);
		}
	}

	private static class CommonFilterComparator extends ViewerComparator {

		@Override
		public void sort(Viewer viewer, Object[] elements) {
			Arrays.sort(elements, (o1, o2) -> {
				ICommonFilterDescriptor lvalue = (ICommonFilterDescriptor) o1;
				ICommonFilterDescriptor rvalue = (ICommonFilterDescriptor) o2;

				return lvalue.getName().compareTo(rvalue.getName());
			});

		}

	}

}
