package org.eclipse.ui.internal.incubator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Experimental Action for search-based navigation to UI elements such as editors, views, commands.
 * 
 */
public class CtrlEAction
		implements
			IWorkbenchWindowActionDelegate,
			IActionDelegate2 {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public CtrlEAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		runWithEvent(action, null);
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {

		// need to get commands here because opening the popup changes which commands are "handled"
		BindingService bindingService = (BindingService) PlatformUI
				.getWorkbench().getService(IBindingService.class);
		Binding[] bindings = bindingService.getBindings();
		SortedSet commands = new TreeSet();
		for (int i = 0; i < bindings.length; i++) {
			Binding binding = bindings[i];
			ParameterizedCommand command = binding.getParameterizedCommand();
			if (command != null && command.getCommand().isHandled()) {
				commands.add(command);
			}
		}

		FilteringInfoPopup popup = new MyInfoPopup(window.getShell(), commands);
		popup.setInput(new Object());
		popup.setSize(300, 400);
		if (event != null) {
			if (event.widget instanceof ToolItem) {
				ToolItem toolItem = (ToolItem) event.widget;
				Rectangle bounds = toolItem.getBounds();
				Point popupLocation = new Point(bounds.x, bounds.y
						+ bounds.height);
				popup
						.setLocation(toolItem.getParent().toDisplay(
								popupLocation));
			} else {
				System.out.println(event.toString());
			}
		}
		TreeItem[] rootItems = ((Tree) popup.getTreeViewer().getControl())
				.getItems();
		if (rootItems.length > 0)
			((Tree) popup.getTreeViewer().getControl())
					.setTopItem(rootItems[0]);
		popup.open();
	}
	/**
	 * @since 3.2
	 *
	 */
	private final class MyInfoPopup extends FilteringInfoPopup {
		private SortedSet commands;
		/**
		 * @param parent
		 * @param style
		 * @param style2
		 * @param field
		 */
		public MyInfoPopup(Shell shell, SortedSet commands) {
			super(shell, SWT.RESIZE, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE,
					false);
			MyInfoPopup.this.commands = commands;
			getTreeViewer().setContentProvider(new MyContentProvider(MyInfoPopup.this.commands));
		}
		protected TreeViewer createTreeViewer(Composite parent, int style) {
			TreeViewer viewer = new TreeViewer(parent, style);
			viewer.setLabelProvider(new MyLabelProvider());
			return viewer;
		}
		protected String getId() {
			return "org.eclipse.ui.internal.incubator.ctrlE"; //$NON-NLS-1$
		}
		protected void handleElementSelected(Object selectedElement) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				if (selectedElement instanceof IViewDescriptor) {
					IViewDescriptor viewDescriptor = (IViewDescriptor) selectedElement;
					try {
						activePage.showView(viewDescriptor.getId());
						return;
					} catch (PartInitException e) {
					}
				}
				if (selectedElement instanceof Saveable) {
					Saveable saveable = (Saveable) selectedElement;
					saveable.show(activePage);
					return;
				}
				if (selectedElement instanceof PreferenceNode) {
					PreferenceNode preferenceNode = (PreferenceNode) selectedElement;
					WorkbenchPreferenceDialog dialog = WorkbenchPreferenceDialog
							.createDialogOn(window.getShell(), preferenceNode
									.getId());
					dialog.open();
					return;
				}
				if (selectedElement instanceof IWizardDescriptor) {
					IWizardDescriptor wizardDescriptor = (IWizardDescriptor) selectedElement;
					NewWizardShortcutAction wizardAction = new NewWizardShortcutAction(
							window, wizardDescriptor);
					wizardAction.run();
					return;
				}
				if (selectedElement instanceof ParameterizedCommand) {
					IHandlerService handlerService = (IHandlerService) window
							.getWorkbench().getService(IHandlerService.class);
					ParameterizedCommand command = (ParameterizedCommand) selectedElement;
					try {
						handlerService.executeCommand(command, null);
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (NotDefinedException e) {
						e.printStackTrace();
					} catch (NotEnabledException e) {
						e.printStackTrace();
					} catch (NotHandledException e) {
						e.printStackTrace();
					}
					return;
				}
				if (selectedElement instanceof ActionContributionItem) {
					ActionContributionItem item = (ActionContributionItem) selectedElement;
					item.getAction().run();
					return;
				}
			}
		}
		public void setInput(Object information) {
			getTreeViewer().setAutoExpandLevel(2);
			getTreeViewer().setInput(information);
		}
	}

	private static class Node {
		private String name;
		Node(String name) {
			this.name = name;
		}
		public String toString() {
			return name;
		}
	}

	private final class MyContentProvider implements ITreeContentProvider {
		private Object input;

		private Node editorNode = new Node(
				IncubatorMessages.CtrlEAction_Editors);
		private Node viewNode = new Node(IncubatorMessages.CtrlEAction_Views);
		private Node commandNode = new Node(
				IncubatorMessages.CtrlEAction_Commands);
		private Node menusNode = new Node(IncubatorMessages.CtrlEAction_Menus);
		private Node newNode = new Node(IncubatorMessages.CtrlEAction_New);
		private Node preferencesNode = new Node(
				IncubatorMessages.CtrlEAction_Preferences);

		private SortedSet commands;

		/**
		 * @param commands
		 */
		public MyContentProvider(SortedSet commands) {
			MyContentProvider.this.commands = commands;
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Node) {
				if (editorNode.equals(parentElement)) {
					SaveablesList saveablesList = (SaveablesList) PlatformUI
							.getWorkbench().getService(
									ISaveablesLifecycleListener.class);
					return saveablesList.getOpenModels();
				} else if (viewNode.equals(parentElement)) {
					return PlatformUI.getWorkbench().getViewRegistry()
							.getViews();
				} else if (commandNode.equals(parentElement)) {
					return commands.toArray();
				} else if (preferencesNode.equals(parentElement)) {
					List elements = PlatformUI.getWorkbench()
							.getPreferenceManager().getElements(
									PreferenceManager.PRE_ORDER);
					Set uniqueElements = new LinkedHashSet(elements);
					return uniqueElements.toArray();
				} else if (menusNode.equals(parentElement)) {
					MenuManager menu = ((WorkbenchWindow) window)
							.getMenuBarManager();
					Set result = new HashSet();
					collectContributions(menu, result);
					return result.toArray();
				} else if (newNode.equals(parentElement)) {
					IWizardCategory rootCategory = WorkbenchPlugin.getDefault()
							.getNewWizardRegistry().getRootCategory();
					List result = new ArrayList();
					collectWizards(rootCategory, result);
					return result.toArray();
				}
			}
			if (parentElement == input) {
				return new Node[]{editorNode, viewNode, commandNode, menusNode,
						newNode, preferencesNode};
			}
			return new Object[0];
		}
		private void collectContributions(MenuManager menu, Set result) {
			IContributionItem[] items = menu.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				if (item instanceof SubContributionItem) {
					item = ((SubContributionItem) item).getInnerItem();
				}
				if (item instanceof MenuManager) {
					collectContributions((MenuManager) item, result);
				} else if (item instanceof ActionContributionItem) {
					result.add(item);
				}
			}
		}
		private void collectWizards(IWizardCategory category, List result) {
			result.addAll(Arrays.asList(category.getWizards()));
			IWizardCategory[] childCategories = category.getCategories();
			for (int i = 0; i < childCategories.length; i++) {
				collectWizards(childCategories[i], result);
			}
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.input = newInput;
		}
	}

	private static final class MyLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof Saveable) {
				Saveable saveable = (Saveable) element;
				return saveable.getName();
			}
			if (element instanceof IViewDescriptor) {
				IViewDescriptor viewDescriptor = (IViewDescriptor) element;
				return viewDescriptor.getLabel();
			}
			if (element instanceof IPreferenceNode) {
				IPreferenceNode preferenceNode = (IPreferenceNode) element;
				return preferenceNode.getLabelText();
			}
			if (element instanceof IWizardDescriptor) {
				IWizardDescriptor wizardDescriptor = (IWizardDescriptor) element;
				return wizardDescriptor.getLabel();
			}
			if (element instanceof ActionContributionItem) {
				ActionContributionItem item = (ActionContributionItem) element;
				return LegacyActionTools.removeMnemonics(item.getAction()
						.getText());
			}
			if (element instanceof ParameterizedCommand) {
				ParameterizedCommand command = (ParameterizedCommand) element;
				try {
					return command.getName();
				} catch (NotDefinedException e) {
					return command.toString();
				}
			}
			return super.getText(element);
		}
	}
}