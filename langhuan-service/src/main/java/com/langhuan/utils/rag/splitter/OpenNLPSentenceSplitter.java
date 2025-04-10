package com.langhuan.utils.rag.splitter;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenNLPSentenceSplitter {

    private final SentenceDetectorME sentenceDetector;

    public OpenNLPSentenceSplitter() throws IOException {
        // 获取src/main/resources/nlpModels处的文件
        InputStream modelIn = getClass().getResourceAsStream("/nlpModels/opennlp-en-ud-ewt-sentence-1.2-2.5.0.bin");
        SentenceModel model = new SentenceModel(modelIn);
        this.sentenceDetector = new SentenceDetectorME(model);
    }

    public List<String> apply(String text) {
        List<String> documents = new ArrayList<>();
        String[] sentences = sentenceDetector.sentDetect(text);

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                documents.add(sentence);
            }
        }
        return documents;
    }
}