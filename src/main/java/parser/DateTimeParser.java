package parser;

import java.time.LocalDateTime;

/**
 * Данный интерфейс описывает
 * извлечение даты и времени и
 * дальнейшее его преобразование в
 * другой формат для удобства работы.
 *
 * @author Constantine on 10.06.2022
 */
public interface DateTimeParser {

    LocalDateTime parse(String parse);
}
