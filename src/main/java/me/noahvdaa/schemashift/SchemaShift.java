package me.noahvdaa.schemashift;

import me.noahvdaa.schemashift.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An instance of SchemaShift, containing a set of database migrations
 * and a database connection to apply them to.
 */
public interface SchemaShift {

    /**
     * Registers the specified migration.
     * Keep in mind that registration order is important, if a new migration
     * is registered before an old migration that was already executed,
     * it will not be executed!
     *
     * @param migration the migration to register
     */
    void registerMigration(@NotNull Migration migration);

    /**
     * Registers the specified migrations, in the order they were provided.
     * Keep in mind that registration order is important, if a new migration
     * is registered before an old migration that was already executed,
     * it will not be executed!
     *
     * @param migrations the migrations to register
     */
    default void registerMigrations(@NotNull Migration... migrations) {
        this.registerMigrations(Arrays.asList(migrations));
    }

    /**
     * Registers the specified migrations, in the order they were provided.
     * Keep in mind that registration order is important, if a new migration
     * is registered before an old migration that was already executed,
     * it will not be executed!
     *
     * @param migrations the migrations to register
     */
    void registerMigrations(@NotNull List<Migration> migrations);

    /**
     * Returns an immutable copy of all registered migrations.
     *
     * @return all registered migrations
     */
    @NotNull
    List<Migration> migrations();

    /**
     * Returns an immutable copy of all registered migrations that are
     * registered after the specified migration, or null if no migration
     * exists with the specified id.
     *
     * @param migration the migration to get all migrations after
     * @return all registered migrations after the specified migration, or null if the migration isn't registered
     */
    @Nullable
    default List<Migration> migrationsAfter(@NotNull Migration migration) {
        Objects.requireNonNull(migration, "migration may not be null");
        return this.migrationsAfter(migration.id());
    }

    /**
     * Returns an immutable copy of all registered migrations that are
     * registered after the specified migration, or null if no migration
     * exists with the specified id.
     *
     * @param migration the migration id to get all migrations after
     * @return all registered migrations after the specified migration, or null if the migration isn't registered
     */
    @Nullable
    List<Migration> migrationsAfter(@NotNull String migration);

    /**
     * Applies all un-applied migrations to the current connection.
     * The return value indicates whether at least one migration was applied.
     *
     * @return whether at least one migration was applied
     * @throws SQLException if an error occurs while applying the migrations
     */
    boolean migrateLatest() throws SQLException;

    /**
     * Creates a new SchemaShift instance with the specified connection.
     *
     * @param connection the connection to use
     * @return the new SchemaShift instance
     */
    static SchemaShift with(@NotNull Connection connection) {
        return new SchemaShiftImpl(
            connection,
            SchemaShiftImpl.DEFAULT_MIGRATIONS_TABLE
        );
    }

    /**
     * Creates a new SchemaShift instance with the specified connection, using
     * a custom name for the table that keeps track of applied migrations.
     * This can be useful if you want to use the same database for multiple
     * applications.
     *
     * @param connection      the connection to use
     * @param migrationsTable the name of the table that keeps track of applied migrations
     * @return the new SchemaShift instance
     */
    static SchemaShift with(@NotNull Connection connection, @NotNull String migrationsTable) {
        return new SchemaShiftImpl(
            connection,
            migrationsTable
        );
    }

}
