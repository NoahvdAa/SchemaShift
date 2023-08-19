package me.noahvdaa.schemashift.migration;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A basic implementation that executes a migration from the query string stored inside.
 * This class can be extended to create a more advanced migration implementation, such as one
 * that runs multiple queries depending on the results of the previous one.
 */
public class BaseMigrationImpl implements Migration {

    private final String id;
    private final String query;

    BaseMigrationImpl(@NotNull String id, @NotNull String query) {
        this.id = Objects.requireNonNull(id, "id may not be null");
        this.query = Objects.requireNonNull(query, "query may not be null");
    }

    @NotNull
    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void apply(@NotNull Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(this.query);
        }
    }

    @NotNull
    static BaseMigrationImpl fromFile(@NotNull String id, @NotNull File file, @NotNull Charset charset) throws IOException {
        Objects.requireNonNull(file, "file may not be null");

        try (FileInputStream is = new FileInputStream(file)) {
            return fromStream(id, is, charset);
        }
    }

    @NotNull
    static BaseMigrationImpl fromResource(@NotNull String id, @NotNull ClassLoader classLoader, @NotNull String resourcePath, @NotNull Charset charset) throws IOException {
        Objects.requireNonNull(classLoader, "classLoader may not be null");
        Objects.requireNonNull(resourcePath, "resourcePath may not be null");

        try (InputStream stream = classLoader.getResourceAsStream(resourcePath)) {
            if (stream == null)
                throw new FileNotFoundException("Resource " + resourcePath + " not found.");

            return fromStream(id, stream, charset);
        }
    }

    @NotNull
    static List<Migration> fromResourceFolder(@NotNull ClassLoader classLoader, @NotNull String folderPath) throws IOException {
        Objects.requireNonNull(classLoader, "classLoader may not be null");
        Objects.requireNonNull(folderPath, "folderPath may not be null");

        List<Migration> migrations = new ArrayList<>();

        try (
            InputStream in = classLoader.getResourceAsStream(folderPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))
        ) {
            String resource;

            while ((resource = br.readLine()) != null) {
                migrations.add(fromResource(resource, classLoader, folderPath + "/" + resource, StandardCharsets.UTF_8));
            }
        }

        return migrations;
    }

    @NotNull
    static BaseMigrationImpl fromStream(@NotNull String id, @NotNull InputStream stream, @NotNull Charset charset) throws IOException {
        Objects.requireNonNull(id, "id may not be null");
        Objects.requireNonNull(stream, "stream may not be null");
        Objects.requireNonNull(charset, "charset may not be null");

        byte[] data = stream.readAllBytes();
        String query = new String(data, charset);

        return new BaseMigrationImpl(id, query);
    }

}
