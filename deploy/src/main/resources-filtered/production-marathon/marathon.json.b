{
    "id": "audit-prod",
    "apps": [{
        "id": "audit-service",
        "cpus": 0.25,
        "mem": 768,
        "instances": 1,
        "container": {
            "docker": {
                "image": "cafaudit/audit-service:${project.version}",
                "network": "BRIDGE",
                "portMappings": [{
                    "containerPort": 8080,
                    "hostPort": 0,
                    "protocol": "tcp",
                    "servicePort": ${CAF_AUDIT_SERVICE_PORT}
                }],
                "forcePullImage": true
            },
            "type": "DOCKER"
        },
        "env": {
            "_JAVA_OPTIONS": "-Xms384m -Xmx384m",
            "CAF_ELASTIC_HOST_AND_PORT_VALUES": "${CAF_ELASTIC_HOST_AND_PORT_VALUES}",
            "CAF_ELASTIC_NUMBER_OF_REPLICAS": "1",
            "CAF_ELASTIC_NUMBER_OF_SHARDS": "5",
            "CAF_LOG_LEVEL": "INFO"
        },
        "healthChecks": [{
            "gracePeriodSeconds": 300,
            "intervalSeconds": 60,
            "maxConsecutiveFailures": 3,
            "path": "/",
            "portIndex": 0,
            "protocol": "HTTP",
            "timeoutSeconds": 20
        }]
    }]
}
