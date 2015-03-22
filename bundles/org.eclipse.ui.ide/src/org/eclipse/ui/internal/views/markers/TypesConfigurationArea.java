/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.MarkerType;

/**
 * TypesConfigurationArea is the FilterConfigurationArea that handles type
 * selection for the filter.
 *
 * @since 3.4
 *
 */
public class TypesConfigurationArea extends GroupFilterConfigurationArea {

	private class CategoryEntry extends TypesEntry {

		private Collection<MarkerTypeEntry> children = new ArrayList<>();
		private String name;

		/**
		 * Create a new instance of the receiver.
		 *
		 * @param categoryName
		 */
		public CategoryEntry(String categoryName) {
			name = categoryName;
		}

		/**
		 * Add the node to the receiver.
		 *
		 * @param node
		 */
		public void add(MarkerTypeEntry node) {
			children.add(node);
			node.setParent(this);

		}

		@Override
		public void addElementsMatching(Collection<MarkerType> selectedTypes, Collection<TypesEntry> entries) {
			Iterator<MarkerTypeEntry> childIterator = children.iterator();
			while (childIterator.hasNext()) {
				childIterator.next().addElementsMatching(selectedTypes, entries);
			}
		}

		@Override
		public Collection<MarkerTypeEntry> getChildren() {
			return children;
		}

		@Override
		public String getLabel() {
			return name;
		}

		@Override
		public TypesEntry getParent() {
			return null;
		}

		@Override
		public boolean hasChildren() {
			return children.size() > 0;
		}

	}

	private class MarkerTypeEntry extends TypesEntry {

		private CategoryEntry category;
		private MarkerType markerType;

		/**
		 * Create an instance of the receiver.
		 *
		 * @param markerType
		 */
		public MarkerTypeEntry(MarkerType markerType) {
			this.markerType = markerType;
		}

		@Override
		public void addElementsMatching(Collection<MarkerType> selectedTypes, Collection<TypesEntry> entries) {
			if (selectedTypes.contains(markerType)) {
				entries.add(this);
			}
		}

		@Override
		public Collection<MarkerTypeEntry> getChildren() {
			return EMPTY_COLLECTION;
		}

		@Override
		public String getLabel() {
			return markerType.getLabel();
		}

		/**
		 * Return the marker type for the receiver.
		 *
		 * @return MarkerType
		 */
		public MarkerType getMarkerType() {
			return markerType;
		}

		@Override
		public TypesEntry getParent() {
			return category;
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		/**
		 * Set the category of the receiver.
		 *
		 * @param categoryEntry
		 */
		public void setParent(CategoryEntry categoryEntry) {
			category = categoryEntry;
		}

	}

	private abstract class TypesEntry {

		/**
		 * Add any elements that contain a type in selectedTypes tp entries.
		 *
		 * @param selectedTypes
		 * @param entries
		 */
		public abstract void addElementsMatching(Collection<MarkerType> selectedTypes, Collection<TypesEntry> entries);

		/**
		 * Return the children of the receiver.
		 *
		 * @return TypesEntry[]
		 */
		public abstract Collection<MarkerTypeEntry> getChildren();

		/**
		 * Return the label for the receiver.
		 *
		 * @return String
		 */
		public abstract String getLabel();

		/**
		 * Return the parent of the receiver.
		 *
		 * @return TypesEntry
		 */
		public abstract TypesEntry getParent();

		/**
		 * Return whether or not the receiver has children.
		 *
		 * @return boolean
		 */
		public abstract boolean hasChildren();

	}

	private static Collection<MarkerTypeEntry> EMPTY_COLLECTION = new HashSet<>();

	private HashMap<MarkerFieldFilterGroup, List<TypesEntry>> models = new HashMap<>(0);

	private CheckboxTreeViewer typesViewer;

