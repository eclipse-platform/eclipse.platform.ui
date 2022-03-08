package org.eclipse.ui.tests.markers;

import static org.eclipse.ui.views.markers.internal.MarkerMessages.filtersDialog_anyResource;
import static org.eclipse.ui.views.markers.internal.MarkerMessages.filtersDialog_anyResourceInSameProject;
import static org.eclipse.ui.views.markers.internal.MarkerMessages.filtersDialog_selectedAndChildren;
import static org.eclipse.ui.views.markers.internal.MarkerMessages.filtersDialog_selectedResource;
import static org.eclipse.ui.views.markers.internal.MarkerMessages.filtersDialog_workingSet;
import static org.eclipse.ui.views.markers.internal.MarkerMessages.filtersDialog_workingSetSelect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.FiltersConfigurationDialog;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.tests.NoScopeConfigTestView;
import org.eclipse.ui.tests.RemoveScopeButtonsTestView;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.junit.After;
import org.junit.Test;

/**
 * @since 3.18
 *
 */
public class ScopeAreaTests {

	FiltersConfigurationDialog dialog;

	@After
	public void cleanup() {
		if (dialog != null) {
			dialog.close();
		}
	}

	Composite showFilterDialog(MarkerSupportView view) throws Exception {

		MarkerContentGenerator generator = getMarkerContentGenerator(view);

		Composite[] composite = new Composite[1];
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new FiltersConfigurationDialog(shell, generator) {
			@Override
			protected Control createDialogArea(Composite parent) {
				composite[0] = (Composite) super.createDialogArea(parent);
				return composite[0];
			}
		};

		dialog.setBlockOnOpen(false);
		dialog.open();

		return composite[0];
	}

	@Test
	public void canRemoveScopeAreaButtons() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(RemoveScopeButtonsTestView.ID);

		Composite composite = showFilterDialog(view);

		assertTrue(filtersDialog_anyResource, isButtonAvailable(composite, filtersDialog_anyResource));
		assertFalse(filtersDialog_anyResourceInSameProject,
				isButtonAvailable(composite, filtersDialog_anyResourceInSameProject));
		assertFalse(filtersDialog_selectedResource, isButtonAvailable(composite, filtersDialog_selectedResource));
		assertTrue(filtersDialog_selectedAndChildren, isButtonAvailable(composite, filtersDialog_selectedAndChildren));
		assertFalse(filtersDialog_workingSet, isButtonAvailable(composite, filtersDialog_workingSet));
		assertFalse(filtersDialog_workingSetSelect, isButtonAvailable(composite, filtersDialog_workingSetSelect));

	}

	@Test
	public void scopeShowsWithNoScopeAreaConfig() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(NoScopeConfigTestView.ID);

		Composite composite = showFilterDialog(view);

		assertTrue(filtersDialog_anyResource, isButtonAvailable(composite, filtersDialog_anyResource));
		assertTrue(filtersDialog_anyResourceInSameProject,
				isButtonAvailable(composite, filtersDialog_anyResourceInSameProject));
		assertTrue(filtersDialog_selectedResource, isButtonAvailable(composite, filtersDialog_selectedResource));
		assertTrue(filtersDialog_selectedAndChildren, isButtonAvailable(composite, filtersDialog_selectedAndChildren));

		assertTrue(MarkerMessages.filtersDialog_noWorkingSet,
				isButtonAvailable(composite, MarkerMessages.filtersDialog_noWorkingSet));
		assertTrue(filtersDialog_workingSetSelect, isButtonAvailable(composite, filtersDialog_workingSetSelect));
	}

	public static MarkerContentGenerator getMarkerContentGenerator(MarkerSupportView view) {
		MarkerContentGenerator generator = null;
		try {
			Field fieldGenerator = ExtendedMarkersView.class.getDeclaredField("generator");
			fieldGenerator.setAccessible(true);
			generator = (MarkerContentGenerator) fieldGenerator.get(view);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		}
		return generator;
	}

	boolean isButtonAvailable(Composite composite, String buttonText) {
		Control[] children = composite.getChildren();
		for (Control ctrl : children) {
			if (ctrl instanceof Button) {
				if (((Button) ctrl).getText().contains(buttonText)) {
					return true;
				}
			} else if (ctrl instanceof Composite && isButtonAvailable((Composite) ctrl, buttonText)) {
				return true;
			}
		}
		return false;
	}
}
