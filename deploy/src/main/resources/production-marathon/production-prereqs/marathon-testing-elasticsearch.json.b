{
    "id": "audit-testing",
    "apps": [{
        "container": {
            "docker": {
                "forcePullImage": true,
                "image": "${projectDockerRegistry}/elasticsearch/elasticsearch",
                "network": "BRIDGE",
                "portMappings": [
                    {
                        "containerPort": 9200,
                        "hostPort": 0,
                        "protocol": "tcp",
                        "servicePort": ${CAF_TESTING_ELASTICSEARCH_HTTP_SERVICE_PORT}
                    },
                    {
                        "containerPort": 9300,
                        "hostPort": 0,
                        "protocol": "tcp",
                        "servicePort": ${CAF_TESTING_ELASTICSEARCH_TRANSPORT_SERVICE_PORT}
                    }
                ],
                "parameters": [
                    { "key": "ulimit", "value": "memlock=-1:-1" },
                    { "key": "ulimit", "value": "nofile=65536:65536" }
                ]
            },
            "type": "DOCKER"
        },
        "cpus": 0.4,
        "env": {
            "ES_JAVA_OPTS": "-Xms256m -Xmx256m",
            "bootstrap.memory_lock": "true",
            "xpack.graph.enabled": "false",
            "xpack.monitoring.enabled": "false",
            "xpack.security.enabled": "false",
            "xpack.watcher.enabled": "false",
            "cluster.name": "${CAF_TESTING_ELASTICSEARCH_CLUSTER_NAME}"
        },
        "healthChecks": [
            {
                "gracePeriodSeconds": 300,
                "intervalSeconds": 60,
                "maxConsecutiveFailures": 3,
                "path": "/",
                "portIndex": 0,
                "protocol": "HTTP",
                "timeoutSeconds": 20
            }
        ],
        "id": "smoke-test-elasticsearch",
        "instances": 1,
        "mem": 2048
    }]
}
