/*******************************************************************************
 * Copyright (c) 2021 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * <p>
 * Parses the preference that specifies which editor should be used when opening
 * a large file.
 * </p>
 *
 * See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=577289
 */
public class LargeFileLimitsPreferenceHandler {

	/**
	 * An editor and file size pair.
	 */
	public static class FileLimit {
		public final String editorId;
		public final long fileSize;

		/**
		 * @param editorId the ID of the editor that should be used for files above the
		 *                 specified size
		 * @param fileSize the size in bytes
		 */
		public FileLimit(String editorId, long fileSize) {
			this.editorId = editorId;
			this.fileSize = fileSize;
		}
	}

	/**
	 * Value of the preference that indicates a dialog should prompt the user to
	 * choose an editor, with which to open the large document.
	 */
	public static final String PROMPT_EDITOR_PREFERENCE_VALUE = IPreferenceConstants.LARGE_FILE_LIMITS + "_prompt"; //$NON-NLS-1$

	private static final IPreferenceStore PREFERENCE_STORE = PrefUtil.getInternalPreferenceStore();

	private static final String DISABLED_EXTENSIONS_KEY = IPreferenceConstants.LARGE_FILE_LIMITS + "_disabled"; //$NON-NLS-1$
	private static final String CONFIGURED_EXTENSIONS_KEY = IPreferenceConstants.LARGE_FILE_LIMITS + "_types"; //$NON-NLS-1$
	private static final String DEFAULT_VALUE_KEY = IPreferenceConstants.LARGE_FILE_LIMITS + "_default_value"; //$NON-NLS-1$
	private static final String DEFAULT_VALUE_ENABLED_KEY = IPreferenceConstants.LARGE_FILE_LIMITS + "_default_enabled"; //$NON-NLS-1$

	private static final String EXTENSION_SEPARATOR = "."; //$NON-NLS-1$
	private static final String PREFERENCE_EXTENSIONS_SEPARATOR = ","; //$NON-NLS-1$
	private static final String PREFERENCE_VALUE_SEPARATOR = ","; //$NON-NLS-1$

	private static final String EMPTY_VALUES = ""; //$NON-NLS-1$

	private static final String LARGE_FILE_ASSOCIATIONS_PREFERENCE_PAGE_ID = "org.eclipse.ui.preferencePages.LargeFileAssociations"; //$NON-NLS-1$

	private static final boolean DEFAULT_REMEMBER_EDITOR_SELECTION = false;

	private final Map<String, List<FileLimit>> preferencesCache;
	private final IPropertyChangeListener preferencesListener;

	private long legacyMaxFileSize = 0;
	private boolean legacyCheckDocumentSize;

	LargeFileLimitsPreferenceHandler() {
		initLegacyPreference();
		preferencesCache = new HashMap<>();
		preferencesListener = e -> {
			String property = e.getProperty();
			// invalidate the cache on any related preferences change
			if (property.startsWith(IPreferenceConstants.LARGE_FILE_LIMITS)) {
				preferencesCache.clear();
			}
		};
		PREFERENCE_STORE.addPropertyChangeListener(preferencesListener);
	}

	private void initLegacyPreference() {
		legacyMaxFileSize = getLargeDocumentLegacyPreferenceValue();
		legacyCheckDocumentSize = legacyMaxFileSize != 0;
	}

	public void dispose() {
		PREFERENCE_STORE.removePropertyChangeListener(preferencesListener);
	}

	public static void setDefaults() {
		PREFERENCE_STORE.setDefault(DEFAULT_VALUE_KEY, 8 * 1024 * 1024);
		PREFERENCE_STORE.setDefault(DEFAULT_VALUE_ENABLED_KEY, false);
	}

	public static void restoreDefaults() {
		PREFERENCE_STORE.setToDefault(DEFAULT_VALUE_KEY);
		PREFERENCE_STORE.setToDefault(DEFAULT_VALUE_ENABLED_KEY);
		List<String> extensions = new ArrayList<>();
		Collections.addAll(extensions, getDisabledExtensionTypes());
		Collections.addAll(extensions, getConfiguredExtensionTypes());
		extensions.forEach(e -> PREFERENCE_STORE.setToDefault(getPreferenceNameForExtension(e)));
		PREFERENCE_STORE.setToDefault(DISABLED_EXTENSIONS_KEY);
		PREFERENCE_STORE.setToDefault(CONFIGURED_EXTENSIONS_KEY);
	}

