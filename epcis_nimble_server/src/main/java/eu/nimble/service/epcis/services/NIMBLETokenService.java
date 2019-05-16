package eu.nimble.service.epcis.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

@Service
public class NIMBLETokenService {

    private static Logger log = LoggerFactory.getLogger(NIMBLETokenService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${data-replication.remote_nimble_epcis_server.url}")
    public String remoteNIMBLEEPCISURL;

    @Value("${data-replication.remote_nimble_epcis_server.username}")
    public String remoteEPCISUsername;

    @Value("${data-replication.remote_nimble_epcis_server.password}")
    public String remoteEPCISPassword;

    //TODO: Clean the code; tranform the project into spring boot project; and then use spring boot configurations
    private static Cache<String, String> tokenCache = Caffeine.newBuilder().maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public String getBearerToken() {
        String tokenCacheKey = remoteEPCISUsername + "_" + remoteEPCISPassword;

        String token = null;

        token = tokenCache.getIfPresent(tokenCacheKey);
        if(token == null)
        {
            token = getBearerToken(remoteEPCISUsername, remoteEPCISPassword);
            if(token != null)
            {
                tokenCache.put(tokenCacheKey, token);
            }
        }

        return token;
    }

    /**
     * Get BearerToken for remote NIMBLE EPCIS server
     *
     * @return BearerToken on success; null, otherwise
     */
    //@Cacheable(value = "tokens", sync = true)
    public String getBearerToken(String username, String password) {

        String url = remoteNIMBLEEPCISURL + "getBearerToken";

        log.info("URL:" + url);

        HttpHeaders headers = new HttpHeaders();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("userID", username)
                .queryParam("password", password);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        String bearerToken = null;
        try {
            HttpEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity,
                    String.class);

            bearerToken = response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Received error during login into NIMBLE platform: " + e.getResponseBodyAsString());
        }

        return bearerToken;
    }
}
