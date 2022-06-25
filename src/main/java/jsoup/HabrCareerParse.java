package jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 2. Парсинг HTML страницы.
 * 2.3. Загрузка деталей поста.
 * 2.4. SqlRuParse.
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
 * <a href="https:/ /career.habr.com/vacancies">Habr</a>
 * (пробел убери) и в пустом месте ПКМ
 * -> исследовать элемент.
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
public class HabrCareerParse implements Parse {

    private static final int NUMBER_OF_PAGES = 5;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    /**
     * Данный метод парсит детали объявления
     * (описание вакансии).
     *
     * 1.Подключаемся и проваливаемся по
     * ссылке в вакансию.
     * 2.Я выяснил, что основной тест
     * описания содержится в div class="style-ugc".
     * 3.Выбрали первый элемент в дереве.
     * 4.С помощью метода {@link Element#text()}
     * извлекли весь текст.
     * 5.Если поймали исключение, то
     * возвращаем пустоту.
     *
     * @param link ссылка на вакансию.
     * @return описание вакансии в одной строке.
     */
    private String retrieveDescription(String link) {
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element firstElement = document.select(".style-ugc").first();
            return firstElement.text();
        } catch (IOException e) {
            LOG.error("Exception write to log", e);
        }
        return "";
    }

    /**
     * Данный метод извлекает данные
     * из одной конкретной вакансии и
     * собирает эти данные в виде
     * объекта {@link Post}.
     *
     * 1.Находим элемент заголовка.
     * Извлекаем оттуда текст.
     * 2.Находим элемент с ссылкой.
     * Извлекаем ссылку с помощью
     * {@link Element#attr(String)}.
     * 3.Находим элемент даты и времени.
     * Извлекаем так же как и ссылку.
     * 4.Описание вакансии вынесли в
     * отдельный метод
     * {@link HabrCareerParse#retrieveDescription}.
     *
     * @param row отдельная вакансия
     *            в списке вакансий.
     * @return объект класса {@link Post}.
     */
    private Post parsePost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        Element timeElement = row.select("time").first();
        String dateTime = timeElement.attr("datetime");
        String linkDetail = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description = retrieveDescription(linkDetail);
        return new Post(
                vacancyName,
                linkDetail,
                description,
                dateTimeParser.parse(dateTime));
    }

    /**
     * Данный метод парсит все вакансии
     * в список объектов {@link Post}.
     *
     * 1.В цикле проходимся по 5 страницам.
     * 2.Подключаемся каждый раз к
     * отдельно взятой странице.
     * link в параметрах - это почти
     * полная ссылка, кроме части с номером
     * страницы. Вот в таком виде:
     * https:/career.habr.com/vacancies/java_developer?page=
     * 3.Парсим список элементов на
     * этой странице с помощью
     * {@link Document#select(String)}
     * и получаем список {@link Elements}.
     * 4.Если поймали исключение, то
     * возвращаем пустой список.
     *
     * @param link ссылка на страницу с
     *             вакансиями.
     * @return список объектов {@link Post}
     * (вакансий).
     */
    @Override
    public List<Post> list(String link) {
        List<Post> vacancies = new ArrayList<>();
        try {
            for (int i = 0; i < NUMBER_OF_PAGES; i++) {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> vacancies.add(parsePost(row)));
            }
            return vacancies;
        } catch (IOException e) {
            LOG.error("Exception write to log", e);
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        String pageLink = "https://career.habr.com/vacancies/java_developer?page=";
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse parser = new HabrCareerParse(dateTimeParser);
        List<Post> posts = parser.list(pageLink);
        for (Post vacancies : posts) {
            System.out.println(vacancies);
        }
    }
}
