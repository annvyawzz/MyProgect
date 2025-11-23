package com.psyche.platform.shared.model;

import lombok.Builder;
import lombok.Data;

/**
 * Модель черты личности для использования во всех микросервисах
 */
@Data
@Builder
public class PersonalityTrait {
    private String code;          // E, I, N, S, T, F, J, P
    private String name;          // Extraversion, Introversion, etc.
    private String description;   // Описание черты
    private Double score;         // 0.0 - 1.0
    private Double weight;        // Вес в расчетах
    private TraitCategory category;
    
    /**
     * Factory Method для создания черт MBTI
     */
    public static PersonalityTrait createMBTITrait(String code, Double score) {
        return PersonalityTrait.builder()
            .code(code)
            .name(getTraitName(code))
            .description(getTraitDescription(code))
            .score(score)
            .weight(1.0)
            .category(TraitCategory.MBTI)
            .build();
    }
    
    private static String getTraitName(String code) {
        return switch (code) {
            case "E" -> "Extraversion";
            case "I" -> "Introversion";
            case "N" -> "Intuition";
            case "S" -> "Sensing";
            case "T" -> "Thinking";
            case "F" -> "Feeling";
            case "J" -> "Judging";
            case "P" -> "Perceiving";
            default -> "Unknown";
        };
    }
    
    private static String getTraitDescription(String code) {
        return switch (code) {
            case "E" -> "Ориентация на внешний мир, общительность";
            case "I" -> "Ориентация на внутренний мир, глубина";
            case "N" -> "Фокусировка на идеях и возможностях";
            case "S" -> "Фокусировка на фактах и реальности";
            case "T" -> "Принятие решений на основе логики";
            case "F" -> "Принятие решений на основе ценностей";
            case "J" -> "Предпочтение структуры и планирования";
            case "P" -> "Предпочтение гибкости и спонтанности";
            default -> "Неизвестная черта";
        };
    }
    
    public enum TraitCategory {
        MBTI,
        BIG_FIVE,
        TEMPERAMENT,
        CUSTOM
    }
    
    /**
     * Builder Pattern для сложного создания объектов
     */
    public static class PersonalityTraitBuilder {
        private String code;
        private String name;
        private Double score = 0.5;
        private Double weight = 1.0;
        
        public PersonalityTraitBuilder withCode(String traitCode) {
            this.code = traitCode;
            this.name = getTraitName(traitCode);
            return this;
        }
        
        public PersonalityTraitBuilder withScore(Double traitScore) {
            this.score = Math.max(0.0, Math.min(1.0, traitScore));
            return this;
        }
        
        public PersonalityTrait build() {
            return new PersonalityTrait(code, name, getTraitDescription(code), score, weight, TraitCategory.MBTI);
        }
    }
}
