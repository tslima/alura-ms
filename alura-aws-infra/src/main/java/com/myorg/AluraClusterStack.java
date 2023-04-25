package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;

public class AluraClusterStack extends Stack {
	private Cluster cluster;
	
    public AluraClusterStack(final Construct scope, final String id, Vpc vpc) {
        this(scope, id,vpc, null);
    }

    public AluraClusterStack(final Construct scope, final String id,  Vpc vpc, final StackProps props) {
        super(scope, id, props);
    
        cluster = Cluster.Builder.create(this, "alura-cluster")
        		.clusterName("cluster-alura")
        		.vpc(vpc)
                .build();
    
    }

	public Cluster getCluster() {
		return cluster;
	}
    
}