	public static boolean isLargeDocumentLegacyPreferenceSet() {
		long legacyPreferenceValue = getLargeDocumentLegacyPreferenceValue();
		return legacyPreferenceValue > 0;
	}

	public static long getDefaultLimit() {
		return PREFERENCE_STORE.getLong(DEFAULT_VALUE_KEY);
	}

	public static boolean isDefaultLimitEnabled() {
		return PREFERENCE_STORE.getBoolean(DEFAULT_VALUE_ENABLED_KEY);
	}

	public static void setDefaultLimit(long fileSize) {
		PREFERENCE_STORE.setValue(DEFAULT_VALUE_KEY, fileSize);
		PREFERENCE_STORE.setValue(DEFAULT_VALUE_ENABLED_KEY, true);
	}

	public static void disableDefaultLimit() {
		PREFERENCE_STORE.setValue(DEFAULT_VALUE_ENABLED_KEY, false);
	}

	public static String[] getConfiguredExtensionTypes() {
		String[] extensions = getExtensionsPreferenceValue(CONFIGURED_EXTENSIONS_KEY);
		return extensions;
	}


	public static void setConfiguredExtensionTypes(String[] extensionTypes) {
		setExtensionsPreferenceValue(CONFIGURED_EXTENSIONS_KEY, extensionTypes);
	}

	public static String[] getDisabledExtensionTypes() {
		String[] extensions = getExtensionsPreferenceValue(DISABLED_EXTENSIONS_KEY);
		return extensions;
	}

	public static void setDisabledExtensionTypes(String[] extensionTypes) {
		setExtensionsPreferenceValue(DISABLED_EXTENSIONS_KEY, extensionTypes);
	}

	public static List<FileLimit> getFileLimitsForExtension(String fileExtension) {
		String preferenceName = getPreferenceNameForExtension(fileExtension);
		List<FileLimit> preferenceValues = getPreferenceValues(preferenceName);
		return preferenceValues;
	}

	public static void removeFileLimitsForExtension(String fileExtension) {
		String preferenceName = getPreferenceNameForExtension(fileExtension);
		PREFERENCE_STORE.setValue(preferenceName, EMPTY_VALUES);
	}

	public static void setFileLimitsForExtension(String fileExtension, List<FileLimit> fileLimits) {
		String preferenceName = getPreferenceNameForExtension(fileExtension);
		setPreferenceValues(preferenceName, fileLimits);
	}

	public static boolean isPromptPreferenceValue(String editorId) {
		boolean isPromptPreferenceValue = PROMPT_EDITOR_PREFERENCE_VALUE.equals(editorId);
		return isPromptPreferenceValue;
	}

	/**
	 * @param editorInput the input for which to check
	 * @return {@code null} if the user was prompted and didn't select an editor,
	 *         empty optional if no preference is set to indicate an editor for
	 *         large documents, or the editor ID specified by the preference or user
	 *         for the given document type
	 */
	Optional<String> getEditorForInput(IEditorInput editorInput) {
		if (editorInput instanceof IPathEditorInput) {
			IPathEditorInput pathEditorInput = (IPathEditorInput) editorInput;
			try {
				IPath inputPath = pathEditorInput.getPath();
				return getEditorForPath(inputPath);
			} catch (Exception e) {
				// Path does not exist?
				Status warning = Status
						.warning("Exception occurred while checking large file editor for " + editorInput, e); //$NON-NLS-1$
				WorkbenchPlugin.log(warning);
			}
		}
		return Optional.empty();
	}

