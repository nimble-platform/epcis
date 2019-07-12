package org.oliot.epcis.service.delete;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import eu.nimble.service.epcis.controller.BaseRestController;
import eu.nimble.service.epcis.services.AuthorizationSrv;
import org.bson.BsonDocument;
import org.oliot.epcis.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteDataBankService extends BaseRestController {

    @Autowired
    AuthorizationSrv authorizationSrv;

    private static Logger log = LoggerFactory.getLogger(DeleteDataBankService.class);

    @PostMapping("/deleteDataBank")
    public ResponseEntity<?> deleteDataBank(@RequestParam("type") String type,
                  @RequestHeader(value="Authorization", required=true) String bearerToken) {

        // Check NIMBLE authorization
        String userPartyID = authorizationSrv.checkToken(bearerToken);
        if (userPartyID == null) {
            return new ResponseEntity<>(new String("Invalid AccessToken"), HttpStatus.UNAUTHORIZED);
        }

        if(type.equals("Data")) {
            String[] dataTypes = {"EventData", "MasterData", "ProductionProcessTemplate"};
            for (String dataType : dataTypes) {
                deleteDataByType(dataType, userPartyID);
            }
        } else {
            deleteDataByType(type, userPartyID);
        }
        log.info("Delete all " + type + " for User: " + userPartyID);
        return new ResponseEntity<>("Delete all " + type + " for User: " + userPartyID ,HttpStatus.OK);
    }

    private void deleteDataByType(String type, String userPartyID) {
        MongoCollection<BsonDocument> collection = Configuration.mongoDatabase.getCollection(type,
                BsonDocument.class);
        BasicDBObject document = new BasicDBObject();
        document.append("userPartyID", userPartyID);
        collection.deleteMany(document);
    }
}
