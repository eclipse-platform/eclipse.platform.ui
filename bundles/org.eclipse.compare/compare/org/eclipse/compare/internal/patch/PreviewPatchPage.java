/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal.patch;

import java.io.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.*;


/**
 * Shows the parsed patch file and any mismatches
 * between files, hunks and the currently selected
 * resources.
 */
/* package */ class PreviewPatchPage extends WizardPage {
	
	/**
	 * Used with CompareInput
	 */
	static class HunkInput implements ITypedElement, IStreamContentAccessor {
		String fContent;
		String fType;
		
		HunkInput(String type, String s) {
			fType= type;
			fContent= s;
		}
		public Image getImage() {
			return null;
		}
		public String getName() {
			return PatchMessages.getString("PreviewPatchPage.NoName.text"); //$NON-NLS-1$
		}
		public String getType() {
			return fType;
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(fContent.getBytes());
		}
	};
		
	private PatchWizard fPatchWizard;
	
	private Tree fTree;
	private Combo fStripPrefixSegments;
	private CompareViewerSwitchingPane fHunkViewer;
	private Button fIgnoreWhitespaceButton;
	private Button fReversePatchButton;
	private Text fFuzzField;
	
	private Image fNullImage;
	private Image fAddImage;
	private Image fDelImage;
	private Image fErrorImage;
	private Image fErrorAddImage;
	private Image fErrorDelImage;
	
	private CompareConfiguration fCompareConfiguration;
	
	
	/* package */ PreviewPatchPage(PatchWizard pw) {
		super("PreviewPatchPage",	//$NON-NLS-1$ 
			PatchMessages.getString("PreviewPatchPage.title"), null); //$NON-NLS-1$
		
		setMessage(PatchMessages.getString("PreviewPatchPage.message"));	//$NON-NLS-1$
		
		fPatchWizard= pw;
		//setPageComplete(false);
		
		int w= 16;
		
		ImageDescriptor addId= CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif");	//$NON-NLS-1$
		ImageDescriptor delId= CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif");	//$NON-NLS-1$

		ImageDescriptor errId= CompareUIPlugin.getImageDescriptor("ovr16/error_ov.gif");	//$NON-NLS-1$
		Image errIm= errId.createImage();
		
		fNullImage= new DiffImage(null, null, w).createImage();
		fAddImage= new DiffImage(null, addId, w).createImage();
		fDelImage= new DiffImage(null, delId, w).createImage();

		fErrorImage= new DiffImage(errIm, null, w).createImage();
		fErrorAddImage= new DiffImage(errIm, addId, w).createImage();
		fErrorDelImage= new DiffImage(errIm, delId, w).createImage();
		
		fCompareConfiguration= new CompareConfiguration();
		
		fCompareConfiguration.setLeftEditable(false);
		fCompareConfiguration.setLeftLabel(PatchMessages.getString("PreviewPatchPage.Left.title")); //$NON-NLS-1$
		
		fCompareConfiguration.setRightEditable(false);
		fCompareConfiguration.setRightLabel(PatchMessages.getString("PreviewPatchPage.Right.title")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared in WizardPage
	 */
	public void setVisible(boolean visible) {
		if (visible)
			buildTree();
		super.setVisible(visible);
	}

	Image getImage(Diff diff) {
		if (diff.fMatches) {
			switch (diff.getType()) {
			case Differencer.ADDITION:
				return fAddImage;
			case Differencer.DELETION:
				return fDelImage;
			}
			return fNullImage;
		}
		switch (diff.getType()) {
		case Differencer.ADDITION:
			return fErrorAddImage;
		case Differencer.DELETION:
			return fErrorDelImage;
		}
		return fErrorImage;
	}
	
	Image getImage(Hunk hunk) {
		if (hunk.fMatches)
			return fNullImage;
		return fErrorImage;
	}
	
	public void createControl(Composite parent) {

		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		setControl(composite);
		
		buildPatchOptionsGroup(composite);
		
		Splitter splitter= new Splitter(composite, SWT.VERTICAL);
		splitter.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
					| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));

		
		// top pane showing diffs and hunks in a check box tree 
		fTree= new Tree(splitter, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd= new GridData();
		gd.verticalAlignment= GridData.FILL;
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace= true;
		fTree.setLayoutData(gd);
				
		// bottom pane showing hunks in compare viewer 
		fHunkViewer= new CompareViewerSwitchingPane(splitter, SWT.BORDER | SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				return CompareUI.findContentViewer(oldViewer, (ICompareInput)input, this, fCompareConfiguration);
			}
		};
		gd= new GridData();
		gd.verticalAlignment= GridData.FILL;
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace= true;
		fHunkViewer.setLayoutData(gd);
		
		// register listeners
		
		fTree.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TreeItem ti= (TreeItem) e.item;
					Object data= e.item.getData();
					if (e.detail == SWT.CHECK) {
						boolean checked= ti.getChecked();
						String s= ti.getText();
						if (data instanceof Hunk) {
							Hunk hunk= (Hunk) data;
							checked= checked && hunk.fMatches;
							//hunk.setEnabled(checked);
							ti.setChecked(checked);
							updateGrayedState(ti);
						} else if (data instanceof Diff) {
							updateCheckedState(ti);
						}
					} else {
						if (data instanceof Hunk)
							PreviewPatchPage.this.fHunkViewer.setInput(createInput((Hunk)data));
						else
							PreviewPatchPage.this.fHunkViewer.setInput(null);
					}
				}
			}
		);
		
		// creating tree's content
		buildTree();

		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer(this, PATCH_HELP_CONTEXT_ID));								
	}
	
	/**
	 *	Create the group for setting various patch options
	 */
	private void buildPatchOptionsGroup(Composite parent) {
				
		final Patcher patcher= fPatchWizard.getPatcher();
		
		Group group= new Group(parent, SWT.NONE);
		group.setText(PatchMessages.getString("PreviewPatchPage.PatchOptions.title")); //$NON-NLS-1$
		GridLayout layout= new GridLayout();
		layout.numColumns= 5;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		//fPatchFileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		// 1st row
		new Label(group, SWT.NONE).setText(PatchMessages.getString("PreviewPatchPage.IgnoreSegments.text")); //$NON-NLS-1$

		fStripPrefixSegments= new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
		int prefixCnt= patcher.getStripPrefixSegments();
		String prefix= Integer.toString(prefixCnt);
		fStripPrefixSegments.add(prefix);
		fStripPrefixSegments.setText(prefix);
		
		addSpacer(group);
		
		fReversePatchButton= new Button(group, SWT.CHECK);
		fReversePatchButton.setText(PatchMessages.getString("PreviewPatchPage.ReversePatch.text")); //$NON-NLS-1$
		
		addSpacer(group);
		
		// 2nd row
		Label l= new Label(group, SWT.NONE);
		l.setText(PatchMessages.getString("PreviewPatchPage.FuzzFactor.text")); //$NON-NLS-1$
		l.setToolTipText(PatchMessages.getString("PreviewPatchPage.FuzzFactor.tooltip")); //$NON-NLS-1$
		fFuzzField= new Text(group, SWT.BORDER);
		fFuzzField.setText("2"); //$NON-NLS-1$
		GridData gd2= new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gd2.widthHint= 30;
		fFuzzField.setLayoutData(gd2);

		addSpacer(group);
		
		fIgnoreWhitespaceButton= new Button(group, SWT.CHECK);
		fIgnoreWhitespaceButton.setText(PatchMessages.getString("PreviewPatchPage.IgnoreWhitespace.text")); //$NON-NLS-1$
		
		addSpacer(group);
				
		// register listeners
			
		if (fStripPrefixSegments != null) 
			fStripPrefixSegments.addSelectionListener(
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (patcher.setStripPrefixSegments(getStripPrefixSegments()))
							updateTree();
					}
				}
			);
		fReversePatchButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (patcher.setReversed(fReversePatchButton.getSelection()))
						updateTree();
				}
			}
		);
		fIgnoreWhitespaceButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (patcher.setIgnoreWhitespace(fIgnoreWhitespaceButton.getSelection()))
						updateTree();
				}
			}
		);
		
		fFuzzField.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (patcher.setFuzz(getFuzzFactor()))
						updateTree();
				}
			}
		);
	}
		
	ICompareInput createInput(Hunk hunk) {
		
		String[] lines= hunk.fLines;
		StringBuffer left= new StringBuffer();
		StringBuffer right= new StringBuffer();
		
		for (int i= 0; i < lines.length; i++) {
			String line= lines[i];
			String rest= line.substring(1);
			switch (line.charAt(0)) {
			case ' ':
				left.append(rest);
				right.append(rest);
				break;
			case '-':
				left.append(rest);
				break;
			case '+':
				right.append(rest);
				break;
			}
		}
		
		Diff diff= hunk.fParent;
		IPath path= diff.getPath();
		String type= path.getFileExtension();
		
		return new DiffNode(new HunkInput(type, left.toString()), new HunkInput(type, right.toString()));
	}		
	
	/**
	 * Builds a tree from list of Diffs.
	 * As a side effect it calculates the maximum number of segments
	 * in all paths.
	 */
	private void buildTree() {
		setPageComplete(true);
		if (fTree != null && !fTree.isDisposed()) {
			fTree.removeAll();
			fHunkViewer.setInput(null);
			
			int length= 99;
			
			Diff[] diffs= fPatchWizard.getPatcher().getDiffs();			
			if (diffs != null) {
				for (int i= 0; i < diffs.length; i++) {
					Diff diff= diffs[i];
					TreeItem d= new TreeItem(fTree, SWT.NULL);
					d.setData(diff);
					d.setImage(getImage(diff));
					
					if (diff.fOldPath != null)
						length= Math.min(length, diff.fOldPath.segmentCount());
					if (diff.fNewPath != null)
						length= Math.min(length, diff.fNewPath.segmentCount());
					
					java.util.List hunks= diff.fHunks;
					java.util.Iterator iter= hunks.iterator();
					while (iter.hasNext()) {
						Hunk hunk= (Hunk) iter.next();
						TreeItem h= new TreeItem(d, SWT.NULL);
						h.setData(hunk);
						h.setText(hunk.getDescription());
					}
				}
			}
			if (fStripPrefixSegments != null && length != 99)
				for (int i= 1; i < length; i++)
					fStripPrefixSegments.add(Integer.toString(i));
		}
		
		updateTree();
	}
	
	private IFile existsInSelection(IPath path) {
		IResource target= fPatchWizard.getTarget();
		if (target instanceof IFile) {
			IFile file= (IFile) target;
			IPath path2= file.getFullPath().removeFirstSegments(1);
			//System.out.println("target: " + path2.toOSString());
			//System.out.println("  path: " + path.toOSString());
			if (path.equals(path2))
				return file;
//			String name= file.getName();
//			if (path.lastSegment().equals(name))
//				return file;
		} else if (target instanceof IContainer) {
			IContainer c= (IContainer) target;
			if (c.exists(path))
				return c.getFile(path);
		}
		return null;
	}
	
	/**
	 * Updates label and checked state of tree items.
	 */
	private void updateTree() {
		if (fTree == null || fTree.isDisposed())
			return;
		int strip= getStripPrefixSegments();
		TreeItem[] children= fTree.getItems();
		for (int i= 0; i < children.length; i++) {
			TreeItem item= children[i];
			Diff diff= (Diff) item.getData();
			diff.fMatches= false;
			String error= null;
			
			boolean create= false;	
			IFile file= null;
			if (diff.getType() == Differencer.ADDITION) {
				IPath p= diff.fNewPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= existsInSelection(p);
				if (file == null) {
					diff.fMatches= true;
				} else {
					// file already exists
					error= PatchMessages.getString("PreviewPatchPage.FileExists.error"); //$NON-NLS-1$
				}
				create= true;
			} else {
				IPath p= diff.fOldPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= existsInSelection(p);
				diff.fMatches= file != null;
				if (file != null) {
					diff.fMatches= true;
				} else {
					// file doesn't exist
					error= PatchMessages.getString("PreviewPatchPage.FileDoesNotExist.error"); //$NON-NLS-1$
				}
			}			
							
			ArrayList failedHunks= new ArrayList();
			fPatchWizard.getPatcher().apply(diff, file, create, failedHunks);

			if (failedHunks.size() > 0)
				diff.fRejected= fPatchWizard.getPatcher().getRejected(failedHunks);
			
			int checkedSubs= 0;	// counts checked hunk items
			TreeItem[] hunkItems= item.getItems();
			for (int h= 0; h < hunkItems.length; h++) {
				Hunk hunk= (Hunk) hunkItems[h].getData();
				boolean failed= failedHunks.contains(hunk);
				String hunkError= null;
				if (failed)
					hunkError= PatchMessages.getString("PreviewPatchPage.NoMatch.error"); //$NON-NLS-1$

				boolean check= !failed;
				hunkItems[h].setChecked(check);
				if (check)
					checkedSubs++;

				String hunkLabel= hunk.getDescription();
				if (hunkError != null)
					hunkLabel+= "   " + hunkError; //$NON-NLS-1$
				hunkItems[h].setText(hunkLabel);
				hunkItems[h].setImage(getImage(hunk));
			}
			
			String label= diff.getDescription(strip);
			if (error != null)
				label+= "    " + error; //$NON-NLS-1$
			item.setText(label);
			item.setImage(getImage(diff));
			item.setChecked(checkedSubs > 0);
			boolean gray= (checkedSubs > 0 &&  checkedSubs < hunkItems.length);
			item.setGrayed(gray);
			item.setExpanded(gray);
		}
		setPageComplete(updateModel());
	}
	
	/**
	 * Updates the gray state of the given diff and the checked state of its children.
	 */
	private void updateCheckedState(TreeItem diffItem) {
		boolean checked= diffItem.getChecked();
		// check whether we can enable all hunks
		TreeItem[] hunks= diffItem.getItems();
		int checkedCount= 0;
		for (int i= 0; i < hunks.length; i++) {
			Hunk hunk= (Hunk) hunks[i].getData();
			if (checked) {
				if (hunk.fMatches) {
					hunks[i].setChecked(true);
					checkedCount++;
				}
			} else {
				hunks[i].setChecked(false);
			}
		}
		diffItem.setGrayed(checkedCount > 0 && checkedCount < hunks.length);
		diffItem.setChecked(checkedCount > 0);
		
		setPageComplete(updateModel());
	}
	
	/**
	 * Updates the gray state of the given items parent.
	 */
	private void updateGrayedState(TreeItem hunk) {
		TreeItem diff= hunk.getParentItem();
		TreeItem[] hunks= diff.getItems();
		int checked= 0;
		for (int i= 0; i < hunks.length; i++)
			if (hunks[i].getChecked())
				checked++;
		diff.setChecked(checked > 0);
		diff.setGrayed(checked > 0 && checked < hunks.length);
		
		setPageComplete(updateModel());
	}
	
	private void addSpacer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= 20;
		label.setLayoutData(gd);
	}
	
	private int getStripPrefixSegments() {
		int stripPrefixSegments= 0;
		if (fStripPrefixSegments != null) {
			String s= fStripPrefixSegments.getText();
			try {
				stripPrefixSegments= Integer.parseInt(s);
			} catch(NumberFormatException ex) {
			}
		}
		return stripPrefixSegments;
	}
	
	private int getFuzzFactor() {
		int fuzzFactor= 0;
		if (fFuzzField != null) {
			String s= fFuzzField.getText();
			try {
				fuzzFactor= Integer.parseInt(s);
			} catch(NumberFormatException ex) {
			}
		}
		return fuzzFactor;
	}
	
	public boolean updateModel() {
		boolean atLeastOneIsEnabled= false;
		if (fTree != null && !fTree.isDisposed()) {
			TreeItem [] diffItems= fTree.getItems();
			for (int i= 0; i < diffItems.length; i++) {
				TreeItem diffItem= diffItems[i];
				Object data= diffItem.getData();
				if (data instanceof Diff) {
					Diff diff= (Diff) data;
					boolean b= diffItem.getChecked();
					diff.setEnabled(b);
					if (b) {
						TreeItem [] hunkItems= diffItem.getItems();
						for (int j= 0; j < hunkItems.length; j++) {
							TreeItem hunkItem= hunkItems[j];
							data= hunkItem.getData();
							if (data instanceof Hunk) {
								Hunk hunk= (Hunk) data;
								b= hunkItem.getChecked();
								hunk.setEnabled(b);
								if (b) {
									atLeastOneIsEnabled= true;
								}
							}
						}
					}
				}
			}
		}
		return atLeastOneIsEnabled;
	}
}