	private Optional<String> getEditorForPath(IPath inputPath) {
		boolean legacyPreferenceApplies = isLargeDocumentFromLegacy(inputPath);
		if (legacyPreferenceApplies) {
			IEditorDescriptor editor = getLegacyAlternateEditor();
			if (editor == null) {
				// the user pressed cancel in the editor selection dialog, indicate no editor
				// should be open
				return null;
			}
			String editorId = editor.getId();
			return Optional.of(editorId);
		}
		String editorId = null;
		boolean isPromptPreferenceValue = false;
		FileLimit fileLimit = getLimitForLargeFile(inputPath);
		if (fileLimit != null) {
			editorId = fileLimit.editorId;
			isPromptPreferenceValue = isPromptPreferenceValue(fileLimit.editorId);
		}
		if (isPromptPreferenceValue) {
			IEditorDescriptor editor = null;
			Shell shell = ProgressManagerUtil.getDefaultParent();
			LargeFileEditorSelectionDialog dialog = new LargeFileEditorSelectionDialog(shell, inputPath.getFileExtension(), fileLimit.fileSize);
			dialog.setMessage(WorkbenchMessages.EditorManager_largeDocumentWarning);
			if (dialog.open() == Window.OK) {
				editor = dialog.getSelectedEditor();
			}
			if (editor == null) {
				// the user pressed cancel in the editor selection dialog, indicate no editor
				// should be open
				return null;
			}
			editorId = editor.getId();
			boolean rememberSelectedEditor = dialog.shouldRememberSelectedEditor();
			if (editorId != null && rememberSelectedEditor) {
				FileLimit newLimit = new FileLimit(editorId, fileLimit.fileSize);
				replaceLimitForLargeFile(inputPath, fileLimit, newLimit);

				addedConfiguredExtensionType(inputPath);
			}
		}
		if (editorId != null && !editorId.isEmpty()) {
			return Optional.of(editorId);
		}
		return Optional.empty();
	}

	private static IEditorDescriptor getLegacyAlternateEditor() {
		Shell shell = ProgressManagerUtil.getDefaultParent();
		EditorSelectionDialog dialog = new EditorSelectionDialog(shell) {
			@Override
			protected IDialogSettings getDialogSettings() {
				IDialogSettings result = new DialogSettings("EditorSelectionDialog"); //$NON-NLS-1$
				result.put(EditorSelectionDialog.STORE_ID_INTERNAL_EXTERNAL, true);
				return result;
			}
		};
		dialog.setMessage(WorkbenchMessages.EditorManager_largeDocumentWarning);

		if (dialog.open() == Window.OK) {
			return dialog.getSelectedEditor();
		}
		return null;
	}

	/**
	 * Determines if an editor chooser dialog should be shown when opening large
	 * files, using the "old" preference:
	 * {@link IPreferenceConstants#LARGE_DOC_SIZE_FOR_EDITORS}
	 */
	boolean isLargeDocumentFromLegacy(IPath path) {
		if (!legacyCheckDocumentSize)
			return false;

		try {
			File file = new File(path.toOSString());
			return file.length() > legacyMaxFileSize;
		} catch (Exception e) {
			// ignore exceptions
			return false;
		}
	}

	FileLimit getLimitForLargeFile(IPath path) {
		FileLimit applicableFileLimit = null;

		try {
			List<FileLimit> fileLimits = getFileLimits(path, preferencesCache);

			if (!fileLimits.isEmpty()) {
				File file = new File(path.toOSString());
				long fileSize = file.length();

				long maxBound = 0;
				for (FileLimit fileLimit : fileLimits) {
					long limit = fileLimit.fileSize;
					if (fileSize > limit && limit > maxBound) {
						maxBound = limit;
						applicableFileLimit = fileLimit;
					}
				}
			}
		} catch (Exception e) {
			WorkbenchPlugin.log("Exception occurred while checking large file editor for path " + path, e); //$NON-NLS-1$
		}
		return applicableFileLimit;
	}

	private void replaceLimitForLargeFile(IPath path, FileLimit oldLimit, FileLimit newLimit) {
		try {
			List<FileLimit> fileLimits = getFileLimits(path, preferencesCache);
			List<FileLimit> newLimits = new ArrayList<>();
			for (FileLimit fileLimit : fileLimits) {
				boolean toReplace = equals(oldLimit, fileLimit);
				if (!toReplace) {
					newLimits.add(fileLimit);
				}
			}
			newLimits.add(newLimit);
			setFileLimitsForPath(path, newLimits);
		} catch (Exception e) {
			WorkbenchPlugin.log("Exception occurred while replacing large file editor preference for path " + path, e); //$NON-NLS-1$
		}
	}

	private static void addedConfiguredExtensionType(IPath inputPath) {
		String extension = inputPath.getFileExtension();
		String[] configuredExtensionTypes = getConfiguredExtensionTypes();
		List<String> newExtensionTypes = new ArrayList<>();
		newExtensionTypes.addAll(Arrays.asList(configuredExtensionTypes));
		if (!newExtensionTypes.contains(extension)) {
			newExtensionTypes.add(extension);
			setConfiguredExtensionTypes(newExtensionTypes.toArray(String[]::new));
		}
	}

