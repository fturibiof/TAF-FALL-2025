package ca.etsmtl.taf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import ca.etsmtl.taf.repository.ApiBddJmeterRepository;
import ca.etsmtl.taf.entity.ApiBddJmeterEntity;
import org.springframework.context.annotation.Profile;




import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.format.DateTimeFormatter;
@Profile("sql")
@Service
public class ApiBddService {

    @Autowired
    private ApiBddJmeterRepository jMeterResultRepository;

    public void saveResult(String module, JsonNode resultData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        switch (module.toLowerCase()) {
            case "jmeter":
            resultData.get("results").forEach(result -> {
                ApiBddJmeterEntity jmeterResult = new ApiBddJmeterEntity();
                
                // Conversion des valeurs String en types appropriés
                jmeterResult.setTimeStamp(Long.parseLong(result.get("timeStamp").asText()));
                jmeterResult.setDate(LocalDateTime.parse(result.get("date").asText(), formatter));
                jmeterResult.setElapsed(Integer.parseInt(result.get("elapsed").asText()));
                jmeterResult.setLabel(result.get("label").asText());
                jmeterResult.setResponseCode(result.get("responseCode").asText());
                jmeterResult.setResponseMessage(result.get("responseMessage").asText());
                jmeterResult.setThreadName(result.get("threadName").asText());
                jmeterResult.setDataType(result.get("dataType").asText());
                jmeterResult.setSuccess(Boolean.parseBoolean(result.get("success").asText()));
                jmeterResult.setFailureMessage(result.get("failureMessage").asText());
                jmeterResult.setBytes(Integer.parseInt(result.get("bytes").asText()));
                jmeterResult.setSentBytes(Integer.parseInt(result.get("sentBytes").asText()));
                jmeterResult.setGrpThreads(Integer.parseInt(result.get("grpThreads").asText()));
                jmeterResult.setAllThreads(Integer.parseInt(result.get("allThreads").asText()));
                jmeterResult.setUrl(result.get("URL").asText());
                jmeterResult.setLatency(Integer.parseInt(result.get("Latency").asText()));
                jmeterResult.setIdleTime(Integer.parseInt(result.get("IdleTime").asText()));
                jmeterResult.setConnect(Integer.parseInt(result.get("Connect").asText()));
                
                jMeterResultRepository.save(jmeterResult);
            });
            break;
            // à l'avenir Rajouter pour gatling, selenium, etc.

            default:
                throw new IllegalArgumentException("Module non pris en charge : " + module);
        }
    }

     // GET all results for a module
     public List<?> getAllResultsByModule(String module) {
        switch (module.toLowerCase()) {
            case "jmeter":
                return jMeterResultRepository.findAll();

            // à l'avenir Rajouter pour gatling, selenium, etc.

            default:
                throw new IllegalArgumentException("Module non pris en charge : " + module);
        }
    }

    // DELETE all results for a module
    public void deleteAllResultsByModule(String module) {
        switch (module.toLowerCase()) {
            case "jmeter":
                jMeterResultRepository.deleteAll();
                break;

            // à l'avenir Rajouter pour gatling, selenium, etc.

            default:
                throw new IllegalArgumentException("Module non pris en charge : " + module);
        }
    }

    // GET a specific result by ID for a module
    public Object getResultById(String module, Long id) {
        switch (module.toLowerCase()) {
            case "jmeter":
                return jMeterResultRepository.findById(id).orElseThrow(() -> new NoSuchElementException("JMeter result not found"));

            // à l'avenir Rajouter pour gatling, selenium, etc.
    
            default:
                throw new IllegalArgumentException("Module non pris en charge : " + module);
        }
    }

    // DELETE a specific result by ID for a module
    public void deleteResultById(String module, Long id) {
        switch (module.toLowerCase()) {
            case "jmeter":
                jMeterResultRepository.deleteById(id);
                break;

                // à l'avenir Rajouter pour gatling, selenium, etc.
        
            default:
                throw new IllegalArgumentException("Module non pris en charge : " + module);
        }
    }
}
