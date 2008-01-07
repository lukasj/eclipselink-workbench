/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.internal.oxm;

import java.util.Map;

import org.eclipse.persistence.internal.oxm.record.MarshalContext;
import org.eclipse.persistence.internal.oxm.record.ObjectMarshalContext;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLChoiceCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeDirectCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLMapping;
import org.eclipse.persistence.oxm.record.MarshalRecord;

public class XMLChoiceCollectionMappingMarshalNodeValue extends NodeValue implements ContainerValue {
    private XMLChoiceCollectionMapping xmlChoiceCollectionMapping;
    private Map<XMLField, NodeValue> fieldToNodeValues;
    private NodeValue choiceElementNodeValue;
    private XMLField xmlField;
    
    public XMLChoiceCollectionMappingMarshalNodeValue(XMLChoiceCollectionMapping mapping, XMLField xmlField) {
        this.xmlChoiceCollectionMapping = mapping;
        this.xmlField = xmlField;
        initializeNodeValue();
    }
    
    public boolean isOwningNode(XPathFragment xPathFragment) {
        return choiceElementNodeValue.isOwningNode(xPathFragment);
    }

    public void setFieldToNodeValues(Map<XMLField, NodeValue> fieldToNodeValues) {
        this.fieldToNodeValues = fieldToNodeValues;
    }

    private void initializeNodeValue() {
        XMLMapping xmlMapping = xmlChoiceCollectionMapping.getChoiceElementMappings().get(xmlField);
        if(xmlMapping instanceof XMLCompositeDirectCollectionMapping) {
            choiceElementNodeValue = new XMLCompositeDirectCollectionMappingNodeValue((XMLCompositeDirectCollectionMapping)xmlMapping);
        } else {
            choiceElementNodeValue = new XMLCompositeCollectionMappingNodeValue((XMLCompositeCollectionMapping)xmlMapping);
        }
    }

    public boolean marshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver) {
        Object value = xmlChoiceCollectionMapping.getAttributeValueFromObject(object);
        if(value == null) {
            return false;
        }
        ContainerPolicy cp = getContainerPolicy();
        Object iterator = cp.iteratorFor(value);
        while(cp.hasNext(iterator)) {
            Object nextValue = cp.next(iterator, session);
            marshalSingleValue(xPathFragment, marshalRecord, object, nextValue, session, namespaceResolver, ObjectMarshalContext.getInstance());
        }
        return true;
    }

    public void marshalSingleValue(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, Object value, AbstractSession session, NamespaceResolver namespaceResolver, MarshalContext marshalContext) {
        XMLField associatedField = xmlChoiceCollectionMapping.getClassToFieldMappings().get(value.getClass());
        if(associatedField != null) {
            NodeValue associatedNodeValue = this.fieldToNodeValues.get(associatedField);
            if(associatedNodeValue != null) {
                //Find the correct fragment
                XPathFragment frag = associatedField.getXPathFragment();
                while(frag != null) {
                    if(associatedNodeValue.isOwningNode(frag)) {
                        ContainerValue nestedNodeValue = (ContainerValue)((XMLChoiceCollectionMappingUnmarshalNodeValue)associatedNodeValue).getChoiceElementNodeValue();
                        nestedNodeValue.marshalSingleValue(frag, marshalRecord, object, value, session, namespaceResolver, marshalContext);
                        break;
                    }
                    frag = frag.getNextFragment();
                    //if next frag is null, call node value before the loop breaks
                     if(frag == null) {
                        ContainerValue nestedNodeValue = (ContainerValue)((XMLChoiceCollectionMappingUnmarshalNodeValue)associatedNodeValue).getChoiceElementNodeValue();
                        nestedNodeValue.marshalSingleValue(frag, marshalRecord, object, value, session, namespaceResolver, marshalContext);
                    }
                }
            }
        }             
    }
    public boolean isMarshalNodeValue() {
        return true;
    }
    
    public boolean isUnmarshalNodeValue() {
        return false;
    }

    public Object getContainerInstance() {
        return getContainerPolicy().containerInstance();
    }

    public void setContainerInstance(Object object, Object containerInstance) {
        xmlChoiceCollectionMapping.setAttributeValueInObject(object, containerInstance);
    }

    public ContainerPolicy getContainerPolicy() {
        return xmlChoiceCollectionMapping.getContainerPolicy();
    }
    
    public boolean isContainerValue() {
        return true;
    }  
    
    public XMLChoiceCollectionMapping getMapping() {
        return xmlChoiceCollectionMapping;
    }    
    
}