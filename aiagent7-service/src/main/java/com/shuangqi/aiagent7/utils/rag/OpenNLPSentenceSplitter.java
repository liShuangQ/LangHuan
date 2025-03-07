package com.shuangqi.aiagent7.utils.rag;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenNLPSentenceSplitter {

    private final SentenceDetectorME sentenceDetector;

    public OpenNLPSentenceSplitter() throws IOException {
        InputStream modelIn = getClass().getResourceAsStream("/models/en-sent.bin");
        SentenceModel model = new SentenceModel(modelIn);
        this.sentenceDetector = new SentenceDetectorME(model);
    }

    public List<Document> apply(String text, Map<String, Object> metadata) {
        List<Document> documents = new ArrayList<>();
        String[] sentences = sentenceDetector.sentDetect(text);

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                documents.add(new Document(sentence, metadata));
            }
        }
        return documents;
    }
}