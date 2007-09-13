/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.parsing;

import java.util.List;

/**
 * INTERNAL
 * <p><b>Purpose</b>: This interface specifies methods to create parse trees
 * and parse tree nodes. 
 * <p><b>Responsibilities</b>:<ul>
 * <li> Used by the EJBQLParser to create an internal representation of an
 * EJBQL query.
 * <li> Abstract from concrete parse tree and parse tree node implementation
 * classes. 
 * <li> The parse is created in a bottom-up fashion. All methods takes any
 * child nodes for the parse tree node to be created as arguments. It is the
 * responsibility of the new<XXX> method to set the parent-child relationship
 * between the returned node any any of the child nodes passed as arguments.
 * </ul>
 */
public interface NodeFactory {

    /** Trim specification constants. */
    public enum TrimSpecification { LEADING, TRAILING, BOTH }
            
    // ------------------------------------------
    // Trees
    // ------------------------------------------

    /** */
    public Object newSelectStatement(int line, int column, 
                                     Object select, Object from, 
                                     Object where, Object groupBy, 
                                     Object having, Object orderBy);

    /** */
    public Object newUpdateStatement(int line, int column, 
                                     Object update, Object set, Object where);

    /** */
    public Object newDeleteStatement(int line, int column, 
                                     Object delete, Object where);

    // ------------------------------------------
    // Top level nodes
    // ------------------------------------------

    /** */
    public Object newSelectClause(int line, int column, 
                                  boolean distinct, List selectExprs);

    /** */
    public Object newFromClause(int line, int column, List varDecls);

    /** */
    public Object newWhereClause(int line, int column, Object condition);

    /** */
    public Object newGroupByClause(int line, int column, List items);

    /** */
    public Object newHavingClause(int line, int column, Object arg);

    /** */
    public Object newOrderByClause(int line, int column, List items);

    /** */
    public Object newUpdateClause(int line, int column, 
                                  String schema, String variable);

    /** */
    public Object newDeleteClause(int line, int column, 
                                  String schema, String variable);

    // ------------------------------------------
    // Variable declaration nodes
    // ------------------------------------------

    /** */
    public Object newRangeVariableDecl(int line, int column, 
                                       String schema, String variable);

    /** */
    public Object newJoinVariableDecl(int line, int column, boolean outer, 
                                      Object path, String variable);

    /** */
    public Object newFetchJoin(int line, int column, boolean outer, Object path);

    /** */
    public Object newCollectionMemberVariableDecl(int line, int column, 
                                                  Object path, String variable);

    /** */
    public Object newVariableDecl(int line, int column, 
                                  Object path, String variable);

    // ------------------------------------------
    // Identifier and path expression nodes
    // ------------------------------------------

    /** */
    public Object newDot(int line, int column, Object left, Object right);

    /** */
    public Object newVariableAccess(int line, int column, String identifier);

    /** */
    public Object newAttribute(int line, int column, String identifier);

    /** */
    public Object newQualifiedAttribute(int line, int column, 
                                        String variable, String attribute);

    // ------------------------------------------
    // Aggregate nodes
    // ------------------------------------------

    /** */
    public Object newAvg(int line, int column, boolean ditinct, Object arg);

    /** */
    public Object newMax(int line, int column, boolean ditinct, Object arg);

    /** */
    public Object newMin(int line, int column, boolean ditinct, Object arg);

    /** */
    public Object newSum(int line, int column, boolean ditinct, Object arg);

    /** */
    public Object newCount(int line, int column, boolean ditinct, Object arg);

    // ------------------------------------------
    // Binary expression nodes
    // ------------------------------------------

    /** */
    public Object newOr(int line, int column, Object left, Object right);
    
    /** */
    public Object newAnd(int line, int column, Object left, Object right);

    /** */
    public Object newEquals(int line, int column, Object left, Object right);

    /** */
    public Object newNotEquals(int line, int column, Object left, Object right);

    /** */
    public Object newGreaterThan(int line, int column, Object left, Object right);

    /** */
    public Object newGreaterThanEqual(int line, int column, 
                                      Object left, Object right);

    /** */
    public Object newLessThan(int line, int column, Object left, Object right);

    /** */
    public Object newLessThanEqual(int line, int column, 
                                   Object left, Object right);
    
    /** */
    public Object newPlus(int line, int column, Object left, Object right);

    /** */
    public Object newMinus(int line, int column, Object left, Object right);

