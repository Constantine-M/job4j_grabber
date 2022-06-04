package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 1. Quartz.
 *
 * Данный класс описывает принципы работы
 * планировщика, благодаря которому
 * можно настраивать действия с периодичностью.
 *
 * 1.Сначала сконфигурируем сам планировщик
 * {@link Scheduler} и начнем его работу
 * {@link Scheduler#start()}.
 *
 * 2.Создадим задачу с помощью
 * {@link JobDetail} и метода
 * {@link JobBuilder#newJob()}.
 * quartz каждый раз создает объект с
 * типом {@link Job} (это интерфейс), поэтому
 * мы создали класс {@link Rabbit},
 * который реализует этот интерфейс.
 *
 * Внутри этого класса мы описали
 * требуемые действия. В нашем случае -
 * это вывод на консоль текста.
 *
 * 3.Создание расписания.
 * С помощью {@link SimpleScheduleBuilder}
 * мы задали периодичность запуска.
 *
 * 4.Триггер.
 * Задача выполняется через триггер
 * {@link Trigger}. Там же можно указать,
 * когда начинать запуск. Мы указали,
 * чтобы запуск начинался сразу.
 *
 * 5.Загрузка задачи и триггера в планировщик.
 * С помощью метода
 * {@link Scheduler#scheduleJob} мы загружаем
 * задачу и триггер с планировщик.
 *
 * @author Constantine on 04.06.2022
 */
public class AlertRabbit {

    /**
     * Данный метод читает файл "rabbit.properties"
     * и загружает настройки.
     *
     * @return {@link Properties}.
     */
    public static Properties rabbitConfig() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            int interval = Integer.parseInt(rabbitConfig().getProperty("rabbit.interval"));
            JobDetail job = JobBuilder.newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here...");
        }
    }
}