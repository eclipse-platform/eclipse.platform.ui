/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.composite.model;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetSaveHelper;
import org.eclipse.ui.internal.cheatsheets.data.IParserTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CompositeCheatSheetSaveHelper extends CheatSheetSaveHelper {
	/**
	 * Constructor 
	 */
	public CompositeCheatSheetSaveHelper() {
		super();
	}

	public IStatus loadCompositeState(CompositeCheatSheetModel model, IPath savePath) {
		if (savePath != null) {
			this.savePath = savePath;
	    } else {
		    this.savePath = Platform
		        .getPluginStateLocation(CheatSheetPlugin.getPlugin());
	    }

		Path filePath = getStateFile(model.getId());
		Document doc = null;
		URL readURL = null;

		try {
			readURL = filePath.toFile().toURL();
			doc = readXMLFile(readURL);
		} catch (MalformedURLException mue) {
			String message = NLS.bind(Messages.ERROR_CREATING_STATEFILE_URL,
					(new Object[] { readURL }));
			IStatus status = new Status(IStatus.ERROR,
					ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
					message, mue);
			return status;
		}

		if (doc == null) { 
			return Status.OK_STATUS;
		}
		Node rootnode = doc.getDocumentElement();
		NamedNodeMap rootatts = rootnode.getAttributes();
		if (isReference(doc)) {
			String path = getAttributeWithName(rootatts, IParserTags.PATH);
			return loadCompositeState(model,  new Path(path));
		}
		
		// The root node should be of type compositeCheatSheetState
		
		if (rootnode.getNodeName() != ICompositeCheatsheetTags.COMPOSITE_CHEATSHEET_STATE) {
			String message = NLS.bind(Messages.ERROR_PARSING_ROOT_NODE_TYPE, 
					(new Object[] {ICompositeCheatsheetTags.COMPOSITE_CHEATSHEET_STATE}));			
			IStatus status = new Status(IStatus.ERROR,
					ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
					message, null);
			return status;
		}
		
		NodeList children = rootnode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			String nodeName = children.item(i).getNodeName();
			if (ICompositeCheatsheetTags.TASK.equals(nodeName)) {
				return loadTaskState((CheatSheetTask)model.getRootTask(), children.item(i));
			}
		}  	
		return  new Status(IStatus.ERROR,
				ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
				Messages.ERROR_PARSING_NO_ROOT, null);
	}

	private IStatus loadTaskState(CheatSheetTask task, Node taskNode) {
		NamedNodeMap attributes = taskNode.getAttributes();
		Node state = attributes.getNamedItem(ICompositeCheatsheetTags.STATE);
		Node percentage = attributes.getNamedItem(ICompositeCheatsheetTags.PERCENTAGE_COMPLETE);
		if (state != null) {
			task.setState(Integer.parseInt(state.getNodeValue()));
		}
		if (percentage != null) {
			task.setPercentageComplete(Integer.parseInt(percentage.getNodeValue()));
		}
		
		NodeList children = taskNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (i < task.getSubtasks().length) {
				loadTaskState((CheatSheetTask)task.getSubtasks()[i], children.item(i));
			}
		}
		// TODO detect bad state
		return Status.OK_STATUS;
	}
	
	/**
	 * Save the state of a composite cheat sheet model
	 * @param model
	 * @return
	 */
	public IStatus saveCompositeState(CompositeCheatSheetModel model) {

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();

			Document doc = documentBuilder.newDocument();
			Element root = doc.createElement(ICompositeCheatsheetTags.COMPOSITE_CHEATSHEET_STATE);

			Path filePath = getStateFile(model.getId());
			
			root.setAttribute(IParserTags.ID, model.getId());
			doc.appendChild(root);
			
            saveTaskState(doc, root, (CheatSheetTask)model.getRootTask());

			StreamResult streamResult = new StreamResult(filePath.toFile());

			DOMSource domSource = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.transform(domSource, streamResult);
		} catch (Exception e) {
			String message = NLS.bind(Messages.ERROR_SAVING_STATEFILE_URL,
					(new Object[] { model.getId() }));
			IStatus status = new Status(IStatus.ERROR,
					ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
					message, e);
			return status;
		}
		return Status.OK_STATUS;
	}

	private void saveTaskState(Document doc, Element parent, CheatSheetTask task) {
		Element taskElement = doc.createElement(ICompositeCheatsheetTags.TASK);
		taskElement.setAttribute(ICompositeCheatsheetTags.STATE, Integer.toString(task.getState())); 
		taskElement.setAttribute(ICompositeCheatsheetTags.PERCENTAGE_COMPLETE, Integer.toString(task.getPercentageComplete())); 
		if (task.getId() != null) {
			taskElement.setAttribute(IParserTags.ID, task.getId());
		}
		ICompositeCheatSheetTask[] subtasks = task.getSubtasks();
		parent.appendChild(taskElement);
		for (int i = 0; i < subtasks.length; i++) {
			saveTaskState(doc, taskElement, (CheatSheetTask)subtasks[i]);
		}
	}

}
