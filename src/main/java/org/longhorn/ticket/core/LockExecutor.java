package org.longhorn.ticket.core;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.locks.InterProcessMutex;

public class LockExecutor {

    private static final Logger logger = LoggerFactory.getLogger( LockExecutor.class );
    private InterProcessMutex lock;
    private String path;


    public interface CriticalSection {
        public void doExec() throws Exception;
    }

    public LockExecutor( CuratorFramework client, String lockPath ) throws Exception {
        this.path = lockPath;
        lock = new InterProcessMutex( client, lockPath );
    }

    public void execute( CriticalSection  cs ) throws Exception {
        try {
            logger.debug( "acquiring lock to {}", path);
            lock.acquire();
            logger.info( "acquired lock to {}", path);
            cs.doExec();
        } finally {
            logger.debug( "releasing lock to {}", path);
            try {
                lock.release();
            } catch( Exception e ) {
                //do nothing
            }
            logger.debug( "released lock to {}", path);
        }
    }
}
