package com.nostalgia.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StorySummaryExtractor {

    private static final List<Pattern> SOURCE_PATTERNS = Arrays.asList(
            Pattern.compile("从(.{1,20}?)(买|购|得|淘|拿|带|收|寻|找|觅)"),
            Pattern.compile("(爷爷|奶奶|外公|外婆|父亲|母亲|爸爸|妈妈|叔叔|阿姨|舅舅|舅妈|伯伯|姑姑)(.{0,10}?)(给|送|留|传|赠|遗留下)的?"),
            Pattern.compile("(祖传|家传|传下|留下|遗留下|继承)(.{0,15})"),
            Pattern.compile("(朋友|同学|同事|邻居)(.{0,10}?)(给|送|赠)的?"),
            Pattern.compile("(买|购|淘|得)(于|自|在)(.{1,20})"),
            Pattern.compile("来自(.{1,20})"),
            Pattern.compile("(二手|旧货|古董|跳蚤)市场(.{0,15})"),
            Pattern.compile("(地摊|集市|庙会|旧书店|旧货店)(.{0,15}?)(买|淘|得)"),
            Pattern.compile("(出国|留学|旅行|出差|旅游)(.{0,15}?)(带|买|购)回"),
            Pattern.compile("(捡|拾|捡到|发现)(.{0,15})"),
            Pattern.compile("(嫁妆|陪嫁|聘礼)(.{0,15})"),
            Pattern.compile("(亲戚|族人|老乡)(.{0,10}?)(给|送|留)的?")
    );

    private static final Map<String, List<String>> EMOTION_DICTIONARY = new LinkedHashMap<>();

    static {
        EMOTION_DICTIONARY.put("怀念", Arrays.asList("怀念", "想念", "思念", "惦记", "牵挂", "忆", "回忆", "追忆", "缅怀", "眷恋", "留念", "思念", "怀恋"));
        EMOTION_DICTIONARY.put("温暖", Arrays.asList("温暖", "温馨", "幸福", "甜蜜", "感动", "暖心", "柔情", "慈爱", "关怀", "疼爱", "呵护"));
        EMOTION_DICTIONARY.put("珍惜", Arrays.asList("珍惜", "珍视", "宝贵", "珍贵", "爱惜", "不舍", "舍不得", "宝贵", "珍藏", "收藏"));
        EMOTION_DICTIONARY.put("感伤", Arrays.asList("感伤", "伤感", "惆怅", "失落", "遗憾", "惋惜", "唏嘘", "心酸", "酸楚", "忧伤", "难过", "伤心", "悲痛"));
        EMOTION_DICTIONARY.put("怀旧", Arrays.asList("怀旧", "念旧", "复古", "旧时光", "往事", "从前", "曾经", "过去", "当年", "那时候", "老时光"));
        EMOTION_DICTIONARY.put("感恩", Arrays.asList("感恩", "感谢", "感激", "报答", "回报", "知恩", "知足"));
        EMOTION_DICTIONARY.put("自豪", Arrays.asList("自豪", "骄傲", "荣耀", "光荣", "体面", "气派", "了不起", "不简单"));
        EMOTION_DICTIONARY.put("新奇", Arrays.asList("新奇", "新鲜", "好奇", "稀罕", "稀奇", "罕见", "少见", "有趣", "有趣", "好玩", "特别"));
        EMOTION_DICTIONARY.put("亲切", Arrays.asList("亲切", "熟悉", "亲近", "贴心", "亲近", "故交", "老友", "默契"));
        EMOTION_DICTIONARY.put("坚韧", Arrays.asList("坚持", "坚强", "坚韧", "毅力", "不屈", "耐久", "耐用", "经久", "踏实", "朴素"));
    }

    private static final List<Pattern> USAGE_SCENE_PATTERNS = Arrays.asList(
            Pattern.compile("(在|于)(.{1,15}?)(时|中|里|上|间)(.{0,10}?)(用|使用|使用着|拿)"),
            Pattern.compile("(日常|每天|每天|经常|常常|时常|总是)(.{0,10}?)(用|使用|穿|戴|听|看|骑|玩)"),
            Pattern.compile("(过年|过节|春节|中秋|端午|婚嫁|嫁娶|喜事|丧事|祭祖)(.{0,15})"),
            Pattern.compile("(上学|读书|上课|写字|做作业|考试)(.{0,15})"),
            Pattern.compile("(干活|务农|种地|收割|耕地|插秧)(.{0,15})"),
            Pattern.compile("(做饭|煮饭|烧水|洗衣服|缝补|织布|裁衣)(.{0,15})"),
            Pattern.compile("(听歌|听戏|听广播|看电影|看电视)(.{0,15})"),
            Pattern.compile("(出行|赶路|骑车|坐车|搭车|赶集)(.{0,15})")
    );

    public static String extractItemSource(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String storyText = text.trim();
        for (Pattern pattern : SOURCE_PATTERNS) {
            Matcher matcher = pattern.matcher(storyText);
            if (matcher.find()) {
                String matched = matcher.group().trim();
                if (matched.length() > 50) {
                    matched = matched.substring(0, 50) + "...";
                }
                return matched;
            }
        }

        return null;
    }

    public static List<String> extractEmotionKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String combinedText = text.trim();
        Map<String, Integer> categoryHits = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : EMOTION_DICTIONARY.entrySet()) {
            String category = entry.getKey();
            List<String> words = entry.getValue();
            int hitCount = 0;
            for (String word : words) {
                if (combinedText.contains(word)) {
                    hitCount++;
                }
            }
            if (hitCount > 0) {
                categoryHits.put(category, hitCount);
            }
        }

        return categoryHits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static String extractUsageScene(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        for (Pattern pattern : USAGE_SCENE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String matched = matcher.group().trim();
                if (matched.length() > 50) {
                    matched = matched.substring(0, 50) + "...";
                }
                return matched;
            }
        }

        return null;
    }

    public static String generateSummaryText(String content, String story, String memory,
                                              String eraBackground, String currentStatus,
                                              String itemSource, String usageScene,
                                              List<String> emotionKeywords) {
        StringBuilder summary = new StringBuilder();

        if (itemSource != null && !itemSource.isBlank()) {
            summary.append("来源：").append(itemSource).append("；");
        }

        if (usageScene != null && !usageScene.isBlank()) {
            summary.append("场景：").append(usageScene).append("；");
        }

        if (emotionKeywords != null && !emotionKeywords.isEmpty()) {
            summary.append("情感：").append(String.join("、", emotionKeywords)).append("；");
        }

        if (summary.length() > 0) {
            return summary.toString();
        }

        String combined = (content != null ? content : "") + " " +
                (story != null ? story : "") + " " +
                (memory != null ? memory : "") + " " +
                (eraBackground != null ? eraBackground : "") + " " +
                (currentStatus != null ? currentStatus : "");
        combined = combined.trim();
        if (combined.length() > 200) {
            return combined.substring(0, 200) + "...";
        }
        return combined.isEmpty() ? null : combined;
    }

    public static class ExtractionResult {
        private final String itemSource;
        private final String usageScene;
        private final List<String> emotionKeywords;
        private final String summaryText;

        public ExtractionResult(String itemSource, String usageScene,
                                List<String> emotionKeywords, String summaryText) {
            this.itemSource = itemSource;
            this.usageScene = usageScene;
            this.emotionKeywords = emotionKeywords;
            this.summaryText = summaryText;
        }

        public String getItemSource() { return itemSource; }
        public String getUsageScene() { return usageScene; }
        public List<String> getEmotionKeywords() { return emotionKeywords; }
        public String getSummaryText() { return summaryText; }
    }

    public static ExtractionResult extract(String content, String story, String memory,
                                            String eraBackground, String currentStatus,
                                            String existingUsageScene) {
        String combinedText = (content != null ? content : "") + " " +
                (story != null ? story : "") + " " +
                (memory != null ? memory : "") + " " +
                (eraBackground != null ? eraBackground : "") + " " +
                (currentStatus != null ? currentStatus : "");

        String storyAndMemory = (story != null ? story : "") + " " + (memory != null ? memory : "");

        String itemSource = extractItemSource(storyAndMemory);
        if (itemSource == null) {
            itemSource = extractItemSource(combinedText);
        }

        List<String> emotionKeywords = extractEmotionKeywords(combinedText);

        String usageScene = existingUsageScene;
        if (usageScene == null || usageScene.isBlank()) {
            usageScene = extractUsageScene(combinedText);
        }

        String summaryText = generateSummaryText(content, story, memory, eraBackground,
                currentStatus, itemSource, usageScene, emotionKeywords);

        return new ExtractionResult(itemSource, usageScene, emotionKeywords, summaryText);
    }
}
