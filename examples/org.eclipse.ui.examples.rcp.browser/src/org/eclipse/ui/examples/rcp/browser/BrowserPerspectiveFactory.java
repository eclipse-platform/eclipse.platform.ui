package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The perspective factory for the RCP Browser Example's perspective.
 * 
 * @since 3.0
 */
public class BrowserPerspectiveFactory implements IPerspectiveFactory {

	/**
	 * Constructs a new <code>BrowserPerspectiveFactory</code>.
	 */
	public BrowserPerspectiveFactory() {
		// do nothing
	}

	/**
	 * The <code>BrowserPerspectiveFactory</code> implementation of this
	 * <code>IPerspectiveFactory</code> method creates the initial layout
	 * of the perspective to have a single Browser view and no editor area.
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(BrowserApp.PLUGIN_ID + ".browserView", false, IPageLayout.RIGHT, .5f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		layout.setEditorAreaVisible(false);
	}

}
