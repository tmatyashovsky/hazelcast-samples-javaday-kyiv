package com.lohika.morning.hazelcast.presentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.lohika.morning.hazelcast.presentation.cache.store.StoreRepository;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    @Resource
    private StoreRepository storeRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config hazelcastConfig = new Config("hazelcastPresentation");

        configureNetwork(hazelcastConfig);

        configureLogging(hazelcastConfig);

        configureJmxSupport(hazelcastConfig);

        configureManagementCenter(hazelcastConfig);

        configureDistributedCache(hazelcastConfig);

        configureWriteThroughDistributedCache(hazelcastConfig);

        configureExecutorService(hazelcastConfig);

        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }

    @Bean(name = "distributedCache")
    public ConcurrentMap<String, String> distributedCache() {
        return this.hazelcastInstance().getMap("distributedCacheConfig");
    }

    @Bean(name = "writeThroughDistributedCache")
    public ConcurrentMap<String, String> writeThroughDistributedCache() {
        return this.hazelcastInstance().getMap("writeThroughDistributedCacheConfig");
    }

    @Bean(name = "executorService")
    public IExecutorService executorService() {
        return this.hazelcastInstance().getExecutorService("executorServiceConfig");
    }

    private void configureNetwork(Config hazelcastConfig) {
        NetworkConfig hazelcastNetworkConfig = hazelcastConfig.getNetworkConfig();
        hazelcastNetworkConfig.setPort(5801).setPortAutoIncrement(true);

        JoinConfig hazelcastJoinConfig = hazelcastNetworkConfig.getJoin();
        // Hazelcast Multicast configuration.
        hazelcastJoinConfig.getMulticastConfig().setEnabled(false);
        // Hazelcast TCP based network configuration.
        hazelcastJoinConfig.getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
    }

    private void configureDistributedCache(Config hazelcastConfig) {
        MapConfig distributedCacheConfig = new MapConfig("distributedCacheConfig");
        distributedCacheConfig.setBackupCount(0).setTimeToLiveSeconds(600);

        hazelcastConfig.addMapConfig(distributedCacheConfig);
    }

    private void configureExecutorService(Config hazelcastConfig) {
        ExecutorConfig executorConfig = new ExecutorConfig("executorServiceConfig");
        executorConfig.setPoolSize(5);
        hazelcastConfig.getExecutorConfigs().put("executorServiceConfig", executorConfig);
    }

    private void configureWriteThroughDistributedCache(Config hazelcastConfig) {
        // Hazelcast write-through distributed cache config.
        MapConfig writeThroughDistributedCacheConfig = new MapConfig("writeThroughDistributedCacheConfig");
        writeThroughDistributedCacheConfig.setBackupCount(0);
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setEnabled(true)
                      .setClassName("com.lohika.morning.hazelcast.presentation.cache.store.HazelcastMapStore")
                      .setWriteDelaySeconds(0);

        Properties mapStoreConfigProperties = new Properties();
        mapStoreConfigProperties.put("tableName", "presentation");
        mapStoreConfigProperties.put("repository", this.storeRepository);
        mapStoreConfig.setProperties(mapStoreConfigProperties);
        writeThroughDistributedCacheConfig.setMapStoreConfig(mapStoreConfig);

        hazelcastConfig.addMapConfig(writeThroughDistributedCacheConfig);
    }

    private void configureManagementCenter(Config hazelcastConfig) {
        // Management config.
        hazelcastConfig.getManagementCenterConfig().setEnabled(true).setUrl("http://localhost:9090/mancenter");
    }

    private void configureJmxSupport(Config hazelcastConfig) {
        // JMX support.
        Map<String, Object> jmxProperties = new HashMap<String, Object>();
        jmxProperties.put("hazelcast.jmx", true);
        jmxProperties.put("hazelcast.jmx.detailed", true);
        hazelcastConfig.getProperties().putAll(jmxProperties);
    }

    private void configureLogging(Config hazelcastConfig) {
        hazelcastConfig.getProperties().put("hazelcast.logging.type", "sl4j");
    }

}
