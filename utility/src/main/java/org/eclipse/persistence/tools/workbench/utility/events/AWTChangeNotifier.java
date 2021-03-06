/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.tools.workbench.utility.events;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * AWT-aware implementation of ChangeNotifier interface:
 * If we are executing on the AWT event-dispatch thread,
 * simply forward the change notification directly to the listener.
 * If we are executing on some other thread, queue up the
 * notification on the AWT event queue so it can be executed
 * on the event-dispatch thread (after the pending events have
 * been dispatched).
 */
public final class AWTChangeNotifier
    implements ChangeNotifier, Serializable
{
    // singleton
    private static ChangeNotifier INSTANCE;

    private static final long serialVersionUID = 1L;


    /**
     * Return the singleton.
     */
    public synchronized static ChangeNotifier instance() {
        if (INSTANCE == null) {
            INSTANCE = new AWTChangeNotifier();
        }
        return INSTANCE;
    }

    /**
     * Ensure non-instantiability.
     */
    private AWTChangeNotifier() {
        super();
    }

    /**
     * @see ChangeNotifier#stateChanged(StateChangeListener, StateChangeEvent)
     */
    @Override
    public void stateChanged(final StateChangeListener listener, final StateChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.stateChanged(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.stateChanged(event);
                    }
                @Override
                    public String toString() {
                        return "stateChanged";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#propertyChange(java.beans.PropertyChangeListener, java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeListener listener, final PropertyChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.propertyChange(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.propertyChange(event);
                    }
                @Override
                    public String toString() {
                        return "propertyChange";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#itemsAdded(CollectionChangeListener, CollectionChangeEvent)
     */
    @Override
    public void itemsAdded(final CollectionChangeListener listener, final CollectionChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.itemsAdded(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.itemsAdded(event);
                    }
                @Override
                    public String toString() {
                        return "itemsAdded (Collection)";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#itemsRemoved(CollectionChangeListener, CollectionChangeEvent)
     */
    @Override
    public void itemsRemoved(final CollectionChangeListener listener, final CollectionChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.itemsRemoved(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.itemsRemoved(event);
                    }
                @Override
                    public String toString() {
                        return "itemsRemoved (Collection)";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#collectionChanged(CollectionChangeListener, CollectionChangeEvent)
     */
    @Override
    public void collectionChanged(final CollectionChangeListener listener, final CollectionChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.collectionChanged(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.collectionChanged(event);
                    }
                @Override
                    public String toString() {
                        return "collectionChanged";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#itemsAdded(ListChangeListener, ListChangeEvent)
     */
    @Override
    public void itemsAdded(final ListChangeListener listener, final ListChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.itemsAdded(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.itemsAdded(event);
                    }
                @Override
                    public String toString() {
                        return "itemsAdded (List)";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#itemsRemoved(ListChangeListener, ListChangeEvent)
     */
    @Override
    public void itemsRemoved(final ListChangeListener listener, final ListChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.itemsRemoved(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.itemsRemoved(event);
                    }
                @Override
                    public String toString() {
                        return "itemsRemoved (List)";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#itemsReplaced(ListChangeListener, ListChangeEvent)
     */
    @Override
    public void itemsReplaced(final ListChangeListener listener, final ListChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.itemsReplaced(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.itemsReplaced(event);
                    }
                @Override
                    public String toString() {
                        return "itemsReplaced (List)";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#listChanged(ListChangeListener, ListChangeEvent)
     */
    @Override
    public void listChanged(final ListChangeListener listener, final ListChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.listChanged(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.listChanged(event);
                    }
                @Override
                    public String toString() {
                        return "listChanged";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#nodeAdded(TreeChangeListener, TreeChangeEvent)
     */
    @Override
    public void nodeAdded(final TreeChangeListener listener, final TreeChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.nodeAdded(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.nodeAdded(event);
                    }
                @Override
                    public String toString() {
                        return "nodeAdded";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#nodeRemoved(TreeChangeListener, TreeChangeEvent)
     */
    @Override
    public void nodeRemoved(final TreeChangeListener listener, final TreeChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.nodeRemoved(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.nodeRemoved(event);
                    }
                @Override
                    public String toString() {
                        return "nodeRemoved";
                    }
                }
            );
        }
    }

    /**
     * @see ChangeNotifier#treeChanged(TreeChangeListener, TreeChangeEvent)
     */
    @Override
    public void treeChanged(final TreeChangeListener listener, final TreeChangeEvent event) {
        if (EventQueue.isDispatchThread()) {
            listener.treeChanged(event);
        } else {
            this.invoke(new Runnable() {
                @Override
                    public void run() {
                        listener.treeChanged(event);
                    }
                @Override
                    public String toString() {
                        return "treeChanged";
                    }
                }
            );
        }
    }

    /**
     * EventQueue.invokeLater(Runnable) seems to work OK;
     * but using #invokeAndWait() can somtimes make things
     * more predictable when debugging.
     */
    private void invoke(Runnable r) {
        EventQueue.invokeLater(r);
//        try {
//            EventQueue.invokeAndWait(r);
//        } catch (InterruptedException ex) {
//            throw new RuntimeException(ex);
//        } catch (java.lang.reflect.InvocationTargetException ex) {
//            throw new RuntimeException(ex);
//        }
    }

    /**
     * Serializable singleton support
     */
    private Object readResolve() {
        return instance();
    }

}
