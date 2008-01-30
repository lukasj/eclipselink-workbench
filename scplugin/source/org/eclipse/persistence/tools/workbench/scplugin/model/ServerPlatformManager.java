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
import org.eclipse.persistence.tools.workbench.utility.iterators.TransformationIterator;


public class ServerPlatformManager extends SCPlatformManager {

	private Map configs;

	public static final String NO_SERVER_ID = "NoServerPlatform";
    public static final String OC4J_11_1_1_ID = "Oc4j_11_1_1_Platform";
    public static final String OC4J_10_1_3_ID = "Oc4j_10_1_3_Platform";
    public static final String OC4J_10_1_2_ID = "Oc4j_10_1_2_Platform";
    public static final String OC4J_9_0_4_ID = "Oc4j_9_0_4_Platform";
    public static final String OC4J_9_0_3_ID = "Oc4j_9_0_3_Platform";
    public static final String WEBLOGIC_10_ID = "WebLogic_10_Platform";
    public static final String WEBLOGIC_9_ID = "WebLogic_9_Platform";
    public static final String WEBLOGIC_8_1_ID = "WebLogic_8_1_Platform";
    public static final String WEBLOGIC_7_0_ID = "WebLogic_7_0_Platform";
    public static final String WEBLOGIC_6_1_ID = "WebLogic_6_1_Platform";
    public static final String WEBSPHERE_6_1_ID = "WebSphere_6_1_Platform";
    public static final String WEBSPHERE_6_0_ID = "WebSphere_6_0_Platform";
    public static final String WEBSPHERE_5_1_ID = "WebSphere_5_1_Platform";
    public static final String WEBSPHERE_5_0_ID = "WebSphere_5_0_Platform";
    public static final String WEBSPHERE_4_0_ID = "WebSphere_4_0_Platform";
    public static final String JBOSS_ID  = "JBossPlatform";
    public static final String SUNAS_ID = "SunAS9ServerPlatform";
    public static final String CUSTOM_SERVER_ID  = "CustomServerPlatform";

	private static ServerPlatformManager INSTANCE;
	
	private ServerPlatformManager() {
		super();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		this.configs = new HashMap();
		this.buildConfigs();
	}

	/**
	 * singleton support
	 */
	public static synchronized ServerPlatformManager instance() {
		if( INSTANCE == null) {
			INSTANCE = new ServerPlatformManager();
		}
		return INSTANCE;
	}	
	
	protected void addConfig( String id, String configClassName) {
		this.configs.put( id, configClassName);
	}
	
	protected void buidPlatforms() {
	    
	    this.addPlatform( NO_SERVER_ID, "org.eclipse.persistence.platform.server.NoServerPlatform");
	    this.addPlatform( OC4J_11_1_1_ID, "org.eclipse.persistence.platform.server.oc4j.Oc4j_11_1_1_Platform");
	    this.addPlatform( OC4J_10_1_3_ID, "org.eclipse.persistence.platform.server.oc4j.Oc4j_10_1_3_Platform");
// as per bug #6194397 these platforms are no longer supported
//	    this.addPlatform( OC4J_10_1_2_ID, "org.eclipse.persistence.platform.server.oc4j.Oc4j_10_1_2_Platform");
//	    this.addPlatform( OC4J_9_0_4_ID, "org.eclipse.persistence.platform.server.oc4j.Oc4j_9_0_4_Platform");
//	    this.addPlatform( OC4J_9_0_3_ID, "org.eclipse.persistence.platform.server.oc4j.Oc4j_9_0_3_Platform");
//	    this.addPlatform( WEBLOGIC_8_1_ID, "org.eclipse.persistence.platform.server.wls.WebLogic_8_1_Platform");
//	    this.addPlatform( WEBLOGIC_7_0_ID, "org.eclipse.persistence.platform.server.wls.WebLogic_7_0_Platform");
//	    this.addPlatform( WEBLOGIC_6_1_ID, "org.eclipse.persistence.platform.server.wls.WebLogic_6_1_Platform");
//	    this.addPlatform( WEBSPHERE_6_0_ID, "org.eclipse.persistence.platform.server.was.WebSphere_6_0_Platform");
//	    this.addPlatform( WEBSPHERE_5_1_ID, "org.eclipse.persistence.platform.server.was.WebSphere_5_1_Platform");
//	    this.addPlatform( WEBSPHERE_5_0_ID, "org.eclipse.persistence.platform.server.was.WebSphere_5_0_Platform");
	    this.addPlatform( WEBLOGIC_9_ID, "org.eclipse.persistence.platform.server.wls.WebLogic_9_Platform");
	    this.addPlatform( WEBLOGIC_10_ID, "org.eclipse.persistence.platform.server.wls.WebLogic_10_Platform");
	    this.addPlatform( WEBSPHERE_6_1_ID, "org.eclipse.persistence.platform.server.was.WebSphere_6_1_Platform");
	    this.addPlatform( JBOSS_ID, "org.eclipse.persistence.platform.server.jboss.JBossPlatform");
	    this.addPlatform( SUNAS_ID, "org.eclipse.persistence.platform.server.sunas.SunAS9ServerPlatform");
	    this.addPlatform( CUSTOM_SERVER_ID, "org.eclipse.persistence.platform.server.CustomServerPlatform");
	}

