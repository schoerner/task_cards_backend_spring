package de.acosci.tasks.common.config;

import jakarta.annotation.PostConstruct;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class LiquiBaseMigration {
    /**
     * Produktiv, z.B. über Config-Server
     * Lokal über lokale Property file
     */
    @Value("${spring.liquibase.change-log}")
    private String changeLogFile;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * 1. Spring Context
     * 2. Sucht Beans
     * 3. Erstellt Beans
     * Abhängigkeiten by Autowiring
     * IOC-Containter (Inversion of Control)
     *
     * nachdem die Bean erstellt wurde => siehe Spring Boot lifecycle
     */
    @PostConstruct
    public void migrate() {
        // Connection aufbauen
        Connection connection = null;

        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .username(username)
                    .password(password)
                    .url(url)
                    .driverClassName(driverClassName)
                    .build();

            connection = dataSource.getConnection();


            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try {
                var liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());
            } catch (LiquibaseException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }

        // Liquibase update

    }
}
