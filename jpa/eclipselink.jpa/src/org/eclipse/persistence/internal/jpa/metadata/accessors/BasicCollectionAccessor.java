/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.metadata.accessors;

import org.eclipse.persistence.annotations.BasicCollection;
import org.eclipse.persistence.annotations.CollectionTable;

import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAccessibleObject;

import org.eclipse.persistence.internal.jpa.metadata.columns.MetadataColumn;
import org.eclipse.persistence.internal.jpa.metadata.columns.MetadataPrimaryKeyJoinColumn;
import org.eclipse.persistence.internal.jpa.metadata.columns.MetadataPrimaryKeyJoinColumns;

import org.eclipse.persistence.internal.jpa.metadata.tables.MetadataCollectionTable;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.converters.Converter;

/**
 * A basic collection accessor. Assumes a @BasicCollection is defined on the
 * accessible object.
 * 
 * @author Guy Pelletier
 * @since TopLink 11g
 */
public class BasicCollectionAccessor extends DirectAccessor {
    protected MetadataColumn m_valueColumn;
    protected DatabaseTable m_collectionTable;
    protected BasicCollection m_basicCollection;
    
    /**
     * INTERNAL:
     */
    public BasicCollectionAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        
        m_basicCollection = getAnnotation(BasicCollection.class);
        
        // Must check, BasicMapAccessor calls this constructor ...
        if (m_basicCollection != null) {
            m_valueColumn = new MetadataColumn(m_basicCollection.valueColumn(), this);
        }
    }
    
    /**
     * INTERNAL: 
     * 
     * Process a @CollectionTable.
     */
    protected MetadataCollectionTable getCollectionTable() {
        CollectionTable collectionTable = getAnnotation(CollectionTable.class);
        return new MetadataCollectionTable(collectionTable, m_logger);
    }
    
    /**
     * INTERNAL: 
     * 
     * Method ignores logging context. Can't be anything but the value 
     * column for a @BasicCollection. Used with @BasicMap however.
     */
    protected MetadataColumn getColumn(String loggingCtx) {
        return m_valueColumn;
    }
    
    /**
     * INTERNAL:
     * 
     * Return the default table name to be used with the database fields of 
     * this basic collection accessor.
     */
    protected DatabaseTable getDefaultTable() {
        return m_collectionTable;
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicCollectionAccessor)
     */
    public String getFetchType() {
        return m_basicCollection.fetch().name();
    }
    
    /**
     * INTERNAL: (Override from MetadataAccessor)
     * 
     * Return the reference class for this accessor. It will try to extract
     * a reference class from a generic specification. If no generics are used,
     * then it will return void.class. This avoids NPE's when processing
     * JPA converters that can default (Enumerated and Temporal) based on the
     * reference class.
     */
    public Class getReferenceClass() {
        Class cls = getReferenceClassFromGeneric();
        return (cls == null) ? void.class : cls;
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if this accessor represents a basic collection mapping.
     */
    public boolean isBasicCollection() {
        return true;
    }
    
    /**
     * INTERNAL: (Overridden in BasicMapAccessor)
     */
    public void process() {
        // Initialize our mapping.
        DirectCollectionMapping mapping = new DirectCollectionMapping();
        
        // Process common direct collection metadata. This must be done before
        // any field processing since field processing requires that the
        // collection table be processed before hand.
        process(mapping);
        
        // Process the fetch type and set the correct indirection on the mapping.
        setIndirectionPolicy(mapping, null);
        
        // Process the value column (we must process this field before the call
        // to processConverter, since it may set a field classification)
        mapping.setDirectField(getDatabaseField(m_logger.VALUE_COLUMN));
        
        // Process a converter for this mapping. We will look for a @Convert
        // first. If none is found then we'll look for a JPA converter, that 
        // is, @Enumerated, @Lob and @Temporal. With everything falling into 
        // a serialized mapping if no converter whatsoever is found.
        processMappingConverter(mapping);
    }
    
    /**
     * INTERNAL:
     */
    protected void process(DirectCollectionMapping mapping) {
        // Set the attribute name.
        mapping.setAttributeName(getAttributeName());
        
        // Will check for PROPERTY access
        setAccessorMethods(mapping);
        
        // Process private owned.
        mapping.setIsPrivateOwned(hasPrivateOwned());
        
        // Process the collection table.
        processCollectionTable(getCollectionTable(), mapping);
        
        // Process a @ReturnInsert and @ReturnUpdate (to log a warning message)
        processReturnInsertAndUpdate();
        
        // Add the mapping to the descriptor.
        m_descriptor.addMapping(mapping);
    }
    
    /**
     * INTERNAL:
     * 
     * Process a MetadataCollectionTable.
     */
    protected void processCollectionTable(MetadataCollectionTable collectionTable, DirectCollectionMapping mapping) {
        // Build the default table name
        String defaultName = getUpperCaseShortJavaClassName() + "_" + getUpperCaseAttributeName(); 
        
        // Process any table defaults and log warning messages.
        processTable(collectionTable, defaultName);
        
        // Set the reference table on the mapping.
        m_collectionTable = collectionTable.getDatabaseTable();
        mapping.setReferenceTable(m_collectionTable);
        
        // Add all the primaryKeyJoinColumns (reference key fields) to the 
        // mapping. Primary key join column validation is performed in the
        // processPrimaryKeyJoinColumns call.
        MetadataPrimaryKeyJoinColumns primaryKeyJoinColumns = collectionTable.getPrimaryKeyJoinColumns(m_descriptor.getPrimaryTable(), m_collectionTable);
        for (MetadataPrimaryKeyJoinColumn primaryKeyJoinColumn : processPrimaryKeyJoinColumns(primaryKeyJoinColumns)) {
            // The default name is the primary key of the owning entity.
            DatabaseField pkField = primaryKeyJoinColumn.getPrimaryKeyField();
            pkField.setName(getName(pkField, m_descriptor.getPrimaryKeyFieldName(), m_logger.PK_COLUMN));
            
            // The default name is the primary key of the owning entity.
            DatabaseField fkField = primaryKeyJoinColumn.getForeignKeyField();
            fkField.setName(getName(fkField, m_descriptor.getPrimaryKeyFieldName(), m_logger.FK_COLUMN));
            
            // Add the reference key field for the direct collection mapping.
            mapping.addReferenceKeyField(fkField, pkField);
        }
    }
    
    /**
     * INTERNAL: (Overridden in BasicMapAccessor)
     */
    public void setConverter(DatabaseMapping mapping, Converter converter) {
        ((DirectCollectionMapping) mapping).setValueConverter(converter);
    }
    
    /**
     * INTERNAL:
     */
    public void setConverterClassName(DatabaseMapping mapping, String converterClassName) {
        ((DirectCollectionMapping) mapping).setValueConverterClassName(converterClassName);
    }
    
    /**
     * INTERNAL: (Overridden in BasicMapAccessor)
     */
    public void setFieldClassification(DatabaseMapping mapping, Class classification) {
        ((DirectCollectionMapping) mapping).setDirectFieldClassification(classification);
    }
}