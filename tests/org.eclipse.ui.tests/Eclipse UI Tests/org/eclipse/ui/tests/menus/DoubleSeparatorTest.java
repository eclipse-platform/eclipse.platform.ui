package org.eclipse.ui.tests.menus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.menus.DynamicMenuContributionItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.5
 *
 */
@RunWith(JUnit4.class)
public class DoubleSeparatorTest extends MenuTestCase {


	public DoubleSeparatorTest() {
		super(DoubleSeparatorTest.class.getSimpleName());
	}

	@Test
	public void testDoubleSeperatorWithDynamic() {

		MenuManager menuManager = new MenuManager();

		ActionContributionItem action1 = new ActionContributionItem(mock(IAction.class));
		Separator separator1 = new Separator();
		DynamicMenuContributionItem dynamic1 = mock(DynamicMenuContributionItem.class);
		Separator separator2 = new Separator();
		ActionContributionItem action2 = new ActionContributionItem(mock(IAction.class));

		when(dynamic1.isVisible()).thenReturn(true);

		menuManager.add(action1);
		menuManager.add(separator1);
		menuManager.add(dynamic1);
		menuManager.add(separator2);
		menuManager.add(action2);

		Menu contextMenu = menuManager.createContextMenu(window.getShell());
		contextMenu.notifyListeners(SWT.Show, null);
		processEvents();

		assertEquals(3, contextMenu.getItemCount());
		assertEquals(action1, contextMenu.getItems()[0].getData());
		assertEquals(separator2, contextMenu.getItems()[1].getData());
		assertEquals(action2, contextMenu.getItems()[2].getData());
	}

	@Test
	public void testDoubleSeperator() {

		MenuManager menuManager = new MenuManager();

		ActionContributionItem action1 = new ActionContributionItem(mock(IAction.class));
		Separator separator1 = new Separator();
		Separator separator2 = new Separator();
		ActionContributionItem action2 = new ActionContributionItem(mock(IAction.class));

		menuManager.add(action1);
		menuManager.add(separator1);
		menuManager.add(separator2);
		menuManager.add(action2);

		Menu contextMenu = menuManager.createContextMenu(window.getShell());
		contextMenu.notifyListeners(SWT.Show, null);
		processEvents();

		assertEquals(3, contextMenu.getItemCount());
		assertEquals(action1, contextMenu.getItems()[0].getData());
		assertEquals(separator2, contextMenu.getItems()[1].getData());
		assertEquals(action2, contextMenu.getItems()[2].getData());
	}

	@Test
	public void testLeadingSeparatorWithDynamic() {
		MenuManager menuManager = new MenuManager();

		DynamicMenuContributionItem dynamic1 = mock(DynamicMenuContributionItem.class);
		Separator separator1 = new Separator();
		ActionContributionItem action1 = new ActionContributionItem(mock(IAction.class));

		when(dynamic1.isVisible()).thenReturn(true);

		menuManager.add(dynamic1);
		menuManager.add(separator1);
		menuManager.add(action1);

		Menu contextMenu = menuManager.createContextMenu(window.getShell());
		contextMenu.notifyListeners(SWT.Show, null);
		processEvents();

		assertEquals(1, contextMenu.getItemCount());
		assertEquals(action1, contextMenu.getItems()[0].getData());
	}

	@Test
	public void testLeadingSeparator() {
		MenuManager menuManager = new MenuManager();

		Separator separator1 = new Separator();
		ActionContributionItem action1 = new ActionContributionItem(mock(IAction.class));

		menuManager.add(separator1);
		menuManager.add(action1);

		Menu contextMenu = menuManager.createContextMenu(window.getShell());
		contextMenu.notifyListeners(SWT.Show, null);
		processEvents();

		assertEquals(1, contextMenu.getItemCount());
		assertEquals(action1, contextMenu.getItems()[0].getData());
	}

	@Test
	public void testTrainlingSeparatorWithDynamic() {
		MenuManager menuManager = new MenuManager();

		ActionContributionItem action1 = new ActionContributionItem(mock(IAction.class));
		Separator separator1 = new Separator();
		DynamicMenuContributionItem dynamic1 = mock(DynamicMenuContributionItem.class);

		when(dynamic1.isVisible()).thenReturn(true);

		menuManager.add(action1);
		menuManager.add(separator1);
		menuManager.add(dynamic1);

		Menu contextMenu = menuManager.createContextMenu(window.getShell());
		contextMenu.notifyListeners(SWT.Show, null);
		processEvents();

		assertEquals(1, contextMenu.getItemCount());
		assertEquals(action1, contextMenu.getItems()[0].getData());
	}

	@Test
	public void testTrainlingSeparator() {
		MenuManager menuManager = new MenuManager();

		ActionContributionItem action1 = new ActionContributionItem(mock(IAction.class));
		Separator separator1 = new Separator();

		menuManager.add(action1);
		menuManager.add(separator1);

		Menu contextMenu = menuManager.createContextMenu(window.getShell());
		contextMenu.notifyListeners(SWT.Show, null);
		processEvents();

		assertEquals(1, contextMenu.getItemCount());
		assertEquals(action1, contextMenu.getItems()[0].getData());
	}

}