	@Override
	public void apply(MarkerFieldFilter filter) {
		Collection<MarkerType> selectedTypes = new ArrayList<>();
		Object[] elements = typesViewer.getCheckedElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof MarkerTypeEntry) {
				selectedTypes.add(((MarkerTypeEntry) elements[i]).getMarkerType());
			}
		}
		MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) typesViewer.getInput();
		((MarkerTypeFieldFilter) filter).setSelectedTypes(selectedTypes, group.generator);
	}

	@Override
	public void applyToGroup(MarkerFieldFilterGroup group) {
		// Nothing to set at the group level
	}

	@Override
	public void createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Tree tree = new Tree(composite, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(false);

		typesViewer = new CheckboxTreeViewer(tree);
		initializeFontMetrics(tree);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = Dialog.convertVerticalDLUsToPixels(
				getFontMetrics(), 100);
		gridData.heightHint = Dialog.convertVerticalDLUsToPixels(
				getFontMetrics(), 50);

		final ITreeContentProvider typesContentProvider = getTypesContentProvider();
		typesViewer.getControl().setLayoutData(gridData);
		typesViewer.setContentProvider(typesContentProvider);
		typesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TypesEntry) element).getLabel();
			}
		});
		typesViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((TypesEntry) e1).getLabel().compareTo(((TypesEntry) e2).getLabel());
			}
		});
		typesViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				boolean checked = event.getChecked();
				typesViewer.setGrayed(element, false);
				setChildrenChecked(element, checked);
				setParentCheckState(element, checked);
			}

			/**
			 * Set the check state of the children of element to
			 * <code>true</code>.
			 *
			 * @param element
			 * @param checked
			 */
			private void setChildrenChecked(Object element, boolean checked) {
				Object[] children = typesContentProvider.getChildren(element);
				if (children.length > 0) {
					for (int i = 0; i < children.length; i++) {
						typesViewer.setChecked(children[i], checked);
					}
				}
			}

			/**
			 * Update the parent check state based on the state of the element
			 *
			 * @param element
			 * @param checked
			 */
			private void setParentCheckState(Object element, boolean checked) {
				Object parentType = typesContentProvider.getParent(element);
				if (parentType == null) {
					return;
				}

				Object[] children = typesContentProvider.getChildren(parentType);
				for (int i = 0; i < children.length; i++) {// At least one
					// different
					if (typesViewer.getChecked(children[i]) != checked) {
						typesViewer.setGrayChecked(parentType, true);
						return;
					}
				}
				typesViewer.setGrayed(parentType, false);
				// All are the same - update the parent
				typesViewer.setChecked(parentType, checked);
			}
		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		GridData buttonsData = new GridData();
		buttonsData.verticalAlignment = GridData.BEGINNING;
		buttonComposite.setLayoutData(buttonsData);

		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText(MarkerMessages.filtersDialog_selectAllTypes);
		selectAllButton.addSelectionListener(getSelectAllButtonListener(typesContentProvider, true));
		setButtonLayoutData(selectAllButton);

		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText(MarkerMessages.filtersDialog_deselectAllTypes);
		deselectAllButton.addSelectionListener(getSelectAllButtonListener(typesContentProvider, false));
		setButtonLayoutData(deselectAllButton);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean grabExcessVerticalSpace() {
		return true;
	}

	/**
	 * Get the listener for select all and deselect all.
	 *
	 * @param typesContentProvider
	 * @param checked
	 *            the check state to set
	 * @return SelectionListener
	 */
	private SelectionListener getSelectAllButtonListener(
			final ITreeContentProvider typesContentProvider,
			final boolean checked) {
		return new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] elements = typesContentProvider.getElements(typesViewer.getInput());
				for (int i = 0; i < elements.length; i++) {
					typesViewer.setSubtreeChecked(elements[i], checked);
				}
			}
		};
	}

	/**
	 * Return the elements for MarkerFieldFilterGroup groip.
	 *
	 * @param group
	 * @return List of TypesEntry
	 */
	protected List<TypesEntry> elementsForGroup(MarkerFieldFilterGroup group) {
		if (models.containsKey(group)) {
			return models.get(group);
		}
		Iterator<MarkerType> roots = group.getAllTypes().iterator();
		List<TypesEntry> markerNodes = new ArrayList<>();
		HashMap<String, CategoryEntry> categories = new HashMap<>();
		while (roots.hasNext()) {
			MarkerType markerType = roots.next();
			String categoryName = MarkerSupportRegistry.getInstance().getCategory(markerType.getId());
			if (categoryName == null) {
				markerNodes.add(new MarkerTypeEntry(markerType));
			} else {
				CategoryEntry category;
				if (categories.containsKey(categoryName)) {
					category = categories.get(categoryName);
				} else {
					category = new CategoryEntry(categoryName);
					categories.put(categoryName, category);
					markerNodes.add(category);
				}
				MarkerTypeEntry node = new MarkerTypeEntry(markerType);
				category.add(node);
			}
		}
		models.put(group, markerNodes);
		return markerNodes;
	}

	/**
	 * Find the type entries for group that correspond to it's current selection
	 * and add them to the checked or grey checked lists as appropriate.
	 *
	 * @param group
	 * @param entries
	 * @param greyEntries
	 */
	private void findTypeEntries(MarkerFieldFilterGroup group,
			Collection<TypesEntry> entries, Collection<TypesEntry> greyEntries) {
		Iterator<TypesEntry> elements = elementsForGroup(group).iterator();

		Collection<MarkerType> selectedTypes = ((MarkerTypeFieldFilter) group.getFilter(getField())).getSelectedTypes();
		while (elements.hasNext()) {
			TypesEntry entry = elements.next();
			entry.addElementsMatching(selectedTypes, entries);
			if (entry.hasChildren()) {// Is it a category?
				Collection<MarkerTypeEntry> children = entry.getChildren();
				if (entries.containsAll(children)) {
					entries.add(entry);
				} else {// See if we need to gray check it
					Iterator<MarkerTypeEntry> iterator = children.iterator();
					while (iterator.hasNext()) {
						if (entries.contains(iterator.next())) {
							greyEntries.add(entry);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Get the content provider for the types.
	 *
	 * @return ITreeContentProvider
	 */
	private ITreeContentProvider getTypesContentProvider() {
		return new ITreeContentProvider() {

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return ((TypesEntry) parentElement).getChildren().toArray();
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return elementsForGroup((MarkerFieldFilterGroup) inputElement)
						.toArray();
			}

			@Override
			public Object getParent(Object element) {
				return ((TypesEntry) element).getParent();
			}

			@Override
			public boolean hasChildren(Object element) {
				return ((TypesEntry) element).hasChildren();
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		};
	}

	@Override
	public void initialize(MarkerFieldFilter filter) {
		// This was already done when initialising from the group.
	}

	@Override
	public void initializeFromGroup(MarkerFieldFilterGroup group) {
		typesViewer.setInput(group);
		typesViewer.refresh();
		Collection<TypesEntry> checked = new HashSet<>();
		Collection<TypesEntry> greyed = new HashSet<>();
		findTypeEntries(group, checked, greyed);
		checked.addAll(greyed);
		typesViewer.setCheckedElements(checked.toArray());
		typesViewer.setGrayedElements(greyed.toArray());
	}

	@Override
	public String getTitle() {
		return MarkerMessages.filtersDialog_typesTitle;
	}
}
