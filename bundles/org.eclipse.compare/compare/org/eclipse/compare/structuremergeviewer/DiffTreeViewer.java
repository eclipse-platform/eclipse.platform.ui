/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.structuremergeviewer;

import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.util.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.compare.internal.*;
import org.eclipse.compare.*;


/**
 * A tree viewer that works on objects implementing
 * the <code>IDiffContainer</code> and <code>IDiffElement</code> interfaces.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed outside
 * this package.
 * </p>
 *
 * @see IDiffContainer
 * @see IDiffElement
 */
public class DiffTreeViewer extends TreeViewer {
	
	static class DiffViewerSorter extends ViewerSorter {
	
		public boolean isSorterProperty(Object element, Object property) {
			return false;
		}
	
		public int category(Object node) {
			if (node instanceof DiffNode) {
				Object o= ((DiffNode) node).getId();
				if (o instanceof DocumentRangeNode)
					return ((DocumentRangeNode) o).getTypeCode();
			}
			return 0;
		}
	}	

	class DiffViewerContentProvider implements ITreeContentProvider {
			
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	
		public boolean isDeleted(Object element) {
			return false;
		}
			
		public void dispose() {
			inputChanged(DiffTreeViewer.this, getInput(), null);
		}
			
		public Object getParent(Object element) {
			if (element instanceof IDiffElement) 
				return ((IDiffElement)element).getParent();
			return null;
		}
		
		public final boolean hasChildren(Object element) {
			if (element instanceof IDiffContainer) 
				return ((IDiffContainer)element).hasChildren();
			return false;
		}
		
		public final Object[] getChildren(Object element) {
			if (element instanceof IDiffContainer)
				return ((IDiffContainer)element).getChildren();
			return new Object[0];
		}
		
		public Object[] getElements(Object element) {
			return getChildren(element);
		}				
	}

	class DiffViewerLabelProvider extends LabelProvider {
		
		public String getText(Object element) {
			if (element instanceof IDiffElement)
				return ((IDiffElement)element).getName();
			return "<null>";
		}
	
		public Image getImage(Object element) {
			if (element instanceof IDiffElement) {
				IDiffElement input= (IDiffElement) element;
				return fCompareConfiguration.getImage(input.getImage(), input.getKind());
			}
			return null;
		}
	}

