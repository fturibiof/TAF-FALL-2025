package ca.etsmtl.selenium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fournit l'URL de l'instance UI pour les tests Selenium.
 * Supporte plusieurs instances (séparées par des virgules) si nécessaire,
 * mais une seule instance suffit pour les tests parallèles.
 */
@Component
public class UiInstanceProvider {
    
    @Value("${ui.instances:http://localhost:4200}")
    private String instances;
    
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private List<String> instanceUrls;
    
    /**
     * Retourne la prochaine URL d'UI disponible en round-robin
     */
    public String getNextUiUrl() {
        if (instanceUrls == null) {
            instanceUrls = List.of(instances.split(","));
        }
        
        if (instanceUrls.size() == 1) {
            return instanceUrls.get(0).trim();
        }
        
        int index = currentIndex.getAndUpdate(i -> (i + 1) % instanceUrls.size());
        return instanceUrls.get(index).trim();
    }
    
    /**
     * Retourne toutes les URLs d'UI configurées
     */
    public List<String> getAllUiUrls() {
        if (instanceUrls == null) {
            instanceUrls = List.of(instances.split(","));
        }
        return instanceUrls;
    }
}
