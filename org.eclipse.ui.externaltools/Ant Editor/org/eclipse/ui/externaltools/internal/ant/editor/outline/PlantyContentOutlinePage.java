package org.eclipse.ui.externaltools.internal.ant.editor.outline;

/**********************************************************************
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyException;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.IAntEditorConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlAttribute;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Content outline page for planty.
 */
public class PlantyContentOutlinePage extends ContentOutlinePage {
	
	private IFile file;
	private Menu menu;
	private IAction openEditorAction;
	
	/**
	 * The content provider for the objects shown in the outline view.
	 */
	private class PlantyContentProvider implements ITreeContentProvider {

		/**
		 * do nothing
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			if (menu != null) {
				menu.dispose();
			}
		}

        
		/**
		 * do nothing
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
        

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentNode) {
			XmlElement tempParentElement = (XmlElement)parentNode;
			List tempChilds = tempParentElement.getChildNodes();
			Object[] tempChildObjects = new Object[tempChilds.size()];
			for(int i=0; i<tempChilds.size(); i++) {
				tempChildObjects[i] = (Object)tempChilds.get(i);
			}
			return tempChildObjects;
		}


		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object aNode) {
			XmlElement tempElement = (XmlElement)aNode;
			return tempElement.getParentNode();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object aNode) {
			return ((XmlElement)aNode).getChildNodes().size() > 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object anInputElement) {
			return getChildren(anInputElement);
		}

	}
    
	/**
	 * The label provider for the objects shown in the outline view.
	 */
	private class PlantyLabelProvider implements ILabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}


		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
		 */
		public Image getImage(Object anElement) {
			XmlElement tempElement = (XmlElement)anElement;
			if("target".equals(tempElement.getName())) { //$NON-NLS-1$
				XmlElement projectNode= tempElement.getParentNode();
				XmlAttribute attribute= projectNode.getAttributeNamed(IAntEditorConstants.ATTR_DEFAULT);
				String defaultTarget= ""; //$NON-NLS-1$
				if (attribute != null) {
					defaultTarget= attribute.getValue();
				}
				if (tempElement.getAttributeNamed(IAntEditorConstants.ATTR_NAME).getValue().equals(defaultTarget)) {
					//TODO:replace with default target icon when available Bug 29815
					return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_TARGET);
				} else {
					return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_TARGET);
				}
			}
			if("project".equals(tempElement.getName())) { //$NON-NLS-1$
				if (tempElement.isErrorNode()) {
					return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT_ERROR);
				} else {
					return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT);
				}
			}
			
			if (tempElement.isErrorNode()) {
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ERROR);
			}
			if("property".equals(tempElement.getName())) { //$NON-NLS-1$
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_PROPERTY);
			}
			
			XmlAttribute attribute= tempElement.getAttributeNamed(IAntEditorConstants.ATTR_TYPE);
			if (attribute.getValue().equals(IAntEditorConstants.TYPE_EXTERNAL)) {
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT);
			}
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_TASK);
		}
        
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
		 */
		public String getText(Object aNode) {
			XmlElement element= (XmlElement) aNode;
			StringBuffer displayName= new StringBuffer(element.getDisplayName());
			if (element.isExternal() && !element.isRootExternal()) {
				displayName.append(AntOutlineMessages.getString("PlantyContentOutlinePage._[external]_1")); //$NON-NLS-1$
			}
			return displayName.toString();
		}
	}
	
	private boolean isDefaultTargetNode(XmlElement node) {
		XmlAttribute type= node.getAttributeNamed(IAntEditorConstants.ATTR_TYPE);
		if (type == null || !type.getValue().equals(IAntEditorConstants.TYPE_TARGET)) {
			return false;
		}
		XmlElement parent= node.getParentNode();
		if (parent != null) {
			type= parent.getAttributeNamed(IAntEditorConstants.ATTR_TYPE);
			while (parent != null && !type.getValue().equals(IAntEditorConstants.TYPE_PROJECT)){
				parent= parent.getParentNode();
				type= parent.getAttributeNamed(IAntEditorConstants.ATTR_TYPE);
			}
		} 
		if (parent == null) {
			return false;
		}
		
		XmlAttribute defaultTarget= parent.getAttributeNamed(IAntEditorConstants.ATTR_DEFAULT);
		XmlAttribute nameAttribute= node.getAttributeNamed(IAntEditorConstants.ATTR_NAME);
		return defaultTarget != null && nameAttribute != null && defaultTarget.getValue().equals(nameAttribute.getValue());
	}
   
	/**
	 * Creates a new PlantyContentOutlinePage.
	 */
	public PlantyContentOutlinePage(IFile aFile) {
		super();
		this.file = aFile;
	}

	/**  
	 * Creates the control (outline view) for this page
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
    
		TreeViewer viewer = getTreeViewer();
        
		/*
		 * We might want to implement our own content provider.
		 * This content provider should be able to work on a dom like tree
		 * structure that resembles the file contents.
		 */
		viewer.setContentProvider(new PlantyContentProvider());

		/*
		 * We probably also need our own label provider.
		 */ 
		viewer.setLabelProvider(new PlantyLabelProvider());
		viewer.setInput(getContentOutline(file));
		viewer.expandToLevel(2);
		
		MenuManager manager= new MenuManager("#PopUp"); //$NON-NLS-1$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		menu= manager.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);

		IPageSite site= getSite();
		site.registerContextMenu(IExternalToolConstants.PLUGIN_ID + ".antEditorOutline", manager, viewer); //$NON-NLS-1$
				
		updateColor();
		openEditorAction= new OpenEditorForExternalEntityAction(this);
		
		viewer.getTree().addTreeListener(new TreeListener() {
			public void treeCollapsed(TreeEvent e) {
			}

			public void treeExpanded(TreeEvent e) {
				updateColor((TreeItem)e.item);
			}
		});
	}
	
	private void contextMenuAboutToShow(IMenuManager menu) {	
		if (openEditorAction.isEnabled()) {
			menu.add(openEditorAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
    
	/**
	 * Gets the content outline for a given input element.
	 * Returns the root XmlElement, or null if the
	 * outline could not be generated.
	 */
	private XmlElement getContentOutline(IAdaptable input) {
		/*
		 * What happens here:
		 * The file is parsed by the SAX Parser.
		 * The Parser then creates the DOM Tree of the file.
		 * The root element is returned here.
		 */
		IFile tempFile = (IFile)input;
         
		String tempWholeDocumentString = getFileContentAsString(tempFile);         
         
		// Create the parser
		SAXParser tempParser;
		try {
			SAXParserFactory tempSAXParserFactory = SAXParserFactory.newInstance();
			tempSAXParserFactory.setNamespaceAware(false);
			tempParser = tempSAXParserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (FactoryConfigurationError e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}

		// Create the handler
		OutlinePreparingHandler tempHandler = null;
		IPath location = tempFile.getLocation();
		try {
			File tempParentFile = null;
			if(location != null) {
				tempParentFile = location.toFile().getParentFile();
			}
			tempHandler = new OutlinePreparingHandler(tempParentFile);
            
		} catch (ParserConfigurationException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}
        
		// Parse!
		try {
			InputSource tempInputSource = new InputSource(new StringReader(tempWholeDocumentString));
			if (location != null) {
				//needed for resolving relative external entities
				tempInputSource.setSystemId(location.toOSString());
			}
			tempParser.setProperty("http://xml.org/sax/properties/lexical-handler", tempHandler); //$NON-NLS-1$
			tempParser.parse(tempInputSource, tempHandler);
		} catch(SAXParseException e) {
			// ignore that on purpose
		} catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (IOException e) {
			XmlElement tempRootElement = tempHandler.getRootElement();
			if (tempRootElement != null) {
				return generateExceptionOutline(tempWholeDocumentString, e, tempRootElement);
			} else {
				ExternalToolsPlugin.getDefault().log(e);		
			}
		}
        
		XmlElement tempRootElement = tempHandler.getRootElement();
		XmlElement tempElement = new XmlElement(""); //$NON-NLS-1$
        
		// Fix position of tags
		if(tempRootElement != null) {
			fixTagLocations(tempRootElement, tempWholeDocumentString);
			tempElement.addChildNode(tempRootElement);
		}
                
		return tempElement;
	}

	private XmlElement generateExceptionOutline(String wholeDocumentString, Exception e, XmlElement rootElement) {
		rootElement.setIsErrorNode(true);		
		XmlElement inputElement = new XmlElement(""); //$NON-NLS-1$
		
		// Fix position of tags
		fixTagLocations(rootElement, wholeDocumentString);
		inputElement.addChildNode(rootElement);
	
		XmlElement errorNode= new XmlElement(e.getMessage());
		errorNode.setIsErrorNode(true);
		rootElement.addChildNode(errorNode);
		return inputElement;
	}
    
	/**
	 * Fixes the starting positions and offset of all tags after parsing.
	 */
	private void fixTagLocations(XmlElement aRootElement, String aWholeDocumentString) {
		BufferedReader tempReader = new BufferedReader(new StringReader(aWholeDocumentString));

		// Fill an array of all lines
		ArrayList tempLineList = new ArrayList();
		String tempLine;
		try {
			while( (tempLine = tempReader.readLine()) != null) {
				tempLineList.add(tempLine);
			}
		} catch (IOException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return;
		}
				
		if(tempLineList.isEmpty()) {
			return;
		}
				
		// Determine all line lengths
		int [] tempLineLengths = new int [tempLineList.size()];
		tempLineLengths[0] = ((String)tempLineList.get(0)).length();
		for (int i = 1; i < tempLineLengths.length; i++) {
			tempLineLengths[i] = ((String)tempLineList.get(i)).length() + tempLineLengths[i-1];
		}

		XmlElement tempElement = aRootElement;
		while(tempElement != null) {
			/*
			 * Every element must have a starting row and column > 0 set, though
			 * it might be that the ending row and column are not set, because
			 * of a non-valid file
			 */
			int tempStartingRow = tempElement.getStartingRow(); // > 0
			int tempStartingColumn = tempElement.getStartingColumn(); // > 0
			tempLine = (String)tempLineList.get(--tempStartingRow);
			if(tempStartingColumn > 0) {
				try {
					tempLine = tempLine.substring(0, tempStartingColumn-1);
				}
				catch(StringIndexOutOfBoundsException e) {
					// This case happens due to a bug in the xerces parser.
					// The parser determines a wrong location of a tag in an xml document
					// if a string is spread over two lines.
					// i.e.: <javac classpath="bla;
					//           blub">
					//       </javac>
					tempLine = tempLine.substring(0, tempStartingColumn-2);
				}
			}
			int tempLessThanIndex = tempLine.lastIndexOf('<');
			while(tempLessThanIndex == -1) {
				/*
				 * must be found in this loop since, this element exists.
				 */
				tempLine = (String)tempLineList.get(--tempStartingRow);
				tempLessThanIndex = tempLine.lastIndexOf('<');
			}
            
			tempElement.setStartingColumn(tempLessThanIndex+1); // 0-based -> 1-based
			tempElement.setStartingRow(tempStartingRow+1);
			int tempStartingIndex = (tempStartingRow > 0 ? tempLineLengths[tempStartingRow-1] : 0);
			tempStartingIndex = (isLineSeparatorMulticharacter()) ? tempStartingIndex + tempStartingRow : tempStartingIndex; // add one char for ever \r
			tempStartingIndex += tempLessThanIndex;
			int tempOffset = tempStartingIndex;
			if (!tempElement.isErrorNode()) {
				tempOffset= findIndexOfSubstringAfterIndex(aWholeDocumentString, tempElement.getName(), tempStartingIndex)-1;
			}
			if(isLineSeparatorMulticharacter()) {
				tempOffset += tempStartingRow; // add one char for every \r
			}
			tempElement.setOffset(tempOffset);
			int tempLength;
			if(tempElement.getEndingRow() <= 0) {
				tempLength = -1;
			} else {
				tempStartingIndex = tempElement.getEndingRow() - 1;
				if(tempElement.getEndingColumn() > 0) {
					tempStartingIndex += tempElement.getEndingColumn();
				} else {
					tempStartingIndex += ((String)tempLineList.get(tempElement.getEndingRow()-1)).length();
				}
				tempStartingIndex += -2;
				if(tempElement.getEndingRow() > 1) {
					tempStartingIndex += tempLineLengths[tempElement.getEndingRow()-2];
				}
				tempLength = findIndexOfSubstringAfterIndex(aWholeDocumentString, ">", tempStartingIndex)+1; //$NON-NLS-1$
				tempLength -= tempOffset;
				if(isLineSeparatorMulticharacter()) {
					tempLength += tempStartingRow; // add one char for every
				}
			}
			tempElement.setLength(tempLength);
			tempElement = findNextElementToFixAfter(tempElement, true);
		}    
	}

	/**
	 * Returns whether current line separator is multicharacter
	 */
	private boolean isLineSeparatorMulticharacter() {
		String tempSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		if(tempSeparator.length() > 1) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the next element that is to be fixed.
	 * @param anElement the element that has just been fixed
	 * @param aProcessChildsFlag whether childs still need to be fixed or not
	 */
	private XmlElement findNextElementToFixAfter(XmlElement anElement, boolean aProcessChildsFlag) {
		List tempChildNodes = null;
        
		if(aProcessChildsFlag) {
			tempChildNodes = anElement.getChildNodes();
			if(tempChildNodes.size() > 0) {
				return (XmlElement)tempChildNodes.get(0);
			}
		}
        
		XmlElement tempParent = anElement.getParentNode();
		if(tempParent != null) {
			tempChildNodes = tempParent.getChildNodes();
			int tempIndex = tempChildNodes.indexOf(anElement);
			if(tempIndex + 1 < tempChildNodes.size()) {
				return (XmlElement)tempChildNodes.get(tempIndex + 1);
			}
			return findNextElementToFixAfter(tempParent, false);
		}
		return null;
	}

	/**
	 * Returns the content of the specified file as <code>String</code>.
	 * <P>
	 * Tabs will be converted to spaces according to the tab size.
	 */
	private String getFileContentAsString(IFile aFile) {
		InputStream tempStream;
		try {
			tempStream = aFile.getContents();
		} catch (CoreException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return ""; //$NON-NLS-1$
		}
        
		InputStreamReader tempReader;
		try {
			tempReader = new InputStreamReader(tempStream, ResourcesPlugin.getEncoding()); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return ""; //$NON-NLS-1$
		}
		BufferedReader tempBufferedReader = new BufferedReader(tempReader);

		StringBuffer tempResult = new StringBuffer();
		try {
			String tempLine;
			tempLine = tempBufferedReader.readLine();
        
			while(tempLine != null) {
				if(tempResult.length() != 0) {
					tempResult.append("\n"); //$NON-NLS-1$
				}
				tempResult.append(tempLine);
				tempLine = tempBufferedReader.readLine();
			}
		} catch (IOException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}

		return tempResult.toString();
	}

	/**
	 * Returns the index of the first occurence of <code>aSubString</code> in 
	 * <code>aSourceString</code> after <code>aStartIndex</code>.
	 * 
	 * @throws PlantyException if the specified sub string does not exist in
	 * the source string.
	 */
	private int findIndexOfSubstringAfterIndex(String aSourceString, String aSubString, int aStartIndex) {
		int tempIndex = aSourceString.indexOf(aSubString, aStartIndex);
		if(tempIndex == -1) {
			throw new PlantyException(MessageFormat.format(AntOutlineMessages.getString("PlantyContentOutlinePage.Substing_not_found"), new String[]{aSubString})); //$NON-NLS-1$
		}
		return tempIndex;
	}		

	/**
	 * Forces the page to update its contents.
	 */
	public void update() {
		getControl().setRedraw(false);
		getTreeViewer().setInput(getContentOutline(file));
		getTreeViewer().expandToLevel(2);
		updateColor();
		getControl().setRedraw(true);
	}
	
	/**
	 * Recolors the tree items for the default target coloring
	 */
	private void updateColor() {
		TreeItem[] items= getTreeViewer().getTree().getItems();
		for (int i = 0; i < items.length; i++) {
			updateColor(items[i]);
		}
	}
	
	/**
	 * Recolors a tree item and all its children for the default target coloring
	 */
	private void updateColor(TreeItem item) {
		if (item.getData() == null) {
			return;
		}
		if (isDefaultTargetNode((XmlElement) item.getData())) {
			item.setForeground(getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE));
		} else {
			item.setForeground(null);
		}
		TreeItem[] children= item.getItems();
		for (int i = 0; i < children.length; i++) {
			updateColor(children[i]);
		}
	}
}
