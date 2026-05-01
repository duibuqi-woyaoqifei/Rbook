package com.rbook.di;

import com.rbook.data.local.RBookDatabase;
import com.rbook.data.local.dao.StatsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DatabaseModule_ProvideStatsDaoFactory implements Factory<StatsDao> {
  private final Provider<RBookDatabase> databaseProvider;

  public DatabaseModule_ProvideStatsDaoFactory(Provider<RBookDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StatsDao get() {
    return provideStatsDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideStatsDaoFactory create(
      Provider<RBookDatabase> databaseProvider) {
    return new DatabaseModule_ProvideStatsDaoFactory(databaseProvider);
  }

  public static StatsDao provideStatsDao(RBookDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStatsDao(database));
  }
}
