package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.BeamShapeLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.GBeamLinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.PeakCentresLinePlotX;
import org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.BeamShapeLinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.GBeamLinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes.BeamDataOutputDriverExperiment;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_HEIGHT;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots.MCMCPlotsWindow.PLOT_WINDOW_WIDTH;
import static org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsWindow.plottingWindow;

public class PeakShapeDemoPlotsController {

    public static List<File> resourceFilesInFolder;

    public static File resourceBrowserTarget;

    public static String currentGroup;

    public static int currentGroupIndex;

    public static int remSize;

    public static List<ImageView> beamImageSet;

    public static List<ImageView> gBeamImageSet;

    static Map<String, List<File>> resourceGroups;

    ListView<File> listViewOfResourcesInFolder;

    AbstractPlot peakCentreLinePlot;
    @FXML
    private ScrollPane resourceListScrollPane;

    @FXML
    private AnchorPane resourceListAnchorPane;

    @FXML
    private AnchorPane plotsAnchorPane;

    @FXML
    private AnchorPane plotPane;

    @FXML
    private VBox masterVBox;

    @FXML
    private ToolBar toolbar;

    @FXML
    private GridPane beamPlotsGridPane;
    @FXML
    private GridPane gBeamPlotGridPane;

    @FXML
    private GridPane peakCentreGridPane;

    @FXML
    private AnchorPane eventAnchorPane;

    @FXML
    private ScrollPane eventScrollPane;

    @FXML
    private TextArea eventLogTextArea;


