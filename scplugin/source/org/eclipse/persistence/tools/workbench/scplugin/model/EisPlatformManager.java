/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.scplugin.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.persistence.tools.workbench.utility.ClassTools;


public class EisPlatformManager extends SCPlatformManager {

    public static final String AQ_ID = "Oracle AQ";
    public static final String ATTUNITY_ID = "Attunity Connect";
    public static final String MQ_ID = "IBM MQSeries";
	
	private static EisPlatformManager INSTANCE;
	
	private Map connectionSpecs;

	private EisPlatformManager() {
		super();
	}
	/**
	 * singleton support
	 */
	public static synchronized EisPlatformManager instance() {
		if( INSTANCE == null) {
			INSTANCE = new EisPlatformManager();
		}
		return INSTANCE;
	}	

	protected void buidPlatforms() {

	    this.addPlatform( AQ_ID, "org.eclipse.persistence.eis.adapters.aq.AQPlatform");
	    this.addPlatform( ATTUNITY_ID, "org.eclipse.persistence.eis.adapters.attunity.AttunityPlatform");
	    this.addPlatform( MQ_ID, "org.eclipse.persistence.eis.adapters.mqseries.MQPlatform");
	}
	
	private void buidConnectionSpecs() {

	    this.connectionSpecs.put( AQ_ID, "org.eclipse.persistence.eis.adapters.aq.AQEISConnectionSpec");
	    this.connectionSpecs.put( ATTUNITY_ID, "org.eclipse.persistence.eis.adapters.attunity.AttunityConnectionSpec");
	    this.connectionSpecs.put( MQ_ID, "org.eclipse.persistence.eis.adapters.mqseries.MQConnectionSpec");
	}
	
	public String getRuntimeConnectionSpecClassName( String platformClassName) {
	    
	    String shortClassName = ClassTools.shortNameForClassNamed( platformClassName);
	    String id = this.getIdFor( shortClassName);
	    return ( String)this.connectionSpecs.get( id);
	}
	
	public Iterator connectionSpecNames() {
	    
	    return this.connectionSpecs.values().iterator();
	}
	
	protected void initialize() {
	    super.initialize();

		this.connectionSpecs = new HashMap();
		this.buidConnectionSpecs();
	}
}