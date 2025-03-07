package com.slt.peotv.apigateway.election;

import org.apache.zookeeper.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class LeaderElection implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final String ELECTION_NAMESPACE = "/election";
    private ZooKeeper zooKeeper;
    private String currentZNodeName;

    public LeaderElection() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, 3000, this);
    }

    public void participateInElection() throws KeeperException, InterruptedException {
        String zNodeFullPath = zooKeeper.create(ELECTION_NAMESPACE + "/node_", new byte[]{},
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        currentZNodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
        determineLeader();
    }

    private void determineLeader() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(children);

        if (children.get(0).equals(currentZNodeName)) {
            System.out.println("I am the leader: " + currentZNodeName);
        } else {
            System.out.println("I am NOT the leader, my node: " + currentZNodeName);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        // Handle node changes if necessary
    }
}
