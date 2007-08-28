/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.legacy;

import java.math.*;

public class InsuredShipment extends Shipment {
    public double insuranceAmount;

    public InsuredShipment() {
        super();
    }

    public static Shipment example1(Employee employee) {
        InsuredShipment example = new InsuredShipment();

        example.employee = employee;
        example.employeeFirstName = employee.firstName;
        example.employeeLastName = employee.lastName;
        example.shipmentNumber = new BigDecimal(11);
        example.quantityShipped = "2 tons";
        example.shipMode = "Train";
        example.insuranceAmount = 50.00;

        example.orders.addElement(Order.example9(example, employee));
        example.orders.addElement(Order.example10(example, employee));

        return (Shipment)example;
    }

    public static Shipment example2(Employee employee) {
        InsuredShipment example = new InsuredShipment();

        example.employee = employee;
        example.employeeFirstName = employee.firstName;
        example.employeeLastName = employee.lastName;
        example.shipmentNumber = new BigDecimal(12);
        example.quantityShipped = "1 ton";
        example.shipMode = "Air";
        example.insuranceAmount = 1250.00;

        example.orders.addElement(Order.example11(example, employee));
        example.orders.addElement(Order.example12(example, employee));

        return (Shipment)example;
    }

    public static Shipment example3(Employee employee) {
        InsuredShipment example = new InsuredShipment();

        example.employee = employee;
        example.employeeFirstName = employee.firstName;
        example.employeeLastName = employee.lastName;
        example.shipmentNumber = new BigDecimal(13);
        example.quantityShipped = "6 kg";
        example.shipMode = "Mail";
        example.insuranceAmount = 5.00;

        example.orders.addElement(Order.example13(example, employee));
        example.orders.addElement(Order.example14(example, employee));

        return (Shipment)example;
    }
}