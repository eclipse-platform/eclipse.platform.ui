/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.structuremergeviewer;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.services.IDisposable;


/**
 * A diff tree viewer that can be configured with a <code>IStructureCreator</code>
 * to retrieve a hierarchical structure from the input object (an <code>ICompareInput</code>)
 * and perform a two-way or three-way compare on it.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed outside
 * this package.
 * </p>
 *
 * @see IStructureCreator
 * @see ICompareInput
 */
public class StructureDiffViewer extends DiffTreeViewer {
		
	private Differencer fDifferencer;
	private boolean fThreeWay= false;
	
	private StructureInfo fAncestorStructure = new StructureInfo();
	private StructureInfo fLeftStructure = new StructureInfo();
	private StructureInfo fRightStructure = new StructureInfo();
	
	private IStructureCreator fStructureCreator;
	private IDiffContainer fRoot;
	private IContentChangeListener fContentChangedListener;
	private CompareViewerSwitchingPane fParent;
	
	/*
	 * A helper class for holding the input and generated structure
	 * for the ancestor, left and right inputs.
	 */
	private class StructureInfo {
		private ITypedElement fInput;
		private IStructureComparator fStructureComparator;
		
		public boolean setInput(ITypedElement newInput) {
			boolean changed = false;
			if (newInput != fInput) {
				if (fInput instanceof IContentChangeNotifier)
					((IContentChangeNotifier)fInput).removeContentChangeListener(fContentChangedListener);
				fInput= newInput;
				if (fInput != null) {
					refresh();
					changed= true;
				} else {
					if (fStructureComparator instanceof IDisposable) {
						IDisposable disposable = (IDisposable) fStructureComparator;
						disposable.dispose();
					}
					fStructureComparator= null;
				}
				if (fInput instanceof IContentChangeNotifier)
					((IContentChangeNotifier)fInput).addContentChangeListener(fContentChangedListener);
			}
			return changed;
		}
		
		public IStructureComparator getStructureComparator() {
			return fStructureComparator;
		}

		public void refresh() {
			IStructureComparator oldComparator = fStructureComparator;
			fStructureComparator= createStructure();
			// Dispose of the old one after in case they are using a shared document
			if (oldComparator instanceof IDisposable) {
				IDisposable disposable = (IDisposable) oldComparator;
				disposable.dispose();
			}
		}

		public Object getInput() {
			return fInput;
		}
		
		private IStructureComparator createStructure() {
			if (fStructureCreator instanceof IStructureCreator2) {
				IStructureCreator2 sc2 = (IStructureCreator2) fStructureCreator;
				try {
					return sc2.createStructure(fInput);
				} catch (CoreException e) {
					CompareUIPlugin.log(e);
				}
			}
			return fStructureCreator.getStructure(fInput);
		}

		public void dispose() {
			if (fStructureComparator instanceof IDisposable) {
				IDisposable disposable = (IDisposable) fStructureComparator;
				disposable.dispose();
			}
		}
	}
	
	/**
	 * Creates a new viewer for the given SWT tree control with the specified configuration.
	 *
	 * @param tree the tree control
	 * @param configuration the configuration for this viewer
	 */
	public StructureDiffViewer(Tree tree, CompareConfiguration configuration) {
		super(tree, configuration);
		Composite c= tree.getParent();
		if (c instanceof CompareViewerSwitchingPane)
			fParent= (CompareViewerSwitchingPane) c;
		initialize();
	}
	
