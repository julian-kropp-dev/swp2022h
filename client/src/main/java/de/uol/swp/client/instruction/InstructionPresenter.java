package de.uol.swp.client.instruction;

import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.textureatlas.TextureAtlasFloorPlans;
import de.uol.swp.client.textureatlas.TextureAtlasInstruction;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

/**
 * Manages the InstructionView.
 *
 * @see de.uol.swp.client.AbstractPresenter
 */
public class InstructionPresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/InstructionTab.fxml";
  private static final List<String> IMAGE_NAMES =
      Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
  @FXML private ScrollPane scrollPane;
  @FXML private FlowPane flowPane;

  /** Initialize from JavaFX. */
  @FXML
  public void initialize() {
    flowPane.setPrefWidth(scrollPane.getPrefWidth());
    flowPane.setMaxWidth(scrollPane.getPrefWidth());
    scrollPane.setFitToHeight(true);

    IMAGE_NAMES.forEach(this::showPicture);
  }

  private void showPicture(String name) {
    Platform.runLater(
        () -> {
          flowPane
              .getChildren()
              .add(new ImageView(TextureAtlasInstruction.getPage(Integer.parseInt(name))));
          ImageView imageView =
              (ImageView) flowPane.getChildren().get(flowPane.getChildren().size() - 1);
          imageView.setPreserveRatio(true);
          imageView.setFitWidth(scrollPane.getPrefWidth());
        });
  }
}
