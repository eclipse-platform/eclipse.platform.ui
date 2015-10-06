/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     René Brandstetter - Bug 411821 - [QuickAccess] Contribute SearchField
 *                                      through a fragment or other means
 *******************************************************************************/
package org.eclipse.e4.ui.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;

public class ModelUtils {
	//public static final String CONTAINING_CONTEXT = "ModelUtils.containingContext";
	public static final String CONTAINING_PARENT = "ModelUtils.containingParent";

	public static EClassifier getTypeArgument(EClass eClass,
			EGenericType eGenericType) {
		ETypeParameter eTypeParameter = eGenericType.getETypeParameter();

		if( eTypeParameter != null ) {
			for (EGenericType eGenericSuperType : eClass.getEAllGenericSuperTypes()) {
				EList<ETypeParameter> eTypeParameters = eGenericSuperType
						.getEClassifier().getETypeParameters();
				int index = eTypeParameters.indexOf(eTypeParameter);
				if (index != -1
						&& eGenericSuperType.getETypeArguments().size() > index) {
					return getTypeArgument(eClass, eGenericSuperType
							.getETypeArguments().get(index));
				}
			}
			return null;
		} else {
			return eGenericType.getEClassifier();
		}
	}

	public static MApplicationElement findElementById(MApplicationElement element, String id) {
		if (id == null || id.length() == 0) {
			return null;
		}
		// is it me?
		if (id.equals(element.getElementId())) {
			return element;
		}
		// Recurse if this is a container
		EList<EObject> elements = ((EObject) element).eContents();
		for (EObject childElement : elements) {
			if (!(childElement instanceof MApplicationElement)) {
				continue;
			}
			MApplicationElement result = findElementById((MApplicationElement) childElement, id);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static List<MApplicationElement> merge(MApplicationElement container, EStructuralFeature feature, List<MApplicationElement> elements, String positionInList) {
		EObject eContainer = (EObject) container;

		if( feature.isMany() ) {
			List<MApplicationElement> copy = new ArrayList<MApplicationElement>(elements);

			@SuppressWarnings("unchecked")
			List<MApplicationElement> list= (List<MApplicationElement>)eContainer.eGet(feature);
			boolean flag = true;
			if( positionInList != null && positionInList.trim().length() != 0 ) {
				int index = -1;

				PositionInfo posInfo = PositionInfo.parse(positionInList);

				if( posInfo != null ){
				  switch (posInfo.getPosition()){
				    case FIRST:
				      index = 0;
				      break;

				    case INDEX:
				      index = posInfo.getPositionReferenceAsInteger();
				      break;

				    case BEFORE:
				    case AFTER:
				      int tmpIndex = -1;
				      String elementId = posInfo.getPositionReference();

				      for( int i = 0; i < list.size(); i++ ) {
		            if( elementId.equals((list.get(i)).getElementId()) ) {
		              tmpIndex = i;
		              break;
		            }
		          }

				      if( tmpIndex != -1 ) {
		            if( posInfo.getPosition() == Position.BEFORE ) {
		              index = tmpIndex;
		            } else {
		              index = tmpIndex + 1;
		            }
		          } else {
		            System.err.println("Could not find element with Id '"+elementId+"'");
		          }

				    case LAST:
				      default:
				        // both no special operation, because the default is adding it at the last position
				        break;
				  }
				} else {
					System.err.println("Not a valid list position.");
				}


				if( index >= 0 && list.size() > index ) {
					flag = false;
					mergeList(list,  elements, index);
				}
			}

			// If there was no match append it to the list
			if( flag ) {
				mergeList(list,  elements, -1);
			}

			return copy;
		} else {
			if( elements.size() >= 1 ) {
				if( elements.size() > 1 ) {
					//FIXME Pass the logger
					System.err.println("The feature is single valued but a list of values is passed in.");
				}
				MApplicationElement e = elements.get(0);
				eContainer.eSet(feature, e);
				return Collections.singletonList(e);
			}
		}

		return Collections.emptyList();
	}

	private static void mergeList(List<MApplicationElement> list, List<MApplicationElement> elements, int index) {
		MApplicationElement[] tmp = new MApplicationElement[elements.size()];
		elements.toArray(tmp);
		for(MApplicationElement element : tmp) {
			String elementID = element.getElementId();
			boolean found = false;
			if ((elementID != null) && (elementID.length() != 0)) {
				for(Object existingObject : list) {
					if (!(existingObject instanceof MApplicationElement)) {
						continue;
					}
					MApplicationElement existingEObject = (MApplicationElement) existingObject;
					if (!elementID.equals(existingEObject.getElementId())) {
						continue;
					}
					if (EcoreUtil.equals((EObject)existingEObject, (EObject)element)) {
						found = true; // skip
						break;
					} else { // replace
						EObject root = EcoreUtil.getRootContainer((EObject) existingEObject);
						// Replacing the object in the container
						EcoreUtil.replace((EObject)existingEObject, (EObject)element);
						// Replacing the object in other references than the container.
						Collection<Setting> settings = UsageCrossReferencer.find((EObject) existingEObject, root);
						for (Setting setting : settings) {
							setting.set(element);
						}
						found = true;
					}
				}
			}
			if (!found) {
				if (index == -1) {
					list.add(element);
				} else {
					list.add(index, element);
				}
			}
		}
	}

	static MApplicationElement getParent(MApplicationElement element) {
		if ( (element instanceof MUIElement) && ((MUIElement)element).getCurSharedRef() != null) {
			return ((MUIElement)element).getCurSharedRef().getParent();
		} else if (element.getTransientData().get(CONTAINING_PARENT) instanceof MApplicationElement) {
			return (MApplicationElement) element.getTransientData().get(CONTAINING_PARENT);
		} else if (element instanceof EObject) {
			EObject eContainer = ((EObject) element).eContainer();
			if (eContainer instanceof MApplicationElement) {
				return (MApplicationElement) eContainer;
			}
		}
		return null;
	}

	public static IEclipseContext getContainingContext(MApplicationElement element) {
		MApplicationElement curParent = getParent(element);

		while (curParent != null) {
			if (curParent instanceof MContext) {
				return ((MContext) curParent).getContext();
			}
			curParent = getParent(curParent);
		}

		return null;
	}
}
