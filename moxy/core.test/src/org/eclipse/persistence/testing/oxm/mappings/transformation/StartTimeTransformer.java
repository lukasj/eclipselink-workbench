/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.transformation;

import java.util.Calendar;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.mappings.transformers.*;
/**
 *  @version $Header: StartTimeTransformer.java 07-oct-2005.21:46:10 pkrogh Exp $
 *  @author  mmacivor
 *  @since   release specific (what release of product did this appear in)
 */

public class StartTimeTransformer extends FieldTransformerAdapter 
{
  public Object buildFieldValue(Object instance, String fieldName, Session session) 
  {
    Employee emp = (Employee)instance;
    return emp.getStartTime();
  }
}