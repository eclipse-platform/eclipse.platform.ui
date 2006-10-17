package org.eclipse.ui.internal.incubator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
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
 * Experimental Action for search-based navigation to UI elements such as
 * editors, views, commands.
 * 
 */
public class CtrlEAction extends AbstractHandler {

	private static final String DIRTY_MARK = "*"; //$NON-NLS-1$

	private IWorkbenchWindow window;

	protected String rememberedText;

	private LinkedList previousPicksList = new LinkedList();

	/**
	 * The constructor.
	 */
	public CtrlEAction() {
	}

	public Object execute(ExecutionEvent executionEvent) {
		// need to get commands here because opening the popup changes which
		// commands are "handled"
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

		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		FilteringInfoPopup popup = new MyInfoPopup(window.getShell(), commands);
		popup.setInput(new Object());
		TreeItem[] rootItems = ((Tree) popup.getTreeViewer().getControl())
				.getItems();
		if (rootItems.length > 0)
			((Tree) popup.getTreeViewer().getControl())
					.setTopItem(rootItems[0]);
		popup.open();
		return null;
	}

	private final static class QuickAccessTreeSorter extends
			TreePathViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			ILabelProvider labelProvider = (ILabelProvider) ((TreeViewer) viewer)
					.getLabelProvider();
			String name1 = stripDirtyIndicator(labelProvider.getText(e1));
			String name2 = stripDirtyIndicator(labelProvider.getText(e2));
			return getComparator().compare(name1, name2);
		}

		public void sort(Viewer viewer, TreePath parentPath, Object[] elements) {
			if (parentPath == null) {
				return;
			}
			Object parent = parentPath.getLastSegment();
			if (parent instanceof Node) {
				Node node = (Node) parent;
				// TODO replace with a proper check
				if (node.name.equals(IncubatorMessages.CtrlEAction_Previous)) {
					return;
				}
			}
			super.sort(viewer, parentPath, elements);
		}
	}

	/**
	 * @since 3.2
	 * 
	 */
	private final class MyInfoPopup extends FilteringInfoPopup {
		private SortedSet commands;

		/**
		 * @param shell
		 * @param commands
		 */
		public MyInfoPopup(Shell shell, SortedSet commands) {
			super(shell, SWT.RESIZE, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE,
					false);
			MyInfoPopup.this.commands = commands;
			getTreeViewer().setContentProvider(
					new MyContentProvider(MyInfoPopup.this.commands));
		}

		protected TreeViewer createTreeViewer(Composite parent, int style) {
			TreeViewer viewer = new TreeViewer(parent, style);
			viewer.setLabelProvider(new MyLabelProvider());
			viewer.setComparator(new QuickAccessTreeSorter());
			return viewer;
		}
		
		protected String getMatchName(Object element) {
			String name = ((ILabelProvider) getTreeViewer().getLabelProvider()).getText(element);
			return stripDirtyIndicator(name);
		}

		protected Point getInitialSize() {
			if (!MyInfoPopup.this.getPersistBounds()) {
				return new Point(300, 400);
			}
			return super.getInitialSize();
		}

		protected Point getInitialLocation(Point initialSize) {
			if (!MyInfoPopup.this.getPersistBounds()) {
				Point size = new Point(300, 400);
				Rectangle parentBounds = MyInfoPopup.this.getParentShell()
						.getBounds();
				int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
				int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
				return new Point(x, y);
			}
			return super.getInitialLocation(initialSize);
		}

		protected IDialogSettings getDialogSettings() {
			String sectionName = getId();
			IDialogSettings settings = WorkbenchPlugin.getDefault()
					.getDialogSettings();
			if (settings == null) {
				settings = WorkbenchPlugin.getDefault().getDialogSettings()
						.addNewSection(sectionName);
			}
			return settings;
		}

		protected String getId() {
			return "org.eclipse.ui.internal.incubator.ctrlE"; //$NON-NLS-1$
		}

		public boolean close() {
			rememberedText = getFilterText().getText();
			return super.close();
		}

		protected void handleElementSelected(Object selectedElement) {
			addPreviousPick(selectedElement);
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
				if (selectedElement instanceof IPerspectiveDescriptor) {
					IPerspectiveDescriptor perspectiveDescriptor = (IPerspectiveDescriptor) selectedElement;
					activePage.setPerspective(perspectiveDescriptor);
				}
				if (selectedElement instanceof IEditorReference) {
					IEditorReference editorReference = (IEditorReference) selectedElement;
					IWorkbenchPart part = editorReference.getPart(true);
					if (part != null) {
						activePage.activate(part);
					}
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

		private String imageId;

		Node(String name, String imageId) {
			this.name = name;
			this.imageId = imageId;
		}

		public String toString() {
			return name;
		}

		/**
		 * @return
		 */
		public String getImageId() {
			return imageId;
		}
	}

	private final class MyContentProvider implements ITreeContentProvider {
		private Object input;

		private Node previousNode = new Node(
				IncubatorMessages.CtrlEAction_Previous, null);

		private Node editorNode = new Node(
				IncubatorMessages.CtrlEAction_Editors, null);

		private Node viewNode = new Node(IncubatorMessages.CtrlEAction_Views,
				IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC);

		private Node perspectiveNode = new Node(
				IncubatorMessages.CtrlEAction_Perspectives,
				IWorkbenchGraphicConstants.IMG_ETOOL_DEF_PERSPECTIVE);

		private Node commandNode = new Node(
				IncubatorMessages.CtrlEAction_Commands, null);

		private Node menusNode = new Node(IncubatorMessages.CtrlEAction_Menus,
				null);

		private Node newNode = new Node(IncubatorMessages.CtrlEAction_New, null);

		private Node preferencesNode = new Node(
				IncubatorMessages.CtrlEAction_Preferences, null);

		private SortedSet commands;

		/**
		 * @param commands
		 */
		public MyContentProvider(SortedSet commands) {
			MyContentProvider.this.commands = commands;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Node) {
				if (previousNode.equals(parentElement)) {
					return getPreviousPicks();
				} else if (editorNode.equals(parentElement)) {
					if(window.getActivePage() != null) {
						return window.getActivePage().getEditorReferences();
					}
				} else if (viewNode.equals(parentElement)) {
					return PlatformUI.getWorkbench().getViewRegistry()
							.getViews();
				} else if (perspectiveNode.equals(parentElement)) {
					return PlatformUI.getWorkbench().getPerspectiveRegistry()
							.getPerspectives();
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
				return new Node[] { previousNode, editorNode, viewNode,
						perspectiveNode, commandNode, menusNode, newNode,
						preferencesNode };
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
		private LocalResourceManager resourceManager = new LocalResourceManager(
				JFaceResources.getResources());

		public Image getImage(Object element) {
			if (element instanceof Node) {
				Node node = (Node) element;
				return findOrCreateImage(WorkbenchImages
						.getImageDescriptor(node.getImageId()));
			}
			if (element instanceof IEditorReference) {
				IEditorReference editorReference = (IEditorReference) element;
				return editorReference.getTitleImage();
			}
			if (element instanceof IViewDescriptor) {
				IViewDescriptor viewDescriptor = (IViewDescriptor) element;
				return findOrCreateImage(viewDescriptor.getImageDescriptor());
			}
			if (element instanceof IPerspectiveDescriptor) {
				IPerspectiveDescriptor perspectiveDescriptor = (IPerspectiveDescriptor) element;
				return findOrCreateImage(perspectiveDescriptor
						.getImageDescriptor());
			}
			if (element instanceof IPreferenceNode) {
				IPreferenceNode preferenceNode = (IPreferenceNode) element;
				return preferenceNode.getLabelImage();
			}
			if (element instanceof IWizardDescriptor) {
				IWizardDescriptor wizardDescriptor = (IWizardDescriptor) element;
				return findOrCreateImage(wizardDescriptor.getDescriptionImage());
			}
			return super.getImage(element);
		}

		/**
		 * @param imageDescriptor
		 * @return image, or null
		 * @throws DeviceResourceException
		 */
		private Image findOrCreateImage(ImageDescriptor imageDescriptor) {
			if (imageDescriptor == null) {
				return null;
			}
			Image image = (Image) resourceManager.find(imageDescriptor);
			if (image == null) {
				try {
					image = resourceManager.createImage(imageDescriptor);
				} catch (DeviceResourceException e) {
					WorkbenchPlugin.log(e);
				}
			}
			return image;
		}

		public void dispose() {
			resourceManager.dispose();
			resourceManager = null;
			super.dispose();
		}

		public String getText(Object element) {
			String separator = " - "; //$NON-NLS-1$
			if (element instanceof IEditorReference) {
				IEditorReference editorReference = (IEditorReference) element;
				StringBuffer result = new StringBuffer();
				if (editorReference.isDirty()) {
					result.append(DIRTY_MARK);
				}
				result.append(editorReference.getName());
				result.append(separator);
				result.append(editorReference.getTitleToolTip());
				return result.toString();
			}
			if (element instanceof IViewDescriptor) {
				IViewDescriptor viewDescriptor = (IViewDescriptor) element;
				return viewDescriptor.getLabel();
			}
			if (element instanceof IPerspectiveDescriptor) {
				IPerspectiveDescriptor perspectiveDescriptor = (IPerspectiveDescriptor) element;
				return perspectiveDescriptor.getLabel();
			}
			if (element instanceof IPreferenceNode) {
				IPreferenceNode preferenceNode = (IPreferenceNode) element;
				IPreferencePage page = preferenceNode.getPage();
				if (page != null && page.getDescription() != null
						&& page.getDescription().length() != 0) {
					return preferenceNode.getLabelText() + separator
							+ page.getDescription();
				}
				return preferenceNode.getLabelText();
			}
			if (element instanceof IWizardDescriptor) {
				IWizardDescriptor wizardDescriptor = (IWizardDescriptor) element;
				return wizardDescriptor.getLabel() + separator
						+ wizardDescriptor.getDescription();
			}
			if (element instanceof ActionContributionItem) {
				ActionContributionItem item = (ActionContributionItem) element;
				IAction action = item.getAction();
				if (action.getToolTipText() != null
						&& action.getToolTipText().length() != 0) {
					return LegacyActionTools.removeMnemonics(action.getText())
							+ separator + action.getToolTipText();
				}
				return LegacyActionTools.removeMnemonics(action.getText());
			}
			if (element instanceof ParameterizedCommand) {
				ParameterizedCommand command = (ParameterizedCommand) element;
				try {
					Command nestedCommand = command.getCommand();
					if (nestedCommand != null
							&& nestedCommand.getDescription() != null
							&& nestedCommand.getDescription().length() != 0) {
						return command.getName() + separator
								+ nestedCommand.getDescription();
					}
					return command.getName();
				} catch (NotDefinedException e) {
					return command.toString();
				}
			}
			return super.getText(element);
		}
	}
	
	/**
	 * @param element
	 */
	private void addPreviousPick(Object element) {
		previousPicksList.remove(element);
		previousPicksList.addFirst(element);
	}

	/**
	 * @return
	 */
	private Object[] getPreviousPicks() {
		return previousPicksList.toArray();
	}

	private static String stripDirtyIndicator(String elementName) {
		if (elementName.startsWith(DIRTY_MARK)) {
			elementName = elementName.substring(1);
		}
		return elementName;
	}


}