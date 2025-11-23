package com.psyche.platform.shared.model;

/**
 * Enum типов MBTI с описаниями
 * Используется во всех микросервисах
 */
public enum MBTIType {
    ISTJ("Инспектор", "Ответственный, организованный, практичный"),
    ISFJ("Защитник", "Преданный, теплый, ответственный"),
    INFJ("Советник", "Проницательный, вдохновляющий, настойчивый"),
    INTJ("Стратег", "Инновационный, независимый, решительный"),
    ISTP("Мастер",", "Спонтанный, логичный, эффективный"),
    ISFP("Композитор", "Дружелюбный, чувствительный, скромный"),
    INFP("Целитель", "Идеалистический, empathetic, творческий"),
    INTP("Архитектор", "Логичный, оригинальный, любознательный"),
    ESTP("Делец", "Энергичный, практичный, спонтанный"),
    ESFP("Развлекатель", "Общительный, дружелюбный, щедрый"),
    ENFP("Борец", "Энтузиаст, творческий, общительный"),
    ENTP("Новатор", "Изобретательный, умный, прямой"),
    ESTJ("Администратор", "Практичный, ответственный, организованный"),
    ESFJ("Консул", "Заботливый, популярный, гармоничный"),
    ENFJ("Наставник", "Харизматичный, вдохновляющий, тактичный"),
    ENTJ("Командир", "Решительный, лидерский, стратегический");
    
    private final String title;
    private final String description;
    
    MBTIType(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    
    /**
     * Определяет группу риска буллинга для типа
     */
    public RiskLevel getBullyingRisk() {
        return switch (this) {
            case ENTJ, ESTJ, ESTP -> RiskLevel.HIGH;
            case ENTP, INTJ -> RiskLevel.MEDIUM;
            default -> RiskLevel.LOW;
        };
    }
    
    public enum RiskLevel {
        LOW("Низкий риск"),
        MEDIUM("Средний риск"),
        HIGH("Высокий риск");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
}
