package parser;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Данный класс реализует метод
 * интерфейса {@link DateTimeParser}
 * и парсит дату и время с
 * последующим преобразованием.
 *
 * @author Constantine on 10.06.2022
 */
public class HabrCareerDateTimeParser implements DateTimeParser {

    /**
     * Данный метод преобразует строку
     * с временем и датой в объект
     * {@link LocalDateTime}.
     *
     * На входе имеем что-то вроде
     * 2022-06-08T19:34:01+03:00 в виде строки.
     * Преобразуем это в {@link ZonedDateTime}
     * с помощью метода {@link ZonedDateTime#parse},
     * а потом избавляемся от часового пояса.
     *
     * @param parse время с часовым поясом
     *              в нашем случае.
     * @return дата и время в виде объекта
     * {@link LocalDateTime}.
     */
    @Override
    public LocalDateTime parse(String parse) {
        ZonedDateTime zdt = ZonedDateTime.parse(parse);
        return zdt.toLocalDateTime();
    }
}
