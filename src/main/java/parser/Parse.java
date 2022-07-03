package parser;

import java.util.List;

/**
 * 2.4. SqlRuParse.
 *
 * Данный интерфейс описывает парсинг сайта.
 *
 * @author Constantine on 20.06.2022
 */
public interface Parse {

    /**
     * Данный метод загружает список
     * всех постов {@link Post}.
     *
     * @param link ссылка на сайт.
     * @return список всех постов.
     */
    List<Post> list(String link);
}
