package eu.nimble.service.epcis.db;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
* Created by Quan Deng, 2019
*/

@Configuration
public class MongoConfig {
    private static Logger log = LoggerFactory.getLogger(MongoConfig.class);

//    @Value("${spring.data.mongodb.host}")
//    public String mongoHost;
//
//    @Value("${spring.data.mongodb.port}")
//    public String mongoPort;

    @Value("${spring.data.mongodb.database}")
    public String mongoDB;
    
    @Value("${spring.data.mongodb.uri}")
    public String mongoURI;
    

    /**
     * MongoTemplate Bean
     * @param mongoDbFactory
     * @return
     */
    @Bean
    public MongoOperations mongoTemplate(){
        return new MongoTemplate(mongoDbFactory());
    }
    
    /**
     * MongoDbFactory bean
     * @return
     */
    @Bean
    public MongoDbFactory mongoDbFactory(){
        return new SimpleMongoDbFactory(mongoClient(), mongoDB);
    }
    
    /**
     * MongoClient bean
     * @return
     */
    @Bean
    public MongoClient mongoClient(){
    	
//    	if(mongoURI == null || mongoURI.isEmpty())
//    	{
//    		return  new MongoClient(mongoHost + ":" + mongoPort);
//    	}
    	
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            }).build();
            
            MongoClientOptions.Builder builder = new MongoClientOptions.Builder().sslEnabled(true).sslInvalidHostNameAllowed(true).socketFactory(sslContext.getSocketFactory());
            MongoClientURI connectionString = new MongoClientURI(mongoURI, builder);
            
            log.info("Connection String: " +  connectionString + ", database name: " + mongoDB);
            return new MongoClient(connectionString);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    
    /**
     * MongoDatabase bean
     * @return
     */
    @Bean
    public MongoDatabase mongoDatabase()
    {
    	return mongoClient().getDatabase(mongoDB);
    }

}