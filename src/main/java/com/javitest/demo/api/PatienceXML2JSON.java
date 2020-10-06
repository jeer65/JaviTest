package com.javitest.demo.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

        import javax.xml.bind.JAXBContext;
        import javax.xml.bind.JAXBElement;
        import javax.xml.bind.JAXBException;
        import javax.xml.bind.Unmarshaller;
        import javax.xml.transform.stream.StreamSource;
        import java.io.IOException;
        import java.io.StringReader;
        import java.util.*;

@Slf4j
@RestController
@RequestMapping("api/convert")
public class BadgeXML2MongoController {
    private final BadgeRepository badgeDataRepository;
    private final EmployeeRepository employeeRepository;

    //    @Autowired
    private final String identifier = "BadgeService-" + UUID.randomUUID().toString();

    @Autowired
    public BadgeXML2MongoController(BadgeRepository badgeDataRepository, EmployeeRepository employeeRepository) {
        this.badgeDataRepository = badgeDataRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a Badge data Document .
     *
     * @param documentDto badge in xml data transfer object
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBadgeMongo(@RequestBody String documentDto) throws JAXBException, IOException {

        // Create the JAXB context
        JAXBContext context = JAXBContext.newInstance(BatchTransactions.class);

        // Create an unmarshaller
        Unmarshaller unmarshaller = context.createUnmarshaller();
        try {
            // Unmarshal the XML
//            JAXBElement<batchtransactions> rootElement = unmarshaller.unmarshal(
//                    new StreamSource(new File("src/main/resources/badgeData.xml")),
//                    batchtransactions.class);
            JAXBElement<BatchTransactions> rootElement = unmarshaller.unmarshal(
                    new StreamSource(new StringReader(documentDto)),
                    BatchTransactions.class);
            //Save to the DATABASE
            BatchTransactions batch = rootElement.getValue();


            List<BadgeData> bd = batch.getTransaction();
            bd.forEach(item -> {
                System.out.println(" employee for: " + item.getFirstname());

            });
            //Send data to DB skip duplicates Transactions on the database
            createObjectinDB(bd);
            //Send a MQTT message
            // badgeDataService.sendObjectMQTT(bd);
        }
        catch (Exception e){
            System.out.println( e.getMessage());
        }
    }

    //Save To DB
    public boolean createObjectinDB (List<BadgeData> badgeEmployees) {
        Map<Long, BadgeData> employeeMap = new HashMap();

        ArrayList<BadgeData> listdata = new ArrayList<BadgeData>();
//        List<transaction> filterEmp =
//        badgeEmployees
//                .stream()
//                .filter()
//           filterEmp

        List<Employee> employeesList = new ArrayList<Employee>();
//        for( Employee empl : employees){
//            log.info(empl.toString());
//        }
        badgeEmployees
                .forEach(emp -> {
                    //TODO: Si existe en la otra base de data actualizar esta coleccion yguardarla como la nueva

                    //is the id present if not add it
                    if( verifyEmployee(emp.getGPID()) != null) {
                        employeesList.add(verifyEmployee(emp.getGPID()));
//                    if (!badgeDataRepository.findById(emp.getPPID()).isPresent()) {
//                        BadgeData data = new BadgeData();
//                        data.setPPID(emp.getPPID());
//                        data.setCDSID(emp.getCDSID());
////                        data.setLastname(emp.getLastname());
////                        data.setFirstname(emp.getFirstname());
////                        data.setGlobalId(emp.getGlobalId());
//                        data.setGpid(emp.getGpid());
//                        data.setDepartment(emp.getDepartment());
//                        data.setBadgeNum(emp.getBadgeNum());
//                        listdata.add(data);
                    }
                });

        employeeRepository.saveAll(employeesList);
        //badgeDataRepository.saveAll(badgeEmployees);

        return true;
    }

    /**
     * Verify and return the TourRating for a particular tourId and Customer
     * @param empid employee identifier
     * @return the found Employee
     * @throws NoSuchElementException if no Employee found
     */
    private Employee verifyEmployee(String empid) throws NoSuchElementException {
        return employeeRepository.findBy_idContains(empid).orElseThrow(() ->
                new NoSuchElementException("Employee request("
                        + empid + "doesn't exist"));
    }
}
