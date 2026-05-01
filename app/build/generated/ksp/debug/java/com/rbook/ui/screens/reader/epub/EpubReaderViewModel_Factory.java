package com.rbook.ui.screens.reader.epub;

import com.rbook.data.readium.ReadiumManager;
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
public final class EpubReaderViewModel_Factory implements Factory<EpubReaderViewModel> {
  private final Provider<ReadiumManager> readiumManagerProvider;

  public EpubReaderViewModel_Factory(Provider<ReadiumManager> readiumManagerProvider) {
    this.readiumManagerProvider = readiumManagerProvider;
  }

  @Override
  public EpubReaderViewModel get() {
    return newInstance(readiumManagerProvider.get());
  }

  public static EpubReaderViewModel_Factory create(
      Provider<ReadiumManager> readiumManagerProvider) {
    return new EpubReaderViewModel_Factory(readiumManagerProvider);
  }

  public static EpubReaderViewModel newInstance(ReadiumManager readiumManager) {
    return new EpubReaderViewModel(readiumManager);
  }
}
