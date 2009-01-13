package org.eclipse.e4.ui.examples.css.rcp.presentation;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultMultiTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultSimpleTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultThemeListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.EmptyTabFolder;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.StandardEditorSystemMenu;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * 
 * @since 3.4
 * 
 */
public class CSSPresentationFactory extends
		WorkbenchPresentationFactory {

	public static String ID = "org.eclipse.e4.ui.examples.css.rcp.presentationFactory";
	
	// don't reset these dynamically, so just keep the information static.
	// see bug:
	// 75422 [Presentations] Switching presentation to R21 switches immediately,
	// but only partially
	private static int editorTabPosition = PlatformUI.getPreferenceStore()
			.getInt(IWorkbenchPreferenceConstants.EDITOR_TAB_POSITION);
	private static int viewTabPosition = PlatformUI.getPreferenceStore()
			.getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createEditorPresentation(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.presentations.IStackPresentationSite)
	 */
	public StackPresentation createEditorPresentation(Composite parent,
			IStackPresentationSite site) {
		CSSTabFolder folder = new CSSTabFolder(parent,
				editorTabPosition | SWT.BORDER, site
						.supportsState(IStackPresentationSite.STATE_MINIMIZED),
				site.supportsState(IStackPresentationSite.STATE_MAXIMIZED));

		/*
		 * Set the minimum characters to display, if the preference is something
		 * other than the default. This is mainly intended for RCP applications
		 * or for expert users (i.e., via the plug-in customization file).
		 * 
		 * Bug 32789.
		 */
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		if (store
				.contains(IWorkbenchPreferenceConstants.EDITOR_MINIMUM_CHARACTERS)) {
			final int minimumCharacters = store
					.getInt(IWorkbenchPreferenceConstants.EDITOR_MINIMUM_CHARACTERS);
			if (minimumCharacters >= 0) {
				folder.setMinimumCharacters(minimumCharacters);
			}
		}

		PresentablePartFolder partFolder = new PresentablePartFolder(folder);

		TabbedStackPresentation result = new TabbedStackPresentation(site,
				partFolder, new StandardEditorSystemMenu(site));

		DefaultThemeListener themeListener = new DefaultThemeListener(folder,
				result.getTheme());
		result.getTheme().addListener(themeListener);

		new DefaultMultiTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS, folder);

		new DefaultSimpleTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				folder);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createViewPresentation(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.presentations.IStackPresentationSite)
	 */
	public StackPresentation createViewPresentation(Composite parent,
			IStackPresentationSite site) {

		CSSTabFolder folder = new CSSTabFolder(parent, viewTabPosition
				| SWT.BORDER, site
				.supportsState(IStackPresentationSite.STATE_MINIMIZED), site
				.supportsState(IStackPresentationSite.STATE_MAXIMIZED));

		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final int minimumCharacters = store
				.getInt(IWorkbenchPreferenceConstants.VIEW_MINIMUM_CHARACTERS);
		if (minimumCharacters >= 0) {
			folder.setMinimumCharacters(minimumCharacters);
		}

		PresentablePartFolder partFolder = new PresentablePartFolder(folder);

		folder.setUnselectedCloseVisible(false);
		folder.setUnselectedImageVisible(true);

		TabbedStackPresentation result = new TabbedStackPresentation(site,
				partFolder, new StandardViewSystemMenu(site));

		DefaultThemeListener themeListener = new DefaultThemeListener(folder,
				result.getTheme());
		result.getTheme().addListener(themeListener);

		new DefaultSimpleTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				folder);

		return result;
	}


}
