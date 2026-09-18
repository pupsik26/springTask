package notification.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationType {

    CREATE(
            "Регистрация на сайте %s",
            "Здравствуйте! Ваш аккаунт на сайте %s был успешно создан."
    ),
    DELETE(
            "Ваш аккаунт удалён",
            "Здравствуйте! Ваш аккаунт был удалён."
    ),
    UPDATE(
            "Обновление данных аккаунта",
            "Здравствуйте! Данные вашего аккаунта на сайте %s были обновлены."
    );

    private final String subjectTemplate;
    private final String textTemplate;

    /**
     * Форматирует тему письма, подставляя название сайта.
     */
    public String getSubject(String siteName) {
        return String.format(subjectTemplate, siteName);
    }

    /**
     * Форматирует текст письма, подставляя название сайта.
     */
    public String getText(String siteName) {
        return String.format(textTemplate, siteName);
    }

    /**
     * Безопасная конвертация из строки (приходит из Kafka/API).
     * Бросает IllegalArgumentException, если операция неизвестна.
     */
    public static OperationType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Operation cannot be null or blank");
        }
        try {
            return OperationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown operation type: " + value, e);
        }
    }
}