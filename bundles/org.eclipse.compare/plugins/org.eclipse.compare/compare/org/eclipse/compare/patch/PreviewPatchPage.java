/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.io.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;

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
			return "no name";
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
	private Text fFuzzField;
	
	private Image fNullImage;
	private Image fAddImage;
	private Image fDelImage;
	
	private CompareConfiguration fCompareConfiguration;
	
	
	/* package */ PreviewPatchPage(PatchWizard pw) {
		super("PreviewPatchPage", "Verify Patch", null);
		
		setMessage(
			"The tree shows the contents of the patch. " +
			"A checked item indicates that a patch could be applied succesfully.\n" +
			"Uncheck an item if you want to exclude it.");
		
		fPatchWizard= pw;
		//setPageComplete(false);
		
		int w= 16;
		fNullImage= new DiffImage(null, null, w).createImage();
		fAddImage= new DiffImage(null, CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif"), w).createImage();
		fDelImage= new DiffImage(null, CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif"), w).createImage();
		
		fCompareConfiguration= new CompareConfiguration();
		
		fCompareConfiguration.setLeftEditable(false);
		fCompareConfiguration.setLeftLabel("Original");
		
		fCompareConfiguration.setRightEditable(false);
		fCompareConfiguration.setRightLabel("Result");
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
		switch (diff.getType()) {
		case Differencer.ADDITION:
			return fAddImage;
		case Differencer.DELETION:
			return fDelImage;
		}
		return fNullImage;
	}
	
	public void createControl(Composite parent) {
				
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		
		buildPatchOptionsGroup(composite);
		
		// top pane showing diffs and hunks in a check box tree 
		fTree= new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd= new GridData();
		gd.verticalAlignment= GridData.FILL;
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace= true;
		fTree.setLayoutData(gd);
				
		// bottom pane showing hunks in compare viewer 
		fHunkViewer= new CompareViewerSwitchingPane(composite, SWT.BORDER) {
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
						boolean enabled= ti.getChecked();
						if (data instanceof Hunk) {
							((Hunk)data).fIsEnabled= enabled;
							updateGrayedState(ti);
						} else if (data instanceof Diff) {
							((Diff)data).fIsEnabled= enabled;
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
		group.setText("Patch Options");
		GridLayout layout= new GridLayout();
		layout.numColumns= 7;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		//fPatchFileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		new Label(group, SWT.NONE).setText("Ignore leading path name segments:");

		fStripPrefixSegments= new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
		int prefixCnt= patcher.getStripPrefixSegments();
		String prefix= Integer.toString(prefixCnt);
		fStripPrefixSegments.add(prefix);
		fStripPrefixSegments.setText(prefix);
		
		addSpacer(group);
		
		Label l= new Label(group, SWT.NONE);
		l.setText("Maximum fuzz factor:");
		l.setToolTipText("Allow context to shift this number of lines from the original place");
		fFuzzField= new Text(group, SWT.BORDER);
		fFuzzField.setText("2");
		GridData gd2= new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gd2.widthHint= 30;
		fFuzzField.setLayoutData(gd2);

		addSpacer(group);
		
		fIgnoreWhitespaceButton= new Button(group, SWT.CHECK);
		fIgnoreWhitespaceButton.setText("Ignore Whitespace");
		
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
			String error= null;
											
			IFile file= null;
			if (diff.getType() == Differencer.ADDITION) {
				IPath p= diff.fNewPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= existsInSelection(p);
				if (file == null) {
					diff.fIsEnabled= true;
				} else {
					// file already exists
					diff.fIsEnabled= false;					
					error= "(file already exists)";
				}
			} else {
				IPath p= diff.fOldPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= existsInSelection(p);
				diff.fIsEnabled= file != null;
				if (file != null) {
					diff.fIsEnabled= true;
				} else {
					// file doesn't exist
					diff.fIsEnabled= false;					
					error= "(file doesn't exist)";
				}
			}			
			
			boolean checked= false;
				
			ArrayList failedHunks= new ArrayList();		// collect rejected hunks here
			
			java.util.List lines= null;
			InputStream is= null;
			try {
				if (file != null) {
					is= file.getContents();
					BufferedReader reader= new BufferedReader(new InputStreamReader(is));
					lines= new LineReader(reader).readLines();
				}
			} catch(CoreException ex) {
				System.out.println("CoreException: " + ex);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch(IOException ex) {
					}
			}
			
			if (lines == null)
				lines= new ArrayList();
			fPatchWizard.getPatcher().patch(diff, lines, failedHunks);
			
			if (! failedHunks.isEmpty()) {
				StringBuffer sb= new StringBuffer();
				Iterator iter= failedHunks.iterator();
				while (iter.hasNext()) {
					Hunk hunk= (Hunk) iter.next();
					sb.append(hunk.getDescription());
					sb.append('\n');
					sb.append(hunk.getContent());
				}
				diff.fRejected= sb.toString();
			}
			
			int checkedSubs= 0;	// counts checked hunk items
			TreeItem[] hunkItems= item.getItems();
			for (int h= 0; h < hunkItems.length; h++) {
				Hunk hunk= (Hunk) hunkItems[h].getData();
				boolean failed= failedHunks.contains(hunk);
				String hunkError= null;
				if (failed)
					hunkError= "(no match)";
				hunk.fIsEnabled= diff.fIsEnabled && !failed;
				hunkItems[h].setChecked(hunk.fIsEnabled);
				if (hunk.fIsEnabled) {
					checkedSubs++;
					checked= true;
				}
				String hunkLabel= hunk.getDescription();
				if (hunkError != null)
					hunkLabel+= "   " + hunkError;
				hunkItems[h].setText(hunkLabel);
			}
			
			String label= diff.getDescription(strip);
			if (error != null)
				label+= "    " + error;
			item.setText(label);
			item.setChecked(checked);
			boolean gray= (checkedSubs > 0 &&  checkedSubs < hunkItems.length);
			item.setGrayed(gray);
			item.setExpanded(gray);
		}
	}
	
	/**
	 * Updates the gray state of the given diff and the checked state of its children.
	 */
	private void updateCheckedState(TreeItem diff) {
		boolean checked= diff.getChecked();
		diff.setGrayed(false);
		TreeItem[] hunks= diff.getItems();
		for (int i= 0; i < hunks.length; i++)
			hunks[i].setChecked(checked);
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
}
