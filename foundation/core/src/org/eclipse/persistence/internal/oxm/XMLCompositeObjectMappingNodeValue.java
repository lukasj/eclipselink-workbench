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

import java.lang.reflect.Modifier;
import java.util.List;
import javax.xml.namespace.QName;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.converters.XMLConverter;
import org.eclipse.persistence.oxm.record.MarshalRecord;
import org.eclipse.persistence.oxm.record.UnmarshalRecord;
import org.eclipse.persistence.oxm.schema.XMLSchemaReference;
import org.eclipse.persistence.sessions.Session;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: This is how the XML Composite Object Mapping is handled
 * when used with the TreeObjectBuilder.</p>
 */
public class XMLCompositeObjectMappingNodeValue extends XMLRelationshipMappingNodeValue implements NullCapableValue {
    private XMLCompositeObjectMapping xmlCompositeObjectMapping;

    public XMLCompositeObjectMappingNodeValue(XMLCompositeObjectMapping xmlCompositeObjectMapping) {
        super();
        this.xmlCompositeObjectMapping = xmlCompositeObjectMapping;
    }

    public boolean marshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver) {
        return marshal(xPathFragment, marshalRecord, object, session, namespaceResolver, null);
    }

    public boolean marshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver, XMLMarshaller marshaller) {
        if (xmlCompositeObjectMapping.isReadOnly()) {
            return false;
        }

        if (xPathFragment.hasLeafElementType()) {
            marshalRecord.setLeafElementType(xPathFragment.getLeafElementType());
        }

        Object objectValue = xmlCompositeObjectMapping.getAttributeValueFromObject(object);
        if (xmlCompositeObjectMapping.getConverter() != null) {
            Converter converter = xmlCompositeObjectMapping.getConverter();
            if (converter instanceof XMLConverter) {
                objectValue = ((XMLConverter)converter).convertObjectValueToDataValue(objectValue, session, marshaller);
            } else {
                objectValue = converter.convertObjectValueToDataValue(objectValue, session);
            }
        }
        if (null == objectValue) {
            return xmlCompositeObjectMapping.getNodeNullPolicy().compositeObjectMarshal(xPathFragment, marshalRecord, object, session, namespaceResolver);
        }

        if ((marshaller != null) && (marshaller.getMarshalListener() != null)) {
            marshaller.getMarshalListener().beforeMarshal(objectValue);
        }
        XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
        marshalRecord.closeStartGroupingElements(groupingFragment);
        XMLDescriptor descriptor = (XMLDescriptor)session.getDescriptor(objectValue);
        TreeObjectBuilder objectBuilder = (TreeObjectBuilder)descriptor.getObjectBuilder();

        // bug#5035551 - if 'self' add any attributes defined on the composite to the parent
        if (xPathFragment.isSelfFragment()) {
            objectBuilder.marshalAttributes(marshalRecord, objectValue, (org.eclipse.persistence.internal.sessions.AbstractSession)session);
        } else {
            getXPathNode().startElement(marshalRecord, xPathFragment, object, session, namespaceResolver, objectBuilder, objectValue);
        }

        List extraNamespaces = objectBuilder.addExtraNamespacesToNamespaceResolver(descriptor, marshalRecord, session);
        writeExtraNamespaces(extraNamespaces, marshalRecord, session);
        if ((xmlCompositeObjectMapping.getReferenceDescriptor() == null) && (descriptor.getSchemaReference() != null)) {
            addTypeAttributeIfNeeded(descriptor, xmlCompositeObjectMapping, marshalRecord);
        }
        objectBuilder.buildRow(marshalRecord, objectValue, (org.eclipse.persistence.internal.sessions.AbstractSession)session, marshaller);
        if (!xPathFragment.isSelfFragment()) {
            marshalRecord.endElement(xPathFragment, namespaceResolver);
        }
        objectBuilder.removeExtraNamespacesFromNamespaceResolver(marshalRecord, extraNamespaces, session);
        if ((marshaller != null) && (marshaller.getMarshalListener() != null)) {
            marshaller.getMarshalListener().afterMarshal(objectValue);
        }
        return true;
    }

    public boolean startElement(XPathFragment xPathFragment, UnmarshalRecord unmarshalRecord, Attributes atts) {
        try {
            unmarshalRecord.removeNullCapableValue(this);

            XMLDescriptor xmlDescriptor = (XMLDescriptor)xmlCompositeObjectMapping.getReferenceDescriptor();
            if (xmlDescriptor == null) {
                xmlDescriptor = findReferenceDescriptor(unmarshalRecord, atts, xmlCompositeObjectMapping);
            }
            boolean isNull = xmlCompositeObjectMapping.getNodeNullPolicy().valueIsNull(atts);
            if (isNull) {
                xmlCompositeObjectMapping.setAttributeValueInObject(unmarshalRecord.getCurrentObject(), null);
            } else {
                XMLField xmlFld = (XMLField)this.xmlCompositeObjectMapping.getField();
                if (xmlFld.hasLastXPathFragment()) {
                    unmarshalRecord.setLeafElementType(xmlFld.getLastXPathFragment().getLeafElementType());
                }
                processChild(xPathFragment, unmarshalRecord, atts, xmlDescriptor);
            }
        } catch (SAXException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
        return true;
    }

    public void endElement(XPathFragment xPathFragment, UnmarshalRecord unmarshalRecord) {
        try {
            if (null == unmarshalRecord.getChildRecord()) {
                return;
            }
            unmarshalRecord.getChildRecord().endDocument();
            Object object = unmarshalRecord.getChildRecord().getCurrentObject();
            if (xmlCompositeObjectMapping.getConverter() != null) {
                Converter converter = xmlCompositeObjectMapping.getConverter();
                if (converter instanceof XMLConverter) {
                    object = ((XMLConverter)converter).convertDataValueToObjectValue(object, unmarshalRecord.getSession(), unmarshalRecord.getUnmarshaller());
                } else {
                    object = converter.convertDataValueToObjectValue(object, unmarshalRecord.getSession());
                }
            }
            xmlCompositeObjectMapping.setAttributeValueInObject(unmarshalRecord.getCurrentObject(), object);
            unmarshalRecord.setChildRecord(null);
        } catch (SAXException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
    }

    public UnmarshalRecord buildSelfRecord(UnmarshalRecord unmarshalRecord, Attributes atts) {
        try {
            XMLDescriptor xmlDescriptor = (XMLDescriptor)xmlCompositeObjectMapping.getReferenceDescriptor();
            if (xmlDescriptor.hasInheritance()) {
                unmarshalRecord.setAttributes(atts);
                Class clazz = xmlDescriptor.getInheritancePolicy().classFromRow(unmarshalRecord, (org.eclipse.persistence.internal.sessions.AbstractSession)unmarshalRecord.getSession());
                if (clazz == null) {
                    // no xsi:type attribute - look for type indicator on the default root element
                    QName leafElementType = unmarshalRecord.getLeafElementType();

                    // if we have a user-set type, try to get the class from the inheritance policy
                    if (leafElementType != null) {
                        Object indicator = xmlDescriptor.getInheritancePolicy().getClassIndicatorMapping().get(leafElementType);

                        // if the inheritance policy does not contain the user-set type, throw an exception
                        if (indicator == null) {
                            throw DescriptorException.missingClassForIndicatorFieldValue(leafElementType, xmlDescriptor.getInheritancePolicy().getDescriptor());
                        }
                        clazz = (Class)indicator;
                    }
                }

                if (clazz != null) {
                    xmlDescriptor = (XMLDescriptor)unmarshalRecord.getSession().getDescriptor(clazz);
                } else {
                    // since there is no xsi:type attribute, use the reference descriptor set 
                    // on the mapping -  make sure it is non-abstract
                    if (Modifier.isAbstract(xmlDescriptor.getJavaClass().getModifiers())) {
                        // need to throw an exception here
                        throw DescriptorException.missingClassIndicatorField(unmarshalRecord, xmlDescriptor.getInheritancePolicy().getDescriptor());
                    }
                }
            }
            TreeObjectBuilder stob2 = (TreeObjectBuilder)xmlDescriptor.getObjectBuilder();
            UnmarshalRecord childRecord = (UnmarshalRecord)stob2.createRecord();
            childRecord.setSession(unmarshalRecord.getSession());
            childRecord.setXMLReader(unmarshalRecord.getXMLReader());
            childRecord.startDocument(this.xmlCompositeObjectMapping);
            xmlCompositeObjectMapping.setAttributeValueInObject(unmarshalRecord.getCurrentObject(), childRecord.getCurrentObject());
            return childRecord;
        } catch (SAXException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
    }

    public void setNullValue(Object object, Session session) {
        xmlCompositeObjectMapping.setAttributeValueInObject(object, null);
    }

    public boolean isNullCapableValue() {
        XMLField xmlField = (XMLField)xmlCompositeObjectMapping.getField();
        if (xmlField.getLastXPathFragment().isSelfFragment) {
            return false;
        }
        return xmlCompositeObjectMapping.getNodeNullPolicy().isNullCapabableValue();
    }

    protected void addTypeAttributeIfNeeded(XMLDescriptor descriptor, DatabaseMapping mapping, MarshalRecord marshalRecord) {
        XMLSchemaReference xmlRef = descriptor.getSchemaReference();
        if (xmlCompositeObjectMapping.shouldAddXsiType(marshalRecord, descriptor) && (xmlRef != null)) {
            addTypeAttribute(descriptor, marshalRecord, xmlRef.getSchemaContext());
        }
    }
}