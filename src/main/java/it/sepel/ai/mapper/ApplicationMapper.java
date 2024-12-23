package it.sepel.ai.mapper;

import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ContenutoSearch;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApplicationMapper {
    String loadConfigProperty(String name);
    void insertContenuto(@Param("contenuto") Contenuto c, @Param("embeddingString") String embeddingString, @Param("esito") String esito);
    void  updateContenuto(@Param("contenuto") Contenuto c, @Param("embeddingString") String embeddingString, @Param("esito") String esito);
    List<Contenuto> searchContenuti(ContenutoSearch cs);
}