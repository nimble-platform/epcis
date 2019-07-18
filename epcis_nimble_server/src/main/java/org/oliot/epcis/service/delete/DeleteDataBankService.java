package org.oliot.epcis.service.delete;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import eu.nimble.service.epcis.controller.BaseRestController;
import eu.nimble.service.epcis.services.AuthorizationSrv;
import io.swagger.annotations.*;
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

@Api(tags = { "EPCIS DataBank Delete" })
@RestController
public class DeleteDataBankService extends BaseRestController {

    @Autowired
    AuthorizationSrv authorizationSrv;

    private static Logger log = LoggerFactory.getLogger(DeleteDataBankService.class);

    @ApiOperation(value = "Delete DataBank based on type", notes = "Delete DataBank based on user and type. A User automatically detect by token. We have 4 available type and they are: <br>" +
            "<textarea disabled style=\"width:98%\" class=\"body-textarea\">" +
            "EventData => To delete all EventData \n" +
            "MasterData => To delete all MasterData \n" +
            "ProductionProcessTemplate => To delete all Production Process Template Data \n" +
            "Data => To delete this all 3 types of data"
            + " </textarea>", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "ObjectId is not valid?"),
            @ApiResponse(code = 401, message = "Unauthorized. Are the headers correct?"), })
    @PostMapping("/deleteDataBank")
    public ResponseEntity<?> deleteDataBank(@ApiParam(value = "Which type of data you want to delete?", required = true)@RequestParam("type") String type,
                @ApiParam(value = "The Bearer token provided by the identity service", required = true)
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
