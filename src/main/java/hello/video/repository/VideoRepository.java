package hello.video.repository;

import hello.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v JOIN FETCH v.user ORDER BY v.uploadDate DESC")
    List<Video> findAllWithUserOrderedByUploadDate();

    @Query("SELECT DISTINCT v FROM Video v " +
            "LEFT JOIN FETCH v.user " +
            "LEFT JOIN FETCH v.likes " +
            "ORDER BY v.uploadDate DESC")
    List<Video> getVideoListWithLikes();
}