	/**
	 * Checks whether a large document preference applies for the specified path.
	 * The preference is formatted as follows:
	 *
	 * <pre>
	 * # all types with a preference
	 * org.eclipse.ui.workbench/largeFileLimits_types=java,xml,txt
	 * # types for which the default is disabled
	 * org.eclipse.ui.workbench/largeFileLimits_disabled=cpp
	 *
	 * # default, show prompt for all types that don't have a preference
	 * org.eclipse.ui.workbench/largeFileLimits_DEFAULT=100000
	 *
	 * org.eclipse.ui.workbench/largeFileLimits.java=33333|org.eclipse.ui.DefaultTextEditor|66666|largeFileLimits_prompt
	 * org.eclipse.ui.workbench/largeFileLimits.xml=44444|org.eclipse.ui.DefaultTextEditor|77777|emacs
	 * org.eclipse.ui.workbench/largeFileLimits.txt=88888|largeFileLimits_prompt
	 * </pre>
	 *
	 * @param preferencesCache cache that stores already retrieved values
	 * @param path             the path of the document, used to determine the file
	 *                         type
	 * @return the values of the preference that applies for the specified path, or
	 *         an empty string if there is no such preference
	 */
	private static List<FileLimit> getFileLimits(IPath path, Map<String, List<FileLimit>> preferencesCache) {
		String fileExtension = path.getFileExtension();
		List<FileLimit> preferenceValues = new ArrayList<>();
		if (fileExtension != null) {
			preferenceValues = preferencesCache.get(fileExtension);
			if (preferenceValues == null) {
				preferenceValues = getLargeFilePreferenceValues(fileExtension);
				preferencesCache.put(fileExtension, preferenceValues);
			}
		}
		return preferenceValues;
	}

	private static List<FileLimit> getLargeFilePreferenceValues(String fileExtension) {
		List<FileLimit> preferenceValues = new ArrayList<>();
		String[] disabled = getDisabledExtensionTypes();
		boolean isDisabled = Arrays.asList(disabled).contains(fileExtension);
		if (!isDisabled) {
			String preferenceName = getPreferenceNameForExtension(fileExtension);
			String largeFilePreference = PREFERENCE_STORE.getString(preferenceName);

			// if no preference exists for the specific file type, check for the default
			if (largeFilePreference == null || largeFilePreference.isEmpty()) {
				long defaultLimit = getDefaultLimit();
				preferenceValues.add(new FileLimit(PROMPT_EDITOR_PREFERENCE_VALUE, defaultLimit));
			} else {
				preferenceValues = getPreferenceValues(preferenceName);
			}
		}

		return preferenceValues;
	}

	private static long getLargeDocumentLegacyPreferenceValue() {
		return PREFERENCE_STORE.getLong(IPreferenceConstants.LARGE_DOC_SIZE_FOR_EDITORS);
	}

	private static boolean equals(FileLimit l1, FileLimit l2) {
		return l1.fileSize == l2.fileSize && l1.editorId.equals(l2.editorId);
	}

	private static String[] getExtensionsPreferenceValue(String preferenceName) {
		String[] extensions = new String[0];
		String extensionTypes = PREFERENCE_STORE.getString(preferenceName);
		if (extensionTypes != null && !extensionTypes.isEmpty()) {
			extensions = extensionTypes.split(PREFERENCE_EXTENSIONS_SEPARATOR);
		}
		return extensions;
	}

	private static void setExtensionsPreferenceValue(String preferenceName, String[] extensionTypes) {
		String preferenceValue = EMPTY_VALUES;
		if (extensionTypes.length > 0) {
			preferenceValue = String.join(PREFERENCE_EXTENSIONS_SEPARATOR, extensionTypes);
		}
		PREFERENCE_STORE.setValue(preferenceName, preferenceValue);
	}

	private static void setFileLimitsForPath(IPath path, List<FileLimit> fileLimits) {
		String fileExtension = path.getFileExtension();
		setFileLimitsForExtension(fileExtension, fileLimits);
	}

	private static List<FileLimit> getPreferenceValues(String preferenceName) {
		String largeFilePreference = PREFERENCE_STORE.getString(preferenceName);
		List<FileLimit> preferenceValues = new ArrayList<>();
		if (largeFilePreference != null && !largeFilePreference.isEmpty()) {
			String[] values = splitPreferenceValues(preferenceName, largeFilePreference);
			preferenceValues = parsePreferenceValues(preferenceName, values);
		}
		return Collections.unmodifiableList(preferenceValues);
	}

