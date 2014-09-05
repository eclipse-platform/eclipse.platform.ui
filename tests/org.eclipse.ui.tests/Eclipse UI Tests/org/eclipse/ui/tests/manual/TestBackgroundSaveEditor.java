/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.manual;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.IJobRunnable;

/**
 * @since 3.3
 * 
 */
public class TestBackgroundSaveEditor extends EditorPart implements
		ISaveablesSource {

	public class MySaveable extends Saveable {

		PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

		private boolean dirty;

		@Override
		public void doSave(IProgressMonitor monitor) throws CoreException {
			SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
			IJobRunnable runnable = doSave(subMonitor.newChild(1), getSite());
			if (runnable!=null) {
				runnable.run(subMonitor.newChild(1));
			}
			monitor.done();
		}

		@Override
		public IJobRunnable doSave(IProgressMonitor monitor,
				IShellProvider shellProvider) throws CoreException {
			monitor.beginTask("Saving in the foreground",
					data.foregroundSaveTime);
			data.setOutput("");
			for (int i = 0; i < data.foregroundSaveTime; i++) {
				if (monitor.isCanceled()) {
					return null;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				data.setOutput(data.getInput().substring(0,
						Math.min(i, data.getInput().length())));
				monitor.worked(1);
			}
			if (data.throwExceptionInForeground) {
				throw new CoreException(new Status(IStatus.ERROR,
						"org.eclipse.ui.tests",
						"Saving in the foreground failed"));
			}
			monitor.done();
			if (!data.saveInBackground) {
				data.setOutput(data.getInput());
				setDirty(false);
				return null;
			}
			IJobRunnable result = new IJobRunnable() {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Saving in the background",
							data.backgroundSaveTime);
					for (int i = 0; i < data.backgroundSaveTime; i++) {
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						data.setOutput(data.getInput().substring(
								0,
								Math.min(i + data.foregroundSaveTime, data
										.getInput().length())));
						monitor.worked(1);
					}
					if (data.throwExceptionInBackground) {
						return new Status(IStatus.ERROR,
								"org.eclipse.ui.tests",
								"Saving in the background failed");
					}
					data.setOutput(data.getInput());
					setDirty(false);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			return result;
		}

		@Override
		public boolean equals(Object object) {
			return this == object;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return input.getImageDescriptor();
		}

		@Override
		public String getName() {
			return input.getName();
		}

		@Override
		public String getToolTipText() {
			return input.getToolTipText();
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean isDirty() {
			return dirty;
		}

		public void setDirty(boolean dirty) {
			firePropertyChange("dirty", new Boolean(this.dirty), new Boolean(
					this.dirty = dirty));
			getSite().getShell().getDisplay().syncExec(new Runnable(){
				@Override
				public void run() {
					TestBackgroundSaveEditor.this
					.firePropertyChange(ISaveablePart.PROP_DIRTY);
				}});
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(propertyName, listener);
		}

		void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			changeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(propertyName, listener);
		}
	}

	private MySaveable mySaveable;
	private Text inputText;
	private IEditorInput input;

	public TestBackgroundSaveEditor() {
		mySaveable = new MySaveable();
	}

	@Override
	public void createPartControl(Composite parent) {
		Realm realm = SWTObservables.getRealm(parent.getDisplay());
		final DataBindingContext dbc = new DataBindingContext(realm);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dbc.dispose();
			}
		});

		final IObservableValue inputObservable = BeansObservables.observeValue(
				realm, data, "input");
		final IObservableValue outputObservable = BeansObservables
				.observeValue(realm, data, "output");

		createInputGroup(parent, dbc, inputObservable);
		createOptionsGroup(parent, realm, dbc);
		createOutputGroup(parent, dbc, outputObservable);

		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(true)
				.generateLayout(parent);
	}

	private void createOutputGroup(Composite parent,
			final DataBindingContext dbc,
			final IObservableValue outputObservable) {
		Group outputGroup = new Group(parent, SWT.NONE);
		outputGroup.setText("Output");
		Text outputText = new Text(outputGroup, SWT.BORDER | SWT.READ_ONLY
				| SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(outputText);
		dbc.bindValue(SWTObservables.observeText(outputText, SWT.NONE),
				outputObservable, null, null);
		GridLayoutFactory.swtDefaults().generateLayout(outputGroup);
	}

	private void createOptionsGroup(Composite parent, Realm realm,
			final DataBindingContext dbc) {
		Group optionsGroup = new Group(parent, SWT.NONE);
		optionsGroup.setText("Options");

		Button dirtyButton = new Button(optionsGroup, SWT.CHECK);
		new Label(optionsGroup, SWT.NONE).setText("Editor is dirty");
		IObservableValue dirtyObservable = BeansObservables.observeValue(realm,
				mySaveable, "dirty");
		dbc.bindValue(SWTObservables.observeSelection(dirtyButton),
				dirtyObservable, null, null);
		// IObservableValue inputAndOutputDiffer = new ComputedValue(realm) {
		// protected Object calculate() {
		// return new Boolean(!Util.equals(inputObservable.getValue(),
		// outputObservable.getValue()));
		// }
		// };
		// dbc.bindValue(dirtyObservable, inputAndOutputDiffer, null);

		Button saveInBackgroundButton = new Button(optionsGroup, SWT.CHECK);
		new Label(optionsGroup, SWT.NONE)
				.setText("Do part of the save in the background");
		dbc.bindValue(SWTObservables.observeSelection(saveInBackgroundButton),
				BeansObservables.observeValue(realm, data, "saveInBackground"),
				null, null);

		Button foregroundExceptionButton = new Button(optionsGroup, SWT.CHECK);
		new Label(optionsGroup, SWT.NONE)
				.setText("Throw exception while saving in the foreground");
		dbc.bindValue(SWTObservables
				.observeSelection(foregroundExceptionButton), BeansObservables
				.observeValue(realm, data, "throwExceptionInForeground"), null, null);

		Button backgroundExceptionButton = new Button(optionsGroup, SWT.CHECK);
		new Label(optionsGroup, SWT.NONE)
				.setText("Throw exception while saving in the background");
		dbc.bindValue(SWTObservables
				.observeSelection(backgroundExceptionButton), BeansObservables
				.observeValue(realm, data, "throwExceptionInBackground"), null, null);

		new Label(optionsGroup, SWT.NONE).setText("Foreground save time:");
		Text optionsForegroundTime = new Text(optionsGroup, SWT.BORDER);
		dbc.bindValue(SWTObservables.observeText(optionsForegroundTime,
				SWT.Modify), BeansObservables.observeValue(realm, data,
				"foregroundSaveTime"), null, null);

		new Label(optionsGroup, SWT.NONE).setText("Background save time:");
		Text optionsBackgroundTime = new Text(optionsGroup, SWT.BORDER);
		dbc.bindValue(SWTObservables.observeText(optionsBackgroundTime,
				SWT.Modify), BeansObservables.observeValue(realm, data,
				"backgroundSaveTime"), null, null);

		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(
				optionsGroup);
	}

	private void createInputGroup(Composite parent,
			final DataBindingContext dbc, final IObservableValue inputObservable) {
		Group inputGroup = new Group(parent, SWT.NONE);
		inputGroup.setText("Input");

		inputText = new Text(inputGroup, SWT.BORDER | SWT.MULTI);
		dbc.bindValue(SWTObservables.observeText(inputText, SWT.Modify),
				inputObservable, null, null);

		GridLayoutFactory.swtDefaults().generateLayout(inputGroup);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			mySaveable.doSave(monitor);
		} catch (CoreException e) {
			String title = "Save failed";
			WorkbenchPlugin.log(title, new Status(IStatus.WARNING,
					PlatformUI.PLUGIN_ID, 0, title, e));
			MessageDialog.openError(getSite().getShell(),
					WorkbenchMessages.Error, title + ':' + e.getMessage());
		}
	}

	@Override
	public void doSaveAs() {
		Assert.isTrue(false, "Should not be called");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		setSite(site);
		setInput(input);
		this.input = input;
	}

	@Override
	public boolean isDirty() {
		return mySaveable.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		inputText.setFocus();
	}

	public static class Data {
		PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
		public String input = "";
		public String output = "";
		public String buffer;
		public boolean saveInBackground;
		public boolean throwExceptionInForeground;
		public boolean throwExceptionInBackground;
		public int foregroundSaveTime;
		public int backgroundSaveTime;

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			firePropertyChange("output", this.output, this.output = output);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(propertyName, listener);
		}

		void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			changeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(propertyName, listener);
		}

		/**
		 * @return Returns the input.
		 */
		public String getInput() {
			return input;
		}

		/**
		 * @param input
		 *            The input to set.
		 */
		public void setInput(String input) {
			this.input = input;
		}

		/**
		 * @return Returns the buffer.
		 */
		public String getBuffer() {
			return buffer;
		}

		/**
		 * @param buffer
		 *            The buffer to set.
		 */
		public void setBuffer(String buffer) {
			this.buffer = buffer;
		}

		/**
		 * @return Returns the saveInBackground.
		 */
		public boolean isSaveInBackground() {
			return saveInBackground;
		}

		/**
		 * @param saveInBackground
		 *            The saveInBackground to set.
		 */
		public void setSaveInBackground(boolean saveInBackground) {
			this.saveInBackground = saveInBackground;
		}

		/**
		 * @return Returns the throwExceptionInForeground.
		 */
		public boolean isThrowExceptionInForeground() {
			return throwExceptionInForeground;
		}

		/**
		 * @param throwExceptionInForeground
		 *            The throwExceptionInForeground to set.
		 */
		public void setThrowExceptionInForeground(
				boolean throwExceptionInForeground) {
			this.throwExceptionInForeground = throwExceptionInForeground;
		}

		/**
		 * @return Returns the throwExceptionInBackground.
		 */
		public boolean isThrowExceptionInBackground() {
			return throwExceptionInBackground;
		}

		/**
		 * @param throwExceptionInBackground
		 *            The throwExceptionInBackground to set.
		 */
		public void setThrowExceptionInBackground(
				boolean throwExceptionInBackground) {
			this.throwExceptionInBackground = throwExceptionInBackground;
		}

		/**
		 * @return Returns the foregroundSaveTime.
		 */
		public int getForegroundSaveTime() {
			return foregroundSaveTime;
		}

		/**
		 * @param foregroundSaveTime
		 *            The foregroundSaveTime to set.
		 */
		public void setForegroundSaveTime(int foregroundSaveTime) {
			this.foregroundSaveTime = foregroundSaveTime;
		}

		/**
		 * @return Returns the backgroundSaveTime.
		 */
		public int getBackgroundSaveTime() {
			return backgroundSaveTime;
		}

		/**
		 * @param backgroundSaveTime
		 *            The backgroundSaveTime to set.
		 */
		public void setBackgroundSaveTime(int backgroundSaveTime) {
			this.backgroundSaveTime = backgroundSaveTime;
		}
	}

	private Data data = new Data();

	@Override
	public Saveable[] getActiveSaveables() {
		return new Saveable[] { mySaveable };
	}

	@Override
	public Saveable[] getSaveables() {
		return new Saveable[] { mySaveable };
	}
	
}
