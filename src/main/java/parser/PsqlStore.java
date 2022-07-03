package parser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Данный класс описывает взаимодействие
 * с БД посредством SQL запросов.
 *
 * @author Constantine on 25.06.2022
 */
public class PsqlStore implements Store, AutoCloseable {

    private Properties cfg;

    private Connection cnn;

    /**
     * В данный конструктор мы передаем
     * файл "properties" - это будет уже
     * загруженый файл.
     * Особенность в том, что мы
     * можем загружать различные
     * файлы настроек.
     *
     * @param cfg файл с настройками.
     */
    public PsqlStore(Properties cfg) {
        this.cfg = cfg;
        initConnection();
    }

    private void initConnection() {
        try {
            Class.forName(cfg.getProperty("driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("login"),
                    cfg.getProperty("password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Данный метод сохраняет объявление в БД.
     *
     * Обрати внимание, что в конце запроса
     * ON CONFLICT - это означает, что в случае
     * конфликта НИЧЕГО НЕ ДЕЛАТЬ.
     * В нашем случае конфликтом являлись
     * дубликаты по полю link (ссылки одинаковые).
     * Если в БД уже есть вакансия и я спустя
     * 2 мин решил запустить парсинг, то, скорее
     * всего, в БД начнут попадать те же
     * вакансии, и БД начнет ругаться на дубликаты.
     *
     * Есть 2 выхода - ничего не делать и
     * обновить запись. В первом случае
     * дубликат просто не будет записан в таблицу,
     * а во втором обновится запись - скорее
     * всего просто изменится ID объявления.
     *
     * @param post объявление {@link Post}.
     */
    @Override
    public void save(Post post) {
        String ls = System.lineSeparator();
        String sql = "INSERT INTO project.post (name, textpost, link, created)" + ls
                    + "VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING";
        try (PreparedStatement ps = cnn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Данный метод извлекает объявления из БД.
     *
     * Чтобы задать параметры {@link Post},
     * используется метод
     * {@link PsqlStore#setPostParameter}.
     *
     * @return список объявлений.
     */
    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM project.post ORDER BY id";
        try (PreparedStatement ps = cnn.prepareStatement(sql)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(setPostParameter(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    /**
     * Данный метод находит объявление по ID.
     * @param id идентификатор (ID) объявления.
     * @return объявление {@link Post}.
     */
    @Override
    public Post findById(int id) {
        String sql = "SELECT * FROM project.post WHERE id = ?";
        try (PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return setPostParameter(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Данный метод задает параметры
     * для {@link Post}.
     *
     * Он необходим, чтобы исключить
     * дублирвоание кода в методах.
     *
     * @param resultSet набор результатов БД.
     * @return заявка с заданными параметрами.
     */
    private Post setPostParameter(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getTimestamp(5).toLocalDateTime()
        );
    }

    /**
     * Данный метод очищает всю таблицу.
     *
     * Команда TRUNCATE быстро удаляет все
     * строки из набора таблиц. Она действует
     * так же, как безусловная команда DELETE
     * для каждой таблицы, но гораздо быстрее.
     *
     * Если перед именем таблицы указано ONLY,
     * очищается только заданная таблица.
     * Без ONLY очищается и заданная таблица,
     * и все её потомки (если таковые есть).
     */
    private void clearTable() {
        String sql = "TRUNCATE ONLY project.post";
        try (Statement statement = cnn.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }
}
