package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * ContentsContribution is the class that defines the content selection
 * contribution for the {@link ExtendedMarkersView}.
 * 
 * @since 3.4
 * 
 */
public class ContentsContribution extends CompoundContributionItem {

	/**
	 * Create a new instance of the receiver.
	 */
	public ContentsContribution() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {

		MarkerContentGenerator[] generators = MarkerSupportRegistry
				.getInstance().getGenerators();
		IContributionItem[] items = new IContributionItem[generators.length];
		for (int i = 0; i < generators.length; i++) {
			final MarkerContentGenerator generator = generators[i];
			items[i] = new ContributionItem() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
				 *      int)
				 */
				public void fill(Menu menu, int index) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(generator.getName());
					item.addListener(SWT.Selection,
							getMenuItemListener(generator));
				}

				private Listener getMenuItemListener(
						final MarkerContentGenerator generator) {
					return new Listener() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
						 */
						public void handleEvent(Event event) {
							IWorkbenchWindow active = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow();
							if (active == null)
								return;
							IWorkbenchPage page = active.getActivePage();
							if (page == null)
								return;
							IWorkbenchPart part = page.getActivePart();
							if (part == null)
								return;
							((ExtendedMarkersView) part)
									.setContentGenerator(generator);
						}
					};
				}
			};
		}
		return items;
	}

}
