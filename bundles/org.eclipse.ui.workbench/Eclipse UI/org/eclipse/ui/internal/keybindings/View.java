package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {

	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
	class Record {
		
		org.eclipse.ui.internal.actions.Action action;
		KeySequence keySequence;
		Scope scope;
		Configuration configuration;
		String platform;			
		String locale;
	}	

	class ViewContentProvider implements IStructuredContentProvider {
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			org.eclipse.ui.internal.actions.Registry actionRegistry = org.eclipse.ui.internal.actions.Registry.getInstance();
			org.eclipse.ui.internal.keybindings.Registry keyBindingRegistry = org.eclipse.ui.internal.keybindings.Registry.getInstance();
			List list = new ArrayList();
			KeyManager keyManager = KeyManager.getInstance();
			SortedMap keySequenceMap = keyManager.getKeyMachine().getKeySequenceMap();
			Iterator iterator = keySequenceMap.entrySet().iterator();
			
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();			
				KeySequence keySequence = (KeySequence) entry.getKey();				
				MatchAction matchAction = (MatchAction) entry.getValue();
				Record record = new Record();
				record.action = (org.eclipse.ui.internal.actions.Action) actionRegistry.getActionMap().get(matchAction.getAction());
				record.keySequence = keySequence;
				State state = matchAction.getMatch().getState();
				List paths = state.getPaths();			
				Path scope = (Path) paths.get(0);
				Path configuration = (Path) paths.get(1);
				Path platform = (Path) paths.get(2);
				Path locale = (Path) paths.get(3);
				String scopeId = ((PathItem) scope.getPathItems().get(scope.getPathItems().size() - 1)).getValue();
				String configurationId = ((PathItem) configuration.getPathItems().get(configuration.getPathItems().size() - 1)).getValue();
				record.scope = (Scope) keyBindingRegistry.getScopeMap().get(scopeId);
				record.configuration = (Configuration) keyBindingRegistry.getConfigurationMap().get(configurationId);
				list.add(record);
			}

			return list.toArray();
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public String getColumnText(Object obj, int index) {
			if (obj instanceof Record) {
				Record record = (Record) obj;			
				KeyManager keyManager = KeyManager.getInstance();
				
				switch (index) {
					case 0: 
						return record.action != null ? record.action.toString() : "?";
					case 1:	
						return keyManager.getTextForKeySequence(record.keySequence);
					case 2:
						return record.scope != null ? record.scope.toString() : "?";
					case 3:
						return record.configuration != null ? record.configuration.toString() : "?";
					case 4:
						return record.platform != null ? record.platform.toString() : "";
					case 5:
						return record.locale != null ? record.locale.toString() : "";
				}					
			}

			return getText(obj);
		}
		
		public Image getColumnImage(Object obj, int index) {
			return null; //getImage(obj);
		}
		
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	class NameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Record) e1).keySequence.compareTo(((Record) e2).keySequence);	
		}
	}

	public View() {
		super();
	}

	private String columnHeaders[] = {
		"Action",
		"Key Sequence",
		"Scope",
		"Configuration",
		"Platform",
		"Locale" };

	private ColumnLayoutData columnLayouts[] = {
		new ColumnWeightData(400),
		new ColumnWeightData(200),
		new ColumnWeightData(100),
		new ColumnWeightData(100),
		new ColumnWeightData(100),
		new ColumnWeightData(100) };
	
	/*
	void createColumns() {
		SelectionListener headerListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int column = table.indexOf((TableColumn) e.widget);
				TaskSorter oldSorter = (TaskSorter) viewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					viewer.refresh();
				} else {
					viewer.setSorter(new TaskSorter(TaskList.this, column));
				}
			}
		};

		if(memento != null) {
			//restore columns width
			IMemento children[] = memento.getChildren(TAG_COLUMN);
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					Integer val = children[i].getInteger(TAG_NUMBER);
					if (val != null) {
						int index = val.intValue();
						val = children[i].getInteger(TAG_WIDTH);
						if (val != null) {
							columnLayouts[index] = new ColumnPixelData(val.intValue(), true);
						}
					}
				}
			}
		}
	}
	*/

	public void createPartControl(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		
		for (int i = 0; i < columnHeaders.length; i++) {
			layout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
			//tc.addSelectionListener(headerListener);
		}

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(ResourcesPlugin.getWorkspace());		
	
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				View.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator("Additions"));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				viewer.refresh();
				//showMessage("Action 1 executed");
			}
		};
		
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_TASK_TSK));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Key Bindings", message);
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
