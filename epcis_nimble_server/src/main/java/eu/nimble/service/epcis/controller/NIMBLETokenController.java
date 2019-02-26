package eu.nimble.service.epcis.controller;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


/**
* Created by Quan Deng, 2019
*/

@RestController
public class NIMBLETokenController {
    private static Logger log = LoggerFactory.getLogger(NIMBLETokenController.class);

	@Value("${spring.identity-service.url}")
	private String identityServiceURL;
	
    @Autowired
    private RestTemplate restTemplate;
	
	@PostMapping("/getToken")
	public ResponseEntity<?> getAccessToken(@RequestParam(required = true) String userID,
			@RequestParam(required = true) String password)
	{
		    
        String url = identityServiceURL + "/login";

        log.info("URL:" + url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject json = new JSONObject();
        json.put("username", userID);
        json.put("password", password);
        HttpEntity<String> entity = new HttpEntity<String>(json.toString(),headers);
        
        String token = "";
        try {
	        String result = restTemplate.postForObject(url, entity, String.class);
	        
	        JSONObject jsonUser = new JSONObject(result);
	        token = jsonUser.getString("accessToken");
	        
	        return ResponseEntity.ok(token);
        } catch (HttpStatusCodeException e) {
        	log.error("Received error during login into NIMBLE platform: " + e.getResponseBodyAsString());
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
}
