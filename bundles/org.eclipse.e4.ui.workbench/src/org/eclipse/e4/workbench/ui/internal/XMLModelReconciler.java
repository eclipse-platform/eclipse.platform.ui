/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MGenericTile;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.model.application.SideValue;
import org.eclipse.e4.workbench.modeling.IDelta;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLModelReconciler extends ModelReconciler {

	private static final String REFERENCE_ELEMENT_NAME = "reference"; //$NON-NLS-1$
	private static final String ORIGINALREFERENCE_ELEMENT_NAME = "originalReference"; //$NON-NLS-1$

	private static final String CHANGES_ATTNAME = "changes"; //$NON-NLS-1$

	private static final String CONTAINER_ATTNAME = "container"; //$NON-NLS-1$

	private static final String TYPE_ATTNAME = "type"; //$NON-NLS-1$

	private static final String UNSET_ATTNAME = "unset"; //$NON-NLS-1$
	private static final String UNSET_ATTVALUE_TRUE = "true"; //$NON-NLS-1$

	private ChangeRecorder changeRecorder;

	private ChangeDescription changeDescription;

	private EObject rootObject;

	private void collect(Collection<Object> objects, MElementContainer<?> container) {
		for (Object object : container.getChildren()) {
			if (object instanceof MPart) {
				MPart part = (MPart) object;
				for (MMenu menu : part.getMenus()) {
					objects.add(menu);
				}

				MToolBar toolBar = part.getToolbar();
				if (toolBar != null) {
					objects.add(toolBar);
				}
			}

			if (object instanceof MElementContainer<?>) {
				collect(objects, (MElementContainer<?>) object);
			}
		}
	}

	public void recordChanges(Object object) {
		Assert.isNotNull(object);
		rootObject = (EObject) object;

		Collection<Object> objects = new LinkedList<Object>();
		objects.add(object);

		if (object instanceof MApplication) {
			for (MWindow window : ((MApplication) object).getChildren()) {
				collect(objects, window);
			}
		}

		changeRecorder = new ChangeRecorder(objects);
		changeDescription = null;
	}

	private List<Object> getReferences(List<Object> references, Object object) {
		if (object instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) object).getChildren()) {
				getReferences(references, child);
			}
		}

		if (object instanceof MHandlerContainer) {
			for (Object child : ((MHandlerContainer) object).getHandlers()) {
				getReferences(references, child);
			}
		}

		if (object instanceof MBindingContainer) {
			for (Object child : ((MBindingContainer) object).getBindings()) {
				getReferences(references, child);
			}
		}

		if (object instanceof MPart) {
			for (Object child : ((MPart) object).getMenus()) {
				getReferences(references, child);
			}
		}

		if (object instanceof MApplication) {
			for (Object child : ((MApplication) object).getCommands()) {
				getReferences(references, child);
			}
		}

		references.add(object);

		return references;
	}

	public Collection<ModelDelta> constructDeltas(Object object, Object serializedState) {
		rootObject = (EObject) object;
		List<Object> references = getReferences(new LinkedList<Object>(), rootObject);

		Document document = (Document) serializedState;
		Element root = document.getDocumentElement();

		Collection<ModelDelta> deltas = new LinkedList<ModelDelta>();

		NodeList rootNodeList = (NodeList) root;
		for (int i = 0; i < rootNodeList.getLength(); i++) {
			Element element = (Element) rootNodeList.item(i);
			constructDeltas(deltas, references, rootObject, element);
		}

		return deltas;
	}

	private static EStructuralFeature getStructuralFeature(EObject object, String featureName) {
		if (featureName.equals(APPLICATIONELEMENT_ID_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getApplicationElement_Id();
		} else if (featureName.equals(APPLICATION_COMMANDS_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getApplication_Commands();
		} else if (featureName.equals(UILABEL_LABEL_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getUILabel_Label();
		} else if (featureName.equals(UILABEL_TOOLTIP_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getUILabel_Tooltip();
		} else if (featureName.equals(UILABEL_ICONURI_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getUILabel_IconURI();
		} else if (featureName.equals(UIELEMENT_TOBERENDERED_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getUIElement_ToBeRendered();
		} else if (featureName.equals(UIELEMENT_VISIBLE_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getUIElement_Visible();
		} else if (featureName.equals(ELEMENTCONTAINER_CHILDREN_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getElementContainer_Children();
		} else if (featureName.equals(UIELEMENT_PARENT_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getUIElement_Parent();
		} else if (featureName.equals(ELEMENTCONTAINER_ACTIVECHILD_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getElementContainer_ActiveChild();
		} else if (featureName.equals(WINDOW_X_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getWindow_X();
		} else if (featureName.equals(WINDOW_Y_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getWindow_Y();
		} else if (featureName.equals(WINDOW_WIDTH_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getWindow_Width();
		} else if (featureName.equals(WINDOW_HEIGHT_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getWindow_Height();
		} else if (featureName.equals(COMMAND_COMMANDNAME_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getCommand_CommandName();
		} else if (featureName.equals(COMMAND_DESCRIPTION_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getCommand_Description();
		} else if (featureName.equals(KEYSEQUENCE_KEYSEQUENCE_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getKeySequence_KeySequence();
		} else if (featureName.equals(BINDINGCONTAINER_BINDINGS_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getBindingContainer_Bindings();
		} else if (featureName.equals(HANDLER_COMMAND_ATTNAME)) {
			if (object instanceof MKeyBinding) {
				return MApplicationPackage.eINSTANCE.getKeyBinding_Command();
			} else if (object instanceof MHandler) {
				return MApplicationPackage.eINSTANCE.getHandler_Command();
			}
			return MApplicationPackage.eINSTANCE.getHandledItem_Command();
		} else if (featureName.equals(COMMAND_PARAMETERS_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getKeyBinding_Parameters();
		} else if (featureName.equals(ITEM_ENABLED_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getItem_Enabled();
		} else if (featureName.equals(ITEM_SELECTED_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getItem_Selected();
		} else if (featureName.equals(ITEM_SEPARATOR_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getItem_Separator();
		} else if (featureName.equals(PART_MENUS_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getPart_Menus();
		} else if (featureName.equals(PART_TOOLBAR_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getPart_Toolbar();
		} else if (featureName.equals(GENERICTILE_HORIZONTAL_ATTNAME)
				|| featureName.equals(TRIMCONTAINER_HORIZONTAL_ATTNAME)) {
			// technically the values are identical
			if (object instanceof MGenericTile<?>) {
				return MApplicationPackage.eINSTANCE.getGenericTile_Horizontal();
			}
			return MApplicationPackage.eINSTANCE.getTrimContainer_Horizontal();
		} else if (featureName.equals(TRIMCONTAINER_SIDE_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getTrimContainer_Side();
		} else if (featureName.equals(HANDLERCONTAINER_HANDLERS_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getHandlerContainer_Handlers();
		} else if (featureName.equals(CONTRIBUTION_PERSISTEDSTATE_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getContribution_PersistedState();
		} else if (featureName.equals(WINDOW_MAINMENU_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getWindow_MainMenu();
		} else if (featureName.equals(GENERICTILE_WEIGHTS_ATTNAME)) {
			return MApplicationPackage.eINSTANCE.getGenericTile_Weights();
		}
		return null;
	}

	private Object getValue(EStructuralFeature feature, String featureValue) {
		Class<?> instanceClass = feature.getEType().getInstanceClass();
		if (instanceClass == String.class) {
			return featureValue;
		} else if (instanceClass == int.class) {
			return Integer.valueOf(featureValue);
		} else if (instanceClass == boolean.class) {
			return Boolean.valueOf(featureValue);
		} else if (feature == MApplicationPackage.eINSTANCE.getTrimContainer_Side()) {
			return SideValue.getByName(featureValue);
		}
		return null;
	}

	private Collection<EStructuralFeature> collectFeatures(Collection<EStructuralFeature> features,
			EClass eClass) {
		features.addAll(eClass.getEStructuralFeatures());
		for (EClass superType : eClass.getESuperTypes()) {
			collectFeatures(features, superType);
		}
		return features;
	}

	private Collection<EStructuralFeature> collectFeatures(EObject object) {
		return collectFeatures(new HashSet<EStructuralFeature>(), object.eClass());
	}

	private boolean isReference(EStructuralFeature feature) {
		return feature == MApplicationPackage.eINSTANCE.getElementContainer_Children();
	}

	private Object findReference(List<Object> references, String id) {
		for (Object reference : references) {
			if (getLocalId(reference).equals(id)) {
				return reference;
			}
		}

		return null;
	}

	private boolean constructDeltas(Collection<ModelDelta> deltas, List<Object> references,
			EObject eObject, Element element) {
		String id = element.getAttribute(APPLICATIONELEMENT_ID_ATTNAME);
		if (eObject instanceof MApplicationElement || eObject instanceof MKeyBinding) {
			if (getLocalId(eObject).equals(id)) {
				constructObjectDeltas(deltas, references, eObject, element);
				return true;
			}
		}

		if (eObject instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) eObject).getChildren()) {
				if (constructDeltas(deltas, references, (EObject) child, element)) {
					return true;
				}
			}
		}

		if (eObject instanceof MBindingContainer) {
			for (MKeyBinding keyBinding : ((MBindingContainer) eObject).getBindings()) {
				if (constructDeltas(deltas, references, (EObject) keyBinding, element)) {
					return true;
				}
			}
		}

		if (eObject instanceof MHandlerContainer) {
			for (MHandler handler : ((MHandlerContainer) eObject).getHandlers()) {
				if (constructDeltas(deltas, references, (EObject) handler, element)) {
					return true;
				}
			}
		}

		if (eObject instanceof MApplication) {
			for (MCommand command : ((MApplication) eObject).getCommands()) {
				if (constructDeltas(deltas, references, (EObject) command, element)) {
					return true;
				}
			}
		}

		if (eObject instanceof MPart) {
			MPart part = (MPart) eObject;

			for (MMenu menu : part.getMenus()) {
				if (constructDeltas(deltas, references, (EObject) menu, element)) {
					return true;
				}
			}

			MToolBar toolBar = part.getToolbar();
			if (toolBar != null) {
				if (constructDeltas(deltas, references, (EObject) toolBar, element)) {
					return true;
				}
			}
		}

		if (eObject instanceof MWindow) {
			MWindow window = (MWindow) eObject;
			if (constructDeltas(deltas, references, (EObject) window.getMainMenu(), element)) {
				return true;
			}
		}

		return false;
	}

	private void constructObjectDeltas(Collection<ModelDelta> deltas, List<Object> references,
			EObject eObject, Element element) {
		Collection<EStructuralFeature> features = collectFeatures(eObject);

		NodeList nodeList = (NodeList) element;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element) nodeList.item(i);
			String featureName = node.getNodeName();
			if (isSingleReference(featureName)) {
				ModelDelta delta = createSingleReferenceDelta(references, eObject, features, node,
						featureName);
				deltas.add(delta);
			} else if (isChainedReference(featureName)) {
				ModelDelta delta = createMultiReferenceDelta(references, eObject, features, node,
						featureName);
				deltas.add(delta);
			} else if (isChainedAttribute(featureName)) {
				EStructuralFeature feature = getStructuralFeature(eObject, featureName);
				List<Object> values = new ArrayList<Object>();

				NodeList attributes = (NodeList) node;
				for (int j = 0; j < attributes.getLength(); j++) {
					Element attribute = (Element) attributes.item(j);
					Object value = getValue(feature, attribute.getAttribute(featureName));
					values.add(value);
				}

				ModelDelta delta = new EMFModelDeltaSet(eObject, feature, values);
				deltas.add(delta);
			} else {
				ModelDelta delta = createAttributeDelta(references, eObject, features, node,
						featureName);
				deltas.add(delta);
			}
		}
	}

	private ModelDelta createSingleReferenceDelta(List<Object> references, EObject eObject,
			Collection<EStructuralFeature> features, Element node, String featureName) {
		Object match = null;

		if (!Boolean.parseBoolean(node.getAttribute(UNSET_ATTNAME))) {
			NodeList referencedIds = (NodeList) node;
			for (int j = 0; j < referencedIds.getLength(); j++) {
				Element reference = (Element) referencedIds.item(j);
				String referenceId = reference.getAttribute(APPLICATIONELEMENT_ID_ATTNAME);
				match = findReference(references, referenceId);
				if (match != null) {
					break;
				}
			}

			if (match == null) {
				match = createObject((Element) referencedIds.item(0), references);
			}
		}

		EStructuralFeature feature = getStructuralFeature(eObject, featureName);
		features.remove(feature);

		return new EMFModelDeltaSet(eObject, feature, match);
	}

	public static Object threeWayMerge(List<?> originalReferences, List<?> userReferences,
			List<?> currentReferences) {
		int userSize = userReferences.size();
		int originalSize = originalReferences.size();

		if (userSize == 0) {
			// the user removed all the original parts
			List<Object> collectedReferences = new ArrayList<Object>(currentReferences);
			collectedReferences.removeAll(originalReferences);
			return collectedReferences;
		} else if (originalSize == 0) {
			List<Object> collectedReferences = new ArrayList<Object>(userReferences);
			collectedReferences.addAll(currentReferences);
			return collectedReferences;
		} else if (currentReferences.isEmpty()) {
			// currently not referencing anything, so just return what the user had exactly
			return userReferences;
		}

		List<Object> collectedReferences2 = new ArrayList<Object>(currentReferences);
		collectedReferences2.removeAll(originalReferences);

		List<Object> collectedReferences = new ArrayList<Object>(userReferences);
		collectedReferences.addAll(collectedReferences2);

		return collectedReferences;
	}

	private ModelDelta createMultiReferenceDelta(List<Object> references, EObject eObject,
			Collection<EStructuralFeature> features, Element node, String featureName) {
		NodeList referencedIds = (NodeList) node;
		EStructuralFeature feature = getStructuralFeature(eObject, featureName);

		if (feature == MApplicationPackage.eINSTANCE.getBindingContainer_Bindings()) {
			return createBindingReferenceDelta(references, eObject, features, referencedIds,
					feature);
		}
		return createIdentifiedReferenceDelta(references, eObject, features, referencedIds, feature);
	}

	private ModelDelta createBindingReferenceDelta(List<Object> references, EObject eObject,
			Collection<EStructuralFeature> features, NodeList referencedIds,
			EStructuralFeature feature) {
		List<Object> originalReferences = new ArrayList<Object>();
		List<Object> userReferences = new ArrayList<Object>();
		List<?> currentReferences = (List<?>) eObject.eGet(feature);

		for (int i = 0; i < referencedIds.getLength(); i++) {
			Element reference = (Element) referencedIds.item(i);
			String referenceId = reference.getAttribute(APPLICATIONELEMENT_ID_ATTNAME);
			Object match = findReference(references, referenceId);
			if (match != null) {
				if (reference.getNodeName().equals(REFERENCE_ELEMENT_NAME)) {
					Object eKeyBinding = createObject(reference, references);
					userReferences.add(eKeyBinding);
				} else {
					originalReferences.add(match);
				}
			} else {
				Object eKeyBinding = createObject(reference, references);
				userReferences.add(eKeyBinding);
			}
		}

		features.remove(feature);

		return new EMFModelDeltaThreeWayDelayedSet(eObject, feature, originalReferences,
				userReferences, currentReferences);
	}

	private static EObject createObject(String type) {
		if (type.equals(MPart.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createPart();
		} else if (type.equals(MCommand.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createCommand();
		} else if (type.equals(MHandler.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createHandler();
		} else if (type.equals(MKeyBinding.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createKeyBinding();
		} else if (type.equals(MMenu.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createMenu();
		} else if (type.equals(MWindow.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createWindow();
		} else if (type.equals(MToolBar.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createToolBar();
		} else if (type.equals(MMenuItem.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createMenuItem();
		} else if (type.equals(MPartStack.class.getSimpleName())) {
			return (EObject) MApplicationFactory.eINSTANCE.createPartStack();
		}
		return null;
	}

	private Object getReference(Element element, List<Object> references) {
		String id = element.getAttribute(APPLICATIONELEMENT_ID_ATTNAME);
		if (!id.equals("")) { //$NON-NLS-1$
			return findReference(references, id);
		}
		return createObject(element, references);
	}

	private IDelta createObject(String typeName, Element reference, List<Object> references) {
		EObject object = createObject(typeName);
		CompositeDelta compositeDelta = new CompositeDelta(object);

		NodeList elementAttributes = (NodeList) reference;
		for (int i = 0; i < elementAttributes.getLength(); i++) {
			Element item = (Element) elementAttributes.item(i);
			if (!Boolean.parseBoolean(item.getAttribute(UNSET_ATTNAME))) {
				String attributeName = item.getNodeName();
				EStructuralFeature attributeFeature = getStructuralFeature(object, attributeName);
				if (isSingleReference(attributeName)) {
					String id = item.getAttribute(attributeName);
					Object objectReference = findReference(references, id);

					IDelta delta = new EMFModelDeltaSet(object, attributeFeature, objectReference);
					compositeDelta.add(delta);
				} else if (isChainedReference(attributeName)) {
					List<Object> objectReferences = new ArrayList<Object>();
					NodeList objectReferenceNodes = (NodeList) item;

					for (int j = 0; j < objectReferenceNodes.getLength(); j++) {
						Node node = objectReferenceNodes.item(j);
						Object o = getReference((Element) node, references);
						objectReferences.add(o);
					}

					IDelta delta = new EMFModelDeltaSet(object, attributeFeature, objectReferences);
					compositeDelta.add(delta);
				} else {
					object.eSet(attributeFeature, getValue(attributeFeature, item
							.getAttribute(attributeName)));
				}
			}
		}

		return compositeDelta;
	}

	private Object createObject(Element reference, List<Object> references) {
		return createObject(reference.getAttribute(TYPE_ATTNAME), reference, references);
	}

	private ModelDelta createIdentifiedReferenceDelta(List<Object> references, EObject eObject,
			Collection<EStructuralFeature> features, NodeList referencedIds,
			EStructuralFeature feature) {
		List<Object> originalReferences = new ArrayList<Object>();
		List<Object> userReferences = new ArrayList<Object>();
		List<?> currentReferences = (List<?>) eObject.eGet(feature);

		for (int i = 0; i < referencedIds.getLength(); i++) {
			Element reference = (Element) referencedIds.item(i);
			if (UNSET_ATTVALUE_TRUE.equals(reference.getAttribute(UNSET_ATTNAME))) {
				userReferences.add(createObject(reference, references));
			} else {
				String referenceId = reference.getAttribute(APPLICATIONELEMENT_ID_ATTNAME);
				Object match = findReference(references, referenceId);
				if (match != null) {
					if (reference.getNodeName().equals(REFERENCE_ELEMENT_NAME)) {
						userReferences.add(match);
					} else {
						originalReferences.add(match);
					}
				}
			}
		}

		features.remove(feature);

		// Object merged = threeWayMerge(originalReferences, userReferences, currentReferences);
		// return new EMFModelDeltaSet(eObject, feature, merged);
		return new EMFModelDeltaThreeWayDelayedSet(eObject, feature, originalReferences,
				userReferences, currentReferences);
	}

	private ModelDelta createAttributeDelta(List<Object> references, EObject eObject,
			Collection<EStructuralFeature> features, Element node, String featureName) {
		EStructuralFeature feature = getStructuralFeature(eObject, featureName);
		features.remove(feature);

		if (Boolean.parseBoolean(node.getAttribute(UNSET_ATTNAME))) {
			return new EMFModelDeltaUnset(eObject, feature);
		}

		if (isReference(feature)) {
			Object reference = findReference(references, node
					.getAttribute(APPLICATIONELEMENT_ID_ATTNAME));
			return new EMFModelDeltaSet(eObject, feature, reference);
		}

		Object value = getValue(feature, node.getAttribute(featureName));
		return new EMFModelDeltaSet(eObject, feature, value);
	}

	private Document createDocument() {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
			return document;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object serialize() {
		calculateDeltas();

		Document document = createDocument();
		Element root = document.createElement(CHANGES_ATTNAME);
		document.appendChild(root);

		EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription.getObjectChanges();
		for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
			EObject object = entry.getKey();
			Element persistedElement = persist(document, entry, object);
			if (persistedElement != null) {
				root.appendChild(persistedElement);
			}
		}

		try {
			return document;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Element persist(Document document, Entry<EObject, EList<FeatureChange>> entry,
			EObject object) {
		if (getOriginalId(object) == null) {
			return null;
		}

		Element modelChange = null;

		List<FeatureChange> changes = entry.getValue();
		for (FeatureChange change : changes) {
			// we only want to persist deltas of non-transient features
			if (!change.getFeature().isTransient()) {
				String featureName = change.getFeatureName();
				if (shouldPersist(featureName)) {
					if (modelChange == null) {
						modelChange = createElement(document, object);
					}

					Element deltaElement = createDeltaElement(document, object, change, featureName);
					modelChange.appendChild(deltaElement);
				}
			}
		}

		return modelChange;
	}

	private Element createElement(Document document, EObject object) {
		String id = getOriginalId(object);
		Assert.isNotNull(id);

		Class<?> rootInterface = object.getClass().getInterfaces()[0];

		Element modelChange = document.createElement(rootInterface.getSimpleName());
		modelChange.setAttribute(APPLICATIONELEMENT_ID_ATTNAME, id);

		EObject container = object.eContainer();
		if (!(container instanceof ChangeDescription)) {
			modelChange.setAttribute(CONTAINER_ATTNAME, getOriginalId(container));
		}

		return modelChange;
	}

	private String getResourceId(EObject object, EObject container) {
		Resource resource = object.eResource();
		if (resource instanceof XMLResource) {
			return ((XMLResource) resource).getID(object);
		}

		resource = container.eResource();
		if (resource instanceof XMLResource) {
			return ((XMLResource) resource).getID(object);
		}

		return null;
	}

	private String getLocalId(Object object) {
		EObject reference = (EObject) object;

		String id = getResourceId(reference, reference.eContainer());
		if (id != null) {
			return id;
		}

		id = getModelId(reference);
		if (id != null) {
			return id;
		}

		EObject identifiableContainer = reference.eContainer();
		EObject lastValidContainer = identifiableContainer;
		while (identifiableContainer != null
				&& !(identifiableContainer instanceof ChangeDescription)) {
			id = getModelId(identifiableContainer);
			if (id != null) {
				return createId(reference, id, identifiableContainer);
			}

			lastValidContainer = identifiableContainer;
			identifiableContainer = identifiableContainer.eContainer();
		}

		return createId(reference, "application", lastValidContainer); //$NON-NLS-1$
	}

	private static List<MPart> getParts(List<MPart> parts, Object object) {
		if (object instanceof MPart) {
			parts.add((MPart) object);
		}

		if (object instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) object).getChildren()) {
				getParts(parts, child);
			}
		}

		return parts;
	}

	private String createId(EObject reference, String id, EObject identifiableContainer) {
		if (reference instanceof MApplication) {
			return id;
		}

		StringBuilder builder = new StringBuilder();
		EObject referenceContainer = reference.eContainer();

		boolean matched = false;

		List<MPart> parts = getParts(new LinkedList<MPart>(), rootObject);
		if (referenceContainer == null || referenceContainer instanceof ChangeDescription) {
			for (MPart part : parts) {
				if (insertMenuReference(reference, builder, part)) {
					reference = (EObject) part;
					referenceContainer = reference.eContainer();
					identifiableContainer = rootObject;
					break;
				} else if (part.getToolbar() == reference) {
					builder.insert(0, "/toolBar"); //$NON-NLS-1$
					reference = (EObject) part;
					referenceContainer = reference.eContainer();
					identifiableContainer = rootObject;
					break;
				}
			}
		}

		if (referenceContainer instanceof MElementContainer<?>) {
			EObject eContainer = referenceContainer.eContainer();
			if (referenceContainer instanceof MMenu && eContainer == null) {
				for (MPart part : parts) {
					if (part.getMenus().contains(referenceContainer)) {
						while (referenceContainer != null
								&& !(referenceContainer instanceof ChangeDescription)) {
							insertChildrenReference(reference, builder,
									((MMenu) referenceContainer).getChildren());
							reference = referenceContainer;
							referenceContainer = referenceContainer.eContainer();
						}

						if (insertMenuReference(reference, builder, part)) {
							reference = (EObject) part;
							referenceContainer = reference.eContainer();
							identifiableContainer = rootObject;
						}
						break;
					}
				}
			}

			matched = insertChildrenReference(reference, builder, referenceContainer);
		}

		if (!matched && referenceContainer instanceof MBindingContainer) {
			matched = insertBindingReference(reference, builder, referenceContainer);
		}

		if (!matched && referenceContainer instanceof MHandlerContainer) {
			matched = insertHandlerReference(reference, builder, referenceContainer);
		}

		if (!matched && referenceContainer instanceof MApplication) {
			matched = insertCommandReference(reference, builder, referenceContainer);
		}

		if (!matched && referenceContainer instanceof MWindow) {
			if (((MWindow) referenceContainer).getMainMenu() == reference) {
				builder.insert(0, "/mainMenu"); //$NON-NLS-1$
				matched = true;
			}
		}

		Assert.isTrue(matched);

		EObject container = referenceContainer;

		while (container != identifiableContainer) {
			container = referenceContainer.eContainer();
			if (container instanceof MElementContainer<?>) {
				matched = insertChildrenReference(referenceContainer, builder, container);
			}

			if (!matched && container instanceof MWindow) {
				if (((MWindow) container).getMainMenu() == referenceContainer) {
					builder.insert(0, "/mainMenu"); //$NON-NLS-1$
					matched = true;
				}
			}

			if (!matched && referenceContainer instanceof MBindingContainer) {
				matched = insertBindingReference(reference, builder, referenceContainer);
			}

			if (!matched && container instanceof MApplication) {
				matched = insertCommandReference(referenceContainer, builder, container);
			}

			referenceContainer = container;

			Assert.isTrue(matched);
			matched = false;
		}

		builder.insert(0, id);

		return builder.toString();
	}

	private static boolean insertReference(EObject reference, StringBuilder builder,
			List<?> children, String referenceTag) {
		for (int i = 0; i < children.size(); i++) {
			if (reference == children.get(i)) {
				builder.insert(0, i);
				builder.insert(0, '.');
				builder.insert(0, referenceTag);
				builder.insert(0, "/@"); //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	private static boolean insertChildrenReference(EObject reference, StringBuilder builder,
			EObject container) {
		return insertChildrenReference(reference, builder, ((MElementContainer<?>) container)
				.getChildren());
	}

	private static boolean insertChildrenReference(EObject reference, StringBuilder builder,
			List<?> children) {
		return insertReference(reference, builder, children, "children"); //$NON-NLS-1$
	}

	private static boolean insertBindingReference(EObject reference, StringBuilder builder,
			EObject container) {
		return insertBindingReference(reference, builder, ((MBindingContainer) container)
				.getBindings());
	}

	private static boolean insertBindingReference(EObject reference, StringBuilder builder,
			List<?> bindings) {
		return insertReference(reference, builder, bindings, "bindings"); //$NON-NLS-1$
	}

	private static boolean insertHandlerReference(EObject reference, StringBuilder builder,
			EObject container) {
		return insertHandlerReference(reference, builder, ((MHandlerContainer) container)
				.getHandlers());
	}

	private static boolean insertHandlerReference(EObject reference, StringBuilder builder,
			List<?> handlers) {
		return insertReference(reference, builder, handlers, "handlers"); //$NON-NLS-1$
	}

	private static boolean insertCommandReference(EObject reference, StringBuilder builder,
			EObject container) {
		return insertCommandReference(reference, builder, ((MApplication) container).getCommands());
	}

	private static boolean insertCommandReference(EObject reference, StringBuilder builder,
			List<?> commands) {
		return insertReference(reference, builder, commands, "commands"); //$NON-NLS-1$
	}

	private static boolean insertMenuReference(EObject reference, StringBuilder builder,
			List<?> menus) {
		return insertReference(reference, builder, menus, "menus"); //$NON-NLS-1$
	}

	private static boolean insertMenuReference(EObject reference, StringBuilder builder, MPart part) {
		return insertMenuReference(reference, builder, part.getMenus());
	}

	private EObject getOriginalContainer(EObject reference) {
		if (changeDescription == null) {
			return reference.eContainer();
		}

		if (reference instanceof MCommand) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean commandsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == rootObject) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(APPLICATION_COMMANDS_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							commandsChanged = true;
							break;
						}
					}
					break;
				}
			}

			if (!commandsChanged) {
				return reference.eContainer();
			}
		}

		if (reference instanceof MHandler) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean handlersChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key instanceof MHandlerContainer) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(HANDLERCONTAINER_HANDLERS_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							handlersChanged = true;
							break;
						}
					}
				}
			}

			if (!handlersChanged) {
				return reference.eContainer();
			}
		}

		if (reference instanceof MKeyBinding) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean bindingsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key instanceof MBindingContainer) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(BINDINGCONTAINER_BINDINGS_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							bindingsChanged = true;
							break;
						}
					}
				}
			}

			if (!bindingsChanged) {
				return reference.eContainer();
			}
		}

		if (reference instanceof MUIElement) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == reference) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(UIELEMENT_PARENT_ATTNAME)) {
							return (EObject) change.getValue();
						}
					}
					break;
				}
			}

			if (reference instanceof MMenu) {
				for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
					EObject key = entry.getKey();
					if (key instanceof MPart) {
						for (FeatureChange change : entry.getValue()) {
							if (change.getFeatureName().equals(PART_MENUS_ATTNAME)) {
								List<?> originalMenus = (List<?>) change.getValue();
								if (originalMenus.contains(reference)) {
									return key;
								}
								break;
							}
						}
					}
				}

				boolean menuSet = false;
				boolean menuChanged = false;

				for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
					EObject key = entry.getKey();
					if (key instanceof MWindow) {
						for (FeatureChange change : entry.getValue()) {
							if (change.getFeatureName().equals(WINDOW_MAINMENU_ATTNAME)) {
								Object oldMenu = change.getValue();
								if (oldMenu == reference) {
									return key;
								} else if (oldMenu == null
										&& ((MWindow) key).getMainMenu() == reference) {
									menuSet = true;
								}
								menuChanged = true;
								break;
							}
						}
					}
				}

				if (menuChanged && menuSet) {
					return null;
				}
			}

			boolean newElement = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == reference.eContainer()) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(ELEMENTCONTAINER_CHILDREN_ATTNAME)) {
							EList<?> value = (EList<?>) change.getValue();
							if (value.contains(reference)) {
								return key;
							}
							newElement = true;
							break;
						}
					}
					break;
				}
			}

			if (!newElement) {
				return reference instanceof MApplication ? changeDescription : reference
						.eContainer();
			}
		}

		return reference instanceof MApplication ? changeDescription : null;
	}

	/**
	 * Computes and returns the original id of the specified object. The meaning of the term
	 * "original" is defined as the state of the object prior to having its state monitored via
	 * {@link #recordChanges(Object)}.
	 * <p>
	 * If the object is an MApplicationElement, the id that has already been set will be used.
	 * However, if an id has not been set, one will be constructed and returned. As the lookup
	 * strategy relies on an id for finding objects, it is important that should the id have to be
	 * constructed internally that it store sufficient information for the reconciler to use for
	 * finding the original object that it constructed the id for.
	 * </p>
	 * <p>
	 * For example, in an application with two windows, the id that is constructed for the two
	 * windows may be:
	 * <ol>
	 * <li><code>application/@children.0</code></li>
	 * <li><code>application/@children.1</code></li>
	 * </ol>
	 * However, suppose that the positions of the windows are swapped at some point during the
	 * lifecycle of the application by the user. If an id is constructed now it will incorrectly
	 * point to the wrong windows. This is where the notion of an "original" id comes into play.
	 * This method <b>must</b> accurately identify an object prior to any state changes so that a
	 * delta can be calculated correctly.
	 * </p>
	 * 
	 * @param object
	 *            the object to query an id for
	 * @return an id suitable for looking up the object, or <code>null</code> if the object did not
	 *         exist or was not known within the scope of the originally monitored object
	 */
	private String getOriginalId(Object object) {
		EObject reference = (EObject) object;
		EObject container = getOriginalContainer(reference);

		// did not exist in the model originally
		if (container == null) {
			return null;
		}

		String id = getResourceId(reference, container);
		if (id != null) {
			return id;
		}

		if (changeDescription != null) {
			return getOriginalId(object, reference, container);
		}

		return getLocalId(object);
	}

	private String getOriginalId(Object object, EObject reference, EObject container) {
		EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription.getObjectChanges();
		for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
			EObject key = entry.getKey();
			if (key == container) {
				for (FeatureChange change : entry.getValue()) {
					String featureName = change.getFeatureName();
					if (featureName.equals(ELEMENTCONTAINER_CHILDREN_ATTNAME)
							&& reference instanceof MUIElement) {
						EList<?> originalChildren = (EList<?>) change.getValue();
						EList<?> currentChildren = ((MElementContainer<?>) container).getChildren();

						int index = originalChildren.indexOf(reference);
						if (index == -1) {
							return null;
						} else if (index == currentChildren.indexOf(reference)) {
							return getLocalId(object);
						} else {
							String id = getModelId(reference);
							if (id != null) {
								return id;
							}

							StringBuilder builder = new StringBuilder();
							insertChildrenReference(reference, builder, originalChildren);
							String localId = getOriginalId(container);
							builder.insert(0, localId);
							return builder.toString();
						}
					} else if (featureName.equals(APPLICATION_COMMANDS_ATTNAME)
							&& reference instanceof MCommand) {
						EList<?> originalCommands = (EList<?>) change.getValue();
						EList<?> currentCommands = ((MApplication) container).getCommands();

						int index = originalCommands.indexOf(reference);
						if (index == -1) {
							return null;
						} else if (index == currentCommands.indexOf(reference)) {
							return getLocalId(object);
						} else {
							String id = getModelId(reference);
							if (id != null) {
								return id;
							}

							StringBuilder builder = new StringBuilder();
							insertCommandReference(reference, builder, originalCommands);
							String localId = getOriginalId(container);
							builder.insert(0, localId);
							return builder.toString();
						}
					} else if (featureName.equals(HANDLERCONTAINER_HANDLERS_ATTNAME)
							&& reference instanceof MHandler) {
						EList<?> originalHandlers = (EList<?>) change.getValue();
						EList<?> currentHandlers = ((MHandlerContainer) container).getHandlers();

						int index = originalHandlers.indexOf(reference);
						if (index == -1) {
							return null;
						} else if (index == currentHandlers.indexOf(reference)) {
							return getLocalId(object);
						} else {
							String id = getModelId(reference);
							if (id != null) {
								return id;
							}

							StringBuilder builder = new StringBuilder();
							insertHandlerReference(reference, builder, originalHandlers);
							String localId = getOriginalId(container);
							builder.insert(0, localId);
							return builder.toString();
						}
					} else if (featureName.equals(BINDINGCONTAINER_BINDINGS_ATTNAME)
							&& reference instanceof MKeyBinding) {
						EList<?> originalBindings = (EList<?>) change.getValue();
						EList<?> currentBindings = ((MBindingContainer) container).getBindings();

						int index = originalBindings.indexOf(reference);
						if (index == -1) {
							return null;
						} else if (index == currentBindings.indexOf(reference)) {
							return getLocalId(object);
						} else {
							String id = getModelId(reference);
							if (id != null) {
								return id;
							}

							StringBuilder builder = new StringBuilder();
							insertBindingReference(reference, builder, originalBindings);
							String originalId = getOriginalId(container);
							builder.insert(0, originalId);
							return builder.toString();
						}
					} else if (featureName.equals(PART_MENUS_ATTNAME) && reference instanceof MMenu) {
						EList<?> originalMenus = (EList<?>) change.getValue();
						EList<?> currentMenus = ((MPart) container).getMenus();

						int index = originalMenus.indexOf(reference);
						if (index == -1) {
							return null;
						} else if (index == currentMenus.indexOf(reference)) {
							return getLocalId(object);
						} else {
							String id = getModelId(reference);
							if (id != null) {
								return id;
							}

							StringBuilder builder = new StringBuilder();
							insertMenuReference(reference, builder, originalMenus);
							String originalId = getOriginalId(container);
							builder.insert(0, originalId);
							return builder.toString();
						}
					}
				}
				return getLocalId(object);
			}
		}

		return getLocalId(object);
	}

	private Element createDeltaElement(Document document, EObject object,
			FeatureChange featureChange, String featureName) {
		Element featureElement = document.createElement(featureName);
		EStructuralFeature feature = featureChange.getFeature();

		if (object.eIsSet(feature)) {
			Object value = object.eGet(feature);
			if (isSingleReference(featureName)) {
				Element referenceElement = createReferenceElement(document, (EObject) value);
				featureElement.appendChild(referenceElement);
			} else if (isChainedReference(featureName)) {
				List<?> references = (List<?>) value;
				for (Object reference : references) {
					Element referenceElement = createReferenceElement(document, (EObject) reference);
					featureElement.appendChild(referenceElement);
				}

				references = (List<?>) featureChange.getValue();
				for (Object reference : references) {
					Element referenceElement = document
							.createElement(ORIGINALREFERENCE_ELEMENT_NAME);
					referenceElement.setAttribute(APPLICATIONELEMENT_ID_ATTNAME,
							getOriginalId(reference));
					featureElement.appendChild(referenceElement);
				}
			} else if (isChainedAttribute(featureName)) {
				List<?> attributes = (List<?>) value;
				for (Object attribute : attributes) {
					Element attributeElement = document.createElement(featureName);
					attributeElement.setAttribute(featureName, String.valueOf(attribute));
					featureElement.appendChild(attributeElement);
				}
			} else {
				featureElement.setAttribute(featureName, String.valueOf(value));
			}
		} else {
			if (isChainedReference(featureName)) {
				List<?> references = (List<?>) featureChange.getValue();
				for (Object reference : references) {
					Element referenceElement = document
							.createElement(ORIGINALREFERENCE_ELEMENT_NAME);
					referenceElement.setAttribute(APPLICATIONELEMENT_ID_ATTNAME,
							getOriginalId(reference));
					featureElement.appendChild(referenceElement);
				}
			} else {
				featureElement.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
			}
		}

		return featureElement;
	}

	private Element createReferenceElement(Document document, EObject eObject) {
		Element referenceElement = document.createElement(REFERENCE_ELEMENT_NAME);
		String id = getOriginalId(eObject);
		if (id == null) {
			// the object did not exist, mark it as such so it can be created during the
			// delta application phase
			referenceElement.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
			// note what we need to create
			referenceElement.setAttribute(TYPE_ATTNAME, eObject.getClass().getInterfaces()[0]
					.getSimpleName());

			populateNewReferenceElement(document, eObject, referenceElement);
		} else {
			referenceElement.setAttribute(APPLICATIONELEMENT_ID_ATTNAME, id);
		}

		return referenceElement;
	}

	private void populateNewReferenceElement(Document document, EObject eObject,
			Element referenceElement) {
		for (EStructuralFeature collectedFeature : collectFeatures(eObject)) {
			String collectedFeatureName = collectedFeature.getName();
			if (!collectedFeature.isTransient() && shouldPersist(collectedFeatureName)) {
				Element referenceAttributeElement = document.createElement(collectedFeatureName);
				if (eObject.eIsSet(collectedFeature)) {
					if (collectedFeatureName.equals(APPLICATIONELEMENT_ID_ATTNAME)) {
						referenceAttributeElement.setAttribute(collectedFeatureName,
								getLocalId(eObject));
					} else {
						if (isSingleReference(collectedFeatureName)) {
							referenceAttributeElement.setAttribute(collectedFeatureName,
									getOriginalId(eObject.eGet(collectedFeature)));
						} else if (isChainedReference(collectedFeatureName)) {
							List<?> references = (List<?>) eObject.eGet(collectedFeature);
							for (Object reference : references) {
								Element ef = createReferenceElement(document, (EObject) reference);
								referenceAttributeElement.appendChild(ef);
							}
						} else {
							referenceAttributeElement.setAttribute(collectedFeatureName, String
									.valueOf(eObject.eGet(collectedFeature)));
						}
					}
				} else {
					referenceAttributeElement.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
				}
				referenceElement.appendChild(referenceAttributeElement);
			}
		}
	}

	private static boolean isSingleReference(String featureName) {
		// an ElementContainer has a single reference to its active child
		return featureName.equals(ELEMENTCONTAINER_ACTIVECHILD_ATTNAME) ||
		// a Handler has a single reference to a command
				featureName.equals(HANDLER_COMMAND_ATTNAME) ||
				// a KeyBinding has a single reference to a command
				featureName.equals(KEYBINDING_COMMAND_ATTNAME) ||
				// a Window has a single reference to a menu
				featureName.equals(WINDOW_MAINMENU_ATTNAME) ||
				// a Part has a single reference to a tool bar
				featureName.equals(PART_TOOLBAR_ATTNAME);
	}

	private static boolean isChainedReference(String featureName) {
		// an ElementContainer has multiple children
		return featureName.equals(ELEMENTCONTAINER_CHILDREN_ATTNAME) ||
		// a BindingContainer has multiple bindings
				featureName.equals(BINDINGCONTAINER_BINDINGS_ATTNAME) ||
				// a Part has multiple menus
				featureName.equals(PART_MENUS_ATTNAME) ||
				// an Application has multiple commands
				featureName.equals(APPLICATION_COMMANDS_ATTNAME) ||
				// a HandlerContainer has multiple handlers
				featureName.equals(HANDLERCONTAINER_HANDLERS_ATTNAME);
	}

	private boolean isChainedAttribute(String featureName) {
		// a GenericTile has multiple integer weights
		return featureName.equals(GENERICTILE_WEIGHTS_ATTNAME);
	}

	private boolean shouldPersist(String featureName) {
		// parent changes are captured by children changes already
		return !featureName.equals(UIELEMENT_PARENT_ATTNAME);
	}

	private ChangeDescription calculateDeltas() {
		if (changeDescription == null) {
			changeDescription = changeRecorder.endRecording();
		}
		return changeDescription;
	}

}