	protected void buildConfigs() {
	    
	    this.addConfig( NO_SERVER_ID, "null");
	    this.addConfig( OC4J_11_1_1_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.oc4j.Oc4j_11_1_1_PlatformConfig");
	    this.addConfig( OC4J_10_1_3_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.oc4j.Oc4j_10_1_3_PlatformConfig");
//	  as per bug #6194397 these platforms are no longer supported
//	    this.addConfig( OC4J_10_1_2_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.oc4j.Oc4j_10_1_2_PlatformConfig");
//	    this.addConfig( OC4J_9_0_4_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.oc4j.Oc4j_9_0_4_PlatformConfig");
//	    this.addConfig( OC4J_9_0_3_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.oc4j.Oc4j_9_0_3_PlatformConfig");
//	    this.addConfig( WEBLOGIC_8_1_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebLogic_8_1_PlatformConfig");
//	    this.addConfig( WEBLOGIC_7_0_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebLogic_7_0_PlatformConfig");
//	    this.addConfig( WEBLOGIC_6_1_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebLogic_6_1_PlatformConfig");
//	    this.addConfig( WEBSPHERE_6_0_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebSphere_6_0_PlatformConfig");
//	    this.addConfig( WEBSPHERE_5_1_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebSphere_5_1_PlatformConfig");
//	    this.addConfig( WEBSPHERE_5_0_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebSphere_5_0_PlatformConfig");
	    this.addConfig( WEBLOGIC_9_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebLogic_9_PlatformConfig");
	    this.addConfig( WEBLOGIC_10_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebLogic_10_PlatformConfig");
	    this.addConfig( WEBSPHERE_6_1_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.WebSphere_6_1_PlatformConfig");
	    this.addConfig( JBOSS_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.JBossPlatformConfig");
	    this.addConfig( SUNAS_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.SunAS9PlatformConfig");
	    this.addConfig( CUSTOM_SERVER_ID, "org.eclipse.persistence.internal.sessions.factories.model.platform.CustomServerPlatformConfig");
	}

	public Iterator platformShortNames() {
	    
		return new TransformationIterator( this.platformIds()) {
			protected Object transform( Object next) {
				String id = ( String)next;
				
		        return ClassTools.shortNameForClassNamed( getRuntimePlatformClassNameFor( id));
			}
		};
	}
	
	public Iterator configIds() {
	    
	    return this.configs.keySet().iterator();
	}
	
	public Iterator configNames() {
	    
	    return this.configs.values().iterator();
	}
	
	public Iterator configShortNames() {
		return new TransformationIterator( this.configNames()) {
			protected Object transform( Object next) {
				return ClassTools.shortNameForClassNamed(( String)next);
			}
		};
	}
	
	public String getRuntimePlatformConfigClassNameForPlatformId( String platformId) {
	    if (configs.containsKey(platformId)) {
	    	return (String)configs.get(platformId);
	    }
		throw new IllegalArgumentException( "missing platform config named: "
				+ platformId);
	}

}