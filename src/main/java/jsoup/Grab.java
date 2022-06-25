package jsoup;

import org.quartz.Scheduler;

/**
 * Данный интерфейс описывает планировщика.
 *
 * То есть наш парсер будет запускаться
 * с некоторой периодичностью.
 *
 * @author Constantine on 25.06.2022
 */
public interface Grab {

    /**
     * Данный метод запускает планировщик.
     *
     * @param parse парсер объявлений.
     * @param store хранилище объявлений.
     * @param scheduler планировщик
     *                  (мы будет использовать quartz).
     */
    void init(Parse parse, Store store, Scheduler scheduler);
}
