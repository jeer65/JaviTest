package com.javitest.demo.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.javitest.demo.model.PatientsXml;
import com.javitest.demo.model.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(path="api/convert")
public class PatienceXML2JSON {
    public PatienceXML2JSON() {
    }

    /**
     * Create a Patience data Document .
     *
     * @param documentDto patience data in xml data transfer object
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String CreateJson(@RequestBody String documentDto) throws JAXBException {
        try {
            // Create the JAXB context
        JAXBContext context = JAXBContext.newInstance(PatientsXml.class);

        // Create an unmarshaller
        Unmarshaller unmarshaller = context.createUnmarshaller();


            JAXBElement<PatientsXml> rootElement = unmarshaller.unmarshal(
                    new StreamSource(new StringReader(documentDto)),
                    PatientsXml.class);
            //Save to the DATABASE
            PatientsXml _patients = rootElement.getValue();


            List<Patient> bd = _patients.getPatients();
            bd.forEach(item -> {
                System.out.println(" patience for: " + item.getName());
                // String json = ow.writeValueAsString(object);

            });
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            try {
                String jsonmqtt = ow.writeValueAsString(bd);
                //PRINT THE DOC IN JSON
                System.out.println(jsonmqtt);
                return jsonmqtt;
            } catch (JsonProcessingException e) {
                System.out.println( e.getMessage());
            }
            return "Error on XML data";
        }
        catch ( JAXBException e){
            System.out.println( e.getMessage());
            return "Error on XML data";
        }
    }

}
