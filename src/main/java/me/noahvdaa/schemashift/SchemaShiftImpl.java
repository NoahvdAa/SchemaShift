package me.noahvdaa.schemashift;

import me.noahvdaa.schemashift.migration.Migration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
final class SchemaShiftImpl implements SchemaShift {

    static final String DEFAULT_MIGRATIONS_TABLE = "schemashift_meta";
    private final Connection connection;
    private final String migrationsTable;
    private final List<Migration> migrations;
    private String lastMigration = null;

    private final static String TABLE_CHECK_QUERY = """
        SELECT COUNT(*) FROM `information_schema`.`tables` WHERE `table_schema` = DATABASE() AND `table_name` = ?;
        """;
    private final static String LATEST_MIGRATION_QUERY = """
        SELECT `last_migration_id` FROM `%s`;
        """;
    private final static String CREATE_SCHEMASHIFT_META_TABLE = """
        CREATE TABLE IF NOT EXISTS `%s` (
            `version` ENUM('1') NOT NULL PRIMARY KEY DEFAULT '1',
            `last_migration_id` VARCHAR(255) NULL
        );
        """;
    private final static String UPDATE_LAST_MIGRATION_QUERY = """
        INSERT INTO `%s` (`last_migration_id`) VALUES (?) ON DUPLICATE KEY UPDATE `last_migration_id` = ?;
        """;


    SchemaShiftImpl(@NotNull Connection connection, @NotNull String migrationsTable) {
        this.connection = Objects.requireNonNull(connection, "connection may not be null");
        this.migrationsTable = Objects.requireNonNull(migrationsTable, "migrationsTable may not be null");
        this.migrations = new ArrayList<>();
    }

    @Override
    public void registerMigration(@NotNull Migration migration) {
        Objects.requireNonNull(migration, "migration may not be null");
        Optional<Migration> existingMigration = this.migrations.stream().filter((mig) -> mig.id().equals(migration.id())).findAny();
        if (existingMigration.isPresent())
            throw new IllegalArgumentException("A migration with the id '" + migration.id() + "' is already registered");

        this.migrations.add(migration);
        this.lastMigration = migration.id();
    }

    @Override
    public void registerMigrations(@NotNull List<Migration> migrations) {
        Objects.requireNonNull(migrations, "migrations may not be null");
        for (Migration migration : migrations) {
            this.registerMigration(migration);
        }
    }

    @NotNull
    @Override
    public List<Migration> migrations() {
        return List.copyOf(this.migrations);
    }

    @Nullable
    @Override
    public List<Migration> migrationsAfter(@NotNull String migration) {
        Objects.requireNonNull(migration, "migration may not be null");
        Optional<Migration> existingMigration = this.migrations.stream().filter((mig) -> mig.id().equals(migration)).findFirst();
        if (existingMigration.isEmpty())
            return null;

        int index = this.migrations.indexOf(existingMigration.get());
        return this.migrations.subList(index + 1, this.migrations.size());
    }

    @Override
    public boolean migrateLatest() throws SQLException {
        String latestCompleted = this.getLatestMigrationId();

        // already up to date
        if (Objects.equals(latestCompleted, this.lastMigration))
            return false;

        List<Migration> toRun = latestCompleted == null ? this.migrations : this.migrationsAfter(latestCompleted);
        if (toRun == null)
            return false;

        // Make sure the schemashift meta table exists now.
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(String.format(CREATE_SCHEMASHIFT_META_TABLE, this.migrationsTable));
        }

        try (PreparedStatement updateLastMigrationStatement = this.connection.prepareStatement(String.format(UPDATE_LAST_MIGRATION_QUERY, this.migrationsTable))) {
            for (Migration migration : toRun) {
                migration.apply(this.connection);

                updateLastMigrationStatement.setString(1, migration.id());
                updateLastMigrationStatement.setString(2, migration.id());
                updateLastMigrationStatement.execute();
            }
        }

        return true;
    }

    @Nullable
    private String getLatestMigrationId() throws SQLException {
        if (!this.hasMigrationsTable())
            return null;

        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(String.format(LATEST_MIGRATION_QUERY, this.migrationsTable));
            if (!resultSet.next())
                return null;

            return resultSet.getString(1);
        }
    }

    private boolean hasMigrationsTable() throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(TABLE_CHECK_QUERY)) {
            statement.setString(1, this.migrationsTable);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return false;

            return resultSet.getInt(1) != 0;
        }
    }

}
