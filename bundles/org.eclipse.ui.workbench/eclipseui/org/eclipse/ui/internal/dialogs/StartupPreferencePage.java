/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.dialogs;

import static org.eclipse.ui.internal.WindowsDefenderConfigurator.PREFERENCE_STARTUP_CHECK_SKIP;
import static org.eclipse.ui.internal.WindowsDefenderConfigurator.PREFERENCE_STARTUP_CHECK_SKIP_DEFAULT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.UserScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.GroupFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TableFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WindowsDefenderConfigurator;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.testing.ContributionInfo;
import org.osgi.framework.Constants;

/**
 * The Startup preference page.
 */
public class StartupPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Table pluginsList;

	private Workbench workbench;

	private Map<IScopeContext, Button> windowsDefenderIgnore = Map.of();

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.STARTUP_PREFERENCE_PAGE);

		Composite composite = CompositeFactory.newComposite(SWT.NONE) //
				.layout(GridLayoutFactory.swtDefaults().margins(0, 0).create())
				.layoutData(new GridData(SWT.FILL, SWT.FILL, true, true)) //
				.font(parent.getFont()).create(parent);

		createExtraContent(composite);
		if (composite.getChildren().length > 0) {
			new Label(composite, SWT.NONE); // add spacer
		}
		createEarlyStartupSelection(composite);

		return composite;
	}

	protected void createExtraContent(Composite composite) {
		if (Platform.OS.isWindows()) {
			new Label(composite, SWT.NONE); // add spacer

			GridDataFactory grapHorizontalSpace = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING)
					.grab(true, false);

			Group group = GroupFactory.newGroup(SWT.SHADOW_NONE)
					.text(WorkbenchMessages.WindowsDefenderConfigurator_statusCheck)
					.layoutData(grapHorizontalSpace.create()).create(composite);
			GridLayoutFactory.swtDefaults().spacing(5, 7).applyTo(group);

			String infoText = WindowsDefenderConfigurator
					.bindProductName(WorkbenchMessages.WindowsDefenderConfigurator_exclusionInformation);
			LabelFactory.newLabel(SWT.WRAP).text(infoText).layoutData(grapHorizontalSpace.create()).create(group);

			Button ignoreAllInstall = createCheckBox(WorkbenchMessages.WindowsDefenderConfigurator_ignoreAllChoice,
					false, group);

			windowsDefenderIgnore = Map.of(UserScope.INSTANCE, ignoreAllInstall);

			ButtonFactory.newButton(SWT.PUSH)
					.text(WorkbenchMessages.WindowsDefenderConfigurator_runExclusionFromPreferenceButtonLabel)
					.font(composite.getFont()).onSelect(e -> {
						Shell shell = getShell();
						try {
							Boolean excluded = WindowsDefenderConfigurator.runCheckEnforced(null);
							if (excluded == Boolean.FALSE) {
								// Show a dialog that Defender is inactive to give some kind of feedback
								MessageDialog.open(MessageDialog.INFORMATION, shell,
										WorkbenchMessages.WindowsDefenderConfigurator_statusCheck,
										WorkbenchMessages.WindowsDefenderConfigurator_statusInactive, SWT.NONE);
							}
						} catch (CoreException ex) {
							ErrorDialog.openError(shell, "Windows Defender exclusion check failed", //$NON-NLS-1$
									"An unexpected error occured while running the Windows Defender exclusion check.", //$NON-NLS-1$
									ex.getStatus());
							WorkbenchPlugin.log("Error while running the Windows Defender exclusion check", ex); //$NON-NLS-1$
						}
					}).create(group);

			LabelFactory.newLabel(SWT.WRAP).text(WorkbenchMessages.WindowsDefenderConfigurator_scriptHint)
					.layoutData(grapHorizontalSpace.create()).create(group);

			Button showScriptButton = WidgetFactory.button(SWT.PUSH).create(group);

			// Use back-tick characters to split a command over multiple lines:
			// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_parsing?view=powershell-7.4#line-continuation
			Text scriptText = WidgetFactory.text(SWT.BORDER | SWT.H_SCROLL | SWT.READ_ONLY)
					.text(WindowsDefenderConfigurator.createAddExclusionsPowershellCommand(" `\n ")) //$NON-NLS-1$
					.layoutData(grapHorizontalSpace.create()).create(group);

			setScriptBlockVisible(scriptText, showScriptButton, false);
			showScriptButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
					e -> setScriptBlockVisible(scriptText, showScriptButton, !scriptText.getVisible())));

			updateWindowsDefenderHandlingOptions();
		}
	}

	private void setScriptBlockVisible(Text text, Button button, boolean visible) {
		button.setText(!visible ? WorkbenchMessages.WindowsDefenderConfigurator_scriptShowLabel
				: WorkbenchMessages.WindowsDefenderConfigurator_scriptHideLabel);
		text.setVisible(visible);
		((GridData) text.getLayoutData()).exclude = !visible;
		text.requestLayout();
	}

	protected static Button createCheckBox(String text, boolean checked, Composite composite) {
		Button button = ButtonFactory.newButton(SWT.CHECK).text(text).font(composite.getFont()).create(composite);
		button.setSelection(checked);
		return button;
	}

	protected void createEarlyStartupSelection(Composite parent) {
		LabelFactory.newLabel(SWT.NONE).text(WorkbenchMessages.StartupPreferencePage_label) //
				.layoutData(new GridData(SWT.FILL, SWT.CENTER, true, false)) //
				.font(parent.getFont()).create(parent);
		pluginsList = TableFactory.newTable(SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL)
				.layoutData(new GridData(SWT.FILL, SWT.FILL, true, true)) //
				.font(parent.getFont()).create(parent);
		TableViewer viewer = new TableViewer(pluginsList);
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return Platform.getBundle(((ContributionInfo) element).getBundleId()).getHeaders()
						.get(Constants.BUNDLE_NAME);
			}
		});
		viewer.setComparator(new ViewerComparator());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(workbench.getEarlyActivatedPlugins());
		updateCheckState();
	}

	private void updateCheckState() {
		Set<String> disabledPlugins = new HashSet<>(Arrays.asList(workbench.getDisabledEarlyActivatedPlugins()));
		for (int i = 0; i < pluginsList.getItemCount(); i++) {
			TableItem item = pluginsList.getItem(i);
			String pluginId = ((ContributionInfo) item.getData()).getBundleId();
			item.setChecked(!disabledPlugins.contains(pluginId));
		}
	}

	private void updateWindowsDefenderHandlingOptions() {
		windowsDefenderIgnore.forEach((scope, button) -> {
			IEclipsePreferences node = WindowsDefenderConfigurator.getPreference(scope);
			boolean ignore = node.getBoolean(PREFERENCE_STARTUP_CHECK_SKIP, PREFERENCE_STARTUP_CHECK_SKIP_DEFAULT);
			button.setSelection(ignore);
		});
	}

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = (Workbench) workbench;
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
		store.setToDefault(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
		updateCheckState();
		windowsDefenderIgnore.values().forEach(b -> b.setSelection(PREFERENCE_STARTUP_CHECK_SKIP_DEFAULT));
		updateWindowsDefenderHandlingOptions();
	}

	@Override
	public boolean performOk() {
		StringBuilder preference = new StringBuilder();
		TableItem[] items = pluginsList.getItems();
		for (TableItem item : items) {
			if (!item.getChecked()) {
				preference.append(((ContributionInfo) item.getData()).getBundleId());
				preference.append(IPreferenceConstants.SEPARATOR);
			}
		}
		String pref = preference.toString();
		IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
		store.setValue(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP, pref);
		PrefUtil.savePrefs();

		windowsDefenderIgnore.forEach((scope, button) -> {
			try {
				// If disabled remove the node to allow higher-level scopes to be considered
				String skip = button.getSelection() ? Boolean.TRUE.toString() : null;
				WindowsDefenderConfigurator.savePreference(scope, PREFERENCE_STARTUP_CHECK_SKIP, skip);
			} catch (CoreException e) {
				WorkbenchPlugin.log("Failed to save Windows Defender exclusion check preferences", e); //$NON-NLS-1$
			}
		});
		return true;
	}
}
