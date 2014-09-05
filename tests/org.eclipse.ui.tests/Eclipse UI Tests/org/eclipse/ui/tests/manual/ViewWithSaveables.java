/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.manual;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.part.ViewPart;

public class ViewWithSaveables extends ViewPart implements ISaveablesSource,
		ISaveablePart {

	WritableList saveables = new WritableList();
	IObservableValue dirty = new ComputedValue() {
		@Override
		protected Object calculate() {
			for (Iterator it = saveables.iterator(); it.hasNext();) {
				MySaveable saveable = (MySaveable) it.next();
				if (saveable.isDirty()) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}
	};
	private TableViewer viewer;
	private IObservableValue selection;

	public ViewWithSaveables() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).span(4,1).applyTo(viewer.getControl());
		ObservableListContentProvider observableListContentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(observableListContentProvider);
		viewer.setLabelProvider(new ObservableMapLabelProvider(
				new DirtyObservableMap(observableListContentProvider
						.getKnownElements())) {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				return getText(element);
			}

			@Override
			public String getText(Object element) {
				MySaveable saveable = (MySaveable) element;
				return (saveable.isDirty() ? "*" : "") + saveable.toString();
			}
		});
		viewer.setInput(saveables);
		{
			Button button = new Button(parent, SWT.PUSH);
			button.setText("Add");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addSaveable();
				}
			});
		}
		selection = ViewersObservables.observeSingleSelection(viewer);
		{
			final Button button = new Button(parent, SWT.PUSH);
			button.setText("Remove");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeSaveable();
				}
			});
			new ControlUpdater(button) {
				@Override
				protected void updateControl() {
					button.setEnabled(selection.getValue() != null);
				}
			};
		}
		{
			final Button button = new Button(parent, SWT.CHECK);
			button.setText("dirty");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MySaveable saveable = (MySaveable) selection.getValue();
					saveable.setDirty(button.getSelection());
				}
			});
			new ControlUpdater(button) {
				@Override
				protected void updateControl() {
					MySaveable saveable = (MySaveable) selection.getValue();
					if (saveable == null) {
						button.setEnabled(false);
						button.setSelection(false);
					} else {
						button.setEnabled(true);
						// we know that isDirty is implemented using a
						// WritableValue,
						// and thus a dependency on that writable value will
						// result from
						// calling isDirty().
						button.setSelection(saveable.isDirty());
					}
				}
			};
		}
		getSite().setSelectionProvider(viewer);
		dirty.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				firePropertyChange(ISaveablePart.PROP_DIRTY);
			}
		});
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).generateLayout(parent);
	}

	void removeSaveable() {
		Saveable[] toRemove = getActiveSaveables();
		ISaveablesLifecycleListener lifecycleListener = getSite()
				.getService(ISaveablesLifecycleListener.class);
		SaveablesLifecycleEvent event = new SaveablesLifecycleEvent(this,
				SaveablesLifecycleEvent.PRE_CLOSE, toRemove, false);
		lifecycleListener.handleLifecycleEvent(event);
		if (!event.isVeto()) {
			saveables.removeAll(Arrays.asList(toRemove));
			lifecycleListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
					this, SaveablesLifecycleEvent.POST_CLOSE, toRemove, false));
		}
	}

	void addSaveable() {
		MySaveable saveable = new MySaveable();
		saveables.add(saveable);
		ISaveablesLifecycleListener lifecycleListener = getSite()
				.getService(ISaveablesLifecycleListener.class);
		lifecycleListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
				this, SaveablesLifecycleEvent.POST_OPEN,
				new Saveable[] { saveable }, false));
	}

	@Override
	public void setFocus() {
	}

	@Override
	public Saveable[] getActiveSaveables() {
		Saveable selectedSaveable = (Saveable) selection.getValue();
		return selectedSaveable == null ? new Saveable[0]
				: new Saveable[] { selectedSaveable };
	}

	@Override
	public Saveable[] getSaveables() {
		return (Saveable[]) saveables.toArray(new Saveable[saveables.size()]);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		Assert.isTrue(false,
				"Save operations should happen through the saveables.");
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return ((Boolean) dirty.getValue()).booleanValue();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	class MySaveable extends Saveable {

		private IObservableValue myDirty = new WritableValue(Boolean.FALSE,
				Boolean.TYPE);

		@Override
		public void doSave(IProgressMonitor monitor) throws CoreException {
			setDirty(false);
		}

		@Override
		public boolean equals(Object object) {
			return this == object;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return toString();
		}

		@Override
		public String getToolTipText() {
			return toString();
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean isDirty() {
			return ((Boolean) myDirty.getValue()).booleanValue();
		}

		IObservableValue getDirty() {
			return myDirty;
		}

		void setDirty(boolean dirty) {
			myDirty.setValue(Boolean.valueOf(dirty));
		}

	}

	class DirtyObservableMap extends ComputedObservableMap {

		Map writableValueToElement = new HashMap();
		
		private IValueChangeListener valueChangeListener = new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
					fireMapChange(Diffs.createMapDiffSingleChange(writableValueToElement.get(event
							.getSource()), event.diff.getOldValue(), event.diff
							.getNewValue()));
			}
		};

		public DirtyObservableMap(IObservableSet knownElements) {
			super(knownElements);
			init();
		}

		@Override
		protected Object doGet(Object key) {
			MySaveable saveable = (MySaveable) key;
			return Boolean.valueOf(saveable.isDirty());
		}

		@Override
		protected Object doPut(Object key, Object value) {
				MySaveable saveable = (MySaveable) key;
				Boolean oldValue = Boolean.valueOf(saveable.isDirty());
				saveable.setDirty(((Boolean) value).booleanValue());
				keySet().add(key);
				return oldValue;
		}

		@Override
		protected void hookListener(Object key) {
			MySaveable saveable = (MySaveable) key;
			IObservableValue oValue = saveable.getDirty();
			writableValueToElement.put(oValue, saveable);
			oValue.addValueChangeListener(valueChangeListener);
		}

		@Override
		protected void unhookListener(Object key) {
			MySaveable saveable = (MySaveable) key;
			saveable.getDirty().removeValueChangeListener(valueChangeListener);
			writableValueToElement.remove(saveable.getDirty());
		}

	}

}
