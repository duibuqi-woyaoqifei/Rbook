package com.rbook.data.repository;

import com.rbook.data.local.dao.StatsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class StatsRepositoryImpl_Factory implements Factory<StatsRepositoryImpl> {
  private final Provider<StatsDao> statsDaoProvider;

  public StatsRepositoryImpl_Factory(Provider<StatsDao> statsDaoProvider) {
    this.statsDaoProvider = statsDaoProvider;
  }

  @Override
  public StatsRepositoryImpl get() {
    return newInstance(statsDaoProvider.get());
  }

  public static StatsRepositoryImpl_Factory create(Provider<StatsDao> statsDaoProvider) {
    return new StatsRepositoryImpl_Factory(statsDaoProvider);
  }

  public static StatsRepositoryImpl newInstance(StatsDao statsDao) {
    return new StatsRepositoryImpl(statsDao);
  }
}
