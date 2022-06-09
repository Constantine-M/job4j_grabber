package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 1. Quartz.
 * 1.1. Job c параметрами.
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
public class AlertRabbit implements AutoCloseable {

    /**
     * Данный метод устанавливает
     * соединение с БД и возвращает
     * {@link Connection}.
     *
     * Если {@link Connection} != null,
     * то соединение установлено.
     *
     * @return {@link Connection}.
     */
    public Connection init(Properties config) {
        try {
            Class.forName(config.getProperty("postgre-Parser"));
            return DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Данный метод удаляет таблицу.
     * Написал просто так, потому что
     * во время выполнения устал
     * удалять таблицу руками из БД.
     *
     * @param cn соединение с БД.
     */
    public void dropTable(Connection cn) {
        String sql = "DROP TABLE IF EXISTS rabbit";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
        Properties config = rabbitConfig();
        try (AlertRabbit rabbit = new AlertRabbit(); Connection cn = rabbit.init(config)) {
            rabbit.dropTable(cn);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            int interval = Integer.parseInt(config.getProperty("rabbit.interval"));
            JobDetail job = JobBuilder.newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Данный метод закрывает
     * соединение.
     *
     * Т.к. наш класс объявлен
     * {@link AutoCloseable}.
     * Создаем {@link AlertRabbit}
     * один раз, используя конструкцию
     * выше, а внутри производим
     * все-все-все манипуляции с таблицей.
     * После этого соединение автоматически
     * закрывается.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        Properties config = rabbitConfig();
        Connection cn = init(config);
        if (cn != null) {
            cn.close();
        }
    }

    public static class Rabbit implements Job {

        /**
         * Каждый запуск работы {@link Job}
         * вызывает конструктор.
         */
        public Rabbit() {
            System.out.println("Calling constructor...");
        }

        /**
         * Данный метод предназначен для
         * вывода времени, которое он
         * берет из таблицы rabbit.
         *
         * Т.к. в нашей таблице только поле
         * со временем, то я просто вывел
         * всё что есть в таблице и вернул
         * результат.
         *
         * Метод написан "лишь бы видеть
         * время на консоли".
         *
         * @param connection соединение с БД.
         * @return время выполнения {@link Job}.
         * @throws SQLException
         */
        private Timestamp displayTime(Connection connection) throws SQLException {
            Timestamp time = null;
            String selectSql = "SELECT * FROM rabbit";
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        time = resultSet.getTimestamp(1);
                    }
                }
            }
            return time;
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
         * 4.В методе {@link AlertRabbit#main}
         * при создании {@link Job} мы указываем
         * параметры data. В них мы передаем ссылку
         * на {@link Connection}.
         *
         * 5.Чтобы получить объекты (или что-то)
         * из context, используются методы
         * {@link JobExecutionContext#getJobDetail()}
         * и {@link JobDetail#getJobDataMap()}.
         *
         * 6.Полученные объекты являются
         * общими для каждой работы. В нашем
         * случае - это {@link Connection}.
         *
         * @param context общий ресурс,
         *                откуда мы можем взять данные.
         * @throws JobExecutionException
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here...");
            Connection connection = (Connection) context
                    .getJobDetail()
                    .getJobDataMap()
                    .get("connection");
            try (Statement statement = connection.createStatement()) {
                String createSql = String.format("CREATE TABLE IF NOT EXISTS rabbit (%s)",
                        "created_date TIMESTAMP"
                );
                statement.execute(createSql);
                String insertSql = "INSERT INTO rabbit (created_date) VALUES (?)";
                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    ps.execute();
                }
                System.out.println("Job's done at " + displayTime(connection));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}