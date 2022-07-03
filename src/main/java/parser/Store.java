package parser;

import java.util.List;

/**
 * Данный интерфейс описывает хранилище.
 * Это может быть как хранение данных
 * в памяти, так и хранение в БД.
 *
 * @author Constantine on 25.06.2022
 */
public interface Store {

    /**
     * Данный метод сохраняет объявление в БД.
     * @param post объявление {@link Post}.
     */
    void save(Post post);

    /**
     * Данный метод извлекает объявления из БД.
     * @return список объявлений.
     */
    List<Post> getAll();

    /**
     * Данный метод находит объявление по ID.
     * @param id идентификатор (ID) объявления.
     * @return объявление {@link Post}.
     */
    Post findById(int id);
}
