package parser;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 6. Grabber.
 *
 * Данный класс является связующим
 * элементом между планировщиком,
 * хранилищем и непосредственно
 * парсером. В нем мы и соединим все
 * элементы.
 *
 * @author Constantine on 30.06.2022
 */
public class Grabber implements Grab {

    private final Properties cfg = new Properties();

    public Store store() {
        return new PsqlStore(cfg);
    }

    /**
     * Cконфигурируем сам планировщик
     * {@link Scheduler} и начнем его работу
     * {@link Scheduler#start()}.
     * @return объект планировщик {@link Scheduler}.
     * @throws SchedulerException
     */
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() {
        try (InputStream in = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Данный метод инициализирует
     * работу планировщика.
     *
     * 1.Создадим хранилище {@link JobDataMap}.
     * В нем мы будем хранить дополнительные
     * параметры,  которые будем
     * передавать в {@link Job}.
     * Они пригодятся при выполнении
     * планировщика в методе
     * {@link GrabJob#execute}.
     *
     * 2.Создадим задачу с помощью
     * {@link JobDetail} и метода
     * {@link JobBuilder#newJob()}.
     * Я сразу написал newJob (я вверху
     * статику добавил).
     * Quartz каждый раз создает объект с
     * типом {@link Job} (это интерфейс),
     * поэтому мы создали класс
     * {@link Grabber.GrabJob},
     * который реализует этот интерфейс.
     *
     * 3.Далее добавим в планировщик
     * расписание, а точнее
     * интервал, с которым будет
     * выполняться {@link Job}.
     * Для этого воспользуемся
     * {@link SimpleScheduleBuilder#simpleSchedule()}.
     * Заметь - я снова использовал
     * статику вверху, чтобы не писать
     * лишнее.
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
     * Внутри этого класса мы описали
     * требуемые действия.
     * @param parse парсер объявлений.
     * @param store хранилище объявлений.
     * @param scheduler планировщик
     */
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        data.put("cfg", cfg);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        public GrabJob() {
            System.out.println("Calling constructor...");
        }

        /**
         * Данный метод выполняет работу
         * объекта {@link Job}.
         *
         * 1.Quartz создает объект {@link Job},
         * каждый раз при выполнении работы.
         *
         * 2.Объект коннект будет передаваться в Job.
         *
         * 3.Каждый запуск работы {@link Job} вызывает
         * конструктор. Чтобы в объект Job иметь
         * общий ресурс нужно использовать
         * {@link JobExecutionContext}.
         *
         * 4.В методе {@link Grabber#main}
         * при создании {@link Job} мы указываем
         * параметры data. В них мы передаем ссылки
         * на {@link Parse} и {@link Store}.
         *
         * 5.Чтобы получить объекты (или что-то)
         * из context, используются методы
         * {@link JobExecutionContext#getJobDetail()}
         * и {@link JobDetail#getJobDataMap()}.
         *
         * 6.Полученные объекты являются
         * общими для каждой работы. У нас -
         * это хранилище и парсер.
         *
         * Здесь мы НЕ МОЖЕМ использовать
         * конструкцию try-catch, поэтому
         * не совсем понятно, закрывается
         * соединение автоматически или
         * нет.
         * Предположу, что в {@link Grabber}
         * мы передаем интерфейс {@link Store},
         * в котором в свою очередь мы
         * можем вернуть любое хранилище.
         * В нашем случае - это {@link PsqlStore}.
         * Соответственно и методы будут
         * выполняться в том классе (открытие/
         * закрытие соединения в том числе).
         *
         * @param context общий ресурс,
         *                откуда мы можем взять данные.
         * @throws JobExecutionException
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("GRABBER RUNS HERE...");
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            Properties cfg = (Properties) map.get("cfg");
            List<Post> posts = parse.list(cfg.getProperty("habrPage"));
            System.out.println("SAVING VACANCIES...");
            posts.forEach(store::save);
            System.out.println("AND NOW, GET ALL VACANCIES");
            List<Post>  vacancies = store.getAll();
            vacancies.forEach(System.out::println);
            System.out.println("AND THEN FIND BY ID = 165");
            System.out.println(store.findById(165));
            System.out.println(System.lineSeparator());
            System.out.println("PARSING WILL START AGAIN...IN "
                    + cfg.getProperty("time") + " sec");
        }
    }

    public static void main(String[] args) throws SchedulerException {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
    }
}