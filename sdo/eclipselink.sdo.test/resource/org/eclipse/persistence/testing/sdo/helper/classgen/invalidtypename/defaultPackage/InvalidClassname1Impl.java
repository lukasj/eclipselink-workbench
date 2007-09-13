/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package defaultPackage;

import org.eclipse.persistence.sdo.SDODataObject;

public class InvalidClassname1Impl extends SDODataObject implements InvalidClassname1 {

   public static final int START_PROPERTY_INDEX = 0;

   public static final int END_PROPERTY_INDEX = START_PROPERTY_INDEX + 2;

   public InvalidClassname1Impl() {}

   public java.util.List getEmail() {
      return getList(START_PROPERTY_INDEX + 0);
   }

   public void setEmail(java.util.List value) {
      set(START_PROPERTY_INDEX + 0 , value);
   }

   public java.util.List getPhone() {
      return getList(START_PROPERTY_INDEX + 1);
   }

   public void setPhone(java.util.List value) {
      set(START_PROPERTY_INDEX + 1 , value);
   }

   public java.lang.String getCustID() {
      return getString(START_PROPERTY_INDEX + 2);
   }

   public void setCustID(java.lang.String value) {
      set(START_PROPERTY_INDEX + 2 , value);
   }


}
