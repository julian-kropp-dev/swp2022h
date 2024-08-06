package de.uol.swp.client.di;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import javafx.fxml.FXMLLoader;

/** Class that provides Instances of the FXMLLoader. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class FXMLLoaderProvider implements Provider<FXMLLoader> {
  @Inject Injector injector;

  @Override
  public FXMLLoader get() {
    FXMLLoader loader = new FXMLLoader();
    loader.setControllerFactory(injector::getInstance);
    return loader;
  }
}
