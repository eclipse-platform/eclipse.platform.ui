/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NamedHandleObjectComparator;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.UnionSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.NamedHandleObjectLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.ICommandImageService;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * <p>
 * A preference page that is capable of displaying and editing the bindings
 * between commands and user input events. These are typically things like
 * keyboard shortcuts.
 * </p>
 * <p>
 * This preference page has four general types of methods. Create methods are
 * called when the page is first made visible. They are responsible for creating
 * all of the widgets, and laying them out within the preference page. Fill
 * methods populate the contents of the widgets that contain collections of data
 * from which items can be selected. The select methods respond to selection
 * events from the user, such as a button press or a table selection. The update
 * methods update the contents of various widgets based on the current state of
 * the user interface. For example, the command name label will always try to
 * match the current select in the binding table.
 * </p>
 * 
 * @since 3.2
 */
public final class NewKeysPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	private static final String TRACING_COMPONENT = "NewKeysPref"; //$NON-NLS-1$

	/**
	 * @since 3.3
	 * 
	 */
	private final class ResortColumn extends SelectionAdapter {
		private final BindingComparator comparator;
		private final TreeColumn treeColumn;
		private final Tree tree;
		private final int column;

		/**
		 * @param comparator
		 * @param commandNameColumn
		 * @param tree
		 */
		private ResortColumn(BindingComparator comparator,
				TreeColumn treeColumn, Tree tree, int column) {
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.tree = tree;
			this.column = column;
		}

		public void widgetSelected(SelectionEvent e) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
			}
			tree.setSortColumn(treeColumn);
			comparator.setSortColumn(column);
			tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
			try {
				filteredTree.getViewer().getTree().setRedraw(false);
				filteredTree.getViewer().refresh();
			} finally {
				filteredTree.getViewer().getTree().setRedraw(true);
			}
		}
	}

	private class ObservableSetContentProvider implements ITreeContentProvider {

		private class KnownElementsSet extends ObservableSet {

			KnownElementsSet(Set wrappedSet) {
				super(SWTObservables.getRealm(Display.getDefault()),
						wrappedSet, Object.class);
			}

			void doFireDiff(Set added, Set removed) {
				fireSetChange(Diffs.createSetDiff(added, removed));
			}

			void doFireStale(boolean isStale) {
				if (isStale) {
					fireStale();
				} else {
					fireChange();
				}
			}
		}

		private IObservableSet readableSet;

		private Viewer viewer;

		/**
		 * This readableSet returns the same elements as the input readableSet.
		 * However, it only fires events AFTER the elements have been added or
		 * removed from the viewer.
		 */
		private KnownElementsSet knownElements;

		private ISetChangeListener listener = new ISetChangeListener() {

			public void handleSetChange(SetChangeEvent event) {
				boolean wasStale = knownElements.isStale();
				if (isDisposed()) {
					return;
				}
				doDiff(event.diff.getAdditions(), event.diff.getRemovals(),
						true);
				if (!wasStale && event.getObservableSet().isStale()) {
					knownElements.doFireStale(true);
				}
			}
		};

		private IStaleListener staleListener = new IStaleListener() {
			public void handleStale(StaleEvent event) {
				knownElements.doFireStale(event.getObservable().isStale());
			}
		};

		/**
		 * 
		 */
		public ObservableSetContentProvider() {
			readableSet = new ObservableSet(SWTObservables.getRealm(Display
					.getDefault()), Collections.EMPTY_SET, Object.class) {
			};
			knownElements = new KnownElementsSet(readableSet);
		}

		public void dispose() {
			setInput(null);
		}

		private void doDiff(Set added, Set removed, boolean updateViewer) {
			knownElements.doFireDiff(added, Collections.EMPTY_SET);

			if (updateViewer) {
				if (added.size() > 20 || removed.size() > 20) {
					viewer.refresh();
				} else {
					Object[] toAdd = added.toArray();
					if (viewer instanceof TreeViewer) {
						TreeViewer tv = (TreeViewer) viewer;
						tv.add(model, toAdd);
					} else if (viewer instanceof AbstractListViewer) {
						AbstractListViewer lv = (AbstractListViewer) viewer;
						lv.add(toAdd);
					}
					Object[] toRemove = removed.toArray();
					if (viewer instanceof TreeViewer) {
						TreeViewer tv = (TreeViewer) viewer;
						tv.remove(toRemove);
					} else if (viewer instanceof AbstractListViewer) {
						AbstractListViewer lv = (AbstractListViewer) viewer;
						lv.remove(toRemove);
					}
				}
			}
			knownElements.doFireDiff(Collections.EMPTY_SET, removed);
		}

		public Object[] getElements(Object inputElement) {
			return readableSet.toArray();
		}

		/**
		 * Returns the readableSet of elements known to this content provider.
		 * Items are added to this readableSet before being added to the viewer,
		 * and they are removed after being removed from the viewer. The
		 * readableSet is always updated after the viewer. This is intended for
		 * use by label providers, as it will always return the items that need
		 * labels.
		 * 
		 * @return readableSet of items that will need labels
		 */
		public IObservableSet getKnownElements() {
			return knownElements;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = viewer;

			if (newInput != null && !(newInput instanceof IObservableSet)) {
				throw new IllegalArgumentException(
						"This content provider only works with input of type IReadableSet"); //$NON-NLS-1$
			}

			setInput((IObservableSet) newInput);
		}

		private boolean isDisposed() {
			return viewer.getControl() == null
					|| viewer.getControl().isDisposed();
		}

		private void setInput(IObservableSet newSet) {
			boolean updateViewer = true;
			if (newSet == null) {
				newSet = new ObservableSet(SWTObservables.getRealm(Display
						.getDefault()), Collections.EMPTY_SET, Object.class) {
				};
				// don't update the viewer - its input is null
				updateViewer = false;
			}

			boolean wasStale = false;
			if (readableSet != null) {
				wasStale = readableSet.isStale();
				readableSet.removeSetChangeListener(listener);
				readableSet.removeStaleListener(staleListener);
			}

			HashSet additions = new HashSet();
			HashSet removals = new HashSet();

			additions.addAll(newSet);
			additions.removeAll(readableSet);

			removals.addAll(readableSet);
			removals.removeAll(newSet);

			readableSet = newSet;

			doDiff(additions, removals, updateViewer);

			if (readableSet != null) {
				readableSet.addSetChangeListener(listener);
				readableSet.addStaleListener(staleListener);
			}

			boolean isStale = (readableSet != null && readableSet.isStale());
			if (isStale != wasStale) {
				knownElements.doFireStale(isStale);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	/**
	 * A FilteredTree that provides a combo which is used to organize and
	 * display elements in the tree according to the selected criteria.
	 * 
	 */
	protected class CategoryFilterTree extends FilteredTree {

		private CategoryPatternFilter filter;

		/**
		 * Constructor for PatternFilteredTree.
		 * 
		 * @param parent
		 * @param treeStyle
		 * @param filter
		 */
		protected CategoryFilterTree(Composite parent, int treeStyle,
				CategoryPatternFilter filter) {
			super(parent, treeStyle, filter);
			this.filter = filter;
		}

		public void filterCategories(boolean b) {
			filter.filterCategories(b);
			textChanged();
		}

		public boolean isFilteringCategories() {
			return filter.isFilteringCategories();
		}
	}

	/**
	 * A label provider that simply extracts the command name and the formatted
	 * trigger sequence from a given binding, and matches them to the correct
	 * column.
	 */
	private final class BindingLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/**
		 * The index of the column containing the command name.
		 */
		private static final int COLUMN_COMMAND = 0;

		/**
		 * The index of the column containing the trigger sequence.
		 */
		private static final int COLUMN_TRIGGER_SEQUENCE = 1;

		/**
		 * The index of the column containing the trigger sequence.
		 */
		private static final int COLUMN_WHEN = 2;

		/**
		 * The index of the column containing the Category.
		 */
		private static final int COLUMN_CATEGORY = 3;

		/**
		 * The index of the column with the image for User binding
		 */
		private static final int COLUMN_USER = 4;

		/**
		 * A resource manager for this preference page.
		 */
		private final LocalResourceManager localResourceManager = new LocalResourceManager(
				JFaceResources.getResources());

		public final void dispose() {
			super.dispose();
			localResourceManager.dispose();
		}

		public final Image getColumnImage(final Object element,
				final int columnIndex) {
			final Object value = element;
			if (value instanceof Binding) {
				switch (columnIndex) {
				case COLUMN_COMMAND:
					final ParameterizedCommand parameterizedCommand = ((Binding) value)
							.getParameterizedCommand();
					if (parameterizedCommand != null) {
						final String commandId = parameterizedCommand.getId();
						final ImageDescriptor imageDescriptor = commandImageService
								.getImageDescriptor(commandId);
						if (imageDescriptor == null) {
							return null;
						}
						try {
							return localResourceManager
									.createImage(imageDescriptor);
						} catch (final DeviceResourceException e) {
							final String message = "Problem retrieving image for a command '" //$NON-NLS-1$
									+ commandId + '\'';
							final IStatus status = new Status(IStatus.ERROR,
									WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
							WorkbenchPlugin.log(message, status);
						}
					}
					return null;

				case COLUMN_USER:
					if (((Binding) value).getType() == Binding.USER)
						return ImageFactory.getImage("change"); //$NON-NLS-1$
					return ImageFactory.getImage("blank"); //$NON-NLS-1$
				}

			} else if (value instanceof ParameterizedCommand) {
				switch (columnIndex) {
				case COLUMN_COMMAND:
					final ParameterizedCommand parameterizedCommand = (ParameterizedCommand) value;
					final String commandId = parameterizedCommand.getId();
					final ImageDescriptor imageDescriptor = commandImageService
							.getImageDescriptor(commandId);
					if (imageDescriptor == null) {
						return null;
					}
					try {
						return localResourceManager
								.createImage(imageDescriptor);
					} catch (final DeviceResourceException e) {
						final String message = "Problem retrieving image for a command '" //$NON-NLS-1$
								+ commandId + '\'';
						final IStatus status = new Status(IStatus.ERROR,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
						WorkbenchPlugin.log(message, status);
					}
					return null;
				case COLUMN_USER:
					return ImageFactory.getImage("blank"); //$NON-NLS-1$
				}

			} else if ((value instanceof Category) || (value instanceof String)) {
				switch (columnIndex) {
				case COLUMN_COMMAND:
					final URL url = BundleUtility.find(PlatformUI.PLUGIN_ID,
							ICON_GROUP_OF_BINDINGS);
					final ImageDescriptor imageDescriptor = ImageDescriptor
							.createFromURL(url);
					try {
						return localResourceManager
								.createImage(imageDescriptor);
					} catch (final DeviceResourceException e) {
						final String message = "Problem retrieving image for groups of bindings: '" //$NON-NLS-1$
								+ ICON_GROUP_OF_BINDINGS + '\'';
						final IStatus status = new Status(IStatus.ERROR,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
						WorkbenchPlugin.log(message, status);
					}
				}

			}

			return null;
		}

		private boolean checkConflict(Binding binding) {
			Collection matches = (Collection) localChangeManager
					.getActiveBindingsDisregardingContext().get(
							binding.getTriggerSequence());
			if (matches != null) {
				Iterator i = matches.iterator();
				while (i.hasNext()) {
					Binding b = (Binding) i.next();
					if (binding != b
							&& b.getContextId().equals(binding.getContextId())
							&& b.getSchemeId().equals(binding.getSchemeId())) {
						return true;
					}
				}
			}
			return false;
		}

		public final String getColumnText(final Object element,
				final int columnIndex) {
			final Object value = element;
			if (value instanceof Binding) {
				final Binding binding = (Binding) value;
				switch (columnIndex) {
				case COLUMN_COMMAND:
					try {
						return binding.getParameterizedCommand().getName();

					} catch (final NotDefinedException e) {
						return NewKeysPreferenceMessages.Undefined_Command;
					}
				case COLUMN_TRIGGER_SEQUENCE:
					if (checkConflict(binding)) {
						return "*" + binding.getTriggerSequence().format(); //$NON-NLS-1$
					}
					return binding.getTriggerSequence().format();

				case COLUMN_WHEN:
					try {
						return contextService
								.getContext(binding.getContextId()).getName();
					} catch (NotDefinedException e1) {
						return NewKeysPreferenceMessages.Undefined_Context;
					}
				case COLUMN_CATEGORY:
					try {
						return binding.getParameterizedCommand().getCommand()
								.getCategory().getName();
					} catch (NotDefinedException e) {
						return NewKeysPreferenceMessages.Unavailable_Category;
					}
				default:
					return null;
				}
			} else if (value instanceof Category) {
				if (columnIndex == COLUMN_COMMAND) {
					try {
						return ((Category) value).getName();
					} catch (final NotDefinedException e) {
						return NewKeysPreferenceMessages.Unavailable_Category;
					}
				}

				return null;

			} else if (value instanceof String) {
				// This is a context.
				if (columnIndex == COLUMN_COMMAND) {
					try {
						return contextService.getContext((String) value)
								.getName();
					} catch (final NotDefinedException e) {
						return NewKeysPreferenceMessages.Undefined_Context;
					}
				}

				return null;
			} else if (value instanceof ParameterizedCommand) {
				if (columnIndex == COLUMN_COMMAND) {
					try {
						return ((ParameterizedCommand) value).getName();
					} catch (final NotDefinedException e) {
						return NewKeysPreferenceMessages.Undefined_Command;
					}
				}
				if (columnIndex == COLUMN_TRIGGER_SEQUENCE)
					return ""; //$NON-NLS-1$

				if (columnIndex == COLUMN_WHEN)
					return ""; //$NON-NLS-1$

				if (columnIndex == COLUMN_CATEGORY) {
					try {
						return ((ParameterizedCommand) value).getCommand()
								.getCategory().getName();
					} catch (NotDefinedException e) {
						return NewKeysPreferenceMessages.Unavailable_Category;
					}
				}
				return null;
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			String rc = getColumnText(element, 0);
			if (rc == null) {
				super.getText(element);
			}
			StringBuffer buf = new StringBuffer(rc);
			for (int i = 1; i < COLUMN_USER; i++) {
				String text = getColumnText(element, i);
				if (text != null) {
					buf.append(' ');
					buf.append(text);
				}
			}
			return buf.toString();
		}
	}

	/**
	 * Sorts the bindings in the filtered tree based on the current grouping.
	 */
	private final class BindingComparator extends ViewerComparator {

		private int sortColumn = 0;

		private int lastSortColumn = 0;

		private boolean ascending = true;

		public final int category(final Object element) {
			switch (grouping) {
			case GROUPING_CATEGORY:
				// TODO This has to be done with something other than the hash.
				try {
					final ParameterizedCommand command = (element instanceof ParameterizedCommand) ? (ParameterizedCommand) element
							: ((Binding) element).getParameterizedCommand();
					return command.getCommand().getCategory().hashCode();
				} catch (final NotDefinedException e) {
					return 0;
				}
			case GROUPING_CONTEXT:
				// TODO This has to be done with something other than the hash.
				if (element instanceof Binding) {
					return ((Binding) element).getContextId().hashCode();
				}
			case GROUPING_NONE:
			default:
				return 0;
			}
		}

		public final int compare(final Viewer viewer, final Object a,
				final Object b) {

			int result = compareColumn(viewer, a, b, sortColumn);
			if (result == 0 && sortColumn != lastSortColumn) {
				result = compareColumn(viewer, a, b, lastSortColumn);
			}
			return ascending ? result : (-1) * result;
		}
		
		private int compareColumn(final Viewer viewer, final Object a, final Object b,
				final int columnNumber) {
			if (columnNumber == BindingLabelProvider.COLUMN_USER) {
				return sortUser(viewer, a, b);
			}
			IBaseLabelProvider baseLabel = ((TreeViewer)viewer).getLabelProvider();
			if (baseLabel instanceof ITableLabelProvider) {
				ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
				String e1p = tableProvider.getColumnText(a, columnNumber);
				String e2p = tableProvider.getColumnText(b, columnNumber);
				if (e1p != null && e2p != null) {
					return getComparator().compare(e1p, e2p);
				}
			}
			return 0;
		}
		
		private int sortUser(final Viewer viewer, final Object a, final Object b) {
			int typeA = (a instanceof Binding?((Binding)a).getType():Binding.SYSTEM);
			int typeB = (b instanceof Binding?((Binding)b).getType():Binding.SYSTEM);
			int result = typeA - typeB;
			return result;
		}

		/**
		 * @return Returns the sortColumn.
		 */
		public int getSortColumn() {
			return sortColumn;
		}

		/**
		 * @param sortColumn
		 *            The sortColumn to set.
		 */
		public void setSortColumn(int sortColumn) {
			lastSortColumn = this.sortColumn;
			if (lastSortColumn != sortColumn) {
				ascending = true;
			}
			this.sortColumn = sortColumn;
		}

		/**
		 * @return Returns the ascending.
		 */
		public boolean isAscending() {
			return ascending;
		}

		/**
		 * @param ascending
		 *            The ascending to set.
		 */
		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		}
	}

	/**
	 * The constant value for <code>grouping</code> when the bindings should
	 * be grouped by category.
	 */
	private static final int GROUPING_CATEGORY = 0;

	/**
	 * The constant value for <code>grouping</code> when the bindings should
	 * be grouped by context.
	 */
	private static final int GROUPING_CONTEXT = 1;

	/**
	 * The constant value for <code>grouping</code> when the bindings should
	 * not be grouped (i.e., they should be displayed in a flat list).
	 */
	private static final int GROUPING_NONE = 2;

	/**
	 * The path at which the icon for "groups of bindings" is located.
	 */
	private static final String ICON_GROUP_OF_BINDINGS = "$nl$/icons/full/obj16/keygroups_obj.gif"; //$NON-NLS-1$

	private static final String CONTEXT_ID_ACTION_SETS = "org.eclipse.ui.contexts.actionSet"; //$NON-NLS-1$

	private static final String CONTEXT_ID_INTERNAL = ".internal."; //$NON-NLS-1$

	/**
	 * The number of items to show in the bindings table tree.
	 */
	private static final int ITEMS_TO_SHOW = 7;

	/**
	 * A comparator that can be used for display of
	 * <code>NamedHandleObject</code> instances to the end user.
	 */
	private static final NamedHandleObjectComparator NAMED_HANDLE_OBJECT_COMPARATOR = new NamedHandleObjectComparator();

	public final static String TAG_DIALOG_SECTION = "org.eclipse.ui.preferences.keysPreferencePage"; //$NON-NLS-1$

	private static final String TAG_FILTER_ACTION_SETS = "actionSetFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_INTERNAL = "internalFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_UNCAT = "uncategorizedFilter"; //$NON-NLS-1$

	/**
	 * Sorts the given array of <code>NamedHandleObject</code> instances based
	 * on their name. This is generally useful if they will be displayed to an
	 * end users.
	 * 
	 * @param objects
	 *            The objects to be sorted; must not be <code>null</code>.
	 * @return The same array, but sorted in place; never <code>null</code>.
	 */
	private static final NamedHandleObject[] sortByName(
			final NamedHandleObject[] objects) {
		Arrays.sort(objects, NAMED_HANDLE_OBJECT_COMPARATOR);
		return objects;
	}

	/**
	 * The workbench's binding service. This binding service is used to access
	 * the current set of bindings, and to persist changes.
	 */
	private IBindingService bindingService;

	/**
	 * The text widget containing the key sequence. This value is
	 * <code>null</code> until the controls are created.
	 */
	private Text bindingText;

	/**
	 * The workbench's command image service. This command image service is used
	 * to provide an icon beside each command.
	 */
	private ICommandImageService commandImageService;

	/**
	 * The label containing the name of the currently selected binding's
	 * command. This value is <code>null</code> until the controls are
	 * created.
	 */
	private Label commandNameValueLabel;

	/**
	 * The workbench's command service. This command service is used to access
	 * the list of commands.
	 */
	private ICommandService commandService;

	/**
	 * The workbench's context service. This context service is used to access
	 * the list of contexts.
	 */
	private IContextService contextService;

	/**
	 * The label containing the description of the currently selected binding's
	 * command. This value is <code>null</code> until the controls are
	 * created.
	 */
	private Text descriptionValueText;

	/**
	 * The filtered tree containing the list of commands and bindings to edit.
	 */
	private CategoryFilterTree filteredTree;

	private CategoryPatternFilter patternFilter;

	/**
	 * The grouping for the bindings tree. Either there should be no group
	 * (i.e., flat list), or the bindings should be grouped by either category
	 * or context.
	 */
	private int grouping = GROUPING_NONE;

	/**
	 * The key sequence entry widget containing the trigger sequence for the
	 * currently selected binding. This value is <code>null</code> until the
	 * controls are created.
	 */
	private KeySequenceText keySequenceText;

	/**
	 * A binding manager local to this preference page. When the page is
	 * initialized, the current bindings are read out from the binding service
	 * and placed in this manager. This manager is then updated as the user
	 * makes changes. When the user has finished, the contents of this manager
	 * are compared with the contents of the binding service. The changes are
	 * then persisted.
	 */
	private BindingManager localChangeManager;

	/**
	 * The context id of the binding which the user is trying to add. This value
	 * is derived from the binding that is selected at the time the user tried
	 * to add a binding. If this value is <code>null</code>, then the user is
	 * not currently trying to add a binding to a command that already has a
	 * binding.
	 */
	private String markedContextId = null;

	/**
	 * The parameterized command to which the user is currently trying to add a
	 * binding. If this value is <code>null</code>, then the user is not
	 * currently trying to add a binding to a command that already has a
	 * binding.
	 */
	private ParameterizedCommand markedParameterizedCommand = null;

	/**
	 * The combo box containing the list of possible schemes to choose from.
	 * This value is <code>null</code> until the contents are created.
	 */
	private ComboViewer schemeCombo = null;

	private boolean filterActionSetContexts = true;

	private boolean filterInternalContexts = true;

	private IObservableSet commandModel;

	private IObservableSet bindingModel;

	private UnionSet model;

	/**
	 * The combo box containing the list of possible contexts to choose from.
	 * This value is <code>null</code> until the contents are create.
	 */
	private ComboViewer whenCombo = null;

	/**
	 * Adds a new binding based on an existing binding. The command and the
	 * context are copied from the existing binding. The scheme id is set to be
	 * the user's personal derivative scheme. The preference page is updated,
	 * and focus is placed in the key sequence field.
	 * 
	 * @param binding
	 *            The binding to be added; must not be <code>null</code>.
	 */
	private final void bindingAdd(final Binding binding) {
		if (!(binding.getParameterizedCommand().getCommand().isDefined()))
			return;

		// Remember the parameterized command and context.
		markedParameterizedCommand = binding.getParameterizedCommand();
		markedContextId = binding.getContextId();

		// Update the preference page.
		update();

		// Select the new binding.
		filteredTree.getViewer().setSelection(
				new StructuredSelection(binding.getParameterizedCommand()),
				true);
		bindingText.setFocus();
		bindingText.selectAll();
	}

	/**
	 * Removes an existing binding. The preference page is then updated.
	 * 
	 * @param binding
	 *            The binding to be removed; must not be <code>null</code>.
	 */
	private final void bindingRemove(final KeyBinding binding) {
		ArrayList extraSystemDeletes = new ArrayList();
		final String contextId = binding.getContextId();
		final String schemeId = binding.getSchemeId();
		final KeySequence triggerSequence = binding.getKeySequence();
		if (binding.getType() == Binding.USER) {
			localChangeManager.removeBinding(binding);
		} else {
			// TODO This should be the user's personal scheme.
			Collection previousConflictMatches = (Collection) localChangeManager
					.getActiveBindingsDisregardingContext().get(
							binding.getTriggerSequence());
			KeyBinding deleteBinding = new KeyBinding(triggerSequence, null,
					schemeId, contextId, null, null, null, Binding.USER);
			localChangeManager.addBinding(deleteBinding);
			if (previousConflictMatches != null) {
				Iterator i = previousConflictMatches.iterator();
				while (i.hasNext()) {
					Binding b = (Binding) i.next();
					if (b != binding && deletes(deleteBinding, b)) {
						extraSystemDeletes.add(b);
					}
				}
			}
		}

		// update the model
		bindingModel.remove(binding);
		bindingAdd(binding);
		if (!extraSystemDeletes.isEmpty()) {
			Iterator i = extraSystemDeletes.iterator();
			while (i.hasNext()) {
				KeyBinding b = (KeyBinding) i.next();
				KeyBinding newBinding = new KeyBinding(b.getKeySequence(), b
						.getParameterizedCommand(), b.getSchemeId(), b
						.getContextId(), null, null, null, Binding.USER);
				localChangeManager.addBinding(newBinding);

				bindingModel.remove(b);
				bindingModel.add(newBinding);
			}
		}
		updateConflicts(binding);
	}

	private final void updateConflicts(final Collection bindings) {
		Iterator i = bindings.iterator();
		while (i.hasNext()) {
			final Binding b = (Binding) i.next();
			if (b.getParameterizedCommand()!=null) {
				updateConflicts(b);
			}
		}
	}

	private final void updateConflicts(final Binding binding) {
		Collection matches = (Collection) localChangeManager
				.getActiveBindingsDisregardingContext().get(
						binding.getTriggerSequence());
		if (matches != null) {
			Iterator i = matches.iterator();
			while (i.hasNext()) {
				Binding b = (Binding) i.next();
				if (binding != b
						&& b.getContextId().equals(binding.getContextId())) {
					filteredTree.getViewer().update(b, null);
				}
			}
		}
	}

	private final void bindingRestore(final KeyBinding binding) {
		final ParameterizedCommand cmd = binding.getParameterizedCommand();
		bindingRestore(cmd, false);
	}

	private String locale = Locale.getDefault().toString();

	private boolean localMatches(String l) {
		if (l == null) {
			return true;
		}
		return Util.equals(locale, l);
	}

	private String platform = SWT.getPlatform();

	private boolean platformMatches(String p) {
		if (p == null) {
			return true;
		}
		return Util.equals(platform, p);
	}
	
	private final String[] getSchemeIds(String schemeId) {
		final List strings = new ArrayList();
		while (schemeId != null) {
			strings.add(schemeId);
			try {
				schemeId = bindingService.getScheme(schemeId).getParentId();
			} catch (final NotDefinedException e) {
				return new String[0];
			}
		}

		return (String[]) strings.toArray(new String[strings.size()]);
	}

	private final void bindingRestore(final ParameterizedCommand cmd,
			boolean removeCmd) {
		Set addSystemAll = new HashSet();
		ArrayList removeUser = new ArrayList();
		ArrayList removeBinding = new ArrayList();
		Binding[] bindings = localChangeManager.getBindings();
		for (int i = 0; i < bindings.length; i++) {
			final Binding b = bindings[i];
			if (b.getParameterizedCommand() == null
					&& localMatches(b.getLocale())
					&& platformMatches(b.getPlatform())) {
				// flat out, a delete marker
				removeBinding.add(b);
			} else if (cmd.equals(b.getParameterizedCommand())) {
				if (b.getType() == Binding.SYSTEM
						&& localMatches(b.getLocale())
						&& platformMatches(b.getPlatform())) {
					// a system binding for this command
					addSystemAll.add(b);
				} else if (b.getType() == Binding.USER) {
					// a user binding for this command
					removeUser.add(b);
					localChangeManager.removeBinding(b);
				}
			}
		}

		if (!addSystemAll.isEmpty()) {
			String[] activeSchemeIds = getSchemeIds(getSchemeId());
			Binding[] sysArray = (Binding[]) addSystemAll
					.toArray(new Binding[addSystemAll.size()]);
			for (int k = 0; k < sysArray.length; k++) {
				Binding sys = sysArray[k];
				boolean deleted = false;
				for (Iterator i = removeBinding.iterator(); i.hasNext();) {
					Binding del = (Binding) i.next();
					if (deletes(del, sys)) {
						if (del.getType() == Binding.USER) {
							removeUser.add(del);
							localChangeManager.removeBinding(del);
						} else {
							deleted = true;
							addSystemAll.remove(sys);
						}
					}
				}
				// Check the scheme ids.
				final String schemeId = sys.getSchemeId();
				boolean found = false;
				if (activeSchemeIds != null && !deleted) {
					for (int j = 0; j < activeSchemeIds.length; j++) {
						if (Util.equals(schemeId, activeSchemeIds[j])) {
							found = true;
							break;
						}
					}
				}
				if (!found && sys.getType() == Binding.SYSTEM) {
					addSystemAll.remove(sys);
				}
			}
		}
		

		bindingModel.addAll(addSystemAll);
		bindingModel.removeAll(removeUser);
		updateConflicts(addSystemAll);
		updateConflicts(removeUser);
		if (addSystemAll.isEmpty()) {
			commandModel.add(cmd);
			filteredTree.getViewer().setSelection(new StructuredSelection(cmd),
					true);
		} else if (removeCmd) {
			commandModel.remove(cmd);
		}
		if (!addSystemAll.isEmpty()) {
			// Select the new binding.
			filteredTree.getViewer().setSelection(
					new StructuredSelection(addSystemAll.iterator().next()),
					true);
		}

		update();
	}

	final static boolean deletes(final Binding del, final Binding binding) {
		boolean deletes = true;
		deletes &= Util.equals(del.getContextId(), binding.getContextId());
		deletes &= Util.equals(del.getTriggerSequence(), binding
				.getTriggerSequence());
		if (del.getLocale() != null) {
			deletes &= Util.equals(del.getLocale(), binding.getLocale());
		}
		if (del.getPlatform() != null) {
			deletes &= Util.equals(del.getPlatform(), binding.getPlatform());
		}
		deletes &= (binding.getType() == Binding.SYSTEM);
		deletes &= Util.equals(del.getParameterizedCommand(), null);

		return deletes;
	}

	/**
	 * Creates the button bar across the bottom of the preference page. This
	 * button bar contains the "Advanced..." button.
	 * 
	 * @param parent
	 *            The composite in which the button bar should be placed; never
	 *            <code>null</code>.
	 * @return The button bar composite; never <code>null</code>.
	 */
	private final Control createButtonBar(final Composite parent) {
		GridLayout layout;
		GridData gridData;
		int widthHint;

		// Create the composite to house the button bar.
		final Composite buttonBar = new Composite(parent, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		buttonBar.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		buttonBar.setLayoutData(gridData);

		// Advanced button.
		final Button advancedButton = new Button(buttonBar, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		advancedButton.setText(NewKeysPreferenceMessages.AdvancedButton_Text);
		gridData.widthHint = Math.max(widthHint, advancedButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		advancedButton.setLayoutData(gridData);
		advancedButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				KeysPreferenceFiltersDialog dialog = new KeysPreferenceFiltersDialog(
						getShell());
				dialog.setFilterActionSet(filterActionSetContexts);
				dialog.setFilterInternal(filterInternalContexts);
				dialog.setFilterUncategorized(filteredTree.isFilteringCategories());
				if (dialog.open() == Window.OK) {
					filterActionSetContexts = dialog.getFilterActionSet();
					filterInternalContexts = dialog.getFilterInternal();
					filteredTree.filterCategories(dialog
							.getFilterUncategorized());
					whenCombo.setInput(getContexts());
					updateDataControls();
				}
			}
		});
		return buttonBar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected final Control createContents(final Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.KEYS_PREFERENCE_PAGE);
		
		GridLayout layout = null;

		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}

		IDialogSettings settings = getDialogSettings();
		if (settings.get(TAG_FILTER_ACTION_SETS) != null) {
			filterActionSetContexts = settings
					.getBoolean(TAG_FILTER_ACTION_SETS);
		}
		if (settings.get(TAG_FILTER_INTERNAL) != null) {
			filterInternalContexts = settings.getBoolean(TAG_FILTER_INTERNAL);
		}
		patternFilter = new CategoryPatternFilter(
				true, commandService.getCategory(null));
		if (settings.get(TAG_FILTER_UNCAT) != null) {
			patternFilter.filterCategories(settings
					.getBoolean(TAG_FILTER_UNCAT));
		}

		// Creates a composite to hold all of the page contents.
		final Composite page = new Composite(parent, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		page.setLayout(layout);

		createSchemeControls(page);
		createTree(page);
		createTreeControls(page);
		createDataControls(page);
		createButtonBar(page);

		fill();
		update();

		applyDialogFont(page);

		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT, "Created page in " //$NON-NLS-1$
					+ elapsedTime + "ms"); //$NON-NLS-1$
		}

		return page;
	}

	private final Control createDataControls(final Composite parent) {
		GridLayout layout;
		GridData gridData;

		// Creates the data area.
		final Composite dataArea = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		dataArea.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		dataArea.setLayoutData(gridData);

		// LEFT DATA AREA
		// Creates the left data area.
		final Composite leftDataArea = new Composite(dataArea, SWT.NONE);
		layout = new GridLayout(3, false);
		leftDataArea.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalAlignment = SWT.FILL;
		leftDataArea.setLayoutData(gridData);

		// The command name label.
		final Label commandNameLabel = new Label(leftDataArea, SWT.NONE);
		commandNameLabel
				.setText(NewKeysPreferenceMessages.CommandNameLabel_Text);

		// The current command name.
		commandNameValueLabel = new Label(leftDataArea, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		commandNameValueLabel.setLayoutData(gridData);

		// The binding label.
		final Label bindingLabel = new Label(leftDataArea, SWT.NONE);
		bindingLabel.setText(NewKeysPreferenceMessages.BindingLabel_Text);

		// The key sequence entry widget.
		bindingText = new Text(leftDataArea, SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 200;
		bindingText.setLayoutData(gridData);

		bindingText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				bindingService.setKeyFilterEnabled(false);
			}

			public void focusLost(FocusEvent e) {
				bindingService.setKeyFilterEnabled(true);
			}
		});
		bindingText.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!bindingService.isKeyFilterEnabled()) {
					bindingService.setKeyFilterEnabled(true);
				}
			}
		});

		keySequenceText = new KeySequenceText(bindingText);
		keySequenceText.setKeyStrokeLimit(4);
		keySequenceText
				.addPropertyChangeListener(new IPropertyChangeListener() {
					public final void propertyChange(
							final PropertyChangeEvent event) {
						if (!event.getOldValue().equals(event.getNewValue())) {
							keySequenceChanged();
						}
					}
				});

		// Button for adding trapped key strokes
		final Button addKeyButton = new Button(leftDataArea, SWT.LEFT
				| SWT.ARROW);
		addKeyButton
				.setToolTipText(NewKeysPreferenceMessages.AddKeyButton_ToolTipText);
		gridData = new GridData();
		gridData.heightHint = schemeCombo.getCombo().getTextHeight();
		addKeyButton.setLayoutData(gridData);

		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		final Control[] tabStops = dataArea.getTabList();
		final ArrayList newTabStops = new ArrayList();
		for (int i = 0; i < tabStops.length; i++) {
			Control tabStop = tabStops[i];
			newTabStops.add(tabStop);
			if (bindingText.equals(tabStop)) {
				newTabStops.add(addKeyButton);
			}
		}
		final Control[] newTabStopArray = (Control[]) newTabStops
				.toArray(new Control[newTabStops.size()]);
		dataArea.setTabList(newTabStopArray);

		// Construct the menu to attach to the above button.
		final Menu addKeyMenu = new Menu(addKeyButton);
		final Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
			final MenuItem menuItem = new MenuItem(addKeyMenu, SWT.PUSH);
			menuItem.setText(trappedKey.format());
			menuItem.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					keySequenceText.insert(trappedKey);
					bindingText.setFocus();
					bindingText.setSelection(bindingText.getTextLimit());
				}
			});
		}
		addKeyButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent selectionEvent) {
				Point buttonLocation = addKeyButton.getLocation();
				buttonLocation = dataArea.toDisplay(buttonLocation.x,
						buttonLocation.y);
				Point buttonSize = addKeyButton.getSize();
				addKeyMenu.setLocation(buttonLocation.x, buttonLocation.y
						+ buttonSize.y);
				addKeyMenu.setVisible(true);
			}
		});

		final IObservableValue selection = ViewersObservables
				.observeSingleSelection(filteredTree.getViewer());

		// The when label.
		final Label whenLabel = new Label(leftDataArea, SWT.NONE);
		whenLabel.setText(NewKeysPreferenceMessages.WhenLabel_Text);

		// The when combo.
		whenCombo = new ComboViewer(leftDataArea);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		whenCombo.getCombo().setLayoutData(gridData);
		whenCombo.setLabelProvider(new NamedHandleObjectLabelProvider());
		whenCombo.setContentProvider(new ArrayContentProvider());
		whenCombo.setComparator(new ViewerComparator());
		whenCombo
				.addPostSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						updateWhenCombo();
					}
				});

		whenCombo.getCombo().setVisibleItemCount(20);
		whenCombo.getCombo().setVisible(false);
		whenLabel.setVisible(false);
		selection.addValueChangeListener(new IValueChangeListener() {

			public void handleValueChange(ValueChangeEvent event) {
				boolean visible = false;
				if (selection.getValue() instanceof KeyBinding) {
					visible = true;
				}
				Combo combo = whenCombo.getCombo();
				if (!combo.isDisposed()) {
					combo.setVisible(visible);
				}
				if (!whenLabel.isDisposed()) {
					whenLabel.setVisible(visible);
				}
			}
		});
		
		final Label asterisk = new Label(leftDataArea, SWT.NONE);
		asterisk.setText(NewKeysPreferenceMessages.Asterisk_Text);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		asterisk.setLayoutData(gridData);

		// RIGHT DATA AREA
		// Creates the right data area.
		final Composite rightDataArea = new Composite(dataArea, SWT.NONE);
		layout = new GridLayout(1, false);
		rightDataArea.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		rightDataArea.setLayoutData(gridData);

		// The description label.
		final Label descriptionLabel = new Label(rightDataArea, SWT.NONE);
		descriptionLabel
				.setText(NewKeysPreferenceMessages.DescriptionLabel_Text);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		descriptionLabel.setLayoutData(gridData);

		// The description value.
		descriptionValueText = new Text(rightDataArea, SWT.BORDER | SWT.MULTI
				| SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalIndent = 20;
		descriptionValueText.setLayoutData(gridData);
		return dataArea;
	}

	private final Control createSchemeControls(final Composite parent) {
		GridLayout layout;
		GridData gridData;

		// Create a composite to hold the controls.
		final Composite schemeControls = new Composite(parent, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		schemeControls.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		schemeControls.setLayoutData(gridData);

		// Create the label.
		final Label schemeLabel = new Label(schemeControls, SWT.NONE);
		schemeLabel.setText(NewKeysPreferenceMessages.SchemeLabel_Text);

		// Create the combo.
		schemeCombo = new ComboViewer(schemeControls);
		schemeCombo.setLabelProvider(new NamedHandleObjectLabelProvider());
		schemeCombo.setContentProvider(new ArrayContentProvider());
		gridData = new GridData();
		gridData.widthHint = 150;
		gridData.horizontalAlignment = SWT.FILL;
		schemeCombo.getCombo().setLayoutData(gridData);
		schemeCombo
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public final void selectionChanged(
							final SelectionChangedEvent event) {
						selectSchemeCombo(event);
					}
				});

		return schemeControls;
	}

	private final Control createTree(final Composite parent) {
		GridData gridData;

		filteredTree = new CategoryFilterTree(parent, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.BORDER, patternFilter);
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		filteredTree.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		filteredTree.setLayoutData(gridData);

		// Make sure the filtered tree has a height of ITEMS_TO_SHOW
		final Tree tree = filteredTree.getViewer().getTree();
		tree.setHeaderVisible(true);
		final Object layoutData = tree.getLayoutData();
		if (layoutData instanceof GridData) {
			gridData = (GridData) layoutData;
			final int itemHeight = tree.getItemHeight();
			if (itemHeight > 1) {
				gridData.heightHint = ITEMS_TO_SHOW * itemHeight;
			}
		}

		final BindingComparator comparator = new BindingComparator();
		comparator.setSortColumn(0);

		// Create the columns for the tree.

		final TreeColumn commandNameColumn = new TreeColumn(tree, SWT.LEFT,
				BindingLabelProvider.COLUMN_COMMAND);
		commandNameColumn
				.setText(NewKeysPreferenceMessages.CommandNameColumn_Text);
		tree.setSortColumn(commandNameColumn);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		commandNameColumn.addSelectionListener(new ResortColumn(comparator,
				commandNameColumn, tree, BindingLabelProvider.COLUMN_COMMAND));

		final TreeColumn triggerSequenceColumn = new TreeColumn(tree, SWT.LEFT,
				BindingLabelProvider.COLUMN_TRIGGER_SEQUENCE);
		triggerSequenceColumn
				.setText(NewKeysPreferenceMessages.TriggerSequenceColumn_Text);
		triggerSequenceColumn.addSelectionListener(new ResortColumn(comparator,
				triggerSequenceColumn, tree,
				BindingLabelProvider.COLUMN_TRIGGER_SEQUENCE));

		final TreeColumn whenColumn = new TreeColumn(tree, SWT.LEFT,
				BindingLabelProvider.COLUMN_WHEN);
		whenColumn.setText(NewKeysPreferenceMessages.WhenColumn_Text);
		whenColumn.addSelectionListener(new ResortColumn(comparator,
				whenColumn, tree, BindingLabelProvider.COLUMN_WHEN));

		final TreeColumn categoryColumn = new TreeColumn(tree, SWT.LEFT,
				BindingLabelProvider.COLUMN_CATEGORY);
		categoryColumn.setText(NewKeysPreferenceMessages.CategoryColumn_Text);
		categoryColumn.addSelectionListener(new ResortColumn(comparator,
				categoryColumn, tree, BindingLabelProvider.COLUMN_CATEGORY));

		final TreeColumn userMarker = new TreeColumn(tree, SWT.LEFT,
				BindingLabelProvider.COLUMN_USER);
		userMarker.setText(NewKeysPreferenceMessages.UserColumn_Text);
		userMarker.addSelectionListener(new ResortColumn(comparator,
				userMarker, tree, BindingLabelProvider.COLUMN_USER));

		// Set up the providers for the viewer.
		final TreeViewer viewer = filteredTree.getViewer();
		viewer.setLabelProvider(new BindingLabelProvider());

		viewer.setContentProvider(new ObservableSetContentProvider());

		viewer.setComparator(comparator);

		/*
		 * Listen for selection changes so that the data controls can be
		 * updated.
		 */
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public final void selectionChanged(final SelectionChangedEvent event) {
				selectTreeRow(event);
			}
		});

		// Adjust how the filter works.
		filteredTree.getPatternFilter().setIncludeLeadingWildcard(true);
		return filteredTree;
	}

	private final Control createTreeControls(final Composite parent) {
		GridLayout layout;
		GridData gridData;
		int widthHint;

		// Creates controls related to the tree.
		final Composite treeControls = new Composite(parent, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		treeControls.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.END;
		treeControls.setLayoutData(gridData);

		final IObservableValue selection = ViewersObservables
				.observeSingleSelection(filteredTree.getViewer());

		// Create the delete binding button.
		final Button addBindingButton = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		addBindingButton
				.setText(NewKeysPreferenceMessages.AddBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, addBindingButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		addBindingButton.setLayoutData(gridData);
		addBindingButton.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent event) {
				selectAddBindingButton(event);
			}
		});
		new ControlUpdater(addBindingButton) {
			protected void updateControl() {
				Object selectedObject = selection.getValue();
				addBindingButton
						.setEnabled(selectedObject instanceof KeyBinding);
			}
		};

		// Create the delete binding button.
		final Button removeBindingButton = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		removeBindingButton
				.setText(NewKeysPreferenceMessages.RemoveBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, removeBindingButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		removeBindingButton.setLayoutData(gridData);
		removeBindingButton.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent event) {
				selectRemoveBindingButton(event);
			}
		});
		new ControlUpdater(removeBindingButton) {
			protected void updateControl() {
				Object selectedObject = selection.getValue();
				removeBindingButton
						.setEnabled(selectedObject instanceof KeyBinding);
			}
		};

		// Create the delete binding button.
		final Button restore = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		restore.setText(NewKeysPreferenceMessages.RestoreBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, restore.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		restore.setLayoutData(gridData);
		restore.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent event) {
				selectRestoreBindingButton(event);
			}
		});

		return treeControls;
	}

	/**
	 * Copies all of the information from the workbench into a local change
	 * manager, and then the local change manager is used to populate the
	 * contents of the various widgets on the page.
	 * 
	 * The widgets affected by this method are: scheme combo, bindings
	 * table/tree model, and the when combo.
	 */
	private final void fill() {
		// Make an internal binding manager to track changes.
		localChangeManager = new BindingManager(new ContextManager(),
				new CommandManager());
		final Scheme[] definedSchemes = bindingService.getDefinedSchemes();
		try {
			for (int i = 0; i < definedSchemes.length; i++) {
				final Scheme scheme = definedSchemes[i];
				final Scheme copy = localChangeManager
						.getScheme(scheme.getId());
				copy.define(scheme.getName(), scheme.getDescription(), scheme
						.getParentId());
			}
			localChangeManager
					.setActiveScheme(bindingService.getActiveScheme());
		} catch (final NotDefinedException e) {
			throw new Error(
					"There is a programmer error in the keys preference page"); //$NON-NLS-1$
		}
		localChangeManager.setLocale(bindingService.getLocale());
		localChangeManager.setPlatform(bindingService.getPlatform());
		localChangeManager.setBindings(bindingService.getBindings());

		// Update the scheme combo.
		schemeCombo
				.setInput(sortByName(localChangeManager.getDefinedSchemes()));
		setScheme(localChangeManager.getActiveScheme());

		// Update the when combo.
		whenCombo.setInput(getContexts());

		commandModel = new WritableSet();
		bindingModel = new WritableSet();
		model = new UnionSet(
				new IObservableSet[] { bindingModel, commandModel });

		bindingModel.addAll(localChangeManager
				.getActiveBindingsDisregardingContextFlat());
		fillInCommands();

		if (DEBUG) {
			Tracing.printTrace(TRACING_COMPONENT,
					"fill in size: " + model.size()); //$NON-NLS-1$
		}

		filteredTree.getViewer().setInput(model);
	}

	/**
	 * 
	 */
	private void fillInCommands() {
		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}
		
		final Collection commandIds = commandService.getDefinedCommandIds();
		final Collection commands = new HashSet();
		final Iterator commandIdItr = commandIds.iterator();
		while (commandIdItr.hasNext()) {
			final String currentCommandId = (String) commandIdItr.next();
			final Command currentCommand = commandService
					.getCommand(currentCommandId);
			try {
				commands.addAll(ParameterizedCommand
						.generateCombinations(currentCommand));
			} catch (final NotDefinedException e) {
				// It is safe to just ignore undefined commands.
			}
		}

		// Remove duplicates.
		Iterator i = bindingModel.iterator();
		while (i.hasNext()) {
			commands.remove(((Binding) i.next()).getParameterizedCommand());
		}

		commandModel.addAll(commands);

		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT, "fillInCommands in " //$NON-NLS-1$
					+ elapsedTime + "ms"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public final void init(final IWorkbench workbench) {
		bindingService = (IBindingService) workbench
				.getService(IBindingService.class);
		commandImageService = (ICommandImageService) workbench
				.getService(ICommandImageService.class);
		commandService = (ICommandService) workbench
				.getService(ICommandService.class);
		contextService = (IContextService) workbench
				.getService(IContextService.class);
	}

	/**
	 * Updates the interface as the key sequence has changed. This finds the
	 * selected item. If the selected item is a binding, then it updates the
	 * binding -- either by updating a user binding, or doing the deletion
	 * marker dance with a system binding. If the selected item is a
	 * parameterized command, then a binding is created based on the data
	 * controls.
	 */
	private final void keySequenceChanged() {
		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}

		final KeySequence keySequence = keySequenceText.getKeySequence();
		if (!keySequence.isComplete()) {
			return;
		}

		if ((keySequence == null) || (keySequence.isEmpty())) {
			ISelection selection = filteredTree.getViewer().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				final Object node = structuredSelection.getFirstElement();
				if (node instanceof KeyBinding) {
					bindingRemove((KeyBinding) node);
				}
			}
			return;
		}

		ISelection selection = filteredTree.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			final Object node = structuredSelection.getFirstElement();
			if (node != null) {
				final Object object = node;
				selection = whenCombo.getSelection();
				final String contextId;
				if (selection instanceof IStructuredSelection) {
					structuredSelection = (IStructuredSelection) selection;
					final Object firstElement = structuredSelection
							.getFirstElement();
					if (firstElement == null) {
						contextId = IContextIds.CONTEXT_ID_WINDOW;
					} else {
						contextId = ((Context) firstElement).getId();
					}
				} else {
					contextId = IContextIds.CONTEXT_ID_WINDOW;
				}
				if (object instanceof KeyBinding) {
					KeyBinding keyBinding = (KeyBinding) object;
					if (!keyBinding.getContextId().equals(contextId)
							|| !keyBinding.getKeySequence().equals(keySequence)) {
						final KeyBinding binding = new KeyBinding(
								keySequence,
								keyBinding.getParameterizedCommand(),
								getSchemeId(),
								contextId, null, null, null, Binding.USER);

						ArrayList extraSystemDeletes = new ArrayList();
						if (keyBinding.getType() == Binding.USER) {
							localChangeManager.removeBinding(keyBinding);
						} else {							
							// TODO This should be the user's personal scheme.
							Collection previousConflictMatches = (Collection) localChangeManager
									.getActiveBindingsDisregardingContext().get(
											keyBinding.getTriggerSequence());
							KeyBinding deleteBinding = new KeyBinding(
									keyBinding.getKeySequence(), null,
									keyBinding.getSchemeId(), keyBinding
											.getContextId(), null, null, null,
									Binding.USER);
							localChangeManager.addBinding(deleteBinding);
							if (previousConflictMatches != null) {
								Iterator i = previousConflictMatches.iterator();
								while (i.hasNext()) {
									Binding b = (Binding) i.next();
									if (b != keyBinding && deletes(deleteBinding, b)) {
										extraSystemDeletes.add(b);
									}
								}
							}
						}
						localChangeManager.addBinding(binding);
						// update the model
						bindingModel.remove(keyBinding);
						bindingModel.add(binding);
						if (!extraSystemDeletes.isEmpty()) {
							Iterator i = extraSystemDeletes.iterator();
							while (i.hasNext()) {
								KeyBinding b = (KeyBinding) i.next();
								KeyBinding newBinding = new KeyBinding(b.getKeySequence(), b
										.getParameterizedCommand(), b.getSchemeId(), b
										.getContextId(), null, null, null, Binding.USER);
								localChangeManager.addBinding(newBinding);

								bindingModel.remove(b);
								bindingModel.add(newBinding);
							}
						}
						updateConflicts(keyBinding);
						updateConflicts(binding);
						// end update the model
						update();
						filteredTree.getViewer().setSelection(
								new StructuredSelection(binding), true);
					}
				} else if (object instanceof ParameterizedCommand) {
					// TODO This should use the user's personal scheme.
					final KeyBinding binding = new KeyBinding(keySequence,
							(ParameterizedCommand) object,
							getSchemeId(),
							contextId, null, null, null, Binding.USER);
					localChangeManager.addBinding(binding);
					// update the model
					// end update the model
					bindingModel.add(binding);
					commandModel.remove(object);
					updateConflicts(binding);
					update();

					filteredTree.getViewer().setSelection(
							new StructuredSelection(binding), true);
				}
			}
		}
		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT, "keySequenceChanged in " //$NON-NLS-1$
					+ elapsedTime + "ms"); //$NON-NLS-1$
		}
	}

	/**
	 * Logs the given exception, and opens an error dialog saying that something
	 * went wrong. The exception is assumed to have something to do with the
	 * preference store.
	 * 
	 * @param exception
	 *            The exception to be logged; must not be <code>null</code>.
	 */
	private final void logPreferenceStoreException(final Throwable exception) {
		final String message = NewKeysPreferenceMessages.PreferenceStoreError_Message;
		String exceptionMessage = exception.getMessage();
		if (exceptionMessage == null) {
			exceptionMessage = message;
		}
		final IStatus status = new Status(IStatus.ERROR,
				WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
		WorkbenchPlugin.log(message, status);
		StatusUtil.handleStatus(message, exception, StatusManager.SHOW);
	}

	protected final void performDefaults() {

		// Ask the user to confirm
		final String title = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxText;
		final String message = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxMessage;
		final boolean confirmed = MessageDialog.openConfirm(getShell(), title,
				message);

		if (confirmed) {
			// Fix the scheme in the local changes.
			final String defaultSchemeId = bindingService.getDefaultSchemeId();
			final Scheme defaultScheme = localChangeManager
					.getScheme(defaultSchemeId);
			try {
				localChangeManager.setActiveScheme(defaultScheme);
			} catch (final NotDefinedException e) {
				// At least we tried....
			}

			// Fix the bindings in the local changes.
			final Binding[] currentBindings = localChangeManager.getBindings();
			final int currentBindingsLength = currentBindings.length;
			final Set trimmedBindings = new HashSet();
			for (int i = 0; i < currentBindingsLength; i++) {
				final Binding binding = currentBindings[i];
				if (binding.getType() != Binding.USER) {
					trimmedBindings.add(binding);
				}
			}
			final Binding[] trimmedBindingArray = (Binding[]) trimmedBindings
					.toArray(new Binding[trimmedBindings.size()]);
			localChangeManager.setBindings(trimmedBindingArray);

			// Apply the changes.
			try {
				bindingService.savePreferences(defaultScheme,
						trimmedBindingArray);
			} catch (final IOException e) {
				logPreferenceStoreException(e);
			}
			long startTime = 0L;
			if (DEBUG) {
				startTime = System.currentTimeMillis();
			}
			busyRefillTree();
			if (DEBUG) {
				final long elapsedTime = System.currentTimeMillis() - startTime;
				Tracing.printTrace(TRACING_COMPONENT,
						"performDefaults:model in " //$NON-NLS-1$
								+ elapsedTime + "ms"); //$NON-NLS-1$
			}
		}

		setScheme(localChangeManager.getActiveScheme());

		super.performDefaults();
	}

	/**
	 * We're re-filling the  entire tree, both bindings and commands.  It's
	 * loud.
	 */
	private void busyRefillTree() {
		if (bindingModel==null) {
			// we haven't really been created yet.
			return;
		}
		BusyIndicator.showWhile(filteredTree.getViewer().getTree()
				.getDisplay(), new Runnable() {
			public void run() {
				try {
					filteredTree.getViewer().getTree().setRedraw(false);

					bindingModel.clear();
					commandModel.clear();
					Collection comeBack = localChangeManager
							.getActiveBindingsDisregardingContextFlat();
					bindingModel.addAll(comeBack);

					// showAllCheckBox.setSelection(false);
					fillInCommands();
				} finally {
					filteredTree.getViewer().getTree().setRedraw(true);
				}
			}
		});
		updateDataControls();
	}

	public final boolean performOk() {
		// Save the preferences.
		try {
			bindingService.savePreferences(
					localChangeManager.getActiveScheme(), localChangeManager
							.getBindings());
		} catch (final IOException e) {
			logPreferenceStoreException(e);
		}
		saveState(getDialogSettings());
		return super.performOk();
	}

	/**
	 * Handles the selection event on the add binding button. This adds a new
	 * binding based on the current selection.
	 * 
	 * @param event
	 *            Ignored.
	 */
	private final void selectAddBindingButton(final SelectionEvent event) {
		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}

		// Check to make sure we've got a selection.
		final TreeViewer viewer = filteredTree.getViewer();
		final ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		final Object firstElement = structuredSelection.getFirstElement();
		final Object value = firstElement;
		if (value instanceof KeyBinding) {
			bindingAdd((KeyBinding) value);
		} else if (value instanceof ParameterizedCommand) {
			bindingText.setFocus();
		}

		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT, "selectAddBindingButton in " //$NON-NLS-1$
					+ elapsedTime + "ms"); //$NON-NLS-1$
		}
	}

	/**
	 * Handles the selection event on the remove binding button. This removes
	 * the selected binding.
	 * 
	 * @param event
	 *            Ignored.
	 */
	private final void selectRemoveBindingButton(final SelectionEvent event) {
		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}
		// Check to make sure we've got a selection.
		final TreeViewer viewer = filteredTree.getViewer();
		final ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		final Object firstElement = structuredSelection.getFirstElement();
		final Object value = firstElement;
		if (value instanceof KeyBinding) {
			bindingRemove((KeyBinding) value);
		} else if (value == markedParameterizedCommand) {
			commandModel.remove(markedParameterizedCommand);
			markedParameterizedCommand = null;
			markedContextId = null;
			update();
		}
		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT,
					"selectRemoveBindingButton in " //$NON-NLS-1$
							+ elapsedTime + "ms"); //$NON-NLS-1$
		}
	}

	private final void selectRestoreBindingButton(final SelectionEvent event) {
		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}
		// Check to make sure we've got a selection.
		final TreeViewer viewer = filteredTree.getViewer();
		final ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		final Object firstElement = structuredSelection.getFirstElement();
		final Object value = firstElement;
		if (value instanceof KeyBinding) {
			bindingRestore((KeyBinding) value);
		} else if (value instanceof ParameterizedCommand) {
			bindingRestore((ParameterizedCommand) value, true);
		}
		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT,
					"selectRestoreBindingButton in " //$NON-NLS-1$
							+ elapsedTime + "ms"); //$NON-NLS-1$
		}
	}

	/**
	 * Handles a selection event on the scheme combo. If the scheme has changed,
	 * then the local change manager is updated, and the page's contents are
	 * updated as well.
	 * 
	 * @param event
	 *            The selection event; must not be <code>null</code>.
	 */
	private final void selectSchemeCombo(final SelectionChangedEvent event) {
		final ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			final Object firstElement = ((IStructuredSelection) selection)
					.getFirstElement();
			if (firstElement instanceof Scheme) {
				final Scheme newScheme = (Scheme) firstElement;
				if (newScheme != localChangeManager.getActiveScheme()) {
					try {
						localChangeManager.setActiveScheme(newScheme);
						busyRefillTree();
					} catch (final NotDefinedException e) {
						// TODO The scheme wasn't valid.
					}
				}
			}
		}
	}

	/**
	 * If the row has changed, then update the data controls.
	 */
	private final void selectTreeRow(final SelectionChangedEvent event) {
		updateDataControls();
	}

	/**
	 * Sets the currently selected scheme. Setting the scheme always triggers an
	 * update of the underlying widgets.
	 * 
	 * @param scheme
	 *            The scheme to select; may be <code>null</code>.
	 */
	private final void setScheme(final Scheme scheme) {
		schemeCombo.setSelection(new StructuredSelection(scheme));
	}

	/**
	 * Updates all of the controls on this preference page in response to a user
	 * interaction.
	 */
	private final void update() {
		updateTree();
		updateDataControls();
	}

	/**
	 * Updates the data controls to match the current selection, if any.
	 */
	private final void updateDataControls() {
		final ISelection selection = filteredTree.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			final Object node = structuredSelection.getFirstElement();
			if (node != null) {
				final Object object = node;
				if (object instanceof KeyBinding) {
					final KeyBinding binding = (KeyBinding) object;
					try {
						commandNameValueLabel.setText(binding
								.getParameterizedCommand().getName());
						String description = binding.getParameterizedCommand()
								.getCommand().getDescription();
						if (description == null) {
							description = Util.ZERO_LENGTH_STRING;
						}
						descriptionValueText.setText(description);
					} catch (final NotDefinedException e) {
						// It's probably okay to just let this one slide.
					}
					whenCombo.setSelection(new StructuredSelection(
							contextService.getContext(binding.getContextId())));
					keySequenceText.setKeySequence(binding.getKeySequence());

				} else if (object instanceof ParameterizedCommand) {
					final ParameterizedCommand command = (ParameterizedCommand) object;
					try {
						commandNameValueLabel.setText(command.getName());
						String description = command.getCommand()
								.getDescription();
						if (description == null) {
							description = Util.ZERO_LENGTH_STRING;
						}
						descriptionValueText.setText(description);
					} catch (final NotDefinedException e) {
						// It's probably okay to just let this one slide.
					}
					keySequenceText.clear();
					if (command == markedParameterizedCommand) {
						whenCombo.setSelection(new StructuredSelection(
								contextService.getContext(markedContextId)));
					} else {
						whenCombo
								.setSelection(new StructuredSelection(
										contextService
												.getContext(IContextIds.CONTEXT_ID_WINDOW)));
					}
				}
			} else {
				commandNameValueLabel.setText(""); //$NON-NLS-1$
				descriptionValueText.setText(""); //$NON-NLS-1$
				keySequenceText.clear();
				whenCombo.setSelection(null);
			}
		}
	}

	private final void updateTree() {
		long startTime = 0L;
		if (DEBUG) {
			startTime = System.currentTimeMillis();
		}

		final TreeViewer viewer = filteredTree.getViewer();

		// Add the marked parameterized command, if any.
		if (markedParameterizedCommand != null) {
			commandModel.add(markedParameterizedCommand);
			markedParameterizedCommand = null;
		}

		// Repack all of the columns.
		final Tree tree = viewer.getTree();
		final TreeColumn[] columns = tree.getColumns();

		columns[BindingLabelProvider.COLUMN_COMMAND].setWidth(240);
		columns[BindingLabelProvider.COLUMN_TRIGGER_SEQUENCE].setWidth(130);
		columns[BindingLabelProvider.COLUMN_WHEN].setWidth(130);
		columns[BindingLabelProvider.COLUMN_CATEGORY].setWidth(130);
		columns[BindingLabelProvider.COLUMN_USER].setWidth(50);

		if (DEBUG) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Tracing.printTrace(TRACING_COMPONENT, "Refreshed page in " //$NON-NLS-1$
					+ elapsedTime + "ms"); //$NON-NLS-1$
		}
	}

	/**
	 * Save the state of the receiver.
	 * 
	 * @param dialogSettings
	 */
	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}
		dialogSettings.put(TAG_FILTER_ACTION_SETS, filterActionSetContexts);
		dialogSettings.put(TAG_FILTER_INTERNAL, filterInternalContexts);
		dialogSettings.put(TAG_FILTER_UNCAT, filteredTree.isFilteringCategories());
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();

		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}

	protected Object[] getContexts() {

		Context[] contexts = contextService.getDefinedContexts();
		List filteredContexts = new ArrayList();
		try {
			if (filterActionSetContexts) {
				for (int i = 0; i < contexts.length; i++) {
					String parentId = contexts[i].getParentId();
					boolean check = false;
					if (contexts[i].getId().equalsIgnoreCase(
							CONTEXT_ID_ACTION_SETS)) {
						check = true;
					}
					while (parentId != null) {
						if (parentId.equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
							check = true;
						}
						parentId = contextService.getContext(parentId)
								.getParentId();
					}
					if (!check) {
						filteredContexts.add(contexts[i]);
					}
				}
			} else {
				filteredContexts.addAll(Arrays.asList(contexts));
			}

			if (filterInternalContexts) {
				for (int i = 0; i < filteredContexts.size(); i++) {
					if (((Context) filteredContexts.get(i)).getId().indexOf(
							CONTEXT_ID_INTERNAL) != -1) {
						filteredContexts.remove(i);
					}
				}
			}

		} catch (NotDefinedException e) {
			return contexts;
		}

		return filteredContexts.toArray();
	}

	private void updateWhenCombo() {
		ISelection selection = filteredTree.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object node = structuredSelection.getFirstElement();
			if (node != null) {
				final Object object = node;
				selection = whenCombo.getSelection();
				final String contextId;
				if (selection instanceof IStructuredSelection) {
					structuredSelection = (IStructuredSelection) selection;
					final Object firstElement = structuredSelection
							.getFirstElement();
					if (firstElement == null) {
						contextId = IContextIds.CONTEXT_ID_WINDOW;
					} else {
						contextId = ((Context) firstElement).getId();
					}
				} else {
					contextId = IContextIds.CONTEXT_ID_WINDOW;
				}
				if (object instanceof KeyBinding) {
					KeyBinding keyBinding = (KeyBinding) object;
					if (!keyBinding.getContextId().equals(contextId)) {
						final KeyBinding binding = new KeyBinding(
								keyBinding.getKeySequence(),
								keyBinding.getParameterizedCommand(),
								getSchemeId(),
								contextId, null, null, null, Binding.USER);

						if (keyBinding.getType() == Binding.USER) {
							localChangeManager.removeBinding(keyBinding);
						} else {
							localChangeManager.addBinding(new KeyBinding(
									keyBinding.getKeySequence(), null,
									keyBinding.getSchemeId(), keyBinding
											.getContextId(), null, null, null,
									Binding.USER));
						}
						localChangeManager.addBinding(binding);
						// update the model
						bindingModel.remove(keyBinding);
						bindingModel.add(binding);
						updateConflicts(keyBinding);
						updateConflicts(binding);
						// end update the model
						update();
						filteredTree.getViewer().setSelection(
								new StructuredSelection(binding), true);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
	 */
	public void applyData(Object data) {
		if (data instanceof Binding && filteredTree != null) {
			filteredTree.getViewer().setSelection(
					new StructuredSelection(data), true);
		}
	}
	
	public String getSchemeId() {
		ISelection sel = schemeCombo.getSelection();
		if (sel instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection)sel).getFirstElement();
			if (o instanceof Scheme) {
				return ((Scheme)o).getId();
			}
		}
		return IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID;
	}
}
