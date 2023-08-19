# SchemaShift

SchemaShift is a tiny, simple migration framework for Java. It's designed for use with MariaDB/MySQL databases, but
other database systems with a compatible syntax should also work.

## Usage

```java
SchemaShift migrator = SchemaShift.with(myConnection);

// Directly create a migration from a query
migrator.registerMigration(Migration.fromQuery(
    "2023_08_01_add_mytable",
    "CREATE TABLE IF NOT EXISTS `mytable` ( ... );"
));

// Or read from a resource:
migrator.registerMigration(
    Migration.fromResource("2023_08_02_add_another_table.sql")
);

// Or read an entire folder of resources!
migrator.registerMigrations(
    Migration.fromResourceFolder("migrations")
);

migrator.migrateLatest();
```

## Download

SchemaShift is currently in development, so snapshots are deployed to the bytecode.space snapshots repository, which you
must manually add to your build tool's repositories section:

```kotlin
repositories {
    maven {
        url = uri("https://repo.bytecode.space/repository/maven-public/")
    }
}
```

```xml

<repositories>
    <repository>
        <id>bytecodespace</id>
        <url>https://repo.bytecode.space/repository/maven-public/</url>
    </repository>
</repositories>
```

Add the dependency:

```kotlin
dependencies {
    implementation("me.noahvdaa:schemashift:1.0.0-SNAPSHOT")
}
```

```xml
<dependencies>
    <dependency>
        <groupId>me.noahvdaa</groupId>
        <artifactId>schemashift</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```
