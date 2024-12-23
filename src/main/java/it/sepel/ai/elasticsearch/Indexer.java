package it.sepel.ai.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import it.sepel.ai.AppException;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ContenutoSearch;
import it.sepel.ai.mapper.ApplicationMapper;
import it.sepel.ai.openai.OpenAIManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class Indexer {

    //formato del nome
    //Amministrazione pubblica-QUESITO-123456-23.12.2024.pdf
    //Elettorale-ARTICOLO_FOCUS-123456-15.10.2018.pdf
    private static final Logger log = LoggerFactory.getLogger(Indexer.class);
    public static final String FILES_DIR_PATH = "/home/mario/Scrivania/db/newai";

    @Autowired
    private ELManager elManager;

    @Autowired
    private IndexService indexService;

    @Autowired
    private OpenAIManager openAIManager;

    @Autowired
    private ApplicationMapper mapper;

    //divide il testo in pezzi di massimo maxTokes (usa python esterno)
    private List<String> splitTestoContenuto(String testo, int maxTokens) {
        var encoding = Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);
        List<Integer> tokens = encoding.encode(testo).boxed();
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += maxTokens) {
            List<Integer> subList = tokens.subList(i, Math.min(i + maxTokens, tokens.size()));
            IntArrayList chunkTokens = new IntArrayList();
            for (Integer t : subList) {
                chunkTokens.add(t);
            }
            String chunkText = encoding.decode(chunkTokens);
            chunks.add(chunkText);
        }
        return chunks;
    }

    //calcola la media degli embedding per ottenere un solo embedding per il documento
    private List<Double> mediaEmbedding(List<List<Double>> embeddings) {
        int dimension = embeddings.get(0).size();
        Double[] combinedEmbedding = new Double[dimension];
        for (int i = 0; i < dimension; i++) {
            combinedEmbedding[i] = 0.0;
        }
        for (List<Double> embedding : embeddings) {
            for (int i = 0; i < dimension; i++) {
                combinedEmbedding[i] += embedding.get(i);
            }
        }
        for (int i = 0; i < dimension; i++) {
            combinedEmbedding[i] /= embeddings.size();
        }
        return List.of(combinedEmbedding);
    }

    public MultipartFile convertFileToMultipartFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile(
                file.getName(),
                file.getName(),
                "application/pdf",
                input
        );
    }

    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        }
    }

    private void listFilesInDirectory(File directory, List<String> fileNames) {
        if (!directory.exists()) {
            log.info("La directory specificata non esiste.");
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    //fileNames.add(file.getName());
                    fileNames.add(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    log.info("Directory: " + file.getName());
                    listFilesInDirectory(file, fileNames);
                }
            }
        }
    }

    public void indexOneContenutoFromFile() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String esito = "OK";
        File f = new File("/home/mario/Scrivania/db/guida_portale_UPk371DOHV.pdf");
        Contenuto c = new Contenuto();
        c.setData(new Date());
        c.setId(UUID.randomUUID().toString());
        c.setMateria("Varie");
        c.setIdContenuto(9999999);
        String testo = extractTextFromPDF(convertFileToMultipartFile(f));
        c.setTesto(testo);
        try {
            List<Double> embeddingVector = openAIManager.generateEmbedding(c.getTesto());
            c.setEmbedding(embeddingVector);
            elManager.saveContenuto(c);
        } catch (Exception e) {
            log.error("ERRORE", e);
            esito = e.getMessage();
        } finally {
            String embeddingString = "";
            if (null != c.getEmbedding()) {
                try {
                    embeddingString = objectMapper.writeValueAsString(c.getEmbedding());
                } catch (JsonProcessingException ec) {
                    log.error("ERRORE", ec);
                }
            }
            mapper.insertContenuto(c, embeddingString, esito);
        }

    }

    //indicizza tutti i contenuti presenti nella directory FILES_DIR_PATH e suddivisvi per materia
    //all'interno di ogni materia sono suddivivi per tipo contenuto
    //calcola anche l'embedding di ogni testo
    public void indexAllContenutiFromFiles() {

        /*
        indexService.deleteIndex("sepel_ai");
        log.info("Index deleted!");
        indexService.createIndex("sepel_ai");
        log.info("Indice created");
         */
        log.info("Starting to index contenuti...");
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        List<String> filePaths = new ArrayList();
        File directory = new File(FILES_DIR_PATH);
        listFilesInDirectory(directory, filePaths);
        log.info("found  [" + filePaths.size() + "] files");
        ObjectMapper objectMapper = new ObjectMapper();
        for (String filePath : filePaths) {
            Contenuto c = new Contenuto();
            String esito = "OK";
            try {
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                StringTokenizer st = new StringTokenizer(fileName, "-");
                while (st.hasMoreTokens()) {
                    String materia = st.nextToken();
                    String tipoContenuto = st.nextToken();
                    String idContenuto = st.nextToken();
                    String data = st.nextToken();
                    data = data.replaceAll(".pdf", "");
                    data = data.replaceAll(".txt", "");
                    if (StringUtils.isNotEmpty(data)) {
                        c.setData(df.parse(data));
                    }
                    c.setId(UUID.randomUUID().toString());
                    c.setMateria(materia);
                    c.setTipoContenuto(tipoContenuto);
                    c.setIdContenuto(Integer.valueOf(idContenuto));
                    File f = new File(filePath);

                    String testo;
                    if (fileName.contains("ARTICOLO_FOCUS") || fileName.contains("ARTICOLO_RIVISTA")) {
                        testo = extractTextFromPDF(convertFileToMultipartFile(f));
                    } else {
                        testo = new String(Files.readAllBytes(Paths.get(filePath)));
                    }

                    //rimuovo eventuale html
                    testo = Jsoup.parse(testo).text();
                    c.setTesto(testo);

                    //genera embedding vector
                    List<Double> embeddingVector = openAIManager.generateEmbedding(c.getTesto());
                    c.setEmbedding(embeddingVector);

                    log.info("Indexing contenuto [" + c.getTipoContenuto() + "] [" + c.getIdContenuto() + "]");
                    elManager.saveContenuto(c);
                }
            } catch (Exception e) {
                log.error("ERRORE", e);
                esito = e.getMessage();
            } finally {
                String embeddingString = "";
                if (null != c.getEmbedding()) {
                    try {
                        embeddingString = objectMapper.writeValueAsString(c.getEmbedding());
                    } catch (JsonProcessingException ec) {
                        log.error("ERRORE", ec);
                    }
                }
                mapper.insertContenuto(c, embeddingString, esito);
            }
            //if(true) break;
        }
    }

    //indicizza tutti i contenuti presi dalla query impostata
    //utili per reindicizzare quei documenti che hanno dato errore
    //nella generazione embedding nella prima passata
    //carica i files sempre dalla stessa directory del metodo precedente
    public void indexAllContenutiFromDb() throws Exception {
        ContenutoSearch cs = new ContenutoSearch();

        //cs.setId("0074671b-ba68-408d-a1b2-1a360e4ff432");
        cs.setErrori("SI");

        List<Contenuto> cc = mapper.searchContenuti(cs);
        log.info("Trovati [" + cc.size() + "] contenuti");
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        String extension;
        ObjectMapper objectMapper = new ObjectMapper();
        for (Contenuto c : cc) {
            String esito = "OK";
            try {
                String tipoContenuto = c.getTipoContenuto();
                if ("ARTICOLO_FOCUS".equals(tipoContenuto) || "ARTICOLO_RIVISTA".equals(tipoContenuto)) {
                    extension = ".pdf";
                } else {
                    extension = ".txt";
                }
                String data = "";
                if(null != c.getData()) {
                    data = df.format(c.getData());
                }
                String fileName = c.getMateria() + "-" + tipoContenuto + "-" + c.getIdContenuto() + "-" + data  + extension;
                String filePath = Indexer.FILES_DIR_PATH + "/" + c.getMateria() + "/" + c.getTipoContenuto() + "/" + fileName;
                File f = new File(filePath);

                if (!f.exists()) {
                    throw new AppException("Il file [" + filePath + "] non esiste");
                }

                String testo;
                if (fileName.contains("ARTICOLO_FOCUS") || fileName.contains("ARTICOLO_RIVISTA")) {
                    testo = extractTextFromPDF(convertFileToMultipartFile(f));
                } else {
                    testo = new String(Files.readAllBytes(Paths.get(filePath)));
                }

                //rimuovo eventuale html
                testo = Jsoup.parse(testo).text();
                
                //salva il testo in un file perche essendo troppo lungo non puo essere passato nella riga di comando
                // Salva il testo in un file temporaneo
                /*
                File tempFile = File.createTempFile("input/opt/python/countToken/input", ".txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    writer.write(testo);
                }
                */

                //calcola il numero dei token per il testo
                //ProcessBuilder processBuilder = new ProcessBuilder("python3", "/opt/python/countToken/count_tokens.py", testo);
                //ProcessBuilder processBuilder = new ProcessBuilder("python3", "/opt/python/countToken/count_tokens.py", tempFile.getAbsolutePath());
                //Process process = processBuilder.start();
                //BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                //String line;
                Integer numToken = 80000;
                //while ((line = reader.readLine()) != null) {
                //    numToken = Integer.valueOf(line);
                //    log.info("Numero di token: " + line);
                //}

                List<Double> finalEmbedding;
                if (numToken > 8000) {
                    List<String> chunks = splitTestoContenuto(testo, 8000);
                    List<List<Double>> embeddings = new ArrayList();

                    for (int i = 0; i < chunks.size(); i++) {
                        //log.info("Chunk " + (i + 1) + ":\n" + chunks.get(i) + "\n");

                        //genera embedding per ogni chunk
                        List<Double> dd = openAIManager.generateEmbedding(chunks.get(i));
                        embeddings.add(dd);
                    }
                    finalEmbedding = mediaEmbedding(embeddings);
                    //log.info("embedding =" + finalEmbedding);
                } else {

                    //non serve splittare il testo
                    finalEmbedding = openAIManager.generateEmbedding(testo);
                }

                c.setEmbedding(finalEmbedding);
                elManager.saveContenuto(c);
                log.info("contenuto ["  + c.getId() + "] saved");
            } catch (Exception e) {
                log.error("ERRORE", e);
                esito = e.getMessage();
            } finally {
                String embeddingString = "";
                if (null != c.getEmbedding()) {
                    try {
                        embeddingString = objectMapper.writeValueAsString(c.getEmbedding());
                    } catch (JsonProcessingException ec) {
                        log.error("ERRORE", ec);
                    }
                }
                mapper.updateContenuto(c, embeddingString, esito);
            }
            //if(true) break;
        }
    }
}
