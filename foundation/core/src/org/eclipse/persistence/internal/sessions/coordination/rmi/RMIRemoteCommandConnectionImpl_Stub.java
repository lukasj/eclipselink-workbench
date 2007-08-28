/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.
package org.eclipse.persistence.internal.sessions.coordination.rmi;

public final class RMIRemoteCommandConnectionImpl_Stub extends java.rmi.server.RemoteStub implements org.eclipse.persistence.internal.sessions.coordination.rmi.RMIRemoteCommandConnection, java.rmi.Remote {
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_executeCommand_0;

    static {
        try {
            $method_executeCommand_0 = org.eclipse.persistence.internal.sessions.coordination.rmi.RMIRemoteCommandConnection.class.getMethod("executeCommand", new java.lang.Class[] { org.eclipse.persistence.sessions.coordination.Command.class });
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError("stub class initialization failed");
        }
    }

    // constructors
    public RMIRemoteCommandConnectionImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }

    // methods from remote interfaces
    // implementation of executeCommand(Command)
    public java.lang.Object executeCommand(org.eclipse.persistence.sessions.coordination.Command $param_Command_1) throws java.rmi.RemoteException {
        try {
            Object $result = ref.invoke(this, $method_executeCommand_0, new java.lang.Object[] { $param_Command_1 }, 9085902066222517268L);
            return ((java.lang.Object)$result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}