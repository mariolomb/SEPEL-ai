package it.sepel.ai.mapper;

import it.sepel.ai.domain.AIResponse;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ContenutoSearch;
import it.sepel.ai.domain.ItemCronologia;
import it.sepel.ai.domain.Rating;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApplicationMapper {
    Integer getUniqueId();
    String loadConfigProperty(String name);
    void insertContenuto(@Param("contenuto") Contenuto c, @Param("embeddingString") String embeddingString, @Param("esito") String esito);
    void  updateContenuto(@Param("contenuto") Contenuto c, @Param("embeddingString") String embeddingString, @Param("esito") String esito);
    List<Contenuto> searchContenuti(ContenutoSearch cs);
    void logRequest(AIResponse r);
    Map loadContenutoAI(@Param("idContenuto") Integer idContenuto, @Param("tipoContenuto") String tipoContenuto);
    void deleteContenuto(String id);
    void indicizzaContenuto(Contenuto c);
    List<ItemCronologia> loadCronologia(String codiceAbbonato);
    List<String> loadMaterie();
    void updateRating(Rating r);
}