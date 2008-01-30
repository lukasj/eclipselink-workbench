/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.scplugin.model.adapter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.persistence.tools.workbench.utility.CollectionTools;
import org.eclipse.persistence.tools.workbench.utility.iterators.CloneIterator;
import org.eclipse.persistence.tools.workbench.utility.iterators.TransformationIterator;

import org.eclipse.persistence.internal.sessions.factories.model.pool.ConnectionPoolConfig;
import org.eclipse.persistence.internal.sessions.factories.model.pool.PoolsConfig;
import org.eclipse.persistence.internal.sessions.factories.model.pool.ReadConnectionPoolConfig;
import org.eclipse.persistence.internal.sessions.factories.model.pool.WriteConnectionPoolConfig;

/**
 * Session Configuration model adapter class for the 
 * TopLink Foudation Library class PoolsConfig
 * 
 * @see PoolsConfig
 * 
 * @author Tran Le
 */
final class PoolsAdapter extends SCAdapter {

	private Collection pools;
	private volatile ReadConnectionPoolAdapter readConnectionPool;
	public final static String READ_CONNECTION_POOL_CONFIG_PROPERTY = "readConnectionPoolConfig";
	private volatile WriteConnectionPoolAdapter writeConnectionPool;
	public final static String WRITE_CONNECTION_POOL_CONFIG_PROPERTY = "writeConnectionPoolConfig";
	private volatile ConnectionPoolAdapter sequenceConnectionPool;
	public final static String SEQUENCE_CONNECTION_POOL_CONFIG_PROPERTY = "sequenceConnectionPoolConfig";

	/**
	 * Creates a new Pools for the specified model object.
	 */
	PoolsAdapter( SCAdapter parent, PoolsConfig scConfig) {
		
		super( parent, scConfig);
	}
	/**
	 * Creates a new Pools.
	 */
	protected PoolsAdapter( SCAdapter parent) {
		
		super( parent);
	}
	/**
	 * Subclasses should override this method to add their children
	 * to the specified collection.
	 * @see #children()
	 */
	protected void addChildrenTo( List children) {
		super.addChildrenTo( children);
		
		synchronized (pools) { children.addAll( this.pools); }
		
		if( this.readConnectionPool != null) 
			children.add( this.readConnectionPool);

		if( this.writeConnectionPool != null) 
			children.add( this.writeConnectionPool);

		if( this.sequenceConnectionPool != null) 
			children.add( this.sequenceConnectionPool);
	}
	/**
	 * Factory method for building this model.
	 */
	protected Object buildModel() {
		return new PoolsConfig();
	}
	/**
	 * Factory method for adding a read pool.
	 * ReadConnectionPool is stored in its corresponding instance variable.
	 */
	ReadConnectionPoolAdapter addReadConnectionPool() {
		
		if( this.readConnectionPool == null) {
			this.readConnectionPool = new ReadConnectionPoolAdapter( this);
			this.config().setReadConnectionPoolConfig(( ReadConnectionPoolConfig)this.readConnectionPool.getModel());
		}
		return this.readConnectionPool;
	}
	/**
	 * Factory method for adding a sequence pool.
	 */
	ConnectionPoolAdapter addSequenceConnectionPool() {
		
		if( this.sequenceConnectionPool == null) {
			this.sequenceConnectionPool = new ConnectionPoolAdapter( this, ConnectionPoolAdapter.SEQUENCE_CONNECTION_POOL_NAME);
			this.config().setSequenceConnectionPoolConfig(( ConnectionPoolConfig)this.sequenceConnectionPool.getModel());
		}		
		return this.sequenceConnectionPool;
	}
	/**
	 * Factory method for adding a write pool.
	 * WriteConnectionPool is stored in its corresponding instance variable.
	 */
	WriteConnectionPoolAdapter addWriteConnectionPool() {
		
		if( this.writeConnectionPool == null) {
			this.writeConnectionPool = new WriteConnectionPoolAdapter( this);
			this.config().setWriteConnectionPoolConfig(( WriteConnectionPoolConfig)this.writeConnectionPool.getModel());
		}		
		return this.writeConnectionPool;
	}
	/**
	 * Factory method for adding a pool.
	 */
	ConnectionPoolAdapter addConnectionPoolNamed( String name) {
		
		ConnectionPoolAdapter namedPool = new ConnectionPoolAdapter( this, name);
		
		return this.addPool( namedPool);
	}
	/**
	 * Remove the pool with the given name.
	 */
	ConnectionPoolAdapter removeConnectionPoolNamed( String name) {
		
		ConnectionPoolAdapter pool = this.poolNamed( name);
		
		if( pool != null) {
			if( pool == this.readConnectionPool || pool == this.sequenceConnectionPool)
				throw new UnsupportedOperationException( "Read or Sequence pool cannot be removed.");

			if( pool == this.writeConnectionPool) {
				this.removeWriteConnectionPool();
			}
			else {
				this.removePool( pool);
			}
		}
		return pool;
	}
	
