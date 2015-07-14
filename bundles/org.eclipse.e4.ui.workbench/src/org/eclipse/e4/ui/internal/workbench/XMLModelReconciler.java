/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.workbench.modeling.IDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
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

	private static final String XMIID_ATTNAME = "xmiId"; //$NON-NLS-1$

	private static final String REFERENCE_ELEMENT_NAME = "reference"; //$NON-NLS-1$
	private static final String ORIGINALREFERENCE_ELEMENT_NAME = "originalReference"; //$NON-NLS-1$

	private static final String NAMESPACE_ATTNAME = "e4namespace"; //$NON-NLS-1$
	private static final String OLD_CONTRIBUTION_URI_PREFIX = "platform:/plugin/"; //$NON-NLS-1$
	private static final String NEW_CONTRIBUTION_URI_PREFIX = "bundleclass://"; //$NON-NLS-1$

	/**
	 * The name of the root element that describes the model deltas in XML form (value is
	 * <code>changes</code>).
	 */
	private static final String CHANGES_ELEMENT_NAME = "changes"; //$NON-NLS-1$

	/**
	 * The name of the attribute that describes the version of the model deltas (value is
	 * <code>version</code>).
	 */
	private static final String VERSION_ATTNAME = "version"; //$NON-NLS-1$

	/**
	 * The version of the model deltas.
	 *
	 * <ul>
	 * <li>1.1 - introduced direct references to binding contexts instead of using string ids (see
	 * bug 320171 and bug 338444)</li>
	 * <li>1.0 (no change) - the model was updated with MArea, code was inserted to handle this case
	 * so the version number was not actually increased (see bug 328388)</li>
	 * <li>1.0 - first version of the model that went out for 4.0</li>
	 * </ul>
	 */
	// a new string is constructed because we do not know want the value to be inlined
	private static final String VERSION_NUMBER = new String("1.1"); //$NON-NLS-1$

	/**
	 * An attribute for describing the type of the object in question (value is <code>type</code>).
	 */
	private static final String TYPE_ATTNAME = "type"; //$NON-NLS-1$

	private static final String UNSET_ATTNAME = "unset"; //$NON-NLS-1$
	private static final String UNSET_ATTVALUE_TRUE = "true"; //$NON-NLS-1$

	private static final String ENTRY_ATTVALUE_KEY = "key"; //$NON-NLS-1$
	private static final String ENTRY_ATTVALUE_VALUE = "value"; //$NON-NLS-1$

	private ChangeRecorder changeRecorder;

	private ChangeDescription changeDescription;

	private EObject rootObject;

	/**
	 * A map of all the objects that were originally defined in the model.
	 */
	private WeakHashMap<EObject, EObject> originalObjects = new WeakHashMap<>();

	/**
	 * Records all of the objects in the original model so that we can determine whether an element
	 * was originally a part of the defined model or not.
	 */
	private void record() {
		originalObjects.clear();
		originalObjects.put(rootObject, null);
		Iterator<EObject> it = rootObject.eAllContents();
		while (it.hasNext()) {
			EObject object = it.next();
			originalObjects.put(object, null);
		}
	}

	@Override
	public void recordChanges(Object object) {
		Assert.isNotNull(object);
		rootObject = (EObject) object;
		changeRecorder = new ChangeRecorder(rootObject) {
			@Override
			protected boolean shouldRecord(EStructuralFeature feature, EObject eObject) {
				return !feature.isTransient() && super.shouldRecord(feature, eObject);
			}

			@Override
			protected boolean shouldRecord(EStructuralFeature feature, EReference containment,
					Notification notification, EObject eObject) {
				return !feature.isTransient()
						&& super.shouldRecord(feature, containment, notification, eObject);
			}
		};
		changeDescription = null;
		record();
	}

	static List<Object> getReferences(Object object) {
		Iterator<EObject> it = ((EObject) object).eAllContents();
		List<Object> references = new LinkedList<>();
		while (it.hasNext()) {
			Object reference = it.next();
			references.add(reference);
		}
		return references;
	}

	@Override
	public Collection<ModelDelta> constructDeltas(Object object, Object serializedState) {
		rootObject = (EObject) object;
		List<Object> references = getReferences(rootObject);

		Document document = (Document) serializedState;

		Collection<ModelDelta> deltas = new LinkedList<>();

		Element rootElement = document.getDocumentElement();
		String version = rootElement.getAttribute(VERSION_ATTNAME);
		try {
			if (version == null || version.length() == 0
					|| Double.parseDouble(version) < Double.parseDouble(VERSION_NUMBER)) {
				return deltas;
			}
		} catch (NumberFormatException e) {
			// some corrupt versioning, ignore this deltas file
			return deltas;
		}

		NodeList rootNodeList = (NodeList) rootElement;
		for (int i = 0; i < rootNodeList.getLength(); i++) {
			Node node = rootNodeList.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				constructDeltas(deltas, references, rootObject, element,
						element.getAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME));
			}
		}

		return deltas;
	}

	private static EStructuralFeature getStructuralFeature(EObject object, String featureName) {
		for (EStructuralFeature sf : object.eClass().getEAllStructuralFeatures()) {
			if (sf.getName().equals(featureName)) {
				return sf;
			}
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
		} else if (feature == UiPackageImpl.eINSTANCE.getGenericTrimContainer_Side()) {
			return SideValue.getByName(featureValue);
		} else if (feature == MenuPackageImpl.eINSTANCE.getItem_Type()) {
			return ItemType.getByName(featureValue);
		}
		return null;
	}

	static Object findReference(List<Object> references, String id) {
		for (Object reference : references) {
			if (reference instanceof MApplicationElement && getLocalId(reference).equals(id)) {
				return reference;
			}
		}

		return null;
	}

	private boolean constructDeltas(Collection<ModelDelta> deltas, List<Object> references,
			EObject object, Element element, String id) {
		if (object instanceof MApplicationElement || object instanceof MKeyBinding) {
			if (getLocalId(object).equals(id)) {
				constructDeltas(deltas, references, object, element);
				return true;
			}
		}

		if (object instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) object).getChildren()) {
				if (constructDeltas(deltas, references, (EObject) child, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MPerspective) {
			for (MWindow window : ((MPerspective) object).getWindows()) {
				if (constructDeltas(deltas, references, (EObject) window, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MBindingTableContainer) {
			for (MBindingTable bindingTable : ((MBindingTableContainer) object).getBindingTables()) {
				if (constructDeltas(deltas, references, (EObject) bindingTable, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MBindingTable) {
			for (MKeyBinding keyBinding : ((MBindingTable) object).getBindings()) {
				if (constructDeltas(deltas, references, (EObject) keyBinding, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MHandlerContainer) {
			for (MHandler handler : ((MHandlerContainer) object).getHandlers()) {
				if (constructDeltas(deltas, references, (EObject) handler, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MApplication) {
			for (MCommand command : ((MApplication) object).getCommands()) {
				if (constructDeltas(deltas, references, (EObject) command, element, id)) {
					return true;
				}
			}

			for (MAddon addon : ((MApplication) object).getAddons()) {
				if (constructDeltas(deltas, references, (EObject) addon, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MPartDescriptorContainer) {
			for (MPartDescriptor descriptor : ((MPartDescriptorContainer) object).getDescriptors()) {
				if (constructDeltas(deltas, references, (EObject) descriptor, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MPart) {
			MPart part = (MPart) object;

			for (MMenu menu : part.getMenus()) {
				if (constructDeltas(deltas, references, (EObject) menu, element, id)) {
					return true;
				}
			}

			MToolBar toolBar = part.getToolbar();
			if (toolBar != null) {
				if (constructDeltas(deltas, references, (EObject) toolBar, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MMenuContributions) {
			for (MMenuContribution contribution : ((MMenuContributions) object)
					.getMenuContributions()) {
				if (constructDeltas(deltas, references, (EObject) contribution, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MToolBarContributions) {
			for (MToolBarContribution contribution : ((MToolBarContributions) object)
					.getToolBarContributions()) {
				if (constructDeltas(deltas, references, (EObject) contribution, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MTrimContributions) {
			for (MTrimContribution contribution : ((MTrimContributions) object)
					.getTrimContributions()) {
				if (constructDeltas(deltas, references, (EObject) contribution, element, id)) {
					return true;
				}
			}
		}

		if (object instanceof MWindow) {
			MWindow window = (MWindow) object;
			if (constructDeltas(deltas, references, (EObject) window.getMainMenu(), element, id)) {
				return true;
			}

			if (object instanceof MTrimmedWindow) {
				MTrimmedWindow trimmedWindow = (MTrimmedWindow) object;
				for (MTrimBar trimBar : trimmedWindow.getTrimBars()) {
					if (constructDeltas(deltas, references, (EObject) trimBar, element, id)) {
						return true;
					}
				}
			}
		}

		if (object instanceof MHandledItem) {
			for (MParameter parameter : ((MHandledItem) object).getParameters()) {
				if (constructDeltas(deltas, references, (EObject) parameter, element, id)) {
					return true;
				}
			}
		}

		return false;
	}

	private void constructDeltas(Collection<ModelDelta> deltas, List<Object> references,
			EObject object, Element element) {
		String elementName = element.getNodeName();
		if (elementName.equals(CONTEXT_PROPERTIES_ATTNAME)) {
			constructEntryDelta(deltas, UiPackageImpl.eINSTANCE.getContext_Properties(), object,
					element);
		} else if (elementName.equals(APPLICATIONELEMENT_PERSISTEDSTATE_ATTNAME)) {
			constructEntryDelta(deltas,
					ApplicationPackageImpl.eINSTANCE.getApplicationElement_PersistedState(),
					object, element);
		} else {
			constructObjectDeltas(deltas, references, object, element);
		}
	}

	private void constructEntryDelta(Collection<ModelDelta> deltas, EStructuralFeature feature,
			EObject object, Element element) {
		if (element.getAttribute(UNSET_ATTNAME).equals(UNSET_ATTVALUE_TRUE)) {
			EMFDeltaEntrySet delta = new EMFDeltaEntrySet(object, feature,
					element.getAttribute(ENTRY_ATTVALUE_KEY), null);
			deltas.add(delta);
		} else {
			EMFDeltaEntrySet delta = new EMFDeltaEntrySet(object, feature,
					element.getAttribute(ENTRY_ATTVALUE_KEY),
					element.getAttribute(ENTRY_ATTVALUE_VALUE));
			deltas.add(delta);
		}
	}

	private void constructObjectDeltas(Collection<ModelDelta> deltas, List<Object> references,
			EObject object, Element element) {
		NodeList nodeList = (NodeList) element;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (!(node instanceof Element)) {
				continue;
			}

			Element innerElement = (Element) node;
			String featureName = innerElement.getNodeName();
			EStructuralFeature feature = getStructuralFeature(object, featureName);
			if (feature != null) {
				if (isChainedReference(featureName)) {
					ModelDelta delta = createMultiReferenceDelta(deltas, references, object,
							feature, innerElement);
					deltas.add(delta);
				} else if (isUnset(innerElement)) {
					ModelDelta delta = new EMFModelDeltaUnset(object, feature);
					deltas.add(delta);
				} else if (isDirectReference(featureName)) {
					ModelDelta delta = createDirectReferenceDelta(deltas, references, object,
							feature, innerElement);
					deltas.add(delta);
				} else if (isIndirectReference(featureName)) {
					ModelDelta delta = createIndirectReferenceDelta(references, object, feature,
							innerElement);
					deltas.add(delta);
				} else if (isUnorderedChainedAttribute(featureName)) {
					ModelDelta delta = createUnorderedChainedAttributeDelta(object, feature,
							innerElement, featureName);
					deltas.add(delta);
				} else if (isStringToStringMap(featureName)) {
					ModelDelta delta = createMapDelta(object, innerElement, feature);
					deltas.add(delta);
				} else {
					ModelDelta delta = createAttributeDelta(object, feature, innerElement,
							featureName);
					deltas.add(delta);
				}
			}
		}
	}

	private ModelDelta createDirectReferenceDelta(Collection<ModelDelta> deltas,
			List<Object> references, EObject eObject, EStructuralFeature feature, Element node) {
		NodeList referencedIds = (NodeList) node;
		Element reference = getFirstElement(referencedIds);
		String referenceId = reference.getAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME);

		Object match = findReference(references, referenceId);
		if (match == null) {
			// couldn't find a reference, must be a new object
			match = createObject(deltas, reference, references);
		}

		return new EMFModelDeltaSet(eObject, feature, match);
	}

	private static Element getFirstElement(NodeList list) {
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item instanceof Element) {
				return (Element) item;
			}
		}
		return null;
	}

	private ModelDelta createIndirectReferenceDelta(List<Object> references, EObject eObject,
			EStructuralFeature feature, Element node) {
		NodeList referencedIds = (NodeList) node;

		Element reference = getFirstElement(referencedIds);
		String referenceId = reference.getAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME);

		Object match = findReference(references, referenceId);
		if (match == null) {
			return createDelayedDelta(eObject, feature, reference);
		}

		return new EMFModelDeltaSet(eObject, feature, match);
	}

	private ModelDelta createMapDelta(EObject object, Element innerElement,
			EStructuralFeature feature) {
		Map<String, String> deltaMap = new HashMap<>();
		NodeList attributes = (NodeList) innerElement;
		for (int j = 0; j < attributes.getLength(); j++) {
			Node entry = attributes.item(j);
			if (entry instanceof Element) {
				Element keyValue = (Element) entry;
				String key = keyValue.getAttribute(ENTRY_ATTVALUE_KEY);
				String value = keyValue.getAttribute(ENTRY_ATTVALUE_VALUE);
				deltaMap.put(key, value);
			}
		}
		return new EMFDeltaMapSet(object, feature, deltaMap);
	}

	private ModelDelta createDelayedDelta(EObject object, EStructuralFeature feature,
			Element element) {
		String referenceId = element.getAttribute(XMIID_ATTNAME);
		return new EMFModelDeltaDelayedSet(object, feature, rootObject, referenceId);
	}

	public static List<?> threeWayMerge(List<?> originalReferences, List<?> userReferences,
			List<?> currentReferences) {
		int userSize = userReferences.size();
		int originalSize = originalReferences.size();

		if (userSize == 0) {
			// the user removed all the original parts
			List<Object> collectedReferences = new ArrayList<>(currentReferences);
			collectedReferences.removeAll(originalReferences);
			return collectedReferences;
		} else if (originalSize == 0) {
			List<Object> collectedReferences = new ArrayList<>(userReferences);
			collectedReferences.addAll(currentReferences);
			return collectedReferences;
		} else if (currentReferences.isEmpty()) {
			// currently not referencing anything, so just return what the user had exactly
			return userReferences;
		} else if (currentReferences.size() == originalSize
				&& currentReferences.containsAll(originalReferences)) {
			// since both versions contain the same thing, just use whatever the user had
			return userReferences;
		}

		if (originalReferences.containsAll(userReferences)
				&& !userReferences.containsAll(originalReferences)) {
			List<Object> collectedReferences2 = new ArrayList<>(originalReferences);
			collectedReferences2.removeAll(userReferences);

			List<Object> collectedReferences = new ArrayList<>(currentReferences);
			collectedReferences.removeAll(collectedReferences2);

			return collectedReferences;
		}

		List<Position> positions = new ArrayList<>();

		for (int i = 0; i < userReferences.size(); i++) {
			Object user = userReferences.get(i);

			Position p = getPosition(originalReferences, userReferences, currentReferences, user, i);
			if (p != null) {
				positions.add(p);
			}
		}

		List<Object> collectedRefs = new ArrayList<>(currentReferences);

		for (Position position : positions) {
			Object after = position.getAfter();
			if (after != null) {
				int index = currentReferences.indexOf(after);
				collectedRefs.add(index + 1, position.getObject());
			}

			Object before = position.getBefore();
			if (before != null) {
				int index = currentReferences.indexOf(before);
				collectedRefs.add(index, position.getObject());
			}
		}

		return collectedRefs;
	}

	private static Position getPosition(List<?> originalReferences, List<?> userReferences,
			List<?> currentReferences, Object object, int originalIndex) {
		int index = originalReferences.indexOf(object);
		if (index == -1) {
			Object after = null;
			for (int i = originalIndex - 1; i > -1; i--) {
				Object afterCandidate = userReferences.get(i);
				int afterIndex = currentReferences.indexOf(afterCandidate);
				if (afterIndex != -1) {
					after = afterCandidate;
					break;
				}
			}

			Object before = null;
			for (int i = originalIndex + 1; i < userReferences.size(); i++) {
				Object beforeCandidate = userReferences.get(i);
				int beforeIndex = currentReferences.indexOf(beforeCandidate);
				if (beforeIndex != -1) {
					before = beforeCandidate;
					break;
				}
			}

			return new Position(object, after, before);
		}
		return null;
	}

	static class Position {

		private final Object object;
		private final Object after;
		private final Object before;

		Position(Object object, Object after, Object before) {
			this.object = object;
			this.after = after;
			this.before = before;
		}

		public Object getObject() {
			return object;
		}

		public Object getBefore() {
			return before;
		}

		public Object getAfter() {
			return after;
		}

	}

	private ModelDelta createMultiReferenceDelta(Collection<ModelDelta> deltas,
			List<Object> references, EObject eObject, EStructuralFeature feature, Element node) {
		NodeList referencedIds = (NodeList) node;
		List<Object> originalReferences = new ArrayList<>();
		List<Object> userReferences = new ArrayList<>();
		List<?> currentReferences = (List<?>) eObject.eGet(feature);

		for (int i = 0; i < referencedIds.getLength(); i++) {
			Node item = referencedIds.item(i);
			if (item instanceof Element) {
				Element reference = (Element) item;
				if (isUnset(reference)) {
					userReferences.add(createObject(deltas, reference, references));
				} else {
					String referenceId = reference
							.getAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME);
					Object match = findReference(references, referenceId);
					if (match != null) {
						// determine if this was a reference set by the user or a reference that was
						// originally defined by the application
						if (reference.getNodeName().equals(REFERENCE_ELEMENT_NAME)) {
							userReferences.add(match);
						} else {
							originalReferences.add(match);
						}
					}
				}
			}
		}

		return new EMFModelDeltaThreeWayDelayedSet(eObject, feature, originalReferences,
				userReferences, currentReferences);
	}

	private static EObject createObject(String namespace, String type) {
		EFactory factory = EPackage.Registry.INSTANCE.getEFactory(namespace);
		for (EClassifier classifier : factory.getEPackage().getEClassifiers()) {
			if (classifier instanceof EClass) {
				EClass cls = (EClass) classifier;
				if (cls.getInstanceClassName().equals(type)) {
					return factory.create(cls);
				}
			}
		}
		return null;
	}

	private Object getReference(Collection<ModelDelta> deltas, Element element,
			List<Object> references) {
		String id = element.getAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME);
		if (!id.equals("")) { //$NON-NLS-1$
			return findReference(references, id);
		}
		return createObject(deltas, element, references);
	}

	private Object createObject(Collection<ModelDelta> deltas, Element element,
			List<Object> references) {
		String typeName = element.getAttribute(TYPE_ATTNAME);
		String namespace = element.getAttribute(NAMESPACE_ATTNAME);

		EObject object = createObject(namespace, typeName);
		CompositeDelta compositeDelta = new CompositeDelta(object);

		E4XMIResource resource = (E4XMIResource) rootObject.eResource();
		resource.setInternalId(object, element.getAttribute(XMIID_ATTNAME));

		NodeList elementAttributes = (NodeList) element;
		for (int i = 0; i < elementAttributes.getLength(); i++) {
			Node node = elementAttributes.item(i);
			if (node instanceof Element) {
				Element item = (Element) node;
				if (!isUnset(item)) {
					String attributeName = item.getNodeName();
					EStructuralFeature attributeFeature = getStructuralFeature(object,
							attributeName);
					if (attributeFeature != null) {
						if (isDirectReference(attributeName)) {
							String id = item.getAttribute(attributeName);
							Object objectReference = findReference(references, id);
							if (objectReference == null) {
								objectReference = createObject(deltas,
										getFirstElement((NodeList) item), references);
							}

							IDelta delta = new EMFModelDeltaSet(object, attributeFeature,
									objectReference);
							compositeDelta.add(delta);
						} else if (isIndirectReference(attributeName)) {
							String id = item.getAttribute(attributeName);
							Object objectReference = findReference(references, id);
							if (objectReference == null) {
								NodeList list = (NodeList) item;
								Element refElement = getFirstElement(list);
								if (refElement != null) {
									ModelDelta delta = createDelayedDelta(object, attributeFeature,
											refElement);
									deltas.add(delta);
								}
							} else {
								IDelta delta = new EMFModelDeltaSet(object, attributeFeature,
										objectReference);
								compositeDelta.add(delta);
							}
						} else if (isChainedReference(attributeName)) {
							List<Object> objectReferences = new ArrayList<>();
							NodeList objectReferenceNodes = (NodeList) item;

							for (int j = 0; j < objectReferenceNodes.getLength(); j++) {
								Node refNode = objectReferenceNodes.item(j);
								if (refNode instanceof Element) {
									Object objectReference = getReference(deltas,
											(Element) refNode, references);
									if (objectReference != null) {
										objectReferences.add(objectReference);
									}
								}
							}

							IDelta delta = new EMFModelDeltaSet(object, attributeFeature,
									objectReferences);
							compositeDelta.add(delta);
						} else if (isUnorderedChainedAttribute(attributeName)) {
							ModelDelta delta = createUnorderedChainedAttributeDelta(object,
									attributeFeature, item, attributeName);
							deltas.add(delta);
						} else if (isStringToStringMap(attributeName)) {
							EStructuralFeature feature = getStructuralFeature(object, attributeName);
							@SuppressWarnings("unchecked")
							EMap<String, String> map = (EMap<String, String>) object.eGet(feature);

							NodeList attributes = (NodeList) item;
							for (int j = 0; j < attributes.getLength(); j++) {
								Node entry = attributes.item(j);
								if (entry instanceof Element) {
									Element keyValue = (Element) entry;
									map.put(keyValue.getAttribute(ENTRY_ATTVALUE_KEY),
											keyValue.getAttribute(ENTRY_ATTVALUE_VALUE));
								}
							}
						} else {
							Object value = getValue(attributeFeature,
									item.getAttribute(attributeName));
							if (CONTRIBUTION_URI_ATTNAME.equals(attributeFeature.getName()))
								value = ((String) value).replaceFirst(OLD_CONTRIBUTION_URI_PREFIX,
										NEW_CONTRIBUTION_URI_PREFIX);
							object.eSet(attributeFeature, value);
						}
					}
				}
			}
		}

		return compositeDelta;
	}

	private ModelDelta createUnorderedChainedAttributeDelta(EObject object,
			EStructuralFeature feature, Element node, String featureName) {
		Set<Object> values = new HashSet<>();
		NodeList attributes = (NodeList) node;
		for (int j = 0; j < attributes.getLength(); j++) {
			Node item = attributes.item(j);
			if (item instanceof Element) {
				Element attribute = (Element) item;
				Object value = getValue(feature, attribute.getAttribute(featureName));
				values.add(value);
			}
		}

		List<?> currentValues = (List<?>) object.eGet(feature);
		values.addAll(currentValues);

		return new EMFModelDeltaSet(object, feature, new ArrayList<>(values));
	}

	private ModelDelta createAttributeDelta(EObject eObject, EStructuralFeature feature,
			Element node, String featureName) {
		Object value = getValue(feature, node.getAttribute(featureName));
		return new EMFModelDeltaSet(eObject, feature, value);
	}

	private Document createDocument() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ChangeDescription calculateDeltas() {
		if (changeDescription == null) {
			changeDescription = changeRecorder.endRecording();
		}
		return changeDescription;
	}

	@Override
	public Object serialize() {
		calculateDeltas();

		// begin construction of the XML document
		Document document = createDocument();
		Element root = document.createElement(CHANGES_ELEMENT_NAME);
		root.setAttribute(VERSION_ATTNAME, VERSION_NUMBER);
		document.appendChild(root);

		EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription.getObjectChanges();
		for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
			EObject object = entry.getKey();
			// persist every change
			Element persistedElement = persist(document, entry, object);
			if (persistedElement != null) {
				// append the change to the document
				root.appendChild(persistedElement);
			}
		}

		return document;
	}

	/**
	 * Returns an XML representation of the changes that have occurred in the specified object. Or
	 * <code>null</code> if changes pertaining to this object should not be persisted
	 *
	 * @param document
	 *            the root XML document
	 * @param entry
	 *            a list of changes that have occurred to the object
	 * @param object
	 *            the object to persist
	 * @return an XML representation of the changes of the object, or <code>null</code> if its
	 *         changes should be ignored
	 */
	private Element persist(Document document, Entry<EObject, EList<FeatureChange>> entry,
			EObject object) {
		if (object instanceof Entry<?, ?>) {
			return persistEntry(document, object);
		}
		return persistObject(document, entry, object);
	}

	private Element persistObject(Document document, Entry<EObject, EList<FeatureChange>> entry,
			EObject object) {
		if (getOriginalId(object) == null) {
			return null;
		}

		Element modelChange = null;

		List<FeatureChange> featureChanges = entry.getValue();
		for (FeatureChange featureChange : featureChanges) {
			// we only want to persist deltas of non-transient features
			if (!featureChange.getFeature().isTransient()) {
				String featureName = featureChange.getFeatureName();
				// check if we're interested in this particular feature
				if (shouldPersist(featureName)) {
					if (modelChange == null) {
						// create an element to record this object's changes
						modelChange = createElement(document, object);
					}

					// create a delta for this particular change
					Element deltaElement = createDeltaElement(document, object, featureChange,
							featureName);
					modelChange.appendChild(deltaElement);
				}
			}
		}

		return modelChange;
	}

	private String getEntryElementName(EObject object, Entry<?, ?> entry) {
		if (object instanceof MContext) {
			for (Entry<String, String> property : ((MContext) object).getProperties().entrySet()) {
				if (property == entry) {
					return CONTEXT_PROPERTIES_ATTNAME;
				}
			}
		}

		if (object instanceof MContribution) {
			for (Entry<String, String> state : ((MContribution) object).getPersistedState()
					.entrySet()) {
				if (state == entry) {
					return APPLICATIONELEMENT_PERSISTEDSTATE_ATTNAME;
				}
			}
		}

		return null;
	}

	private Element persistEntry(Document document, EObject object) {
		EObject container = object.eContainer();
		if (getOriginalId(container) == null) {
			return null;
		}

		Entry<?, ?> entry = (Entry<?, ?>) object;
		String elementName = getEntryElementName(container, entry);
		if (elementName == null) {
			return null;
		}

		Element element = createElement(document, elementName, container);
		element.setAttribute(ENTRY_ATTVALUE_KEY, (String) entry.getKey());

		String value = (String) entry.getValue();
		if (value == null) {
			element.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
		} else {
			element.setAttribute(ENTRY_ATTVALUE_VALUE, (String) entry.getValue());
		}

		return element;
	}

	private Element createElement(Document document, EObject object) {
		Class<?> rootInterface = object.getClass().getInterfaces()[0];
		// this technically doesn't have to be tagged with this name, it's not parsed, but makes the
		// XML more readable
		return createElement(document, rootInterface.getCanonicalName(), object);
	}

	private Element createElement(Document document, String elementName, EObject object) {
		Element modelChange = document.createElement(elementName);
		modelChange.setAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME, getOriginalId(object));
		return modelChange;
	}

	/**
	 * Retrieves the id of the object by querying for it from the resource.
	 *
	 * @param object
	 *            the object to retrieve the id for
	 * @param container
	 *            the parent container of the object
	 * @return the object's id as recorded by the containing resource
	 */
	private static String getResourceId(EObject object, EObject container) {
		Resource resource = object.eResource();
		if (resource instanceof XMLResource) {
			return ((XMLResource) resource).getID(object);
		}

		resource = container.eResource();
		if (resource instanceof XMLResource) {
			return ((XMLResource) resource).getID(object);
		}

		throw new IllegalStateException(object + " could not be identified"); //$NON-NLS-1$
	}

	private String findResourceId(EObject object, EObject container) {
		Resource resource = object.eResource();
		if (resource instanceof XMLResource) {
			return ((XMLResource) resource).getID(object);
		}

		resource = container.eResource();
		if (resource instanceof XMLResource) {
			return ((XMLResource) resource).getID(object);
		}

		if (originalObjects.containsKey(object)) {
			resource = rootObject.eResource();
			if (resource instanceof XMLResource) {
				return ((XMLResource) resource).getID(object);
			}
		}

		throw new IllegalStateException(object + " could not be identified"); //$NON-NLS-1$
	}

	private static String getLocalId(Object object) {
		EObject reference = (EObject) object;
		return getResourceId(reference, reference.eContainer());
	}

	/**
	 * Retrieves the original containing parent object of the specified reference. If
	 * <code>null</code> is returned, the object was not initially known by the change recorder.
	 *
	 * @param reference
	 *            the object to find its original container for
	 * @return the original parent container of the object, or <code>null</code> if it did not
	 *         initially exist
	 */
	private EObject getOriginalContainer(EObject reference) {
		if (changeDescription == null) {
			// no changes have been recorded, just ask the container through EMF directly
			return reference.eContainer();
		} else if (!originalObjects.containsKey(reference)) {
			return null;
		}

		if (reference instanceof MCommandParameter) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean parametersChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key instanceof MCommand) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(COMMAND_PARAMETERS_ATTNAME)) {
							List<?> parameters = (List<?>) change.getValue();
							for (Object parameter : parameters) {
								if (parameter == reference) {
									return key;
								}
							}

							parametersChanged = true;
							break;
						}
					}
					break;
				}
			}

			return parametersChanged ? null : reference.eContainer();
		}

		if (reference instanceof MParameter) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean parametersChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key instanceof MHandledItem) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(HANDLEDITEM_PARAMETERS_ATTNAME)) {
							List<?> parameters = (List<?>) change.getValue();
							for (Object parameter : parameters) {
								if (parameter == reference) {
									return key;
								}
							}

							parametersChanged = true;
							break;
						}
					}
					break;
				}
			}

			return parametersChanged ? null : reference.eContainer();
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

		if (reference instanceof MMenuContribution) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean menuContributionsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == rootObject) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(
								MENUCONTRIBUTIONS_MENUCONTRIBUTIONS_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							menuContributionsChanged = true;
							break;
						}
					}
					break;
				}
			}

			return menuContributionsChanged ? null : reference.eContainer();
		}

		if (reference instanceof MToolBarContribution) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean toolBarContributionsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == rootObject) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(
								TOOLBARCONTRIBUTIONS_TOOLBARCONTRIBUTIONS_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							toolBarContributionsChanged = true;
							break;
						}
					}
					break;
				}
			}

			return toolBarContributionsChanged ? null : reference.eContainer();
		}

		if (reference instanceof MTrimContribution) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean toolBarContributionsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == rootObject) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(
								TRIMCONTRIBUTIONS_TRIMCONTRIBUTIONS_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							toolBarContributionsChanged = true;
							break;
						}
					}
					break;
				}
			}

			return toolBarContributionsChanged ? null : reference.eContainer();
		}

		if (reference instanceof MBindingTable) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();

			boolean bindingTablesChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == rootObject) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(BINDINGCONTAINER_BINDINGTABLES_ATTNAME)) {
							List<?> commands = (List<?>) change.getValue();
							for (Object command : commands) {
								if (command == reference) {
									return key;
								}
							}

							bindingTablesChanged = true;
							break;
						}
					}
					break;
				}
			}

			if (!bindingTablesChanged) {
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
				if (key instanceof MBindingTable) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(BINDINGTABLE_BINDINGS_ATTNAME)) {
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

		if (reference instanceof MPartDescriptor) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();
			boolean descriptorsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key == rootObject) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(
								PARTDESCRIPTORCONTAINER_DESCRIPTORS_ATTNAME)) {
							List<?> descriptors = (List<?>) change.getValue();
							for (Object descriptor : descriptors) {
								if (descriptor == reference) {
									return key;
								}
							}
							descriptorsChanged = true;
							break;
						}
					}
					break;
				}
			}

			return descriptorsChanged ? null : reference.eContainer();
		}

		if (reference instanceof MTrimBar) {
			EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription
					.getObjectChanges();
			boolean trimBarsChanged = false;

			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key instanceof MTrimmedWindow) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(TRIMMEDWINDOW_TRIMBARS_ATTNAME)) {
							List<?> trimBars = (List<?>) change.getValue();
							for (Object trimBar : trimBars) {
								if (trimBar == reference) {
									return key;
								}
							}
							trimBarsChanged = true;
							break;
						}
					}

					if (trimBarsChanged) {
						break;
					}
				}
			}

			if (trimBarsChanged) {
				return null;
			}

			for (EObject rootChild : rootObject.eContents()) {
				if (rootChild instanceof MTrimmedWindow) {
					if (((MTrimmedWindow) rootChild).getTrimBars().contains(reference)) {
						return rootChild;
					}
				}
			}

			return null;
		}

		EMap<EObject, EList<FeatureChange>> objectChanges = changeDescription.getObjectChanges();
		if (reference instanceof MUIElement) {
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
				boolean appendedMenu = false;

				for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
					EObject key = entry.getKey();
					if (key instanceof MPart) {
						for (FeatureChange change : entry.getValue()) {
							if (change.getFeatureName().equals(PART_MENUS_ATTNAME)) {
								List<?> originalMenus = (List<?>) change.getValue();
								if (originalMenus.contains(reference)) {
									return key;
								}

								if (((MPart) key).getMenus().contains(reference)) {
									appendedMenu = true;
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
				} else if (appendedMenu && !menuSet && !menuChanged) {
					return null;
				}

				EObject container = reference.eContainer();
				if (!(container instanceof MMenu)) {
					return container;
				}
			}

			if (reference instanceof MToolBar) {
				for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
					EObject key = entry.getKey();
					if (key instanceof MPart) {
						for (FeatureChange change : entry.getValue()) {
							if (change.getFeatureName().equals(PART_TOOLBAR_ATTNAME)) {
								if (change.getValue() == reference) {
									return key;
								}
							}
						}
					}
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
						} else if (change.getFeatureName().equals(PART_TOOLBAR_ATTNAME)) {
							if (reference.equals(change.getValue())) {
								return key;
							}
							newElement = true;
							break;
						} else if (change.getFeatureName().equals(WINDOW_SHAREDELEMENTS_ATTNAME)) {
							EList<?> value = (EList<?>) change.getValue();
							if (value.contains(reference)) {
								return key;
							}
							newElement = true;
							break;
						} else if (change.getFeatureName().equals(PERSPECTIVE_WINDOWS_ATTNAME)) {
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

				for (FeatureChange change : entry.getValue()) {
					if (change.getFeatureName().equals(WINDOW_SHAREDELEMENTS_ATTNAME)) {
						EList<?> value = (EList<?>) change.getValue();
						if (value.contains(reference)) {
							return key;
						}
					}
				}
			}

			if (reference instanceof MWindow) {
				for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
					EObject key = entry.getKey();
					if (key instanceof MPerspective) {
						for (FeatureChange change : entry.getValue()) {
							if (change.getFeatureName().equals(PERSPECTIVE_WINDOWS_ATTNAME)) {
								EList<?> value = (EList<?>) change.getValue();
								if (value.contains(reference)) {
									return key;
								}
								break;
							}
						}
					}
				}
			}

			if (!newElement) {
				return reference instanceof MApplication ? changeDescription : reference
						.eContainer();
			}
		}

		if (reference instanceof MAddon) {
			for (Entry<EObject, EList<FeatureChange>> entry : objectChanges.entrySet()) {
				EObject key = entry.getKey();
				if (key instanceof MApplication) {
					for (FeatureChange change : entry.getValue()) {
						if (change.getFeatureName().equals(APPLICATION_ADDONS_ATTNAME)) {
							EList<?> value = (EList<?>) change.getValue();
							return value.contains(reference) ? key : null;
						}
					}
				}
			}

			return reference.eContainer();
		}

		return null;
	}

	/**
	 * Computes and returns the original id of the specified object. The meaning of the term
	 * "original" is defined as the state of the object prior to having its state monitored via
	 * {@link #recordChanges(Object)}. The identifier for the object will be queried from the
	 * resource. If the object did not exist was not known when changes began recording,
	 * <code>null</code> will be returned.
	 *
	 * @param object
	 *            the object to query an id for
	 * @return an id suitable for looking up the object, or <code>null</code> if the object did not
	 *         exist or was not known within the scope of the originally monitored object
	 */
	private String getOriginalId(Object object) {
		EObject reference = (EObject) object;
		EObject originalContainer = getOriginalContainer(reference);

		// did not exist in the model originally
		if (originalContainer == null) {
			return null;
		}

		if (originalContainer != changeDescription) {
			EObject container = originalContainer;
			while (container != rootObject) {
				container = getOriginalContainer(container);
				if (container == null) {
					return null;
				}
			}
		}

		return findResourceId(reference, originalContainer);
	}

	/**
	 * Creates an XML element that will capture the delta that occurred in the object.
	 *
	 * @param document
	 *            the root XML document
	 * @param object
	 *            the object that has changed
	 * @param featureChange
	 *            the captured information about the change
	 * @param featureName
	 *            the name of the feature that has changed
	 * @return the XML element about the change
	 */
	private Element createDeltaElement(Document document, EObject object,
			FeatureChange featureChange, String featureName) {
		EStructuralFeature feature = featureChange.getFeature();
		if (object.eIsSet(feature)) {
			return createSetDeltaElement(document, object, featureChange, featureName, feature);
		}

		return createUnsetDeltaElement(document, featureChange, featureName);
	}

	private Element createSetDeltaElement(Document document, EObject object,
			FeatureChange featureChange, String featureName, EStructuralFeature feature) {
		Element featureElement = document.createElement(featureName);
		Object value = object.eGet(feature);
		if (isSingleReference(featureName)) {
			// record what we're currently referencing, we create an element instead of simply
			// recording the id because we need to describe the entire object if the object is new,
			// simply recording an id would be insufficient for recording the attributes of the new
			// object
			Element referenceElement = createReferenceElement(document, (EObject) value,
					featureName);
			featureElement.appendChild(referenceElement);
		} else if (isChainedReference(featureName)) {
			// record what we're currently referencing
			appendReferenceElements(document, featureElement, (List<?>) value);
			// record what was originally referenced
			appendOriginalReferenceElements(document, featureElement,
					(List<?>) featureChange.getValue());
		} else if (isUnorderedChainedAttribute(featureName)) {
			appendUnorderedChainedAttributeElements(document, (List<?>) value, featureName,
					featureElement);
		} else if (isStringToStringMap(featureName)) {
			appendStringToStringMapElements(document, featureElement, object, feature, featureName);
		} else {
			featureElement.setAttribute(featureName, String.valueOf(value));
		}
		return featureElement;
	}

	private Element createUnsetDeltaElement(Document document, FeatureChange featureChange,
			String featureName) {
		Element featureElement = document.createElement(featureName);
		if (isChainedReference(featureName)) {
			appendOriginalReferenceElements(document, featureElement,
					(List<?>) featureChange.getValue());
		} else {
			featureElement.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
		}
		return featureElement;
	}

	/**
	 * Creates an element to describe that the specified object was a reference that was originally
	 * there when the application started prior to the applying of any deltas. It should be noted
	 * that an object that was originally referenced will not necessarily be dereferenced after any
	 * deltas have been applied.
	 *
	 * @param document
	 *            the root XML document
	 * @param reference
	 *            the referenced object
	 * @return an XML element to record that the specified object was originally referenced by
	 *         another object when the application started
	 */
	private Element createOriginalReferenceElement(Document document, Object reference) {
		Element referenceElement = document.createElement(ORIGINALREFERENCE_ELEMENT_NAME);
		referenceElement.setAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME,
				getOriginalId(reference));
		return referenceElement;
	}

	private void appendOriginalReferenceElements(Document document, Element element,
			List<?> references) {
		for (Object reference : references) {
			Element referenceElement = createOriginalReferenceElement(document, reference);
			element.appendChild(referenceElement);
		}
	}

	/**
	 * Creates a new element that defines a reference to another object.
	 *
	 * @param document
	 *            the root XML document
	 * @param eObject
	 *            the object to reference
	 * @param featureName
	 *            the name of the feature describing the reference relationship, if
	 *            <code>null</code>, it is assumed to be a direct reference
	 * @return an XML element that describes the reference to the provided object
	 * @see #isDirectReference(String)
	 * @see #isIndirectReference(String)
	 */
	private Element createReferenceElement(Document document, EObject eObject, String featureName) {
		String id = getOriginalId(eObject);

		if (id == null) {
			if (featureName == null || isDirectReference(featureName)) {
				// didn't exist originally, we need a completely new reference that describes the
				// object and its attributes
				return createNewReferenceElement(document, eObject);
			}
			return createUniqueReferenceElement(document, eObject);
		}

		Element referenceElement = document.createElement(REFERENCE_ELEMENT_NAME);
		referenceElement.setAttribute(APPLICATIONELEMENT_ELEMENTID_ATTNAME, id);
		return referenceElement;
	}

	private void appendReferenceElements(Document document, Element element, List<?> references) {
		for (Object reference : references) {
			Element ef = createReferenceElement(document, (EObject) reference, null);
			element.appendChild(ef);
		}
	}

	private Element createNewReferenceElement(Document document, EObject eObject) {
		Element referenceElement = createUniqueReferenceElement(document, eObject);

		// object did not exist, mark it as such so it can be created during the
		// when the deltas are applied
		referenceElement.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
		// note what we need to create by storing the type
		referenceElement.setAttribute(TYPE_ATTNAME,
				eObject.getClass().getInterfaces()[0].getCanonicalName());

		for (EStructuralFeature collectedFeature : collectFeatures(eObject)) {
			String featureName = collectedFeature.getName();
			// ignore transient features and features that we are not interested in
			if (!collectedFeature.isTransient() && shouldPersist(featureName)) {
				Element referenceAttributeElement = createAttributeElement(document, eObject,
						collectedFeature, featureName);
				referenceElement.appendChild(referenceAttributeElement);
			}
		}

		return referenceElement;
	}

	private Element createUniqueReferenceElement(Document document, EObject eObject) {
		EClass cls = eObject.eClass();
		EPackage pkg = (EPackage) cls.eContainer();

		Element referenceElement = document.createElement(REFERENCE_ELEMENT_NAME);

		E4XMIResource resource = (E4XMIResource) rootObject.eResource();
		String internalId = resource.getInternalId(eObject);
		referenceElement.setAttribute(XMIID_ATTNAME, internalId);
		referenceElement.setAttribute(NAMESPACE_ATTNAME, pkg.getNsURI());

		return referenceElement;
	}

	/**
	 * Creates an element that describes the state of the feature in the specified object.
	 *
	 * @param document
	 *            the root XML element document
	 * @param object
	 *            the object to describe
	 * @param feature
	 *            the feature of interest
	 * @param featureName
	 *            the name of the feature
	 * @return an XML element that describes the feature's value in the provided object
	 */
	private Element createAttributeElement(Document document, EObject object,
			EStructuralFeature feature, String featureName) {
		Element referenceAttributeElement = document.createElement(featureName);
		if (object.eIsSet(feature)) {
			if (isSingleReference(featureName)) {
				Object value = object.eGet(feature);
				String id = getOriginalId(value);
				if (id == null) {
					Element referenceElement = createReferenceElement(document, (EObject) value,
							featureName);
					referenceAttributeElement.appendChild(referenceElement);
				} else {
					referenceAttributeElement.setAttribute(featureName, id);
				}
			} else if (isChainedReference(featureName)) {
				List<?> references = (List<?>) object.eGet(feature);
				appendReferenceElements(document, referenceAttributeElement, references);
			} else if (isUnorderedChainedAttribute(featureName)) {
				appendUnorderedChainedAttributeElements(document, (List<?>) object.eGet(feature),
						featureName, referenceAttributeElement);
			} else if (isStringToStringMap(featureName)) {
				appendStringToStringMapElements(document, referenceAttributeElement, object,
						feature, featureName);
			} else {
				referenceAttributeElement.setAttribute(featureName,
						String.valueOf(object.eGet(feature)));
			}
		} else {
			referenceAttributeElement.setAttribute(UNSET_ATTNAME, UNSET_ATTVALUE_TRUE);
		}
		return referenceAttributeElement;
	}

	private void appendUnorderedChainedAttributeElements(Document document, List<?> attributes,
			String featureName, Element referenceAttributeElement) {
		// iterate over all the attributes
		for (Object attribute : attributes) {
			// create a new element for each attribute
			Element attributeElement = document.createElement(featureName);
			// set the attribute's value into the element
			attributeElement.setAttribute(featureName, String.valueOf(attribute));
			// append the element to the parent
			referenceAttributeElement.appendChild(attributeElement);
		}
	}

	private void appendStringToStringMapElements(Document document, Element parentElement,
			EObject object, EStructuralFeature feature, String featureName) {
		EMap<?, ?> map = (EMap<?, ?>) object.eGet(feature);
		// iterate over the map
		for (Entry<?, ?> entry : map.entrySet()) {
			// create a new element for each entry in the map
			Element entryElement = document.createElement(featureName);
			// set the string keys and values into the element
			entryElement.setAttribute(ENTRY_ATTVALUE_KEY, (String) entry.getKey());
			entryElement.setAttribute(ENTRY_ATTVALUE_VALUE, (String) entry.getValue());
			// append the element to the parent
			parentElement.appendChild(entryElement);
		}
	}

	private static boolean isStringToStringMap(String featureName) {
		return featureName.equals(APPLICATIONELEMENT_PERSISTEDSTATE_ATTNAME)
				|| featureName.equals(CONTEXT_PROPERTIES_ATTNAME);
	}

	/**
	 * Determines whether this feature is a direct reference to a particular object. That is, the
	 * reference is a 1-1 relationship and the referenced object is not referred by other objects
	 * and is a hard containment feature.
	 * <p>
	 * An example of a direct reference would be a window's main menu. The menu is "owned" by the
	 * window and cannot be one of the menus in a part.
	 * </p>
	 *
	 * @param featureName
	 *            the name of the interested feature
	 * @return <code>true</code> if this particular feature directly references and "owns" the
	 *         target object, <code>false</code> otherwise
	 * @see #isIndirectReference(String)
	 */
	private static boolean isDirectReference(String featureName) {
		// a Window has a single reference to a menu
		return featureName.equals(WINDOW_MAINMENU_ATTNAME) ||
		// a Part has a single reference to a tool bar
				featureName.equals(PART_TOOLBAR_ATTNAME) ||
				// a UIElement has a single reference to a visibleWhen
				featureName.equals(UIELEMENT_VISIBLEWHEN_ATTNAME);
	}

	/**
	 * Determines whether this feature is an indirect reference to a particular object. That is, the
	 * reference refers to an object and this reference does not necessarily describe containment.
	 * <p>
	 * An example of an indirect reference would be a handler's command. The handler points to a
	 * command but the command is actually "owned" by an application in its list of commands. In
	 * this case, there is no containment involved between the handler and the command.
	 * </p>
	 * <p>
	 * Another example of an indirect reference would be a element container's active child. In this
	 * case, the element container's contains the active child in its list of children and both this
	 * listing and the active child reference is a containment feature.
	 * </p>
	 *
	 * @param featureName
	 *            the name of the interested feature
	 * @return <code>true</code> if this particular feature directly references and "owns" the
	 *         target object, <code>false</code> otherwise
	 * @see #isDirectReference(String)
	 */
	private static boolean isIndirectReference(String featureName) {
		// an ElementContainer has a single reference to its active child
		return featureName.equals(ELEMENTCONTAINER_SELECTEDELEMENT_ATTNAME) ||
		// a Handler has a single reference to a command
				featureName.equals(HANDLER_COMMAND_ATTNAME) ||
				// a KeyBinding has a single reference to a command
				featureName.equals(KEYBINDING_COMMAND_ATTNAME) ||
				// a Placeholder has a single reference to a ui element
				featureName.equals(PLACEHOLDER_REF_NAME) ||
				// a BindingTable has a single reference to a bindingContext
				featureName.equals(BINDINGTABLE_BINDINGCONTEXT_ATTNAME);
	}

	/**
	 * Returns whether the feature is a reference to another object.
	 *
	 * @param featureName
	 *            the name of the feature
	 * @return <code>true</code> if this particular feature is a reference to another object,
	 *         <code>false</code> otherwise
	 */
	private static boolean isSingleReference(String featureName) {
		return isDirectReference(featureName) || isIndirectReference(featureName);
	}

	/**
	 * Returns whether the feature is a list of object references.
	 *
	 * @param featureName
	 *            the name of the feature
	 * @return <code>true</code> if this particular feature is referring to a list of object
	 *         references, <code>false</code> otherwise
	 */
	private static boolean isChainedReference(String featureName) {
		// an ElementContainer has multiple children
		return featureName.equals(ELEMENTCONTAINER_CHILDREN_ATTNAME) ||
		// a BindingContainer has multiple bindings
				featureName.equals(BINDINGTABLE_BINDINGS_ATTNAME) ||
				// a Part has multiple menus
				featureName.equals(PART_MENUS_ATTNAME) ||
				// an Application has multiple commands
				featureName.equals(APPLICATION_COMMANDS_ATTNAME) ||
				// a SnippetContainer has multiple snippets
				featureName.equals(SNIPPETCONTAINER_SNIPPETS_ATTNAME) ||
				// a HandlerContainer has multiple handlers
				featureName.equals(HANDLERCONTAINER_HANDLERS_ATTNAME) ||
				// a BindingContainer has multiple binding tables
				featureName.equals(BINDINGCONTAINER_BINDINGTABLES_ATTNAME) ||
				// a TrimmedWindow has multiple trim bars
				featureName.equals(TRIMMEDWINDOW_TRIMBARS_ATTNAME) ||
				// a Window has multiple shared elements
				featureName.equals(WINDOW_SHAREDELEMENTS_ATTNAME) ||
				// a MenuContributions has multiple menu contributions
				featureName.equals(MENUCONTRIBUTIONS_MENUCONTRIBUTIONS_ATTNAME) ||
				// a Command has multiple (command) parameters
				featureName.equals(COMMAND_PARAMETERS_ATTNAME) ||
				// a HandledItem has multiple parameters
				featureName.equals(HANDLEDITEM_PARAMETERS_ATTNAME) ||
				// a ToolBarContributions has multiple tool bar contributions
				featureName.equals(TOOLBARCONTRIBUTIONS_TOOLBARCONTRIBUTIONS_ATTNAME) ||
				// a TrimContributions has multiple trim contributions
				featureName.equals(TRIMCONTRIBUTIONS_TRIMCONTRIBUTIONS_ATTNAME) ||
				// a Perspective has multiple windows
				featureName.equals(PERSPECTIVE_WINDOWS_ATTNAME) ||
				// a Binding has multiple binding contexts
				featureName.equals(BINDINGS_BINDINGCONTEXTS_ATTNAME) ||
				// a BindingContainer has multiple root contexts
				featureName.equals(BINDINGCONTAINER_ROOTCONTEXT_ATTNAME);
	}

	private static boolean isUnorderedChainedAttribute(String featureName) {
		return featureName.equals(APPLICATIONELEMENT_TAGS_ATTNAME);
	}

	/**
	 * Returns whether this feature should be persisted.
	 *
	 * @param featureName
	 *            the name of the feature
	 * @return <code>true</code> if this particular feature should be persisted, <code>false</code>
	 *         otherwise
	 */
	private static boolean shouldPersist(String featureName) {
		// parent changes are captured by children changes already
		return !featureName.equals(UIELEMENT_PARENT_ATTNAME)
				&& !featureName.equals(PARTDESCRIPTORCONTAINER_DESCRIPTORS_ATTNAME);
	}

	private static Collection<EStructuralFeature> collectFeatures(
			Collection<EStructuralFeature> features, EClass eClass) {
		features.addAll(eClass.getEStructuralFeatures());
		for (EClass superType : eClass.getESuperTypes()) {
			collectFeatures(features, superType);
		}
		return features;
	}

	private static Collection<EStructuralFeature> collectFeatures(EObject object) {
		return collectFeatures(new HashSet<EStructuralFeature>(), object.eClass());
	}

	private static boolean isUnset(Element element) {
		return UNSET_ATTVALUE_TRUE.equals(element.getAttribute(UNSET_ATTNAME));
	}

}
