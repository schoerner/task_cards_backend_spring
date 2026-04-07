package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Optional public profile data for a user that can be shown to other project members.
 * Uses a shared primary key with the corresponding user entity.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;

    @JsonIgnore
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /**
     * Public display name shown inside the application.
     */
    @Column(length = 100)
    private String name;

    /**
     * Optional public contact email address shown to other members.
     * This may differ from the login email.
     */
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    /**
     * Optional URL to the user's profile image.
     */
    @Column(name = "picture_url", length = 1000)
    private String pictureUrl;

    /**
     * Optional short biography / profile description.
     */
    @Column(length = 2000)
    private String description;
}