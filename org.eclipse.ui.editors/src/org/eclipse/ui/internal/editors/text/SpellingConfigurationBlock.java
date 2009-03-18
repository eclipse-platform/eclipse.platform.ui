/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor;
import org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock;
import org.eclipse.ui.texteditor.spelling.SpellingEngineDescriptor;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Configures spelling preferences.
 *
 * @since 3.1
 */
class SpellingConfigurationBlock implements IPreferenceConfigurationBlock {

	/**
	 * Error preferences block.
	 */
	private static class ErrorPreferences implements ISpellingPreferenceBlock {

		/** Error message */
		private String fMessage;

		/** Error label */
		private Label fLabel;

		/**
		 * Initialize with the given error message.
		 *
		 * @param message the error message
		 */
		protected ErrorPreferences(String message) {
			fMessage= message;
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public Control createControl(Composite composite) {
			Composite inner= new Composite(composite, SWT.NONE);
			inner.setLayout(new FillLayout(SWT.VERTICAL));

			fLabel= new Label(inner, SWT.CENTER);
			fLabel.setText(fMessage);

			return inner;
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#initialize(org.eclipse.ui.texteditor.spelling.IStatusMonitor)
		 */
		public void initialize(IPreferenceStatusMonitor statusMonitor) {
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#canPerformOk()
		 */
		public boolean canPerformOk() {
			return true;
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#performOk()
		 */
		public void performOk() {
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#performDefaults()
		 */
		public void performDefaults() {
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#performRevert()
		 */
		public void performRevert() {
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock#setEnabled(boolean)
		 */
		public void setEnabled(boolean enabled) {
			fLabel.setEnabled(enabled);
		}
	}

	/**
	 * Forwarding status monitor for accessing the current status.
	 */
	private static class ForwardingStatusMonitor implements IPreferenceStatusMonitor {

		/** Status monitor to which status changes are forwarded */
		private IPreferenceStatusMonitor fForwardedMonitor;

		/** Latest reported status */
		private IStatus fStatus;

		/**
		 * Initializes with the given status monitor to which status changes are forwarded.
		 *
		 * @param forwardedMonitor the status monitor to which changes are forwarded
		 */
		public ForwardingStatusMonitor(IPreferenceStatusMonitor forwardedMonitor) {
			fForwardedMonitor= forwardedMonitor;
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor#statusChanged(org.eclipse.core.runtime.IStatus)
		 */
		public void statusChanged(IStatus status) {
			fStatus= status;
			fForwardedMonitor.statusChanged(status);
		}

		/**
		 * Returns the latest reported status.
		 *
		 * @return the latest reported status, can be <code>null</code>
		 */
		public IStatus getStatus() {
			return fStatus;
		}
	}

	/** The overlay preference store. */
	private final OverlayPreferenceStore fStore;

	/* The controls */
	private Combo fProviderCombo;
	private Button fEnablementCheckbox;
	private ComboViewer fProviderViewer;
	private Composite fComboGroup;
	private Composite fGroup;
	private StackLayout fStackLayout;

	/* the model */
	private final Map fProviderDescriptors;
	private final Map fProviderPreferences;
	private final Map fProviderControls;

	private ForwardingStatusMonitor fStatusMonitor;

	private ISpellingPreferenceBlock fCurrentBlock;


	public SpellingConfigurationBlock(OverlayPreferenceStore store, IPreferenceStatusMonitor statusMonitor) {
		Assert.isNotNull(store);
		fStore= store;
		fStore.addKeys(createOverlayStoreKeys());
		fStatusMonitor= new ForwardingStatusMonitor(statusMonitor);
		fProviderDescriptors= createListModel();
		fProviderPreferences= new HashMap();
		fProviderControls= new HashMap();
	}

	private Map createListModel() {
		SpellingEngineDescriptor[] descs= EditorsUI.getSpellingService().getSpellingEngineDescriptors();
		Map map= new HashMap();
		for (int i= 0; i < descs.length; i++) {
			map.put(descs[i].getId(), descs[i]);
		}
		return map;
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {

		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SpellingService.PREFERENCE_SPELLING_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, SpellingService.PREFERENCE_SPELLING_ENGINE));

		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	/**
	 * Creates page for spelling preferences.
	 *
	 * @param parent the parent composite
	 * @return the control for the preference page
	 */
	public Control createControl(Composite parent) {

		Composite composite= new Composite(parent, SWT.NULL);
		// assume parent page uses grid-data
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;

		PixelConverter pc= new PixelConverter(composite);
		layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);

		if (EditorsUI.getSpellingService().getSpellingEngineDescriptors().length == 0) {
			Label label= new Label(composite, SWT.NONE);
			label.setText(TextEditorMessages.SpellingConfigurationBlock_error_not_installed);
			return composite;
		}

		/* check box for new editors */
		fEnablementCheckbox= new Button(composite, SWT.CHECK);
		fEnablementCheckbox.setText(TextEditorMessages.SpellingConfigurationBlock_enable);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fEnablementCheckbox.setLayoutData(gd);
		fEnablementCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean enabled= fEnablementCheckbox.getSelection();
				fStore.setValue(SpellingService.PREFERENCE_SPELLING_ENABLED, enabled);
				updateCheckboxDependencies();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label label= new Label(composite, SWT.CENTER);
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(gd);

		if (fProviderDescriptors.size() > 1) {
			fComboGroup= new Composite(composite, SWT.NONE);
			gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
			gd.horizontalIndent= 10;
			fComboGroup.setLayoutData(gd);
			GridLayout gridLayout= new GridLayout(2, false);
			gridLayout.marginWidth= 0;
			fComboGroup.setLayout(gridLayout);

			Label comboLabel= new Label(fComboGroup, SWT.CENTER);
			gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER);
			comboLabel.setLayoutData(gd);
			comboLabel.setText(TextEditorMessages.SpellingConfigurationBlock_combo_caption);

			label= new Label(composite, SWT.CENTER);
			gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(gd);

			fProviderCombo= new Combo(fComboGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd= new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
			fProviderCombo.setLayoutData(gd);

			fProviderViewer= createProviderViewer();
		}

		Composite groupComp= new Composite(composite, SWT.NONE);
		gd= new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan= 2;
		groupComp.setLayoutData(gd);
		GridLayout gridLayout= new GridLayout(1, false);
		gridLayout.marginWidth= 0;
		groupComp.setLayout(gridLayout);

		/* contributed provider preferences. */
		fGroup= new Composite(groupComp, SWT.NONE);
		gd= new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent= 10;
		fGroup.setLayoutData(gd);
		fStackLayout= new StackLayout();
		fGroup.setLayout(fStackLayout);

		return composite;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#applyData(java.lang.Object)
	 * @since 3.4
	 */
	public void applyData(Object data) {
	}

	private ComboViewer createProviderViewer() {
		/* list viewer */
		final ComboViewer viewer= new ComboViewer(fProviderCombo);
		viewer.setContentProvider(new IStructuredContentProvider() {

			/*
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
			}

			/*
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			}

			/*
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return fProviderDescriptors.values().toArray();
			}
		});
		viewer.setLabelProvider(new LabelProvider() {
			/*
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object element) {
				return null;
			}

			/*
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((SpellingEngineDescriptor) element).getLabel();
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel= (IStructuredSelection) event.getSelection();
				if (sel.isEmpty())
					return;
				if (fCurrentBlock != null && fStatusMonitor.getStatus() != null && fStatusMonitor.getStatus().matches(IStatus.ERROR))
					if (isPerformRevert()) {
						ISafeRunnable runnable= new ISafeRunnable() {
							public void run() throws Exception {
								fCurrentBlock.performRevert();
							}
							public void handleException(Throwable x) {
							}
						};
						SafeRunner.run(runnable);
					} else {
						revertSelection();
						return;
					}
				fStore.setValue(SpellingService.PREFERENCE_SPELLING_ENGINE, ((SpellingEngineDescriptor) sel.getFirstElement()).getId());
				updateListDependencies();
			}

			private boolean isPerformRevert() {
				Shell shell= viewer.getControl().getShell();
				MessageDialog dialog= new MessageDialog(shell, TextEditorMessages.SpellingConfigurationBlock_error_title, null, TextEditorMessages.SpellingConfigurationBlock_error_message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 1);
				return dialog.open() == 0;
			}

			private void revertSelection() {
				try {
					viewer.removeSelectionChangedListener(this);
					SpellingEngineDescriptor desc= EditorsUI.getSpellingService().getActiveSpellingEngineDescriptor(fStore);
					if (desc != null)
						viewer.setSelection(new StructuredSelection(desc), true);
				} finally {
					viewer.addSelectionChangedListener(this);
				}
			}
		});
		viewer.setInput(fProviderDescriptors);
		viewer.refresh();

		return viewer;
	}

	private void updateCheckboxDependencies() {
		final boolean enabled= fEnablementCheckbox.getSelection();
		if (fComboGroup != null)
			setEnabled(fComboGroup, enabled);
		SpellingEngineDescriptor desc= EditorsUI.getSpellingService().getActiveSpellingEngineDescriptor(fStore);
		String id= desc != null ? desc.getId() : ""; //$NON-NLS-1$
		final ISpellingPreferenceBlock preferenceBlock= (ISpellingPreferenceBlock) fProviderPreferences.get(id);
		if (preferenceBlock != null) {
			ISafeRunnable runnable= new ISafeRunnable() {
				public void run() throws Exception {
					preferenceBlock.setEnabled(enabled);
				}
				public void handleException(Throwable x) {
				}
			};
			SafeRunner.run(runnable);
		}
	}

	private void setEnabled(Control control, boolean enabled) {
		if (control instanceof Composite) {
			Control[] children= ((Composite) control).getChildren();
			for (int i= 0; i < children.length; i++)
				setEnabled(children[i], enabled);
		}
		control.setEnabled(enabled);
	}

	void updateListDependencies() {
		SpellingEngineDescriptor desc= EditorsUI.getSpellingService().getActiveSpellingEngineDescriptor(fStore);
		String id;
		if (desc == null) {
			// safety in case there is no such descriptor
			id= ""; //$NON-NLS-1$
			String message= TextEditorMessages.SpellingConfigurationBlock_error_not_exist;
			EditorsPlugin.log(new Status(IStatus.WARNING, EditorsUI.PLUGIN_ID, IStatus.OK, message, null));
			fCurrentBlock= new ErrorPreferences(message);
		} else {
			id= desc.getId();
			fCurrentBlock= (ISpellingPreferenceBlock) fProviderPreferences.get(id);
			if (fCurrentBlock == null) {
				try {
					fCurrentBlock= desc.createPreferences();
					fProviderPreferences.put(id, fCurrentBlock);
				} catch (CoreException e) {
					EditorsPlugin.log(e);
					fCurrentBlock= new ErrorPreferences(e.getLocalizedMessage());
				}
			}
		}

		Control control= (Control) fProviderControls.get(id);
		if (control == null) {
			final Control[] result= new Control[1];
			ISafeRunnable runnable= new ISafeRunnable() {
				public void run() throws Exception {
					result[0]= fCurrentBlock.createControl(fGroup);
				}
				public void handleException(Throwable x) {
				}
			};
			SafeRunner.run(runnable);
			control= result[0];
			if (control == null) {
				String message= TextEditorMessages.SpellingConfigurationBlock_info_no_preferences;
				EditorsPlugin.log(new Status(IStatus.WARNING, EditorsUI.PLUGIN_ID, IStatus.OK, message, null));
				control= new ErrorPreferences(message).createControl(fGroup);
			} else {
				fProviderControls.put(id, control);
			}
		}
		Dialog.applyDialogFont(control);
		fStackLayout.topControl= control;
		control.pack();
		fGroup.layout();
		fGroup.getParent().layout();

		fStatusMonitor.statusChanged(new StatusInfo());
		ISafeRunnable runnable= new ISafeRunnable() {
			public void run() throws Exception {
				fCurrentBlock.initialize(fStatusMonitor);
			}
			public void handleException(Throwable x) {
			}
		};
		SafeRunner.run(runnable);
	}

	public void initialize() {
		restoreFromPreferences();
	}

	public boolean canPerformOk() {
		SpellingEngineDescriptor desc= EditorsUI.getSpellingService().getActiveSpellingEngineDescriptor(fStore);
		String id= desc != null ? desc.getId() : ""; //$NON-NLS-1$
		final ISpellingPreferenceBlock block= (ISpellingPreferenceBlock) fProviderPreferences.get(id);
		if (block == null)
			return true;

		final Boolean[] result= new Boolean[] { Boolean.TRUE };
		ISafeRunnable runnable= new ISafeRunnable() {
			public void run() throws Exception {
				result[0]= Boolean.valueOf(block.canPerformOk());
			}
			public void handleException(Throwable x) {
			}
		};
		SafeRunner.run(runnable);
		return result[0].booleanValue();
	}

	public void performOk() {
		for (Iterator it= fProviderPreferences.values().iterator(); it.hasNext();) {
			final ISpellingPreferenceBlock block= (ISpellingPreferenceBlock) it.next();
			ISafeRunnable runnable= new ISafeRunnable() {
				public void run() throws Exception {
					block.performOk();
				}
				public void handleException(Throwable x) {
				}
			};
			SafeRunner.run(runnable);
		}
	}

	public void performDefaults() {
		restoreFromPreferences();
		for (Iterator it= fProviderPreferences.values().iterator(); it.hasNext();) {
			final ISpellingPreferenceBlock block= (ISpellingPreferenceBlock) it.next();
			ISafeRunnable runnable= new ISafeRunnable() {
				public void run() throws Exception {
					block.performDefaults();
				}
				public void handleException(Throwable x) {
				}
			};
			SafeRunner.run(runnable);
		}
	}

	public void dispose() {
		for (Iterator it= fProviderPreferences.values().iterator(); it.hasNext();) {
			final ISpellingPreferenceBlock block= (ISpellingPreferenceBlock) it.next();
			ISafeRunnable runnable= new ISafeRunnable() {
				public void run() throws Exception {
					block.dispose();
				}
				public void handleException(Throwable x) {
				}
			};
			SafeRunner.run(runnable);
		}
	}

	private void restoreFromPreferences() {
		if (fEnablementCheckbox == null)
			return;

		boolean enabled= fStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED);
		fEnablementCheckbox.setSelection(enabled);

		if (fProviderViewer == null)
			updateListDependencies();
		else {
			SpellingEngineDescriptor desc= EditorsUI.getSpellingService().getActiveSpellingEngineDescriptor(fStore);
			if (desc != null)
				fProviderViewer.setSelection(new StructuredSelection(desc), true);
		}

		updateCheckboxDependencies();
	}
}
