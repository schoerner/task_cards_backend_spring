package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser_Id(Long userId);

    @Query("""
            select up
            from UserProfile up
            where (:query is null or trim(:query) = ''
                   or lower(coalesce(up.name, '')) like lower(concat('%', :query, '%'))
                   or lower(coalesce(up.contactEmail, '')) like lower(concat('%', :query, '%')))
            order by
                case
                    when lower(coalesce(up.name, '')) like lower(concat(:query, '%')) then 0
                    when lower(coalesce(up.contactEmail, '')) like lower(concat(:query, '%')) then 1
                    else 2
                end,
                lower(coalesce(up.name, '')),
                lower(coalesce(up.contactEmail, ''))
            """)
    List<UserProfile> searchByNameOrContactEmail(@Param("query") String query);
}
