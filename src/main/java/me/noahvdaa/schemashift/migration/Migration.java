package me.noahvdaa.schemashift.migration;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * A migration that can be executed on a database connection.
 * <p>
 * The id of a migration should be unique and should never change,
 * since otherwise it won't be possible to determine whether a migration
 * has already been applied to the database.
 */
public interface Migration {

    /**
     * The unique id of this migration.
     * This value should be final and should never change.
     *
     * @return this migration's unique id
     */
    @NotNull
    String id();

    /**
     * Tries to apply this migration to the specified connection.
     *
     * @param connection the connection to apply this migration to
     * @throws SQLException if an error occurs while applying this migration
     */
    void apply(@NotNull Connection connection) throws SQLException;

    /**
     * Creates a migration from the specified file.
     * The id will be generated from the filename, and the charset
     * will be assumed to be UTF-8.
     *
     * @param file the file to create the migration from
     * @return the created migration
     * @throws IOException if an error occurs while reading the file
     */
    @NotNull
    static Migration fromFile(@NotNull File file) throws IOException {
        Objects.requireNonNull(file, "file may not be null");

        return fromFile(
            file.getName(),
            file,
            StandardCharsets.UTF_8
        );
    }

    /**
     * Creates a migration from the specified file, with the specified id.
     * The charset will be assumed to be UTF-8.
     *
     * @param id   the id of the new migration
     * @param file the file to create the migration from
     * @return the created migration
     * @throws IOException if an error occurs while reading the file
     */
    @NotNull
    static Migration fromFile(@NotNull String id, @NotNull File file) throws IOException {
        return BaseMigrationImpl.fromFile(id, file, StandardCharsets.UTF_8);
    }

    /**
     * Creates a migration from the specified file, with the specified id and charset.
     *
     * @param id      the id of the new migration
     * @param file    the file to create the migration from
     * @param charset the charset to read the file with
     * @return the created migration
     * @throws IOException if an error occurs while reading the file
     */
    @NotNull
    static Migration fromFile(@NotNull String id, @NotNull File file, @NotNull Charset charset) throws IOException {
        return BaseMigrationImpl.fromFile(id, file, charset);
    }

    /**
     * Creates a migration from the specified resource.
     * The id will be generated from the filename, and the charset
     * will be assumed to be UTF-8.
     * The classloader for this class will be used to load the resource.
     *
     * @param resourcePath the path to the resource to create the migration from
     * @return the created migration
     * @throws IOException if an error occurs while reading the resource
     */
    @NotNull
    static Migration fromResource(@NotNull String resourcePath) throws IOException {
        Objects.requireNonNull(resourcePath, "resourcePath may not be null");

        return fromResource(
            new File(resourcePath).getName(),
            Migration.class.getClassLoader(),
            resourcePath,
            StandardCharsets.UTF_8
        );
    }

    /**
     * Creates a migration from the specified resource, with the specified id and classloader.
     * The charset will be assumed to be UTF-8.
     *
     * @param id           the id of the new migration
     * @param classLoader  the classloader to load the resource with
     * @param resourcePath the path to the resource to create the migration from
     * @return the created migration
     * @throws IOException if an error occurs while reading the resource
     */
    @NotNull
    static Migration fromResource(@NotNull String id, @NotNull ClassLoader classLoader, @NotNull String resourcePath) throws IOException {
        return fromResource(
            id,
            classLoader,
            resourcePath,
            StandardCharsets.UTF_8
        );
    }

    /**
     * Creates a migration from the specified resource, with the specified id, classloader and charset.
     *
     * @param id           the id of the new migration
     * @param classLoader  the classloader to load the resource with
     * @param resourcePath the path to the resource to create the migration from
     * @param charset      the charset to read the resource with
     * @return the created migration
     * @throws IOException if an error occurs while reading the resource
     */
    @NotNull
    static Migration fromResource(@NotNull String id, @NotNull ClassLoader classLoader, @NotNull String resourcePath, @NotNull Charset charset) throws IOException {
        return BaseMigrationImpl.fromResource(id, classLoader, resourcePath, charset);
    }

    /**
     * Creates a list of migrations by scanning all the files in the specified resource folder.
     * The id of each migration will be generated from the filename,
     * and the charset will be assumed to be UTF-8.
     *
     * @param folderPath the path to the folder to scan
     * @return the list of migrations
     * @throws IOException if an error occurs while reading the files
     */
    @NotNull
    static List<Migration> fromResourceFolder(@NotNull String folderPath) throws IOException {
        return fromResourceFolder(
            Migration.class.getClassLoader(),
            folderPath
        );
    }

    /**
     * Creates a list of migrations by scanning all the files in the specified resource folder,
     * with the specified classloader.
     * The id of each migration will be generated from the filename,
     * and the charset will be assumed to be UTF-8.
     *
     * @param classLoader the classloader to load the resources with
     * @param folderPath  the path to the folder to scan
     * @return the list of migrations
     * @throws IOException if an error occurs while reading the files
     */
    @NotNull
    private static List<Migration> fromResourceFolder(@NotNull ClassLoader classLoader, @NotNull String folderPath) throws IOException {
        return BaseMigrationImpl.fromResourceFolder(
            classLoader,
            folderPath
        );
    }

    /**
     * Creates a migration from the specified query.
     *
     * @param id    the id of the new migration
     * @param query the query to create the migration from
     * @return the created migration
     */
    @NotNull
    static Migration fromQuery(@NotNull String id, @NotNull String query) {
        return new BaseMigrationImpl(
            id,
            query
        );
    }

}
