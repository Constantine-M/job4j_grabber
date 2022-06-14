import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 2.2. Модель данных - Post.
 *
 * Данный класс описывает вакансию.
 *
 * @author Constantine on 14.06.2022
 */
public class Post {

    private final int id;

    /** Название вакансии. */
    private final String title;

    /** Ссылка на описание вакансии. */
    private final String link;

    /** Описание вакансии. */
    private final String description;

    /** Дата создания вакансии. */
    private final LocalDateTime created;

    public Post(int id, String title, String link, String description, LocalDateTime created) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.description = description;
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return id == post.id
                && Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, link);
    }

    @Override
    public String toString() {
        return "Post{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", link='" + link + '\''
                + ", description='" + description + '\''
                + ", created=" + created
                + '}';
    }
}