	private static String[] splitPreferenceValues(String preferenceName, String preferenceValue) {
		String[] values = preferenceValue.split(PREFERENCE_VALUE_SEPARATOR);
		if (values.length % 2 != 0) {
			String errorMessage = NLS.bind(
					"Expected pairs of values separated by \"{0}\" for preference \"{1}\" but got: \"{2}\"", //$NON-NLS-1$
					new String[] { PREFERENCE_VALUE_SEPARATOR, preferenceName, Arrays.toString(values) });
			WorkbenchPlugin.log(new IllegalArgumentException(errorMessage));
			values = new String[0];
		}
		return values;
	}

	private static List<FileLimit> parsePreferenceValues(String preferenceName, String[] preferenceValues) {
		List<FileLimit> fileLimits = new ArrayList<>();
		for (int i = 0; i < preferenceValues.length; i += 2) {
			String sizeString = preferenceValues[i + 0];
			String editorId = preferenceValues[i + 1];
			try {
				long bytes = Long.parseLong(sizeString);
				FileLimit fileLimit = new FileLimit(editorId, bytes);
				fileLimits.add(fileLimit);
			} catch (NumberFormatException e) {
				String errorMessage = NLS.bind(
						"Skipped invalid file size value \"{0}\" stored in preference \"{1}\" with value \"{2}\"", //$NON-NLS-1$
						new String[] { sizeString, preferenceName, Arrays.toString(preferenceValues) });
				WorkbenchPlugin.log(new IllegalArgumentException(errorMessage, e));
			}
		}
		return fileLimits;
	}

	private static void setPreferenceValues(String preferenceName, List<FileLimit> fileLimits) {
		StringBuilder preferenceValue = new StringBuilder();
		for (int i = 0; i < fileLimits.size(); ++i) {
			FileLimit fileLimit = fileLimits.get(i);
			preferenceValue.append(fileLimit.fileSize);
			preferenceValue.append(PREFERENCE_VALUE_SEPARATOR);
			preferenceValue.append(fileLimit.editorId);
			if (i < fileLimits.size() - 1) {
				preferenceValue.append(PREFERENCE_VALUE_SEPARATOR);
			}
		}
		PREFERENCE_STORE.setValue(preferenceName, preferenceValue.toString());
	}

	private static String getPreferenceNameForExtension(String fileExtension) {
		String preferenceName = IPreferenceConstants.LARGE_FILE_LIMITS + EXTENSION_SEPARATOR + fileExtension;
		return preferenceName;
	}

	private static void openPreferencePage(Shell shell) {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,
				LARGE_FILE_ASSOCIATIONS_PREFERENCE_PAGE_ID, null, null);
		dialog.open();
	}

	private static class LargeFileEditorSelectionDialog extends EditorSelectionDialog {

		private final String extension;
		private final long fileSize;

		private Button rememberSelectionButton;
		private boolean rememberSelection = DEFAULT_REMEMBER_EDITOR_SELECTION;

		LargeFileEditorSelectionDialog(Shell shell, String extension, long size) {
			super(shell);
			this.extension = extension;
			this.fileSize = size;
			rememberSelection = false;
		}

		@Override
		protected IDialogSettings getDialogSettings() {
			IDialogSettings result = new DialogSettings("LargeFileEditorSelectionDialog"); //$NON-NLS-1$
			result.put(EditorSelectionDialog.STORE_ID_INTERNAL_EXTERNAL, true);
			return result;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite contents = (Composite) super.createDialogArea(parent);

			Composite preferenceGroup = new Composite(contents, SWT.NONE);
			preferenceGroup.setLayout(new FillLayout(SWT.VERTICAL));

			rememberSelectionButton = new Button(preferenceGroup, SWT.CHECK);
			String buttonText = NLS.bind(WorkbenchMessages.LargeFileAssociation_Dialog_rememberSelectedEditor,
					extension, fileSize);
			rememberSelectionButton.setText(buttonText);
			rememberSelectionButton.setSelection(rememberSelection);
			rememberSelectionButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					rememberSelection = rememberSelectionButton.getSelection();
				}
			});

			Shell shell = parent.getShell();
			Link preferencePageLink = new Link(preferenceGroup, SWT.NONE);
			preferencePageLink.setText(WorkbenchMessages.LargeFileAssociation_Dialog_configureFileAssociationsLink);
			preferencePageLink.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openPreferencePage(shell);
				}
			});

			return contents;
		}

		boolean shouldRememberSelectedEditor() {
			return rememberSelection;
		}
	}
}
