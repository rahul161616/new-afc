# Liquibase

Schema migrations live under:

```text
src/main/resources/db/changelog
```

The master changelog is:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

Current baseline:

```text
src/main/resources/db/changelog/changes/001-baseline-schema.yaml
```

To add a schema change:

1. Create a new file in `src/main/resources/db/changelog/changes/`.
2. Use a new changeSet id, for example `007-add-response-deadline`.
3. Include the file from `db.changelog-master.yaml`.
4. Never edit an already-applied changeSet in production.

For a fresh Render database, Liquibase will create the schema automatically at app startup.

For an existing database that already has tables, either start with a fresh Render PostgreSQL database or baseline the existing database before enabling this changelog.
