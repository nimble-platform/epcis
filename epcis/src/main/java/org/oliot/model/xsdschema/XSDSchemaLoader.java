package org.oliot.model.xsdschema;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
* Created by Quan Deng, 2019
*/

public class XSDSchemaLoader {
    private static Logger log = LoggerFactory.getLogger(XSDSchemaLoader.class);

    /**
     * 
     * @param schemaName schema name with file extension e.g. EPCglobal-epcis-1_2.xsd 
     * @return
     */
    public File getXSDSchema(String schemaName)
    {
    	File file = null;
    	
		try {
			file = new ClassPathResource("xsdSchema/" + schemaName).getFile();
		} catch (IOException e) {
			log.error("not able to load xsd schema: " + schemaName);
		}
    	
    	return file;
    }
}
