/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.jpa.complexaggregate;

import org.eclipse.persistence.tools.schemaframework.*;

public class ComplexAggregateTableCreator extends org.eclipse.persistence.tools.schemaframework.TableCreator {
    public ComplexAggregateTableCreator() {
        setName("EJB3ComplexAggregateProject");

        addTableDefinition(buildCITYSLICKERTable());
        addTableDefinition(buildCOUNTRYDWELLERTable());
        addTableDefinition(buildHOCKEYPLAYERTable());
        //addTableDefinition(buildHOCKEYSEQTable());
        addTableDefinition(buildHOCKEYTEAMTable());
        addTableDefinition(buildWORLDTable());
    }

    public TableDefinition buildCITYSLICKERTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_CITYSLICKER");

        FieldDefinition fieldFNAME = new FieldDefinition();
        fieldFNAME.setName("FNAME");
        fieldFNAME.setTypeName("VARCHAR2");
        fieldFNAME.setSize(20);
        fieldFNAME.setSubSize(0);
        fieldFNAME.setIsPrimaryKey(true);
        fieldFNAME.setIsIdentity(false);
        fieldFNAME.setUnique(true);
        fieldFNAME.setShouldAllowNull(false);
        table.addField(fieldFNAME);
        
        FieldDefinition fieldLNAME = new FieldDefinition();
        fieldLNAME.setName("LNAME");
        fieldLNAME.setTypeName("VARCHAR2");
        fieldLNAME.setSize(20);
        fieldLNAME.setSubSize(0);
        fieldLNAME.setIsPrimaryKey(true);
        fieldLNAME.setIsIdentity(false);
        fieldLNAME.setUnique(true);
        fieldLNAME.setShouldAllowNull(false);
        table.addField(fieldLNAME);
        
        FieldDefinition fieldAGE = new FieldDefinition();
        fieldAGE.setName("AGE");
        fieldAGE.setTypeName("NUMBER");
        fieldAGE.setSize(15);
        fieldAGE.setSubSize(0);
        fieldAGE.setIsPrimaryKey(false);
        fieldAGE.setIsIdentity(false);
        fieldAGE.setUnique(false);
        fieldAGE.setShouldAllowNull(false);
        table.addField(fieldAGE);
        
        FieldDefinition fieldGENDER = new FieldDefinition();
        fieldGENDER.setName("GENDER");
        fieldGENDER.setTypeName("VARCHAR2");
        fieldGENDER.setSize(6);
        fieldGENDER.setSubSize(0);
        fieldGENDER.setIsPrimaryKey(false);
        fieldGENDER.setIsIdentity(false);
        fieldGENDER.setUnique(false);
        fieldGENDER.setShouldAllowNull(true);
        table.addField(fieldGENDER);

        FieldDefinition fieldWORLDID = new FieldDefinition();
        fieldWORLDID.setName("WORLD_ID");
        fieldWORLDID.setTypeName("NUMBER");
        fieldWORLDID.setSize(10);
        fieldWORLDID.setSubSize(0);
        fieldWORLDID.setIsPrimaryKey(false);
        fieldWORLDID.setIsIdentity(false);
        fieldWORLDID.setUnique(false);
        fieldWORLDID.setShouldAllowNull(true);
        fieldWORLDID.setForeignKeyFieldName("CMP3_WORLD.ID");
        table.addField(fieldWORLDID);
        
