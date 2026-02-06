package com.wei.pet.pet_rescue.common;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wyr on 2026/2/5
 */
public class NlpUtils {

    /**
     * 提取核心关键词
     */
    public static List<String> extractKeywords(String sentence) {
        // 使用 HanLP 分词
        List<Term> termList = HanLP.segment(sentence);

        List<String> keywords = new ArrayList<>();
        for (Term term : termList) {
            // 只保留名词(n)和动词(v)，且长度大于1
            if ((term.nature.startsWith("n") || term.nature.startsWith("v"))
                    && term.word.length() > 1) {
                keywords.add(term.word);
            }
        }
        return keywords;
    }
}