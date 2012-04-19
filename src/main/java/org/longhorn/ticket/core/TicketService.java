package org.longhorn.ticket.core;
import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger( TicketService.class );

    private static final String NAME_SPACE = "TicketService";
    private static final String ROOT_PATH = "/instances";

    private TicketGenerator generator;
    private CuratorFramework client;
    private String zkConn;
    private int workerId;

    public TicketService(String zkConn){

        this.zkConn = zkConn;
    }

    public void start() {

        connectAndStartZooKeeperClient();
        initGenerator();
        logger.info("Started ticket service" );
    }
    
    private void connectAndStartZooKeeperClient() {
        
        logger.debug( "trying to connect zookeeper at {}", zkConn );
        try{
            client = CuratorFrameworkFactory.builder()
                .retryPolicy(new ExponentialBackoffRetry(100, 1000))
                .connectString( zkConn )
                .namespace( NAME_SPACE )
                .build();
        } catch ( IOException e ) {
            throw new TicketServiceRuntimeException("failed to connect zookeeper at " + zkConn, e);
        }
        client.start();
        logger.info("connected zookeeper at {}", zkConn);
    }

    private void initGenerator() {
        try {
            LockExecutor lockExecutor = new LockExecutor(client, "/id_lock");
            lockExecutor.execute( new LockExecutor.CriticalSection () {
                    public void doExec() throws Exception {
                        createGenerator();
                    }
                } );
        } catch (Exception e){
            throw new TicketServiceRuntimeException( "failed to initialize ticket service", e);
        }
    }
    private void createGenerator() throws Exception {
        findNextAvailableWorkerId();
        holdWorkerId();
        generator = new TicketGenerator( workerId );
        logger.info( "ticket service run as worker " + workerId );
    }

    private void findNextAvailableWorkerId() throws Exception {

        int[] workerIds = new int[ 1 << TicketGenerator.NODE_BITS_LENGTH ];
        List<String> childrenPathList = client.getChildren().forPath(ROOT_PATH);

        if ( childrenPathList.size() >= workerIds.length ) {
            throw new TicketServiceRuntimeException( "ticket service are full at " + workerIds.length );
        }

        for ( String path : childrenPathList ) {
            logger.debug( "childPath: " + path );
            String[] pathSegments = path.split("/");
            int workerId = Integer.parseInt( pathSegments[ pathSegments.length - 1 ] );
            workerIds[ workerId ] = 1;
        }
        workerId = -1;
        for (int i = 0; i < workerIds.length; i++){
            if ( workerIds[i] == 0 ) {
                logger.debug( "found available id " + i );
                workerId = i;
                break;
            }
        }
        if ( workerId < 0 ) {
            throw new TicketServiceRuntimeException( "could not find available ids" );
        }
    }

    private boolean holdWorkerId() throws Exception {
        String path = ROOT_PATH + "/" + workerId;
        
        client.create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.EPHEMERAL)
            .forPath(path);
        logger.info( "successfully hold worker id " + workerId );
        return true;
    }

    public Long nextTicket() {
        return generator.nextTicket();
    }

    public void stop() {
        if ( client != null ) {
            try {
                client.close();
            } catch( Exception e ) {
                // do nothing
                logger.info( "not gracefully close zookeeper client");
            }
        }
    }
}
