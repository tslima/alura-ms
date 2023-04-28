package com.myorg;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.MemoryUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class AluraServiceStack extends Stack {
    public AluraServiceStack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id,cluster, null);
    }

    public AluraServiceStack(final Construct scope, final String id, Cluster cluster,final StackProps props) {
        super(scope, id, props);
        
        Map<String, String> autenticacao= new HashMap<>();
        autenticacao.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" + Fn.importValue("pedidos-db-endpoint") + ":3306/alurafood-pedidos?createDatabaseIfNotExist=true");
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        autenticacao.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-senha"));
        
        
        ApplicationLoadBalancedFargateService app = ApplicationLoadBalancedFargateService.Builder.create(this, "alura-service")
        .serviceName("alura-service-ola")
        .cluster(cluster)           // Required
        .cpu(512)                   // Default is 256
         .desiredCount(1)           // Default is 1
         .listenerPort(8080)
         .assignPublicIp(true)
         .taskImageOptions(
                 ApplicationLoadBalancedTaskImageOptions.builder()
                         .image(ContainerImage.fromRegistry("tslima/pedidos-ms"))
                         .containerPort(8080)
                         .containerName("app-ola")   
                         .environment(autenticacao)                         
                         .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                 .logGroup(LogGroup.Builder.create(this, "PedidosMsLogGroup")
                                     .logGroupName("PedidosMsLog")
                                     .removalPolicy(RemovalPolicy.DESTROY)
                                     .build())
                                 .streamPrefix("PedidosMS")
                                 .build()))
                         
                         .build())
         .memoryLimitMiB(1024)       // Default is 512
         .publicLoadBalancer(true)   // Default is true
         .build();
        
        app.getTargetGroup().configureHealthCheck(HealthCheck.builder().path("/").build());
        
        ScalableTaskCount scalableTarget = app.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(3)
                .build());
        
        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());
        
        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(65)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());
    }
}