    /** */
    public Object newMultiply(int line, int column, Object left, Object right);

    /** */
    public Object newDivide(int line, int column, Object left, Object right);

    // ------------------------------------------
    // Unary expression nodes
    // ------------------------------------------

    /** */
    public Object newUnaryPlus(int line, int column, Object arg);
    
    /** */
    public Object newUnaryMinus(int line, int column, Object arg);
    
    /** */
    public Object newNot(int line, int column, Object arg);
    
    // ------------------------------------------
    // Conditional expression nodes
    // ------------------------------------------
    
    /** */
    public Object newBetween(int line, int column, boolean not, Object arg, 
                             Object lower, Object upper);

    /** */
    public Object newLike(int line, int column, boolean not, Object string, 
                          Object pattern, Object escape) ;

    /** */
    public Object newEscape(int line, int column, Object arg);

    /** */
    public Object newIn(int line, int column, 
                        boolean not, Object expr, List items);

    /** */
    public Object newIsNull(int line, int column, boolean not, Object expr);

    /** */
    public Object newIsEmpty(int line, int column, boolean not, Object expr) ;

    /** */
    public Object newMemberOf(int line, int column, boolean not, 
                              Object expr, Object collection);

    // ------------------------------------------
    // Parameter nodes
    // ------------------------------------------
 
    /** */
    public Object newPositionalParameter(int line, int colimn, String position);

    /** */
    public Object newNamedParameter(int line, int colimn, String name);
    
    // ------------------------------------------
    // Literal nodes
    // ------------------------------------------

    /** */
    public Object newBooleanLiteral(int line, int column, Object value);
    
    /** */
    public Object newIntegerLiteral(int line, int column, Object value);
    
    /** */
    public Object newLongLiteral(int line, int column, Object value);
    
    /** */
    public Object newFloatLiteral(int line, int column, Object value);

    /** */
    public Object newDoubleLiteral(int line, int column, Object value);

    /** */
    public Object newStringLiteral(int line, int column, Object value);
    
    /** */
    public Object newNullLiteral(int line, int column);
    
    // ------------------------------------------
    // Objects for functions returning strings
    // ------------------------------------------

    /** */
    public Object newConcat(int line, int column, Object left, Object right);

    /** */
    public Object newSubstring(int line, int column, 
                               Object string, Object start, Object length);

    /** */
    public Object newTrim(int line, int column, TrimSpecification trimSpec, 
                          Object trimChar, Object string);

    /** */
    public Object newLower(int line, int column, Object arg);

    /** */
    public Object newUpper(int line, int column, Object arg);

    // ------------------------------------------
    // Objects for functions returning numerics
    // ------------------------------------------

    /** */
    public Object newLocate(int line, int column, 
                            Object pattern, Object arg, Object startPos);

    /** */
    public Object newLength(int line, int column, Object arg);

    /** */
    public Object newAbs(int line, int column, Object arg);

    /** */
    public Object newSqrt(int line, int column, Object arg);

    /** */
    public Object newMod(int line, int column, Object left, Object right);

    /** */
    public Object newSize(int line, int column, Object arg);
    
    // ------------------------------------------
    // Objects for functions returning datetime
    // ------------------------------------------

    /** */
    public Object newCurrentDate(int line, int column);
    
    /** */
    public Object newCurrentTime(int line, int column);
    
    /** */
    public Object newCurrentTimestamp(int line, int column);
    
    // ------------------------------------------
    // Subquery nodes
    // ------------------------------------------

    /** */
    public Object newSubquery(int line, int column, Object select, Object from, Object where, 
                              Object groupBy, Object having);

    /** */
    public Object newExists(int line, int column, boolean not, Object subquery);

    /** */
    public Object newIn(int line, int column, boolean not, Object expr, Object subquery);

    /** */
    public Object newAll(int line, int column, Object subquery);

    /** */
    public Object newAny(int line, int column, Object subquery);

    /** */
    public Object newSome(int line, int column, Object subquery);

    // ------------------------------------------
    // Miscellaneous nodes
    // ------------------------------------------

    /** */
    public Object newAscOrdering(int line, int column, Object arg);

    /** */
    public Object newDescOrdering(int line, int column, Object arg);

    /** */
    public Object newConstructor(int line, int colimn, 
                                 String className, List args);

    /** */
    public Object newSetClause(int line, int colimn, List assignments);

    /** */
    public Object newSetAssignmentClause(int line, int column, 
                                         Object target, Object value);

}