	ConnectionPoolAdapter removeWriteConnectionPool() {
		ConnectionPoolAdapter writePool = this.writeConnectionPool;
		
		// remove config
		this.config().setWriteConnectionPoolConfig( null);
		// remove adapter
		this.writeConnectionPool = null;
		return writePool;
	}
	
	ConnectionPoolAdapter removeSequenceConnectionPool() {	
		ConnectionPoolAdapter sequencePool = this.sequenceConnectionPool;
		
		// remove config
		this.config().setSequenceConnectionPoolConfig( null);
		// remove adapter
		this.sequenceConnectionPool = null;
		return sequencePool;
	}
	/**
	 * Adds the given pool.
	 */
	private ConnectionPoolAdapter addPool( ConnectionPoolAdapter poolAdapter) {
		// add config
		this.getConnectionPoolConfigs().add( poolAdapter.getModel());
		// add adapter
		this.getPools().add( poolAdapter);
		
		return poolAdapter;
	}
	/**
	 * Removes the given pool.
	 */
	private void removePool( ConnectionPoolAdapter poolAdapter) {	
		// remove config
		this.getConnectionPoolConfigs().remove( poolAdapter.getModel());
		// remove adapter
		this.getPools().remove( poolAdapter);
	}
	/**
	 * Initializes this adapter.
	 */
	protected void initialize() {
		super.initialize();
	
		this.setConfigRequired( true);
		this.pools = new Vector();
	}
	/**
	 * Initializes this new model.
	 * ReadConnectionPool, WriteConnectionPool, and SequenceConnectionPool 
	 * are stored in their corresponding instance variable. 
	 */
	protected void initialize( Object newConfig) {
		super.initialize( newConfig);

		if( !this.platformIsXml()) {
			this.addReadConnectionPool();
			this.addWriteConnectionPool();
		}
	}
	/**
	 * Initializes this adapter from the specified config model.
	 */
	protected void initializeFromModel( Object scConfig) {
		
		super.initializeFromModel( scConfig);
	
		if( !this.platformIsXml()) {
//TOREVIEW 
//			if( this.getReadConnectionPoolConfig() == null || this.getWriteConnectionPoolConfig() == null)
//				throw new NoSuchElementException( this.displayString() + " ReadConnectionPool or WriteConnectionPool not found.");
		
			this.readConnectionPool = ( ReadConnectionPoolAdapter)this.adapt( config().getReadConnectionPoolConfig());
			this.writeConnectionPool = ( WriteConnectionPoolAdapter)this.adapt( config().getWriteConnectionPoolConfig());
			this.sequenceConnectionPool = ( ConnectionPoolAdapter)this.adapt( config().getSequenceConnectionPoolConfig());
			this.pools.addAll( this.adaptAll( this.getConnectionPoolConfigs()));
		}
	}

	public boolean platformIsRdbms() {

		return (( SessionAdapter)getParent()).platformIsRdbms();
	}
	
	public boolean platformIsEis() {

		return (( SessionAdapter)getParent()).platformIsEis();
	}
	
	public boolean platformIsXml() {

		return (( SessionAdapter)getParent()).platformIsXml();
	}
	/**
	 * Returns this pools adapters collection.
	 */
	private Collection getPools() {
		
		return this.pools;
	}
	/**
	 * Returns a collection of session names.
	 */
	Collection getPoolNames() {

		return CollectionTools.collection(new TransformationIterator(pools())
		{
			protected Object transform(Object next) {
				return (( ConnectionPoolAdapter) next).getName();
			}
		});
	}
	/**
	 * Returns an iterator on a collection of pools adapters.
	 */
	Iterator pools() {
		
		return new CloneIterator(pools);
	}
	
	int poolsSize() {
		
		return pools.size();
	}
	/**
	 * Returns the collection of pools from the config model.
	 */
	private Collection getConnectionPoolConfigs() {
		
		return this.config().getConnectionPoolConfigs();
	}
	
	private ReadConnectionPoolConfig getReadConnectionPoolConfig() {
		
		return this.config().getReadConnectionPoolConfig();
	}
	
	ReadConnectionPoolAdapter getReadConnectionPool() {
		
		return this.readConnectionPool;
	}
	
	private ConnectionPoolConfig getWriteConnectionPoolConfig() {
		
		return this.config().getWriteConnectionPoolConfig();
	}
	
	ConnectionPoolAdapter getWriteConnectionPool() {
		
		return this.writeConnectionPool;
	}
	
	private ConnectionPoolConfig getSequenceConnectionPoolConfig() {
		
		return this.config().getSequenceConnectionPoolConfig();
	}
	
	ConnectionPoolAdapter getSequenceConnectionPool() {
		
		return this.sequenceConnectionPool;
	}

	/**
	 * Returns the appropriate pool.
	 */
	ConnectionPoolAdapter poolNamed( String name) {

		for( Iterator i = pools(); i.hasNext();) {
			ConnectionPoolAdapter pool = ( ConnectionPoolAdapter) i.next();
			if( name.equals( pool.getName()))
				return pool;
		}	
		return null;	
	}
	/**
	 * Returns the adapter's Config Model Object.
	 */
	private final PoolsConfig config() {
		
		return ( PoolsConfig)this.getModel();
	}

	
}