    public static String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        PeakShapeDemoPlotsController.currentGroup = currentGroup;
    }

    public static List<File> getResourceGroups(String group) {
        return resourceGroups.get(group);
    }

    @FXML
    void initialize() {
        masterVBox.setPrefSize(PLOT_WINDOW_WIDTH, PLOT_WINDOW_HEIGHT);
        toolbar.setPrefSize(PLOT_WINDOW_WIDTH, 30.0);

        masterVBox.prefWidthProperty().bind(plotsAnchorPane.widthProperty());
        masterVBox.prefHeightProperty().bind(plotsAnchorPane.heightProperty());

        plotPane.prefWidthProperty().bind(masterVBox.widthProperty());
        plotPane.prefHeightProperty().bind(masterVBox.heightProperty());

        beamPlotsGridPane.prefWidthProperty().bind(plotPane.widthProperty());
        beamPlotsGridPane.prefHeightProperty().bind(plotPane.heightProperty());

        gBeamPlotGridPane.prefWidthProperty().bind(plotPane.widthProperty());
        gBeamPlotGridPane.prefHeightProperty().bind(plotPane.heightProperty());

        peakCentreGridPane.prefWidthProperty().bind(plotPane.widthProperty());
        peakCentreGridPane.prefHeightProperty().bind(plotPane.heightProperty());


        resourceListAnchorPane.prefWidthProperty().bind(resourceListScrollPane.widthProperty());
        resourceListAnchorPane.prefHeightProperty().bind(resourceListScrollPane.heightProperty());

        eventAnchorPane.prefHeightProperty().bind(eventScrollPane.heightProperty());
        eventAnchorPane.prefWidthProperty().bind(eventScrollPane.widthProperty());
        eventLogTextArea.prefHeightProperty().bind(eventAnchorPane.heightProperty());
        eventLogTextArea.prefWidthProperty().bind(eventAnchorPane.widthProperty());


    }


    @FXML
    public void browseResourceFileAction() {
        resourceBrowserTarget = FileHandlerUtil.selectPeakShapeResourceFolderForBrowsing(plottingWindow);
        if (resourceBrowserTarget == null) {
            System.out.println("File not chosen");
        } else {
            populateListOfGroups();
        }

    }

    private void populateListOfGroups() {

        resourceFilesInFolder = new ArrayList<>();
        File[] allFiles;
        resourceGroups = new TreeMap<>();

        if (null != resourceBrowserTarget) {
            for (File file : Objects.requireNonNull(resourceBrowserTarget.listFiles((file, name) -> name.toLowerCase().endsWith(".txt")))) {
                try {
                    List<String> contentsByLine = new ArrayList<>(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
                    if (5 < contentsByLine.size() && (contentsByLine.get(4).startsWith("Peak Centre Mass"))) {
                        resourceFilesInFolder.add(file);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }


        if (!resourceFilesInFolder.isEmpty()) {
            ListView<String> listViewOfGroupResourcesInFolder = new ListView<>();
            listViewOfGroupResourcesInFolder.setCellFactory(
                    (parameter)
                            -> new PeakShapeDemoPlotsController.ResourceDisplayName()
            );
            allFiles = resourceFilesInFolder.toArray(new File[0]);
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("");

            // Generates a map of groups
            Pattern groupPattern = Pattern.compile("C-(.*?)-S");

            for (File allFile : allFiles) {
                // Checks if substring in filename is already present in map
                Matcher groupMatch = groupPattern.matcher(allFile.getName());
                if (groupMatch.find()) {
                    if (resourceGroups.containsKey(groupMatch.group(1))) {
                        resourceGroups.get(groupMatch.group(1)).add(allFile);
                    } else {
                        resourceGroups.put(groupMatch.group(1), new ArrayList<>());
                        resourceGroups.get(groupMatch.group(1)).add(allFile);
                    }
                }

            }


            IntuitiveStringComparator<String> intuitiveStringComparator = new IntuitiveStringComparator<>();
            for (Map.Entry<String, List<File>> entry : resourceGroups.entrySet()) {
                List<File> files = entry.getValue();
                files.sort((file1, file2) -> intuitiveStringComparator.compare(file1.getName(), file2.getName()));
            }

            ObservableList<String> items = FXCollections.observableArrayList(resourceGroups.keySet().stream().toList());
            listViewOfGroupResourcesInFolder.setItems(items);


            listViewOfGroupResourcesInFolder.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Files will be manipulated here when group is selected
                currentGroup = newValue;
                processFilesAndShowPeakCentre(newValue);
                populateListOfResources(newValue);
                eventLogTextArea.textProperty().unbind();
                eventLogTextArea.setText("Select File From Plot");
            });


            listViewOfGroupResourcesInFolder.getSelectionModel().selectFirst();
            listViewOfGroupResourcesInFolder.prefHeightProperty().bind(resourceListAnchorPane.prefHeightProperty());
            listViewOfGroupResourcesInFolder.prefWidthProperty().bind(resourceListAnchorPane.prefWidthProperty());
            resourceListAnchorPane.getChildren().add(listViewOfGroupResourcesInFolder);
            eventLogTextArea.setText("Select File From Plot");

        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("No valid resources");


            resourceListAnchorPane.getChildren().removeAll();
            eventAnchorPane.getChildren().removeAll();


            beamPlotsGridPane.getChildren().removeAll();
            peakCentreGridPane.getChildren().removeAll();

        }

    }


    public void processFilesAndShowPeakCentre(String groupValue) {

        double[] finalYAxis;
        double[] finalXAxis;
        beamImageSet = new ArrayList<>();
        gBeamImageSet = new ArrayList<>();

        if (plotsAnchorPane.getChildren().size() > 1) {
            plotsAnchorPane.getChildren().remove(1, remSize);
        }
        remSize = 1;

        double[] xAxis = new double[resourceGroups.get(groupValue).size()];
        double[] yAxis = new double[resourceGroups.get(groupValue).size()];
        for (int k = 0; k < resourceGroups.get(groupValue).size(); k++) {
            resourceBrowserTarget = resourceGroups.get(groupValue).get(k);
            if (null != resourceBrowserTarget && resourceBrowserTarget.isFile()) {
                try {
                    PlotBuilder[] plots = BeamDataOutputDriverExperiment.modelTest(resourceBrowserTarget.toPath(), this::processFilesAndShowPeakCentre);
                    xAxis[k] = k + 1;
                    yAxis[k] = BeamDataOutputDriverExperiment.getMeasBeamWidthAMU();
                    AbstractPlot gBeamLinePlot = GBeamLinePlot.generatePlot(
                            new Rectangle(beamPlotsGridPane.getCellBounds(0, 0).getWidth(),
                                    beamPlotsGridPane.getCellBounds(0, 0).getHeight()),
                            (GBeamLinePlotBuilder) plots[1]
                    );

                    AbstractPlot beamShapeLinePlot = BeamShapeLinePlot.generatePlot(
                            new Rectangle(beamPlotsGridPane.getCellBounds(0, 0).getWidth(),
                                    beamPlotsGridPane.getCellBounds(0, 0).getHeight()),
                            (BeamShapeLinePlotBuilder) plots[0]
                    );

                    gBeamLinePlot.preparePanel();
                    beamShapeLinePlot.preparePanel();

                    // Creates a rendered image of the beam shape and g-beam line plots
                    WritableImage writableImage1 = new WritableImage((int) beamShapeLinePlot.getWidth(), (int) beamShapeLinePlot.getHeight());
                    beamShapeLinePlot.snapshot(null, writableImage1);
                    ImageView image1 = new ImageView(writableImage1);
                    image1.setFitWidth(85);
                    image1.setFitHeight(46);

                    WritableImage writableImage2 = new WritableImage((int) gBeamLinePlot.getWidth(), (int) gBeamLinePlot.getHeight());
                    gBeamLinePlot.snapshot(null, writableImage2);
                    ImageView image2 = new ImageView(writableImage2);
                    image2.setFitWidth(85);
                    image2.setFitHeight(46);

                    // adds the rendered images to a list that will be used later
                    beamImageSet.add(image1);
                    gBeamImageSet.add(image2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        finalYAxis = yAxis;
        finalXAxis = xAxis;

        LinePlotBuilder peakCentrePlotBuilder = LinePlotBuilder.initializeLinePlot(finalXAxis, finalYAxis, new String[]{"PeakCentre Plot"}, "Blocks", "Peak Widths");

        peakCentreLinePlot = PeakCentresLinePlotX.generatePlot(new Rectangle(peakCentreGridPane.getCellBounds(0, 0).getWidth(), peakCentreGridPane.getCellBounds(0, 0).getHeight()), peakCentrePlotBuilder);

        peakCentreGridPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            peakCentreLinePlot.setWidthF(newValue.intValue());
            peakCentreLinePlot.repaint();
        });

        peakCentreGridPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            peakCentreLinePlot.setHeightF(newValue.intValue());
            peakCentreLinePlot.repaint();
            if (plotsAnchorPane.getChildren().size() > 1) {
                plotsAnchorPane.getChildren().remove(1, remSize);
            }
            remSize = 1;

            int size = 1;
            for (int i = 0; i < gBeamImageSet.size(); i++) {
                plotsAnchorPane.getChildren().add(gBeamImageSet.get(i));
                ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
                pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
                if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 200) {
                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 300);
                } else {
                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 155);
                }

                pos.setVisible(false);
                size++;
                remSize++;


            }
            for (int i = 0; i < beamImageSet.size(); i++) {
                plotsAnchorPane.getChildren().add(beamImageSet.get(i));
                ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(size + i);
                pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
                if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 200) {
                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 260);
                } else {
                    pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 195);
                }
                pos.setVisible(false);
                remSize++;
            }
        });

        peakCentreLinePlot.preparePanel();
        peakCentreGridPane.add(peakCentreLinePlot, 0, 0);
        int size = 1;
        for (int i = 0; i < gBeamImageSet.size(); i++) {
            plotsAnchorPane.getChildren().add(gBeamImageSet.get(i));
            ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
            pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
            if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 220) {
                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 300);
            } else {
                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 155);
            }

            pos.setVisible(false);
            size++;
            remSize++;


        }
        for (int i = 0; i < beamImageSet.size(); i++) {
            plotsAnchorPane.getChildren().add(beamImageSet.get(i));
            ImageView pos = (ImageView) plotsAnchorPane.getChildren().get(size + i);
            pos.setX(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[i]) - 35);
            if ((peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 160) < 220) {
                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 260);
            } else {
                pos.setY(peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[i]) + 195);
            }
            pos.setVisible(false);
            remSize++;
        }

        // Selects file from peakCentre plot
        peakCentreGridPane.setOnMouseClicked(click -> {
            peakCentreLinePlot.getOnMouseClicked();
            processDataFileAndShowPlotsOfPeakShapes();

            listViewOfResourcesInFolder.getSelectionModel().select(currentGroupIndex);
        });

        //int finalSize = size;

        peakCentreGridPane.setOnMouseMoved(mouse -> {
//            int index = (int) peakCentreLinePlot.convertMouseXToValue(mouse.getX());

//            if (peakCentreLinePlot.mouseInHouse(mouse.getX(), mouse.getY()) && index >= 1 && mouse.getY() > 10) {
//                for (int i = 0; i < peakCentreLinePlot.getxAxisData().length; i++) {
//                    ImageView pos1 = (ImageView) plotsAnchorPane.getChildren().get(finalSize + (i));
//                    pos1.setVisible(false);
//                    ImageView pos2 = (ImageView) plotsAnchorPane.getChildren().get(i + 1);
//                    pos2.setVisible(false);
//                    if (peakCentreLinePlot.getxAxisData()[i] == index) {
//                        pos1 = (ImageView) plotsAnchorPane.getChildren().get(finalSize + (index - 1));
//                        pos1.setVisible(true);
//                        pos2 = (ImageView) plotsAnchorPane.getChildren().get(index);
//                        pos2.setVisible(true);
//                    }
//
//                }
//
//            } else {
//                for (int i = 1; i < plotsAnchorPane.getChildren().size(); i++) {
//                    plotsAnchorPane.getChildren().get(i).setVisible(false);
//                }
//            }

        });
    }

    private void populateListOfResources(String groupValue) {
        listViewOfResourcesInFolder = new ListView<>();
        listViewOfResourcesInFolder.setCellFactory(param -> new PeakShapeDemoPlotsController.ResourceDisplayName2());
        eventLogTextArea.textProperty().unbind();
        int initialIndex;

        ObservableList<File> items = FXCollections.observableArrayList(resourceGroups.get(groupValue));
        listViewOfResourcesInFolder.setItems(items);


        listViewOfResourcesInFolder.setOnMouseClicked(click -> {
            peakCentreLinePlot.repaint();
            int index;
            if (1 == click.getClickCount()) {
                resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
                index = listViewOfResourcesInFolder.getSelectionModel().getSelectedIndex();
                peakCentreLinePlot.getGraphicsContext2D().setLineWidth(1.0);
                peakCentreLinePlot.getGraphicsContext2D().strokeOval(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[index]) - 6, peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[index]) - 6, 12, 12);
                processDataFileAndShowPlotsOfPeakShapes();
            }
        });

        listViewOfResourcesInFolder.setOnKeyPressed(key -> {
            peakCentreLinePlot.repaint();
            int index;
            if (KeyCode.DOWN == key.getCode() || KeyCode.UP == key.getCode()) {
                resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
                index = listViewOfResourcesInFolder.getSelectionModel().getSelectedIndex();
                processDataFileAndShowPlotsOfPeakShapes();
                peakCentreLinePlot.getGraphicsContext2D().setLineWidth(1.0);
                peakCentreLinePlot.getGraphicsContext2D().strokeOval(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[index]) - 6, peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[index]) - 6, 12, 12);
            }
        });

        listViewOfResourcesInFolder.getSelectionModel().selectFirst();
        initialIndex = listViewOfResourcesInFolder.getSelectionModel().getSelectedIndex();
        resourceBrowserTarget = listViewOfResourcesInFolder.getSelectionModel().getSelectedItem();
        peakCentreLinePlot.getGraphicsContext2D().setLineWidth(1.0);
        peakCentreLinePlot.getGraphicsContext2D().strokeOval(peakCentreLinePlot.mapX(peakCentreLinePlot.getxAxisData()[initialIndex]) - 6, peakCentreLinePlot.mapY(peakCentreLinePlot.getyAxisData()[initialIndex]) - 6, 12, 12);
        processDataFileAndShowPlotsOfPeakShapes();

        listViewOfResourcesInFolder.prefHeightProperty().bind(eventAnchorPane.heightProperty());
        listViewOfResourcesInFolder.prefWidthProperty().bind(eventAnchorPane.widthProperty());
        eventAnchorPane.getChildren().add(listViewOfResourcesInFolder);
    }


    public void processDataFileAndShowPlotsOfPeakShapes() {


        if (null != resourceBrowserTarget && resourceBrowserTarget.isFile()) {
            PeakShapesService service = new PeakShapesService(resourceBrowserTarget.toPath());
            eventLogTextArea.textProperty().bind(service.valueProperty());
            try {
                PlotBuilder[] plots = BeamDataOutputDriverExperiment.modelTest(resourceBrowserTarget.toPath(), this::processFilesAndShowPeakCentre);

                AbstractPlot gBeamLinePlot = GBeamLinePlot.generatePlot(
                        new Rectangle(gBeamPlotGridPane.getCellBounds(0, 0).getWidth(),
                                gBeamPlotGridPane.getCellBounds(0, 0).getHeight()),
                        (GBeamLinePlotBuilder) plots[1]
                );

                AbstractPlot beamShapeLinePlot = BeamShapeLinePlot.generatePlot(
                        new Rectangle(beamPlotsGridPane.getCellBounds(0, 0).getWidth(),
                                beamPlotsGridPane.getCellBounds(0, 0).getHeight()),
                        (BeamShapeLinePlotBuilder) plots[0]
                );

                gBeamPlotGridPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        gBeamLinePlot.setWidthF(newValue.intValue());
                        gBeamLinePlot.repaint();
                    }
                });

                gBeamPlotGridPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        gBeamLinePlot.setHeightF(newValue.intValue());
                        gBeamLinePlot.repaint();


                    }
                });


                beamPlotsGridPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        beamShapeLinePlot.setWidthF(newValue.intValue());
                        beamShapeLinePlot.repaint();
                    }
                });
                beamPlotsGridPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > 100) {
                        beamShapeLinePlot.setHeightF(newValue.intValue());
                        beamShapeLinePlot.repaint();

                    }
                });

                gBeamLinePlot.preparePanel();
                gBeamPlotGridPane.add(gBeamLinePlot, 0, 0);


                beamShapeLinePlot.preparePanel();
                beamPlotsGridPane.add(beamShapeLinePlot, 0, 0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            eventLogTextArea.textProperty().unbind();
            eventLogTextArea.setText("Please Choose Folder");
        }

    }

    static class ResourceDisplayName2 extends ListCell<File> {

        @Override
        protected void updateItem(File resource, boolean empty) {
            super.updateItem(resource, empty);
            if (null == resource || empty) {
                setText(null);
            } else {
                setText(resource.getName());
            }
        }
    }

    static class ResourceDisplayName extends ListCell<String> {

        @Override
        protected void updateItem(String resource, boolean empty) {

            super.updateItem(resource, empty);
            if (null == resource || empty) {
                setText(null);
            } else {
                setText(resource);
            }
        }
    }
}