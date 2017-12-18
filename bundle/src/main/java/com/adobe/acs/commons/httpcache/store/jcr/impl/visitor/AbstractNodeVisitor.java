package com.adobe.acs.commons.httpcache.store.jcr.impl.visitor;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.util.TraversingItemVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.httpcache.store.jcr.impl.JCRHttpCacheStoreConstants;

public abstract class AbstractNodeVisitor extends TraversingItemVisitor.Default
{
    private static final Logger log = LoggerFactory.getLogger(AbstractNodeVisitor.class);
    private static final long WARN_THRESHOLD = 2000;

    private final long deltaSaveThreshold;
    private final long startTimeInMs = System.currentTimeMillis();
    private long delta = 0;
    private long evictionCount = 0;
    private long loopCounter = 0;
    private Session session;


    public AbstractNodeVisitor( int maxLevel, long deltaSaveThreshold) {
        super(false, maxLevel);
        this.deltaSaveThreshold = deltaSaveThreshold;
    }

    public void visit(Node node) throws RepositoryException {
        session = node.getSession();
        super.visit(node);
    }

    public void close() throws RepositoryException
    {
        if(delta > 0){
            session.save();
            delta = 0;
        }
    }

    protected void entering(Node node, int level) throws RepositoryException {
        loopCounter++;
        logPossibleOverload();
    }

    protected void leaving(Node node, int level)
            throws RepositoryException{
        logPossibleOverload();
    }

    private void logPossibleOverload(){
        long current = System.currentTimeMillis();
        if((loopCounter % 10 == 0) && startTimeInMs + WARN_THRESHOLD < current){
            log.warn("Visiting the JCR cache with the {} is taking too long! taking {} seconds", getClass().getSimpleName(), (current - startTimeInMs / 1000));
        }
    }


    public static boolean isCacheEntryNode(final Node node) throws RepositoryException
    {
        return node.hasProperty(JCRHttpCacheStoreConstants.PN_ISCACHEENTRYNODE);
    }

    public static boolean isEmptyBucketNode(final Node node) throws RepositoryException
    {
        return  !node.hasProperty(JCRHttpCacheStoreConstants.PN_ISCACHEENTRYNODE)
                && !node.hasNodes()
                && !node.getName().equals(JCRHttpCacheStoreConstants.ROOT_NODE_NAME);
    }

    protected void persistSession() throws RepositoryException
    {
        if(delta > deltaSaveThreshold){
            session.save();
            delta = 0;
        }

        delta++;
        evictionCount++;
    }

    public long getEvictionCount()
    {
        return evictionCount;
    }
}