	static class FilterSame extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IDiffElement)
				return (((IDiffElement)element).getKind() & Differencer.PSEUDO_CONFLICT) == 0;
			return true;
		}
		public boolean isFilterProperty(Object element, Object property) {
			return false;
		}
	}

	private ResourceBundle fBundle;
	private CompareConfiguration fCompareConfiguration;
	private ViewerFilter fViewerFilter;
	private IPropertyChangeListener fPropertyChangeListener;
	private IPropertyChangeListener fPreferenceChangeListener;

	private Action fCopyLeftToRightAction;
	private Action fCopyRightToLeftAction;
	private Action fNextAction;
	private Action fPreviousAction;
		
	/**
	 * Creates a new viewer for the given SWT tree control with the specified configuration.
	 *
	 * @param tree the tree control
	 * @param configuration the configuration for this viewer
	 */
	public DiffTreeViewer(Tree tree, CompareConfiguration configuration) {
		super(tree);
		initialize(configuration);
	}
	
	/**
	 * Creates a new viewer under the given SWT parent and with the specified configuration.
	 *
	 * @param parent the SWT control under which to create the viewer
	 * @param configuration the configuration for this viewer
	 */
	public DiffTreeViewer(Composite parent, CompareConfiguration configuration) {
		super(new Tree(parent, SWT.MULTI));
		initialize(configuration);
	}
	
	private void initialize(CompareConfiguration configuration) {
		
		Control tree= getControl();
		
		tree.setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle());

		Composite parent= tree.getParent();
		
		fBundle= ResourceBundle.getBundle("org.eclipse.compare.structuremergeviewer.DiffTreeViewerResources");
		
		// register for notification with the CompareConfiguration 
		fCompareConfiguration= configuration;
		if (fCompareConfiguration != null) {
			fPropertyChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					DiffTreeViewer.this.propertyChange(event);
				}
			};
			fCompareConfiguration.addPropertyChangeListener(fPropertyChangeListener);
		}	
		
		// register for notification with the Compare plugin's PreferenceStore 
		fPreferenceChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(CompareConfiguration.SHOW_PSEUDO_CONFLICTS))
					syncShowPseudoConflictFilter();			
			}
		};
		IPreferenceStore ps= CompareUIPlugin.getDefault().getPreferenceStore();
		if (ps != null)
			ps.addPropertyChangeListener(fPreferenceChangeListener);
			
	
		setContentProvider(new DiffViewerContentProvider());
		setLabelProvider(new DiffViewerLabelProvider());
		
		addSelectionChangedListener(
			new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent se) {
					updateActions();
				}
			}
		);
						
		syncShowPseudoConflictFilter();			
				
		setSorter(new DiffViewerSorter());
		
		ToolBarManager tbm= CompareViewerSwitchingPane.getToolBarManager(parent);
		if (tbm != null) {
			tbm.removeAll();
			
			tbm.add(new Separator("merge"));
			tbm.add(new Separator("modes"));
			tbm.add(new Separator("navigation"));
			
			createToolItems(tbm);
			updateActions();
			
			tbm.update(true);
		}
		
		MenuManager mm= new MenuManager();
		mm.setRemoveAllWhenShown(true);
		mm.addMenuListener(
			new IMenuListener() {
				public void menuAboutToShow(IMenuManager mm) {
					fillContextMenu(mm);
				}
			}
		);
		tree.setMenu(mm.createContextMenu(tree));
	}
			
	/**
	 * Returns the viewer's name.
	 *
	 * @return the viewer's name
	 */
	public String getTitle() {
		return "Structure Compare";
	}
	
	/**
	 * Returns the resource bundle.
	 *
	 * @return the viewer's resource bundle
	 */
	protected ResourceBundle getBundle() {
		return fBundle;
	}

	/**
	 * Returns the compare configuration of this viewer.
	 *
	 * @return the compare configuration of this viewer
	 */
	public CompareConfiguration getCompareConfiguration() {
		return fCompareConfiguration;
	}
			
	/**
	 * Called on the viewer disposal.
	 * Unregisters from the compare configuration.
	 * Clients may extend if they have to do additional cleanup.
	 */
	protected void handleDispose(DisposeEvent event) {
		
		if (fPreferenceChangeListener != null) {
			IPreferenceStore ps= CompareUIPlugin.getDefault().getPreferenceStore();
			if (ps != null)
				ps.addPropertyChangeListener(fPreferenceChangeListener);
			fPreferenceChangeListener= null;
		}
				
		if (fCompareConfiguration != null) {
			if (fPropertyChangeListener != null)
				fCompareConfiguration.removePropertyChangeListener(fPropertyChangeListener);
			fCompareConfiguration= null;
		}
		fPropertyChangeListener= null;
		
		super.handleDispose(event);
	}
	
	/**
	 * Tracks property changes of the configuration object.
	 * Clients may extend to track their own property changes.
	 */
	protected void propertyChange(PropertyChangeEvent event) {
			
		//if (event.getProperty().equals(CompareConfiguration.SHOW_PSEUDO_CONFLICTS))
		//	syncShowPseudoConflictFilter();	 		 
	}
	
	protected void inputChanged(Object in, Object oldInput) {
		super.inputChanged(in, oldInput);
		expandToLevel(2);
		updateActions();
		
//			System.out.println("inputChanged: " + in);
//			if (fCompareConfiguration != null) {
//				System.out.println("  left: " + fCompareConfiguration.isLeftEditable());
//				System.out.println("  right: " + fCompareConfiguration.isRightEditable());
//			}

	}

	/**
	 * Overridden to avoid expanding <code>DiffNode</code>s that shouldn't expand
	 * (i.e. where the <code>dontExpand</code> method returns <code>true</code>).
	 */
	protected void internalExpandToLevel(Widget node, int level) {
				
		Object data= node.getData();
		if (data instanceof DiffNode && ((DiffNode)data).dontExpand())
			return;
		
		super.internalExpandToLevel(node, level);
	}
	
	//---- merge action support

	/**
	 * This factory method is called after the viewer's controls have been created.
	 * It installs four actions in the given <code>ToolBarManager</code>. Two actions
	 * allow for copying one side of a <code>DiffNode</code> to the other side.
	 * Two other actions are for navigating from one node to the next (previous).
	 * <p>
	 * Clients can override this method and are free to decide whether they want to call
	 * the inherited method.
	 *
	 * @param toolbarManager the toolbar manager for which to add the actions
	 */
	protected void createToolItems(ToolBarManager toolbarManager) {
		
		fCopyLeftToRightAction= new Action() {
			public void run() {
				copySelected(true);
			}
		};
		Utilities.initAction(fCopyLeftToRightAction, fBundle, "action.TakeLeft.");
		toolbarManager.appendToGroup("merge", fCopyLeftToRightAction);

		fCopyRightToLeftAction= new Action() {
			public void run() {
				copySelected(false);
			}
		};
		Utilities.initAction(fCopyRightToLeftAction, fBundle, "action.TakeRight.");
		toolbarManager.appendToGroup("merge", fCopyRightToLeftAction);
		
		fNextAction= new Action() {
			public void run() {
				navigate(true);
			}
		};
		Utilities.initAction(fNextAction, fBundle, "action.NextDiff.");
		toolbarManager.appendToGroup("navigation", fNextAction);

		fPreviousAction= new Action() {
			public void run() {
				navigate(false);
			}
		};
		Utilities.initAction(fPreviousAction, fBundle, "action.PrevDiff.");
		toolbarManager.appendToGroup("navigation", fPreviousAction);
	}
	
	/**
	 * This method is called to add actions to the viewer's context menu.
	 * It installs actions for copying one side of a <code>DiffNode</code> to the other side.
	 * Clients can override this method and are free to decide whether they want to call
	 * the inherited method.
	 *
	 * @param manager the menu manager for which to add the actions
	 */
	protected void fillContextMenu(IMenuManager manager) {
		if (fCopyLeftToRightAction != null)
			manager.add(fCopyLeftToRightAction);
		if (fCopyRightToLeftAction != null)
			manager.add(fCopyRightToLeftAction);
	}

	/**
	 * Copies one side of all <code>DiffNode</code>s in the current selection to the other side.
	 * Called from the (internal) actions for copying the sides of a <code>DiffNode</code>.
	 * Clients may override. 
	 * 
	 * @param leftToRight if <code>true</code> the left side is copied to the right side.
	 * If <code>false</code> the right side is copied to the left side
	 */
	protected void copySelected(boolean leftToRight) {
		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator e= ((IStructuredSelection) selection).iterator();
			while (e.hasNext()) {
				Object element= e.next();
				if (element instanceof ICompareInput)
					copyOne((ICompareInput) element, leftToRight);
			}
		}
	}
	
	/**
	 * Called to copy one side of the given node to the other.
	 * This default implementation delegates the call to <code>ICompareInput.copy(...)</code>.
	 * Clients may override. 
	 * 
	 * @param leftToRight if <code>true</code> the left side is copied to the right side.
	 * If <code>false</code> the right side is copied to the left side
	 */
	protected void copyOne(ICompareInput node, boolean leftToRight) {
		
		node.copy(leftToRight);
		
		// update node's image
		update(new Object[] { node }, null);
	}
	
	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may override. 
	 *
	 * @param next if <code>true</code> the next node is selected, otherwise the previous node
	 */
	protected void navigate(boolean next) {
		
		Control c= getControl();
		if (!(c instanceof Tree))
			return;
			
		Tree tree= (Tree) c;
		TreeItem children[]= tree.getSelection();
		TreeItem item= null;
		
		if (children != null && children.length > 0)
			item= children[0];
			
		if (item != null) {
			if (!next) {
			
				TreeItem parent= item.getParentItem();
				if (parent != null)
					children= parent.getItems();
				else
					children= tree.getItems();
				
				if (children != null && children.length > 0) {
					// goto previous child
					int index= 0;
					for (; index < children.length; index++)
						if (children[index] == item)
							break;
					
					if (index > 0) {
						
						item= children[index-1];
						
						while (true) {
							int n= item.getItemCount();
							if (n <= 0)
								break;
								
							item.setExpanded(true);
							item= item.getItems()[n-1];
						}

						// previous
						internalSetSelection(item);
						return;
					}
				}
				
				// go up
				if (parent != null) {
					internalSetSelection(parent);
					return;
				}
				item= null;
						
			} else {
				item.setExpanded(true);
				createChildren(item);
				
				if (item.getItemCount() > 0) {
					// has children: go down
					children= item.getItems();
					internalSetSelection(children[0]);
					return;
				}
				
				while (item != null) {
					children= null;
					TreeItem parent= item.getParentItem();
					if (parent != null)
						children= parent.getItems();
					else
						children= tree.getItems();
					
					if (children != null && children.length > 0) {
						// goto next child
						int index= 0;
						for (; index < children.length; index++)
							if (children[index] == item)
								break;
						
						if (index < children.length-1) {
							// next
							internalSetSelection(children[index+1]);
							return;
						}
					}
					
					// go up
					item= parent;
				}
			}
		}
		
		// at end (or beginning): wrap around		
		if (item == null) {
			children= tree.getItems();
			if (children != null && children.length > 0)
				internalSetSelection(children[next ? 0 : children.length-1]);
		}
	}
	
	private void internalSetSelection(TreeItem ti) {
		if (ti != null) {
			Object data= ti.getData();
			setSelection(new StructuredSelection(data));
		}
	}
	
	//---- private
	
	private void syncShowPseudoConflictFilter() {
		
		boolean showPseudoConflicts= Utilities.getBoolean(fCompareConfiguration, CompareConfiguration.SHOW_PSEUDO_CONFLICTS, false);
		
		if (showPseudoConflicts) {
			if (fViewerFilter != null) {
				removeFilter(fViewerFilter);
			}
		} else {
			if (fViewerFilter == null)
				fViewerFilter= new FilterSame();	
			addFilter(fViewerFilter);
		}
	}
	
	private final boolean isEditable(Object element, boolean left) {
		if (element instanceof ICompareInput) {
			ICompareInput diff= (ICompareInput) element;
			Object side= left ? diff.getLeft() : diff.getRight();
			if (side == null && diff instanceof IDiffElement) {
				IDiffContainer container= ((IDiffElement)diff).getParent();
				if (container instanceof ICompareInput) {
					ICompareInput parent= (ICompareInput) container;
					side= left ? parent.getLeft() : parent.getRight();
				}
			}
			if (side instanceof IEditableContent)
				return ((IEditableContent) side).isEditable();
		}
		return false;
	}
		
	private void updateActions() {
		int leftToRight= 0;
		int rightToLeft= 0;
		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			Iterator e= ss.iterator();
			while (e.hasNext()) {
				Object element= e.next();
				if (element instanceof ICompareInput) {
					ICompareInput diff= (ICompareInput) element;
					if (isEditable(element, false))
						leftToRight++;
					if (isEditable(element, true))
						rightToLeft++;
					if (leftToRight > 0 && rightToLeft > 0)
						break;
				}
			}
		}
		if (fCopyLeftToRightAction != null)
			fCopyLeftToRightAction.setEnabled(leftToRight > 0);
		if (fCopyRightToLeftAction != null)
			fCopyRightToLeftAction.setEnabled(rightToLeft > 0);
	}
}

