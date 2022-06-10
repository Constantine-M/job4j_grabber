package jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 2. Парсинг HTML страницы.
 *
 * Данный класс описывает принципы работы
 * с библиотекой {@link Jsoup},
 * которая предназначена для
 * парсинга HTML страниц.
 *
 * 1.У нас есть две константы. Первая - это
 * ссылка на сайт в целом. Вторая указывает
 * на страницу с вакансиями непосредственно.
 *
 * 2.Сначала мы получаем страницу, чтобы
 * с ней можно было работать. Для этого
 * подключаемся с помощью {@link Connection},
 * а потом получаем структуру страницы
 * с помощью {@link Document} и метода
 * {@link Connection#get()}.
 *
 * 3.Далее анализируя структуру страницы,
 * мы получаем, что признаком вакансии
 * является CSS класс .vacancy-card__inner,
 * а признаком названия класс
 * .vacancy-card__title. Ссылка на вакансию
 * вложена в элемент названия, сама же ссылка
 * содержит абсолютный путь к вакансии
 * (относительно домена. Это наша
 * константа {@see SOURCE_LINK}).
 * Все это ты увидишь, если откроешь
 * <a href="https://career.habr.com/vacancies">Habr</a>
 * и в пустом месте ПКМ -> исследовать элемент.
 * Это работает точно для Ябраузера.
 *
 * 4.На основе анализа прописываем парсинг.
 * 4.1.Сначала мы получаем все вакансии
 * страницы с помощью
 * {@link Document#select(String)}.
 * Перед CSS классом ставится точка.
 * Это правила CSS селекторов, с которыми
 * работает метод, указанный выше.
 * 4.2.Проходимся по каждой вакансии
 * и извлекаем нужные для нас данные.
 * Сначала получаем элементы,
 * содержащие название и ссылку.
 * Для этого можно воспользоваться
 * селектором {@link Element#select)}
 * или через индекс {@link Element#child(int)}.
 * 4.3.В конечном итоге получаем
 * данные в виде текста с помощью
 * метода {@link Element#text()}.
 * Метод возвращает все содержимое
 * элемента в виде текста, т.е.
 * весь текст, что находится вне тегов HTML.
 *
 * 5.Ссылка на вакансию является
 * аттрибутом, поэтому её нужно получить
 * как аттрибут. Для этого используем
 * метод {@link Element#attr(String)}.
 *
 * @author Constantine on 08.06.2022
 */
public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format(
            "%s/vacancies/java_developer", SOURCE_LINK);

    /**
     * Данный метод приводит дату и время
     * в удобочитаемый формат.
     *
     * С помощью {@link DateTimeFormatter}
     * мы выбираем паттерн (сами),
     * согласно которому будет отображаться
     * дата и время.
     *
     * @param dateTime местное время в формате
     *                 {@link LocalDateTime}.
     * @return кастомизированное время, в
     * виде строки.
     */
    private static String getCustomDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss a");
        return formatter.format(dateTime);
    }

    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            Element timeElement = row.select("time").first();
            String dateTime = timeElement.attr("datetime");
            String customTime = getCustomDateTime(dateTimeParser.parse(dateTime));
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s - %s %s%n", customTime, vacancyName, link);
        });
    }
}