        return table;
    }
    
    public TableDefinition buildCOUNTRYDWELLERTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_COUNTRY_DWELLER");

        FieldDefinition fieldFNAME = new FieldDefinition();
        fieldFNAME.setName("FNAME");
        fieldFNAME.setTypeName("VARCHAR2");
        fieldFNAME.setSize(20);
        fieldFNAME.setSubSize(0);
        fieldFNAME.setIsPrimaryKey(true);
        fieldFNAME.setIsIdentity(false);
        fieldFNAME.setUnique(true);
        fieldFNAME.setShouldAllowNull(false);
        table.addField(fieldFNAME);
        
        FieldDefinition fieldLNAME = new FieldDefinition();
        fieldLNAME.setName("LNAME");
        fieldLNAME.setTypeName("VARCHAR2");
        fieldLNAME.setSize(20);
        fieldLNAME.setSubSize(0);
        fieldLNAME.setIsPrimaryKey(true);
        fieldLNAME.setIsIdentity(false);
        fieldLNAME.setUnique(true);
        fieldLNAME.setShouldAllowNull(false);
        table.addField(fieldLNAME);
        
        FieldDefinition fieldAGE = new FieldDefinition();
        fieldAGE.setName("AGE");
        fieldAGE.setTypeName("NUMBER");
        fieldAGE.setSize(15);
        fieldAGE.setSubSize(0);
        fieldAGE.setIsPrimaryKey(false);
        fieldAGE.setIsIdentity(false);
        fieldAGE.setUnique(false);
        fieldAGE.setShouldAllowNull(false);
        table.addField(fieldAGE);

        FieldDefinition fieldGENDER = new FieldDefinition();
        fieldGENDER.setName("GENDER");
        fieldGENDER.setTypeName("VARCHAR2");
        fieldGENDER.setSize(6);
        fieldGENDER.setSubSize(0);
        fieldGENDER.setIsPrimaryKey(false);
        fieldGENDER.setIsIdentity(false);
        fieldGENDER.setUnique(false);
        fieldGENDER.setShouldAllowNull(true);
        table.addField(fieldGENDER);
        
        FieldDefinition fieldWORLDID = new FieldDefinition();
        fieldWORLDID.setName("WORLD_ID");
        fieldWORLDID.setTypeName("NUMBER");
        fieldWORLDID.setSize(10);
        fieldWORLDID.setSubSize(0);
        fieldWORLDID.setIsPrimaryKey(false);
        fieldWORLDID.setIsIdentity(false);
        fieldWORLDID.setUnique(false);
        fieldWORLDID.setShouldAllowNull(true);
        fieldWORLDID.setForeignKeyFieldName("CMP3_WORLD.ID");
        table.addField(fieldWORLDID);
        
        return table;
    }
    
    public TableDefinition buildHOCKEYPLAYERTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_HOCKEY_PLAYER");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("PLAYERID");
        fieldID.setTypeName("NUMBER");
        fieldID.setSize(19);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        FieldDefinition fieldFNAME = new FieldDefinition();
        fieldFNAME.setName("FNAME");
        fieldFNAME.setTypeName("VARCHAR2");
        fieldFNAME.setSize(20);
        fieldFNAME.setSubSize(0);
        fieldFNAME.setIsPrimaryKey(false);
        fieldFNAME.setIsIdentity(false);
        fieldFNAME.setUnique(false);
        fieldFNAME.setShouldAllowNull(false);
        table.addField(fieldFNAME);
        
        FieldDefinition fieldLNAME = new FieldDefinition();
        fieldLNAME.setName("LNAME");
        fieldLNAME.setTypeName("VARCHAR2");
        fieldLNAME.setSize(20);
        fieldLNAME.setSubSize(0);
        fieldLNAME.setIsPrimaryKey(false);
        fieldLNAME.setIsIdentity(false);
        fieldLNAME.setUnique(false);
        fieldLNAME.setShouldAllowNull(false);
        table.addField(fieldLNAME);
        
        FieldDefinition fieldAGE = new FieldDefinition();
        fieldAGE.setName("AGE");
        fieldAGE.setTypeName("NUMBER");
        fieldAGE.setSize(15);
        fieldAGE.setSubSize(0);
        fieldAGE.setIsPrimaryKey(false);
        fieldAGE.setIsIdentity(false);
        fieldAGE.setUnique(false);
        fieldAGE.setShouldAllowNull(true);
        table.addField(fieldAGE);
        
        FieldDefinition fieldHEIGHT = new FieldDefinition();
        fieldHEIGHT.setName("HEIGHT");
        fieldHEIGHT.setTypeName("DOUBLE PRECIS");
        fieldHEIGHT.setSize(15);
        fieldHEIGHT.setSubSize(0);
        fieldHEIGHT.setIsPrimaryKey(false);
        fieldHEIGHT.setIsIdentity(false);
        fieldHEIGHT.setUnique(false);
        fieldHEIGHT.setShouldAllowNull(true);
        table.addField(fieldHEIGHT);
        
        FieldDefinition fieldWEIGHT = new FieldDefinition();
        fieldWEIGHT.setName("WEIGHT");
        fieldWEIGHT.setTypeName("DOUBLE PRECIS");
        fieldWEIGHT.setSize(15);
        fieldWEIGHT.setSubSize(0);
        fieldWEIGHT.setIsPrimaryKey(false);
        fieldWEIGHT.setIsIdentity(false);
        fieldWEIGHT.setUnique(false);
        fieldWEIGHT.setShouldAllowNull(true);
        table.addField(fieldWEIGHT);
        
        FieldDefinition fieldJERSEYNUMBER = new FieldDefinition();
        fieldJERSEYNUMBER.setName("JERSEY_NUMBER");
        fieldJERSEYNUMBER.setTypeName("NUMBER");
        fieldJERSEYNUMBER.setSize(15);
        fieldJERSEYNUMBER.setSubSize(0);
        fieldJERSEYNUMBER.setIsPrimaryKey(false);
        fieldJERSEYNUMBER.setIsIdentity(false);
        fieldJERSEYNUMBER.setUnique(false);
        fieldJERSEYNUMBER.setShouldAllowNull(true);
        table.addField(fieldJERSEYNUMBER);
        
        FieldDefinition fieldPOSITION = new FieldDefinition();
        fieldPOSITION.setName("POSITION");
        fieldPOSITION.setTypeName("VARCHAR2");
        fieldPOSITION.setSize(20);
        fieldPOSITION.setSubSize(0);
        fieldPOSITION.setIsPrimaryKey(false);
        fieldPOSITION.setIsIdentity(false);
        fieldPOSITION.setUnique(false);
        fieldPOSITION.setShouldAllowNull(true);
        table.addField(fieldPOSITION);
        
        FieldDefinition fieldTEAMID = new FieldDefinition();
        fieldTEAMID.setName("TEAM_ID");
        fieldTEAMID.setTypeName("NUMBER");
        fieldTEAMID.setSize(19);
        fieldTEAMID.setSubSize(0);
        fieldTEAMID.setIsPrimaryKey(false);
        fieldTEAMID.setIsIdentity(false);
        fieldTEAMID.setUnique(false);
        fieldTEAMID.setShouldAllowNull(true);
        fieldTEAMID.setForeignKeyFieldName("CMP3_HOCKEY_TEAM.ID");
        table.addField(fieldTEAMID);
        
        return table;
    }
    
    public static TableDefinition buildHOCKEYSEQTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_HOCKEY_SEQ");

        FieldDefinition fieldSEQ_COUNT = new FieldDefinition();
        fieldSEQ_COUNT.setName("SEQ_COUNT");
        fieldSEQ_COUNT.setTypeName("NUMBER");
        fieldSEQ_COUNT.setSize(15);
        fieldSEQ_COUNT.setSubSize(0);
        fieldSEQ_COUNT.setIsPrimaryKey(false);
        fieldSEQ_COUNT.setIsIdentity(false);
        fieldSEQ_COUNT.setUnique(false);
        fieldSEQ_COUNT.setShouldAllowNull(false);
        table.addField(fieldSEQ_COUNT);

        FieldDefinition fieldSEQ_NAME = new FieldDefinition();
        fieldSEQ_NAME.setName("SEQ_NAME");
        fieldSEQ_NAME.setTypeName("VARCHAR2");
        fieldSEQ_NAME.setSize(80);
        fieldSEQ_NAME.setSubSize(0);
        fieldSEQ_NAME.setIsPrimaryKey(true);
        fieldSEQ_NAME.setIsIdentity(false);
        fieldSEQ_NAME.setUnique(false);
        fieldSEQ_NAME.setShouldAllowNull(false);
        table.addField(fieldSEQ_NAME);

        return table;
    }
    
    public TableDefinition buildHOCKEYTEAMTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_HOCKEY_TEAM");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("NUMBER");
        fieldID.setSize(19);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        FieldDefinition fieldNAME = new FieldDefinition();
        fieldNAME.setName("NAME");
        fieldNAME.setTypeName("VARCHAR2");
        fieldNAME.setSize(20);
        fieldNAME.setSubSize(0);
        fieldNAME.setIsPrimaryKey(false);
        fieldNAME.setIsIdentity(false);
        fieldNAME.setUnique(false);
        fieldNAME.setShouldAllowNull(false);
        table.addField(fieldNAME);
        
        FieldDefinition fieldLEVEL = new FieldDefinition();
        fieldLEVEL.setName("TEAM_LEVEL");
        fieldLEVEL.setTypeName("VARCHAR2");
        fieldLEVEL.setSize(20);
        fieldLEVEL.setSubSize(0);
        fieldLEVEL.setIsPrimaryKey(false);
        fieldLEVEL.setIsIdentity(false);
        fieldLEVEL.setUnique(false);
        fieldLEVEL.setShouldAllowNull(false);
        table.addField(fieldLEVEL);

        FieldDefinition fieldHOMECOLOR = new FieldDefinition();
        fieldHOMECOLOR.setName("HOME_COLOR");
        fieldHOMECOLOR.setTypeName("VARCHAR2");
        fieldHOMECOLOR.setSize(20);
        fieldHOMECOLOR.setSubSize(0);
        fieldHOMECOLOR.setIsPrimaryKey(false);
        fieldHOMECOLOR.setIsIdentity(false);
        fieldHOMECOLOR.setUnique(false);
        fieldHOMECOLOR.setShouldAllowNull(true);
        table.addField(fieldHOMECOLOR);
        
        FieldDefinition fieldAWAYCOLOR = new FieldDefinition();
        fieldAWAYCOLOR.setName("AWAY_COLOR");
        fieldAWAYCOLOR.setTypeName("VARCHAR2");
        fieldAWAYCOLOR.setSize(20);
        fieldAWAYCOLOR.setSubSize(0);
        fieldAWAYCOLOR.setIsPrimaryKey(false);
        fieldAWAYCOLOR.setIsIdentity(false);
        fieldAWAYCOLOR.setUnique(false);
        fieldAWAYCOLOR.setShouldAllowNull(true);
        table.addField(fieldAWAYCOLOR);
        
        return table;
    }
    
     public TableDefinition buildWORLDTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_WORLD");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("NUMBER");
        fieldID.setSize(19);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        return table;
    }
}