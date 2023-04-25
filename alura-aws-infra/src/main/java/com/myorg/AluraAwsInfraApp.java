package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class AluraAwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        AluraVPCStack aluraVPC = new AluraVPCStack(app,"vpc");
        
        AluraRdsStack aluraRDS = new AluraRdsStack(app,"rds", aluraVPC.getVpc());
        aluraRDS.addDependency(aluraVPC);
        
        AluraClusterStack aluraCluster = new AluraClusterStack(app, "cluster", aluraVPC.getVpc());
        aluraCluster.addDependency(aluraVPC);
        
        AluraServiceStack aluraService = new AluraServiceStack(app, "service", aluraCluster.getCluster());
        aluraService.addDependency(aluraCluster);
        aluraService.addDependency(aluraRDS);
        app.synth();
    }
}