	/**
	 * Creates a new viewer under the given SWT parent with the specified configuration.
	 *
	 * @param parent the SWT control under which to create the viewer
	 * @param configuration the configuration for this viewer
	 */
	public StructureDiffViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration);
		if (parent instanceof CompareViewerSwitchingPane)
			fParent= (CompareViewerSwitchingPane) parent;
		initialize();
	}
	
	private void initialize() {
		
		setAutoExpandLevel(3);
		
		fContentChangedListener= new IContentChangeListener() {
			public void contentChanged(IContentChangeNotifier changed) {
				StructureDiffViewer.this.contentChanged(changed);
			}
		};
		new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput input) {
				StructureDiffViewer.this.compareInputChanged(input);
			}
		};
	}
	
	/**
	 * Configures the <code>StructureDiffViewer</code> with a structure creator.
	 * The structure creator is used to create a hierarchical structure
	 * for each side of the viewer's input element of type <code>ICompareInput</code>.
	 *
	 * @param structureCreator the new structure creator
	 */
	public void setStructureCreator(IStructureCreator structureCreator) {
		if (fStructureCreator != structureCreator) {
			fStructureCreator= structureCreator;
			Control tree= getControl();
			if (tree != null && !tree.isDisposed())
				tree.setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle());
		}
	}
	
	/**
	 * Returns the structure creator or <code>null</code> if no
	 * structure creator has been set with <code>setStructureCreator</code>.
	 *
	 * @return the structure creator or <code>null</code>
	 */
	public IStructureCreator getStructureCreator() {
		return fStructureCreator;
	}
	
	/**
	 * Reimplemented to get the descriptive title for this viewer from the <code>IStructureCreator</code>.
	 * @return the viewer's name
	 */
	public String getTitle() {
		if (fStructureCreator != null)
			return fStructureCreator.getName();
		return super.getTitle();
	}
	
	/**
	 * Overridden because the input of this viewer is not identical to the root of the tree.
	 * The tree's root is a IDiffContainer that was returned from the method <code>diff</code>.
	 * 
	 * @return the root of the diff tree produced by method <code>diff</code>
	 */
	protected Object getRoot() {
		return fRoot;
	}
	
    /*
     * (non-Javadoc) Method declared on StructuredViewer.
     * Overridden to create the comparable structures from the input object
	 * and to feed them through the differencing engine. Note: for this viewer
	 * the value from <code>getInput</code> is not identical to <code>getRoot</code>.
	 */
	protected void inputChanged(Object input, Object oldInput) {
		if (input instanceof ICompareInput) {
			compareInputChanged((ICompareInput) input);
			if (input != oldInput)
				initialSelection();
		}
	}
	
	protected void initialSelection() {
		expandToLevel(2);
	}

	/* (non Javadoc)
	 * Overridden to unregister all listeners.
	 */
	protected void handleDispose(DisposeEvent event) {
		
		compareInputChanged(null);
		
		fContentChangedListener= null;
				
		super.handleDispose(event);
	}
	
	/**
	 * Recreates the comparable structures for the input sides.
	 * @param input this viewer's new input
	 */
	protected void compareInputChanged(ICompareInput input) {
		ITypedElement t= null;
		boolean changed= false;
		
		if (input != null)
			t= input.getAncestor();
		fThreeWay= (t != null);
		if (fAncestorStructure.setInput(t))
			changed = true;
		
		if (input != null)
			t= input.getLeft();
		if (fLeftStructure.setInput(t))
			changed = true;
		
		if (input != null)
			t= input.getRight();
		if (fRightStructure.setInput(t))
			changed = true;
		
		if (changed)
			diff();
	}
	
	/**
	 * Calls <code>diff</code> whenever the byte contents changes.
	 * @param changed the object that sent out the notification
	 */
	protected void contentChanged(IContentChangeNotifier changed) {
		
		if (fStructureCreator == null)
			return;
			
		if (changed != null) {
			if (changed == fAncestorStructure.getInput()) {
				fAncestorStructure.refresh();
			} else if (changed == fLeftStructure.getInput()) {
				fLeftStructure.refresh();
			} else if (changed == fRightStructure.getInput()) {
				fRightStructure.refresh();
			} else
				return;
		} else {
			fAncestorStructure.refresh();
			fLeftStructure.refresh();
			fRightStructure.refresh();
		}
		
		diff();
	}

	/**
	 * This method is called from within <code>diff()</code> before the difference
	 * tree is being built.
	 * Clients may override this method to perform their own pre-processing.
	 * This default implementation does nothing.
	 * @param ancestor the ancestor input to the differencing operation
	 * @param left the left input to the differencing operation
	 * @param right the right input to the differencing operation
	 * @since 2.0
	 */
	protected void preDiffHook(IStructureComparator ancestor, IStructureComparator left, IStructureComparator right) {
		// we do nothing here
	}
	
	/**
	 * Runs the difference engine and refreshes the tree.
	 */
	protected void diff() {
		
		IStructureComparator ancestorComparator = fAncestorStructure.getStructureComparator();
		IStructureComparator leftComparator = fLeftStructure.getStructureComparator();
		IStructureComparator rightComparator = fRightStructure.getStructureComparator();
		
		preDiffHook(ancestorComparator, 
				leftComparator, 
				rightComparator);
							
		String message= null;
		
		if ((fThreeWay && ancestorComparator == null) || leftComparator == null || rightComparator == null) {
			// could not get structure of one (or more) of the legs
			fRoot= null;
			message= CompareMessages.StructureDiffViewer_StructureError;	
			
		} else {	// calculate difference of the two (or three) structures

			if (fDifferencer == null)
				fDifferencer= new Differencer() {
					protected boolean contentsEqual(Object o1, Object o2) {
						return StructureDiffViewer.this.contentsEqual(o1, o2);
					}
					protected Object visit(Object data, int result, Object ancestor, Object left, Object right) {
						Object o= super.visit(data, result, ancestor, left, right);
						if (fLeftIsLocal && o instanceof DiffNode)
							((DiffNode)o).swapSides(fLeftIsLocal);
						return o;
					}
				};
			
			fRoot= (IDiffContainer) fDifferencer.findDifferences(fThreeWay, null, null,
					ancestorComparator, leftComparator, rightComparator);
					
			if (fRoot == null || fRoot.getChildren().length == 0) {
				message= CompareMessages.StructureDiffViewer_NoStructuralDifferences;	
			} else {
				postDiffHook(fDifferencer, fRoot);
			}
		}
		if (fParent != null)
			fParent.setTitleArgument(message);
			
		refresh(getRoot());
	}
	
	/**
	 * This method is called from within <code>diff()</code> after the difference
	 * tree has been built.
	 * Clients may override this method to perform their own post-processing.
	 * This default implementation does nothing.
	 * @param differencer the differencer used to perform the differencing
	 * @param root the non-<code>null</code> root node of the difference tree
	 * @since 2.0
	 */
	protected void postDiffHook(Differencer differencer, IDiffContainer root) {
		// we do nothing here
	}
	
	/*
	 * Performs a byte compare on the given objects.
	 * Called from the difference engine.
	 * Returns <code>null</code> if no structure creator has been set.
	 */
	private boolean contentsEqual(Object o1, Object o2) {
		if (fStructureCreator != null) {
			boolean ignoreWhiteSpace= Utilities.getBoolean(getCompareConfiguration(), CompareConfiguration.IGNORE_WHITESPACE, false);		
			String s1= fStructureCreator.getContents(o1, ignoreWhiteSpace);
			String s2= fStructureCreator.getContents(o2, ignoreWhiteSpace);
			if (s1 == null || s2 == null)
				return false;
			return s1.equals(s2);
		}
		return false;
	}
	
	/**
	 * Tracks property changes of the configuration object.
	 * Clients may override to track their own property changes.
	 * In this case they must call the inherited method.
	 * @param event the property changed event that triggered the call to this method
	 */
	protected void propertyChange(PropertyChangeEvent event) {
		String key= event.getProperty();
		if (key.equals(CompareConfiguration.IGNORE_WHITESPACE))
			diff();
		else
			super.propertyChange(event);
	}
		
	/**
	 * Overridden to call the <code>save</code> method on the structure creator after
	 * nodes have been copied from one side to the other side of an input object.
	 *
	 * @param leftToRight if <code>true</code> the left side is copied to the right side.
	 * If <code>false</code> the right side is copied to the left side
	 */
	protected void copySelected(boolean leftToRight) {
		super.copySelected(leftToRight);
		
		if (fStructureCreator != null)
			fStructureCreator.save(
							leftToRight ? fRightStructure.getStructureComparator() : fLeftStructure.getStructureComparator(),
							leftToRight ? fRightStructure.getInput() : fLeftStructure.getInput());
	}
}

